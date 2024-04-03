package cc.ayakurayuki.repo.urls;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;

/**
 * @author Ayakura Yuki
 * @date 2024/04/03-16:47
 */
public abstract class CollectionUtils {

  public static boolean isEmpty(Collection<?> collection) {
    return (collection == null || collection.isEmpty());
  }

  public static boolean isNotEmpty(Collection<?> collection) {
    return !isEmpty(collection);
  }

  public static boolean isEmpty(Map<?, ?> map) {
    return (map == null || map.isEmpty());
  }

  public static boolean isNotEmpty(Map<?, ?> map) {
    return !isEmpty(map);
  }

  public static <K, V> V safeGet(Map<K, V> map, K key) {
    if (map == null || key == null) {
      return null;
    }
    try {
      return map.get(key);
    } catch (ClassCastException | NullPointerException e) {
      return null;
    }
  }

  public static <K, V> V safeGetOrDefault(Map<K, V> map, K key, V defaultValue) {
    if (map == null) {
      return defaultValue;
    }
    try {
      return map.getOrDefault(key, defaultValue);
    } catch (ClassCastException | NullPointerException e) {
      return defaultValue;
    }
  }

  public static <T> boolean contains(T value, Collection<T> targets) {
    if (targets == null || targets.isEmpty()) {
      return false;
    }

    for (T target : targets) {
      if (Objects.equals(value, target)) {
        return true;
      }
    }

    return false;
  }

  public static <T> boolean notContains(T value, Collection<T> targets) {
    return !contains(value, targets);
  }

}
