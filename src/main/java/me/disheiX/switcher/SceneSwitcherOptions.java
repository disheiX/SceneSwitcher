package me.disheiX.switcher;

import com.google.gson.*;
import xyz.duncanruns.jingle.Jingle;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class SceneSwitcherOptions {
    public static class SceneData {
        public String name = "";

        public SceneData() {}

        public SceneData(String name) {
            this.name = name;
        }
    }

    public SceneData mag_scene = new SceneData();
    public SceneData plannar_abuse_scene = new SceneData();
    public SceneData thin_scene = new SceneData();
    public SceneData playing_scene = new SceneData();
    public boolean enabled = true;
    public Map<String, SceneData> custom_scenes = new HashMap<>();

    public SceneSwitcherOptions() {
        // default values for new instances
        mag_scene.name = "Jingle Mag";
        playing_scene.name = "Playing";
        plannar_abuse_scene.name = "";
        thin_scene.name = "";
        enabled = true;
    }

    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(SceneData.class, new JsonDeserializer<SceneData>() {
                @Override
                public SceneData deserialize(JsonElement json, java.lang.reflect.Type typeOfT, JsonDeserializationContext context) {
                    if (json.isJsonPrimitive()) {
                        // Handle old format where scene was just a string
                        return new SceneData(json.getAsString());
                    } else {
                        // Handle new format
                        JsonObject obj = json.getAsJsonObject();
                        return new SceneData(obj.has("name") ? obj.get("name").getAsString() : "");
                    }
                }
            })
            .setPrettyPrinting()
            .create();

    private static final Path CONFIG_PATH = Jingle.FOLDER.resolve("scene-switcher-config.json");
    private static SceneSwitcherOptions instance;

    public static void save() throws IOException {
        try (FileWriter writer = new FileWriter(CONFIG_PATH.toFile())) {
            writer.write(GSON.toJson(getInstance()));
        }
    }

    public static SceneSwitcherOptions getInstance() {
        if (instance == null) {
            try {
                if (Files.exists(CONFIG_PATH)) {
                    String json = new String(Files.readAllBytes(CONFIG_PATH));
                    instance = GSON.fromJson(json, SceneSwitcherOptions.class);
                } else {
                    instance = new SceneSwitcherOptions();
                }
            } catch (IOException e) {
                instance = new SceneSwitcherOptions();
            }
        }
        return instance;
    }
}

