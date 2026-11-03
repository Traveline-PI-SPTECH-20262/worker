-- =========================================
-- BANCO DE DADOS
-- =========================================

CREATE DATABASE IF NOT EXISTS traveline;
USE traveline;


-- =========================================
-- TABELA: PAIS
-- =========================================

CREATE TABLE pais (
    idpais INT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(35) NOT NULL,
    continente VARCHAR(45)
);


-- =========================================
-- TABELA: ESTADO
-- =========================================

CREATE TABLE estado (
    idestado INT AUTO_INCREMENT PRIMARY KEY,
    sigla VARCHAR(2) NOT NULL,
    nome VARCHAR(45) NOT NULL
);


-- =========================================
-- TABELA: MEIO
-- =========================================

CREATE TABLE meio (
    idmodal INT AUTO_INCREMENT PRIMARY KEY,
    tipo VARCHAR(45) NOT NULL
);


-- =========================================
-- TABELA: AGENCIA
-- =========================================

CREATE TABLE agencia (
    idAgencia INT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    cnpj VARCHAR(18) NOT NULL,
    email VARCHAR(100) NOT NULL,
    dataCadastro DATE,
    telefone VARCHAR(20),
    codigoAtivacao VARCHAR(45)
);


-- =========================================
-- TABELA: USUARIO
-- =========================================

CREATE TABLE usuario (
    idusuario INT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL,
    senha VARCHAR(255) NOT NULL,
    cargo VARCHAR(50),
    idAgencia INT NOT NULL,

    FOREIGN KEY (idAgencia)
        REFERENCES agencia(idAgencia)
);


-- =========================================
-- TABELA: PLANILHA
-- =========================================

CREATE TABLE planilha (
    idplanilha INT AUTO_INCREMENT PRIMARY KEY,
    caminho VARCHAR(255) NOT NULL,
    dataHora DATETIME,
    situacao VARCHAR(45),
    dataInicio DATE,
    dataConclusao DATE,
    agencia_idAgencia INT NOT NULL,

    FOREIGN KEY (agencia_idAgencia)
        REFERENCES agencia(idAgencia)
);


-- =========================================
-- TABELA: TURISTA POR ESTADO E MES
-- =========================================

CREATE TABLE turista_por_estado_mes (
    idturista_por_estado_mes INT AUTO_INCREMENT PRIMARY KEY,
    ano VARCHAR(45),
    mes VARCHAR(45),
    quantidade_turista INT,
    planilha_idplanilha INT,
    estado_idestado INT,
    meio_idmodal INT,

    FOREIGN KEY (planilha_idplanilha)
        REFERENCES planilha(idplanilha),

    FOREIGN KEY (estado_idestado)
        REFERENCES estado(idestado),

    FOREIGN KEY (meio_idmodal)
        REFERENCES meio(idmodal)
);


-- =========================================
-- TABELA: TURISTA POR PAIS E MES
-- =========================================

CREATE TABLE turista_por_pais_mes (
    idturista_por_pais_mes INT AUTO_INCREMENT PRIMARY KEY,
    ano VARCHAR(45),
    mes VARCHAR(45),
    quantidade_turistas INT,
    meio_idmodal INT,
    pais_idpais INT,
    planilha_idplanilha INT,

    FOREIGN KEY (meio_idmodal)
        REFERENCES meio(idmodal),

    FOREIGN KEY (pais_idpais)
        REFERENCES pais(idpais),

    FOREIGN KEY (planilha_idplanilha)
        REFERENCES planilha(idplanilha)
);


-- =========================================
-- TABELA: RELATORIO
-- =========================================

CREATE TABLE relatorio (
    idrelatorio INT AUTO_INCREMENT PRIMARY KEY,
    titulo VARCHAR(100),
    mensagem VARCHAR(100),
    descricao VARCHAR(100),
    dataCriacao DATE,
    usuario_idusuario INT NOT NULL,

    FOREIGN KEY (usuario_idusuario)
        REFERENCES usuario(idusuario)
);


-- =========================================
-- TABELA: RECOMENDACOES
-- =========================================

CREATE TABLE recomendacoes (
    idrecomendacoes INT AUTO_INCREMENT PRIMARY KEY,
    dataGeracao DATETIME,
    pontuacao DECIMAL(5,2),
    nivel VARCHAR(45),
    sazonalidade VARCHAR(45),
    idAgencia INT NOT NULL,
    idLocalDestino INT,

    FOREIGN KEY (idAgencia)
        REFERENCES agencia(idAgencia)

);


INSERT INTO agencia (nome, cnpj, email, dataCadastro, telefone, codigoAtivacao)
VALUES ('Agencia Teste', '00.000.000/0001-00', 'teste@agencia.com', CURDATE(), '(11) 99999-9999', 'TESTE123');
