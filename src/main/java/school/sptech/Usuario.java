package school.sptech;

public class Usuario {
    private Integer idUsuario;
    private String nome;
    private String email;
    private boolean receberEmail;

    public Usuario(Integer idUsuario, String nome, String email, boolean receberEmail) {
        this.idUsuario = idUsuario;
        this.nome = nome;
        this.email = email;
        this.receberEmail = receberEmail;
    }

    public Integer getIdUsuario() {
        return idUsuario;
    }

    public String getNome() {
        return nome;
    }

    public String getEmail() {
        return email;
    }

    public boolean isReceberEmail() {
        return receberEmail;
    }
}
