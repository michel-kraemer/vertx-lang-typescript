package io.vertx.lang.typescript.cache;

import io.vertx.lang.typescript.Source;

public class NoopCache implements Cache {
  @Override
  public String get(Source src) {
    return null;
  }

  @Override
  public void put(Source src, String value) {
    // do not cache
  }
}
