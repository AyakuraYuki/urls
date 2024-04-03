package cc.ayakurayuki.repo.urls;

import cc.ayakurayuki.repo.urls.wrapper.CutResult;
import cc.ayakurayuki.repo.urls.wrapper.Pair;
import com.google.common.base.MoreObjects;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * A URL represents a parsed URL (technically, a URI reference).
 * <p>
 * The general form represented is:
 * <p>
 * {@code [scheme:][//[userinfo@]host][/]path[?query][#fragment]}
 * <p>
 * URLs that do not start with a slash after the scheme are interpreted as:
 * <p>
 * {@code scheme:opaque[?query][#fragment]}
 * <p>
 * The Host field contains the host and port subcomponents of the URL.
 * When the port is present, it is separated from the host with a colon.
 * When the host is an IPv6 address, it must be enclosed in square brackets:
 * "[fe80::1]:80". The [net.JoinHostPort] function combines a host and port
 * into a string suitable for the Host field, adding square brackets to
 * the host when necessary.
 * <p>
 * Note that the Path field is stored in decoded form: /%47%6f%2f becomes /Go/.
 * A consequence is that it is impossible to tell which slashes in the Path were
 * slashes in the raw URL and which were %2f. This distinction is rarely important,
 * but when it is, the code should use the [URL.EscapedPath] method, which preserves
 * the original encoding of Path.
 * <p>
 * The RawPath field is an optional field which is only set when the default
 * encoding of Path is different from the escaped path. See the EscapedPath method
 * for more details.
 * <p>
 * URL's String method uses the EscapedPath method to obtain the path.
 */
public class URL implements Serializable {

  private static final long serialVersionUID = -5075342469965931013L;

  public static final URL empty = new URL();

  private String   scheme      = "";
  private String   opaque      = "";    // encoded opaque data
  private Userinfo user        = null;  // username and password information
  private String   host        = "";    // host or host:port (see Hostname and Port methods)
  private String   path        = "";    // path (relative paths may omit leading slash)
  private String   rawPath     = "";    // encoded path hint (see EscapedPath method)
  private boolean  omitHost    = false; // do not emit empty host (authority)
  private boolean  forceQuery  = false; // append a query ('?') even if RawQuery is empty
  private String   rawQuery    = "";    // encoded query values, without '?'
  private String   fragment    = "";    // fragment for references, without '#'
  private String   rawFragment = "";    // encoded fragment hint (see EscapedFragment method)

  public URL deepClone() {
    URL url = new URL();
    url.scheme = this.scheme;
    url.opaque = this.opaque;
    if (this.user != null) {
      url.user = this.user.deepClone();
    }
    url.host = this.host;
    url.path = this.path;
    url.rawPath = this.rawPath;
    url.omitHost = this.omitHost;
    url.forceQuery = this.forceQuery;
    url.rawQuery = this.rawQuery;
    url.fragment = this.fragment;
    url.rawFragment = this.rawFragment;
    return url;
  }

  /**
   * EscapedPath returns the escaped form of u.Path.
   * In general there are multiple possible escaped forms of any path.
   * EscapedPath returns u.RawPath when it is a valid escaping of u.Path.
   * Otherwise, EscapedPath ignores u.RawPath and computes an escaped
   * form on its own.
   * The [URL.String] and [URL.RequestURI] methods use EscapedPath to construct
   * their results.
   * In general, code should call EscapedPath instead of
   * reading u.RawPath directly.
   */
  public String escapedPath() {
    if (Strings.isNotEmpty(this.rawPath) && URLs.validEncoded(this.rawPath, Encoding.Path)) {
      String p = URLs.unescape(this.rawPath, Encoding.Path);
      if (Strings.equals(p, this.path)) {
        return this.rawPath;
      }
    }
    if (Objects.equals(this.path, "*")) {
      return "*"; // don't escape
    }
    return URLs.escape(this.path, Encoding.Path);
  }

  /**
   * EscapedFragment returns the escaped form of u.Fragment.
   * In general there are multiple possible escaped forms of any fragment.
   * EscapedFragment returns u.RawFragment when it is a valid escaping of u.Fragment.
   * Otherwise, EscapedFragment ignores u.RawFragment and computes an escaped
   * form on its own.
   * The [URL.String] method uses EscapedFragment to construct its result.
   * In general, code should call EscapedFragment instead of
   * reading u.RawFragment directly.
   */
  public String escapedFragment() {
    if (Strings.isNotEmpty(this.rawFragment) && URLs.validEncoded(this.rawFragment, Encoding.Fragment)) {
      String f = URLs.unescape(this.rawFragment, Encoding.Fragment);
      if (Strings.equals(f, this.fragment)) {
        return this.rawFragment;
      }
    }
    return URLs.escape(this.fragment, Encoding.Fragment);
  }

  /**
   * toString reassembles the [URL] into a valid URL string.
   * <p>
   * The general form of the result is one of:
   * <pre>
   *   scheme:opaque?query#fragment
   *   scheme://userinfo@host/path?query#fragment
   * </pre>
   * If this.opaque is non-empty, String uses the first form;
   * otherwise it uses the second form.
   * <p>
   * Any non-ASCII characters in host are escaped.
   * <p>
   * To obtain the path, String uses this.EscapedPath().
   * <p>
   * In the second form, the following rules apply:
   * <ul>
   *   <li>if this.scheme is empty, scheme: is omitted.</li>
   *   <li>if this.user is null, userinfo@ is omitted.</li>
   *   <li>if this.host is empty, host/ is omitted.</li>
   *   <li>if this.scheme and this.host are empty and this.user is null, the entire scheme://userinfo@host/ is omitted.</li>
   *   <li>if this.host is non-empty and this.path begins with a /, the form host/path does not add its own /.</li>
   *   <li>if this.rawQuery is empty, ?query is omitted.</li>
   *   <li>if this.fragment is empty, #fragment is omitted.</li>
   * </ul>
   */
  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder();
    if (Strings.isNotEmpty(this.scheme)) {
      buf.append(this.scheme);
      buf.append(':');
    }
    if (Strings.isNotEmpty(this.opaque)) {
      buf.append(this.opaque);
    } else {
      if (Strings.isNotEmpty(this.scheme) || Strings.isNotEmpty(this.host) || this.user != null) {
        if (this.omitHost && Strings.isEmpty(this.host) && this.user == null) {
          // omit empty host
        } else {
          if (Strings.isNotEmpty(this.host) || Strings.isNotEmpty(this.path) || this.user != null) {
            buf.append("//");
          }
          if (this.user != null) {
            buf.append(this.user);
            buf.append('@');
          }
          if (Strings.isNotEmpty(this.host)) {
            buf.append(URLs.escape(this.host, Encoding.Host));
          }
        }
      }
      String path = this.escapedPath();
      if (Strings.isNotEmpty(path) && path.charAt(0) != '/' && Strings.isNotEmpty(this.host)) {
        buf.append('/');
      }
      if (buf.length() == 0) {
        // RFC 3986 §4.2
        // A path segment that contains a colon character (e.g., "this:that")
        // cannot be used as the first segment of a relative-path reference, as
        // it would be mistaken for a scheme name. Such a segment must be
        // preceded by a dot-segment (e.g., "./this:that") to make a relative-path
        // reference.
        CutResult cutTuple = Strings.cut(path, "/");
        String segment = cutTuple.getBefore();
        if (Strings.contains(segment, ":")) {
          buf.append("./");
        }
      }
      buf.append(path);
    }
    if (this.forceQuery || Strings.isNotEmpty(this.rawQuery)) {
      buf.append('?');
      buf.append(this.rawQuery);
    }
    if (Strings.isNotEmpty(this.fragment)) {
      buf.append('#');
      buf.append(this.escapedFragment());
    }
    return buf.toString();
  }

  /**
   * redacted is like [URL.String] but replaces any password with "xxxxx".
   * Only the password in u.User is redacted.
   */
  public String redacted() {
    URL ru = this.deepClone();
    if (this.user != null && this.user.isPasswordSet()) {
      ru.setUser(new Userinfo(this.user.username(), "xxxxx"));
    }
    return ru.toString();
  }

  /**
   * isAbs reports whether the [URL] is absolute.
   * <p>
   * Absolute means that it has a non-empty scheme.
   */
  public boolean isAbs() {
    return Strings.isNotEmpty(this.scheme);
  }

  /**
   * Parse parses a [URL] in the context of the receiver. The provided URL
   * may be relative or absolute. Parse returns nil, err on parse
   * failure, otherwise its return value is the same as [URL.ResolveReference].
   */
  public URL parse(String ref) {
    URL refURL = URLs.Parse(ref);
    return this.resolveReference(refURL);
  }

  /**
   * ResolveReference resolves a URI reference to an absolute URI from
   * an absolute base URI u, per RFC 3986 Section 5.2. The URI reference
   * may be relative or absolute. ResolveReference always returns a new
   * [URL] instance, even if the returned URL is identical to either the
   * base or reference. If ref is an absolute URL, then ResolveReference
   * ignores base and returns a copy of ref.
   */
  public URL resolveReference(URL ref) {
    URL url = ref.deepClone();
    if (Strings.isEmpty(ref.getScheme())) {
      url.setScheme(this.scheme);
    }
    if (Strings.isNotEmpty(ref.getScheme()) || Strings.isNotEmpty(ref.getHost()) || ref.getUser() != null) {
      // The "absoluteURI" or "net_path" cases.
      // We can ignore the error from setPath since we know we provided a
      // validly-escaped path.
      url.setPath(URLs.resolvePath(ref.escapedPath(), ""));
      return url;
    }
    if (Strings.isNotEmpty(ref.getOpaque())) {
      url.setUser(null);
      url.setHost("");
      url.setPath("");
      return url;
    }
    if (Strings.isEmpty(ref.getPath()) && !ref.isForceQuery() && Strings.isEmpty(ref.getRawQuery())) {
      url.setRawQuery(this.rawQuery);
      if (Strings.isEmpty(ref.getFragment())) {
        url.setFragment(this.fragment);
        url.setRawFragment(this.rawFragment);
      }
    }
    // The "abs_path" or "rel_path" cases.
    url.setHost(this.host);
    url.setUser(this.user);
    url.setPath(URLs.resolvePath(this.escapedPath(), ref.escapedPath()));
    return url;
  }

  /**
   * query parses RawQuery and returns the corresponding values.
   * It silently discards malformed value pairs.
   * To check errors use [ParseQuery].
   */
  public Values query() {
    return URLs.ParseQuery(this.rawQuery);
  }

  /**
   * requestURI returns the encoded path?query or opaque?query
   * string that would be used in an HTTP request for u.
   */
  public String requestURI() {
    String result = this.opaque;
    if (Strings.isEmpty(result)) {
      result = this.escapedPath();
      if (Strings.isEmpty(result)) {
        result = "/";
      }
    } else {
      if (Strings.startsWith(result, "//")) {
        result = this.scheme + ":" + result;
      }
    }
    if (this.forceQuery || Strings.isNotEmpty(this.rawQuery)) {
      result += "?" + this.rawQuery;
    }
    return result;
  }

  /**
   * hostname returns this.host, stripping any valid port number if present.
   * <p>
   * If the result is enclosed in square brackets, as literal IPv6 addresses are,
   * the square brackets are removed from the result.
   */
  public String hostname() {
    Pair<String, String> result = URLs.splitHostPort(this.getHost());
    return MoreObjects.firstNonNull(result.getA(), "");
  }

  /**
   * port returns the port part of this.host, without the leading colon.
   * <p>
   * If this.host doesn't contain a valid numeric port, returns an empty string.
   */
  public String port() {
    Pair<String, String> result = URLs.splitHostPort(this.getHost());
    return MoreObjects.firstNonNull(result.getB(), "");
  }

  /**
   * JoinPath returns a new [URL] with the provided path elements joined to
   * any existing path and the resulting path cleaned of any ./ or ../ elements.
   * Any sequences of multiple / characters will be reduced to a single /.
   */
  public URL joinPath(String... elem) {
    List<String> list = new ArrayList<>();
    list.add(this.escapedPath());
    list.addAll(Arrays.asList(elem));
    String[] elements = list.toArray(String[]::new);
    String p;
    if (!Strings.startsWith(elements[0], "/")) {
      // Return a relative path if u is relative,
      // but ensure that it contains no ../ elements.
      elements[0] = "/" + elements[0];
      p = Paths.join(elements).substring(1);
    } else {
      p = Paths.join(elements);
    }
    // path.Join will remove any trailing slashes.
    // Preserve at least one.
    if (Strings.endsWith(elements[elements.length - 1], "/") && !Strings.endsWith(p, "/")) {
      p += "/";
    }
    this.setPath(p);
    return this;
  }

  // -------------------- accessors -------------------- //

  public String getScheme() {
    return scheme;
  }

  public void setScheme(String scheme) {
    this.scheme = scheme;
  }

  public String getOpaque() {
    return opaque;
  }

  public void setOpaque(String opaque) {
    this.opaque = opaque;
  }

  public Userinfo getUser() {
    return user;
  }

  public void setUser(Userinfo user) {
    this.user = user;
  }

  public String getHost() {
    return host;
  }

  public void setHost(String host) {
    this.host = host;
  }

  public String getPath() {
    return path;
  }

  /**
   * setPath sets the Path and RawPath fields of the URL based on the provided
   * escaped path p. It maintains the invariant that RawPath is only specified
   * when it differs from the default encoding of the path.
   * <p>
   * For example:
   * <ul>
   * <li>setPath("/foo/bar")   will set Path="/foo/bar" and RawPath=""</li>
   * <li>setPath("/foo%2fbar") will set Path="/foo/bar" and RawPath="/foo%2fbar"</li>
   * </ul>
   * <p>
   * setPath will return an error only if the provided path contains an invalid
   * escaping.
   */
  public void setPath(String p) {
    String path = URLs.unescape(p, Encoding.Path);
    this.path = path;
    String escp = URLs.escape(path, Encoding.Path);
    if (Strings.equals(p, escp)) {
      this.setRawPath("");
    } else {
      this.setRawPath(p);
    }
  }

  public String getRawPath() {
    return rawPath;
  }

  public void setRawPath(String rawPath) {
    this.rawPath = rawPath;
  }

  public boolean isOmitHost() {
    return omitHost;
  }

  public void setOmitHost(boolean omitHost) {
    this.omitHost = omitHost;
  }

  public boolean isForceQuery() {
    return forceQuery;
  }

  public void setForceQuery(boolean forceQuery) {
    this.forceQuery = forceQuery;
  }

  public String getRawQuery() {
    return rawQuery;
  }

  public void setRawQuery(String rawQuery) {
    this.rawQuery = rawQuery;
  }

  public String getFragment() {
    return fragment;
  }

  /**
   * setFragment is like setPath but for Fragment/RawFragment.
   */
  public void setFragment(String f) {
    String frag = URLs.unescape(f, Encoding.Fragment);
    this.fragment = frag;
    String escf = URLs.escape(frag, Encoding.Fragment);
    if (Strings.equals(f, escf)) {
      // default encoding is fine
      this.rawFragment = "";
    } else {
      this.rawFragment = f;
    }
  }

  public String getRawFragment() {
    return rawFragment;
  }

  public void setRawFragment(String rawFragment) {
    this.rawFragment = rawFragment;
  }

}
