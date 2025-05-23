package me.disheiX.switcher;

import me.disheiX.switcher.state.ObsState;
import me.disheiX.switcher.state.ObsStateManager;
import me.disheiX.switcher.util.ResizingStateUtil;
import com.google.common.io.Resources;
import me.disheiX.switcher.gui.SceneSwitcherGUI;
import org.apache.logging.log4j.Level;
import xyz.duncanruns.jingle.Jingle;
import xyz.duncanruns.jingle.JingleAppLaunch;
import xyz.duncanruns.jingle.gui.JingleGUI;
import xyz.duncanruns.jingle.instance.InstanceState;
import xyz.duncanruns.jingle.plugin.PluginEvents;
import xyz.duncanruns.jingle.plugin.PluginManager;
import xyz.duncanruns.jingle.script.CustomizableManager;
import xyz.duncanruns.jingle.util.WindowStateUtil;

import java.awt.*;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicLong;

public class SceneSwitcher {
    private static final Path LUA_SCRIPT_PATH = Jingle.FOLDER.resolve("jingle-obs-switcher.lua");
    private static ObsState lastState;

    public static void main(String[] args) throws IOException {
        JingleAppLaunch.launchWithDevPlugin(args, PluginManager.JinglePluginData.fromString(
                Resources.toString(Resources.getResource(SceneSwitcher.class, "/jingle.plugin.json"), Charset.defaultCharset())
        ), SceneSwitcher::initialize);
    }

    public static void initialize() {
        String version = SceneSwitcher.class.getPackage().getImplementationVersion();
        Jingle.log(Level.INFO, "SceneSwitcher v" + (version != null ? version : "DEV") + " plugin initialized");

        CustomizableManager.load();

        SceneSwitcherOptions.load();
        lastState = SceneSwitcherOptions.getDefaultState();

        JingleGUI.addPluginTab("OBS Scene Switcher", new SceneSwitcherGUI());
        generateResources();

        AtomicLong timeTracker = new AtomicLong(System.currentTimeMillis());

        PluginEvents.END_TICK.register(() -> {
            long currentTime = System.currentTimeMillis();
            if (currentTime - timeTracker.get() > 5) {
                try {
                    if (SceneSwitcherOptions.getInstance().enabled) {
                        updateState();
                    }
                } catch (Exception e) {
                    Jingle.logError("(SceneSwitcher) Error while checking for resize", e);
                }
                timeTracker.set(currentTime);
            }
        });
    }

    public static void updateState() {
        ObsState currentState;

        if (ResizingStateUtil.isCurrentlyResized()) {
            assert Jingle.getMainInstance().isPresent();
            Rectangle currentRectangle = WindowStateUtil.getHwndRectangle(Jingle.getMainInstance().get().hwnd);
            currentState = SceneSwitcherOptions.getStateFromRectangle(currentRectangle);
        } else if (Jingle.getMainInstance().map(i -> i.stateTracker.isCurrentState(InstanceState.WALL)).orElse(false)) {
            currentState = SceneSwitcherOptions.getStateFromName("Walling");
        } else {
            currentState = SceneSwitcherOptions.getDefaultState();
        }

        if (lastState.getName().equals(currentState.getName())) {
            return;
        }

        lastState = ObsStateManager.updateState(lastState, currentState);
    }

    private static void generateResources() {
        String scriptPath = "jingle-obs-switcher.lua";
        try (InputStream from = SceneSwitcher.class.getResourceAsStream("/" + scriptPath)) {
            assert from != null;
            Files.copy(from, LUA_SCRIPT_PATH, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            Jingle.logError("(SceneSwitcher) Failed to write the state link script", e);
        }
    }

    public static Path getLuaScriptPath() {
        return LUA_SCRIPT_PATH;
    }
}
