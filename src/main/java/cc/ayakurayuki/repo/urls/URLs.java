package cc.ayakurayuki.repo.urls;

import cc.ayakurayuki.repo.urls.exception.EscapeException;
import cc.ayakurayuki.repo.urls.exception.InvalidHostException;
import cc.ayakurayuki.repo.urls.exception.UrlException;
import cc.ayakurayuki.repo.urls.wrapper.CutResult;
import cc.ayakurayuki.repo.urls.wrapper.Pair;
import cc.ayakurayuki.repo.urls.wrapper.PairEx;
import cc.ayakurayuki.repo.urls.wrapper.Result;
import com.google.common.base.MoreObjects;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Ayakura Yuki
 * @date 2024/04/02-18:00
 */
public abstract class URLs {

  private static final Logger log = LoggerFactory.getLogger(URLs.class);

  private static final String UPPER_HEX = "0123456789ABCDEF";

  private static boolean ishex(char c) {
    if (Character.isDigit(c)) {
      return true;
    }
    return Character.isLetter(c);
  }

  private static byte unhex(char c) {
    if (Character.isDigit(c)) {
      return (byte) (c - '0');
    }
    if (Character.isLetter(c) && Character.isLowerCase(c)) {
      return (byte) (c - 'a' + 10);
    }
    if (Character.isLetter(c) && Character.isUpperCase(c)) {
      return (byte) (c - 'A' + 10);
    }
    return 0;
  }

  private static boolean shouldEscape(char c, Encoding mode) {
    // unreserved characters (alphanum)
    if (Character.isLetterOrDigit(c)) {
      return false;
    }

    if (mode == Encoding.Host || mode == Encoding.Zone) {
      // Host allows
      //   sub-delims = "!" / "$" / "&" / "'" / "(" / ")" / "*" / "+" / "," / ";" / "="
      // as part of reg-name.
      // We add : because we include :port as part of host.
      // We add [ ] because we include [ipv6]:port as part of host.
      // We add < > because they're the only characters left that
      // we could possibly allow, and Parse will reject them if we
      // escape them (because hosts can't use %-encoding for
      // ASCII bytes).
      switch (c) {
        case '!':
        case '$':
        case '&':
        case '\'':
        case '(':
        case ')':
        case '*':
        case '+':
        case ',':
        case ';':
        case '=':
        case ':':
        case '[':
        case ']':
        case '<':
        case '>':
        case '"':
          return false;
      }
    }

    switch (c) {
      // unreserved characters (mark)
      case '-':
      case '_':
      case '.':
      case '~':
        return false;

      // reserved characters (reserved)
      case '$':
      case '&':
      case '+':
      case ',':
      case '/':
      case ':':
      case ';':
      case '=':
      case '?':
      case '@':
        // Different sections of the URL allow a few of the reserved characters to appear unescaped.
        switch (mode) {
          case Path:
            // The RFC allows : @ & = + $ but saves / ; , for assigning
            // meaning to individual path segments. This package
            // only manipulates the path as a whole, so we allow those
            // last three as well. That leaves only ? to escape.
            return c == '?';

          case PathSegment:
            // The RFC allows : @ & = + $ but saves / ; , for assigning
            // meaning to individual path segments.
            return c == '/' || c == ';' || c == ',' || c == '?';

          case UserPassword:
            // The RFC allows ';', ':', '&', '=', '+', '$', and ',' in
            // userinfo, so we must escape only '@', '/', and '?'.
            // The parsing of userinfo treats ':' as special so we must escape
            // that too.
            return c == '@' || c == '/' || c == '?' || c == ':';

          case QueryComponent:
            // The RFC reserves (so we must escape) everything.
            return true;

          case Fragment:
            // The RFC text is silent but the grammar allows
            // everything, so escape nothing.
            return false;
        }
    }

    if (mode == Encoding.Fragment) {
      // RFC 3986 §2.2 allows not escaping sub-delims. A subset of sub-delims are
      // included in reserved from RFC 2396 §2.2. The remaining sub-delims do not
      // need to be escaped. To minimize potential breakage, we apply two restrictions:
      // (1) we always escape sub-delims outside of the fragment, and (2) we always
      // escape single quote to avoid breaking callers that had previously assumed that
      // single quotes would be escaped. See issue #19917.
      switch (c) {
        case '!':
        case '(':
        case ')':
        case '*':
          return false;
      }
    }

    // Everything else must be escaped.
    return true;
  }

