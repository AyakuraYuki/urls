package cc.ayakurayuki.repo.urls;

import java.util.List;

/**
 * @author Ayakura Yuki
 * @date 2024/04/08-14:58
 */
public final class Cases {

  public static class URLTest {

    private String in        = "";
    private URL    out       = URL.empty; // expected parse
    private String roundtrip = "";        // expected result of reserializing the URL; empty means same as "in"

    public URLTest() {
    }

    public String in() {
      return in;
    }

    public URLTest in(String in) {
      this.in = in;
      return this;
    }

    public URL out() {
      return out;
    }

    public URLTest out(URL out) {
      this.out = out;
      return this;
    }

    public String roundtrip() {
      return roundtrip;
    }

    public URLTest roundtrip(String roundtrip) {
      this.roundtrip = roundtrip;
      return this;
    }

  }

  public static final List<URLTest> urlTests;

  static {
    urlTests = List.of(
        // no path
        new URLTest()
            .in("https://www.google.com")
            .out(URL.builder().scheme("https").host("www.google.com").build()),

        // path
        new URLTest()
            .in("https://www.google.com/")
            .out(URL.builder().scheme("https").host("www.google.com").path("/").build()),

        // path with hex escaping
        new URLTest()
            .in("https://www.google.com/file%20one%26two")
            .out(URL.builder().scheme("https").host("www.google.com").path("/file one&two").rawPath("/file%20one%26two").build()),

        // fragment with hex escaping
        new URLTest()
            .in("https://www.google.com/#file%20one%26two")
            .out(URL.builder().scheme("https").host("www.google.com").path("/").fragment("file one&two").rawFragment("file%20one%26two").build()),

        // user
        new URLTest()
            .in("ftp://webmaster@www.google.com/")
            .out(URL.builder().scheme("ftp").user(new Userinfo("webmaster")).host("www.google.com").path("/").build()),

        // escape sequence in username
        new URLTest()
            .in("ftp://john%20doe@www.google.com/")
            .out(URL.builder().scheme("ftp").user(new Userinfo("john doe")).host("www.google.com").path("/").build())
            .roundtrip("ftp://john%20doe@www.google.com/"),

        // escape query
        new URLTest()
            .in("https://www.google.com/?")
            .out(URL.builder().scheme("https").host("www.google.com").path("/").forceQuery(true).build()),

        // query ending in question mark
        new URLTest()
            .in("https://www.google.com/?foo=bar?")
            .out(URL.builder().scheme("https").host("www.google.com").path("/").rawQuery("foo=bar?").build()),

        // query
        new URLTest()
            .in("https://www.google.com/?q=go+language")
            .out(URL.builder().scheme("https").host("www.google.com").path("/").rawQuery("q=go+language").build()),

        // query with hex escaping: NOT parsed
        new URLTest()
            .in("https://www.google.com/?q=go%20language")
            .out(URL.builder().scheme("https").host("www.google.com").path("/").rawQuery("q=go%20language").build()),

        // %20 outside query
        new URLTest()
            .in("https://www.google.com/a%20b?q=c+d")
            .out(URL.builder().scheme("https").host("www.google.com").path("/a b").rawQuery("q=c+d").build()),

        // path without leading /, so no parsing
        new URLTest()
            .in("http:www.google.com/?q=go+language")
            .out(URL.builder().scheme("http").opaque("www.google.com/").rawQuery("q=go+language").build())
            .roundtrip("http:www.google.com/?q=go+language"),

        // path without leading /, so no parsing
        new URLTest()
            .in("http:%2f%2fwww.google.com/?q=go+language")
            .out(URL.builder().scheme("http").opaque("%2f%2fwww.google.com/").rawQuery("q=go+language").build())
            .roundtrip("http:%2f%2fwww.google.com/?q=go+language"),

        // non-authority with path; see golang.org/issue/46059
        new URLTest()
            .in("mailto:/webmaster@golang.org")
            .out(URL.builder().scheme("mailto").path("/webmaster@golang.org").omitHost(true).build()),

        // non-authority
        new URLTest()
            .in("mailto:webmaster@golang.org")
            .out(URL.builder().scheme("mailto").opaque("webmaster@golang.org").build()),

        // unescaped :// in query should not create a scheme
        new URLTest()
            .in("/foo?query=http://bad")
            .out(URL.builder().path("/foo").rawQuery("query=http://bad").build()),

        // leading // without scheme should create an authority
        new URLTest()
            .in("//foo")
            .out(URL.builder().host("foo").build()),

        // leading // without scheme, with userinfo, path, and query
        new URLTest()
            .in("//user@foo/path?a=b")
            .out(URL.builder().user(new Userinfo("user")).host("foo").path("/path").rawQuery("a=b").build()),

        // Three leading slashes isn't an authority, but doesn't return an error.
        new URLTest()
            .in("///threeslashes")
            .out(URL.builder().path("///threeslashes").build()),

        new URLTest()
            .in("https://user:password@google.com")
            .out(URL.builder().scheme("https").user(new Userinfo("user", "password")).host("google.com").build())
            .roundtrip("https://user:password@google.com"),

        // unescaped @ in username should not confuse host
        new URLTest()
            .in("https://j@ne:password@google.com")
            .out(URL.builder().scheme("https").user(new Userinfo("j@ne", "password")).host("google.com").build())
            .roundtrip("https://j%40ne:password@google.com"),

        // unescaped @ in password should not confuse host
        new URLTest()
            .in("https://jane:p@ssword@google.com")
            .out(URL.builder().scheme("https").user(new Userinfo("jane", "p@ssword")).host("google.com").build())
            .roundtrip("https://jane:p%40ssword@google.com"),
        new URLTest()
            .in("https://j@ne:password@google.com/p@th?q=@go")
            .out(URL.builder().scheme("https").user(new Userinfo("j@ne", "password")).host("google.com").path("/p@th").rawQuery("q=@go").build())
            .roundtrip("https://j%40ne:password@google.com/p@th?q=@go"),
        new URLTest()
            .in("https://www.google.com/?q=go+language#foo")
            .out(URL.builder().scheme("https").host("www.google.com").path("/").rawQuery("q=go+language").fragment("foo").build()),
        new URLTest()
            .in("https://www.google.com/?q=go+language#foo&bar")
            .out(URL.builder().scheme("https").host("www.google.com").path("/").rawQuery("q=go+language").fragment("foo&bar").build())
            .roundtrip("https://www.google.com/?q=go+language#foo&bar"),
        new URLTest()
            .in("https://www.google.com/?q=go+language#foo%26bar")
            .out(URL.builder().scheme("https").host("www.google.com").path("/").rawQuery("q=go+language").fragment("foo&bar").rawFragment("foo%26bar").build())
            .roundtrip("https://www.google.com/?q=go+language#foo%26bar"),
        new URLTest()
            .in("file:///home/adg/rabbits")
            .out(URL.builder().scheme("file").path("/home/adg/rabbits").build())
            .roundtrip("file:///home/adg/rabbits"),

        // "Windows" paths are no exception to the rule.
        new URLTest()
            .in("file:///C:/FooBar/Baz.txt")
            .out(URL.builder().scheme("file").path("/C:/FooBar/Baz.txt").build())
            .roundtrip("file:///C:/FooBar/Baz.txt"),

        // case-insensitive scheme
        new URLTest()
            .in("MaIlTo:webmaster@golang.org")
            .out(URL.builder().scheme("mailto").opaque("webmaster@golang.org").build())
            .roundtrip("mailto:webmaster@golang.org"),

        // Relative path
        new URLTest()
            .in("a/b/c")
            .out(URL.builder().path("a/b/c").build())
            .roundtrip("a/b/c"),

        // escaped '?' in username and password
        new URLTest()
            .in("https://%3Fam:pa%3Fsword@google.com")
            .out(URL.builder().scheme("https").user(new Userinfo("?am", "pa?sword")).host("google.com").build()),

        // host subcomponent; IPv4 address in RFC 3986
        new URLTest()
            .in("http://192.168.0.1/")
            .out(URL.builder().scheme("http").host("192.168.0.1").path("/").build()),

        // host and port subcomponents; IPv4 address in RFC 3986
        new URLTest()
            .in("http://192.168.0.1:8080/")
            .out(URL.builder().scheme("http").host("192.168.0.1:8080").path("/").build()),

        // host subcomponent; IPv6 address in RFC 3986
        new URLTest()
            .in("http://[fe80::1]/")
            .out(URL.builder().scheme("http").host("[fe80::1]").path("/").build()),

        // host and port subcomponents; IPv6 address in RFC 3986
        new URLTest()
            .in("http://[fe80::1]:8080/")
            .out(URL.builder().scheme("http").host("[fe80::1]:8080").path("/").build()),

        // host subcomponent; IPv6 address with zone identifier in RFC 6874
        new URLTest()
            .in("http://[fe80::1%25en0]/") // alphanum zone identifier
            .out(URL.builder().scheme("http").host("[fe80::1%en0]").path("/").build()),

        // host and port subcomponents; IPv6 address with zone identifier in RFC 6874
        new URLTest()
            .in("http://[fe80::1%25en0]:8080/") // alphanum zone identifier
            .out(URL.builder().scheme("http").host("[fe80::1%en0]:8080").path("/").build()),

        // host subcomponent; IPv6 address with zone identifier in RFC 6874
        new URLTest()
            .in("http://[fe80::1%25%65%6e%301-._~]/") // percent-encoded+unreserved zone identifier
            .out(URL.builder().scheme("http").host("[fe80::1%en01-._~]").path("/").build())
            .roundtrip("http://[fe80::1%25en01-._~]/"),

        // host and port subcomponents; IPv6 address with zone identifier in RFC 6874
        new URLTest()
            .in("http://[fe80::1%25%65%6e%301-._~]:8080/") // percent-encoded+unreserved zone identifier
            .out(URL.builder().scheme("http").host("[fe80::1%en01-._~]:8080").path("/").build())
            .roundtrip("http://[fe80::1%25en01-._~]:8080/"),

        // alternate escapings of path survive round trip
        new URLTest()
            .in("https://rest.rsc.io/foo%2fbar/baz%2Fquux?alt=media")
            .out(URL.builder().scheme("https").host("rest.rsc.io").path("/foo/bar/baz/quux").rawPath("/foo%2fbar/baz%2Fquux").rawQuery("alt=media").build()),

        // net/url issue 12036
        new URLTest()
            .in("mysql://a,b,c/bar")
            .out(URL.builder().scheme("mysql").host("a,b,c").path("/bar").build()),

        // worst case host, still round trips
        new URLTest()
            .in("scheme://!$&'()*+,;=hello!:1/path")
            .out(URL.builder().scheme("scheme").host("!$&'()*+,;=hello!:1").path("/path").build()),

        // worst case path, still round trips
        new URLTest()
            .in("https://host/!$&'()*+,;=:@[hello]")
            .out(URL.builder().scheme("https").host("host").path("/!$&'()*+,;=:@[hello]").rawPath("/!$&'()*+,;=:@[hello]").build()),

        // golang.org/issue/5684
        new URLTest()
            .in("https://example.com/oid/[order_id]")
            .out(URL.builder().scheme("https").host("example.com").path("/oid/[order_id]").rawPath("/oid/[order_id]").build()),

        // golang.org/issue/12200 (colon with empty port)
        new URLTest()
            .in("http://192.168.0.2:8080/foo")
            .out(URL.builder().scheme("http").host("192.168.0.2:8080").path("/foo").build()),
        new URLTest()
            .in("http://192.168.0.2:/foo")
            .out(URL.builder().scheme("http").host("192.168.0.2:").path("/foo").build()),
        // Malformed IPv6 but still accepted.
        new URLTest()
            .in("http://2b01:e34:ef40:7730:8e70:5aff:fefe:edac:8080/foo")
            .out(URL.builder().scheme("http").host("2b01:e34:ef40:7730:8e70:5aff:fefe:edac:8080").path("/foo").build()),
        // Malformed IPv6 but still accepted.
        new URLTest()
            .in("http://2b01:e34:ef40:7730:8e70:5aff:fefe:edac:/foo")
            .out(URL.builder().scheme("http").host("2b01:e34:ef40:7730:8e70:5aff:fefe:edac:").path("/foo").build()),
        new URLTest()
            .in("http://[2b01:e34:ef40:7730:8e70:5aff:fefe:edac]:8080/foo")
            .out(URL.builder().scheme("http").host("[2b01:e34:ef40:7730:8e70:5aff:fefe:edac]:8080").path("/foo").build()),
        new URLTest()
            .in("http://[2b01:e34:ef40:7730:8e70:5aff:fefe:edac]:/foo")
            .out(URL.builder().scheme("http").host("[2b01:e34:ef40:7730:8e70:5aff:fefe:edac]:").path("/foo").build()),

        // golang.org/issue/7991 and golang.org/issue/12719 (non-ascii %-encoded in host)
        new URLTest()
            .in("http://hello.世界.com/foo")
            .out(URL.builder().scheme("http").host("hello.世界.com").path("/foo").build())
            .roundtrip("http://hello.%E4%B8%96%E7%95%8C.com/foo"),
        new URLTest()
            .in("http://hello.%e4%b8%96%e7%95%8c.com/foo")
            .out(URL.builder().scheme("http").host("hello.世界.com").path("/foo").build())
            .roundtrip("http://hello.%E4%B8%96%E7%95%8C.com/foo"),
        new URLTest()
            .in("http://hello.%E4%B8%96%E7%95%8C.com/foo")
            .out(URL.builder().scheme("http").host("hello.世界.com").path("/foo").build()),

        // golang.org/issue/10433 (path beginning with //)
        new URLTest()
            .in("https://example.com//foo")
            .out(URL.builder().scheme("https").host("example.com").path("//foo").build()),

        // test that we can reparse the host names we accept.
        new URLTest()
            .in("myscheme://authority<\"hi\">/foo")
            .out(URL.builder().scheme("myscheme").host("authority<\"hi\">").path("/foo").build()),

        // spaces in hosts are disallowed but escaped spaces in IPv6 scope IDs are grudgingly OK.
        // This happens on Windows.
        // golang.org/issue/14002
        new URLTest()
            .in("tcp://[2020::2020:20:2020:2020%25Windows%20Loves%20Spaces]:2020")
            .out(URL.builder().scheme("tcp").host("[2020::2020:20:2020:2020%Windows Loves Spaces]:2020").build()),

        // test we can roundtrip magnet url
        // fix issue https://golang.org/issue/20054
        new URLTest()
            .in("magnet:?xt=urn:btih:c12fe1c06bba254a9dc9f519b335aa7c1367a88a&dn")
            .out(URL.builder().scheme("magnet").host("").path("").rawQuery("xt=urn:btih:c12fe1c06bba254a9dc9f519b335aa7c1367a88a&dn").build())
            .roundtrip("magnet:?xt=urn:btih:c12fe1c06bba254a9dc9f519b335aa7c1367a88a&dn"),
        new URLTest()
            .in("mailto:?subject=hi")
            .out(URL.builder().scheme("mailto").host("").path("").rawQuery("subject=hi").build())
            .roundtrip("mailto:?subject=hi")
    );
  }

