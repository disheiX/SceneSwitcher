package me.disheiX.switcher.gui;

import me.disheiX.switcher.SceneSwitcherOptions;
import me.disheiX.switcher.state.ObsState;
import xyz.duncanruns.jingle.Jingle;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.util.function.Predicate;

public class ObsStateElement {
    public JLabel obsStateIdLabel;
    public JTextField dimensionsField;
    public JTextField sceneNameField;
    public JTextField toggledSourcesField;
    public JButton removeButton;

    public ObsState obsState;

    ObsStateElement(ObsState obsState) {
        this.obsStateIdLabel = new JLabel(obsState.getName() + ":");
        this.dimensionsField = new JTextField(obsState.getDimensions());
        this.sceneNameField = new JTextField(obsState.getActiveScene());
        this.toggledSourcesField = new JTextField(String.join(", ", obsState.getToggledSources()));
        this.removeButton = new JButton("x");
        this.removeButton.setMargin(new Insets(0, 1, 0, 1));
        this.removeButton.setForeground(Color.RED);

        this.obsState = obsState;
    }

    public void build(SceneSwitcherGUI gui) {
        gui.getScenesPanel().add(this.obsStateIdLabel, SceneSwitcherGUI.gbc);

        this.addNewChangeListener(this.sceneNameField, s -> !s.trim().isEmpty(), "setActiveScene");
        gui.getScenesPanel().add(this.sceneNameField, SceneSwitcherGUI.gbc);

        this.addNewChangeListener(this.toggledSourcesField, ObsState::isValidSourcesListString, "setToggledSources");
        gui.getScenesPanel().add(this.toggledSourcesField, SceneSwitcherGUI.gbc);

        if (!this.obsState.getName().equals("Playing")) {
            if (!this.obsState.getName().equals("Walling")) {
                this.addNewChangeListener(this.dimensionsField, ObsState::isValidDimensionsString, "setDimensions");
                gui.getScenesPanel().add(this.dimensionsField, SceneSwitcherGUI.gbc);
            }

            SceneSwitcherGUI.gbc.gridx = 4;
            this.addRemoveActionListener(gui);
            gui.getScenesPanel().add(this.removeButton, SceneSwitcherGUI.gbc);
            SceneSwitcherGUI.gbc.gridx = GridBagConstraints.RELATIVE;
        }

        SceneSwitcherGUI.gbc.gridy++;

        this.setEnabled(SceneSwitcherOptions.getInstance().enabled);
    }

    private void setEnabled(boolean enable) {
        this.dimensionsField.setEnabled(enable);
        this.sceneNameField.setEnabled(enable);
        this.toggledSourcesField.setEnabled(enable);
        this.removeButton.setEnabled(enable);
    }

    protected void addNewChangeListener(JTextField textField, Predicate<String> validator, String stateMethod) {
        textField.getDocument().addDocumentListener((ChangeListener) e -> {
            textField.setForeground(validator.test(textField.getText()) ? null : Color.RED);
            if (validator.test(textField.getText())) {
                try {
                    ObsState.class.getDeclaredMethod(stateMethod, String.class).invoke(this.obsState, textField.getText());
                    SceneSwitcherOptions.save();
                } catch (Throwable ex) {
                    Jingle.logError("(SceneSwitcher) Failed to invoke provided method:", ex);
                }
            }
        });
    }