  /**
   * QueryUnescape does the inverse transformation of {@link #QueryEscape(String)},
   * converting each 3-byte encoded substring of the form "%AB" into the hex-decoded
   * byte 0xAB.
   * <p>
   * It throws an error if any % is not followed by two hexadecimal digits.
   */
  public static String QueryUnescape(String s) {
    return unescape(s, Encoding.QueryComponent);
  }

  /**
   * PathUnescape does the inverse transformation of {@link #PathEscape(String)},
   * converting each 3-byte encoded substring of the form "%AB" into the hex-decoded
   * byte 0xAB. It throws an error if any % is not followed by two hexadecimal digits.
   * <p>
   * PathUnescape is identical to {@link #QueryUnescape(String)} except that it does
   * not unescape '+' to ' ' (space).
   */
  public static String PathUnescape(String s) {
    return unescape(s, Encoding.PathSegment);
  }

  /**
   * unescape unescapes a string;
   * the mode specifies which section of URL string is being unescaped.
   */
  protected static String unescape(String s, Encoding mode) {
    // count %, check that they're well-formed
    int n = 0;
    boolean hasPlus = false;

    for (int i = 0; i < s.length(); ) {
      char c = s.charAt(i);

      switch (c) {
        case '%':
          n++;
          if (i + 2 >= s.length() || !ishex(s.charAt(i + 1)) || !ishex(s.charAt(i + 2))) {
            s = s.substring(i);
            if (s.length() > 3) {
              s = s.substring(0, 3);
            }
            throw new EscapeException(s);
          }
          // Per https://tools.ietf.org/html/rfc3986#page-21
          // in the host component %-encoding can only be used
          // for non-ASCII bytes.
          // But https://tools.ietf.org/html/rfc6874#section-2
          // introduces %25 being allowed to escape a percent sign
          // in IPv6 scoped-address literals. Yay.
          if (mode == Encoding.Host && unhex(s.charAt(i + 1)) < 8 && !Strings.startsWith(s, "%25", i)) {
            throw new EscapeException(s.substring(i, i + 3));
          }
          if (mode == Encoding.Zone) {
            // RFC 6874 says basically "anything goes" for zone identifiers
            // and that even non-ASCII can be redundantly escaped,
            // but it seems prudent to restrict %-escaped bytes here to those
            // that are valid host name bytes in their unescaped form.
            // That is, you can use escaping in the zone identifier but not
            // to introduce bytes you couldn't just write directly.
            // But Windows puts spaces here! Yay.
            int v = unhex(s.charAt(i + 1)) << 4 | unhex(s.charAt(i + 2));
            if (!Strings.startsWith(s, "%25", i) && v != ' ' && shouldEscape((char) v, Encoding.Host)) {
              throw new InvalidHostException(s.substring(i, i + 3));
            }
          }
          i += 3;
          break;

        case '+':
          hasPlus = mode == Encoding.QueryComponent;
          i++;
          break;

        default:
          if ((mode == Encoding.Host || mode == Encoding.Zone) && c < 0x80 && shouldEscape(c, mode)) {
            throw new InvalidHostException(s.substring(i, i + 1));
          }
          i++;
          break;
      }
    }

    if (n == 0 && !hasPlus) {
      return s;
    }

    StringBuilder t = new StringBuilder();
    for (int i = 0; i < s.length(); i++) {
      char c = s.charAt(i);
      switch (c) {
        case '%':
          int v = unhex(s.charAt(i + 1)) << 4 | unhex(s.charAt(i + 2));
          t.append((char) v);
          i += 2;
          break;
        case '+':
          if (mode == Encoding.QueryComponent) {
            t.append(' ');
          } else {
            t.append('+');
          }
          break;
        default:
          t.append(s.charAt(i));
          break;
      }
    }
    return t.toString();
  }

  /**
   * QueryEscape escapes the string, so it can be safely placed
   * inside a URL query.
   */
  public static String QueryEscape(String s) {
    return escape(s, Encoding.QueryComponent);
  }

  /**
   * PathEscape escapes the string, so it can be safely placed inside a URL path segment,
   * replacing special characters (including /) with %XX sequences as needed.
   */
  public static String PathEscape(String s) {
    return escape(s, Encoding.PathSegment);
  }

