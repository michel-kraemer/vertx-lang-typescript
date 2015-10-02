/// <reference path="../../../build/typings/vertx-js/vertx.d.ts" />

import ModuleServer = require("./moduleServerModule");

var ms = new ModuleServer();
ms.run(vertx);
