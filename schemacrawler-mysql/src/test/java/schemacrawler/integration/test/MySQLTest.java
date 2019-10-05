/*
========================================================================
SchemaCrawler
http://www.schemacrawler.com
Copyright (c) 2000-2019, Sualeh Fatehi <sualeh@hotmail.com>.
All rights reserved.
------------------------------------------------------------------------

SchemaCrawler is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.

SchemaCrawler and the accompanying materials are made available under
the terms of the Eclipse Public License v1.0, GNU General Public License
v3 or GNU Lesser General Public License v3.

You may elect to redistribute this code under any of these licenses.

The Eclipse Public License is available at:
http://www.eclipse.org/legal/epl-v10.html

The GNU General Public License v3 and the GNU Lesser General Public
License v3 are available at:
http://www.gnu.org/licenses/

========================================================================
*/
package schemacrawler.integration.test;


import static org.hamcrest.MatcherAssert.assertThat;
import static schemacrawler.test.utility.ExecutableTestUtility.executableExecution;
import static schemacrawler.test.utility.FileHasContent.*;

import java.sql.SQLException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import schemacrawler.schemacrawler.*;
import schemacrawler.test.utility.BaseAdditionalDatabaseTest;
import schemacrawler.test.utility.DatabaseServerContainer;
import schemacrawler.tools.executable.SchemaCrawlerExecutable;
import schemacrawler.tools.text.schema.SchemaTextOptions;
import schemacrawler.tools.text.schema.SchemaTextOptionsBuilder;

public class MySQLTest
  extends BaseAdditionalDatabaseTest
{

  private DatabaseServerContainer databaseServer;

  @BeforeEach
  public void createDatabase()
    throws SchemaCrawlerException, SQLException
  {
    databaseServer = new MySQLDatabaseServerContainer("books");
    databaseServer.startServer();

    createDataSource(databaseServer);
    createDatabase("/mysql.scripts.txt");
  }

  @AfterEach
  public void stopDatabaseServer()
  {
    databaseServer.stopServer();
  }

  @Test
  public void testMySQLWithConnection()
    throws Exception
  {
    final SchemaCrawlerOptionsBuilder schemaCrawlerOptionsBuilder = SchemaCrawlerOptionsBuilder
      .builder();
    schemaCrawlerOptionsBuilder
      .withSchemaInfoLevel(SchemaInfoLevelBuilder.maximum())
      .includeSchemas(new RegularExpressionInclusionRule("books"))
      .includeAllSequences().includeAllSynonyms().includeAllRoutines();
    final SchemaCrawlerOptions options = schemaCrawlerOptionsBuilder
      .toOptions();

    final SchemaTextOptionsBuilder textOptionsBuilder = SchemaTextOptionsBuilder
      .builder();
    textOptionsBuilder.showDatabaseInfo().showJdbcDriverInfo();
    final SchemaTextOptions textOptions = textOptionsBuilder.toOptions();

    final SchemaCrawlerExecutable executable = new SchemaCrawlerExecutable(
      "details");
    executable.setSchemaCrawlerOptions(options);
    executable
      .setAdditionalConfiguration(SchemaTextOptionsBuilder.builder(textOptions)
                                    .toConfig());

    assertThat(outputOf(executableExecution(getConnection(), executable)),
               hasSameContentAs(classpathResource("testMySQLWithConnection.txt")));
  }

}
