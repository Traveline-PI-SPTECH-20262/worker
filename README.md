# Worker de planilhas (Java puro + Apache POI + Jakarta Mail)

Worker simples que pega uma planilha Excel, lê com o Apache POI, valida os dados e "grava" no banco. Por enquanto a gravação é só um log com o SQL que seria executado.

Planilha usada: `01. a 03. Chegadas de turistas internacionais ao Brasil__A2022-2021-V49-1ed.xlsx` (raiz do projeto), aba `Brasil_1.1`.

## Como rodar

Execute a classe `Main` no IntelliJ.

## Classes

Todas ficam em `src/main/java/org/example/`:

| Classe | O que faz |
|---|---|
| `Main` | Chama as etapas na ordem. Comece a leitura por ela. |
| `LeitorEmail` | Busca o anexo `.xlsx` no e-mail (Jakarta Mail). |
| `LeitorPlanilha` | Lê a planilha com Apache POI e cria os objetos `ChegadaTurista`. |
| `ChegadaTurista` | O dado que vai para o banco (continente, país, via, ano, quantidade). |
| `Validador` | Diz se um registro pode ir para o banco. |
| `ChegadaTuristaDao` | CRUD. Hoje só imprime o SQL no console. |

## O fluxo

```
e-mail ou arquivo do disco
        ▼
LeitorPlanilha   (Apache POI: Workbook -> Sheet -> Row -> Cell)
        ▼
List<ChegadaTurista>
        ▼
Validador        (válido? se não, loga e descarta)
        ▼
ChegadaTuristaDao.inserir()   (hoje: só o log do INSERT)
```

### 1. Pegar o arquivo

No `Main`, a variável `USAR_EMAIL` escolhe a origem:

- `false` (padrão): abre o `.xlsx` da raiz do projeto com `FileInputStream`.
- `true`: usa o `LeitorEmail`. Preencha `host`, `usuario` e `senha` nele (no Gmail, use uma "senha de app"). O caminho no Jakarta Mail é `Session` → `Store` (conexão) → `Folder` (INBOX) → `Message` (e-mail) → `Multipart` (partes do e-mail) → `Part` (o anexo). O primeiro anexo `.xlsx` encontrado, do e-mail mais novo para o mais antigo, é devolvido como `InputStream`.

Nos dois casos, o resultado é um `InputStream` que vai para o `LeitorPlanilha`.

### 2. Ler a planilha (Apache POI)

Todo o Apache POI está no `LeitorPlanilha`:

| POI | O que é | No código |
|---|---|---|
| `Workbook` | o arquivo inteiro | `new XSSFWorkbook(arquivo)` |
| `Sheet` | uma aba | `workbook.getSheet("Brasil_1.1")` |
| `Row` | uma linha (começa em 0) | `for (Row row : sheet)` |
| `Cell` | uma célula (coluna A = 0, B = 1...) | `row.getCell(1)` |

Como a aba está organizada:

- Coluna B: nome do continente (com espaços na frente) ou do país.
- Colunas D a V: quantidades em blocos de 3 anos (2019, 2020, 2021). As vias são Total (D), Aérea (H), Terrestre (L), Marítima (P) e Fluvial (T).

Regras da leitura:

- Linhas sem número na coluna D (título, cabeçalho, rodapé) são puladas.
- A linha "Brasil" é o total geral e também é pulada.
- A linha de um continente só guarda o nome dele para os países que vêm abaixo.
- Cada país vira 15 objetos `ChegadaTurista` (5 vias × 3 anos). Com a planilha atual são 92 países, o que dá 1.380 registros.

Atenção: no POI, `getNumericCellValue()` só funciona em célula numérica, e `getStringCellValue()` só em célula de texto. Por isso o código verifica `getCellType()` antes de ler.

### 3. Validar

O `Validador` rejeita registros sem continente ou com quantidade negativa. Os rejeitados aparecem no console com `[VALIDACAO]` e não vão para o banco.

### 4. Gravar (CRUD)

O `ChegadaTuristaDao` tem `inserir`, `buscar`, `atualizar` e `deletar`. Hoje cada método só imprime o SQL:

```
[CRUD] INSERT INTO chegada_turista (continente, pais, via_acesso, ano, quantidade) VALUES ('África', 'Angola', 'AEREA', 2019, 19090)
```

Para ligar num banco de verdade, adicione o driver JDBC no `pom.xml` e troque o `System.out.println` de cada método por um `PreparedStatement` com o mesmo SQL.

## Logs

Cada etapa imprime com um prefixo, para você achar no console onde cada coisa acontece: `[MAIN]`, `[EMAIL]`, `[POI]`, `[VALIDACAO]`, `[CRUD]`.
