package me.disheiX.switcher.state;

import org.apache.logging.log4j.Level;
import xyz.duncanruns.jingle.Jingle;
import xyz.duncanruns.jingle.util.FileUtil;

import java.io.IOException;
import java.nio.file.Path;

public class ObsStateManager {
    private static final Path OUT = Jingle.FOLDER.resolve("obs-switcher-state");

    public static ObsState updateState(ObsState lastState, ObsState newState) {
        String output = newState.getFullString(lastState);
        try {
            FileUtil.writeString(OUT, output);
            Jingle.log(Level.DEBUG, "(SceneSwitcher) Writing new state: " + output + "\n" +
                    "to file: " + OUT);
        } catch (IOException e) {
            Jingle.logError("(SceneSwitcher) Failed to write obs-switcher-state:", e);
        }
        return newState;
    }
}
