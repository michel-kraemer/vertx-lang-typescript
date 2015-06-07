package de.undercouch.vertx.lang.typescript;

import io.vertx.core.Vertx;

public class TestRun {
  private static void deploy(Vertx vertx, int i) {
    long start = System.currentTimeMillis();
    vertx.deployVerticle("test.ts", ar -> {
      if (ar.succeeded()) {
        System.out.println("Deploying test took " + (System.currentTimeMillis() - start) + "ms");
        long start2 = System.currentTimeMillis();
        vertx.deployVerticle("test2.ts", ar2 -> {
          if (ar2.succeeded()) {
            System.out.println("Deploying test2 took " + (System.currentTimeMillis() - start2) + "ms");
            if (i > 0) {
              deploy(vertx, i - 1);
            }
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
  
  public static void main(String[] args) {
    Vertx vertx = Vertx.vertx();
    deploy(vertx, 10);
  }
}
