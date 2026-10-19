package org.example;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

// Lê a planilha com Apache POI e transforma cada linha em objetos ChegadaTurista
//
// Hierarquia do Apache POI:
//   Workbook = o arquivo .xlsx inteiro
//   Sheet    = uma aba
//   Row      = uma linha (começa em 0: linha 1 do Excel = row 0)
//   Cell     = uma célula (começa em 0: coluna A = 0, B = 1, C = 2...)
public class LeitorPlanilha {

    // Aba usada da planilha do Anuário de Turismo
    String ABA = "Brasil_1.1";

    // Coluna B = nome do continente ou país
    int COLUNA_NOME = 1;

    // Cada via ocupa 3 colunas seguidas (2019, 2020, 2021)
    String[] VIAS = {"TOTAL", "AEREA", "TERRESTRE", "MARITIMA", "FLUVIAL"};
    int[] COLUNA_INICIAL_DA_VIA = {3, 7, 11, 15, 19}; // D, H, L, P, T
    int[] ANOS = {2019, 2020, 2021};

    public List<ChegadaTurista> ler(InputStream arquivo) throws Exception {
        List<ChegadaTurista> chegadas = new ArrayList<>();

        // [POI] WORKBOOK: abre o arquivo .xlsx
        Workbook workbook = new XSSFWorkbook(arquivo);
        System.out.println("[POI] Workbook aberto com " + workbook.getNumberOfSheets() + " abas");

        // [POI] SHEET: pega a aba pelo nome
        Sheet sheet = workbook.getSheet(ABA);
        System.out.println("[POI] Lendo aba " + sheet.getSheetName());

        String continenteAtual = null;

        // [POI] ROW: percorre cada linha da aba
        for (Row row : sheet) {

            // [POI] CELL: lê as células da linha
            Cell celulaNome = row.getCell(COLUNA_NOME);
            Cell celulaPrimeiroValor = row.getCell(3); // coluna D

            // Pula título, cabeçalho e rodapé: linhas sem número na coluna D
            if (celulaNome == null || celulaPrimeiroValor == null
                    || celulaPrimeiroValor.getCellType() != CellType.NUMERIC) {
                continue;
            }

            String nomeOriginal = celulaNome.getStringCellValue();
            String nome = nomeOriginal.trim();

            // Linha dos anos (2019, 2020, 2021) tem número na coluna D mas não tem nome
            if (nome.isEmpty()) {
                continue;
            }

            // "Brasil" é o total geral, não é país
            if (nome.equals("Brasil")) {
                continue;
            }

            // Continentes vêm com espaços na frente: só guarda e vai para a próxima linha
            if (nomeOriginal.startsWith(" ")) {
                continenteAtual = nome;
                System.out.println("[POI] Continente: " + continenteAtual);
                continue;
            }

            // É um país: cria um objeto para cada via x ano
            for (int v = 0; v < VIAS.length; v++) {
                for (int a = 0; a < ANOS.length; a++) {
                    int coluna = COLUNA_INICIAL_DA_VIA[v] + a;
                    Cell celula = row.getCell(coluna);
                    // getNumericCellValue() só funciona em célula numérica
                    long quantidade = (long) celula.getNumericCellValue();
                    chegadas.add(new ChegadaTurista(continenteAtual, nome, VIAS[v], ANOS[a], quantidade));
                }
            }
        }

        workbook.close();
        System.out.println("[POI] " + chegadas.size() + " registros lidos");
        return chegadas;
    }
}
