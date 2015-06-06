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
