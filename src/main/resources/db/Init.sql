--This script have to be run manualy to prepare local environment for migration process

--CREATE ROLE db_executor;
--CREATE ROLE db_reader;
--
--GRANT CONNECT ON DATABASE tickets4sale to db_reader;
--GRANT CONNECT ON DATABASE tickets4sale to db_executor;
--
--CREATE USER employee with password 'tickets4sale';
--
--GRANT db_reader to employee;
--GRANT db_executor to employee;

CREATE SCHEMA IF NOT EXISTS tickets;

--GRANT USAGE ON SCHEMA tickets to db_reader;
--GRANT USAGE ON SCHEMA tickets to db_executor;
--GRANT EXECUTE ON ALL FUNCTIONS IN SCHEMA tickets to db_executor;
--GRANT SELECT,USAGE ON ALL SEQUENCES IN SCHEMA tickets TO db_reader;
--GRANT SELECT ON ALL TABLES IN SCHEMA tickets TO db_reader;

