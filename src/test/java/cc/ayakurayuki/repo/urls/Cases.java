package cc.ayakurayuki.repo.urls;

import cc.ayakurayuki.repo.urls.exception.EscapeException;
import java.util.LinkedList;
import java.util.List;
import lombok.AllArgsConstructor;

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

  @AllArgsConstructor
  public static class ParseRequestURLTest {

    public final String  url;
    public final boolean expectedValid;

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

  @AllArgsConstructor
  public static class StringURLTest {

    public final URL    url;
    public final String want;

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

  // ==================================================================================================== //
  // ==================================================================================================== //
  // ==================================================================================================== //

  @AllArgsConstructor
  public static class URLRedactedTest {

    public final String name;
    public final URL    url;
    public final String want;

  }

  public static final List<URLRedactedTest> urlRedactedTests;

  static {
    urlRedactedTests = List.of(
        new URLRedactedTest("non-blank Password",
                            URL.builder().scheme("http").host("host.tld").path("this:that").user(new Userinfo("user", "password")).build(),
                            "http://user:xxxxx@host.tld/this:that"),
        new URLRedactedTest("blank Password",
                            URL.builder().scheme("http").host("host.tld").path("this:that").user(new Userinfo("user")).build(),
                            "http://user@host.tld/this:that"),
        new URLRedactedTest("nil User",
                            URL.builder().scheme("http").host("host.tld").path("this:that").user(new Userinfo("", "password")).build(),
                            "http://:xxxxx@host.tld/this:that"),
        new URLRedactedTest("blank Username, blank Password",
                            URL.builder().scheme("http").host("host.tld").path("this:that").build(),
                            "http://host.tld/this:that"),
        new URLRedactedTest("empty URL", URL.builder().build(), "")
    );
  }

  // ==================================================================================================== //
  // ==================================================================================================== //
  // ==================================================================================================== //

  @AllArgsConstructor
  public static class EscapeTest {

    public final String          in;
    public final String          out;
    public final EscapeException err;

  }

  public static final List<EscapeTest> unescapeTests;

  static {
    unescapeTests = List.of(
        new EscapeTest("", "", null),
        new EscapeTest("abc", "abc", null),
        new EscapeTest("1%41", "1A", null),
        new EscapeTest("1%41%42%43", "1ABC", null),
        new EscapeTest("%4a", "J", null),
        new EscapeTest("%6F", "o", null),
        new EscapeTest("%", "", new EscapeException("%")), // not enough characters after %
        new EscapeTest("%a", "", new EscapeException("%a")), // not enough characters after %
        new EscapeTest("%1", "", new EscapeException("%1")), // not enough characters after %
        new EscapeTest("123%45%6", "", new EscapeException("%6")), // not enough characters after %
        new EscapeTest("%zzzzz", "", new EscapeException("%zz")), // invalid hex digits
        new EscapeTest("a+b", "a b", null),
        new EscapeTest("a%20b", "a b", null)
    );
  }

  public static final List<EscapeTest> queryEscapeTests;

  static {
    queryEscapeTests = List.of(
        new EscapeTest("", "", null),
        new EscapeTest("abc", "abc", null),
        new EscapeTest("one two", "one+two", null),
        new EscapeTest("10%", "10%25", null),
        new EscapeTest(" ?&=#+%!<>#\"{}|\\^[]`☺\t:/@$'()*,;", "+%3F%26%3D%23%2B%25%21%3C%3E%23%22%7B%7D%7C%5C%5E%5B%5D%60%E2%98%BA%09%3A%2F%40%24%27%28%29%2A%2C%3B", null)
    );
  }

  public static final List<EscapeTest> pathEscapeTests;

  static {
    pathEscapeTests = List.of(
        new EscapeTest("", "", null),
        new EscapeTest("abc", "abc", null),
        new EscapeTest("abc+def", "abc+def", null),
        new EscapeTest("a/b", "a%2Fb", null),
        new EscapeTest("one two", "one%20two", null),
        new EscapeTest("10%", "10%25", null),
        new EscapeTest(" ?&=#+%!<>#\"{}|\\^[]`☺\t:/@$'()*,;", "%20%3F&=%23+%25%21%3C%3E%23%22%7B%7D%7C%5C%5E%5B%5D%60%E2%98%BA%09:%2F@$%27%28%29%2A%2C%3B", null)
    );
  }

  // ==================================================================================================== //
  // ==================================================================================================== //
  // ==================================================================================================== //

  @AllArgsConstructor
  public static class EncodeQueryTest {

    public final Values m;
    public final String expected;

  }

  public static final List<EncodeQueryTest> encodeQueryTests;

  static {
    encodeQueryTests = new LinkedList<>();
    encodeQueryTests.add(new EncodeQueryTest(new Values(), ""));

    Values case2 = new Values();
    case2.set("q", "puppies");
    case2.set("oe", "utf8");
    encodeQueryTests.add(new EncodeQueryTest(case2, "oe=utf8&q=puppies"));

    Values case3 = new Values();
    case3.add("q", "dogs");
    case3.add("q", "&");
    case3.add("q", "7");
    encodeQueryTests.add(new EncodeQueryTest(case3, "q=dogs&q=%26&q=7"));

    Values case4 = new Values();
    case4.add("a", "a1");
    case4.add("a", "a2");
    case4.add("a", "a3");
    case4.add("b", "b1");
    case4.add("b", "b2");
    case4.add("b", "b3");
    case4.add("c", "c1");
    case4.add("c", "c2");
    case4.add("c", "c3");
    encodeQueryTests.add(new EncodeQueryTest(case4, "a=a1&a=a2&a=a3&b=b1&b=b2&b=b3&c=c1&c=c2&c=c3"));
  }

  // ==================================================================================================== //
  // ==================================================================================================== //
  // ==================================================================================================== //

  @AllArgsConstructor
  public static class ResolveTest {

    public final String base;
    public final String ref;
    public final String expected;

  }

  public static final List<ResolveTest> resolvePathTests;

  static {
    resolvePathTests = List.of(
        new ResolveTest("a/b", ".", "/a/"),
        new ResolveTest("a/b", "c", "/a/c"),
        new ResolveTest("a/b", "..", "/"),
        new ResolveTest("a/", "..", "/"),
        new ResolveTest("a/", "../..", "/"),
        new ResolveTest("a/b/c", "..", "/a/"),
        new ResolveTest("a/b/c", "../d", "/a/d"),
        new ResolveTest("a/b/c", ".././d", "/a/d"),
        new ResolveTest("a/b", "./..", "/"),
        new ResolveTest("a/./b", ".", "/a/"),
        new ResolveTest("a/../", ".", "/"),
        new ResolveTest("a/.././b", "c", "/c")
    );
  }

  public static final List<ResolveTest> resolveReferenceTests;

  static {
    resolveReferenceTests = List.of(
        // Absolute URL references
        new ResolveTest("http://foo.com?a=b", "https://bar.com/", "https://bar.com/"),
        new ResolveTest("http://foo.com/", "https://bar.com/?a=b", "https://bar.com/?a=b"),
        new ResolveTest("http://foo.com/", "https://bar.com/?", "https://bar.com/?"),
        new ResolveTest("http://foo.com/bar", "mailto:foo@example.com", "mailto:foo@example.com"),

        // Path-absolute references
        new ResolveTest("http://foo.com/bar", "/baz", "http://foo.com/baz"),
        new ResolveTest("http://foo.com/bar?a=b#f", "/baz", "http://foo.com/baz"),
        new ResolveTest("http://foo.com/bar?a=b", "/baz?", "http://foo.com/baz?"),
        new ResolveTest("http://foo.com/bar?a=b", "/baz?c=d", "http://foo.com/baz?c=d"),

        // Multiple slashes
        new ResolveTest("http://foo.com/bar", "http://foo.com//baz", "http://foo.com//baz"),
        new ResolveTest("http://foo.com/bar", "http://foo.com///baz/quux", "http://foo.com///baz/quux"),

        // Scheme-relative
        new ResolveTest("https://foo.com/bar?a=b", "//bar.com/quux", "https://bar.com/quux"),

        // Path-relative references:

        // ... current directory
        new ResolveTest("http://foo.com", ".", "http://foo.com/"),
        new ResolveTest("http://foo.com/bar", ".", "http://foo.com/"),
        new ResolveTest("http://foo.com/bar/", ".", "http://foo.com/bar/"),

        // ... going down
        new ResolveTest("http://foo.com", "bar", "http://foo.com/bar"),
        new ResolveTest("http://foo.com/", "bar", "http://foo.com/bar"),
        new ResolveTest("http://foo.com/bar/baz", "quux", "http://foo.com/bar/quux"),

        // ... going up
        new ResolveTest("http://foo.com/bar/baz", "../quux", "http://foo.com/quux"),
        new ResolveTest("http://foo.com/bar/baz", "../../../../../quux", "http://foo.com/quux"),
        new ResolveTest("http://foo.com/bar", "..", "http://foo.com/"),
        new ResolveTest("http://foo.com/bar/baz", "./..", "http://foo.com/"),
        // ".." in the middle (issue 3560)
        new ResolveTest("http://foo.com/bar/baz", "quux/dotdot/../tail", "http://foo.com/bar/quux/tail"),
        new ResolveTest("http://foo.com/bar/baz", "quux/./dotdot/../tail", "http://foo.com/bar/quux/tail"),
        new ResolveTest("http://foo.com/bar/baz", "quux/./dotdot/.././tail", "http://foo.com/bar/quux/tail"),
        new ResolveTest("http://foo.com/bar/baz", "quux/./dotdot/./../tail", "http://foo.com/bar/quux/tail"),
        new ResolveTest("http://foo.com/bar/baz", "quux/./dotdot/dotdot/././../../tail", "http://foo.com/bar/quux/tail"),
        new ResolveTest("http://foo.com/bar/baz", "quux/./dotdot/dotdot/./.././../tail", "http://foo.com/bar/quux/tail"),
        new ResolveTest("http://foo.com/bar/baz", "quux/./dotdot/dotdot/dotdot/./../../.././././tail", "http://foo.com/bar/quux/tail"),
        new ResolveTest("http://foo.com/bar/baz", "quux/./dotdot/../dotdot/../dot/./tail/..", "http://foo.com/bar/quux/dot/"),

        // Remove any dot-segments prior to forming the target URI.
        // https://datatracker.ietf.org/doc/html/rfc3986#section-5.2.4
        new ResolveTest("http://foo.com/dot/./dotdot/../foo/bar", "../baz", "http://foo.com/dot/baz"),

        // Triple dot isn't special
        new ResolveTest("http://foo.com/bar", "...", "http://foo.com/..."),

        // Fragment
        new ResolveTest("http://foo.com/bar", ".#frag", "http://foo.com/#frag"),
        new ResolveTest("http://example.org/", "#!$&%27()*+,;=", "http://example.org/#!$&%27()*+,;="),

        // Paths with escaping (issue 16947).
        new ResolveTest("http://foo.com/foo%2fbar/", "../baz", "http://foo.com/baz"),
        new ResolveTest("http://foo.com/1/2%2f/3%2f4/5", "../../a/b/c", "http://foo.com/1/a/b/c"),
        new ResolveTest("http://foo.com/1/2/3", "./a%2f../../b/..%2fc", "http://foo.com/1/2/b/..%2fc"),
        new ResolveTest("http://foo.com/1/2%2f/3%2f4/5", "./a%2f../b/../c", "http://foo.com/1/2%2f/3%2f4/a%2f../c"),
        new ResolveTest("http://foo.com/foo%20bar/", "../baz", "http://foo.com/baz"),
        new ResolveTest("http://foo.com/foo", "../bar%2fbaz", "http://foo.com/bar%2fbaz"),
        new ResolveTest("http://foo.com/foo%2dbar/", "./baz-quux", "http://foo.com/foo%2dbar/baz-quux"),

        // RFC 3986: Normal Examples
        // https://datatracker.ietf.org/doc/html/rfc3986#section-5.4.1
        new ResolveTest("http://a/b/c/d;p?q", "g:h", "g:h"),
        new ResolveTest("http://a/b/c/d;p?q", "g", "http://a/b/c/g"),
        new ResolveTest("http://a/b/c/d;p?q", "./g", "http://a/b/c/g"),
        new ResolveTest("http://a/b/c/d;p?q", "g/", "http://a/b/c/g/"),
        new ResolveTest("http://a/b/c/d;p?q", "/g", "http://a/g"),
        new ResolveTest("http://a/b/c/d;p?q", "//g", "http://g"),
        new ResolveTest("http://a/b/c/d;p?q", "?y", "http://a/b/c/d;p?y"),
        new ResolveTest("http://a/b/c/d;p?q", "g?y", "http://a/b/c/g?y"),
        new ResolveTest("http://a/b/c/d;p?q", "#s", "http://a/b/c/d;p?q#s"),
        new ResolveTest("http://a/b/c/d;p?q", "g#s", "http://a/b/c/g#s"),
        new ResolveTest("http://a/b/c/d;p?q", "g?y#s", "http://a/b/c/g?y#s"),
        new ResolveTest("http://a/b/c/d;p?q", ";x", "http://a/b/c/;x"),
        new ResolveTest("http://a/b/c/d;p?q", "g;x", "http://a/b/c/g;x"),
        new ResolveTest("http://a/b/c/d;p?q", "g;x?y#s", "http://a/b/c/g;x?y#s"),
        new ResolveTest("http://a/b/c/d;p?q", "", "http://a/b/c/d;p?q"),
        new ResolveTest("http://a/b/c/d;p?q", ".", "http://a/b/c/"),
        new ResolveTest("http://a/b/c/d;p?q", "./", "http://a/b/c/"),
        new ResolveTest("http://a/b/c/d;p?q", "..", "http://a/b/"),
        new ResolveTest("http://a/b/c/d;p?q", "../", "http://a/b/"),
        new ResolveTest("http://a/b/c/d;p?q", "../g", "http://a/b/g"),
        new ResolveTest("http://a/b/c/d;p?q", "../..", "http://a/"),
        new ResolveTest("http://a/b/c/d;p?q", "../../", "http://a/"),
        new ResolveTest("http://a/b/c/d;p?q", "../../g", "http://a/g"),

        // RFC 3986: Abnormal Examples
        // https://datatracker.ietf.org/doc/html/rfc3986#section-5.4.2
        new ResolveTest("http://a/b/c/d;p?q", "../../../g", "http://a/g"),
        new ResolveTest("http://a/b/c/d;p?q", "../../../../g", "http://a/g"),
        new ResolveTest("http://a/b/c/d;p?q", "/./g", "http://a/g"),
        new ResolveTest("http://a/b/c/d;p?q", "/../g", "http://a/g"),
        new ResolveTest("http://a/b/c/d;p?q", "g.", "http://a/b/c/g."),
        new ResolveTest("http://a/b/c/d;p?q", ".g", "http://a/b/c/.g"),
        new ResolveTest("http://a/b/c/d;p?q", "g..", "http://a/b/c/g.."),
        new ResolveTest("http://a/b/c/d;p?q", "..g", "http://a/b/c/..g"),
        new ResolveTest("http://a/b/c/d;p?q", "./../g", "http://a/b/g"),
        new ResolveTest("http://a/b/c/d;p?q", "./g/.", "http://a/b/c/g/"),
        new ResolveTest("http://a/b/c/d;p?q", "g/./h", "http://a/b/c/g/h"),
        new ResolveTest("http://a/b/c/d;p?q", "g/../h", "http://a/b/c/h"),
        new ResolveTest("http://a/b/c/d;p?q", "g;x=1/./y", "http://a/b/c/g;x=1/y"),
        new ResolveTest("http://a/b/c/d;p?q", "g;x=1/../y", "http://a/b/c/y"),
        new ResolveTest("http://a/b/c/d;p?q", "g?y/./x", "http://a/b/c/g?y/./x"),
        new ResolveTest("http://a/b/c/d;p?q", "g?y/../x", "http://a/b/c/g?y/../x"),
        new ResolveTest("http://a/b/c/d;p?q", "g#s/./x", "http://a/b/c/g#s/./x"),
        new ResolveTest("http://a/b/c/d;p?q", "g#s/../x", "http://a/b/c/g#s/../x"),

        // Extras.
        new ResolveTest("https://a/b/c/d;p?q", "//g?q", "https://g?q"),
        new ResolveTest("https://a/b/c/d;p?q", "//g#s", "https://g#s"),
        new ResolveTest("https://a/b/c/d;p?q", "//g/d/e/f?y#s", "https://g/d/e/f?y#s"),
        new ResolveTest("https://a/b/c/d;p#s", "?y", "https://a/b/c/d;p?y"),
        new ResolveTest("https://a/b/c/d;p?q#s", "?y", "https://a/b/c/d;p?y"),

        // Empty path and query but with ForceQuery (issue 46033).
        new ResolveTest("https://a/b/c/d;p?q#s", "?", "https://a/b/c/d;p?")
    );
  }

  // ==================================================================================================== //
  // ==================================================================================================== //
  // ==================================================================================================== //

  @AllArgsConstructor
  public static class ParseTest {

    public final String  query;
    public final Values  out;
    public final boolean ok;

  }

  public static final List<ParseTest> parseTests;

  static {
    parseTests = new LinkedList<>();

    Values v1 = new Values();
    v1.set("a", "1");
    parseTests.add(new ParseTest("a=1", v1, true));

    Values v2 = new Values();
    v2.set("a", "1");
    v2.set("b", "2");
    parseTests.add(new ParseTest("a=1&b=2", v2, true));

    Values v3 = new Values();
    v3.add("a", "1");
    v3.add("a", "2");
    v3.add("a", "banana");
    parseTests.add(new ParseTest("a=1&a=2&a=banana", v3, true));

    Values v4 = new Values();
    v4.set("ascii", "<key: 0x90>");
    parseTests.add(new ParseTest("ascii=%3Ckey%3A+0x90%3E", v4, true));

    parseTests.add(new ParseTest("a=1;b=2", new Values(), false));
    parseTests.add(new ParseTest("a;b=1", new Values(), false));

    Values v5 = new Values();
    v5.set("a", ";");
    parseTests.add(new ParseTest("a=%3B", v5, true)); // hex encoding for semicolon

    Values v6 = new Values();
    v6.set("a;b", "1");
    parseTests.add(new ParseTest("a%3Bb=1", v6, true));

    Values v7 = new Values();
    v7.set("a", "1");
    parseTests.add(new ParseTest("a=1&a=2;a=banana", v7, false));

    Values v8 = new Values();
    v8.set("c", "1");
    parseTests.add(new ParseTest("a;b&c=1", v8, false));

    Values v9 = new Values();
    v9.set("a", "1");
    v9.set("c", "4");
    parseTests.add(new ParseTest("a=1&b=2;a=3&c=4", v9, false));

    Values v10 = new Values();
    v10.set("a", "1");
    parseTests.add(new ParseTest("a=1&b=2;c=3", v10, false));

    parseTests.add(new ParseTest(";", new Values(), false));
    parseTests.add(new ParseTest("a=1;", new Values(), false));

    Values v11 = new Values();
    v11.set("a", "1");
    parseTests.add(new ParseTest("a=1&;", v11, false));

    Values v12 = new Values();
    v12.set("b", "2");
    parseTests.add(new ParseTest(";a=1&b=2", v12, false));

    Values v13 = new Values();
    v13.set("a", "1");
    parseTests.add(new ParseTest("a=1&b=2;", v13, false));
  }

  // ==================================================================================================== //
  // ==================================================================================================== //
  // ==================================================================================================== //

  @AllArgsConstructor
  public static class RequestURITest {

    public final URL    url;
    public final String out;

  }

  public static final List<RequestURITest> requritests;

  static {
    requritests = List.of(
        new RequestURITest(URL.builder().scheme("http").host("example.com").path("").build(), "/"),
        new RequestURITest(URL.builder().scheme("http").host("example.com").path("/a b").build(), "/a%20b"),

        // golang.org/issue/4860 variant 1
        new RequestURITest(URL.builder().scheme("http").host("example.com").opaque("/%2F/%2F/").build(), "/%2F/%2F/"),

        // golang.org/issue/4860 variant 2
        new RequestURITest(URL.builder().scheme("http").host("example.com").opaque("//other.example.com/%2F/%2F/").build(), "http://other.example.com/%2F/%2F/"),

        // better fix for issue 4860
        new RequestURITest(URL.builder().scheme("http").host("example.com").path("/////").rawPath("/%2F/%2F/").build(), "/%2F/%2F/"),
        new RequestURITest(
            URL.builder()
                .scheme("http")
                .host("example.com")
                .path("/////")
                .rawPath("/WRONG/") // ignored because doesn't match Path
                .build(),
            "/////"
        ),
        new RequestURITest(URL.builder().scheme("http").host("example.com").path("/a b").rawQuery("q=go+language").build(), "/a%20b?q=go+language"),
        new RequestURITest(
            URL.builder()
                .scheme("http")
                .host("example.com")
                .path("/a b")
                .rawPath("/a b") // ignored because invalid
                .rawQuery("q=go+language")
                .build(),
            "/a%20b?q=go+language"
        ),
        new RequestURITest(
            URL.builder()
                .scheme("http")
                .host("example.com")
                .path("/a?b")
                .rawPath("/a?b") // ignored because invalid
                .rawQuery("q=go+language")
                .build(),
            "/a%3Fb?q=go+language"
        ),
        new RequestURITest(URL.builder().scheme("myschema").opaque("opaque").build(), "opaque"),
        new RequestURITest(URL.builder().scheme("myschema").opaque("opaque").rawQuery("q=go+language").build(), "opaque?q=go+language"),
        new RequestURITest(URL.builder().scheme("http").host("example.com").path("//foo").build(), "//foo"),
        new RequestURITest(URL.builder().scheme("http").host("example.com").path("/foo").forceQuery(true).build(), "/foo?")
    );
  }

  // ==================================================================================================== //
  // ==================================================================================================== //
  // ==================================================================================================== //

  @AllArgsConstructor
  public static class ParseErrorsTest {

    public final String  in;
    public final boolean wantErr;

  }

  public static final List<ParseErrorsTest> parseErrorsTests;

  static {
    parseErrorsTests = List.of(
        new ParseErrorsTest("http://[::1]", false),
        new ParseErrorsTest("http://[::1]:80", false),
        new ParseErrorsTest("http://[::1]:namedport", true), // rfc3986 3.2.3
        new ParseErrorsTest("http://x:namedport", true),     // rfc3986 3.2.3
        new ParseErrorsTest("http://[::1]/", false),
        new ParseErrorsTest("http://[::1]a", true),
        new ParseErrorsTest("http://[::1]%23", true),
        new ParseErrorsTest("http://[::1%25en0]", false),    // valid zone id
        new ParseErrorsTest("http://[::1]:", false),         // colon, but no port OK
        new ParseErrorsTest("http://x:", false),             // colon, but no port OK
        new ParseErrorsTest("http://[::1]:%38%30", true),    // not allowed: % encoding only for non-ASCII
        new ParseErrorsTest("http://[::1%25%41]", false),    // RFC 6874 allows over-escaping in zone
        new ParseErrorsTest("http://[%10::1]", true),        // no %xx escapes in IP address
        new ParseErrorsTest("http://[::1]/%48", false),      // %xx in path is fine
        new ParseErrorsTest("http://%41:8080/", true),       // not allowed: % encoding only for non-ASCII
        new ParseErrorsTest("mysql://x@y(z:123)/foo", true), // not well-formed per RFC 3986, golang.org/issue/33646
        new ParseErrorsTest("mysql://x@y(1.2.3.4:123)/foo", true),

        new ParseErrorsTest(" http://foo.com", true),  // invalid character in schema
        new ParseErrorsTest("ht tp://foo.com", true),  // invalid character in schema
        new ParseErrorsTest("ahttp://foo.com", false), // valid schema characters
        new ParseErrorsTest("1http://foo.com", true),  // invalid character in schema

        new ParseErrorsTest("http://[]%20%48%54%54%50%2f%31%2e%31%0a%4d%79%48%65%61%64%65%72%3a%20%31%32%33%0a%0a/", true), // golang.org/issue/11208
        new ParseErrorsTest("http://a b.com/", true),    // no space in host name please
        new ParseErrorsTest("cache_object://foo", true), // scheme cannot have _, relative path cannot have : in first segment
        new ParseErrorsTest("cache_object:foo", true),
        new ParseErrorsTest("cache_object:foo/bar", true),
        new ParseErrorsTest("cache_object/:foo/bar", false)
    );
  }

  // ==================================================================================================== //
  // ==================================================================================================== //
  // ==================================================================================================== //

  @AllArgsConstructor
  public static class ShouldEscapeTest {

    public final char     in;
    public final Encoding mode;
    public final boolean  escape;

  }

  public static final List<ShouldEscapeTest> shouldEscapeTests;

  static {
    shouldEscapeTests = List.of(
        // Unreserved characters (§2.3)
        new ShouldEscapeTest('a', Encoding.Path, false),
        new ShouldEscapeTest('a', Encoding.UserPassword, false),
        new ShouldEscapeTest('a', Encoding.QueryComponent, false),
        new ShouldEscapeTest('a', Encoding.Fragment, false),
        new ShouldEscapeTest('a', Encoding.Host, false),
        new ShouldEscapeTest('z', Encoding.Path, false),
        new ShouldEscapeTest('A', Encoding.Path, false),
        new ShouldEscapeTest('Z', Encoding.Path, false),
        new ShouldEscapeTest('0', Encoding.Path, false),
        new ShouldEscapeTest('9', Encoding.Path, false),
        new ShouldEscapeTest('-', Encoding.Path, false),
        new ShouldEscapeTest('-', Encoding.UserPassword, false),
        new ShouldEscapeTest('-', Encoding.QueryComponent, false),
        new ShouldEscapeTest('-', Encoding.Fragment, false),
        new ShouldEscapeTest('.', Encoding.Path, false),
        new ShouldEscapeTest('_', Encoding.Path, false),
        new ShouldEscapeTest('~', Encoding.Path, false),

        // User information (§3.2.1)
        new ShouldEscapeTest(':', Encoding.UserPassword, true),
        new ShouldEscapeTest('/', Encoding.UserPassword, true),
        new ShouldEscapeTest('?', Encoding.UserPassword, true),
        new ShouldEscapeTest('@', Encoding.UserPassword, true),
        new ShouldEscapeTest('$', Encoding.UserPassword, false),
        new ShouldEscapeTest('&', Encoding.UserPassword, false),
        new ShouldEscapeTest('+', Encoding.UserPassword, false),
        new ShouldEscapeTest(',', Encoding.UserPassword, false),
        new ShouldEscapeTest(';', Encoding.UserPassword, false),
        new ShouldEscapeTest('=', Encoding.UserPassword, false),

        // Host (IP address, IPv6 address, registered name, port suffix; §3.2.2)
        new ShouldEscapeTest('!', Encoding.Host, false),
        new ShouldEscapeTest('$', Encoding.Host, false),
        new ShouldEscapeTest('&', Encoding.Host, false),
        new ShouldEscapeTest('\'', Encoding.Host, false),
        new ShouldEscapeTest('(', Encoding.Host, false),
        new ShouldEscapeTest(')', Encoding.Host, false),
        new ShouldEscapeTest('*', Encoding.Host, false),
        new ShouldEscapeTest('+', Encoding.Host, false),
        new ShouldEscapeTest(',', Encoding.Host, false),
        new ShouldEscapeTest(';', Encoding.Host, false),
        new ShouldEscapeTest('=', Encoding.Host, false),
        new ShouldEscapeTest(':', Encoding.Host, false),
        new ShouldEscapeTest('[', Encoding.Host, false),
        new ShouldEscapeTest(']', Encoding.Host, false),
        new ShouldEscapeTest('0', Encoding.Host, false),
        new ShouldEscapeTest('9', Encoding.Host, false),
        new ShouldEscapeTest('A', Encoding.Host, false),
        new ShouldEscapeTest('z', Encoding.Host, false),
        new ShouldEscapeTest('_', Encoding.Host, false),
        new ShouldEscapeTest('-', Encoding.Host, false),
        new ShouldEscapeTest('.', Encoding.Host, false)
    );
  }

  // ==================================================================================================== //
  // ==================================================================================================== //
  // ==================================================================================================== //

  @AllArgsConstructor
  public static class URLHostnameAndPortTest {

    public final String in; // URL.host field
    public final String host;
    public final String port;

  }

  public static final List<URLHostnameAndPortTest> urlHostnameAndPortTests;

  static {
    urlHostnameAndPortTests = List.of(
        new URLHostnameAndPortTest("foo.com:80", "foo.com", "80"),
        new URLHostnameAndPortTest("foo.com", "foo.com", ""),
        new URLHostnameAndPortTest("foo.com:", "foo.com", ""),
        new URLHostnameAndPortTest("FOO.COM", "FOO.COM", ""), // no canonicalization
        new URLHostnameAndPortTest("1.2.3.4", "1.2.3.4", ""),
        new URLHostnameAndPortTest("1.2.3.4:80", "1.2.3.4", "80"),
        new URLHostnameAndPortTest("[1:2:3:4]", "1:2:3:4", ""),
        new URLHostnameAndPortTest("[1:2:3:4]:80", "1:2:3:4", "80"),
        new URLHostnameAndPortTest("[::1]:80", "::1", "80"),
        new URLHostnameAndPortTest("[::1]", "::1", ""),
        new URLHostnameAndPortTest("[::1]:", "::1", ""),
        new URLHostnameAndPortTest("localhost", "localhost", ""),
        new URLHostnameAndPortTest("localhost:443", "localhost", "443"),
        new URLHostnameAndPortTest("some.super.long.domain.example.org:8080", "some.super.long.domain.example.org", "8080"),
        new URLHostnameAndPortTest("[2001:0db8:85a3:0000:0000:8a2e:0370:7334]:17000", "2001:0db8:85a3:0000:0000:8a2e:0370:7334", "17000"),
        new URLHostnameAndPortTest("[2001:0db8:85a3:0000:0000:8a2e:0370:7334]", "2001:0db8:85a3:0000:0000:8a2e:0370:7334", ""),

        // Ensure that even when not valid, Host is one of "Hostname",
        // "Hostname:Port", "[Hostname]" or "[Hostname]:Port".
        // See https://golang.org/issue/29098.
        new URLHostnameAndPortTest("[google.com]:80", "google.com", "80"),
        new URLHostnameAndPortTest("google.com]:80", "google.com]", "80"),
        new URLHostnameAndPortTest("google.com:80_invalid_port", "google.com:80_invalid_port", ""),
        new URLHostnameAndPortTest("[::1]extra]:80", "::1]extra", "80"),
        new URLHostnameAndPortTest("google.com]extra:extra", "google.com]extra:extra", "")
    );
  }

  // ==================================================================================================== //
  // ==================================================================================================== //
  // ==================================================================================================== //

  @AllArgsConstructor
  public static class EscapeBenchmarkCase {

    public final String unescaped;
    public final String query;
    public final String path;

  }

  public static final List<EscapeBenchmarkCase> escapeBenchmarkCases;

  static {
    escapeBenchmarkCases = List.of(
        new EscapeBenchmarkCase("one two", "one+two", "one%20two"),
        new EscapeBenchmarkCase(
            "Фотки собак",
            "%D0%A4%D0%BE%D1%82%D0%BA%D0%B8+%D1%81%D0%BE%D0%B1%D0%B0%D0%BA",
            "%D0%A4%D0%BE%D1%82%D0%BA%D0%B8%20%D1%81%D0%BE%D0%B1%D0%B0%D0%BA"),
        new EscapeBenchmarkCase("shortrun(break)shortrun", "shortrun%28break%29shortrun", "shortrun%28break%29shortrun"),
        new EscapeBenchmarkCase(
            "longerrunofcharacters(break)anotherlongerrunofcharacters",
            "longerrunofcharacters%28break%29anotherlongerrunofcharacters",
            "longerrunofcharacters%28break%29anotherlongerrunofcharacters"),
        new EscapeBenchmarkCase(
            "padded/with+various%characters?that=need$some@escaping+paddedsowebreak/256bytes".repeat(4),
            "padded%2Fwith%2Bvarious%25characters%3Fthat%3Dneed%24some%40escaping%2Bpaddedsowebreak%2F256bytes".repeat(4),
            "padded%2Fwith+various%25characters%3Fthat=need$some@escaping+paddedsowebreak%2F256bytes".repeat(4))
    );
  }

  // ==================================================================================================== //
  // ==================================================================================================== //
  // ==================================================================================================== //

  @AllArgsConstructor
  public static class JoinPathTest {

    public final String   base;
    public final String[] elem;
    public final String   out;

  }

  public static final List<JoinPathTest> joinPathTests;

  static {
    joinPathTests = List.of(
        new JoinPathTest("https://go.googlesource.com", new String[]{"go"}, "https://go.googlesource.com/go"),
        new JoinPathTest("https://go.googlesource.com/a/b/c", new String[]{"../../../go"}, "https://go.googlesource.com/go"),
        new JoinPathTest("https://go.googlesource.com/", new String[]{"../go"}, "https://go.googlesource.com/go"),
        new JoinPathTest("https://go.googlesource.com", new String[]{"../go"}, "https://go.googlesource.com/go"),
        new JoinPathTest("https://go.googlesource.com", new String[]{"../go", "../../go", "../../../go"}, "https://go.googlesource.com/go"),
        new JoinPathTest("https://go.googlesource.com/../go", null, "https://go.googlesource.com/go"),
        new JoinPathTest("https://go.googlesource.com/", new String[]{"./go"}, "https://go.googlesource.com/go"),
        new JoinPathTest("https://go.googlesource.com//", new String[]{"/go"}, "https://go.googlesource.com/go"),
        new JoinPathTest("https://go.googlesource.com//", new String[]{"/go", "a", "b", "c"}, "https://go.googlesource.com/go/a/b/c"),
        new JoinPathTest("http://[fe80::1%en0]:8080/", new String[]{"/go"}, ""),
        new JoinPathTest("https://go.googlesource.com", new String[]{"go/"}, "https://go.googlesource.com/go/"),
        new JoinPathTest("https://go.googlesource.com", new String[]{"go//"}, "https://go.googlesource.com/go/"),
        new JoinPathTest("https://go.googlesource.com", null, "https://go.googlesource.com/"),
        new JoinPathTest("https://go.googlesource.com/", null, "https://go.googlesource.com/"),
        new JoinPathTest("https://go.googlesource.com/a%2fb", new String[]{"c"}, "https://go.googlesource.com/a%2fb/c"),
        new JoinPathTest("https://go.googlesource.com/a%2fb", new String[]{"c%2fd"}, "https://go.googlesource.com/a%2fb/c%2fd"),
        new JoinPathTest("https://go.googlesource.com/a/b", new String[]{"/go"}, "https://go.googlesource.com/a/b/go"),
        new JoinPathTest("/", null, "/"),
        new JoinPathTest("a", null, "a"),
        new JoinPathTest("a", new String[]{"b"}, "a/b"),
        new JoinPathTest("a", new String[]{"../b"}, "b"),
        new JoinPathTest("a", new String[]{"../../b"}, "b"),
        new JoinPathTest("", new String[]{"a"}, "a"),
        new JoinPathTest("", new String[]{"../a"}, "a")
    );
  }

}
