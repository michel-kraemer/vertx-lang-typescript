TypeScript for Vert.x 3.1 (or higher) [![Build Status](https://travis-ci.org/michel-kraemer/vertx-lang-typescript.svg?branch=master)](https://travis-ci.org/michel-kraemer/vertx-lang-typescript)
=====================================

This library adds TypeScript 1.6 support to [Vert.x 3.1](http://vertx.io) or higher.

Usage
-----

The library registers a new factory for verticles written in TypeScript. **Just
add the library to your class path and you're done**. Files with a name ending
in `.ts` will automatically be compiled to JavaScript when they are executed.

[Type definitions](https://oss.sonatype.org/content/repositories/snapshots/de/undercouch/vertx-lang-typescript/1.0.0-SNAPSHOT/)
for the Vert.x JavaScript API are also provided. Use them in your
favourite TypeScript editor to get **auto-completion**, **API documentation** and
**meaningful error messages**.

<img src="https://raw.githubusercontent.com/michel-kraemer/vertx-lang-typescript/cf2cc49d3d8b65adff4fb3d66e4fb9faaae74135/screencast.gif">

### Add to local Vert.x installation

Download the [library's main jar](https://oss.sonatype.org/content/repositories/snapshots/de/undercouch/vertx-lang-typescript/1.0.0-SNAPSHOT/)
and put it into the `lib` folder of your local Vert.x installation. It will be automatically
added to the classpath by the Vert.x start script.

### Add to your application

If your application's build is based on **Maven** add the following lines to your
`pom.xml`:

```xml
<repositories>
    <repository>
        <id>sonatype-nexus-snapshots</id>
        <name>Sonatype Nexus Snapshots</name>
        <url>https://oss.sonatype.org/content/repositories/snapshots</url>
        <releases>
            <enabled>false</enabled>
        </releases>
        <snapshots>
            <enabled>true</enabled>
        </snapshots>
    </repository>
</repositories>

<dependency>
    <groupId>de.undercouch</groupId>
    <artifactId>vertx-lang-typescript</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

If you use **Gradle** add the following to your `build.gradle`:

```gradle
repositories {
    maven {
        url 'https://oss.sonatype.org/content/repositories/snapshots'
    }
}

dependencies {
    compile 'de.undercouch:vertx-lang-typescript:1.0.0-SNAPSHOT'
}
```

Example
-------

The following script creates an HTTP server:

```typescript
/// <reference path="./typings/vertx-js/vertx.d.ts" />

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

```bash
export VERTX_OPTS=-Dvertx.typescriptCache=memory
```

In your Java program you can use

```java
System.setProperty("vertx.typescriptCache", "memory");
```

### Cache compiled scripts on disk

Caching scripts in memory only makes a difference if you, for example, deploy
the same verticle multiple times. The ramp-up time, however, may still be
rather long.

To mitigate this you can cache compiled scripts on disk. Set the
`vertx.typescriptCache` system property to `disk`.

```bash
export VERTX_OPTS=-Dvertx.typescriptCache=disk
```

or

```java
System.setProperty("vertx.typescriptCache", "disk");
```

Cached scripts will be stored in a directory called `typescript_code_cache`
in the current working directory by default. You can change this location
by setting the `vertx.typescriptCacheDir` system property:

```bash
export VERTX_OPTS=-Dvertx.typescriptCache=disk -Dvertx.typescriptCacheDir=/tmp/typescript-cache
```

or

```java
System.setProperty("vertx.typescriptCache", "disk");
System.setProperty("vertx.typescriptCacheDir", "/tmp/typescript-cache");
```

Building
--------

Perform a full build with

```bash
./gradlew build
```

Just as Vert.x 3.x the library requires Java 8.

License
-------

The library is licensed under the
[Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0).

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
