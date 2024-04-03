package cc.ayakurayuki.repo.urls;

import cc.ayakurayuki.repo.urls.wrapper.Pair;
import java.util.Arrays;

/**
 * @author Ayakura Yuki
 * @date 2024/04/03-18:35
 */
public abstract class Paths {

  /**
   * A LazyBuf is a lazily constructed path buffer.
   * It supports append, reading previously appended bytes,
   * and retrieving the final string. It does not allocate a buffer
   * to hold the output until that output diverges from s.
   */
  private static class LazyBuf {

    private final String s;
    private       char[] buf;
    private       int    w;

    private LazyBuf(String s) {
      this.s = s;
    }

    char index(int i) {
      if (buf != null) {
        return buf[i];
      }
      return s.charAt(i);
    }

    void append(char c) {
      if (buf == null) {
        if (w < s.length() && s.charAt(w) == c) {
          w++;
          return;
        }
        buf = new char[s.length() * 2];
        char[] initialChars = s.substring(0, w).toCharArray();
        System.arraycopy(initialChars, 0, buf, 0, initialChars.length);
      }
      buf[w] = c;
      w++;
    }

    String string() {
      if (buf == null) {
        return s.substring(0, w);
      }
      return String.valueOf(Arrays.copyOfRange(buf, 0, w));
    }

  }

  /**
   * Clean returns the shortest path name equivalent to path
   * by purely lexical processing. It applies the following rules
   * iteratively until no further processing can be done:
   * <p>
   *   <ol>
   *     <li>Replace multiple slashes with a single slash.</li>
   *     <li>Eliminate each . path name element (the current directory).</li>
   *     <li>Eliminate each inner .. path name element (the parent directory) along with the non-.. element that precedes it.</li>
   *     <li>Eliminate .. elements that begin a rooted path: that is, replace "/.." by "/" at the beginning of a path.</li>
   *   </ol>
   *  The returned path ends in a slash only if it is the root "/".
   * <p>
   *  If the result of this process is an empty string, Clean
   *  returns the string ".".
   * <p>
   *  See also Rob Pike, “Lexical File Names in Plan 9 or
   *  Getting Dot-Dot Right,”
   *  <a href="https://9p.io/sys/doc/lexnames.html">https://9p.io/sys/doc/lexnames.html</a>
   */
  public static String clean(String path) {
    if (Strings.isEmpty(path)) {
      return ".";
    }

    boolean rooted = path.charAt(0) == '/';
    int n = path.length();

    // Invariants:
    //	reading from path; r is index of next byte to process.
    //	writing to buf; w is index of next byte to write.
    //	dotdot is index in buf where .. must stop, either because
    //		it is the leading slash or it is a leading ../../.. prefix.
    LazyBuf out = new LazyBuf(path);
    int r = 0;
    int dotdot = 0;
    if (rooted) {
      out.append('/');
      r = 1;
      dotdot = 1;
    }

    while (r < n) {
      if (path.charAt(r) == '/') {
        // empty path element
        r++;
      } else if (path.charAt(r) == '.' && (r + 1 == n || path.charAt(r + 1) == '/')) {
        // . element
        r++;
      } else if (path.charAt(r) == '.' && path.charAt(r + 1) == '.' && (r + 2 == n || path.charAt(r + 2) == '/')) {
        // .. element: remove to last /
        r += 2;
        if (out.w > dotdot) {
          // can backtrack
          out.w--;
          while (out.w > dotdot && out.index(out.w) != '/') {
            out.w--;
          }
        } else if (!rooted) {
          // cannot backtrack, but not rooted, so append .. element.
          if (out.w > 0) {
            out.append('/');
          }
          out.append('.');
          out.append('.');
          dotdot = out.w;
        }
      } else {
        // real path element.
        // add slash if needed
        if ((rooted && out.w != 1) || (!rooted && out.w != 0)) {
          out.append('/');
        }
        // copy element
        for (; r < n && path.charAt(r) != '/'; r++) {
          out.append(path.charAt(r));
        }
      }
    }

    // Turn empty string into "."
    if (out.w == 0) {
      return ".";
    }

    return out.string();
  }

  /**
   * Split splits path immediately following the final slash,
   * separating it into a directory and file name component.
   * If there is no slash in path, Split returns an empty dir and
   * file set to path.
   * <p>
   * The returned values have the property that path = dir+file.
   *
   * @return a pair of split result, {@code pair.a} is dir, {@code pair.b} is file.
   */
  public static Pair<String, String> split(String path) {
    int i = Strings.lastIndexCharString(path, '/');
    return new Pair<>(path.substring(0, i + 1), path.substring(i + 1));
  }

  /**
   * Join joins any number of path elements into a single path,
   * separating them with slashes. Empty elements are ignored.
   * The result is Cleaned. However, if the argument list is
   * empty or all its elements are empty, Join returns
   * an empty string.
   */
  public static String join(String... elem) {
    int size = 0;
    for (String e : elem) {
      size += e.length();
    }
    if (size == 0) {
      return "";
    }
    StringBuilder buf = new StringBuilder();
    for (String e : elem) {
      if (buf.length() > 0 || Strings.isNotEmpty(e)) {
        if (buf.length() > 0) {
          buf.append('/');
        }
      }
      buf.append(e);
    }
    return clean(buf.toString());
  }

  /**
   * Ext returns the file name extension used by path.
   * The extension is the suffix beginning at the final dot
   * in the final slash-separated element of path;
   * it is empty if there is no dot.
   */
  public static String ext(String path) {
    for (int i = path.length() - 1; i >= 0 && path.charAt(i) != '/'; i--) {
      if (path.charAt(i) == '.') {
        return path.substring(i);
      }
    }
    return "";
  }

  /**
   * Base returns the last element of path.
   * Trailing slashes are removed before extracting the last element.
   * <p>
   * If the path is empty, Base returns ".".
   * <p>
   * If the path consists entirely of slashes, Base returns "/".
   */
  public static String base(String path) {
    if (Strings.isEmpty(path)) {
      return ".";
    }
    // Strip trailing slashes.
    while (!path.isEmpty() && path.charAt(path.length() - 1) == '/') {
      path = path.substring(0, path.length() - 1);
    }
    // Find the last element
    int i = Strings.lastIndexCharString(path, '/');
    if (i >= 0) {
      path = path.substring(i + 1);
    }
    // If empty now, it had only slashes.
    if (Strings.isEmpty(path)) {
      return "/";
    }
    return path;
  }

  /**
   * reports whether the path is absolute.
   */
  public static boolean isAbs(String path) {
    return !path.isEmpty() && path.charAt(0) == '/';
  }

  /**
   * Dir returns all but the last element of path, typically the path's directory.
   * After dropping the final element using [Split], the path is Cleaned and trailing
   * slashes are removed.
   * <p>
   * If the path is empty, Dir returns ".".
   * <p>
   * If the path consists entirely of slashes followed by non-slash bytes, Dir
   * returns a single slash. In any other case, the returned path does not end in a
   * slash.
   */
  public static String dir(String path) {
    Pair<String, String> pair = split(path);
    return clean(pair.getA());
  }

}
