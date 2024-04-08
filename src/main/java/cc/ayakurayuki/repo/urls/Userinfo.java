package cc.ayakurayuki.repo.urls;

import java.io.Serializable;
import java.util.Objects;

/**
 * The Userinfo type is an immutable encapsulation of username and
 * password details for a [URL]. An existing Userinfo value is guaranteed
 * to have a username set (potentially empty, as allowed by RFC 2396),
 * and optionally a password.
 */
public class Userinfo implements Serializable {

  private static final long serialVersionUID = 326054622141061701L;

  private final String  username;
  private final String  password;
  private final boolean passwordSet;

  Userinfo(String username) {
    this.username = username;
    this.password = "";
    this.passwordSet = false;
  }

  Userinfo(String username, String password) {
    this.username = username;
    this.password = password;
    this.passwordSet = true;
  }

  private Userinfo(String username, String password, boolean passwordSet) {
    this.username = username;
    this.password = password;
    this.passwordSet = passwordSet;
  }

  public String username() {
    if (username == null) {
      return "";
    }
    return username;
  }

  public String password() {
    if (password == null) {
      return "";
    }
    return password;
  }

  public boolean isPasswordSet() {
    return passwordSet;
  }

  @Override
  public String toString() {
    String s = URLs.escape(username(), Encoding.UserPassword);
    if (isPasswordSet()) {
      s += ":" + URLs.escape(password(), Encoding.UserPassword);
    }
    return s;
  }

  public Userinfo deepClone() {
    return new Userinfo(this.username, this.password, this.passwordSet);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Userinfo userinfo = (Userinfo) o;
    return passwordSet == userinfo.passwordSet
        && Objects.equals(username, userinfo.username)
        && Objects.equals(password, userinfo.password);
  }

  @Override
  public int hashCode() {
    return Objects.hash(username, password, passwordSet);
  }

}
