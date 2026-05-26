-- Create users
CREATE USER precrime_migration WITH PASSWORD 'migration_pass';
CREATE USER precrime_app WITH PASSWORD 'app_pass';

-- Grant permissions to migration user
GRANT ALL PRIVILEGES ON DATABASE precrime TO precrime_migration;

-- Connect to the precrime database
\c precrime

-- Grant schema permissions to migration user
GRANT ALL PRIVILEGES ON SCHEMA public TO precrime_migration;
GRANT CREATE ON SCHEMA public TO precrime_migration;

-- Grant schema usage to app user
GRANT USAGE ON SCHEMA public TO precrime_app;

-- Ensure app user can use existing sequences (if any) and tables
-- (Though they will likely be created later by Flyway)
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO precrime_app;
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO precrime_app;

-- Set default privileges for tables/sequences created by the migration user in the future
ALTER DEFAULT PRIVILEGES FOR USER precrime_migration IN SCHEMA public
    GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO precrime_app;

ALTER DEFAULT PRIVILEGES FOR USER precrime_migration IN SCHEMA public
    GRANT USAGE, SELECT ON SEQUENCES TO precrime_app;
