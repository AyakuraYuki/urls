package cc.ayakurayuki.repo.urls;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import cc.ayakurayuki.repo.urls.wrapper.Result;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.util.LinkedList;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * @author Ayakura Yuki
 * @date 2024/04/07-09:49
 */
@RunWith(JUnit4.class)
public class ExampleTest {

  @Test
  public void examplePathEscape() {
    String path = URLs.PathEscape("my/cool+blog&about,stuff");
    assertEquals("my%2Fcool+blog&about%2Cstuff", path);
  }

  @Test
  public void examplePathUnescape() {
    String escapedPath = "my%2Fcool+blog&about%2Cstuff";
    String path = URLs.PathUnescape(escapedPath).ok();
    assertEquals("my/cool+blog&about,stuff", path);
  }

  @Test
  public void exampleQueryEscape() {
    String query = URLs.QueryEscape("my/cool+blog&about,stuff");
    assertEquals("my%2Fcool%2Bblog%26about%2Cstuff", query);
  }

  @Test
  public void exampleQueryUnescape() {
    String escapedQuery = "my%2Fcool%2Bblog%26about%2Cstuff";
    String query = URLs.QueryUnescape(escapedQuery).ok();
    assertEquals("my/cool+blog&about,stuff", query);
  }

  @Test
  public void exampleValues() {
    Values v = new Values();
    v.set("name", "Ava");
    v.add("friend", "Jess");
    v.add("friend", "Sarah");
    v.add("friend", "Zoe");

    boolean equals = v.encode().equals("friend=Jess&friend=Sarah&friend=Zoe&name=Ava");
    assertTrue(equals);

    assertEquals("Ava", v.value("name"));
    assertEquals("Jess", v.value("friend"));

    List<String> wants = new LinkedList<>();
    wants.add("Jess");
    wants.add("Sarah");
    wants.add("Zoe");
    assertEquals(wants, v.get("friend"));
  }

  @Test
  public void exampleValues_add() {
    Values v = new Values();
    v.add("cat sounds", "meow");
    v.add("cat sounds", "mew");
    v.add("cat sounds", "mau");

    List<String> wants = new LinkedList<>();
    wants.add("meow");
    wants.add("mew");
    wants.add("mau");
    assertEquals(wants, v.get("cat sounds"));
  }

  @Test
  public void exampleValues_del() {
    Values v = new Values();
    v.add("cat sounds", "meow");
    v.add("cat sounds", "mew");
    v.add("cat sounds", "mau");

    v.del("cat sounds");
    assertTrue(CollectionUtils.isEmpty(v.get("cat sounds")));
  }

  @Test
  public void exampleValues_encode() {
    Values v = new Values();
    v.add("cat sounds", "meow");
    v.add("cat sounds", "mew/");
    v.add("cat sounds", "mau$");
    assertEquals("cat+sounds=meow&cat+sounds=mew%2F&cat+sounds=mau%24", v.encode());
  }

  @Test
  public void exampleValues_value() {
    Values v = new Values();
    v.add("cat sounds", "meow");
    v.add("cat sounds", "mew");
    v.add("cat sounds", "mau");
    assertEquals("meow", v.value("cat sounds"));
    assertEquals("", v.value("dog sounds"));
  }

  @Test
  public void exampleValues_has() {
    Values v = new Values();
    v.add("cat sounds", "meow");
    v.add("cat sounds", "mew");
    v.add("cat sounds", "mau");
    assertTrue(v.has("cat sounds"));
    assertFalse(v.has("dog sounds"));
  }

  @Test
  public void exampleValues_set() {
    Values v = new Values();
    v.add("cat sounds", "meow");
    v.add("cat sounds", "mew");
    v.add("cat sounds", "mau");

    v.set("cat sounds", "meow");

    List<String> wants = new LinkedList<>();
    wants.add("meow");
    assertEquals(wants, v.get("cat sounds"));
  }

  @Test
  public void exampleURL() {
    Result<URL, Exception> parsed = URLs.Parse("http://bing.com/search?q=dotnot");
    if (parsed.isErr()) {
      return;
    }
    URL u = parsed.ok();
    u.setScheme("https");
    u.setHost("google.com");
    Values v = u.query();
    v.set("q", "golang");
    u.setRawQuery(v.encode()); // if `setRawQuery()` is not called, the original query string is unchanged
    assertEquals("https://google.com/search?q=golang", u.toString());
  }

  @Test
  public void exampleURL_roundtrip() {
    // Parse + String preserve the original encoding.
    URL u = URLs.Parse("https://example.com/foo%2fbar").ok();
    assertEquals("/foo/bar", u.getPath());
    assertEquals("/foo%2fbar", u.getRawPath());
    assertEquals("https://example.com/foo%2fbar", u.toString());
  }

  @Test
  public void exampleURL_resolveReference() {
    URL u = URLs.Parse("../../..//search?q=dotnet").ok();
    URL base = URLs.Parse("http://example.com/directory/").ok();
    URL resolved = base.resolveReference(u);
    assertEquals("http://example.com/search?q=dotnet", resolved.toString());
  }

  @Test
  public void exampleParseQuery() {
    Result<Values, Exception> parseResult = URLs.ParseQuery("x=1&y=2&y=3");
    Values m = parseResult.ok();

    Gson gson = new GsonBuilder().disableHtmlEscaping().create();
    String json = gson.toJson(m);

    // json should be:
    // {"x":["1"],"y":["2","3"]}
    assertEquals("{\"x\":[\"1\"],\"y\":[\"2\",\"3\"]}", json);
  }

