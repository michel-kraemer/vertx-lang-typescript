/// <reference path="vertx-js/vertx.d.ts" />

vertx.createHttpClient().getNow(8080, "localhost", "/", res => {
  console.log("Got response " + res.statusCode());
  res.bodyHandler(body => {
    console.log("Got data " + body.toString("utf8"));
  });
});
