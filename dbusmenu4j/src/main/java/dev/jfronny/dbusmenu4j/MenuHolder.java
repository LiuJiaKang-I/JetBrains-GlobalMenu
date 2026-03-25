package dev.jfronny.dbusmenu4j;

import org.jetbrains.annotations.Nullable;

public interface MenuHolder {
    @Nullable Menu find(int menuId);
}
