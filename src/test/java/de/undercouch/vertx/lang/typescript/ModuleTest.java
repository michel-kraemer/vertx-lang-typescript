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

import java.util.Arrays;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import de.undercouch.vertx.lang.typescript.compiler.NodeCompiler;
import io.vertx.core.Vertx;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.RunTestOnContext;
import io.vertx.ext.unit.junit.VertxUnitRunnerWithParametersFactory;

/**
 * Tests if a simple module can be loaded
 * @author Michel Kraemer
 */
@RunWith(Parameterized.class)
@Parameterized.UseParametersRunnerFactory(VertxUnitRunnerWithParametersFactory.class)
public class ModuleTest {
  @Rule
  public RunTestOnContext runTestOnContext = new RunTestOnContext();
  
  @Parameterized.Parameters
  public static Iterable<Boolean> useNodeCompiler() {
    if (NodeCompiler.supportsNode()) {
      if (Boolean.parseBoolean(System.getenv("TRAVIS"))) {
        return Arrays.asList(true);
      }
      return Arrays.asList(true, false);
    } else {
      return Arrays.asList(false);
    }
  }
  
  public ModuleTest(boolean useNodeCompiler) {
    System.setProperty(TypeScriptVerticleFactory.PROP_NAME_DISABLE_NODE_COMPILER,
        String.valueOf(!useNodeCompiler));
  }
  
  /**
   * Tests if a simple HTTP server can be deployed. Relies on the current
   * working directory being the project's root.
   * @throws Exception if something goes wrong
   */
  @Test
  public void simpleModule(TestContext context) throws Exception {
    Async async = context.async();
    Vertx vertx = runTestOnContext.vertx();
    vertx.deployVerticle("moduleTest.ts", context.asyncAssertSuccess(deploymentID -> {
      vertx.undeploy(deploymentID, context.asyncAssertSuccess(r -> async.complete()));
    }));
  }
}
