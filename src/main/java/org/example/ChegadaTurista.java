package org.example;


public class ChegadaTurista {

    String continente;
    String pais;
    String viaAcesso;
    int ano;
    long quantidade;

    public ChegadaTurista(String continente, String pais, String viaAcesso, int ano, long quantidade) {
        this.continente = continente;
        this.pais = pais;
        this.viaAcesso = viaAcesso;
        this.ano = ano;
        this.quantidade = quantidade;
    }

    @Override
    public String toString() {
        return continente + " | " + pais + " | " + viaAcesso + " | " + ano + " | " + quantidade;
    }
}
