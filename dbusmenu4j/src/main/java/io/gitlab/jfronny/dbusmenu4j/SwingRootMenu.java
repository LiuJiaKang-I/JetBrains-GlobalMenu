package io.gitlab.jfronny.dbusmenu4j;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.List;
import java.util.stream.IntStream;

public class SwingRootMenu<R extends SwingRootMenu<R, ?>, T extends SwingMenuHolder<R>> extends Menu.Abstract {
    protected @Nullable List<JMenuItem> menuItems;
    private final String name;
    private final T holder;

    public SwingRootMenu(@Nullable List<JMenuItem> menuItems, String name, T holder) {
        this.menuItems = menuItems;
        this.name = name;
        this.holder = holder;
    }

    public SwingRootMenu(JMenuBar bar, String name, T holder) {
        this(IntStream.range(0, bar.getMenuCount()).<JMenuItem>mapToObj(bar::getMenu).toList(), name, holder);
    }

    @Override
    public int getId() {
        return 0;
    }

    @Override
    public boolean isSeparator() {
        return menuItems == null;
    }

    @Override
    public @NotNull String getLabel() {
        return name;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public boolean isVisible() {
        return true;
    }

    @Override
    public byte @Nullable [] getIconData() {
        return null;
    }

    @Override
    public String @Nullable [] getShortcut() {
        return null;
    }

    @Override
    public @Nullable String getToggleType() {
        return null;
    }

    @Override
    public int getToggleState() {
        return 0;
    }

    private @Nullable List<Menu> children = null;

    @Override
    public @Nullable List<Menu> getChildren() {
        return children;
    }

    protected void syncChildren() {
        if (menuItems == null) children = null;
        else children = menuItems.stream().<Menu>map(s -> {
            SwingMenu<R, T> menu = child(s, holder);
            menu.syncChildren(2); // setting this to 2 may help prevent missing entries but is SLLOOOOWWW
            return menu;
        }).toList();
    }

    protected SwingMenu<R, T> child(JMenuItem item, T holder) {
        return new SwingMenu<>(item, holder);
    }

    @Override
    public void onEvent() {
    }

    @Override
    public void update() {
        super.update();
        runOnEDT(this::syncChildren);
    }

    protected void runOnEDT(Runnable runnable) {
        if (SwingUtilities.isEventDispatchThread()) {
            runnable.run();
        } else {
            SwingUtilities.invokeLater(runnable);
        }
    }
}