    protected void addRemoveActionListener(SceneSwitcherGUI gui) {
        this.removeButton.addActionListener(e -> {
            int choice = JOptionPane.showConfirmDialog(null,
                    "Are you sure you want to remove '" + this.obsState.getName() + "'?",
                    "Remove " + this.obsState.getName() + "?", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (choice == JOptionPane.YES_OPTION) {
                SceneSwitcherOptions.getInstance().obsStates.remove(obsState);
                SceneSwitcherOptions.save();
                gui.updateScenesPanel();
            }
        });
    }

    protected static class Labels extends ObsStateElement {
        private static final JButton NAME_BUTTON = new JButton("State Name:");
        private static final JButton ACTIVE_SCENE_BUTTON = new JButton("Active Scene:");
        private static final JButton TOGGLED_SOURCES_BUTTON = new JButton("Toggled Sources:");
        private static final JButton DIMENSIONS_BUTTON = new JButton("Game Size:");

        static {
            NAME_BUTTON.setBackground(Color.DARK_GRAY);
            ACTIVE_SCENE_BUTTON.setBackground(Color.DARK_GRAY);
            TOGGLED_SOURCES_BUTTON.setBackground(Color.DARK_GRAY);
            DIMENSIONS_BUTTON.setBackground(Color.DARK_GRAY);

            NAME_BUTTON.addActionListener(e -> JOptionPane.showMessageDialog(null, NAME_BUTTON.getText() + "\n" +
                            "This is the name of the state.\n" +
                            "While states can be named almost anything, the names 'Playing' and 'Walling' are reserved for OBS/game states predefined by Jingle.\n" +
                            "'Playing' is the default state and has no dimensions and cannot be removed.\n" +
                            "'Walling' will be activated if the current game state is 'Wall' and the game is not resized. This state can be removed.",
                    NAME_BUTTON.getText() + " Info", JOptionPane.INFORMATION_MESSAGE));
            ACTIVE_SCENE_BUTTON.addActionListener(e -> JOptionPane.showMessageDialog(null, ACTIVE_SCENE_BUTTON.getText() + "\n" +
                            "This is the OBS scene that will be active while this state is active.\n" +
                            "The format is the name of the scene as it is written in OBS.\n" +
                            "eg. The Jingle default: \"Playing\"",
                    ACTIVE_SCENE_BUTTON.getText() + " Info", JOptionPane.INFORMATION_MESSAGE));
            TOGGLED_SOURCES_BUTTON.addActionListener(e -> JOptionPane.showMessageDialog(null, TOGGLED_SOURCES_BUTTON.getText() + "\n" +
                            "This is a list of OBS scene items that will be toggled to 'enabled' while the state is active, and 'disabled' when the state is deactivated.\n" +
                            "The format is a comma separated list including, first the name of the scene that the desired source is located, then a colon (':'), then the name of the source as it is written in OBS.\n" +
                            "eg. The 'calculator' source in the 'Overlays' scene and the 'Mag' source in the 'Projectors' scene: \"Overlays:calculator, Projectors:Mag\"",
                    TOGGLED_SOURCES_BUTTON.getText() + " Info", JOptionPane.INFORMATION_MESSAGE));
            DIMENSIONS_BUTTON.addActionListener(e -> JOptionPane.showMessageDialog(null, DIMENSIONS_BUTTON.getText() + "\n" +
                            "This is the window dimensions that, if your active Minecraft game window matches, will be cause this state to be activated.\n" +
                            "If you use an ID that the default Resizing script uses, as a new state name, this field will be automatically filled with the existing associated dimensions.\n" +
                            "The format is the same as the included default Resizing script, first the width, then an 'x', then the height.\n" +
                            "eg. The default Eye Zoom dimensions: \"384x16384\"",
                    DIMENSIONS_BUTTON.getText() + " Info", JOptionPane.INFORMATION_MESSAGE));
        }

        protected Labels() {
            super(new ObsState("label", "0x0", "", ""));
        }

        @Override
        public void build(SceneSwitcherGUI gui) {
            SceneSwitcherGUI.gbc.fill = GridBagConstraints.NONE;
            gui.getScenesPanel().add(NAME_BUTTON, SceneSwitcherGUI.gbc);
            gui.getScenesPanel().add(ACTIVE_SCENE_BUTTON, SceneSwitcherGUI.gbc);
            gui.getScenesPanel().add(TOGGLED_SOURCES_BUTTON, SceneSwitcherGUI.gbc);
            gui.getScenesPanel().add(DIMENSIONS_BUTTON, SceneSwitcherGUI.gbc);
            SceneSwitcherGUI.gbc.fill = GridBagConstraints.HORIZONTAL;
            SceneSwitcherGUI.gbc.gridy++;
        }
    }

    private interface ChangeListener extends DocumentListener {
        void update(DocumentEvent e);

        @Override
        default void insertUpdate(DocumentEvent e) {
            update(e);
        }
        @Override
        default void removeUpdate(DocumentEvent e) {
            update(e);
        }
        @Override
        default void changedUpdate(DocumentEvent e) {
            update(e);
        }
    }
}