  protected static String escape(String s, Encoding mode) {
    int spaceCount = 0;
    int hexCount = 0;

    for (int i = 0; i < s.length(); i++) {
      char c = s.charAt(i);
      if (shouldEscape(c, mode)) {
        if (c == ' ' && mode == Encoding.QueryComponent) {
          spaceCount++;
        } else {
          hexCount++;
        }
      }
    }

    if (spaceCount == 0 && hexCount == 0) {
      return s;
    }

    if (hexCount == 0) {
      StringBuilder t = new StringBuilder(s);
      for (int i = 0; i < t.length(); i++) {
        if (t.charAt(i) == ' ') {
          t.setCharAt(i, '+');
        }
      }
      return t.toString();
    }

    StringBuilder t = new StringBuilder();
    for (int i = 0; i < s.length(); i++) {
      char c = s.charAt(i);
      if (c == ' ' && mode == Encoding.QueryComponent) {
        t.append('+');
      } else if (shouldEscape(c, mode)) {
        t.append('%');
        t.append(UPPER_HEX.charAt(c >> 4));
        t.append(UPPER_HEX.charAt(c & 15));
      } else {
        t.append(c);
      }
    }
    return t.toString();
  }

  /**
   * Maybe rawURL is of the form scheme:path.
   * (Scheme must be [a-zA-Z][a-zA-Z0-9+.-]*)
   * If so, return scheme, path; else return "", rawURL.
   *
   * @return pair of scheme and path, {@code pair.a} is scheme, {@code pair.b} is path
   */
  private static PairEx<String, String, Exception> getScheme(String rawURL) {
    for (int i = 0; i < rawURL.length(); i++) {
      char c = rawURL.charAt(i);
      if (Character.isLetter(c)) {
        // do nothing
      } else if (Character.isDigit(c) || c == '+' || c == '-' || c == '.') {
        if (i == 0) {
          return new PairEx<>("", rawURL, null);
        }
      } else if (c == ':') {
        if (i == 0) {
          return new PairEx<>("", "", new IllegalArgumentException("missing protocol scheme"));
        }
        return new PairEx<>(rawURL.substring(0, i), rawURL.substring(i + 1), null);
      } else {
        // we have encountered an invalid character,
        // so there is no valid scheme
        return new PairEx<>("", rawURL, null);
      }
    }
    return new PairEx<>("", rawURL, null);
  }

  /**
   * Parse parses a raw url into a [URL] structure.
   * <p>
   * The url may be relative (a path, without a host) or absolute
   * (starting with a scheme). Trying to parse a hostname and path
   * without a scheme is invalid but may not necessarily return an
   * error, due to parsing ambiguities.
   */
  public static URL Parse(String rawURL) {
    // cut off #frag
    CutResult cutResult = Strings.cut(rawURL, "#");
    String u = cutResult.getBefore();
    String frag = cutResult.getAfter();
    Result<URL, Throwable> result = parse(u, false);
    if (result.isErr()) {
      throw new UrlException("parse", rawURL, result.err());
    }
    URL url = result.ok();
    if (Strings.isEmpty(frag)) {
      return url;
    }
    try {
      url.setFragment(frag);
    } catch (Exception e) {
      throw new UrlException("parse", rawURL, e);
    }
    return url;
  }

  /**
   * ParseRequestURI parses a raw url into a [URL] structure. It assumes that
   * url was received in an HTTP request, so the url is interpreted
   * only as an absolute URI or an absolute path.
   * The string url is assumed not to have a #fragment suffix.
   * (Web browsers strip #fragment before sending the URL to a web server.)
   */
  public static URL ParseRequestURI(String rawURL) {
    Result<URL, Throwable> result = parse(rawURL, true);
    if (result.isErr()) {
      throw new UrlException("parse", rawURL, result.err());
    }
    return result.ok();
  }

