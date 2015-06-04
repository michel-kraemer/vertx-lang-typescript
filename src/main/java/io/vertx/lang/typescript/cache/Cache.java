package io.vertx.lang.typescript.cache;

import io.vertx.lang.typescript.Source;

public interface Cache {
  String get(Source src);
  void put(Source src, String value);
}
