package io.gitlab.jfronny.dbusmenu4j;

import org.freedesktop.dbus.Tuple;
import org.freedesktop.dbus.annotations.Position;

public class DPair<A, B> extends Tuple {
    @Position(0) private A a;
    @Position(1) private B b;

    public DPair(A a, B b) {
        this.a = a;
        this.b = b;
    }

    public void setA(A a) {
        this.a = a;
    }

    public A getA() {
        return a;
    }

    public void setB(B b) {
        this.b = b;
    }

    public B getB() {
        return b;
    }
}
