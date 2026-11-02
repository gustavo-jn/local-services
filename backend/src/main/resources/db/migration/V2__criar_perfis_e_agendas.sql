CREATE TABLE perfis_profissionais (
    id UUID NOT NULL PRIMARY KEY,
    usuario_id UUID NOT NULL,
    nome VARCHAR(120) NOT NULL,
    descricao VARCHAR(3000) NOT NULL,
    aceita_novos_servicos BOOLEAN NOT NULL DEFAULT TRUE,
    publicado BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT uk_perfis_usuario UNIQUE (usuario_id),
    CONSTRAINT fk_perfis_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios(id),
    CONSTRAINT ck_perfis_nome CHECK (char_length(trim(nome)) BETWEEN 2 AND 120),
    CONSTRAINT ck_perfis_descricao CHECK (char_length(trim(descricao)) BETWEEN 10 AND 3000)
);

CREATE TABLE agendas (
    id UUID NOT NULL PRIMARY KEY,
    perfil_profissional_id UUID NOT NULL,
    fuso_referencia VARCHAR(60) NOT NULL DEFAULT 'America/Sao_Paulo',
    CONSTRAINT uk_agendas_perfil UNIQUE (perfil_profissional_id),
    CONSTRAINT fk_agendas_perfil FOREIGN KEY (perfil_profissional_id) REFERENCES perfis_profissionais(id)
);
