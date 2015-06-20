/// <reference path="vertx-js/vertx.d.ts" />

var eb = vertx.eventBus();

eb.consumer("ping-address", message => {
  console.log("Received message: " + message.body());
  message.reply("pong!");
});

console.log("Receiver ready!");
