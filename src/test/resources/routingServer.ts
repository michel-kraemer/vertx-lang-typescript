/// <reference path="../../../build/typings/vertx-js/vertx.d.ts" />
/// <reference path="../../../build/typings/vertx-web-js/router.d.ts" />

import Router = require("vertx-web-js/router");

var context = vertx.getOrCreateContext();
var config = context.config();

var router = Router.router(vertx);
router.route().handler(routingContext => {
  var response = routingContext.response();
  response.putHeader("Content-Type", "text/html").end("Hello Routing");
});

vertx.createHttpServer().requestHandler(router.accept).listen(config["port"]);
