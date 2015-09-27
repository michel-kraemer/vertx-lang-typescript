/// <reference path="../../../build/typings/vertx-js/vertx.d.ts" />

var context = vertx.getOrCreateContext();
var config = context.config();

vertx.createHttpServer().requestHandler(req => {
  req.response().putHeader("Content-Type", "text/plain").end("Hello");
}).listen(config["port"]);
