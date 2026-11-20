package school.sptech;

import java.sql.Connection;
import software.amazon.awssdk.services.s3.S3Client;

public class Main {
    public static void main(String[] args) throws Exception {
        System.out.println("[MAIN] Iniciando worker");

        String bucket = Config.get("S3_BUCKET");
        if (bucket.isEmpty()) {
            System.out.println("[MAIN] S3_BUCKET vazio no .env, nada a processar");
            return;
        }

        try (
              S3Client s3Client = new S3Provider().getS3Client();
              Connection conexao = Conexao.getConexao()
        ) {
            Worker worker = new Worker(
                  new LeitorS3(s3Client, bucket),
                  new AgenciaDao(conexao),
                  new PlanilhaDao(conexao),
                  new TuristaDao(conexao),
                  new RecomendacaoDao(conexao));

            worker.executar();
        }

        System.out.println("[MAIN] Worker finalizado");
    }
}
