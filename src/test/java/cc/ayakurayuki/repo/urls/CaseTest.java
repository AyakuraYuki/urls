package cc.ayakurayuki.repo.urls;

import java.util.List;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * @author Ayakura Yuki
 * @date 2024/04/07-11:43
 */
@RunWith(JUnit4.class)
public class CaseTest {

  private static class URLTest {

    String in;
    URL    out; // expected parse
    String roundtrip; // expected result of reserializing the URL; empty means same as "in"

    public URLTest(String in, URL out, String roundtrip) {
      this.in = in;
      this.out = out;
      this.roundtrip = roundtrip;
    }

  }

  private static final List<URLTest> urlTests = List.of(
      // no path
      new URLTest("https://www.google.com", URL.builder().scheme("https").host("www.google.com").build(), ""),

      // path
      new URLTest("https://www.google.com/", URL.builder().scheme("https").host("www.google.com").path("/").build(), ""),

      // path with hex escaping
      new URLTest("https://www.google.com/file%20one%26two", URL.builder().scheme("https").host("www.google.com").path("/file one&two").rawPath("/file%20one%26two").build(), ""),

      // fragment with hex escaping
      new URLTest("https://www.google.com/#file%20one%26two", URL.builder().scheme("https").host("www.google.com").path("/").fragment("file one&two").rawFragment("file%20one%26two").build(), ""),

      // user
      new URLTest("ftp://webmaster@www.google.com/", URL.builder().scheme("ftp").user(new Userinfo("webmaster")).host("www.google.com").path("/").build(), ""),

      // escape sequence in username
      new URLTest("ftp://john%20doe@www.google.com/", URL.builder().scheme("ftp").user(new Userinfo("john doe")).host("www.google.com").path("/").build(), "ftp://john%20doe@www.google.com/"),

      // escape query
      new URLTest("https://www.google.com/?", URL.builder().scheme("https").host("www.google.com").path("/").forceQuery(true).build(), ""),

      // query ending in question mark
      new URLTest("https://www.google.com/?foo=bar?", URL.builder().scheme("https").host("www.google.com").path("/").rawQuery("foo=bar").build(), ""),

      // query
      new URLTest("https://www.google.com/?q=go+language", URL.builder().scheme("https").host("www.google.com").path("/").rawQuery("q=go+language").build(), ""),

      // query with hex escaping: NOT parsed
      new URLTest("https://www.google.com/?q=go%20language", URL.builder().scheme("https").host("www.google.com").path("/").rawQuery("q=go%20language").build(), ""),

      // %20 outside query
      new URLTest("https://www.google.com/a%20b?q=c+d", URL.builder().scheme("https").host("www.google.com").path("/a b").rawQuery("q=c+d").build(), ""),

      // path without leading /, so no parsing
      new URLTest("http:www.google.com/?q=go+language", URL.builder().scheme("http").opaque("www.google.com/").rawQuery("q=go+language").build(), "http:www.google.com/?q=go+language"),

      // path without leading /, so no parsing
      new URLTest("http:%2f%2fwww.google.com/?q=go+language",
                  URL.builder().scheme("http").opaque("%2f%2fwww.google.com/").rawQuery("q=go+language").build(),
                  "http:%2f%2fwww.google.com/?q=go+language"),

      // non-authority with path; see golang.org/issue/46059
      new URLTest("mailto:/webmaster@golang.org", URL.builder().scheme("mailto").path("/webmaster@golang.org").omitHost(true).build(), ""),

      // non-authority
      new URLTest("mailto:webmaster@golang.org", URL.builder().scheme("mailto").opaque("webmaster@golang.org").build(), ""),

      // unescaped :// in query should not create a scheme
      new URLTest("/foo?query=http://bad", URL.builder().path("/foo").rawQuery("query=http://bad").build(), ""),

      // leading // without scheme should create an authority
      new URLTest("//foo", URL.builder().host("foo").build(), ""),

      // leading // without scheme, with userinfo, path, and query
      new URLTest("//user@foo/path?a=b", URL.builder().user(new Userinfo("user")).host("foo").path("/path").rawQuery("a=b").build(), ""),

      // Three leading slashes isn't an authority, but doesn't return an error.
      new URLTest("///threeslashes", URL.builder().path("///threeslashes").build(), ""),

      new URLTest("https://user:password@google.com", URL.builder().scheme("https").user(new Userinfo("user", "password")).host("google.com").build(), "https://user:password@google.com"),

      // unescaped @ in username should not confuse host
      new URLTest("https://j@ne:password@google.com", URL.builder().scheme("https").user(new Userinfo("j@ne", "password")).host("google.com").build(), "https://j%40ne:password@google.com"),

      // unescaped @ in password should not confuse host
      new URLTest("https://jane:p@ssword@google.com", URL.builder().scheme("https").user(new Userinfo("jane", "p@ssword")).host("google.com").build(), "https://jane:p%40ssword@google.com"),
      new URLTest("https://j@ne:password@google.com/p@th?q=@go",
                  URL.builder().scheme("https").user(new Userinfo("j@ne", "password")).host("google.com").path("/p@th").rawQuery("q=@go").build(),
                  "https://j%40ne:password@google.com/p@th?q=@go")
  );

}
