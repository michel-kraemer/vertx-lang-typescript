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

import java.util.HashMap;
import java.util.Map;

/**
 * A cache keeping compiled code in memory
 * @author Michel Kraemer
 */
public class InMemoryCache implements Cache {
  // TODO use soft keys
  private Map<Source, String> cache = new HashMap<>();
  
  @Override
  public String get(Source src) {
    return cache.get(src);
  }

  @Override
  public void put(Source src, String value) {
    cache.put(src, value);
  }
}
