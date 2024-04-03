package cc.ayakurayuki.repo.urls.wrapper;

import java.io.Serializable;
import java.util.Objects;

/**
 * @author Ayakura Yuki
 * @date 2024/04/03-16:31
 */
public class PairEx<A, B, Err extends Throwable> implements Error, Serializable {

  private static final long serialVersionUID = -861806058447952953L;

  private final A   a;
  private final B   b;
  private final Err err;

  public PairEx(A a, B b, Err err) {
    this.a = a;
    this.b = b;
    this.err = err;
  }

  public A getA() {
    return a;
  }

  public B getB() {
    return b;
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
    PairEx<?, ?, ?> pairEx = (PairEx<?, ?, ?>) o;
    return Objects.equals(a, pairEx.a)
        && Objects.equals(b, pairEx.b)
        && Objects.equals(err, pairEx.err);
  }

  @Override
  public int hashCode() {
    return Objects.hash(a, b, err);
  }

  @Override
  public String toString() {
    return String.format(
        "PairEx(%s, %s) %s error%s",
        a, b,
        isErr() ? "with" : "without",
        isErr() ? ": " + err.getMessage() : ""
    );
  }

}
