package io.gitlab.jfronny.dbusmenu4j;

import com.canonical.*;
import org.freedesktop.dbus.connections.impl.DBusConnection;
import org.freedesktop.dbus.exceptions.DBusException;
import org.freedesktop.dbus.interfaces.DBusSigHandler;
import org.freedesktop.dbus.types.UInt32;
import org.freedesktop.dbus.types.Variant;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.util.*;
import java.util.List;

public class DbusmenuImpl implements Dbusmenu {
    private static final Logger log = LoggerFactory.getLogger("dbusmenu4j/DbusmenuImpl");
    private static final boolean isDebug = System.getProperty("io.gitlab.jfronny.dbusmenu4j.debug") != null;

    private final String menuPath;
    private final MenuHolder menuHolder;

    public DbusmenuImpl(long windowId, MenuHolder menuHolder) {
        this.menuPath = getMenuPath(windowId);
        this.menuHolder = menuHolder;
    }

    @Override
    public String getObjectPath() {
        return menuPath;
    }

    @Override
    public UInt32 getVersion() {
        return new UInt32(3);
    }

    @Override
    public String getTextDirection() {
        return "none";
    }

    @Override
    public String getStatus() {
        return "normal";
    }

    @Override
    public List<String> getIconThemePath() {
        return List.of();
    }

    @Override
    public DPair<UInt32, GetLayoutLayoutStruct> GetLayout(int parentId, int recursionDepth, List<String> propertyNames) {
        int actualRecursionDepth = recursionDepth < 0 ? Integer.MAX_VALUE : recursionDepth;
        try {
            return new DPair<>(
                    new UInt32(Integer.toUnsignedLong(parentId)),
                    getLayout(parentId, actualRecursionDepth, propertyNames, Objects.requireNonNull(menuHolder.find(parentId)))
            );
        } catch (Exception e) {
            log.error("Failed to get layout for menu " + parentId, e);
            throw e;
        }
    }

    private GetLayoutLayoutStruct getLayout(int parentId, int recursionDepth, @Nullable List<String> propertyNames, Menu menu) {
        Map<String, Variant<?>> properties = readProperties(menu);
        List<Variant<?>> children = new ArrayList<>();

        // technically, this means we return one more level than requested, but KDE doesn't like it if we don't
        if (recursionDepth < 0) return new GetLayoutLayoutStruct(menu.getId(), properties, children);

        var subMenus = menu.getChildren();
        if (subMenus != null) {
            for (Menu child : subMenus) {
                children.add(new Variant<>(getLayout(parentId, recursionDepth - 1, propertyNames, child)));
            }
            properties.put("children-display", new Variant<>("submenu"));
        }

        return new GetLayoutLayoutStruct(menu.getId(), properties, children);
    }

    @Override
    public List<GetGroupPropertiesPropertiesStruct> GetGroupProperties(List<Integer> ids, List<String> propertyNames) {
        List<GetGroupPropertiesPropertiesStruct> result = new ArrayList<>();
        for (Integer id : ids) {
            Menu menu = menuHolder.find(id);
            if (menu != null) {
                result.add(new GetGroupPropertiesPropertiesStruct(id, readProperties(menu)));
            }
        }
        return result;
    }

    @Override
    public Variant<?> GetProperty(int id, String name) {
        Menu menu = menuHolder.find(id);
        if (menu == null) return null;
        return readProperties(menu).get(name);
    }

    private Map<String, Variant<?>> readProperties(Menu menu) {
        if (menu.isSeparator()) return new HashMap<>(Map.of("type", new Variant<>("separator")));
        Map<String, Variant<?>> properties = new HashMap<>();
        properties.put("type", new Variant<>("standard"));
        properties.put("label", new Variant<>(menu.getLabel()));
        properties.put("visible", new Variant<>(menu.isVisible()));
        properties.put("enabled", new Variant<>(menu.isEnabled()));
        String[] shortcut = menu.getShortcut();
        if (shortcut != null && shortcut.length != 0) properties.put("shortcut", new Variant<>(new String[][] { shortcut }));
        String toggleType = menu.getToggleType();
        if (toggleType != null && !toggleType.isEmpty()) {
            properties.put("toggle-type", new Variant<>(menu.getToggleType()));
            properties.put("toggle-state", new Variant<>(menu.getToggleState()));
        }
        byte[] iconData = menu.getIconData();
        if (iconData != null && iconData.length != 0) properties.put("icon-data", new Variant<>(menu.getIconData()));
        return properties;
    }

