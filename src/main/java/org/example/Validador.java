package org.example;

// Regras que o dado precisa cumprir antes de ir para o banco
public class Validador {

    public boolean valido(ChegadaTurista chegada) {
        if (chegada.continente == null) {
            System.out.println("[VALIDACAO] Rejeitado, sem continente: " + chegada);
            return false;
        }
        if (chegada.quantidade < 0) {
            System.out.println("[VALIDACAO] Rejeitado, quantidade negativa: " + chegada);
            return false;
        }
        return true;
    }
}
