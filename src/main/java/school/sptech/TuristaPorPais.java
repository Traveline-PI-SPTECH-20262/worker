package school.sptech;

public class TuristaPorPais {
    private String pais;
    private String continente;
    private String meio;
    private Integer ano;
    private String mes;
    private Integer quantidade;

    public TuristaPorPais() {
    }

    public TuristaPorPais(String pais, String continente, String meio, Integer ano, String mes, Integer quantidade) {
        this.pais = pais;
        this.continente = continente;
        this.meio = meio;
        this.ano = ano;
        this.mes = mes;
        this.quantidade = quantidade;
    }

    public String getPais() {
        return pais;
    }

    public void setPais(String pais) {
        this.pais = pais;
    }

    public String getContinente() {
        return continente;
    }

    public void setContinente(String continente) {
        this.continente = continente;
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
        return "TuristaPorPais{" +
              "pais='" + pais + '\'' +
              ", continente='" + continente + '\'' +
              ", meio='" + meio + '\'' +
              ", ano=" + ano +
              ", mes='" + mes + '\'' +
              ", quantidade=" + quantidade +
              '}';
    }
}
