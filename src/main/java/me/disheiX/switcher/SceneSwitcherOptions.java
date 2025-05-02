package me.disheiX.switcher;

import com.google.gson.*;
import me.disheiX.switcher.state.ObsState;
import xyz.duncanruns.jingle.Jingle;
import xyz.duncanruns.jingle.instance.InstanceState;
import xyz.duncanruns.jingle.script.CustomizableManager;
import xyz.duncanruns.jingle.util.FileUtil;

import java.awt.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.List;

public class SceneSwitcherOptions {
    private static final Path CONFIG_PATH = Jingle.FOLDER.resolve("scene-switcher-config.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().serializeNulls().create();
    private static SceneSwitcherOptions instance;

    public boolean enabled;
    public List<ObsState> obsStates;

    public static void load() {
        if (!Files.exists(CONFIG_PATH)) {
            loadDefaultSettings();
        } else {
            String s;
            try {
                s = FileUtil.readString(CONFIG_PATH);
                instance = GSON.fromJson(s, SceneSwitcherOptions.class);
            } catch (IOException | JsonSyntaxException e) {
                Jingle.logError("(SceneSwitcher) Error while reading settings, resetting back to default:", e);
                loadDefaultSettings();
            }
        }
        validate();
        save();
    }

    public static void validate() {
        if (instance.obsStates == null) {
            instance.obsStates = defaultObsStates();
            return;
        }

        List<ObsState> remove = new ArrayList<>();
        for (ObsState obsState: instance.obsStates) {
            try {
                if (obsState.getName().isEmpty() || obsState.getActiveScene().isEmpty() || obsState.getToggledSources().isEmpty()) {
                    throw new NullPointerException();
                }
                obsState.getDimensions();
            } catch (NullPointerException e) {
                remove.add(obsState);
            }
        }
        instance.obsStates.removeAll(remove);
        if (instance.obsStates.isEmpty()) {
            instance.obsStates = defaultObsStates();
        }
        getDefaultState();
        getWallingState();
    }

    public static void save() {
        try {
            FileUtil.writeString(CONFIG_PATH, GSON.toJson(instance));
        } catch (IOException e) {
            Jingle.logError("(SceneSwitcher) Failed to save SceneSwitcher settings:", e);
        }
    }

    private static void loadDefaultSettings() {
        instance = new SceneSwitcherOptions();
        instance.enabled = true;
        instance.obsStates = defaultObsStates();
    }

    private static List<ObsState> defaultObsStates() {
        List<ObsState> obsStates = new ArrayList<>();
        obsStates.add(new ObsState("Playing", "0x0", "Playing", ""));
        obsStates.add(new ObsState("Walling", "0x0", "Walling", ""));
        obsStates.add(new ObsState("eye_measuring", CustomizableManager.get("Resizing", "eye_measuring"), "Playing", ""));
        obsStates.add(new ObsState("planar_abuse", CustomizableManager.get("Resizing", "planar_abuse"), "Playing", ""));
        obsStates.add(new ObsState("thin_bt", CustomizableManager.get("Resizing", "thin_bt"), "Playing", ""));
        return obsStates;
    }

    public static ObsState getWallingState() {
        return instance.obsStates.stream().filter(obsState -> obsState.getName().equals("Walling")).findFirst().orElseGet(() -> {
            instance.obsStates.add(1, new ObsState("Walling", "0x0", "Walling", ""));
            return instance.obsStates.get(1);
        });
    }

    public static ObsState getDefaultState() {
        return instance.obsStates.stream().filter(obsState -> obsState.getName().equals("Playing")).findFirst().orElseGet(() -> {
            instance.obsStates.add(0, new ObsState("Playing", "0x0", "Playing", ""));
            return instance.obsStates.get(0);
        });
    }

    public static ObsState getStateMatchingRectangle(Rectangle rectangle) {
        return instance.obsStates.stream()
                .filter(obsState -> obsState.matchesRectangle(rectangle))
                .findFirst().orElseGet(() -> {
                    if (Jingle.getMainInstance().map(i -> i.stateTracker.isCurrentState(InstanceState.WALL)).orElse(false)) {
                        return getWallingState();
                    } else {
                        return getDefaultState();
                    }
                });
    }

    public static boolean matchingExistingName(String nameString) {
        return instance.obsStates.stream().anyMatch(obsState -> obsState.getName().equals(nameString));
    }

    public static boolean matchingExistingDimensions(String sizeString) {
        return instance.obsStates.stream().anyMatch(obsState -> obsState.getDimensions().equals(sizeString));
    }

    public static SceneSwitcherOptions getInstance() {
        return instance;
    }
}