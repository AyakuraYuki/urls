package cc.ayakurayuki.repo.urls;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import cc.ayakurayuki.repo.urls.Cases.EncodeQueryTest;
import cc.ayakurayuki.repo.urls.Cases.EscapeTest;
import cc.ayakurayuki.repo.urls.Cases.JoinPathTest;
import cc.ayakurayuki.repo.urls.Cases.ParseErrorsTest;
import cc.ayakurayuki.repo.urls.Cases.ParseRequestURLTest;
import cc.ayakurayuki.repo.urls.Cases.ParseTest;
import cc.ayakurayuki.repo.urls.Cases.RequestURITest;
import cc.ayakurayuki.repo.urls.Cases.ResolveTest;
import cc.ayakurayuki.repo.urls.Cases.ShouldEscapeTest;
import cc.ayakurayuki.repo.urls.Cases.StringURLTest;
import cc.ayakurayuki.repo.urls.Cases.URLHostnameAndPortTest;
import cc.ayakurayuki.repo.urls.Cases.URLRedactedTest;
import cc.ayakurayuki.repo.urls.Cases.URLTest;
import cc.ayakurayuki.repo.urls.wrapper.Result;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.util.Arrays;
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
    if (u == null) {
      return null;
    }
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
      Result<URL, Exception> parsed = URLs.Parse(tt.in());
      if (parsed.isErr()) {
        fail(String.format("Parse(\"%s\") returned error %s", tt.in(), parsed.err().getMessage()));
        continue;
      }
      URL u = parsed.ok();
      assertEquals(String.format("Parse(%s)\n\tgot:  %s\n\twant: %s\n", tt.in(), ufmt(u), ufmt(tt.out())), tt.out(), u);
    }
  }

  @Test
  public void testParseRequestURI() {
    for (ParseRequestURLTest test : Cases.parseRequestURLTests) {
      Exception err = URLs.ParseRequestURI(test.url).err();
      if (test.expectedValid && err != null) {
        fail(String.format("ParseRequestURI(%s) gave err %s; want no error", test.url, err.getMessage()));
      } else if (!test.expectedValid && err == null) {
        fail(String.format("ParseRequestURI(%s) gave no err; want some error", test.url));
      }
    }

    Result<URL, Exception> parsed = URLs.ParseRequestURI(Cases.pathThatLooksSchemeRelative);
    if (parsed.isErr()) {
      fail(String.format("Unexpected error %s", parsed.err().getMessage()));
    }
    if (!Strings.equals(parsed.ok().getPath(), Cases.pathThatLooksSchemeRelative)) {
      fail(String.format("ParseRequestURI path:\n\tgot  %s\n\twant %s\n", parsed.ok().getPath(), Cases.pathThatLooksSchemeRelative));
    }
  }

  @Test
  public void testURLString() {
    for (URLTest tt : Cases.urlTests) {
      Result<URL, Exception> parsed = URLs.Parse(tt.in());
      if (parsed.isErr()) {
        fail(String.format("Parse(\"%s\") returned error %s", tt.in(), parsed.err().getMessage()));
      }
      URL u = parsed.ok();
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

  private URL mustParse(String url) {
    Result<URL, Exception> parsed = URLs.Parse(url);
    if (parsed.isErr()) {
      fail(String.format("[testResolveReference] Parse(\"%s\") got error %s", url, parsed.err().getMessage()));
    }
    return parsed.ok();
  }

  @Test
  public void testResolveReference() {
    URL opaque = URL.builder().scheme("scheme").opaque("opaque").build();
    for (ResolveTest test : Cases.resolveReferenceTests) {
      URL base = mustParse(test.base);
      URL rel = mustParse(test.ref);
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
    URL u = URLs.Parse(url).ok();
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

  @Test
  public void testParseErrors() {
    for (ParseErrorsTest tt : Cases.parseErrorsTests) {
      Result<URL, Exception> parsed = URLs.Parse(tt.in);
      if (tt.wantErr) {
        if (!parsed.isErr()) {
          fail(String.format("Parse(\"%s\") = %s; want an error", tt.in, ufmt(parsed.ok())));
        }
        continue;
      }
      if (parsed.isErr()) {
        fail(String.format("Parse(%s) = %s, want no error", tt.in, parsed.err().getMessage()));
      }
    }
  }

  // issue 11202
  @Test
  public void testStarRequest() {
    Result<URL, Exception> parsed = URLs.Parse("*");
    if (parsed.isErr()) {
      fail(String.format("unexpected exception: %s", parsed.err().getMessage()));
    }
    URL u = parsed.ok();
    String got = u.requestURI();
    assertEquals("*", got);
  }

  @Test
  public void testShouldEscape() {
    for (ShouldEscapeTest tt : Cases.shouldEscapeTests) {
      boolean got = URLs.shouldEscape(tt.in, tt.mode);
      assertEquals(String.format("shouldEscape(%s, %s) returned %b; expected %b", tt.in, tt.mode, got, tt.escape), tt.escape, got);
    }
  }

  @Test
  public void testURLHostnameAndPort() {
    for (URLHostnameAndPortTest tt : Cases.urlHostnameAndPortTests) {
      URL u = URL.builder().host(tt.in).build();
      String host = u.hostname();
      String port = u.port();
      assertEquals(String.format("hostname for host %s = %s; want %s", tt.in, host, tt.host), tt.host, host);
      assertEquals(String.format("port for host %s = %s; want %s", tt.in, host, tt.host), tt.port, port);
    }
  }

  @Test
  public void testJSON() {
    Result<URL, Exception> parsed = URLs.Parse("https://www.google.com/x?y=z");
    if (parsed.isErr()) {
      fail(String.format("unexpected exception: %s", parsed.err().getMessage()));
    }
    URL u = parsed.ok();
    Gson gson = new GsonBuilder()
        .disableHtmlEscaping()
        .create();
    String json = gson.toJson(u);
    URL u1 = gson.fromJson(json, URL.class);
    assertEquals(u, u1);
    assertEquals(u.toString(), u1.toString());
  }

  @Test
  public void testNullUser() {
    Result<URL, Exception> parsed = URLs.Parse("http://foo.com/");
    if (parsed.isErr()) {
      fail(String.format("unexpected exception: %s", parsed.err().getMessage()));
    }
    URL u = parsed.ok();
    assertEquals("", u.username());
    assertEquals("", u.password());
    assertFalse(u.isPasswordSet());
    assertEquals("", u.userToString());
  }

  @Test
  public void testInvalidUserPassword() {
    try {
      URLs.Parse("http://user^:passwo^rd@foo.com/");
    } catch (Exception e) {
      if (!Strings.contains(e.getMessage(), "invalid userinfo")) {
        fail(String.format("unexpected exception: %s (expected substring \"invalid userinfo\")", e.getMessage()));
      }
    }
  }

  @Test
  public void testRejectControlCharacters() {
    List<String> tests = List.of(
        "http://foo.com/?foo\nbar",
        "http\r://foo.com/",
        "http://foo\u007f.com/"
    );
    final String wantSub = "invalid control characters in url";
    for (String s : tests) {
      try {
        URLs.Parse(s);
      } catch (Exception e) {
        if (!Strings.contains(e.getMessage(), wantSub)) {
          fail(String.format("unexpected exception: %s (expected substring \"invalid control characters in url\")", e.getMessage()));
        }
      }
    }

    // But don't reject non-ASCII CTLs, at least for now:
    try {
      URLs.Parse("http://foo.com/ctl\u0080");
    } catch (Exception e) {
      fail(String.format("error parsing URL with non-ASCII control byte: %s", e.getMessage()));
    }
  }

  @Test
  public void testJoinPath() {
    for (JoinPathTest tt : Cases.joinPathTests) {
      String wantErr = "null";
      if (Strings.isEmpty(tt.out)) {
        wantErr = "non-null error";
      }

      Result<String, Exception> joinPathResult = URLs.JoinPath(tt.base, tt.elem);
      String out = joinPathResult.ok();
      Exception err = joinPathResult.err();
      if (!Strings.equals(out, tt.out) || ((err == null) != (Strings.isNotEmpty(tt.out)))) {
        fail(String.format("JoinPath(\"%s\", \"%s\") = \"%s\", %s; want \"%s\", %s", tt.base, Arrays.toString(tt.elem), out, err, tt.out, wantErr));
      }

      Result<URL, Exception> parsed = URLs.Parse(tt.base);
      URL u = parsed.ok();
      err = parsed.err();
      if (!parsed.isErr()) {
        u = u.joinPath(tt.elem);
        out = u.toString();
      }
      if (!Strings.equals(out, tt.out) || ((err == null) != (Strings.isNotEmpty(tt.out)))) {
        fail(String.format("Parse(\"%s\").joinPath(\"%s\") = \"%s\", %s; want \"%s\", %s", tt.base, Arrays.toString(tt.elem), out, err, tt.out, wantErr));
      }
    }
  }

}
