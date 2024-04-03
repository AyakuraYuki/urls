package cc.ayakurayuki.repo.urls;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Values maps a string key to a list of values.
 * <p>
 * It is typically used for query parameters and form values.
 * <p>
 * Unlike Header map, the keys in a Values map are case-sensitive.
 * <p>
 * Warning: Values is non-thread-safe implementation.
 *
 * @author Ayakura Yuki
 */
public class Values extends HashMap<String, List<String>> {

  private static final long serialVersionUID = -6169209659977938504L;

  /**
   * Gets the first value associated with the given key.
   * If there are no values associated with the key, Get returns
   * the empty string. To access multiple values, use the map
   * directly.
   */
  public String value(String key) {
    List<String> vs = super.get(key);
    if (CollectionUtils.isEmpty(vs)) {
      return "";
    }
    return vs.get(0);
  }

  /**
   * Sets the key to value. It replaces any existing values.
   */
  public void set(String key, String value) {
    List<String> vs = new ArrayList<>();
    vs.add(value);
    super.put(key, vs);
  }

  /**
   * Adds the value to key. It appends to any existing values associated with key.
   */
  public void add(String key, String value) {
    List<String> vs = super.get(key);
    if (CollectionUtils.isEmpty(vs)) {
      set(key, value);
    } else {
      vs.add(value);
    }
  }

  /**
   * Deletes the values associated with key.
   */
  public void del(String key) {
    super.remove(key);
  }

  /**
   * Checks whether a given key is set.
   */
  public boolean has(String key) {
    return super.containsKey(key);
  }

  /**
   * Encodes the values into URL Encoded form and sort by key.
   */
  public String encode() {
    if (isEmpty()) {
      return "";
    }
    StringBuilder buf = new StringBuilder();
    Set<String> keys = new TreeSet<>(keySet());
    for (String k : keys) {
      List<String> vs = get(k);
      String keyEscaped = URLs.QueryEscape(k);
      for (String v : vs) {
        if (buf.length() > 0) {
          buf.append('&');
        }
        buf.append(keyEscaped);
        buf.append('=');
        buf.append(URLs.QueryEscape(v));
      }
    }
    return buf.toString();
  }

}
