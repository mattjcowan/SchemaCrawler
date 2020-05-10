-- Oracle syntax
CREATE USER BOOKS IDENTIFIED BY BOOKS;
GRANT UNLIMITED TABLESPACE TO BOOKS;
GRANT CREATE MATERIALIZED VIEW TO BOOKS;
GRANT QUERY REWRITE TO BOOKS;
GRANT CREATE ANY TABLE TO BOOKS;
ALTER SESSION SET CURRENT_SCHEMA = BOOKS;
