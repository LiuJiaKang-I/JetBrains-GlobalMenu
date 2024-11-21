package io.gitlab.jfronny.dbusmenu4j;

public interface DMLog {
    void warn(String message);
    void error(String text, Throwable exception);
    boolean isDebug();
}
