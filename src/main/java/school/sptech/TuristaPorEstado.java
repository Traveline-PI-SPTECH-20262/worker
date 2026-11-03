package school.sptech;

public class TuristaPorEstado {
    private String estado;
    private String sigla;
    private String meio;
    private Integer ano;
    private String mes;
    private Integer quantidade;

    public TuristaPorEstado() {
    }

    public TuristaPorEstado(String estado, String sigla, String meio, Integer ano, String mes, Integer quantidade) {
        this.estado = estado;
        this.sigla = sigla;
        this.meio = meio;
        this.ano = ano;
        this.mes = mes;
        this.quantidade = quantidade;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getSigla() {
        return sigla;
    }

    public void setSigla(String sigla) {
        this.sigla = sigla;
    }

    public String getMeio() {
        return meio;
    }

    public void setMeio(String meio) {
        this.meio = meio;
    }

    public Integer getAno() {
        return ano;
    }

    public void setAno(Integer ano) {
        this.ano = ano;
    }

    public String getMes() {
        return mes;
    }

    public void setMes(String mes) {
        this.mes = mes;
    }

    public Integer getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(Integer quantidade) {
        this.quantidade = quantidade;
    }

    @Override
    public String toString() {
        return "TuristaPorEstado{" +
              "estado='" + estado + '\'' +
              ", sigla='" + sigla + '\'' +
              ", meio='" + meio + '\'' +
              ", ano=" + ano +
              ", mes='" + mes + '\'' +
              ", quantidade=" + quantidade +
              '}';
    }
}
