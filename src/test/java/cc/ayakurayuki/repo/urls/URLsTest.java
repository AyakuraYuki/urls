package cc.ayakurayuki.repo.urls;

import static org.junit.Assert.assertTrue;

import cc.ayakurayuki.repo.urls.wrapper.Pair;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * @author Ayakura Yuki
 * @date 2024/04/03-17:22
 */
@RunWith(JUnit4.class)
public class URLsTest {

  private static class splitHostPortTest {

    String hostPort;
    String host;
    String port;

    public splitHostPortTest(String hostPort, String host, String port) {
      this.hostPort = hostPort;
      this.host = host;
      this.port = port;
    }

  }

  private final List<splitHostPortTest> splitHostPortTests = List.of(
      new splitHostPortTest("www.bilibili.com", "www.bilibili.com", ""),
      new splitHostPortTest("127.0.0.1", "127.0.0.1", ""),
      new splitHostPortTest("127.0.0.1:80", "127.0.0.1", "80"),
      new splitHostPortTest("[::]:80", "::", "80")
  );

  @Test
  public void splitHostPort() {
    for (splitHostPortTest tt : splitHostPortTests) {
      Pair<String, String> result = URLs.splitHostPort(tt.hostPort);
      assertTrue(
          String.format("splitHostPort(%s) = %s, %s, want %s %s", tt.hostPort, result.getA(), result.getB(), tt.host, tt.port),
          Strings.equals(tt.host, result.getA()) && Strings.equals(tt.port, result.getB())
      );
    }
  }

}
