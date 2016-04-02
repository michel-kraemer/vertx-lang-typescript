// Copyright 2016 Michel Kraemer
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

import java.util.ArrayList;
import java.util.List;

import org.junit.runners.Parameterized;

import de.undercouch.vertx.lang.typescript.compiler.NodeCompiler;
import de.undercouch.vertx.lang.typescript.compiler.V8Compiler;

/**
 * Base class for tests that run on multiple TypeScript compilers
 * @author Michel Kraemer
 */
public class MultipleCompilerTestBase {
  protected static enum Compiler {
    V8,
    NODE,
    ENGINE
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
  
  public MultipleCompilerTestBase(Compiler compiler) {
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
}