  /**
   * parse parses a URL from a string in one of two contexts. If
   * viaRequest is true, the URL is assumed to have arrived via an HTTP request,
   * in which case only absolute URLs or path-absolute relative URLs are allowed.
   * If viaRequest is false, all forms of relative URLs are allowed.
   */
  private static Result<URL, Throwable> parse(String rawURL, boolean viaRequest) {
    String rest = "";

    if (rawURL == null) {
      return Result.ok(URL.empty);
    }

    if (Strings.containsCTLByte(rawURL)) {
      return Result.err(URL.empty, new IllegalArgumentException("invalid control characters in url"));
    }

    if (Strings.isEmpty(rawURL) && viaRequest) {
      return Result.err(URL.empty, new IllegalArgumentException("empty url"));
    }

    URL url = new URL();

    if (Strings.equals(rawURL, "*")) {
      url.setPath("*");
      return Result.ok(url);
    }

    // split off possible leading "http:", "mailto:", etc.
    // cannot contain escaped characters.
    PairEx<String, String, Exception> pairEx = getScheme(rawURL);
    if (pairEx.isErr()) {
      return Result.err(url, pairEx.getErr());
    }
    url.setScheme(pairEx.getA().toLowerCase());
    rest = pairEx.getB();

    if (Strings.endsWith(rest, "?") && Strings.count(rest, "?") == 1) {
      url.setForceQuery(true);
      rest = rest.substring(0, rest.length() - 1);
    } else {
      CutResult cutResult = Strings.cut(rest, "?");
      rest = MoreObjects.firstNonNull(cutResult.getBefore(), "");
      url.setRawQuery(cutResult.getAfter());
    }

    if (!Strings.startsWith(rest, "/")) {
      if (Strings.isNotEmpty(url.getScheme())) {
        // We consider rootless paths per RFC 3986 as opaque.
        url.setOpaque(rest);
        return Result.ok(url);
      }
      if (viaRequest) {
        return Result.err(url, new IllegalArgumentException("invalid uri for request"));
      }

      // Avoid confusion with malformed schemes, like cache_object:foo/bar.
      // See golang.org/issue/16822.
      //
      // RFC 3986, §3.3:
      // In addition, a URI reference (Section 4.1) may be a relative-path reference,
      // in which case the first path segment cannot contain a colon (":") character.
      CutResult cutResult = Strings.cut(rest, "/");
      String segment = cutResult.getBefore();
      if (Strings.contains(segment, ":")) {
        // First path segment has colon. Not allowed in relative URL.
        return Result.err(url, new IllegalArgumentException("first path segment in URL cannot contain colon"));
      }
    }

    if (Strings.isNotEmpty(url.getScheme()) || !viaRequest && !Strings.startsWith(rest, "///") && Strings.startsWith(rest, "//")) {
      String authority = rest.substring(2);
      rest = "";
      int i = authority.indexOf("/");
      if (i >= 0) {
        rest = authority.substring(i);
        authority = authority.substring(0, i);
      }
      PairEx<Userinfo, String, Throwable> parseAuthorityResult = parseAuthority(authority);
      if (parseAuthorityResult.isErr()) {
        return Result.err(url, parseAuthorityResult.getErr());
      }
      url.setUser(parseAuthorityResult.getA());
      url.setHost(parseAuthorityResult.getB());
    } else if (Strings.isNotEmpty(url.getScheme()) && Strings.startsWith(rest, "/")) {
      // OmitHost is set to true when rawURL has an empty host (authority).
      // See golang.org/issue/46059.
      url.setOmitHost(true);
    }

    // Set Path and, optionally, RawPath.
    // RawPath is a hint of the encoding of Path. We don't want to set it if
    // the default escaping of Path is equivalent, to help make sure that people
    // don't rely on it in general.
    try {
      url.setPath(rest);
    } catch (Exception e) {
      return Result.err(url, e);
    }
    return Result.ok(url);
  }

  private static PairEx<Userinfo, String, Throwable> parseAuthority(String authority) {
    Userinfo user;
    String host;

    int i = Strings.lastIndexOf(authority, "@");
    Result<String, Throwable> parseHostResult;
    if (i < 0) {
      parseHostResult = parseHost(authority);
    } else {
      parseHostResult = parseHost(authority.substring(i + 1));
    }
    if (parseHostResult.isErr()) {
      return new PairEx<>(null, "", parseHostResult.err());
    }
    host = parseHostResult.ok();
    if (i < 0) {
      return new PairEx<>(null, host, null);
    }
    String userinfo = authority.substring(0, i);
    if (!validUserinfo(userinfo)) {
      return new PairEx<>(null, "", new IllegalArgumentException("invalid userinfo"));
    }
    if (!Strings.contains(userinfo, ":")) {
      try {
        userinfo = unescape(userinfo, Encoding.UserPassword);
      } catch (Exception e) {
        return new PairEx<>(null, "", e);
      }
      user = new Userinfo(userinfo);
    } else {
      CutResult cutResult = Strings.cut(userinfo, ":");
      String username = MoreObjects.firstNonNull(cutResult.getBefore(), "");
      String password = cutResult.getAfter();
      try {
        username = unescape(username, Encoding.UserPassword);
      } catch (Exception e) {
        return new PairEx<>(null, "", e);
      }
      try {
        password = unescape(password, Encoding.UserPassword);
      } catch (Exception e) {
        return new PairEx<>(null, "", e);
      }
      user = new Userinfo(username, password);
    }
    return new PairEx<>(user, host, null);
  }

