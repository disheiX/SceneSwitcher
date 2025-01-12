package me.disheiX.switcher.util;

import xyz.duncanruns.jingle.resizing.Resizing;
import java.lang.reflect.Field;

public class ResizingStateUtil {
    private static Field currentlyResizedField;

    static {
        try {
            currentlyResizedField = Resizing.class.getDeclaredField("currentlyResized");
            currentlyResizedField.setAccessible(true);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException("Failed to access Resizing.currentlyResized field", e);
            
        }
    }

    public static boolean isCurrentlyResized() {
        try {
            return (boolean) currentlyResizedField.get(null);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Failed to access Resizing.currentlyResized value", e);
        }
    }
}
