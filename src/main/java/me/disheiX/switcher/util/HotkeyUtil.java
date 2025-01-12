package me.disheiX.switcher.util;

import xyz.duncanruns.jingle.plugin.PluginHotkeys;
import xyz.duncanruns.jingle.Jingle;
import java.lang.reflect.Field;
import java.util.Map;

public class HotkeyUtil {
    private static Field actionsMapField;

    static {
        try {
            actionsMapField = PluginHotkeys.class.getDeclaredField("HOTKEYS_ACTIONS");
            actionsMapField.setAccessible(true);
        } catch (NoSuchFieldException e) {
            Jingle.logError("Failed to access PluginHotkeys.actions field", e);
            throw new RuntimeException("Failed to access PluginHotkeys.actions field", e);
        }
    }

    @SuppressWarnings("unchecked")
    public static void removeHotkeyAction(String actionName) {
        try {
            Map<String, Runnable> actions = (Map<String, Runnable>) actionsMapField.get(null);
            actions.remove(actionName);
        } catch (IllegalAccessException e) {
            Jingle.logError("Failed to remove hotkey action", e);
            throw new RuntimeException("Failed to remove hotkey action", e);
        }
    }
}