  /**
   * parseHost parses host as an authority without user information. That is, as host[:port].
   */
  private static Result<String, Throwable> parseHost(String host) {
    int bound;

    if (Strings.startsWith(host, "[")) {
      // Parse an IP-Literal in RFC 3986 and RFC 6874.
      // E.g., "[fe80::1]", "[fe80::1%25en0]", "[fe80::1]:80".
      bound = Strings.lastIndexOf(host, "]");
      if (bound < 0) {
        return Result.err("", new IllegalArgumentException("missing ']' in host"));
      }
      String colonPort = host.substring(bound + 1);
      if (!validOptionalPort(colonPort)) {
        return Result.err("", new IllegalArgumentException(String.format("invalid port %s after host", colonPort)));
      }

      // RFC 6874 defines that %25 (%-encoded percent) introduces
      // the zone identifier, and the zone identifier can use basically
      // any %-encoding it likes. That's different from the host, which
      // can only %-encode non-ASCII bytes.
      // We do impose some restrictions on the zone, to avoid stupidity
      // like newlines.
      int zone = Strings.indexOf(host.substring(0, bound), "%25");
      if (zone >= 0) {
        String host1 = unescape(host.substring(0, zone), Encoding.Host);
        String host2 = unescape(host.substring(zone, bound), Encoding.Host);
        String host3 = unescape(host.substring(bound), Encoding.Host);
        return Result.ok(host1 + host2 + host3);
      }
    } else if ((bound = Strings.lastIndexOf(host, ":")) != -1) {
      String colonPort = host.substring(bound);
      if (!validOptionalPort(colonPort)) {
        return Result.err("", new IllegalArgumentException(String.format("invalid port %s after host", colonPort)));
      }
    }

    try {
      host = unescape(host, Encoding.Host);
    } catch (Exception e) {
      return Result.err("", e);
    }
    return Result.ok(host);
  }

  /**
   * validEncoded reports whether s is a valid-encoded path or fragment, according to mode.
   * <p>
   * It must not contain any bytes that require escaping during encoding.
   */
  protected static boolean validEncoded(String s, Encoding mode) {
    for (int i = 0; i < s.length(); i++) {
      // RFC 3986, Appendix A.
      // pchar = unreserved / pct-encoded / sub-delims / ":" / "@".
      // shouldEscape is not quite compliant with the RFC,
      // so we check the sub-delims ourselves and let
      // shouldEscape handle the others.
      switch (s.charAt(i)) {
        case '!':
        case '$':
        case '&':
        case '\'':
        case '(':
        case ')':
        case '*':
        case '+':
        case ',':
        case ';':
        case '=':
        case ':':
        case '@':
          // ok
          break;

        case '[':
        case ']':
          // ok - not specified in RFC 3986 but left alone by modern browsers
          break;

        case '%':
          // ok - percent encoded, will decode
          break;

        default:
          if (shouldEscape(s.charAt(i), mode)) {
            return false;
          }
          break;
      }
    }
    return true;
  }

  /**
   * validOptionalPort reports whether port is either an empty string or matches /^:\d*$/
   */
  protected static boolean validOptionalPort(String port) {
    if (Strings.isEmpty(port)) {
      return true;
    }
    if (port.charAt(0) != ':') {
      return false;
    }
    for (int i = 1; i < port.length(); i++) {
      char c = port.charAt(i);
      if (!Character.isDigit(c)) {
        return false;
      }
    }
    return true;
  }

  /**
   * ParseQuery parses the URL-encoded query string and returns
   * a map listing the values specified for each key.
   * ParseQuery always returns a non-nil map containing all the
   * valid query parameters found; err describes the first decoding error
   * encountered, if any.
   * <p>
   * Query is expected to be a list of key=value settings separated by ampersands.
   * A setting without an equals sign is interpreted as a key set to an empty
   * value.
   * Settings containing a non-URL-encoded semicolon are considered invalid.
   */
  public static Values ParseQuery(String query) {
    Values m = new Values();
    parseQueryInternal(m, query);
    return m;
  }

