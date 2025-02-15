/*
========================================================================
SchemaCrawler
http://www.schemacrawler.com
Copyright (c) 2000-2022, Sualeh Fatehi <sualeh@hotmail.com>.
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

package schemacrawler.tools.utility;

import static java.util.Objects.requireNonNull;
import static us.fatehi.utility.Utility.isBlank;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import schemacrawler.crawl.ConnectionInfoBuilder;
import schemacrawler.crawl.ResultsCrawler;
import schemacrawler.schema.Catalog;
import schemacrawler.schema.ConnectionInfo;
import schemacrawler.schema.ResultsColumns;
import schemacrawler.schemacrawler.DatabaseServerType;
import schemacrawler.schemacrawler.SchemaCrawlerOptions;
import schemacrawler.schemacrawler.SchemaRetrievalOptions;
import schemacrawler.schemacrawler.SchemaRetrievalOptionsBuilder;
import schemacrawler.schemacrawler.exceptions.DatabaseAccessException;
import schemacrawler.schemacrawler.exceptions.InternalRuntimeException;
import schemacrawler.tools.catalogloader.CatalogLoader;
import schemacrawler.tools.catalogloader.CatalogLoaderRegistry;
import schemacrawler.tools.databaseconnector.DatabaseConnector;
import schemacrawler.tools.databaseconnector.DatabaseConnectorRegistry;
import schemacrawler.tools.options.Config;
import us.fatehi.utility.PropertiesUtility;
import us.fatehi.utility.UtilityMarker;
import us.fatehi.utility.database.DatabaseUtility;
import us.fatehi.utility.string.ObjectToStringFormat;
import us.fatehi.utility.string.StringFormat;

/** SchemaCrawler utility methods. */
@UtilityMarker
public final class SchemaCrawlerUtility {

  private static final Logger LOGGER = Logger.getLogger(SchemaCrawlerUtility.class.getName());

  /**
   * Crawls a database, and returns a catalog.
   *
   * @param connection Live database connection.
   * @param schemaCrawlerOptions Options.
   * @return Database catalog.
   */
  public static Catalog getCatalog(
      final Connection connection, final SchemaCrawlerOptions schemaCrawlerOptions) {
    checkConnection(connection);
    LOGGER.log(Level.CONFIG, new ObjectToStringFormat(schemaCrawlerOptions));

    final SchemaRetrievalOptions schemaRetrievalOptions = matchSchemaRetrievalOptions(connection);

    return getCatalog(connection, schemaRetrievalOptions, schemaCrawlerOptions, new Config());
  }

  public static Catalog getCatalog(
      final Connection connection,
      final SchemaRetrievalOptions schemaRetrievalOptions,
      final SchemaCrawlerOptions schemaCrawlerOptions,
      final Config additionalConfig) {

    final CatalogLoaderRegistry catalogLoaderRegistry = new CatalogLoaderRegistry();
    final CatalogLoader catalogLoader = catalogLoaderRegistry.newChainedCatalogLoader();

    LOGGER.log(Level.CONFIG, new StringFormat("Catalog loader: %s", catalogLoader));
    logConnection(connection);

    catalogLoader.setConnection(connection);
    catalogLoader.setSchemaRetrievalOptions(schemaRetrievalOptions);
    catalogLoader.setSchemaCrawlerOptions(schemaCrawlerOptions);
    catalogLoader.setAdditionalConfiguration(additionalConfig);

    catalogLoader.loadCatalog();
    final Catalog catalog = catalogLoader.getCatalog();
    requireNonNull(catalog, "Catalog could not be retrieved");
    return catalog;
  }

  /**
   * Obtains result-set metadata from a live result-set.
   *
   * @param resultSet Live result-set.
   * @return Result-set metadata.
   */
  public static ResultsColumns getResultsColumns(final ResultSet resultSet) {
    try {
      // NOTE: Some JDBC drivers like SQLite may not work with closed
      // result-sets
      checkResultSet(resultSet);
      final ResultsCrawler resultSetCrawler = new ResultsCrawler(resultSet);
      final ResultsColumns resultsColumns = resultSetCrawler.crawl();
      return resultsColumns;
    } catch (final SQLException e) {
      throw new DatabaseAccessException("Could not retrieve result-set metadata", e);
    }
  }
  /**
   * Returns database specific options using an existing SchemaCrawler database plugin.
   *
   * @return SchemaRetrievalOptions
   */
  public static SchemaRetrievalOptions matchSchemaRetrievalOptions(final Connection connection) {
    final SchemaRetrievalOptionsBuilder schemaRetrievalOptionsBuilder =
        buildSchemaRetrievalOptions(connection);

    final SchemaRetrievalOptions schemaRetrievalOptions = schemaRetrievalOptionsBuilder.toOptions();

    return schemaRetrievalOptions;
  }

