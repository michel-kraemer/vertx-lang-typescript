/// <reference path="../../../build/typings/vertx-js/vertx.d.ts" />

class ModuleServer {
  run(vertx: Vertx) {
    var context = vertx.getOrCreateContext();
    var config = context.config();

    vertx.createHttpServer().requestHandler(req => {
      req.response().putHeader("Content-Type", "text/plain").end("Hello Module");
    }).listen(config["port"]);
  }
}

export = ModuleServer;
