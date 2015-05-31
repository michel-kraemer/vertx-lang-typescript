package io.vertx.lang.typescript;

import io.vertx.core.Vertx;

public class TestRun {
  public static void main(String[] args) {
    Vertx vertx = Vertx.vertx();
    vertx.deployVerticle("test.ts", ar -> {
      if (ar.succeeded()) {
        System.out.println("Succeeded in deploying test");
        vertx.deployVerticle("test2.ts", ar2 -> {
          if (ar.succeeded()) {
            System.out.println("Succeeded in deploying test2");
          } else {
            System.out.println("Failed: " + ar2.cause());
            ar2.cause().printStackTrace();
          }
        });
      } else {
        System.out.println("Failed: " + ar.cause());
        ar.cause().printStackTrace();
      }
    });
  }
}
