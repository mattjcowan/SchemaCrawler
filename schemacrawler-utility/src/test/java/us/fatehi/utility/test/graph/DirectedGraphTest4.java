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
package us.fatehi.utility.test.graph;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.junit.jupiter.api.Test;

import us.fatehi.utility.graph.DirectedGraph;

/**
 * Exercises from https://algocoding.wordpress.com/2015/04/02/detecting-cycles-in-a-
 * directed-graph-with-dfs-python/
 */
public class DirectedGraphTest4 extends GraphTestBase {

  @Test
  public void test1() throws Exception {
    final DirectedGraph<Integer> graph =
        new DirectedGraph<Integer>("") {
          {
            addEdge(0, 1);
            addEdge(0, 2);
            addEdge(2, 3);
            addEdge(3, 4);
            addEdge(4, 2);
          }
        };

    assertThat(containsCycleSimple(graph), is(true));
    assertThat(containsCycleTarjan(graph), is(true));
  }

  @Test
  public void test2() throws Exception {
    final DirectedGraph<Integer> graph =
        new DirectedGraph<Integer>("") {
          {
            addVertex(0);
            addVertex(1);
            addVertex(2);
            addVertex(3);
          }
        };

    assertThat(containsCycleSimple(graph), is(false));
    assertThat(containsCycleTarjan(graph), is(false));
  }

  @Test
  public void test3() throws Exception {
    final DirectedGraph<Integer> graph =
        new DirectedGraph<Integer>("") {
          {
            addVertex(0);
            addEdge(1, 2);
            addEdge(3, 4);
            addEdge(4, 5);
            addEdge(5, 3);
          }
        };

    assertThat(containsCycleSimple(graph), is(true));
    assertThat(containsCycleTarjan(graph), is(true));
  }

  @Test
  public void test4() throws Exception {
    final DirectedGraph<Integer> graph =
        new DirectedGraph<Integer>("") {
          {
            addEdge(0, 1);
            addEdge(0, 2);
            addEdge(1, 3);
            addEdge(1, 4);
            addEdge(5, 6);
            addEdge(5, 7);
          }
        };

    assertThat(containsCycleSimple(graph), is(false));
    assertThat(containsCycleTarjan(graph), is(false));
  }
}
