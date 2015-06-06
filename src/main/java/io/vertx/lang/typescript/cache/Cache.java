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

package io.vertx.lang.typescript.cache;

import io.vertx.lang.typescript.compiler.Source;

/**
 * A cache for already compiled sources
 * @author Michel Kraemer
 */
public interface Cache {
  /**
   * Get the compiled code for a given source
   * @param src the source
   * @return the compiled code or null if the cache does not contain code
   * for the given source
   */
  String get(Source src);
  
  /**
   * Add compiled code to the cache
   * @param src the source
   * @param value the compiled code
   */
  void put(Source src, String value);
}
