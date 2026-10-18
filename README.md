# Worker de planilhas (Java puro + Apache POI + Jakarta Mail)

Worker sem Spring que recebe uma planilha Excel, lê com o Apache POI, transforma em objetos de domínio, valida e envia para a camada de persistência. Por enquanto a persistência **não grava em banco**: ela só escreve no log o SQL que seria executado.

A planilha de referência é a que está na raiz do projeto:
`01. a 03. Chegadas de turistas internacionais ao Brasil__A2022-2021-V49-1ed.xlsx`, aba **`Brasil_1.1`** (chegadas de turistas por país, via de acesso e ano, 2019–2021).

## Como rodar

Pelo IntelliJ: execute a classe `org.example.Main`.

Pelo terminal (na raiz do projeto):

```bash
mvn compile exec:java
```

> Não use `mvn -q`: no modo silencioso o Maven esconde os logs INFO do worker.

Por padrão roda em modo `LOCAL`, lendo o `.xlsx` da raiz. Variáveis de ambiente disponíveis (`config/ConfiguracaoWorker.java`):

| Variável | Padrão | Para quê |
|---|---|---|
| `WORKER_MODO` | `LOCAL` | `LOCAL` lê do disco; `EMAIL` busca anexos via IMAP |
| `WORKER_ARQUIVO` | o `.xlsx` da raiz | caminho do arquivo no modo `LOCAL` |
| `WORKER_ABA` | `Brasil_1.1` | aba que será lida |
| `MAIL_HOST` | | servidor IMAP, ex.: `imap.gmail.com` |
| `MAIL_USUARIO` / `MAIL_SENHA` | | conta e senha (no Gmail, use "senha de app") |
| `MAIL_PASTA` | `INBOX` | pasta da caixa de e-mail |

## Estrutura

```
src/main/java/org/example/
├── Main.java                         monta as peças e chama o worker
├── config/ConfiguracaoWorker.java    lê variáveis de ambiente
├── worker/ProcessadorPlanilhaWorker  ORQUESTRADOR: chama as 5 etapas na ordem
├── ingestao/                         1. de onde vem o arquivo
│   ├── FonteArquivo                     interface
│   ├── FonteArquivoEmail                Jakarta Mail (IMAP, anexos)
│   ├── FonteArquivoLocal                arquivo do disco
│   └── ArquivoRecebido                  nome + origem + bytes
├── leitura/                          2. Apache POI
│   ├── LeitorPlanilha                   Workbook -> Sheet -> Row -> Cell
│   └── LinhaPlanilha                    linha "crua", já sem POI
├── mapeamento/                       3. linha crua -> objeto de domínio
│   ├── LayoutAbaBrasil11                qual coluna é o quê
│   └── MapeadorChegadas
├── dominio/                          objetos de negócio
│   ├── ChegadaTurista
│   └── ViaAcesso
├── validacao/                        4. regras antes de gravar
│   ├── ValidadorChegadas
│   └── ResultadoValidacao               válidos x rejeitados (com motivo)
└── persistencia/                     5. CRUD
    ├── ChegadaTuristaRepository         interface (create/read/update/delete)
    └── ChegadaTuristaRepositoryLog      implementação que só loga o SQL
```

Cada etapa escreve logs com um prefixo, para você achar no console onde cada coisa acontece: `[INGESTAO]`, `[POI][WORKBOOK]`, `[POI][SHEET]`, `[POI][ROW]`, `[POI][CELL]`, `[MAPEAMENTO]`, `[VALIDACAO]`, `[CRUD]`.

## O fluxo, passo a passo

```
 e-mail (IMAP) ou disco
        │  bytes do .xlsx
        ▼
 1. INGESTÃO      FonteArquivo            -> List<ArquivoRecebido>
        ▼
 2. LEITURA       LeitorPlanilha (POI)    -> List<LinhaPlanilha>
        ▼
 3. MAPEAMENTO    MapeadorChegadas        -> List<ChegadaTurista>
        ▼
 4. VALIDAÇÃO     ValidadorChegadas       -> válidos / rejeitados
        ▼
 5. PERSISTÊNCIA  ChegadaTuristaRepository -> hoje: log do INSERT
```

O método `ProcessadorPlanilhaWorker.executar()` é o roteiro do fluxo; comece a leitura do código por ele.

### 1. Ingestão: como os dados chegam

O worker não sabe de onde vem o arquivo; ele só pede a uma `FonteArquivo` uma lista de `ArquivoRecebido` (nome + bytes). Há duas implementações:

- **`FonteArquivoEmail`** (produção) usa o Jakarta Mail, que segue esta cadeia:
  `Session` (configuração) → `Store` (conexão IMAP com usuário/senha) → `Folder` (INBOX) → `Message` (cada e-mail não lido) → `Multipart` (corpo + anexos) → `BodyPart` (o anexo). Os anexos `.xlsx`/`.xls` têm os bytes lidos com `getInputStream()` e o e-mail é marcado como lido.
