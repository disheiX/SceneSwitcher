package me.disheiX.switcher.state;

import me.disheiX.switcher.SceneSwitcherOptions;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ObsState {
    private String name;
    private int width;
    private int height;
    private String activeScene;
    private List<String> toggledSources;

    public ObsState(String name, String sizeString, String activeScene, String toggledSources) {
        this.setName(name);
        this.setDimensions(sizeString);
        this.setActiveScene(activeScene);
        this.setToggledSources(toggledSources);
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setActiveScene(String scene) {
        this.activeScene = scene;
    }

    public void setToggledSources(String toggledSources) {
        this.toggledSources = Arrays.stream(toggledSources.split(",")).map(String::trim).collect(Collectors.toList());
    }

    public void setDimensions(String string) {
        String[] dimensions = string.split("x");
        this.width = Integer.parseInt(dimensions[0]);
        this.height = Integer.parseInt(dimensions[1]);
    }

    public String getName() {
        return this.name;
    }

    public String getActiveScene() {
        return this.activeScene;
    }

    public List<String> getToggledSources() {
        return this.toggledSources;
    }

    public String getDimensions() {
        return this.width + "x" + this.height;
    }

    public String getFullString(ObsState oldState) {
        List<String> oldSources = oldState.name.equals("Playing") ? SceneSwitcherOptions.getAllSources() : oldState.toggledSources;
        List<String> newSources = this.name.equals("Playing") ? SceneSwitcherOptions.getAllSources() : this.toggledSources;

        String toggleOn = newSources.stream()
                .filter(newSource -> !oldSources.contains(newSource))
                .filter(s -> !s.isEmpty())
                .collect(Collectors.joining("&"));
        String toggleOff = oldSources.stream()
                .filter(oldSource -> !newSources.contains(oldSource))
                .filter(s -> !s.isEmpty())
                .collect(Collectors.joining("&"));

        return String.join("|", "name=" + this.getName(), "scene=" + this.getActiveScene(), "on=" + toggleOn, "off=" + toggleOff);
    }

    public boolean matchesRectangle(Rectangle rectangle) {
        return this.width == rectangle.width && this.height == rectangle.height;
    }

    public static boolean isValidDimensionsString(String input) {
        return Pattern.matches("^\\d+x\\d+$", input) && !SceneSwitcherOptions.matchingExistingDimensions(input);
    }

    public static boolean isValidSourcesListString(String input) {
        return Pattern.matches("^([\\w ]+:[\\w ]+($|[, ]+))*$", input);
    }
}
