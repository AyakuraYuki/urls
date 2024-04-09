package cc.ayakurayuki.repo.urls;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import cc.ayakurayuki.repo.urls.Cases.EncodeQueryTest;
import cc.ayakurayuki.repo.urls.Cases.EscapeTest;
import cc.ayakurayuki.repo.urls.Cases.ParseRequestURLTest;
import cc.ayakurayuki.repo.urls.Cases.ParseTest;
import cc.ayakurayuki.repo.urls.Cases.RequestURITest;
import cc.ayakurayuki.repo.urls.Cases.ResolveTest;
import cc.ayakurayuki.repo.urls.Cases.StringURLTest;
import cc.ayakurayuki.repo.urls.Cases.URLRedactedTest;
import cc.ayakurayuki.repo.urls.Cases.URLTest;
import cc.ayakurayuki.repo.urls.wrapper.Result;
import java.util.List;
import java.util.Map;
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
  public void testURLRedacted() {
    for (URLRedactedTest tt : Cases.urlRedactedTests) {
      String got = tt.url.redacted();
      assertEquals(String.format("redacted(%s) == %s (expected %s)", tt.url, got, tt.want), tt.want, got);
    }
  }

  @Test
  public void testUnescape() {
    for (EscapeTest tt : Cases.unescapeTests) {
      Result<String, Exception> unescapeResult = URLs.QueryUnescape(tt.in);
      String actual = unescapeResult.ok();
      Exception err = unescapeResult.err();
      if (!Strings.equals(actual, tt.out) || (err == null) == (tt.err != null)) {
        fail(String.format("QueryUnescape(%s) == %s, %s; want %s, %s", tt.in, actual, err, tt.out, tt.err));
      }

      String in = tt.in;
      String out = tt.out;
      if (Strings.contains(tt.in, "+")) {
        in = tt.in.replace("+", "%20");
        unescapeResult = URLs.PathUnescape(in);
        actual = unescapeResult.ok();
        err = unescapeResult.err();
        if (!Strings.equals(actual, tt.out) || (err == null) == (tt.err != null)) {
          fail(String.format("PathUnescape(%s) == %s, %s; want %s, %s", tt.in, actual, err, tt.out, tt.err));
        }
        if (tt.err == null) {
          unescapeResult = URLs.QueryUnescape(tt.in.replace("+", "XXX"));
          err = unescapeResult.err();
          if (err != null) {
            continue;
          }
          in = tt.in;
          String s = unescapeResult.ok();
          out = s.replace("XXX", "+");
        }
      }

      unescapeResult = URLs.PathUnescape(in);
      actual = unescapeResult.ok();
      err = unescapeResult.err();
      if (!Strings.equals(actual, out) || (err == null) == (tt.err != null)) {
        fail(String.format("PathUnescape(%s) == %s, %s; want %s, %s", in, actual, err, out, tt.err));
      }
    }
  }

  @Test
  public void testQueryEscape() {
    for (EscapeTest tt : Cases.queryEscapeTests) {
      String actual = URLs.QueryEscape(tt.in);
      assertEquals(String.format("QueryEscape(%s) == %s (expected %s)", tt.in, actual, tt.out), tt.out, actual);

      // for bonus points, verify that escape:unescape is an identity.
      Result<String, Exception> unescapeResult = URLs.QueryUnescape(actual);
      String roundtrip = unescapeResult.ok();
      Exception err = unescapeResult.err();
      if (!Strings.equals(roundtrip, tt.in) || err != null) {
        fail(String.format("QueryUnescape(%s) = %s, %s; want %s, %s", actual, roundtrip, err, tt.in, "[no error]"));
      }
    }
  }

  @Test
  public void testPathEscape() {
    for (EscapeTest tt : Cases.pathEscapeTests) {
      String actual = URLs.PathEscape(tt.in);
      assertEquals(String.format("PathEscape(%s) == %s (expected %s)", tt.in, actual, tt.out), tt.out, actual);

      // for bonus points, verify that escape:unescape is an identity.
      Result<String, Exception> unescapeResult = URLs.PathUnescape(actual);
      String roundtrip = unescapeResult.ok();
      Exception err = unescapeResult.err();
      if (!Strings.equals(roundtrip, tt.in) || err != null) {
        fail(String.format("PathUnescape(%s) = %s, %s; want %s, %s", actual, roundtrip, err, tt.in, "[no error]"));
      }
    }
  }

  @Test
  public void testEncodeQuery() {
    for (EncodeQueryTest tt : Cases.encodeQueryTests) {
      String q = tt.m.encode();
      assertEquals(String.format("[%s].encode() got %s, but want %s", tt.m, q, tt.expected), tt.expected, q);
    }
  }

  @Test
  public void testResolvePath() {
    for (ResolveTest test : Cases.resolvePathTests) {
      String got = URLs.resolvePath(test.base, test.ref);
      assertEquals(String.format("for %s + %s, got %s; expected %s", test.base, test.ref, got, test.expected), test.expected, got);
    }
  }

  private URL mustParse(String invokeFrom, String url) {
    try {
      return URLs.Parse(url);
    } catch (Exception e) {
      fail(String.format("Unexpected error when parsing url %s from method %s, error: %s", url, invokeFrom, e.getMessage()));
      return URL.empty; // unreachable line but necessary for completing function declaration
    }
  }

  @Test
  public void testResolveReference() {
    URL opaque = URL.builder().scheme("scheme").opaque("opaque").build();
    for (ResolveTest test : Cases.resolveReferenceTests) {
      URL base = mustParse("testResolveReference", test.base);
      URL rel = mustParse("testResolveReference", test.ref);
      URL url = base.resolveReference(rel);
      String got = url.toString();
      assertEquals(String.format("URL(%s).resolveReference(%s)\n\tgot  %s\n\twant %s", test.base, test.ref, got, test.expected), test.expected, got);
      // Ensure that new instances are returned.
      assertNotSame(String.format("Expected URL.ResolveReference to return new URL instance. base: %s, url: %s\n", base, url), base, url);

      // Test the convenience wrapper too.
      try {
        url = base.parse(test.ref);
        got = url.toString();
        assertEquals(String.format("URL(%s).parse(%s)\n\tgot  %s\n\twant %s", test.base, test.ref, got, test.expected), test.expected, got);
        // Ensure that new instances are returned for the wrapper too.
        assertNotSame(String.format("Expected URL.parse to return new URL instance. base: %s, url: %s\n", base, url), base, url);
      } catch (Exception e) {
        fail(String.format("URL(%s).parse(%s) failed: %s", test.base, test.ref, e.getMessage()));
      }

      // Ensure Opaque resets the URL.
      url = base.resolveReference(opaque);
      assertEquals(String.format("resolveReference failed to resolve opaque URL:\n\tgot  %s\n\twant %s\n", url, opaque), opaque, url);

      // Test the convenience wrapper with an opaque URL too.
      try {
        url = base.parse("scheme:opaque");
        assertEquals(String.format("parse failed to resolve opaque URL:\n\tgot  %s\n\twant %s\n", url, opaque), opaque, url);
        assertNotSame(String.format("Expected URL.parse to return new URL instance. base: %s, url: %s\n", base, url), base, url);
      } catch (Exception e) {
        fail(String.format("URL(%s).parse(\"scheme:opaque\") failed: %s", test.base, e.getMessage()));
      }
    }
  }

  @Test
  public void testQueryValues() {
    String url = "https://x.com?foo=bar&bar=1&bar=2&baz";
    URL u = URLs.Parse(url);
    Values v = u.query();
    assertNotNull(String.format("URLs.Parse(%s) missing query values", url), v);
    assertEquals(String.format("got %d keys in Query values, want 3", v.size()), 3, v.size());

    assertEquals("bar", v.value("foo"));
    // Case sensitive:
    assertEquals("", v.value("Foo"));
    assertEquals("1", v.value("bar"));
    assertEquals("", v.value("baz"));
    assertTrue(v.has("foo"));
    assertTrue(v.has("bar"));
    assertTrue(v.has("baz"));
    assertFalse(v.has("noexist"));

    v.del("bar");
    assertEquals("", v.value("bar"));
  }

  @Test
  public void testParseQuery() {
    for (ParseTest test : Cases.parseTests) {
      Result<Values, Exception> parseResult = URLs.ParseQuery(test.query);
      if (test.ok == parseResult.isErr()) {
        String want = "<error>";
        if (test.ok) {
          want = "<null>";
        }
        fail(String.format("Unexpected error: %s, want %s", parseResult.err(), want));
      }

      Values form = parseResult.ok();
      assertEquals(String.format("ParseQuery(%s) got values with size %d, want %d", test.query, form.size(), test.out.size()), test.out.size(), form.size());

      for (Map.Entry<String, List<String>> entry : test.out.entrySet()) {
        String k = entry.getKey();
        List<String> evs = entry.getValue();
        if (!form.has(k)) {
          fail(String.format("missing key %s in form after parsed %s", k, test.query));
        }
        List<String> vs = form.get(k);
        assertEquals(String.format("size of form[%s] is wrong, got %d, want %d", k, vs.size(), evs.size()), evs.size(), vs.size());
        for (int i = 0; i < evs.size(); i++) {
          String v = vs.get(i);
          assertEquals(String.format("form[%s][%d] = %s, want %s", k, i, v, evs.get(i)), evs.get(i), v);
        }
      }
    }
  }

  @Test
  public void testRequestURI() {
    for (RequestURITest tt : Cases.requritests) {
      String s = tt.url.requestURI();
      assertEquals(String.format("[%s].requestURI() == %s (expected %s)", ufmt(tt.url), s, tt.out), tt.out, s);
    }
  }

  @Test
  public void testParseFailure() {
    String url = "%gh&%ij";
    Result<Values, Exception> result = URLs.ParseQuery(url);
    Exception err = result.err();
    if (!Strings.contains(err.getMessage(), "%gh")) {
      fail(String.format("ParseQuery(%s) returned error %s, want something containing %s", url, err.getMessage(), "%gh"));
    }
  }

}
