CREATE DATABASE IF NOT EXISTS traveline;

USE traveline;

CREATE TABLE pais (
    idpais INT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(35) NOT NULL,
    continente VARCHAR(45),
    criado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE uf (
    iduf INT AUTO_INCREMENT PRIMARY KEY,
    sigla CHAR(2) NOT NULL UNIQUE,
    nome VARCHAR(45) NOT NULL,
    criado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE meio (
    idmodal INT AUTO_INCREMENT PRIMARY KEY,
    tipo VARCHAR(45) NOT NULL,
    criado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE agencia (
    idAgencia INT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    cnpj VARCHAR(18) NOT NULL,
    email VARCHAR(100) NOT NULL,
    dataCadastro DATE,
    telefone VARCHAR(20),
    criado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Histórico de códigos de ativação gerados para cada agência
CREATE TABLE codigo_ativacao (
    idcodigo INT AUTO_INCREMENT PRIMARY KEY,
    codigo VARCHAR(45) NOT NULL UNIQUE,
    status TINYINT NOT NULL DEFAULT 1,
    gerado_em DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    idAgencia INT NOT NULL,
    criado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (idAgencia)
        REFERENCES agencia(idAgencia)
);

CREATE TABLE tela (
	idTela INT AUTO_INCREMENT PRIMARY KEY,
    tela VARCHAR(13) NOT NULL UNIQUE,
    criado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE cargo (
	idCargo INT AUTO_INCREMENT PRIMARY KEY,
    cargo VARCHAR(13) NOT NULL UNIQUE,
    criado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE permissao (
	idPermissao INT AUTO_INCREMENT PRIMARY KEY,
    idTela INT NOT NULL,
    idCargo INT NOT NULL,
    criado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (idTela) REFERENCES tela(idTela),
    FOREIGN KEY (idCargo) REFERENCES cargo(idCargo)
);

CREATE TABLE usuario (
    idusuario INT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL,
    senha VARCHAR(255) NOT NULL,
    idCargo INT NOT NULL,
    idAgencia INT NOT NULL,
    criado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (idAgencia)
        REFERENCES agencia(idAgencia),
    FOREIGN KEY (idCargo)
        REFERENCES cargo(idCargo)
);

CREATE TABLE preferencias_usuario (
	idUsuario INT PRIMARY KEY,
    temaClaro TINYINT NOT NULL DEFAULT 1,
    totalTuristas TINYINT NOT NULL DEFAULT 1,
    viaAerea TINYINT NOT NULL DEFAULT 1,
    viaTerrestre TINYINT NOT NULL DEFAULT 1,
    viaMaritima TINYINT NOT NULL DEFAULT 1,
    viaFluvial TINYINT NOT NULL DEFAULT 1,
    paisesEmissores TINYINT NOT NULL DEFAULT 1,
    receberEmail TINYINT NOT NULL DEFAULT 1,
    
    FOREIGN KEY (idUsuario) REFERENCES usuario(idusuario)
);

CREATE TABLE planilha (
    idplanilha INT AUTO_INCREMENT PRIMARY KEY,
    caminho VARCHAR(255) NOT NULL,
    nome VARCHAR(60) NOT NULL,
    dataHora DATETIME,
    situacao VARCHAR(45),
    dataInicio DATE,
    dataConclusao DATE,
    agencia_idAgencia INT NOT NULL,
    criado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (agencia_idAgencia)
        REFERENCES agencia(idAgencia)
);

CREATE TABLE turista_por_estado_mes (
    idturista_por_estado_mes INT AUTO_INCREMENT PRIMARY KEY,
    ano VARCHAR(45),
    mes VARCHAR(45),
    quantidade_turista INT,
    planilha_idplanilha INT,
    uf_iduf INT,
    meio_idmodal INT,
    criado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (planilha_idplanilha)
        REFERENCES planilha(idplanilha),

    FOREIGN KEY (uf_iduf)
        REFERENCES uf(iduf),

    FOREIGN KEY (meio_idmodal)
        REFERENCES meio(idmodal)
);

CREATE TABLE turista_por_pais_mes (
    idturista_por_pais_mes INT AUTO_INCREMENT PRIMARY KEY,
    ano VARCHAR(45),
    mes VARCHAR(45),
    quantidade_turistas INT,
    meio_idmodal INT,
    pais_idpais INT,
    planilha_idplanilha INT,
    criado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (meio_idmodal)
        REFERENCES meio(idmodal),

    FOREIGN KEY (pais_idpais)
        REFERENCES pais(idpais),

    FOREIGN KEY (planilha_idplanilha)
        REFERENCES planilha(idplanilha)
);

CREATE TABLE relatorio (
    idrelatorio INT AUTO_INCREMENT PRIMARY KEY,
    titulo VARCHAR(100),
    mensagem VARCHAR(100),
    descricao VARCHAR(100),
    dataCriacao DATE,
    usuario_idusuario INT NOT NULL,
    criado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (usuario_idusuario)
        REFERENCES usuario(idusuario)
);

CREATE TABLE recomendacoes (
    idrecomendacoes INT AUTO_INCREMENT PRIMARY KEY,
    dataGeracao DATETIME,
    pontuacao DECIMAL(5,2),
    nivel VARCHAR(45),
    sazonalidade VARCHAR(45),
    idAgencia INT NOT NULL,
    idLocalDestino INT,
    criado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (idAgencia)
        REFERENCES agencia(idAgencia)
);

-- Dados brutos da importação conforme o dicionário de dados
CREATE TABLE dados_brutos_importacao (
    iddado_bruto INT AUTO_INCREMENT PRIMARY KEY,
    planilha_idplanilha INT NOT NULL,
    continente_origem VARCHAR(60),
    pais_origem VARCHAR(60),
    uf_destino CHAR(2),
    via_acesso VARCHAR(30),
    mes_referencia VARCHAR(20),
    ano_referencia VARCHAR(20),
    quantidade_turistas INT,
    posicao_ranking INT,
    percentual_participacao DECIMAL(5,2),
    criado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (planilha_idplanilha)
        REFERENCES planilha(idplanilha)
);

-- Logs gerados pela aplicação Java
CREATE TABLE log_java (
    idlog INT AUTO_INCREMENT PRIMARY KEY,
    nivel VARCHAR(20),
    mensagem TEXT NOT NULL,
    classe VARCHAR(255),
    dataHora DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    criado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO agencia
(idAgencia, nome, cnpj, email, dataCadastro, telefone)
VALUES
(1, 'Traveline Turismo', '12.345.678/0001-90', 'contato@travelineturismo.com', '2026-10-01', '(11) 99999-1111'),
(2, 'Global Travel', '23.456.789/0001-01', 'guilhermesdomingues31@gmail.com', '2026-10-05', '(11) 99999-2222'),
(3, 'Mundo Viagens', '34.567.890/0001-12', 'contato@mundoviagens.com', '2026-10-10', '(21) 99999-3333');

INSERT INTO codigo_ativacao
(idcodigo, codigo, status, gerado_em, idAgencia)
VALUES
(1, 'TRAVELINE001', 1, '2026-10-01 09:00:00', 1),
(2, 'GLOBAL002', 1, '2026-10-05 09:00:00', 2),
(3, 'MUNDO003', 1, '2026-10-10 09:00:00', 3),
(4, 'TRAVELINE-ANTIGO', 0, '2026-09-01 09:00:00', 1),
(5, 'GLOBAL-ANTIGO', 0, '2026-09-05 09:00:00', 2),
(6, 'MUNDO-ANTIGO', 0, '2026-09-10 09:00:00', 3);

INSERT INTO cargo (cargo)
VALUES
('Administrador'),
('Gerente'),
('Analista')
ON DUPLICATE KEY UPDATE cargo = VALUES(cargo);

-- Busca os IDs para funcionar mesmo com cargos já cadastrados.
SET @administrador = (SELECT idCargo FROM cargo WHERE cargo = 'Administrador');
SET @gerente = (SELECT idCargo FROM cargo WHERE cargo = 'Gerente');
SET @analista = (SELECT idCargo FROM cargo WHERE cargo = 'Analista');

INSERT INTO usuario
(idusuario, nome, email, senha, idCargo, idAgencia)
VALUES
(1, 'Guilherme Silva', 'guilherme@travelineturismo.com', '123456', @administrador, 1),
(2, 'Ana Souza', 'ana@travelineturismo.com', '123456', @analista, 1),
(3, 'Carlos Oliveira', 'carlos@globaltravel.com', '123456', @gerente, 2),
(4, 'Mariana Costa', 'mariana@globaltravel.com', '123456', @analista, 2),
(5, 'Pedro Santos', 'pedro@mundoviagens.com', '123456', @administrador, 3);

INSERT INTO preferencias_usuario
(idUsuario, temaClaro, totalTuristas, viaAerea, viaTerrestre, viaMaritima, viaFluvial, paisesEmissores)
VALUES
(1, 1, 1, 1, 1, 1, 1, 1),
(2, 1, 1, 1, 1, 1, 1, 1),
(3, 1, 1, 1, 1, 1, 1, 1),
(4, 1, 1, 1, 1, 1, 1, 1),
(5, 1, 1, 1, 1, 1, 1, 1);
