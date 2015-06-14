/// <reference path="vertx-js/vertx.d.ts" />

import Router = require("vertx-web-js/router");

var router = Router.router(vertx);
router.route().handler(routingContext => {
  var response = routingContext.response();
  response.putHeader("Content-Type", "text/html").end("Hello");
});

vertx.createHttpServer().requestHandler(router.accept).listen(8080);
