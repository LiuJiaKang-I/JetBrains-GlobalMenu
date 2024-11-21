package io.gitlab.jfronny.dbusmenu4j;

import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.List;

public class SwingMenuHolder<T extends SwingRootMenu<T, ?>> implements MenuHolder {
    protected T root;

    protected SwingMenuHolder(T root) {
        this.root = root;
    }

    private SwingMenuHolder(JMenuBar bar, String menuItem, DMLog log) {
        this.root = (T) new SwingRootMenu<>(bar, menuItem, this, log);
        this.root.syncChildren();
    }

    public static SwingMenuHolder<?> get(JMenuBar bar, String menuItem, DMLog log) {
        return new SwingMenuHolder<>(bar, menuItem, log);
    }

    @Override
    public @Nullable Menu find(int menuId) {
        if (menuId == 0) return root;
        return find(root, menuId);
    }

    private @Nullable Menu find(Menu parent, int menuId) {
        List<Menu> children = parent.getChildren();
        if (children == null) return null;
        for (Menu child : children) {
            if (child.getId() == menuId) return child;
            Menu found = find(child, menuId);
            if (found != null) return found;
        }
        return null;
    }

    public int getId(@Nullable JMenuItem menuItem) {
        return System.identityHashCode(menuItem);
    }
}
