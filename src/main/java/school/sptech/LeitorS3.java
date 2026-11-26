package school.sptech;

import java.util.ArrayList;
import java.util.List;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.S3Object;

public class LeitorS3 {
    private S3Client s3Client;
    private String bucket;

    public LeitorS3(S3Client s3Client, String bucket) {
        this.s3Client = s3Client;
        this.bucket = bucket;
    }

    // Lista todas as planilhas .xlsx do bucket.
    // O paginator percorre o bucket inteiro, mesmo com mais de 1000 objetos.
    public List<ArquivoS3> listarArquivos() {
        List<ArquivoS3> arquivos = new ArrayList<>();
        ListObjectsV2Request requisicao = ListObjectsV2Request.builder()
              .bucket(bucket)
              .build();

        for (S3Object objeto : s3Client.listObjectsV2Paginator(requisicao).contents()) {
            if (objeto.key().toLowerCase().endsWith(".xlsx")) {
                arquivos.add(new ArquivoS3(objeto.key()));
            } else {
                Log.info(LeitorS3.class, "[S3] Ignorado (não é .xlsx): " + objeto.key());
            }
        }
        Log.info(LeitorS3.class, "[S3] " + arquivos.size() + " planilha(s) no bucket " + bucket);
        return arquivos;
    }

    // Baixa o conteúdo do arquivo para a memória (o LeitorExcel abre a planilha mais de uma vez).
    public byte[] baixar(ArquivoS3 arquivo) {
        GetObjectRequest requisicao = GetObjectRequest.builder()
              .bucket(bucket)
              .key(arquivo.getChave())
              .build();
        byte[] conteudo = s3Client.getObjectAsBytes(requisicao).asByteArray();
        Log.info(LeitorS3.class, "[S3] Baixado " + arquivo.getChave() + " (" + conteudo.length + " bytes)");
        return conteudo;
    }
}
