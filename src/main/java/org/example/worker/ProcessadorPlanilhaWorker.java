package org.example.worker;

import org.example.dominio.ChegadaTurista;
import org.example.ingestao.ArquivoRecebido;
import org.example.ingestao.FonteArquivo;
import org.example.leitura.LeitorPlanilha;
import org.example.leitura.LinhaPlanilha;
import org.example.mapeamento.MapeadorChegadas;
import org.example.persistencia.ChegadaTuristaRepository;
import org.example.validacao.ResultadoValidacao;
import org.example.validacao.ValidadorChegadas;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Orquestrador: chama as etapas na ordem. Leia este método para entender o fluxo inteiro.
 *
 *   1. INGESTÃO     FonteArquivo            -> List<ArquivoRecebido>
 *   2. LEITURA      LeitorPlanilha (POI)    -> List<LinhaPlanilha>
 *   3. MAPEAMENTO   MapeadorChegadas        -> List<ChegadaTurista>
 *   4. VALIDAÇÃO    ValidadorChegadas       -> ResultadoValidacao
 *   5. PERSISTÊNCIA ChegadaTuristaRepository (hoje só log)
 */
public class ProcessadorPlanilhaWorker {

    private static final Logger log = LoggerFactory.getLogger(ProcessadorPlanilhaWorker.class);

    private final FonteArquivo fonte;
    private final LeitorPlanilha leitor;
    private final MapeadorChegadas mapeador;
    private final ValidadorChegadas validador;
    private final ChegadaTuristaRepository repositorio;
    private final String nomeAba;

    public ProcessadorPlanilhaWorker(FonteArquivo fonte, LeitorPlanilha leitor, MapeadorChegadas mapeador,
                                     ValidadorChegadas validador, ChegadaTuristaRepository repositorio,
                                     String nomeAba) {
        this.fonte = fonte;
        this.leitor = leitor;
        this.mapeador = mapeador;
        this.validador = validador;
        this.repositorio = repositorio;
        this.nomeAba = nomeAba;
    }

    public void executar() {
        log.info("[WORKER] ===== 1. INGESTÃO =====");
        List<ArquivoRecebido> arquivos = fonte.buscarArquivos();
        if (arquivos.isEmpty()) {
            log.info("[WORKER] Nenhum arquivo para processar");
            return;
        }

        for (ArquivoRecebido arquivo : arquivos) {
            // Um arquivo com problema não impede o processamento dos outros
            try {
                processar(arquivo);
            } catch (RuntimeException e) {
                log.error("[WORKER] Falha ao processar '{}'", arquivo.nome(), e);
            }
        }
    }

    private void processar(ArquivoRecebido arquivo) {
        log.info("[WORKER] Processando '{}' (origem: {})", arquivo.nome(), arquivo.origem());

        log.info("[WORKER] ===== 2. LEITURA (Apache POI) =====");
        List<LinhaPlanilha> linhas = leitor.ler(arquivo, nomeAba);

        log.info("[WORKER] ===== 3. MAPEAMENTO =====");
        List<ChegadaTurista> chegadas = mapeador.mapear(linhas);

        log.info("[WORKER] ===== 4. VALIDAÇÃO =====");
        ResultadoValidacao resultado = validador.validar(chegadas);

        log.info("[WORKER] ===== 5. PERSISTÊNCIA (log) =====");
        repositorio.salvarTodos(resultado.validos());

        log.info("[WORKER] '{}' concluído: {} gravado(s), {} rejeitado(s)",
                arquivo.nome(), resultado.validos().size(), resultado.rejeitados().size());
    }
}
