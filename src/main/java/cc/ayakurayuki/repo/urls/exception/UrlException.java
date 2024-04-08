package cc.ayakurayuki.repo.urls.exception;

/**
 * @author Ayakura Yuki
 * @date 2024/04/03-16:17
 */
public class UrlException extends RuntimeException {

  private static final long serialVersionUID = -8574722490716132083L;

  private final String op;
  private final String url;

  public UrlException(String op, String url) {
    super("invalid url");
    this.op = op;
    this.url = url;
  }

  public UrlException(String op, String url, String message) {
    super(message);
    this.op = op;
    this.url = url;
  }

  public UrlException(String op, String url, Throwable cause) {
    super(cause);
    this.op = op;
    this.url = url;
  }

  public String getOp() {
    return op;
  }

  public String getUrl() {
    return url;
  }

  @Override
  public String toString() {
    return String.format("%s %s: %s", op, url, super.getMessage());
  }

}
