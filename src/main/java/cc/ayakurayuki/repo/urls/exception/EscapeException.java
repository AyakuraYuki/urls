package cc.ayakurayuki.repo.urls.exception;

/**
 * @author Ayakura Yuki
 * @date 2024/04/03-16:13
 */
public class EscapeException extends RuntimeException {

  private static final long serialVersionUID = 784167262782072612L;

  private final String escape;

  public EscapeException(String escape) {
    super(String.format("invalid URL escape %s", escape));
    this.escape = escape;
  }

  public String getEscape() {
    return escape;
  }

}
