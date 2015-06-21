declare var Java: {
  type(name: string): any;
  extend(t: any): any;
  from(arr: any): Array<any>;
  to(obj: any, t: string): any;
  super(t: any): any;
  synchronized<T>(f: T): T;
}
