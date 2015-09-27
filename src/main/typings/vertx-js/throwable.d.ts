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