- **`FonteArquivoLocal`** (desenvolvimento) só lê o arquivo do disco, para você testar sem configurar e-mail.

Guardar os bytes em memória (em vez de manter o stream do e-mail aberto) separa a conexão de e-mail do processamento: a conexão já pode ser fechada quando o POI começa a ler.

### 2. Leitura: onde está o Apache POI

Todo o Apache POI fica em **`leitura/LeitorPlanilha.java`**. A hierarquia é:

| Objeto POI | O que é | No código |
|---|---|---|
| `Workbook` | o arquivo inteiro | `WorkbookFactory.create(inputStream)` detecta `.xls` (HSSF) ou `.xlsx` (XSSF) |
| `Sheet` | uma aba | `workbook.getSheet("Brasil_1.1")` |
| `Row` | uma linha | `for (Row row : sheet)`; índice começa em 0 (linha 1 do Excel = `row 0`) |
| `Cell` | uma célula | `row.getCell(col, RETURN_BLANK_AS_NULL)`; coluna A = 0, B = 1 ... |

Pontos de atenção que o código já trata:

- **Tipo da célula**: antes de ler é preciso checar `cell.getCellType()`. Chamar `getNumericCellValue()` numa célula de texto lança exceção.
- **Fórmulas**: usamos `getCachedFormulaResultType()` para pegar o resultado que o Excel salvou, sem recalcular.
- **Datas**: no Excel, data é número; `DateUtil.isCellDateFormatted(cell)` diz se deve virar data.
- **Células vazias**: `RETURN_BLANK_AS_NULL` devolve `null`, o que simplifica as checagens.

A saída é uma lista de `LinhaPlanilha` (número da linha + valores como `String`/`Double`/`null`). A partir daqui nenhuma classe depende do POI.

### 3. Mapeamento: da linha crua para o domínio

A planilha é feita para leitura humana, não para máquina: tem título, cabeçalhos em várias linhas, células mescladas, rodapé ("Fonte: ...") e o cabeçalho se repete no meio da aba (linha 76 em diante). O `MapeadorChegadas` usa o `LayoutAbaBrasil11` para interpretar:

- Linha sem número na coluna D → cabeçalho/título/rodapé, ignorada.
- `Brasil` → total geral, ignorada.
- Nome com espaços à esquerda (ex.: `"          África"`) → é um **continente**: não gera registro, só define o continente dos países seguintes.
- Demais linhas → **país**. Cada país vira 15 objetos `ChegadaTurista`: 5 vias de acesso (Total, Aérea, Terrestre, Marítima, Fluvial) × 3 anos (2019, 2020, 2021).

Ou seja, a planilha "larga" (anos e vias em colunas) vira uma tabela "longa" (uma linha por país/via/ano), que é o formato natural para banco de dados. Com a planilha atual são 92 países → 1.380 registros.

Se o layout da planilha mudar, só o `LayoutAbaBrasil11` precisa ser alterado. Para ler outra aba com formato diferente, crie outro layout e outro mapeador.

### 4. Validação e tratamento

O `ValidadorChegadas` aplica regras de negócio (continente presente, país não vazio, quantidade presente e não negativa). Um registro inválido **não interrompe** o processamento: ele vai para a lista de rejeitados com o motivo e aparece no log como `[VALIDACAO] Rejeitado`. É aqui que entram novas regras (ex.: ano dentro de um intervalo, país numa lista conhecida).

### 5. Persistência (CRUD)

`ChegadaTuristaRepository` é o contrato do CRUD: `salvar`/`salvarTodos` (create), `buscarPorId`/`listarPorPais` (read), `atualizar` (update), `remover` (delete).

A implementação atual, `ChegadaTuristaRepositoryLog`, só escreve no log o SQL que seria executado, por exemplo:

```
[CRUD][CREATE] INSERT INTO chegada_turista (...) VALUES (?, ?, ?, ?, ?) -> ('África', 'Angola', AEREA, 2019, 19090)
```

O comentário da classe tem um `CREATE TABLE` sugerido.

## Próximos passos para ligar no banco

1. Adicionar o driver JDBC no `pom.xml` (ex.: `mysql-connector-j` ou `postgresql`).
2. Criar `ChegadaTuristaRepositoryJdbc implements ChegadaTuristaRepository`, usando `PreparedStatement` com o mesmo SQL do log e `addBatch()`/`executeBatch()` dentro de uma transação em `salvarTodos`.
3. Trocar no `Main` a linha `new ChegadaTuristaRepositoryLog()` pela nova classe. Nenhuma outra etapa muda.

## Limitações conhecidas

- O e-mail é marcado como lido assim que o anexo é baixado; o ideal é marcar só depois de persistir com sucesso (há um `TODO` em `FonteArquivoEmail`).
- "Países não especificados" vem recuado como se fosse continente e por isso não gera registros (há um `TODO` em `MapeadorChegadas`).
- O worker executa uma vez e termina. Para rodar periodicamente, use um agendador do sistema (cron) ou um `ScheduledExecutorService` no `Main`.
