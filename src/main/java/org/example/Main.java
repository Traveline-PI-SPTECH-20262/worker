package org.example;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;

public class Main {

    // true = busca a planilha no e-mail; false = lê o arquivo da raiz do projeto
    static boolean USAR_EMAIL = false;
    static String ARQUIVO = "01. a 03. Chegadas de turistas internacionais ao Brasil__A2022-2021-V49-1ed.xlsx";

    public static void main(String[] args) throws Exception {
        System.out.println("[MAIN] Iniciando worker");

        // 1. PEGAR O ARQUIVO
        InputStream arquivo;
        if (USAR_EMAIL) {
            arquivo = new LeitorEmail().buscarPlanilha();
        } else {
            System.out.println("[MAIN] Lendo arquivo do disco: " + ARQUIVO);
            arquivo = new FileInputStream(ARQUIVO);
        }

        // 2. LER A PLANILHA COM APACHE POI
        List<ChegadaTurista> chegadas = new LeitorPlanilha().ler(arquivo);

        // 3. VALIDAR E 4. GRAVAR
        Validador validador = new Validador();
        ChegadaTuristaDao dao = new ChegadaTuristaDao();
        for (ChegadaTurista chegada : chegadas) {
            if (validador.valido(chegada)) {
                dao.inserir(chegada);
            }
        }

        System.out.println("[MAIN] Worker finalizado");
    }
}
