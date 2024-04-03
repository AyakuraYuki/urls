package cc.ayakurayuki.repo.urls;

import static org.junit.Assert.assertEquals;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * @author Ayakura Yuki
 * @date 2024/04/03-17:13
 */
@RunWith(JUnit4.class)
public class URLTest {

  @Test
  public void url_joinPath() {
    URL url = new URL();
    url.setScheme("https");
    url.setHost("www.bilibili.com");
    url.setPath("/space");
    assertEquals("https://www.bilibili.com/space", url.toString());
    url.joinPath("120331", "dynamic");
    assertEquals("https://www.bilibili.com/space/120331/dynamic", url.toString());
  }

  @Test
  public void deepClone() {
    URL src = new URL();
    src.setScheme("https");
    src.setHost("www.bing.com");
    src.joinPath("chat", "c", "114514");
    src.setRawQuery("t=1");

    Gson gson = new GsonBuilder()
        .disableHtmlEscaping()
        .setPrettyPrinting()
        .create();

    URL dst = src.deepClone();
    System.out.println(gson.toJson(dst));
    System.out.println();

    dst.joinPath("detail");
    System.out.println(gson.toJson(src));
    System.out.println(gson.toJson(dst));
  }

}