  // ==================================================================================================== //
  // ==================================================================================================== //
  // ==================================================================================================== //

  public static class ParseRequestURLTest {

    public final String  url;
    public final boolean expectedValid;

    public ParseRequestURLTest(String url, boolean expectedValid) {
      this.url = url;
      this.expectedValid = expectedValid;
    }

  }

  public static final String pathThatLooksSchemeRelative = "//not.a.user@not.a.host/just/a/path";

  public static final List<ParseRequestURLTest> parseRequestURLTests;

  static {
    parseRequestURLTests = List.of(
        new ParseRequestURLTest("http://foo.com", true),
        new ParseRequestURLTest("http://foo.com/", true),
        new ParseRequestURLTest("http://foo.com/path", true),
        new ParseRequestURLTest("/", true),
        new ParseRequestURLTest(pathThatLooksSchemeRelative, true),
        new ParseRequestURLTest("//not.a.user@%66%6f%6f.com/just/a/path/also", true),
        new ParseRequestURLTest("*", true),
        new ParseRequestURLTest("http://192.168.0.1/", true),
        new ParseRequestURLTest("http://192.168.0.1:8080/", true),
        new ParseRequestURLTest("http://[fe80::1]/", true),
        new ParseRequestURLTest("http://[fe80::1]:8080/", true),

        // Tests exercising RFC 6874 compliance:
        new ParseRequestURLTest("http://[fe80::1%25en0]/", true),                 // with alphanum zone identifier
        new ParseRequestURLTest("http://[fe80::1%25en0]:8080/", true),            // with alphanum zone identifier
        new ParseRequestURLTest("http://[fe80::1%25%65%6e%301-._~]/", true),      // with percent-encoded+unreserved zone identifier
        new ParseRequestURLTest("http://[fe80::1%25%65%6e%301-._~]:8080/", true), // with percent-encoded+unreserved zone identifier

        new ParseRequestURLTest("foo.html", false),
        new ParseRequestURLTest("../dir/", false),
        new ParseRequestURLTest(" http://foo.com", false),
        new ParseRequestURLTest("http://192.168.0.%31/", false),
        new ParseRequestURLTest("http://192.168.0.%31:8080/", false),
        new ParseRequestURLTest("http://[fe80::%31]/", false),
        new ParseRequestURLTest("http://[fe80::%31]:8080/", false),
        new ParseRequestURLTest("http://[fe80::%31%25en0]/", false),
        new ParseRequestURLTest("http://[fe80::%31%25en0]:8080/", false),

        // These two cases are valid as textual representations as
        // described in RFC 4007, but are not valid as address
        // literals with IPv6 zone identifiers in URIs as described in
        // RFC 6874.
        new ParseRequestURLTest("http://[fe80::1%en0]/", false),
        new ParseRequestURLTest("http://[fe80::1%en0]:8080/", false)
    );
  }

  // ==================================================================================================== //
  // ==================================================================================================== //
  // ==================================================================================================== //

  public static class StringURLTest {

    public final URL    url;
    public final String want;

    public StringURLTest(URL url, String want) {
      this.url = url;
      this.want = want;
    }

  }

  public static final List<StringURLTest> stringURLTests;

  static {
    stringURLTests = List.of(
        // No leading slash on path should prepend slash on String() call
        new StringURLTest(URL.builder().scheme("https").host("www.google.com").path("search").build(), "https://www.google.com/search"),

        // Relative path with first element containing ":" should be prepended with "./", golang.org/issue/17184
        new StringURLTest(URL.builder().path("this:that").build(), "./this:that"),

        // Relative path with second element containing ":" should not be prepended with "./"
        new StringURLTest(URL.builder().path("here/this:that").build(), "here/this:that"),

        // Non-relative path with first element containing ":" should not be prepended with "./"
        new StringURLTest(URL.builder().scheme("https").host("www.google.com").path("this:that").build(), "https://www.google.com/this:that")
    );
  }

}
