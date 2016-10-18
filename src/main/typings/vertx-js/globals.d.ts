/// <reference path="./vertx.d.ts"/>
// Global object and type definitions for vertx-js

declare var vertx: Vertx;

/**
 * A simple console object that can be used to print log messages
 * errors, and warnings.
 * @example
 *
 * console.log('Hello standard out');
 * console.warn('Warning standard error');
 * console.error('Alert! Alert!');
 *
 * see vertx-js/util/console.js
 *
 */
interface Console {
  /**
   * Log the msg to STDOUT.
   * @param msg The message to log to standard out.
   */
  log(msg: any);

  /**
   * Log the msg to STDERR
   * @param msg The message to log with a warning to standard error.
   */
  warn(msg: any);

  /**
   * Log the msg to STDERR
   * @param msg The message to log with a warning alert to standard error.
   */
  error(msg: any);
}

declare var console: Console;

/**
 * Global variable providing access to the Java jvm environment
 */
declare var Java: {
  type(name: string): any;
  extend(t: any): any;
  from(arr: any): Array<any>;
  to(obj: any, t: string): any;
  super(t: any): any;
  synchronized<T>(f: T, lockObj?: any): T;
}

/**
 * Global type representing a java throwable
 */
interface Throwable {
  fillInStackTrace(): Throwable;
  getCause(): Throwable;
  getLocalizedMessage(): string;
  getMessage(): string;
  getStackTrace(): any[] /*StackTraceElement[]*/;
  getSuppressed(): Throwable[];
  initCause(cause: Throwable): Throwable;
  printStackTrace(): void;
  printStackTrace(s: any /*PrintStream*/): void;
  printStackTrace(s: any /*PrintWriter*/): void;
  setStackTrace(stackTrace: any[] /*StackTraceElement[]*/): void;
  toString(): string;
}
