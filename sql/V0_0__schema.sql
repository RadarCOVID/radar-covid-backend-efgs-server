CREATE SCHEMA DPPPT;
GRANT ALL ON SCHEMA DPPPT TO radarcovid;

-- To change search_path on a connection-level
ALTER ROLE radarcovid SET search_path TO DPPPT;

