package io.gitlab.jfronny.dbusmenu4j;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Collectors;

public class SwingMenu<R extends SwingRootMenu<R, ?>, T extends SwingMenuHolder<R>> extends Menu.Abstract {
    private static final Logger log = LoggerFactory.getLogger("dbusmenu4j/SwingMenu");

    protected final @Nullable JMenuItem menuItem;
    private final int id;
    protected final T holder;

    public SwingMenu(@Nullable JMenuItem menuItem, int id, T holder) {
        this.menuItem = menuItem;
        this.id = id;
        this.holder = holder;
    }

    public SwingMenu(JMenuItem menuItem, T holder) {
        this(menuItem, holder.getId(menuItem), holder);
    }

    public SwingMenu(int id, T holder) {
        this(null, id, holder);
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public boolean isSeparator() {
        return menuItem == null;
    }

    @Override
    public @NotNull String getLabel() {
        if (menuItem == null) return "";
        String text = menuItem.getText();
        return text == null ? "" : text;
    }

    @Override
    public boolean isEnabled() {
        if (menuItem == null) return false;
        return menuItem.isEnabled();
    }

    @Override
    public boolean isVisible() {
        if (menuItem == null) return false;
        return menuItem.isVisible();
    }

    private final Object iconDataLock = new Object();
    private boolean hasIconData = false;
    private byte[] iconData = null;

    @Override
    public byte @Nullable [] getIconData() {
        if (hasIconData) return iconData;
        synchronized (iconDataLock) {
            if (hasIconData) return iconData;
            hasIconData = true;
            if (menuItem == null) return null;
            Icon icon = menuItem.getIcon();
            if (icon == null) return null;
            // This is somewhat expensive, but we need to do it
            // At least avoid doing it multiple times
            int width = icon.getIconWidth();
            int height = icon.getIconHeight();

            BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

            var graphics = bufferedImage.createGraphics();
            icon.paintIcon(menuItem, graphics, 0, 0);
            graphics.dispose();

            try (ByteArrayOutputStream stream = new ByteArrayOutputStream()) {
                ImageIO.write(bufferedImage, "png", stream);
                return iconData = stream.toByteArray();
            } catch (Exception e) {
                log.error("Failed to convert icon to byte array", e);
                return iconData = null;
            }
        }
    }

    private static final Map<Integer, String> keyEvents = Arrays.stream(KeyEvent.class.getFields())
            .filter(it -> it.getModifiers() == (Modifier.PUBLIC | Modifier.STATIC | Modifier.FINAL) )
            .filter(it -> it.getName().startsWith("VK_"))
            .collect(Collectors.toMap(
                    s -> {
                        try {
                            return s.getInt(null);
                        } catch (IllegalAccessException e) {
                            throw new RuntimeException(e);
                        }
                    },
                    s -> s.getName().substring(3),
                    (a, b) -> a
            ));

    @Override
    public String @Nullable [] getShortcut() {
        if (menuItem == null) return null;
        KeyStroke ks = menuItem.getAccelerator();
        if (ks == null) return null;
        List<String> result = new ArrayList<>();
        int modifiers = ks.getModifiers();
        if ((modifiers & InputEvent.SHIFT_DOWN_MASK) != 0) result.add("Shift");
        if ((modifiers & InputEvent.CTRL_DOWN_MASK) != 0) result.add("Ctrl");
        if ((modifiers & InputEvent.META_DOWN_MASK) != 0) result.add("Meta");
        if ((modifiers & InputEvent.ALT_DOWN_MASK) != 0) result.add("Alt");
        if ((modifiers & InputEvent.ALT_GRAPH_DOWN_MASK) != 0) result.add("AltGraph");
        if ((modifiers & InputEvent.BUTTON1_DOWN_MASK) != 0) result.add("Button1");
        if ((modifiers & InputEvent.BUTTON2_DOWN_MASK) != 0) result.add("Button2");
        if ((modifiers & InputEvent.BUTTON3_DOWN_MASK) != 0) result.add("Button3");
        String keyEvent = keyEvents.get(ks.getKeyCode());
        result.add(keyEvent != null ? keyEvent : "UNKNOWN");
        return result.toArray(String[]::new);
    }

    @Override
    public @Nullable String getToggleType() {
        return switch (menuItem) {
            case JRadioButtonMenuItem b -> "radio";
            case JCheckBoxMenuItem c -> "checkmark";
            case null, default -> null;
        };
    }

    @Override
    public int getToggleState() {
        String toggleType = getToggleType();
        if (toggleType == null || toggleType.isEmpty()) return -1;
        if (menuItem.isSelected()) return 1;
        return 0;
    }

    private List<Menu> children = null;
    @Override
    public @Nullable List<Menu> getChildren() {
        return children;
    }

    @Override
    public void onEvent() {
        switch (menuItem) {
            case JCheckBoxMenuItem c -> c.setSelected(!c.isSelected());
            case JRadioButtonMenuItem r -> r.setSelected(true);
            case null, default -> {}
        }
        var event = new ActionEvent(menuItem, ActionEvent.ACTION_PERFORMED, menuItem.getActionCommand());
        for (ActionListener listener : menuItem.getActionListeners()) {
            listener.actionPerformed(event);
        }
    }

    public void syncChildren(int depth) {
        if (menuItem instanceof JMenu menu) {
            children = new ArrayList<>();
            int len = menu.getItemCount();
            for (int i = 0; i < len; i++) {
                JMenuItem item = menu.getItem(i);
                if (item == null) continue;
                SwingMenu<R, T> swingMenu = child(item, holder);
                if (depth > 0) swingMenu.syncChildren(depth - 1);
                children.add(swingMenu);
            }
        }
    }

    protected SwingMenu<R, T> child(JMenuItem item, T holder) {
        return new SwingMenu<>(item, holder);
    }

    @Override
    public String toString() {
        return "SwingMenu(id=" + id + ", menuItem=" + menuItem + ")";
    }
}
