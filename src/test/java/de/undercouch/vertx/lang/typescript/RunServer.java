package de.undercouch.vertx.lang.typescript;

import io.vertx.core.Vertx;

public class RunServer {
  public static void main(String[] args) {
    Vertx vertx = Vertx.vertx();
    vertx.deployVerticle("server.ts", ar -> {
      if (ar.succeeded()) {
        System.out.println("Successfully deployed server");
      } else {
        System.out.println("Failed: " + ar.cause());
        ar.cause().printStackTrace();
      }
    });
  }
}
