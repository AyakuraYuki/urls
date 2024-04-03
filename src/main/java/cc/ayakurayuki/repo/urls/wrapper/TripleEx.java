package cc.ayakurayuki.repo.urls.wrapper;

import java.io.Serializable;
import java.util.Objects;

/**
 * @author Ayakura Yuki
 * @date 2024/04/03-16:36
 */
public class TripleEx<A, B, C, Err extends Throwable> implements Error, Serializable {

  private static final long serialVersionUID = 6701327722942658714L;

  private final A   a;
  private final B   b;
  private final C   c;
  private final Err err;

  public TripleEx(A a, B b, C c, Err err) {
    this.a = a;
    this.b = b;
    this.c = c;
    this.err = err;
  }

  public A getA() {
    return a;
  }

  public B getB() {
    return b;
  }

  public C getC() {
    return c;
  }

  public Err getErr() {
    return err;
  }

  @Override
  public boolean isErr() {
    return err != null;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    TripleEx<?, ?, ?, ?> tripleEx = (TripleEx<?, ?, ?, ?>) o;
    return Objects.equals(a, tripleEx.a)
        && Objects.equals(b, tripleEx.b)
        && Objects.equals(c, tripleEx.c)
        && Objects.equals(err, tripleEx.err);
  }

  @Override
  public int hashCode() {
    return Objects.hash(a, b, c, err);
  }

  @Override
  public String toString() {
    return String.format(
        "TripleEx(%s, %s, %s) %s error%s",
        a, b, c,
        isErr() ? "with" : "without",
        isErr() ? ": " + err.getMessage() : ""
    );
  }

}
