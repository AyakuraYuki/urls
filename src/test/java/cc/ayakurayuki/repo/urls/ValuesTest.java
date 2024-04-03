package cc.ayakurayuki.repo.urls;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * @author Ayakura Yuki
 * @date 2024/04/04-02:08
 */
@RunWith(JUnit4.class)
public class ValuesTest {

  @Test
  public void values_add() {
    Values values = new Values();
    values.set("key", "apple");
    values.add("key", "banana");
    assertTrue(values.get("key").contains("apple"));
    assertTrue(values.get("key").contains("banana"));
  }

}
