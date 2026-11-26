package school.sptech;

import java.sql.Connection;
import software.amazon.awssdk.services.s3.S3Client;

public class Main {
    public static void main(String[] args) throws Exception {
        Log.info(Main.class, "[MAIN] Iniciando worker");

        String bucket = Config.get("S3_BUCKET");
        if (bucket.isEmpty()) {
            Log.info(Main.class, "[MAIN] S3_BUCKET vazio no .env, nada a processar");
            return;
        }

        try (
              S3Client s3Client = new S3Provider().getS3Client();
              Connection conexao = Conexao.getConexao()
        ) {
            Log.setConexao(conexao);
            Worker worker = new Worker(
                  new LeitorS3(s3Client, bucket),
                  new AgenciaDao(conexao),
                  new PlanilhaDao(conexao),
                  new TuristaDao(conexao),
                  new RecomendacaoDao(conexao),
                  new UsuarioDao(conexao));

            try {
                worker.executar();
            } catch (Exception e) {
                Log.erro(Main.class, "[MAIN] Worker interrompido por erro: " + e.getMessage());
                throw e;
            }
            Log.info(Main.class, "[MAIN] Worker finalizado");
        }
    }
}
