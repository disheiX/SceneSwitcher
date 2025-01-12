package me.disheiX.switcher.gui;

import javax.swing.*;

import org.apache.logging.log4j.Level;

import me.disheiX.switcher.SceneSwitcherOptions;
import me.disheiX.switcher.SceneSwitcher;
import xyz.duncanruns.jingle.Jingle;
import xyz.duncanruns.jingle.hotkey.Hotkey;
import xyz.duncanruns.jingle.plugin.PluginHotkeys;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import me.disheiX.switcher.util.HotkeyUtil;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;

public class SceneSwitcherGUI extends JPanel {
    private static SceneSwitcherGUI instance = null;
    private JCheckBox enabledCheckBox;
    private JTextField magField, plannarAbuseField, thinField, playingField;
    private JButton keyButton, addSceneButton;
    private JPanel scenesPanel;
    private Map<String, JTextField> customSceneFields = new HashMap<>();
    private boolean closed = false;
    private CustomSceneCounter customCounter = new CustomSceneCounter(0);

    public SceneSwitcherGUI() {
        SceneSwitcherOptions options = SceneSwitcherOptions.getInstance();
        setUpWindow();
        initializeFields(options);
        addListeners(options);
        setInitialStates(options);
        this.revalidate();
        this.setMinimumSize(new Dimension(300, 200));
        this.setVisible(true);
    }

    private void setUpWindow() {
        this.setLayout(new GridBagLayout());
        enabledCheckBox = new JCheckBox();
        playingField = new JTextField(15);
        magField = new JTextField(15);
        plannarAbuseField = new JTextField(15);
        thinField = new JTextField(15);
        keyButton = new JButton("Hotkey");
        scenesPanel = new JPanel(new GridBagLayout());
        scenesPanel.setBorder(BorderFactory.createTitledBorder("Scene Names"));
        JPanel customScenesPanel = new JPanel(new GridBagLayout());
        addSceneButton = new JButton("Add Custom Scene");

        GridBagConstraints gbc = createGbc();
        int y = 0;
        addLabelAndComponent("Enabled", enabledCheckBox, gbc, y++);
        addLabelAndComponent("Playing Scene", playingField, gbc, y++);
        addPanel(scenesPanel, gbc, y++);
        addPanel(customScenesPanel, gbc, y++);
        addSceneFields(scenesPanel, customScenesPanel);
        addSceneButtonListener(customScenesPanel);

        JButton copyPathButton = new JButton("Copy script path to clipboard");
        copyPathButton.addActionListener(e -> {
            String path = SceneSwitcher.getLuaScriptPath().toAbsolutePath().toString();
            StringSelection selection = new StringSelection(path);
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, selection);
            Jingle.log(Level.INFO, "(SceneSwitcher) Copied lua script path to clipboard");
        });

