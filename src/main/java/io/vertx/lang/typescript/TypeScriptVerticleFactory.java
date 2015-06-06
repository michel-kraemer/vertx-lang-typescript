package io.vertx.lang.typescript;

import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Verticle;
import io.vertx.core.Vertx;
import io.vertx.core.spi.VerticleFactory;
import io.vertx.lang.js.JSVerticleFactory;
import io.vertx.lang.typescript.cache.Cache;
import io.vertx.lang.typescript.cache.DiskCache;
import io.vertx.lang.typescript.cache.InMemoryCache;
import io.vertx.lang.typescript.cache.NoopCache;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

public class TypeScriptVerticleFactory implements VerticleFactory {
  public static final String PROP_NAME_CACHE = "vertx.typescriptCache";
  public static final String PROP_NAME_CACHE_DIR = "vertx.typescriptCacheDir";
  public static final String CACHE_NONE = "none";
  public static final String CACHE_MEMORY = "memory";
  public static final String CACHE_DISK = "disk";
  public static final String DEFAULT_CACHE_DIR = "typescript_code_cache";
  
  private static final String CACHE_MODE = System.getProperty(PROP_NAME_CACHE, CACHE_NONE);
  private static final String CACHE_DIR = System.getProperty(PROP_NAME_CACHE_DIR, DEFAULT_CACHE_DIR);
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
  
  private static final String TYPESCRIPT_JS = "typescript/bin/typescriptServices.js";
  private static final String COMPILE_JS = "vertx-typescript/util/compile.js";
  
  private final VerticleFactory delegateFactory;
  private ScriptEngine engine;
  
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
  
  private synchronized ScriptEngine getEngine() {
    if (engine != null) {
      return engine;
    }
    
    // create JavaScript engine
    ScriptEngineManager mgr = new ScriptEngineManager();
    engine = mgr.getEngineByName("nashorn");
    if (engine == null) {
      throw new IllegalStateException("Could not find Nashorn JavaScript engine.");
    }
    
    // load TypeScript compiler
    loadScript(TYPESCRIPT_JS);
    
    // load compile.js
    loadScript(COMPILE_JS);
    
    return engine;
  }
  
  private void loadScript(String name) {
    URL typeScriptCompilerUrl = getClass().getClassLoader().getResource(name);
    if (typeScriptCompilerUrl == null) {
      throw new IllegalStateException("Cannot find " + name + " on classpath");
    }

    try (Reader r = new BufferedReader(new InputStreamReader(
        typeScriptCompilerUrl.openStream(), "UTF-8"))) {
      engine.eval(r);
    } catch (ScriptException | IOException e) {
      throw new IllegalStateException("Could not evaluate " + name, e);
    }
  }
  
  public class TypeScriptVerticle implements Verticle {
    private final Verticle delegateVerticle;
    
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
      delegateVerticle.getVertx().executeBlocking((Future<Void> future) -> {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(new TypeScriptClassLoader(
            cl, getEngine(), CACHE));
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
