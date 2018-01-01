-- Table with identity column, generated
-- Oracle 12c syntax
CREATE TABLE Publishers
(
  Id INTEGER GENERATED BY DEFAULT ON NULL AS IDENTITY,
  Publisher VARCHAR(255),
  CONSTRAINT PK_Publishers PRIMARY KEY (Id)
)
;
