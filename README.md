# Worker de planilhas (Java + Apache POI + MySQL)

Segue o padrão do repositório [SPTECH-LP/leitura-arquivos-excel](https://github.com/SPTECH-LP/leitura-arquivos-excel): um `LeitorExcel` que abre o `.xlsx` com `XSSFWorkbook`, classes simples com getters e setters e um `Main` que chama tudo.

As planilhas vêm de um bucket S3 (padrão do [SPTECH-LP/bucket-s3-exemplo-v2](https://github.com/SPTECH-LP/bucket-s3-exemplo-v2)). O nome de cada arquivo começa com o id da agência dona, seguido de `-`: `2-chegadas-2021.xlsx` é da agência `2`.

## Fluxo

1. `AgenciaDao.listarTodas()` carrega as agências do banco.
2. `LeitorS3.listarArquivos()` lista os `.xlsx` do bucket (`S3_BUCKET`).
3. Para cada arquivo, `ArquivoS3.getIdAgencia()` pega o id antes do primeiro `-` e o `Worker` procura a agência.
4. O arquivo é baixado, lido com Apache POI e gravado em `planilha`, `turista_por_pais_mes` e `turista_por_estado_mes` com o id da agência. As recomendações são gravadas e enviadas para o e-mail da agência.

Qualquer erro num arquivo não derruba o worker: o arquivo é pulado, a planilha fica com situação `ERRO` e a agência recebe um e-mail "Problema ao realizar análise de dados". Também são pulados os arquivos com nome fora do padrão `<idAgencia>-<nome>.xlsx` e os com id de agência que não existe no banco (esses sem e-mail, só log).

## Banco

Rode o `script.sql` no MySQL. Ele cria o banco `traveline` e uma agência de teste (`idAgencia = 1`). Para testar, suba no bucket um arquivo chamado `1-chegadas.xlsx`.

As credenciais do banco, do e-mail e da AWS ficam **sempre** no `.env` da raiz (fora do git; copie o `.env.example`). Na AWS Academy, copie `AWS_ACCESS_KEY_ID`, `AWS_SECRET_ACCESS_KEY` e `AWS_SESSION_TOKEN` do AWS Details do Learner Lab (expiram a cada sessão). Se as chaves da AWS ficarem vazias, o SDK usa a cadeia padrão (ex.: role da EC2). Enquanto `EMAIL_HOST`, `EMAIL_USUARIO` e `EMAIL_SENHA` estiverem vazios, o e-mail só é impresso no console.

## Agência de cada arquivo

Não existe mais uma agência fixa no código. A agência vem do nome do arquivo no bucket: o número antes do primeiro `-` é o `idAgencia`. Essa agência precisa existir na tabela `agencia`.

## Como rodar

No IntelliJ: execute a classe `Main`.

Pelo jar (na raiz do projeto, onde estão o `.env` e a planilha):

```bash
mvn clean package
java -jar target/worker-jar-with-dependencies.jar
```

Cada execução registra uma nova linha em `planilha` e grava os turistas ligados a ela.

## Classes

| Classe | O que faz |
|---|---|
| `Main` | Monta o cliente S3, a conexão e o `Worker`, e chama `executar()`. |
| `Worker` | Percorre os arquivos do bucket, identifica a agência dona e grava tudo. |
| `S3Provider` | Cria o `S3Client` com as credenciais do `.env`. |
| `LeitorS3` | Lista e baixa os `.xlsx` do bucket. |
| `ArquivoS3` | Um arquivo do bucket; extrai o id da agência do nome. |
| `Agencia` / `AgenciaDao` | Agência do banco e a listagem de todas. |
| `LeitorExcel` | Lê as abas com Apache POI e cria os objetos. |
| `TuristaPorPais` / `TuristaPorEstado` | Os dados lidos (país ou estado, meio, ano, mês, quantidade). |
| `Conexao` | Abre a conexão JDBC com o MySQL. |
| `PlanilhaDao` | Insere a planilha (`PROCESSANDO`) e muda para `CONCLUIDA` ou `ERRO`. |
| `RecomendacaoDao` | Monta o top 10 de países e de estados e grava em `recomendacoes`. |
| `EnviadorEmail` | Manda as recomendações, ou o aviso de erro, para o e-mail da agência (Jakarta Mail, SMTP). |
| `Config` | Lê o `.env`. |
| `TuristaDao` | Insere os turistas e cadastra `pais`, `uf` e `meio` quando ainda não existem. |

## De onde vem cada dado

| Tabela | Abas |
|---|---|
| `turista_por_pais_mes` e `pais` | `Brasil_1.3` (aérea), `Brasil_1.4` (terrestre), `Brasil_1.5` (marítima), `Brasil_1.6` (fluvial) |
| `turista_por_estado_mes` e `uf` | `SÍNTESE BRASIL_3.4` (aérea), `SINTESE BRASIL_3.5-3.6` (terrestre e marítima), `SINTESE BRASIL_3.7` (fluvial) |

A via vem do título de cada tabela e vira o `meio`: aérea = Avião, terrestre = Ônibus, marítima = Navio, fluvial = Barco.

Ficam de fora: a linha "Brasil" (total geral), "Países não especificados" e "Outras Unidades da Federação" (não têm sigla).

## Recomendações

Depois de gravar a planilha, o `Main` pega o top 10 de países e o top 10 de estados (soma de 2019 a 2021) e grava 20 linhas em `recomendacoes`:

- `pontuacao`: % do total de turistas
- `nivel`: 1º ao 3º ALTO, 4º ao 7º MÉDIO, 8º ao 10º BAIXO
- `sazonalidade`: mês com mais turistas
- `idLocalDestino`: `idpais` ou `iduf` (a tabela ainda não tem uma coluna dizendo qual dos dois)

## Logs

Cada etapa imprime com um prefixo: `[MAIN]`, `[WORKER]`, `[S3]`, `[POI]`, `[BANCO]`, `[EMAIL]`.
