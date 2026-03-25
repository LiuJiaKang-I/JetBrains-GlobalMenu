package dev.jfronny.dbusmenu4j;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface Menu {
    int getId();
    boolean isSeparator();
    @NotNull String getLabel();
    boolean isEnabled();
    boolean isVisible();
    byte @Nullable [] getIconData();
    String @Nullable [] getShortcut();
    @Nullable String getToggleType();
    int getToggleState();
    @Nullable List<Menu> getChildren();

    void onEvent();
    void update();
    void maybeUpdate();

    abstract class Abstract implements Menu {
        private long lastUpdated = 0;

        @Override
        public void maybeUpdate() {
            if (System.currentTimeMillis() - lastUpdated > 1000) update();
        }

        @Override
        public void update() {
            lastUpdated = System.currentTimeMillis();
        }
    }
}
