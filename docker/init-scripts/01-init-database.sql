-- Script de inicialização do banco de dados
-- Este script é executado automaticamente quando o container PostgreSQL é criado

-- Criar extensões necessárias
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- Configurar timezone
SET timezone = 'America/Sao_Paulo';

-- Criar schema para auditoria (se necessário)
CREATE SCHEMA IF NOT EXISTS audit;

-- Comentários informativos
COMMENT ON DATABASE systemrpg_backend_db IS 'Banco de dados do backend SystemRPG';
COMMENT ON SCHEMA public IS 'Schema principal para tabelas de autenticação';
COMMENT ON SCHEMA audit IS 'Schema para tabelas de auditoria e logs';

-- Configurações de performance básicas
ALTER SYSTEM SET shared_preload_libraries = 'pg_stat_statements';
ALTER SYSTEM SET log_statement = 'all';
ALTER SYSTEM SET log_min_duration_statement = 1000;

-- Recarregar configurações
SELECT pg_reload_conf();

-- Log de inicialização
DO $$
BEGIN
    RAISE NOTICE 'Banco de dados systemrpg_backend_db inicializado com sucesso!';
    RAISE NOTICE 'Extensões instaladas: uuid-ossp, pgcrypto';
    RAISE NOTICE 'Timezone configurado: America/Sao_Paulo';
END $$;