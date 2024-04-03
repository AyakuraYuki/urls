package cc.ayakurayuki.repo.urls;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class UrlEncoderTest {

  static List<String>        unreservedCharacters;
  static Map<String, String> someReservedCharacters;
  static Map<String, String> someMultibyteCharacters;
  static Map<String, String> someSurrogatePairs;

  static {
    unreservedCharacters = new ArrayList<>();
    for (int i = 'a'; i <= 'z'; i++) {
      unreservedCharacters.add(Character.toString((char) i));
    }
    for (int i = 'A'; i <= 'Z'; i++) {
      unreservedCharacters.add(Character.toString((char) i));
    }
    for (int i = '0'; i <= '9'; i++) {
      unreservedCharacters.add(Character.toString((char) i));
    }
    unreservedCharacters.add("-");
    unreservedCharacters.add("_");
    unreservedCharacters.add(".");
    unreservedCharacters.add("*");
    unreservedCharacters.add("~");

    someReservedCharacters = new HashMap<>();
    someReservedCharacters.put("+", "%2B");
    someReservedCharacters.put("=", "%3D");
    someReservedCharacters.put("?", "%3F");
    someReservedCharacters.put("{", "%7B");
    someReservedCharacters.put("@", "%40");
    someReservedCharacters.put("&", "%26");
    someReservedCharacters.put("(", "%28");

    someMultibyteCharacters = new HashMap<>();
    someMultibyteCharacters.put("©", "%C2%A9");
    someMultibyteCharacters.put("®", "%C2%AE");

    someSurrogatePairs = new HashMap<>();
    someSurrogatePairs.put("\uD83D\uDE03", "%F0%9F%98%83");
  }

  @Test
  public void doesNotEncodeUnreservedCharacters() {
    for (String character : unreservedCharacters) {
      assertEquals(character, UrlEncoder.encode(character));
    }
  }

  @Test
  public void encodesReservedCharacters() {
    for (Map.Entry<String, String> entry : someReservedCharacters.entrySet()) {
      assertEquals(entry.getValue(), UrlEncoder.encode(entry.getKey()));
    }
  }

  @Test
  public void encodesMultibyteCharacters() {
    for (Map.Entry<String, String> entry : someMultibyteCharacters.entrySet()) {
      assertEquals(entry.getValue(), UrlEncoder.encode(entry.getKey()));
    }
  }

  @Test
  public void encodesSurrogatePairs() {
    for (Map.Entry<String, String> entry : someSurrogatePairs.entrySet()) {
      assertEquals(entry.getValue(), UrlEncoder.encode(entry.getKey()));
    }
  }

}
