package cc.ayakurayuki.repo.urls;

/**
 * @author Ayakura Yuki
 * @date 2024/04/03-16:12
 */
public enum Encoding {

  Path(1),
  PathSegment(2),
  Host(3),
  Zone(4),
  UserPassword(5),
  QueryComponent(6),
  Fragment(7);

  public final int value;

  Encoding(int value) {
    this.value = value;
  }

}
