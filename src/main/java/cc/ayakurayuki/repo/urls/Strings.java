package cc.ayakurayuki.repo.urls;

import cc.ayakurayuki.repo.urls.wrapper.CutResult;
import org.apache.commons.lang3.StringUtils;

/**
 * Strings is a toolbox for {@link String} or {@link CharSequence}, extends {@link org.apache.commons.lang3.StringUtils}
 *
 * @author Ayakura Yuki
 * @date 2024/04/03-16:26
 */
public abstract class Strings extends StringUtils {

  /**
   * cut string s around the first instance of sep,
   * returning the text before and after sep.
   * The found result reports whether sep appears in s.
   * If sep does not appear in s, cut returns s, "", false.
   */
  public static CutResult cut(String s, String sep) {
    // special case
    if (s == null) {
      return new CutResult(null, "", false);
    }

    int i = s.indexOf(sep);
    if (i >= 0) {
      return new CutResult(s.substring(0, i), s.substring(i + sep.length()), true);
    }
    return new CutResult(s, "", false);
  }

  /**
   * count counts the number of non-overlapping instances of substr in s.
   * <p>
   * If substr is an empty string, count returns 1 + the number of Unicode code points in s.
   */
  public static int count(String s, String substr) {
    // special case
    if (s == null) {
      return 0;
    }

    // special case
    if (substr == null || substr.isEmpty()) {
      return (int) (1 + s.codePoints().count());
    }

    String str = s;
    int n = 0;
    for (; ; ) {
      int i = str.indexOf(substr);
      if (i == -1) {
        return n;
      }
      n++;
      str = str.substring(i + substr.length());
    }
  }

  /**
   * containsCTLByte reports whether s contains any ASCII control character.
   */
  public static boolean containsCTLByte(String s) {
    for (int i = 0; i < s.length(); i++) {
      char b = s.charAt(i);
      if (b < ' ' || b == 0x7f) {
        return true;
      }
    }
    return false;
  }

  /**
   * contains reports whether substr is within s.
   */
  public static boolean contains(String s, String substr) {
    if (isEmpty(s)) {
      return false;
    }
    return s.contains(substr);
  }

  public static boolean startsWith(String s, String prefix) {
    return startsWith(s, prefix, 0);
  }

  public static boolean startsWith(String s, String prefix, int offset) {
    return length(s) >= length(prefix) && s.startsWith(prefix, offset);
  }

  public static boolean endsWith(String s, String suffix) {
    return length(s) >= length(suffix) && s.endsWith(suffix);
  }

  public static int lastIndexChar(char[] s, char c) {
    for (int i = s.length - 1; i >= 0; i--) {
      if (s[i] == c) {
        return i;
      }
    }
    return -1;
  }

  public static int lastIndexCharString(String s, char c) {
    for (int i = s.length() - 1; i >= 0; i--) {
      if (s.charAt(i) == c) {
        return i;
      }
    }
    return -1;
  }

}
