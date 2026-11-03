package school.sptech;

public class Recomendacao {
    private String tipo;
    private Integer idLocal;
    private String nome;
    private Integer totalTuristas;
    private Double pontuacao;
    private String nivel;
    private String sazonalidade;

    public Recomendacao() {
    }

    public Recomendacao(String tipo, Integer idLocal, String nome, Integer totalTuristas, Double pontuacao,
          String nivel, String sazonalidade) {
        this.tipo = tipo;
        this.idLocal = idLocal;
        this.nome = nome;
        this.totalTuristas = totalTuristas;
        this.pontuacao = pontuacao;
        this.nivel = nivel;
        this.sazonalidade = sazonalidade;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public Integer getIdLocal() {
        return idLocal;
    }

    public void setIdLocal(Integer idLocal) {
        this.idLocal = idLocal;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public Integer getTotalTuristas() {
        return totalTuristas;
    }

    public void setTotalTuristas(Integer totalTuristas) {
        this.totalTuristas = totalTuristas;
    }

    public Double getPontuacao() {
        return pontuacao;
    }

    public void setPontuacao(Double pontuacao) {
        this.pontuacao = pontuacao;
    }

    public String getNivel() {
        return nivel;
    }

    public void setNivel(String nivel) {
        this.nivel = nivel;
    }

    public String getSazonalidade() {
        return sazonalidade;
    }

    public void setSazonalidade(String sazonalidade) {
        this.sazonalidade = sazonalidade;
    }

    @Override
    public String toString() {
        return "Recomendacao{" +
              "tipo='" + tipo + '\'' +
              ", nome='" + nome + '\'' +
              ", totalTuristas=" + totalTuristas +
              ", pontuacao=" + pontuacao +
              ", nivel='" + nivel + '\'' +
              ", sazonalidade='" + sazonalidade + '\'' +
              '}';
    }
}