        // Add button near the bottom
        gbc.gridy = 100; // High value to ensure it's at the bottom
        gbc.gridx = 0;
        gbc.gridwidth = 4;
        this.add(copyPathButton, gbc);
    }

    private void initializeFields(SceneSwitcherOptions options) {
        enabledCheckBox.setSelected(options.enabled);
        playingField.setText(options.playing_scene.name);
        magField.setText(options.mag_scene.name);
        plannarAbuseField.setText(options.plannar_abuse_scene.name);
        thinField.setText(options.thin_scene.name);
        
        SceneSwitcher.updatePlayingScene(options.playing_scene.name);
    }

    private void addListeners(SceneSwitcherOptions options) {
        enabledCheckBox.addActionListener(e -> toggleFields(checkBoxEnabled()));
        KeyAdapter saveKeyListener = new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                save();
                if (e.getSource() == playingField) {
                    SceneSwitcher.updatePlayingScene(playingField.getText());
                }
            }
        };
        playingField.addKeyListener(saveKeyListener);
        magField.addKeyListener(saveKeyListener);
        plannarAbuseField.addKeyListener(saveKeyListener);
        thinField.addKeyListener(saveKeyListener);
        keyButton.addActionListener(a -> setHotkey());
    }

    private void setInitialStates(SceneSwitcherOptions options) {
        enabledCheckBox.setSelected(options.enabled);
        toggleFields(checkBoxEnabled());
    }

    private void toggleFields(boolean enabled) {
        playingField.setEnabled(enabled);
        magField.setEnabled(enabled);
        plannarAbuseField.setEnabled(enabled);
        thinField.setEnabled(enabled);
        keyButton.setEnabled(enabled);
        addSceneButton.setEnabled(enabled);
        customSceneFields.values().forEach(field -> field.setEnabled(enabled));
        save();
    }

    private void setHotkey() {
        synchronized (this) {
            keyButton.setText("...");
            keyButton.setEnabled(false);
            Hotkey.onNextHotkey(() -> true, hotkey -> {
                keyButton.setText(hotkey.toString());
                keyButton.setEnabled(checkBoxEnabled());
                PluginHotkeys.addHotkeyAction("hotkey trigger created", () -> SceneSwitcher.switchToPlannarAbuse(plannarAbuseField.getText()));
            });
        }
    }

    private void addSceneFields(JPanel scenesPanel, JPanel customScenesPanel) {
        GridBagConstraints sgbc = createGbc();
        int sy = 0;
        addLabelAndComponent("Mag Scene:", magField, sgbc, sy++, scenesPanel);
        addLabelAndComponent("Wide Scene:", plannarAbuseField, sgbc, sy++, scenesPanel);
        addLabelAndComponent("Thin Scene:", thinField, sgbc, sy++, scenesPanel);
        addSeparator(sgbc, sy++, scenesPanel);
        addPanel(customScenesPanel, sgbc, sy++, scenesPanel);
        SceneSwitcherOptions options = SceneSwitcherOptions.getInstance();
        CustomSceneCounter customCounter = new CustomSceneCounter(0);
        options.custom_scenes.forEach((key, value) -> addCustomSceneField(key, value.name, customCounter.value++, customScenesPanel));
        addPanel(addSceneButton, sgbc, sy++, scenesPanel);
    }

    private void addSceneButtonListener(JPanel customScenesPanel) {
        addSceneButton.addActionListener(e -> {
            String sceneName = JOptionPane.showInputDialog(this, "Enter Custom Scene Label:");
            if (sceneName != null && !sceneName.trim().isEmpty()) {
                customScenesPanel.remove(addSceneButton);
                addCustomSceneField(sceneName, "", customCounter.value++, customScenesPanel);
                addPanel(addSceneButton, createGbc(), customCounter.value, customScenesPanel);
                save();
                customScenesPanel.revalidate();
                customScenesPanel.repaint();
            }
        });
    }

    private void addCustomSceneField(String sceneId, String sceneName, int y, JPanel panel) {
        JLabel sceneIdLabel = new JLabel(sceneId + ":");
        JTextField sceneNameField = new JTextField(sceneName, 15);
        sceneNameField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                save();
            }
        });
        JButton removeButton = createRemoveButton(sceneId, panel, sceneIdLabel, sceneNameField);
        PluginHotkeys.addHotkeyAction("Scene Switcher - " + sceneId, () -> SceneSwitcher.switchToCustom(sceneNameField.getText()));
        Jingle.log(Level.INFO, "(SceneSwitcher) Added hotkey action for scene ID \"" + sceneId + "\"");
        customSceneFields.put(sceneId, sceneNameField);
        addComponentsToPanel(panel, sceneIdLabel, sceneNameField, removeButton, y);
        sceneNameField.setEnabled(checkBoxEnabled());
    }

    private JButton createRemoveButton(String sceneId, JPanel panel, JLabel sceneIdLabel, JTextField sceneNameField) {
        JButton removeButton = new JButton("×");
        removeButton.setMargin(new Insets(0, 4, 0, 4));
        removeButton.addActionListener(e -> {
            removeCustomScene(sceneId);
            panel.remove(sceneIdLabel);
            panel.remove(sceneNameField);
            panel.remove(removeButton);
            save();
            panel.revalidate();
            panel.repaint();
        });
        return removeButton;
    }

    private void addComponentsToPanel(JPanel panel, JLabel sceneIdLabel, JTextField sceneNameField, JButton removeButton, int y) {
        GridBagConstraints gbc = createGbc();
        addComponent(panel, sceneIdLabel, gbc, 0, y, 1);
        addComponent(panel, sceneNameField, gbc, 1, y, 2);
        addComponent(panel, removeButton, gbc, 3, y, 1);
    }

    private void removeCustomScene(String sceneId) {
        HotkeyUtil.removeHotkeyAction("Scene Switcher - " + sceneId);
        Jingle.log(Level.INFO, "(SceneSwitcher) Removed hotkey action for scene ID\"" + sceneId + "\"");
        customSceneFields.remove(sceneId);
        save();
    }

    public static SceneSwitcherGUI open(Point initialLocation) {
        if (instance == null || instance.isClosed()) {
            instance = new SceneSwitcherGUI();
            if (initialLocation != null) {
                instance.setLocation(initialLocation);
            }
        } else {
            instance.requestFocus();
        }
        return instance;
    }

    private void save() {
        SceneSwitcherOptions options = SceneSwitcherOptions.getInstance();
        options.enabled = checkBoxEnabled();
        options.playing_scene = new SceneSwitcherOptions.SceneData(playingField.getText());
        options.mag_scene.name = magField.getText();
        options.plannar_abuse_scene.name = plannarAbuseField.getText();
        options.thin_scene.name = thinField.getText();
        options.custom_scenes.clear();
        customSceneFields.forEach((key, value) -> options.custom_scenes.put(key, new SceneSwitcherOptions.SceneData(value.getText())));
        try {
            SceneSwitcherOptions.save();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    private boolean checkBoxEnabled() {
        return enabledCheckBox.isSelected();
    }

    public boolean isClosed() {
        return closed;
    }

    private GridBagConstraints createGbc() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        return gbc;
    }

    private void addLabelAndComponent(String labelText, JComponent component, GridBagConstraints gbc, int y) {
        addComponent(this, new JLabel(labelText), gbc, 0, y, 1);
        addComponent(this, component, gbc, 1, y, 3);
    }

    private void addLabelAndComponent(String labelText, JComponent component, GridBagConstraints gbc, int y, JPanel panel) {
        addComponent(panel, new JLabel(labelText), gbc, 0, y, 1);
        addComponent(panel, component, gbc, 1, y, 3);
    }

    private void addComponent(JPanel panel, JComponent component, GridBagConstraints gbc, int x, int y, int width) {
        gbc.gridx = x;
        gbc.gridy = y;
        gbc.gridwidth = width;
        panel.add(component, gbc);
    }

    private void addPanel(JPanel panel, GridBagConstraints gbc, int y) {
        gbc.gridx = 0;
        gbc.gridy = y;
        gbc.gridwidth = 4;
        this.add(panel, gbc);
    }

    private void addPanel(JComponent component, GridBagConstraints gbc, int y, JPanel panel) {
        gbc.gridx = 0;
        gbc.gridy = y;
        gbc.gridwidth = 4;
        panel.add(component, gbc);
    }

    private void addSeparator(GridBagConstraints gbc, int y, JPanel panel) {
        addComponent(panel, new JSeparator(), gbc, 0, y, 4);
    }

    private class CustomSceneCounter {
        int value;
        CustomSceneCounter(int initial) { value = initial; }
    }
}
