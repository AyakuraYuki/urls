package cc.ayakurayuki.repo.urls.wrapper;

import java.io.Serializable;
import java.util.Objects;

/**
 * @author Ayakura Yuki
 * @date 2024/04/03-16:35
 */
public class Triple<A, B, C> implements Serializable {

  private static final long serialVersionUID = 2174471199434450847L;

  private final A a;
  private final B b;
  private final C c;

  public Triple(A a, B b, C c) {
    this.a = a;
    this.b = b;
    this.c = c;
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

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Triple<?, ?, ?> triple = (Triple<?, ?, ?>) o;
    return Objects.equals(a, triple.a)
        && Objects.equals(b, triple.b)
        && Objects.equals(c, triple.c);
  }

  @Override
  public int hashCode() {
    return Objects.hash(a, b, c);
  }

  @Override
  public String toString() {
    return String.format("Triple(%s, %s, %s)", a, b, c);
  }

}
