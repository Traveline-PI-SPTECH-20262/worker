package school.sptech;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Config {
    private static Properties propriedades;

    public static String get(String chave) {
        if (System.getenv(chave) != null) {
            return System.getenv(chave).trim();
        }
        if (propriedades == null) {
            propriedades = new Properties();
            try (InputStream arquivo = new FileInputStream(".env")) {
                propriedades.load(arquivo);
            } catch (IOException e) {
                System.out.println("[CONFIG] Arquivo .env não encontrado: " + e.getMessage());
            }
        }
        return propriedades.getProperty(chave, "").trim();
    }
}
