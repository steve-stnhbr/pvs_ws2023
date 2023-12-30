package at.ac.tuwien.ifs.sge.agent.risk.util;

public class Triple<A,B,C> {
  private A a;
  private B b;
  private C c;

  public Triple(A a, B b, C c) {
    this.a = a;
    this.b = b;
    this.c = c;
  }

  public A getA() {
    return a;
  }

  public Triple<A, B, C> setA(A a) {
    this.a = a;
    return this;
  }

  public B getB() {
    return b;
  }

  public Triple<A, B, C> setB(B b) {
    this.b = b;
    return this;
  }

  public C getC() {
    return c;
  }

  public Triple<A, B, C> setC(C c) {
    this.c = c;
    return this;
  }
}
