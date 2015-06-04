package io.vertx.lang.typescript.cache;

import io.vertx.lang.typescript.Source;

import java.util.HashMap;
import java.util.Map;

public class InMemoryCache implements Cache {
  // TODO use soft keys
  private Map<Source, String> cache = new HashMap<>();
  
  @Override
  public String get(Source src) {
    return cache.get(src);
  }

  @Override
  public void put(Source src, String value) {
    cache.put(src, value);
  }
}
