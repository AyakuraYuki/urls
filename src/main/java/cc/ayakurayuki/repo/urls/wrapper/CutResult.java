package cc.ayakurayuki.repo.urls.wrapper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author Ayakura Yuki
 * @date 2024/04/03-16:24
 */
public class CutResult {

  @Nullable
  private final String before;

  @Nonnull
  private final String after;

  private final boolean found;

  public CutResult(@Nullable String before, @Nonnull String after, boolean found) {
    this.before = before;
    this.after = after;
    this.found = found;
  }

  @Nullable
  public String getBefore() {
    return before;
  }

  @Nonnull
  public String getAfter() {
    return after;
  }

  public boolean isFound() {
    return found;
  }

}
