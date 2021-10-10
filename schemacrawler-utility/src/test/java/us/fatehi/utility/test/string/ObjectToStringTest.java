package us.fatehi.utility.test.string;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.nio.file.AccessMode;

import org.junit.jupiter.api.Test;

import us.fatehi.test.utility.TestObject;
import us.fatehi.test.utility.TestUtility;
import us.fatehi.utility.ObjectToString;

public class ObjectToStringTest {

  @Test
  public void serialize() {

    final TestObject testObject = TestUtility.makeTestObject();

    System.out.println(ObjectToString.toString(testObject));
  }

  @Test
  public void toStringTest() {
    assertThat(ObjectToString.toString(null), is("null"));

    assertThat(
        ObjectToString.toString(new Object()).replaceAll("\\R", ""),
        is("{  \"@object\": \"java.lang.Object\"}"));
    assertThat(ObjectToString.toString(new int[] {1, 2}), is("[1, 2]"));
    assertThat(ObjectToString.toString(new String[] {"1", "2"}), is("[\"1\", \"2\"]"));

    assertThat(ObjectToString.toString("hello, world"), is("hello, world"));
    assertThat(ObjectToString.toString(AccessMode.READ), is("READ"));
    assertThat(ObjectToString.toString('a'), is("a"));
    assertThat(ObjectToString.toString(Character.valueOf('a')), is("a"));

    assertThat(ObjectToString.toString(1), is("1"));
    assertThat(ObjectToString.toString(Integer.valueOf(1)), is("1"));
    assertThat(ObjectToString.toString(1.1), is("1.1"));
    assertThat(ObjectToString.toString(Double.valueOf(1.1)), is("1.1"));
    assertThat(ObjectToString.toString(true), is("true"));
    assertThat(ObjectToString.toString(Boolean.TRUE), is("true"));

    assertThat(
        ObjectToString.toString(TestUtility.makeTestObject()).replaceAll("\\R", ""),
        is(
            "{  \"@object\": \"us.fatehi.test.utility.TestObject\",  \"integerList\": [1, 1, 2, 3, 5, 8],  "
                + "\"map\":   {    \"1\": \"a\",    \"2\": \"b\",    \"3\": \"c\"  },  \"nullValue\": null,  "
                + "\"objectArray\": [\"a\", \"b\", \"c\"],  \"plainString\": \"hello world\",  "
                + "\"primitiveArray\": [1, 1, 2, 3, 5, 8],  \"primitiveBoolean\": true,  \"primitiveDouble\": 99.99,  "
                + "\"primitiveEnum\": \"READ\",  \"primitiveInt\": 99,  \"subObject\": \".\"}"));
  }
}
