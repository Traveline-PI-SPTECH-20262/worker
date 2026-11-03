package school.sptech;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class LeitorExcel {
    private static final String[] ABAS_PAIS = {"Brasil_1.3", "Brasil_1.4", "Brasil_1.5", "Brasil_1.6"};

    private static final String[] ABAS_ESTADO = {"SÍNTESE BRASIL_3.4", "SINTESE BRASIL_3.5-3.6", "SINTESE BRASIL_3.7"};

    private static final int[] ANOS = {2019, 2020, 2021};

    private static final List<String> MESES = List.of("Janeiro", "Fevereiro", "Março", "Abril", "Maio", "Junho",
          "Julho", "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro");

    private static final Map<String, String> SIGLAS = Map.ofEntries(
          Map.entry("Acre", "AC"), Map.entry("Alagoas", "AL"), Map.entry("Amapá", "AP"),
          Map.entry("Amazonas", "AM"), Map.entry("Bahia", "BA"), Map.entry("Ceará", "CE"),
          Map.entry("Distrito Federal", "DF"), Map.entry("Espírito Santo", "ES"), Map.entry("Goiás", "GO"),
          Map.entry("Maranhão", "MA"), Map.entry("Mato Grosso", "MT"), Map.entry("Mato Grosso do Sul", "MS"),
          Map.entry("Minas Gerais", "MG"), Map.entry("Pará", "PA"), Map.entry("Paraíba", "PB"),
          Map.entry("Paraná", "PR"), Map.entry("Pernambuco", "PE"), Map.entry("Piauí", "PI"),
          Map.entry("Rio de Janeiro", "RJ"), Map.entry("Rio Grande do Norte", "RN"),
          Map.entry("Rio Grande do Sul", "RS"), Map.entry("Rondônia", "RO"), Map.entry("Roraima", "RR"),
          Map.entry("Santa Catarina", "SC"), Map.entry("São Paulo", "SP"), Map.entry("Sergipe", "SE"),
          Map.entry("Tocantins", "TO"));

    public List<TuristaPorPais> extrairTuristasPorPais(String nomeArquivo) {
        List<TuristaPorPais> turistas = new ArrayList<>();

        try (
              InputStream arquivo = new FileInputStream(nomeArquivo);
              Workbook workbook = new XSSFWorkbook(arquivo)
        ) {
            for (String aba : ABAS_PAIS) {
                Sheet sheet = workbook.getSheet(aba);
                System.out.println("[POI] Lendo aba " + aba);

                String meio = null;
                String continente = null;
                Row cabecalhoMeses = null;

                for (Row row : sheet) {
                    String colunaB = texto(row.getCell(1));

                    if (colunaB.startsWith("1.")) {
                        meio = meioDoTitulo(colunaB);
                        continue;
                    }

                    String colunaD = texto(row.getCell(3));
                    if (colunaD.equals("Total") || MESES.contains(colunaD)) {
                        cabecalhoMeses = row;
                        continue;
                    }

                    if (!ehNumero(row.getCell(3))) {
                        continue;
                    }

                    String nome = colunaB.trim();

                    if (nome.isEmpty() || nome.equals("Brasil")) {
                        continue;
                    }

                    if (colunaB.startsWith(" ")) {
                        continente = nome;
                        continue;
                    }

                    if (nome.length() > 35) {
                        nome = nome.replace("Outros países da ", "Outros da ");
                    }

                    for (int coluna = 3; coluna < cabecalhoMeses.getLastCellNum(); coluna += 4) {
                        String mes = texto(cabecalhoMeses.getCell(coluna));
                        if (!MESES.contains(mes)) {
                            continue;
                        }
                        for (int a = 0; a < ANOS.length; a++) {
                            Integer quantidade = numero(row.getCell(coluna + a));
                            turistas.add(new TuristaPorPais(nome, continente, meio, ANOS[a], mes, quantidade));
                        }
                    }
                }
            }

            System.out.println("[POI] " + turistas.size() + " registros de turistas por país lidos");
            return turistas;
        } catch (IOException e) {
            System.out.println("[POI] Erro ao ler " + nomeArquivo + ": " + e.getMessage());
            return turistas;
        }
    }

    public List<TuristaPorEstado> extrairTuristasPorEstado(String nomeArquivo) {
        List<TuristaPorEstado> turistas = new ArrayList<>();

        try (
              InputStream arquivo = new FileInputStream(nomeArquivo);
              Workbook workbook = new XSSFWorkbook(arquivo)
        ) {
            for (String aba : ABAS_ESTADO) {
                Sheet sheet = workbook.getSheet(aba);
                System.out.println("[POI] Lendo aba " + aba);

                String meio = null;
                Row cabecalhoEstados = null;

                for (Row row : sheet) {
                    String colunaB = texto(row.getCell(1));

                    if (colunaB.startsWith("3.")) {
                        meio = meioDoTitulo(colunaB);
                        continue;
                    }

                    if (colunaB.equals("Brasil")) {
                        cabecalhoEstados = sheet.getRow(row.getRowNum() - 4);
                        continue;
                    }

                    if (!MESES.contains(colunaB)) {
                        continue;
                    }

                    for (int coluna = 3; coluna < cabecalhoEstados.getLastCellNum(); coluna += 4) {
                        String estado = texto(cabecalhoEstados.getCell(coluna));
                        String sigla = SIGLAS.get(estado);
                        if (sigla == null) {
                            continue;
                        }
                        for (int a = 0; a < ANOS.length; a++) {
                            Integer quantidade = numero(row.getCell(coluna + a));
                            turistas.add(new TuristaPorEstado(estado, sigla, meio, ANOS[a], colunaB, quantidade));
                        }
                    }
                }
            }

            System.out.println("[POI] " + turistas.size() + " registros de turistas por estado lidos");
            return turistas;
        } catch (IOException e) {
            System.out.println("[POI] Erro ao ler " + nomeArquivo + ": " + e.getMessage());
            return turistas;
        }
    }

    private String meioDoTitulo(String titulo) {
        String t = titulo.toLowerCase();
        if (t.contains("via aérea")) {
            return "Avião";
        }
        if (t.contains("via terrestre")) {
            return "Ônibus";
        }
        if (t.contains("via marítima")) {
            return "Navio";
        }
        if (t.contains("via fluvial")) {
            return "Barco";
        }
        return null;
    }

    private String texto(Cell cell) {
        if (cell == null || cell.getCellType() != CellType.STRING) {
            return "";
        }
        return cell.getStringCellValue();
    }

    private boolean ehNumero(Cell cell) {
        return cell != null && cell.getCellType() == CellType.NUMERIC;
    }

    private Integer numero(Cell cell) {
        if (!ehNumero(cell)) {
            return 0;
        }
        return (int) cell.getNumericCellValue();
    }
}