    @Override
    public void Event(int id, String eventId, Variant<?> data, UInt32 timestamp) {
        switch (innerEvent(id, eventId, data, timestamp)) {
            case EventResult.Failure(var e) -> throw e instanceof RuntimeException re ? re : new RuntimeException(e);
            case EventResult.NotFound e -> {}
            case EventResult.Success e -> {}
        }
    }

    @Override
    public List<Integer> EventGroup(List<EventGroupEventsStruct> events) {
        List<Integer> result = new ArrayList<>();
        for (EventGroupEventsStruct event : events) {
            if (innerEvent(event.getMember0(), event.getMember1(), event.getMember2(), event.getMember3()) instanceof EventResult.NotFound) {
                result.add(event.getMember0());
            }
        }
        return result;
    }

    private EventResult innerEvent(int id, String eventId, @Nullable Variant<?> data, @Nullable UInt32 timestamp) {
        if (isDebug) log.warn("Event {} for menu {} ({})", eventId, id, menuHolder.find(id));
        try {
            Menu menu = menuHolder.find(id);
            if (menu == null) return new EventResult.NotFound();
            switch (eventId) {
                case "clicked" -> menu.onEvent();
                case "opened" -> menu.maybeUpdate();
                default -> log.warn("Unhandled event {} for menu {}", eventId, id);
            }
            return new EventResult.Success();
        } catch (Exception e) {
            log.error("Failed to handle event {} for menu {}", eventId, id, e);
            return new EventResult.Failure(e);
        }
    }

    @Override
    public boolean AboutToShow(int id) {
        return switch (innerAboutToShow(id)) {
            case EventResult.Failure(var e) -> throw e instanceof RuntimeException re ? re : new RuntimeException(e);
            case EventResult.NotFound e -> true;
            case EventResult.Success e -> true;
        };
    }

    @Override
    public DPair<List<Integer>, List<Integer>> AboutToShowGroup(List<Integer> ids) {
        DPair<List<Integer>, List<Integer>> result = new DPair<>(new ArrayList<>(), new ArrayList<>());
        for (Integer id : ids) {
            switch (innerAboutToShow(id)) {
                case EventResult.Failure e -> {}
                case EventResult.NotFound e -> result.getB().add(id);
                case EventResult.Success e -> result.getA().add(id);
            }
        }
        return result;
    }

    private EventResult innerAboutToShow(int id) {
        if (isDebug) log.warn("About to show menu {}", id);
        try {
            Menu menu = menuHolder.find(id);
            if (menu == null) return new EventResult.NotFound();
            menu.maybeUpdate();
            return new EventResult.Success();
        } catch (Exception e) {
            log.error("Failed to update menu {}", id, e);
            return new EventResult.Failure(e);
        }
    }

    private sealed interface EventResult {
        record Success() implements EventResult {}
        record NotFound() implements EventResult {}
        record Failure(Exception e) implements EventResult {}
    }

    private final DBusSigHandler<ItemsPropertiesUpdated> itemsPropertiesUpdated = (item) -> {
        if (isDebug) log.warn("Items properties updated (updated: {}, removed: {})", item.getUpdatedProps(), item.getRemovedProps());
    };

    private final DBusSigHandler<LayoutUpdated> layoutUpdated = (layout) -> {
        if (isDebug) log.warn("Layout updated (parent: {}, revision: {})", layout.getParent(), layout.getRevision());
    };

    private final DBusSigHandler<ItemActivationRequested> itemActivationRequested = (item) -> {
        if (isDebug) log.warn("Item activation requested: {}", item.getId());
    };

    public AutoCloseable export(DBusConnection conn) throws DBusException {
        if (isDebug) log.warn("Exporting menu {} to {}", menuPath, conn.getUniqueName());
        conn.exportObject(menuPath, this);
        List<AutoCloseable> signals = List.of(
                conn.addSigHandler(ItemsPropertiesUpdated.class, itemsPropertiesUpdated),
                conn.addSigHandler(LayoutUpdated.class, layoutUpdated),
                conn.addSigHandler(ItemActivationRequested.class, itemActivationRequested)
        );
        return () -> {
            conn.unExportObject(menuPath);
            for (AutoCloseable signal : signals) {
                signal.close();
            }
        };
    }

    public static String getMenuPath(long windowId) {
        return "/com/canonical/menu/0x" + Long.toString(windowId, 16);
    }
}
