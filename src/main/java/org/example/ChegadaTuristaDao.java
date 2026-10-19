package org.example;


public class ChegadaTuristaDao {

    public void inserir(ChegadaTurista c) {
        System.out.println("[CRUD] INSERT INTO chegada_turista (continente, pais, via_acesso, ano, quantidade) VALUES ('"
                + c.continente + "', '" + c.pais + "', '" + c.viaAcesso + "', " + c.ano + ", " + c.quantidade + ")");
    }

    public void buscar(String pais) {
        System.out.println("[CRUD] SELECT * FROM chegada_turista WHERE pais = '" + pais + "'");
    }

    public void atualizar(int id, long quantidade) {
        System.out.println("[CRUD] UPDATE chegada_turista SET quantidade = " + quantidade + " WHERE id = " + id);
    }

    public void deletar(int id) {
        System.out.println("[CRUD] DELETE FROM chegada_turista WHERE id = " + id);
    }
}
