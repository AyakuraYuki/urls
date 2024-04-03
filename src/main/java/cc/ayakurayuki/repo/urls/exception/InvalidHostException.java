package cc.ayakurayuki.repo.urls.exception;

/**
 * @author Ayakura Yuki
 * @date 2024/04/03-16:16
 */
public class InvalidHostException extends RuntimeException {

  private static final long serialVersionUID = -6088618137746775773L;

  private final String character;

  public InvalidHostException(String character) {
    super(String.format("invalid character %s in host name", character));
    this.character = character;
  }

  public String getCharacter() {
    return character;
  }

}
