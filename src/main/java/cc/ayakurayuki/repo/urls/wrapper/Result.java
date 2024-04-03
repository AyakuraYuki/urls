package cc.ayakurayuki.repo.urls.wrapper;

import java.io.Serializable;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author Ayakura Yuki
 * @date 2024/04/03-16:33
 */
public class Result<T, Err extends Throwable> implements Error, Serializable {

  private static final long serialVersionUID = -2532672928153173911L;

  private final T   ok;
  private final Err err;

  public Result(T ok, Err err) {
    this.ok = ok;
    this.err = err;
  }

  public static <Ok, Err extends Throwable> Result<Ok, Err> ok(@Nullable Ok ok) {
    return new Result<>(ok, null);
  }

  public static <Ok, Err extends Throwable> Result<Ok, Err> err(@Nonnull Err err) {
    return new Result<>(null, err);
  }

  public static <Ok, Err extends Throwable> Result<Ok, Err> err(@Nullable Ok ok, @Nonnull Err err) {
    return new Result<>(ok, err);
  }

  public T ok() {
    return ok;
  }

  public Err err() {
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
    Result<?, ?> result = (Result<?, ?>) o;
    return Objects.equals(ok, result.ok)
        && Objects.equals(err, result.err);
  }

  @Override
  public int hashCode() {
    return Objects.hash(ok, err);
  }

  @Override
  public String toString() {
    return String.format(
        "Result(%s) %s error%s",
        ok,
        isErr() ? "with" : "without",
        isErr() ? ": " + err.getMessage() : ""
    );
  }

}
