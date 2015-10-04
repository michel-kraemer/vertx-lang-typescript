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

package de.undercouch.vertx.lang.typescript.compiler;

import org.junit.Before;

import de.undercouch.vertx.lang.typescript.compiler.EngineCompiler;
import de.undercouch.vertx.lang.typescript.compiler.TypeScriptCompiler;

/**
 * Tests the {@link EngineCompiler}
 * @author Michel Kraemer
 */
public class EngineCompilerTest extends CompilerTestBase {
  private EngineCompiler compiler = new EngineCompiler();

  @Before
  public void beforeMethod() {
    // skip EngineCompiler tests on Circle CI, because they are likely to time out
    org.junit.Assume.assumeTrue(System.getenv("CIRCLE_BUILD_NUM") != null);
  }

  @Override
  protected TypeScriptCompiler getCompiler() {
    return compiler;
  }
}
