package io.vertx.lang.typescript;

import io.vertx.core.Vertx;

public class TestRun {
  public static void main(String[] args) {
    Vertx vertx = Vertx.vertx();
    vertx.deployVerticle("test.ts", ar -> {
      if (ar.succeeded()) {
        System.out.println("Succeeded in deploying");
      } else {
        System.out.println("Failed: " + ar.cause());
        ar.cause().printStackTrace();
      }
    });
  }
}
