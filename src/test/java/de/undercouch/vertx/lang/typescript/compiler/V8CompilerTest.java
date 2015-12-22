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
import org.junit.Ignore;

/**
 * Tests the {@link V8Compiler}
 * @author Michel Kraemer
 */
@Ignore("Disabled until https://github.com/eclipsesource/J2V8/issues/39 is solved")
public class V8CompilerTest extends CompilerTestBase {
  private V8Compiler compiler = new V8Compiler();

  @Before
  public void beforeMethod() {
    // skip V8Compiler tests if V8 runtime is not available
    org.junit.Assume.assumeTrue(V8Compiler.supportsV8());
  }

  @Override
  protected TypeScriptCompiler getCompiler() {
    return compiler;
  }
}
