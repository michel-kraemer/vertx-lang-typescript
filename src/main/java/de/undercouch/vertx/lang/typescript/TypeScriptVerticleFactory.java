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

import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Verticle;
import io.vertx.core.Vertx;
import io.vertx.core.spi.VerticleFactory;
import io.vertx.lang.js.JSVerticleFactory;

import java.io.File;
import java.util.concurrent.atomic.AtomicReference;

import de.undercouch.vertx.lang.typescript.cache.Cache;
import de.undercouch.vertx.lang.typescript.cache.DiskCache;
import de.undercouch.vertx.lang.typescript.cache.InMemoryCache;
import de.undercouch.vertx.lang.typescript.cache.NoopCache;
import de.undercouch.vertx.lang.typescript.compiler.EngineCompiler;
import de.undercouch.vertx.lang.typescript.compiler.NodeCompiler;
import de.undercouch.vertx.lang.typescript.compiler.TypeScriptCompiler;

/**
 * A factory for verticles written in TypeScript
 * @author Michel Kraemer
 */
public class TypeScriptVerticleFactory implements VerticleFactory {
  /**
   * The name of the system property specifying the type of cache to use
   */
  public static final String PROP_NAME_CACHE = "vertx.typescriptCache";
  
  /**
   * The name of the system property specifying the directory of the disk cache
   * (default: {@value #DEFAULT_CACHE_DIR})
   */
  public static final String PROP_NAME_CACHE_DIR = "vertx.typescriptCacheDir";
  
  /**
   * The name of the system property specifying that the Node.js compiler
   * should not be used even if Node.js is available.
   */
  public static final String PROP_NAME_DISABLE_NODE_COMPILER = "vertx.disableNodeCompiler";
  
  /**
   * The name of the system property specifying that multiple instances of the
   * factory should share the same TypeScript compiler.
   */
  public static final String PROP_NAME_SHARE_COMPILER = "vertx.typescriptShareCompiler";
  
  /**
   * Do not cache compiled sources (default)
   */
  public static final String CACHE_NONE = "none";
  
  /**
   * Cache compiled sources in memory
   */
  public static final String CACHE_MEMORY = "memory";
  
  /**
   * Cache compiled sources on disk (in the directory specified by the
   * {@value #PROP_NAME_CACHE_DIR} system property
   */
  public static final String CACHE_DISK = "disk";
  
  /**
   * Default cache directory (relative to current working directory)
   */
  public static final String DEFAULT_CACHE_DIR = "typescript_code_cache";
  
  /**
   * The cache mode
   */
  private static final String CACHE_MODE = System.getProperty(PROP_NAME_CACHE, CACHE_NONE);
  
  /**
   * The cache directory (only used if disk cache is enabled)
   */
  private static final String CACHE_DIR = System.getProperty(PROP_NAME_CACHE_DIR, DEFAULT_CACHE_DIR);
  
  /**
   * The actual code cache
   */
  private static final Cache CACHE;
  static {
    if (CACHE_MODE.equalsIgnoreCase(CACHE_NONE)) {
      CACHE = new NoopCache();
    } else if (CACHE_MODE.equalsIgnoreCase(CACHE_MEMORY)) {
      CACHE = new InMemoryCache();
    } else if (CACHE_MODE.equalsIgnoreCase(CACHE_DISK)) {
      CACHE = new DiskCache(new File(CACHE_DIR));
    } else {
      throw new RuntimeException("Illegal value for " + PROP_NAME_CACHE + ": " + CACHE_MODE);
    }
  }
  
  /**
   * A factory for verticles written in JavaScript. Used to delegate compiled
   * scripts to.
   */
  private final VerticleFactory delegateFactory;
  
  /**
   * The actual TypeScript compiler
   */
  private TypeScriptCompiler compiler;
  
  /**
   * An instance of {@link NodeCompiler} shared amongst multiple instances of
   * the factory. Only set if the {@link #PROP_NAME_SHARE_COMPILER} property
   * is <code>true</code>.
   */
  private static AtomicReference<NodeCompiler> sharedNodeCompiler = new AtomicReference<>();
  
  /**
   * An instance of {@link EngineCompiler} shared amongst multiple instances of
   * the factory. Only set if the {@link #PROP_NAME_SHARE_COMPILER} property
   * is <code>true</code>.
   */
  private static AtomicReference<EngineCompiler> sharedEngineCompiler = new AtomicReference<>();
  
  /**
   * Default constructor
   */
  public TypeScriptVerticleFactory() {
    delegateFactory = new JSVerticleFactory();
  }
  
  @Override
  public void init(Vertx vertx) {
    delegateFactory.init(vertx);
  }
  
  @Override
  public String prefix() {
    return "ts";
  }
  
  @Override
  public Verticle createVerticle(String verticleName, ClassLoader classLoader) throws Exception {
    Verticle v = delegateFactory.createVerticle(verticleName, classLoader);
    return new TypeScriptVerticle(v);
  }
  
  /**
   * @return the best available TypeScript compiler
   */
  private TypeScriptCompiler getTypeScriptCompiler() {
    boolean disableNodeCompiler = Boolean.getBoolean(PROP_NAME_DISABLE_NODE_COMPILER);
    if (compiler == null) {
      boolean share = Boolean.getBoolean(PROP_NAME_SHARE_COMPILER);
      if (!disableNodeCompiler && NodeCompiler.supportsNode()) {
        if (share) {
          NodeCompiler nc = sharedNodeCompiler.get();
          if (nc == null) {
            nc = new NodeCompiler();
            if (!sharedNodeCompiler.compareAndSet(null, nc)) {
              nc = sharedNodeCompiler.get();
            }
          }
          compiler = nc;
        } else {
          compiler = new NodeCompiler();
        }
      } else {
        if (share) {
          EngineCompiler ec = sharedEngineCompiler.get();
          if (ec == null) {
            ec = new EngineCompiler();
            if (!sharedEngineCompiler.compareAndSet(null, ec)) {
              ec = sharedEngineCompiler.get();
            }
          }
          compiler = ec;
        } else {
          compiler = new EngineCompiler();
        }
      }
    }
    return compiler;
  }
  
  /**
   * A verticle written in TypeScript
   */
  public class TypeScriptVerticle implements Verticle {
    /**
     * The JavaScript verticle to delegate to
     */
    private final Verticle delegateVerticle;
    
    /**
     * Creates a verticle
     * @param delegateVerticle the JavaScript verticle to delegate to
     */
    public TypeScriptVerticle(Verticle delegateVerticle) {
      this.delegateVerticle = delegateVerticle;
    }

    @Override
    public Vertx getVertx() {
      return delegateVerticle.getVertx();
    }

    @Override
    public void init(Vertx vertx, Context context) {
      delegateVerticle.init(vertx, context);
    }

    @Override
    public void start(Future<Void> startFuture) throws Exception {
      // compile TypeScript source when verticle is started
      delegateVerticle.getVertx().executeBlocking((Future<Void> future) -> {
        // create a new class loader that automatically compiles sources
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(new TypeScriptClassLoader(
            cl, getTypeScriptCompiler(), CACHE));
        
        // start the JavaScript verticle. this will trigger loading and compiling.
        try {
          delegateVerticle.start(future);
        } catch (Exception e) {
          future.fail(e);
        } finally {
          Thread.currentThread().setContextClassLoader(cl);
        }
      }, res -> {
        if (res.succeeded()) {
          startFuture.complete();
        } else {
          startFuture.fail(res.cause());
        }
      });
    }

    @Override
    public void stop(Future<Void> stopFuture) throws Exception {
      delegateVerticle.stop(stopFuture);
    }
  }
}
