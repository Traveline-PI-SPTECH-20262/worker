package org.example.leitura;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.example.ingestao.ArquivoRecebido;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * ETAPA 2 - LEITURA: é AQUI que o Apache POI trabalha.
 *
 * Hierarquia do POI (do maior para o menor):
 *
 *   Workbook  = o arquivo inteiro (.xlsx -> XSSFWorkbook, .xls -> HSSFWorkbook)
 *     Sheet   = uma aba (ex.: "Brasil_1.1")
 *       Row   = uma linha da aba (índice começa em 0; linha 1 do Excel = row 0)
 *         Cell = uma célula da linha (índice de coluna começa em 0; A = 0, B = 1 ...)
 *
 * Esta classe só converte Workbook/Sheet/Row/Cell em {@link LinhaPlanilha}.
 * Ela NÃO sabe o que cada coluna significa; isso é papel do mapeamento.
 */
public class LeitorPlanilha {

    private static final Logger log = LoggerFactory.getLogger(LeitorPlanilha.class);

    public List<LinhaPlanilha> ler(ArquivoRecebido arquivo, String nomeAba) {
        List<LinhaPlanilha> linhas = new ArrayList<>();

        // [POI] WORKBOOK: WorkbookFactory detecta sozinho se é .xls ou .xlsx
        try (InputStream in = arquivo.abrir();
             Workbook workbook = WorkbookFactory.create(in)) {

            log.info("[POI][WORKBOOK] Aberto '{}' ({}) com {} aba(s)",
                    arquivo.nome(), workbook.getClass().getSimpleName(), workbook.getNumberOfSheets());
            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                log.debug("[POI][WORKBOOK]   aba {}: '{}'", i, workbook.getSheetName(i));
            }

            // [POI] SHEET: pega a aba pelo nome (também existe getSheetAt(indice))
            Sheet sheet = workbook.getSheet(nomeAba);
            if (sheet == null) {
                log.error("[POI][SHEET] Aba '{}' não existe neste arquivo", nomeAba);
                return linhas;
            }
            log.info("[POI][SHEET] Lendo aba '{}' (linhas {} a {})",
                    sheet.getSheetName(), sheet.getFirstRowNum() + 1, sheet.getLastRowNum() + 1);

            // [POI] ROW: o for-each pula linhas que não existem fisicamente no arquivo
            for (Row row : sheet) {
                List<Object> celulas = lerCelulas(row);
                if (celulas.stream().allMatch(v -> v == null)) {
                    continue; // linha visualmente vazia
                }
                LinhaPlanilha linha = new LinhaPlanilha(row.getRowNum() + 1, celulas);
                log.debug("[POI][ROW] Linha {}: {}", linha.numeroLinha(), celulas);
                linhas.add(linha);
            }

        } catch (IOException e) {
            log.error("[POI][WORKBOOK] Falha ao abrir '{}': arquivo corrompido ou formato inválido", arquivo.nome(), e);
        }

        log.info("[POI] {} linha(s) com conteúdo lida(s)", linhas.size());
        return linhas;
    }

    /** [POI] CELL: percorre as colunas da linha, inclusive as vazias no meio. */
    private List<Object> lerCelulas(Row row) {
        List<Object> valores = new ArrayList<>();
        // getLastCellNum() já é "último índice + 1"; retorna -1 se a linha não tem células
        for (int col = 0; col < row.getLastCellNum(); col++) {
            // RETURN_BLANK_AS_NULL: célula vazia vem como null em vez de Cell BLANK
            Cell cell = row.getCell(col, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
            valores.add(lerValor(cell));
        }
        return valores;
    }

    /**
     * [POI] CELL: converte o conteúdo da célula para tipo Java.
     * O POI exige saber o tipo antes de ler: chamar getNumericCellValue()
     * numa célula de texto lança exceção.
     */
    private Object lerValor(Cell cell) {
        if (cell == null) {
            return null;
        }
        CellType tipo = cell.getCellType();
        if (tipo == CellType.FORMULA) {
            // Para fórmulas usamos o último resultado calculado salvo pelo Excel
            tipo = cell.getCachedFormulaResultType();
        }

        return switch (tipo) {
            case STRING -> {
                String texto = cell.getStringCellValue();
                yield texto.isBlank() ? null : texto;
            }
            // Datas no Excel são números; DateUtil diz se a célula está formatada como data
            case NUMERIC -> DateUtil.isCellDateFormatted(cell)
                    ? cell.getLocalDateTimeCellValue()
                    : (Object) cell.getNumericCellValue();
            case BOOLEAN -> cell.getBooleanCellValue();
            case ERROR -> {
                log.warn("[POI][CELL] Erro de fórmula em {}", cell.getAddress());
                yield null;
            }
            default -> null; // BLANK, _NONE
        };
    }
}
