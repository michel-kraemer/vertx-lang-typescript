Vert.x 3.0 TypeScript Support
=============================

This library adds TypeScript support to [Vert.x 3.0](http://vertx.io).

Usage
-----

The library registers a new factory for verticles written in TypeScript. Just
add the library to your class path and you're done. Files with a name ending
in `.ts` will automatically be compiled to JavaScript when they are executed.

The library also contains the type definitions for the Vert.x JavaScript API.

Example
-------

The following script creates an HTTP server:

```typescript
/// <reference path="vertx-typescript/vertx.d.ts" />

vertx.createHttpServer().requestHandler(req => {
  req.response().putHeader("Content-Type", "text/html").end("Hello");
}).listen(8080);
```

Improve performance
-------------------

The TypeScript compiler runs rather slow in the JVM using the Nashorn JavaScript
engine. It takes a couple of cycles before the compiler reaches its full speed.
For this reason, the library offers a number of parameters to tweak the
performance.

### Make use of Node.js

The TypeScript compiler runs a lot faster in [Node.js](https://nodejs.org/).
If the `node` executable is in the path the library automatically makes use of it.

### Cache compiled scripts in memory

If you are compiling a script multiple times in the same Vert.x container you
should set the `vertx.typescriptCache` system property to `memory`. This allows
vertx-lang-typescript to reuse already compiled scripts.

On the command line you can set this property as follows:

    export VERTX_OPTS=-Dvertx.typescriptCache=memory

In your Java program you can use

    System.setProperty("vertx.typescriptCache", "memory");

### Cache compiled scripts on disk

Caching scripts in memory only makes a difference if you, for example, deploy
the same verticle multiple times. The ramp-up time, however, may still be
rather long.

To mitigate this you can cache compiled scripts on disk. Set the
`vertx.typescriptCache` system property to `disk`.

    export VERTX_OPTS=-Dvertx.typescriptCache=disk

or

    System.setProperty("vertx.typescriptCache", "disk");

Cached scripts will be stored in a directory called `typescript_code_cache`
in the current working directory by default. You can change this location
by setting the `vertx.typescriptCacheDir` system property:

    export VERTX_OPTS=-Dvertx.typescriptCache=disk -Dvertx.typescriptCacheDir=/tmp/typescript-cache

or

    System.setProperty("vertx.typescriptCache", "disk");
    System.setProperty("vertx.typescriptCacheDir", "/tmp/typescript-cache");

Building
--------

Perform a full build with

    ./gradlew build

Just as Vert.x 3.0 the library requires Java 8.

License
-------

The library is licensed under the
[Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0).

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
