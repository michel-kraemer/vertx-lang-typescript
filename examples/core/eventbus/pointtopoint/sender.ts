/// <reference path="vertx-js/vertx.d.ts" />

var eb = vertx.eventBus();

vertx.setPeriodic(1000, v => {
  eb.send("ping-address", "ping!", (reply, err) => {
    if (err) {
      console.log("No reply");
    } else {
      console.log("Received reply " + reply.body());
    }
  });
});
