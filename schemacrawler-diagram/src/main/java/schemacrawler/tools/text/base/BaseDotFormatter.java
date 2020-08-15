/*
========================================================================
SchemaCrawler
http://www.schemacrawler.com
Copyright (c) 2000-2020, Sualeh Fatehi <sualeh@hotmail.com>.
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

package schemacrawler.tools.text.base;


import static us.fatehi.utility.IOUtility.readResourceFully;
import static us.fatehi.utility.html.TagBuilder.tableCell;
import static us.fatehi.utility.html.TagBuilder.tableRow;
import static us.fatehi.utility.html.TagOutputFormat.html;

import java.util.Map;
import java.util.Map.Entry;

import schemacrawler.schema.CrawlInfo;
import schemacrawler.schema.DatabaseInfo;
import schemacrawler.schema.JdbcDriverInfo;
import schemacrawler.schemacrawler.SchemaCrawlerException;
import schemacrawler.tools.integration.diagram.DiagramOptions;
import schemacrawler.tools.options.OutputOptions;
import us.fatehi.utility.html.Alignment;
import us.fatehi.utility.html.Tag;

/**
 * Text formatting of schema.
 *
 * @author Sualeh Fatehi
 */
public abstract class BaseDotFormatter
  extends BaseFormatter<DiagramOptions>
{

  protected BaseDotFormatter(final DiagramOptions options,
                             final boolean printVerboseDatabaseInfo,
                             final OutputOptions outputOptions,
                             final String identifierQuoteString)
    throws SchemaCrawlerException
  {
    super(options,
          printVerboseDatabaseInfo,
          outputOptions,
          identifierQuoteString);
  }

  @Override
  public void begin()
  {
    final String header = makeGraphvizHeader();
    formattingHelper
      .append(header)
      .println();
  }

  @Override
  public void handle(final CrawlInfo crawlInfo)
  {
    if (crawlInfo == null)
    {
      return;
    }

    Tag row;

    if (outputOptions.hasTitle())
    {
      final String title = outputOptions.getTitle();
      row = tableRow().make();
      row.addInnerTag(tableCell()
                .withEscapedText(title)
                .withAlignment(Alignment.left)
                .withEmphasis(true)
                .withColumnSpan(2)
                .make());

      formattingHelper
        .append(row.render(html))
        .println();
    }

    if (options.isNoInfo())
    {
      return;
    }

    if (!options.isNoSchemaCrawlerInfo())
    {
      row = tableRow().make();
      row.addInnerTag(tableCell()
                .withEscapedText("generated by")
                .withAlignment(Alignment.right)
                .make());
      row.addInnerTag(tableCell()
                .withEscapedText(crawlInfo
                                   .getSchemaCrawlerVersion()
                                   .toString())
                .withAlignment(Alignment.left)
                .make());

      formattingHelper
        .append(row.render(html))
        .println();

      row = tableRow().make();
      row.addInnerTag(tableCell()
                .withEscapedText("generated on")
                .withAlignment(Alignment.right)
                .make());
      row.addInnerTag(tableCell()
                .withEscapedText(crawlInfo.getCrawlTimestamp())
                .withAlignment(Alignment.left)
                .make());

      formattingHelper
        .append(row.render(html))
        .println();
    }

    if (options.isShowDatabaseInfo())
    {
      row = tableRow().make();
      row.addInnerTag(tableCell()
                .withEscapedText("database version")
                .withAlignment(Alignment.right)
                .make());
      row.addInnerTag(tableCell()
                .withEscapedText(crawlInfo
                                   .getDatabaseVersion()
                                   .toString())
                .withAlignment(Alignment.left)
                .make());

      formattingHelper
        .append(row.render(html))
        .println();
    }
  }

  @Override
  public void handle(final DatabaseInfo dbInfo)
  {
    // No-op
  }

  @Override
  public void handle(final JdbcDriverInfo driverInfo)
  {
    // No-op
  }

  @Override
  public void handleHeaderEnd()
    throws SchemaCrawlerException
  {
    if (options.isNoInfo() && !outputOptions.hasTitle())
    {
      return;
    }

    formattingHelper
      .append("      </table>")
      .println();
    formattingHelper
      .append("    >")
      .println();
    formattingHelper
      .append("  ];")
      .println();
    formattingHelper.println();
  }

  @Override
  public void handleHeaderStart()
    throws SchemaCrawlerException
  {
    if (options.isNoInfo() && !outputOptions.hasTitle())
    {
      return;
    }

    formattingHelper
      .append("  /* ")
      .append("Title Block")
      .append(" -=-=-=-=-=-=-=-=-=-=-=-=-=- */")
      .println();
    formattingHelper
      .append("  graph [ ")
      .println();
    formattingHelper
      .append("    label=<")
      .println();
    formattingHelper
      .append(
        "      <table border=\"1\" cellborder=\"0\" cellspacing=\"0\" color=\"#888888\">")
      .println();
  }

  @Override
  public void handleInfoEnd()
    throws SchemaCrawlerException
  {
    // No-op
  }

  @Override
  public void handleInfoStart()
    throws SchemaCrawlerException
  {
    // No-op
  }

  @Override
  public void end()
    throws SchemaCrawlerException
  {
    formattingHelper
      .append("}")
      .println();

    super.end();
  }

  private String makeGraphvizAttributes(final Map<String, String> graphvizAttributes,
                                        final String prefix)
  {
    final StringBuilder buffer = new StringBuilder();
    for (final Entry<String, String> entry : graphvizAttributes.entrySet())
    {
      final String[] key = entry
        .getKey()
        .split("\\.");
      if (key.length == 2 && key[0].equals(prefix))
      {
        buffer
          .append("    ")
          .append(key[1])
          .append("=")
          .append("\"")
          .append(entry.getValue())
          .append("\"")
          .append("\n");
      }
    }
    return buffer.toString();
  }

  private String makeGraphvizHeader()
  {
    final Map<String, String> graphvizAttributes =
      options.getGraphvizAttributes();
    final String graphvizHeaderTemplate = readResourceFully("/dot.header.txt");
    final String graphvizHeader = String.format(graphvizHeaderTemplate,
                                                makeGraphvizAttributes(
                                                  graphvizAttributes,
                                                  "graph"),
                                                makeGraphvizAttributes(
                                                  graphvizAttributes,
                                                  "node"),
                                                makeGraphvizAttributes(
                                                  graphvizAttributes,
                                                  "edge"));
    return graphvizHeader;
  }

}
