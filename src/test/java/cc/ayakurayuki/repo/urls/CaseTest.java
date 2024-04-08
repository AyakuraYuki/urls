package cc.ayakurayuki.repo.urls;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import cc.ayakurayuki.repo.urls.Cases.ParseRequestURLTest;
import cc.ayakurayuki.repo.urls.Cases.StringURLTest;
import cc.ayakurayuki.repo.urls.Cases.URLTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * @author Ayakura Yuki
 * @date 2024/04/07-11:43
 */
@RunWith(JUnit4.class)
public class CaseTest {

  /**
   * more useful string for debugging than printf struct printer
   */
  public String ufmt(URL u) {
    String user = null;
    String pass = null;
    if (u.getUser() != null) {
      user = u.getUser().username();
      if (u.getUser().isPasswordSet()) {
        pass = u.getUser().password();
      }
    }
    return String.format("opaque=%s, scheme=%s, user=%s, pass=%s, host=%s, path=%s, rawpath=%s, rawq=%s, frag=%s, rawfrag=%s, forcequery=%b, omithost=%b",
                         u.getOpaque(), u.getScheme(), user, pass, u.getHost(), u.getPath(), u.getRawPath(), u.getRawQuery(), u.getFragment(), u.getRawFragment(), u.isForceQuery(), u.isOmitHost());
  }

  @Test
  public void testParse() {
    for (URLTest tt : Cases.urlTests) {
      URL u = URLs.Parse(tt.in());
      assertEquals(String.format("Parse(%s)\n\tgot:  %s\n\twant: %s\n", tt.in(), ufmt(u), ufmt(tt.out())), tt.out(), u);
    }
  }

  @Test
  public void testParseRequestURI() {
    for (ParseRequestURLTest test : Cases.parseRequestURLTests) {
      try {
        URLs.ParseRequestURI(test.url);
        if (!test.expectedValid) {
          fail(String.format("ParseRequestURI(%s) gave no err; want some error", test.url));
        }
      } catch (Exception e) {
        if (test.expectedValid) {
          fail(String.format("ParseRequestURI(%s) gave err %s; want no error", test.url, e.getMessage()));
        }
      }
    }

    URL u = URL.empty;
    try {
      u = URLs.ParseRequestURI(Cases.pathThatLooksSchemeRelative);
    } catch (Exception e) {
      fail(String.format("Unexpected error %s", e.getMessage()));
    }
    if (!Strings.equals(u.getPath(), Cases.pathThatLooksSchemeRelative)) {
      fail(String.format("ParseRequestURI path:\n\tgot  %s\n\twant %s\n", u.getPath(), Cases.pathThatLooksSchemeRelative));
    }
  }

  @Test
  public void testURLString() {
    for (URLTest tt : Cases.urlTests) {
      URL u = URLs.Parse(tt.in());
      String expected = tt.in();
      if (Strings.isNotEmpty(tt.roundtrip())) {
        expected = tt.roundtrip();
      }
      String s = u.toString();
      if (!Strings.equals(s, expected)) {
        fail(String.format("Parse(%s).toString() == %s (expected %s)", tt.in(), s, expected));
      }
    }

    for (StringURLTest tt : Cases.stringURLTests) {
      String got = tt.url.toString();
      if (!Strings.equals(got, tt.want)) {
        fail(String.format("%s -> %s; want %s", ufmt(tt.url), got, tt.want));
      }
    }
  }

  @Test
  public void debugging() {
    String in = "http://hello.世界.com/foo";
    URL u = URLs.Parse(in);
    var s = System.currentTimeMillis();
    System.out.println(u.toString());
    System.out.printf("spent %sms%n", System.currentTimeMillis() - s);
  }

}