  /**
   * Allows building of database specific options programatically, using an existing SchemaCrawler
   * database plugin as a starting point.
   *
   * @return SchemaRetrievalOptionsBuilder
   */
  private static SchemaRetrievalOptionsBuilder buildSchemaRetrievalOptions(
      final Connection connection) {

    checkConnection(connection);

    final DatabaseConnectorRegistry registry =
        DatabaseConnectorRegistry.getDatabaseConnectorRegistry();
    DatabaseConnector dbConnector = registry.findDatabaseConnector(connection);
    final DatabaseServerType databaseServerType = dbConnector.getDatabaseServerType();

    // Log SchemaCrawler database plugin being used
    if (databaseServerType.isUnknownDatabaseSystem()) {
      LOGGER.log(Level.INFO, "Not using any SchemaCrawler database plugin");
    } else {
      LOGGER.log(Level.INFO, "Using SchemaCrawler database plugin for " + databaseServerType);
    }

    final boolean useMatchedDatabasePlugin =
        useMatchedDatabasePlugin(connection, databaseServerType);
    if (!useMatchedDatabasePlugin) {
      dbConnector = DatabaseConnector.UNKNOWN;
    }

    final SchemaRetrievalOptionsBuilder schemaRetrievalOptionsBuilder =
        dbConnector.getSchemaRetrievalOptionsBuilder(connection);
    return schemaRetrievalOptionsBuilder;
  }

  private static void checkConnection(final Connection connection) {
    try {
      DatabaseUtility.checkConnection(connection);
    } catch (final SQLException e) {
      throw new InternalRuntimeException("Bad database connection", e);
    }
  }

  private static void checkResultSet(final ResultSet resultSet) {
    try {
      DatabaseUtility.checkResultSet(resultSet);
    } catch (final SQLException e) {
      throw new DatabaseAccessException("Bad result-set", e);
    }
  }

  private static String extractDatabaseServerTypeFromUrl(final String url) {
    final Pattern urlPattern = Pattern.compile("jdbc:(.*?):.*");
    final Matcher matcher = urlPattern.matcher(url);
    if (!matcher.matches()) {
      return "";
    }
    final String urlDBServerType;
    if (matcher.groupCount() == 1) {
      final String matchedDBServerType = matcher.group(1);
      if (Arrays.asList(
              "db2", "hsqldb", "mariadb", "mysql", "oracle", "postgresql", "sqlite", "sqlserver")
          .contains(matchedDBServerType)) {
        urlDBServerType = matchedDBServerType;
      } else {
        urlDBServerType = null;
      }
    } else {
      urlDBServerType = null;
    }
    if (isBlank(urlDBServerType)) {
      return "";
    } else if ("mariadb".equals(urlDBServerType)) {
      // Special case: MariaDB is handled by the MySQL plugin
      return "mysql";
    }
    return urlDBServerType;
  }

  private static String getConnectionUrl(final Connection connection) {
    requireNonNull(connection, "No connection provided");
    final String url;
    try {
      url = connection.getMetaData().getURL();
    } catch (final SQLException e) {
      LOGGER.log(Level.CONFIG, "Cannot get connection URL");
      return "";
    }
    return url;
  }

  private static void logConnection(final Connection connection) {
    if (connection == null || !LOGGER.isLoggable(Level.INFO)) {
      return;
    }
    try {
      final ConnectionInfo connectionInfo = ConnectionInfoBuilder.builder(connection).build();
      LOGGER.log(Level.INFO, connectionInfo.toString());
    } catch (final SQLException e) {
      LOGGER.log(Level.WARNING, "Could not log connection information");
      LOGGER.log(Level.FINE, "Could not log connection information", e);
    }
  }

  private static boolean useMatchedDatabasePlugin(
      final Connection connection, final DatabaseServerType dbServerType) {

    // Get database connection URL
    final String url = getConnectionUrl(connection);
    if (isBlank(url)) {
      return true;
    }

    // Extract database server type
    final String urlDBServerType = extractDatabaseServerTypeFromUrl(url);
    if (isBlank(urlDBServerType)) {
      return true;
    }

    // Find out what is matched
    final boolean dbConnectorPresent =
        urlDBServerType.equalsIgnoreCase(dbServerType.getDatabaseSystemIdentifier());

    final String withoutDatabasePlugin =
        PropertiesUtility.getSystemConfigurationProperty("SC_WITHOUT_DATABASE_PLUGIN", "");
    final boolean useWithoutDatabasePlugin =
        urlDBServerType.equalsIgnoreCase(withoutDatabasePlugin);

    // Throw exception if plugin is needed, but not found
    if (!dbConnectorPresent && !useWithoutDatabasePlugin) {
      throw new InternalRuntimeException(
          String.format(
              "Add the SchemaCrawler database plugin for <%s> to the CLASSPATH for %n<%s>%n"
                  + "or set \"SC_WITHOUT_DATABASE_PLUGIN=%s\"%n"
                  + "either as an environmental variable or as a Java system property",
              urlDBServerType, url, urlDBServerType));
    }

    final boolean useMatchedDatabasePlugin = dbConnectorPresent && !useWithoutDatabasePlugin;

    return useMatchedDatabasePlugin;
  }

  private SchemaCrawlerUtility() {
    // Prevent instantiation
  }
}
