package io.vertx.lang.typescript;

import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Verticle;
import io.vertx.core.Vertx;
import io.vertx.core.spi.VerticleFactory;
import io.vertx.lang.js.JSVerticleFactory;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

public class TypeScriptVerticleFactory implements VerticleFactory {
  private static final String TYPESCRIPT_JS = "typescript/bin/typescriptServices.js";
  private static final String COMPILE_JS = "vertx-typescript/compile.js";
  
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

    try (Reader r = new InputStreamReader(typeScriptCompilerUrl.openStream(), "UTF-8")) {
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
      ClassLoader cl = Thread.currentThread().getContextClassLoader();
      Thread.currentThread().setContextClassLoader(new TypeScriptClassLoader(cl, getEngine()));
      try {
        delegateVerticle.start(startFuture);
      } finally {
        Thread.currentThread().setContextClassLoader(cl);
      }
    }

    @Override
    public void stop(Future<Void> stopFuture) throws Exception {
      delegateVerticle.stop(stopFuture);
    }
  }
}
