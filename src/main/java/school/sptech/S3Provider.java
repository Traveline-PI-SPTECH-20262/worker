package school.sptech;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.AwsSessionCredentials;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

// Cria o cliente do S3 (baseado no SPTECH-LP/bucket-s3-exemplo-v2),
// lendo as credenciais sempre do .env.
public class S3Provider {
    private final AwsCredentialsProvider credentials;

    public S3Provider() {
        String accessKey = Config.get("AWS_ACCESS_KEY_ID");
        String secretKey = Config.get("AWS_SECRET_ACCESS_KEY");
        String sessionToken = Config.get("AWS_SESSION_TOKEN");

        if (accessKey.isEmpty() || secretKey.isEmpty()) {
            // Sem chaves no .env: usa a cadeia padrão da AWS (ex.: role da EC2)
            System.out.println("[S3] Chaves da AWS vazias no .env, usando DefaultCredentialsProvider");
            this.credentials = DefaultCredentialsProvider.create();
        } else if (sessionToken.isEmpty()) {
            this.credentials = StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey));
        } else {
            // AWS Academy (Learner Lab) usa credenciais temporárias, com session token
            this.credentials = StaticCredentialsProvider.create(
                  AwsSessionCredentials.create(accessKey, secretKey, sessionToken));
        }
    }

    public S3Client getS3Client() {
        String regiao = Config.get("AWS_REGION");
        return S3Client.builder()
              .region(regiao.isEmpty() ? Region.US_EAST_1 : Region.of(regiao))
              .credentialsProvider(credentials)
              .build();
    }
}
