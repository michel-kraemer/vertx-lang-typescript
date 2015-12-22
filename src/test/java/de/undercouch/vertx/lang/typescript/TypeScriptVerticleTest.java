// Copyright 2015 Michel Kraemer
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package de.undercouch.vertx.lang.typescript;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import de.undercouch.vertx.lang.typescript.compiler.NodeCompiler;
import de.undercouch.vertx.lang.typescript.compiler.V8Compiler;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.RunTestOnContext;
import io.vertx.ext.unit.junit.Timeout;
import io.vertx.ext.unit.junit.VertxUnitRunnerWithParametersFactory;

/**
 * Tests various verticles written in TypeScript
 * @author Michel Kraemer
 */
@RunWith(Parameterized.class)
@Parameterized.UseParametersRunnerFactory(VertxUnitRunnerWithParametersFactory.class)
public class TypeScriptVerticleTest {
  private static enum Compiler {
    V8,
    NODE,
    ENGINE
  }
  
  @Rule
  public RunTestOnContext runTestOnContext = new RunTestOnContext();

  @Rule
  public Timeout globalTimeout = Timeout.seconds(60 * 10); // 10 minutes (for the really slow CI server)
  
  @BeforeClass
  public static void setUpClass() {
    System.setProperty(TypeScriptVerticleFactory.PROP_NAME_SHARE_COMPILER, "true");
  }
  
  @Parameterized.Parameters
  public static Iterable<Compiler> useNodeCompiler() {
    List<Compiler> result = new ArrayList<>();
    if (V8Compiler.supportsV8()) {
      result.add(Compiler.V8);
    }
    if (NodeCompiler.supportsNode()) {
      result.add(Compiler.NODE);
    }
    // skip EngineCompiler tests on Circle CI, because they are likely to time out
    if (System.getenv("CIRCLE_BUILD_NUM") == null) {
      result.add(Compiler.ENGINE);
    }
    return result;
  }
  
  public TypeScriptVerticleTest(Compiler compiler) {
    switch (compiler) {
    case V8:
      // nothing to do here. V8 is the one with the highest priority
      break;
    case NODE:
      System.setProperty(TypeScriptVerticleFactory.PROP_NAME_DISABLE_V8_COMPILER, "true");
      break;
    case ENGINE:
      System.setProperty(TypeScriptVerticleFactory.PROP_NAME_DISABLE_V8_COMPILER, "true");
      System.setProperty(TypeScriptVerticleFactory.PROP_NAME_DISABLE_NODE_COMPILER, "true");
      break;
    }
  }
  
  /**
   * Gets a free socket port
   * @return the port
   * @throws IOException if the port could not be determined
   */
  private static int getAvailablePort() throws IOException {
    ServerSocket s = null;
    try {
      s = new ServerSocket(0);
      return s.getLocalPort();
    } finally {
      if (s != null) {
        s.close();
      }
    }
  }
  
  private void doTest(String verticle, String message, TestContext context) throws IOException {
    Async async = context.async();
    int port = getAvailablePort();
    JsonObject config = new JsonObject().put("port", port);
    DeploymentOptions options = new DeploymentOptions().setConfig(config);
    Vertx vertx = runTestOnContext.vertx();
    vertx.deployVerticle(verticle, options, context.asyncAssertSuccess(deploymentID -> {
      vertx.createHttpClient().getNow(port, "localhost", "/", response -> {
        response.bodyHandler(buffer -> {
          context.assertEquals(message, buffer.toString());
          vertx.undeploy(deploymentID, context.asyncAssertSuccess(r -> async.complete()));
        });
      });
    }));
  }
  
  /**
   * Tests if a simple HTTP server can be deployed. Relies on the current
   * working directory being the project's root.
   * @throws Exception if something goes wrong
   */
  @Test
  public void simpleServer(TestContext context) throws Exception {
    doTest("src/test/resources/simpleServer.ts", "Hello", context);
  }
  
  /**
   * Tests if a simple HTTP server using routing can be deployed. Relies on
   * the current working directory being the project's root.
   * @throws Exception if something goes wrong
   */
  @Test
  public void routingServer(TestContext context) throws Exception {
    doTest("src/test/resources/routingServer.ts", "Hello Routing", context);
  }
  
  /**
   * Tests if a simple HTTP server using modules can be deployed. Relies on
   * the current working directory being the project's root.
   * @throws Exception if something goes wrong
   */
  @Test
  public void moduleServer(TestContext context) throws Exception {
    doTest("src/test/resources/moduleServer.ts", "Hello Module", context);
  }
}
