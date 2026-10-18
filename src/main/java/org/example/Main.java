package org.example;

import org.example.config.ConfiguracaoWorker;
import org.example.ingestao.FonteArquivo;
import org.example.ingestao.FonteArquivoEmail;
import org.example.ingestao.FonteArquivoLocal;
import org.example.leitura.LeitorPlanilha;
import org.example.mapeamento.MapeadorChegadas;
import org.example.persistencia.ChegadaTuristaRepository;
import org.example.persistencia.ChegadaTuristaRepositoryLog;
import org.example.validacao.ValidadorChegadas;
import org.example.worker.ProcessadorPlanilhaWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Ponto de entrada do worker.
 *
 * Aqui só montamos as peças (injeção de dependência "na mão", sem Spring)
 * e mandamos o orquestrador executar. A ordem do fluxo está em
 * {@link ProcessadorPlanilhaWorker#executar()}.
 */
public class Main {

    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        log.info("[MAIN] Iniciando worker de planilhas");

        ConfiguracaoWorker config = ConfiguracaoWorker.carregar();
        log.info("[MAIN] Configuração: {}", config);

        // 1) De onde vem o arquivo: e-mail (produção) ou disco (desenvolvimento)
        FonteArquivo fonte = config.modo() == ConfiguracaoWorker.Modo.EMAIL
                ? new FonteArquivoEmail(config)
                : new FonteArquivoLocal(config.caminhoArquivoLocal());

        // 2..5) Leitura com POI -> mapeamento -> validação -> persistência
        LeitorPlanilha leitor = new LeitorPlanilha();
        MapeadorChegadas mapeador = new MapeadorChegadas();
        ValidadorChegadas validador = new ValidadorChegadas();
        ChegadaTuristaRepository repositorio = new ChegadaTuristaRepositoryLog();

        ProcessadorPlanilhaWorker worker = new ProcessadorPlanilhaWorker(
                fonte, leitor, mapeador, validador, repositorio, config.nomeAba());

        worker.executar();

        log.info("[MAIN] Worker finalizado");
    }
}
