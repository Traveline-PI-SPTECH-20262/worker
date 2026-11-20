package school.sptech;

public class Agencia {
    private Integer idAgencia;
    private String nome;
    private String email;

    public Agencia() {
    }

    public Agencia(Integer idAgencia, String nome, String email) {
        this.idAgencia = idAgencia;
        this.nome = nome;
        this.email = email;
    }

    public Integer getIdAgencia() {
        return idAgencia;
    }

    public void setIdAgencia(Integer idAgencia) {
        this.idAgencia = idAgencia;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String toString() {
        return "Agencia{idAgencia=" + idAgencia + ", nome='" + nome + "', email='" + email + "'}";
    }
}
