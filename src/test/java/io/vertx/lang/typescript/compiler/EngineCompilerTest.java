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

package io.vertx.lang.typescript.compiler;

import static org.junit.Assert.assertEquals;
import io.vertx.lang.typescript.TypeScriptClassLoader;
import io.vertx.lang.typescript.cache.NoopCache;

import java.io.IOException;

import org.junit.Test;

/**
 * Tests the {@link EngineCompiler}
 * @author Michel Kraemer
 */
public class EngineCompilerTest {
  private EngineCompiler compiler = new EngineCompiler();

  /**
   * Compiles a very simple script
   * @throws Exception if something goes wrong
   */
  @Test
  public void simpleScript() throws Exception {
    TypeScriptClassLoader cl = new TypeScriptClassLoader(this.getClass().getClassLoader(),
        null, new NoopCache());
    String code = compiler.compile("test.ts", new SourceFactory() {
      @Override
      public Source getSource(String filename) throws IOException {
        if (filename.equals("test.ts")) {
          return new Source(filename, "var i: number = 5;");
        }
        return cl.getSource(filename);
      }
    });
    assertEquals("var i = 5;", code.trim());
  }
}
