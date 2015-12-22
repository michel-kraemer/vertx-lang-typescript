/// <reference path="../../../../build/typings/vertx-js/vertx.d.ts" />

vertx.createHttpServer().requestHandler(req => {
  req.response().putHeader("Content-type", "text/html")
    .end("<html><body><h1>Hello from Vert.x!</h1></body></html>");
}).listen(8080);