  private static void parseQueryInternal(Values m, String query) {
    while (Strings.isNotEmpty(query)) {
      CutResult cutResult = Strings.cut(query, "&");
      String key = cutResult.getBefore();
      query = cutResult.getAfter();
      if (Strings.contains(key, ";")) {
        log.error("invalid semicolon seperator in query: {}", query);
        return;
      }
      if (Strings.isEmpty(key)) {
        continue;
      }
      cutResult = Strings.cut(key, "=");
      key = cutResult.getBefore();
      String value = cutResult.getAfter();
      key = QueryUnescape(key);
      value = QueryUnescape(value);
      m.add(key, value);
    }
  }

  /**
   * resolvePath applies special path segments from refs and applies them to base, per RFC 3986.
   */
  protected static String resolvePath(String base, String ref) {
    Objects.requireNonNull(base);
    Objects.requireNonNull(ref);

    String full = "";
    if (Strings.isEmpty(ref)) {
      full = base;
    } else if (ref.charAt(0) != '/') {
      int i = Strings.lastIndexOf(base, "/");
      full = base.substring(0, i + 1) + ref;
    } else {
      full = ref;
    }
    if (Strings.isEmpty(full)) {
      return "";
    }

    String elem = "";
    StringBuilder dst = new StringBuilder();

    boolean first = true;
    String remaining = full;
    // We want to return a leading '/', so write it now
    dst.append('/');
    boolean found = true;
    while (found) {
      CutResult cutResult = Strings.cut(remaining, "/");
      elem = cutResult.getBefore();
      remaining = cutResult.getAfter();
      found = cutResult.isFound();
      if (Strings.equals(elem, ".")) {
        first = false;
        // drop
        continue;
      }

      if (Strings.equals(elem, "..")) {
        // ignore the leading '/' we already wrote
        String str = dst.substring(1);
        int index = Strings.lastIndexCharString(str, '/');

        dst = new StringBuilder();
        dst.append('/');
        if (index == -1) {
          first = true;
        } else {
          dst.append(str, 0, index);
        }
      } else {
        if (!first) {
          dst.append('/');
        }
        dst.append(elem);
        first = false;
      }
    }

    if (Strings.equals(elem, ".") || Strings.equals(elem, "..")) {
      dst.append('/');
    }

    // We wrote an initial '/', but we don't want two.
    String r = dst.toString();
    if (Strings.length(r) > 1 && r.charAt(1) == '/') {
      r = r.substring(1);
    }
    return r;
  }

  /**
   * splitHostPort separates host and port. If the port is not valid, it returns
   * the entire input as host, and it doesn't check the validity of the host.
   * Unlike net.SplitHostPort, but per RFC 3986, it requires ports to be numeric.
   *
   * @return a pair of split result, {@code pair.a} is host, {@code pair.b} is port
   */
  static Pair<String, String> splitHostPort(String hostPort) {
    String host = hostPort;
    String port = "";

    int colon = Strings.lastIndexOf(host, ':');
    if (colon != -1 && validOptionalPort(host.substring(colon))) {
      host = hostPort.substring(0, colon);
      port = hostPort.substring(colon + 1);
    }

    if (Strings.startsWith(host, "[") && Strings.endsWith(host, "]")) {
      host = host.substring(1, Strings.length(host) - 1);
    }

    return new Pair<>(host, port);
  }

  /**
   * validUserinfo reports whether s is a valid userinfo string per RFC 3986
   * <p>
   * Section 3.2.1:
   * <p>
   * <pre>
   * 	userinfo    = *( unreserved / pct-encoded / sub-delims / ":" )
   * 	unreserved  = ALPHA / DIGIT / "-" / "." / "_" / "~"
   * 	sub-delims  = "!" / "$" / "&" / "'" / "(" / ")"
   * 	              / "*" / "+" / "," / ";" / "="
   * </pre>
   * <p>
   * It doesn't validate pct-encoded. The caller does that via func unescape.
   */
  private static boolean validUserinfo(String s) {
    for (char c : s.toCharArray()) {
      if (Character.isLetterOrDigit(c)) {
        continue;
      }
      switch (c) {
        case '-':
        case '.':
        case '_':
        case ':':
        case '~':
        case '!':
        case '$':
        case '&':
        case '\'':
        case '(':
        case ')':
        case '*':
        case '+':
        case ',':
        case ';':
        case '=':
        case '%':
        case '@':
          continue;
        default:
          return false;
      }
    }
    return true;
  }

  /**
   * JoinPath returns a [URL] string with the provided path elements joined to
   * the existing path of base and the resulting path cleaned of any ./ or ../ elements.
   */
  public static String JoinPath(String base, String... elem) {
    URL url = Parse(base);
    return url.joinPath(elem).toString();
  }

}
