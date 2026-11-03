# Worker de planilhas (Java + Apache POI + MySQL)

Segue o padrão do repositório [SPTECH-LP/leitura-arquivos-excel](https://github.com/SPTECH-LP/leitura-arquivos-excel): um `LeitorExcel` que abre o `.xlsx` com `XSSFWorkbook`, classes simples com getters e setters e um `Main` que chama tudo.

Planilha usada: `01. a 03. Chegadas de turistas internacionais ao Brasil__A2022-2021-V49-1ed.xlsx` (raiz do projeto).

## Banco (MySQL no Docker)

O `script.sql` cria o banco `traveline` e uma agência fake (`idAgencia = 1`, usada como constante no `Main`).

```bash
docker run -d --name mysql-traveline -p 3306:3306 -e MYSQL_ROOT_PASSWORD=root -v "$PWD/script.sql:/docker-entrypoint-initdb.d/script.sql:ro" mysql:8.0
```

Depois disso, basta `docker start mysql-traveline`. As credenciais do banco e do e-mail ficam no `.env` da raiz (fora do git; copie o `.env.example`). Enquanto `EMAIL_HOST`, `EMAIL_USUARIO` e `EMAIL_SENHA` estiverem vazios, o e-mail só é impresso no console.

## Como rodar

Execute a classe `Main` no IntelliJ. Cada execução registra uma nova linha em `planilha` e grava os turistas ligados a ela.

## Classes

| Classe | O que faz |
|---|---|
| `Main` | Lê a planilha e grava no banco. |
| `LeitorExcel` | Lê as abas com Apache POI e cria os objetos. |
| `TuristaPorPais` / `TuristaPorEstado` | Os dados lidos (país ou estado, meio, ano, mês, quantidade). |
| `Conexao` | Abre a conexão JDBC com o MySQL. |
| `PlanilhaDao` | Insere a planilha (`PROCESSANDO`) e muda para `CONCLUIDA` ou `ERRO`. |
| `RecomendacaoDao` | Monta o top 10 de países e de estados e grava em `recomendacoes`. |
| `EnviadorEmail` | Manda as recomendações para o e-mail da agência (Jakarta Mail, SMTP). |
| `Config` | Lê o `.env`. |
| `TuristaDao` | Insere os turistas e cadastra `pais`, `estado` e `meio` quando ainda não existem. |

## De onde vem cada dado

| Tabela | Abas |
|---|---|
| `turista_por_pais_mes` e `pais` | `Brasil_1.3` (aérea), `Brasil_1.4` (terrestre), `Brasil_1.5` (marítima), `Brasil_1.6` (fluvial) |
| `turista_por_estado_mes` e `estado` | `SÍNTESE BRASIL_3.4` (aérea), `SINTESE BRASIL_3.5-3.6` (terrestre e marítima), `SINTESE BRASIL_3.7` (fluvial) |

A via vem do título de cada tabela e vira o `meio`: aérea = Avião, terrestre = Ônibus, marítima = Navio, fluvial = Barco.

Ficam de fora: a linha "Brasil" (total geral), "Países não especificados" e "Outras Unidades da Federação" (não têm sigla).

## Recomendações

Depois de gravar a planilha, o `Main` pega o top 10 de países e o top 10 de estados (soma de 2019 a 2021) e grava 20 linhas em `recomendacoes`:

- `pontuacao`: % do total de turistas
- `nivel`: 1º ao 3º ALTO, 4º ao 7º MÉDIO, 8º ao 10º BAIXO
- `sazonalidade`: mês com mais turistas
- `idLocalDestino`: `idpais` ou `idestado` (a tabela ainda não tem uma coluna dizendo qual dos dois)

## Logs

Cada etapa imprime com um prefixo: `[MAIN]`, `[POI]`, `[BANCO]`, `[EMAIL]`.
