@echo off
..\..\_schemacrawler\bin\schemacrawler.cmd --server=hsqldb --database=schemacrawler --user=sa --password= --info-level=maximum -c=schema --output-format=png -o=database-diagram.png %*
echo Database diagram is in database-diagram.png
