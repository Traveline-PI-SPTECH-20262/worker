package school.sptech;

// Um arquivo (objeto) do bucket S3.
// O nome segue o padrão "<idAgencia>-<qualquer coisa>.xlsx": o número antes do
// primeiro "-" diz de qual agência é a planilha.
public class ArquivoS3 {
    private String chave;

    public ArquivoS3(String chave) {
        this.chave = chave;
    }

    // Caminho completo dentro do bucket (ex.: "planilhas/2-chegadas.xlsx")
    public String getChave() {
        return chave;
    }

    // Só o nome do arquivo, sem as "pastas" do bucket (ex.: "2-chegadas.xlsx")
    public String getNome() {
        return chave.substring(chave.lastIndexOf('/') + 1);
    }

    // Id da agência: o que vem antes do primeiro "-" do nome.
    // Retorna null se o nome não seguir o padrão (sem "-" ou sem número antes dele).
    public Integer getIdAgencia() {
        String nome = getNome();
        int traco = nome.indexOf('-');
        if (traco <= 0) {
            return null;
        }
        try {
            return Integer.valueOf(nome.substring(0, traco).trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @Override
    public String toString() {
        return chave;
    }
}