  @Test
  public void exampleURL_escapedPath() {
    URL u = URLs.Parse("http://example.com/x/y%2Fz").ok();
    assertEquals("/x/y/z", u.getPath());
    assertEquals("/x/y%2Fz", u.getRawPath());
    assertEquals("/x/y%2Fz", u.escapedPath());
  }

  @Test
  public void exampleURL_escapedFragment() {
    URL u = URLs.Parse("http://example.com/#x/y%2Fz").ok();
    assertEquals("x/y/z", u.getFragment());
    assertEquals("x/y%2Fz", u.getRawFragment());
    assertEquals("x/y%2Fz", u.escapedFragment());
  }

  @Test
  public void exampleURL_hostname() {
    URL u = URLs.Parse("https://example.org:8000/path").ok();
    assertEquals("example.org", u.hostname());
    u = URLs.Parse("https://[2001:0db8:85a3:0000:0000:8a2e:0370:7334]:17000").ok();
    assertEquals("2001:0db8:85a3:0000:0000:8a2e:0370:7334", u.hostname());
  }

  @Test
  public void exampleURL_username() {
    // you can get the actual username by calling URL.username(),
    // or get empty value if URL.user is <null>

    URL u = URLs.Parse("https://example.org:8000/path").ok();
    assertEquals("", u.username());
    u = URLs.Parse("https://user@example.org/path").ok();
    assertEquals("user", u.username());
  }

  @Test
  public void exampleURL_password() {
    // you can get the actual password by calling URL.password(),
    // or get empty value if URL.user is <null>

    URL u = URLs.Parse("https://example.org:8000/path").ok();
    assertEquals("", u.password());
    u = URLs.Parse("https://user:abc@example.org/path").ok();
    assertEquals("abc", u.password());
  }

  @Test
  public void exampleURL_isPasswordSet() {
    // you can get the actual password-set flag by calling URL.isPasswordSet(),
    // or get false if URL.user is <null>

    URL u = URLs.Parse("https://example.org:8000/path").ok();
    assertFalse(u.isPasswordSet());
    u = URLs.Parse("https://user:boys@example.org/path").ok();
    assertTrue(u.isPasswordSet());
  }

  @Test
  public void exampleURL_isAbs() {
    URL u = new URL();
    u.setHost("example.com");
    u.setPath("foo");
    assertFalse(u.isAbs());
    u.setScheme("https");
    assertTrue(u.isAbs());
  }

  @Test
  public void exampleURL_parse() {
    URL u = URLs.Parse("https://example.org").ok();
    URL rel = u.parse("/foo");
    assertEquals("https://example.org/foo", rel.toString());
    try {
      u.parse(":foo");
    } catch (Exception e) {
      assertNotNull(e);
    }
  }

  @Test
  public void exampleURL_port() {
    URL u = URLs.Parse("https://example.org").ok();
    assertEquals("", u.port());
    u = URLs.Parse("https://example.org:8080").ok();
    assertEquals("8080", u.port());
  }

  @Test
  public void exampleURL_query() {
    URL u = URLs.Parse("https://example.org/?a=1&a=2&b=&=3&&&&").ok();
    Values q = u.query();

    List<String> wants = List.of("1", "2");
    assertEquals(wants, q.get("a"));
    assertEquals("", q.value("b"));
    assertEquals("3", q.value(""));
  }

  @Test
  public void exampleURL_toString() {
    // toString() gives you the actual url link, it is not for describing the URL object itself
    URL u = new URL();
    u.setScheme("https");
    u.setUser(new Userinfo("me", "pass"));
    u.setHost("example.com");
    u.setPath("foo/bar");
    u.setRawQuery("x=1&y=2");
    u.setFragment("anchor");
    assertEquals("https://me:pass@example.com/foo/bar?x=1&y=2#anchor", u.toString());
    u.setOpaque("opaque");
    assertEquals("https:opaque?x=1&y=2#anchor", u.toString());
  }

  @Test
  public void exampleURL_redacted() {
    URL u = new URL();
    u.setScheme("https");
    u.setUser(new Userinfo("user", "password"));
    u.setHost("example.com");
    u.setPath("foo/bar");
    assertEquals("https://user:xxxxx@example.com/foo/bar", u.redacted());
    u.setUser(new Userinfo("me", "newerPassword"));
    assertEquals("https://me:xxxxx@example.com/foo/bar", u.redacted());
  }

  @Test
  public void exampleURL_requestURI() {
    URL u = URLs.Parse("https://example.org/path?foo=bar").ok();
    assertEquals("/path?foo=bar", u.requestURI());
  }

  @Test
  public void exampleURL_builder() {
    URL u = new URL();
    // should be created with default value like empty strings and false, and with null in user field
    assertEquals("", u.getScheme());
    assertEquals("", u.getOpaque());
    assertNull(u.getUser());
    assertEquals("", u.getHost());
    assertEquals("", u.getPath());
    assertEquals("", u.getRawPath());
    assertFalse(u.isOmitHost());
    assertFalse(u.isForceQuery());
    assertEquals("", u.getRawQuery());
    assertEquals("", u.getFragment());
    assertEquals("", u.getRawFragment());
    assertEquals("", u.toString());

    u = URL.builder()
        .scheme("https")
        .host("www.google.com")
        .path("search")
        .rawQuery("q=java")
        .build();
    assertEquals("https://www.google.com/search?q=java", u.toString());
  }

}
