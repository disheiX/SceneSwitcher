package me.disheiX.switcher;

import me.disheiX.switcher.util.ResizingStateUtil;
import com.google.common.io.Resources;
import me.disheiX.switcher.gui.SceneSwitcherGUI;
import org.apache.logging.log4j.Level;
import xyz.duncanruns.jingle.Jingle;
import xyz.duncanruns.jingle.JingleAppLaunch;
import xyz.duncanruns.jingle.gui.JingleGUI;
import xyz.duncanruns.jingle.plugin.PluginHotkeys;
import xyz.duncanruns.jingle.plugin.PluginManager;
import xyz.duncanruns.jingle.util.FileUtil;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.io.InputStream;
import java.util.Objects;
import javax.swing.Timer;

public class SceneSwitcher {
    private static final Path OUT = Jingle.FOLDER.resolve("obs-switcher-state");
    private static final Path LUA_SCRIPT_PATH = Jingle.FOLDER.resolve("jingle-obs-switcher.lua");
    private static long lastUpdate = 0L;
    private static String last = "";
    private static SceneState currentScene = SceneState.NONE; // Change default state
    private static Timer resizeCheckTimer;  // Add timer field

    private static void setCurrentScene(SceneState newScene) {
        currentScene = newScene;
        tick();
    }

    private enum SceneState {
        PLANNAR_ABUSE,
        MAG,
        THIN,
        CUSTOM,
        PLAYING,
        NONE;

        private String customName;

        public void setCustomName(String name) {
            this.customName = name;
        }

        public String formatOutput() {
            StringBuilder output = new StringBuilder();
            output.append(PLAYING.customName != null ? PLAYING.customName : "N").append("|");
            output.append(currentScene == PLANNAR_ABUSE ? PLANNAR_ABUSE.customName : "N").append("|");
            output.append(currentScene == MAG ? MAG.customName : "N").append("|");
            output.append(currentScene == THIN ? THIN.customName : "N").append("|");
            output.append(currentScene == CUSTOM ? CUSTOM.customName : "N");
            return output.toString();
        }
    }

    public static void main(String[] args) throws IOException {
        JingleAppLaunch.launchWithDevPlugin(args, PluginManager.JinglePluginData.fromString(
                Resources.toString(Resources.getResource(SceneSwitcher.class, "/jingle.plugin.json"), Charset.defaultCharset())
        ), SceneSwitcher::initialize);
    }

    public static void initialize() {
        String version = SceneSwitcher.class.getPackage().getImplementationVersion();
        Jingle.log(Level.INFO, "SceneSwitcher v" + (version != null ? version : "DEV") + " plugin initialized");
        JingleGUI.addPluginTab("OBS Scene Switcher", new SceneSwitcherGUI());
        PluginHotkeys.addHotkeyAction("Scene Switcher - Eye Measuring", () -> switchToMag(SceneSwitcherOptions.getInstance().mag_scene.name));
        PluginHotkeys.addHotkeyAction("Scene Switcher - Plannar Abuse", () -> switchToPlannarAbuse(SceneSwitcherOptions.getInstance().plannar_abuse_scene.name));
        PluginHotkeys.addHotkeyAction("Scene Switcher - Thin BT", () -> switchToThin(SceneSwitcherOptions.getInstance().thin_scene.name));
        generateResources();
        startResizeCheckTimer();
    }

    // Fallback in case the resize check fails
    private static void startResizeCheckTimer() {
        if (resizeCheckTimer != null) {
            resizeCheckTimer.stop();
        }
        resizeCheckTimer = new Timer(1000, e -> {
            if (SceneSwitcherOptions.getInstance().enabled && 
                !ResizingStateUtil.isCurrentlyResized() 
                && (currentScene != SceneState.PLAYING)
                && (currentScene != SceneState.CUSTOM)) {
                    setCurrentScene(SceneState.PLAYING);
            }
        });
        resizeCheckTimer.start();
    }

    private static void generateResources() {
        try {
            String scriptPath = "jingle-obs-switcher.lua";
            InputStream from = SceneSwitcher.class.getResourceAsStream("/" + scriptPath);
            Files.copy(from, LUA_SCRIPT_PATH);
        } catch (FileAlreadyExistsException e) {
            Jingle.log(Level.INFO, "(SceneSwitcher) jingle-obs-switcher.lua already exists");
        } catch (IOException e) {
            Jingle.logError("(SceneSwitcher) Failed to write the obs link script", e);
        }
    }

    public static Path getLuaScriptPath() {
        return LUA_SCRIPT_PATH;
    }

    public static void tick() {
        long currentTime = System.currentTimeMillis();
        if (Math.abs(currentTime - lastUpdate) > 10L) {
            lastUpdate = currentTime;
            String output = currentScene.formatOutput();
            if (Objects.equals(output, last))
                return; 
            last = output;
            try {
                FileUtil.writeString(OUT, output);
            } catch (IOException e) {
                Jingle.logError("(SceneSwitcher) Failed to write obs-link-state:", e);
            } 
        } 
    }

    public static void switchToPlannarAbuse(String sceneName) {
        if (!SceneSwitcherOptions.getInstance().enabled) return;
        boolean isResized = ResizingStateUtil.isCurrentlyResized();
        if (isResized) {
            SceneState.PLANNAR_ABUSE.setCustomName(sceneName);
            setCurrentScene(SceneState.PLANNAR_ABUSE);
        } else {
            setCurrentScene(SceneState.PLAYING);
        }
    }

    public static void switchToMag(String sceneName) {
        if (!SceneSwitcherOptions.getInstance().enabled) return;
        boolean isResized = ResizingStateUtil.isCurrentlyResized();
        if (isResized) {
            SceneState.MAG.setCustomName(sceneName);
            setCurrentScene(SceneState.MAG);
        } else {
            setCurrentScene(SceneState.PLAYING);
        }
    }

    public static void switchToThin(String sceneName) {
        if (!SceneSwitcherOptions.getInstance().enabled) return;
        boolean isResized = ResizingStateUtil.isCurrentlyResized();
        if (isResized) {
            SceneState.THIN.setCustomName(sceneName);
            setCurrentScene(SceneState.THIN);
        } else {
            setCurrentScene(SceneState.PLAYING);
        }
    }

    public static void switchToCustom(String scene) {
        if (!SceneSwitcherOptions.getInstance().enabled) return;
        if (currentScene != SceneState.CUSTOM) {
            SceneState.CUSTOM.setCustomName(scene);
            setCurrentScene(SceneState.CUSTOM);
        } else {
            setCurrentScene(SceneState.PLAYING);
        }
    }

    public static void updatePlayingScene(String sceneName) {
        if (!SceneSwitcherOptions.getInstance().enabled) return;
        SceneState.PLAYING.setCustomName(sceneName);
        tick();
    }
}
