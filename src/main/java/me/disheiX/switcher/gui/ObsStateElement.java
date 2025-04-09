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

    public SceneSwitcherGUI panel;
    public ObsState obsState;

    ObsStateElement(SceneSwitcherGUI panel, ObsState obsState) {
        this.obsStateIdLabel = new JLabel(obsState.getName() + ":");
        this.dimensionsField = new JTextField(obsState.getDimensions());
        this.sceneNameField = new JTextField(obsState.getActiveScene());
        this.toggledSourcesField = new JTextField(String.join(", ", obsState.getToggledSources()));
        this.removeButton = new JButton("x");
        this.removeButton.setMargin(new Insets(0, 1, 0, 1));
        this.removeButton.setForeground(Color.RED);

        this.panel = panel;
        this.obsState = obsState;
    }

    public void build() {
        this.addNewChangeListener(this.dimensionsField, ObsState::isValidDimensionsString, "setDimensions");
        this.addNewChangeListener(this.sceneNameField, s -> !s.trim().isEmpty(), "setActiveScene");
        this.addNewChangeListener(this.toggledSourcesField, ObsState::isValidSourcesListString, "setToggledSources");

        this.addRemoveActionListener();

        this.panel.getScenesPanel().add(this.obsStateIdLabel, SceneSwitcherGUI.gbc);
        this.panel.getScenesPanel().add(this.sceneNameField, SceneSwitcherGUI.gbc);
        this.panel.getScenesPanel().add(this.toggledSourcesField, SceneSwitcherGUI.gbc);
        this.panel.getScenesPanel().add(this.dimensionsField, SceneSwitcherGUI.gbc);
        this.panel.getScenesPanel().add(this.removeButton, SceneSwitcherGUI.gbc);
        SceneSwitcherGUI.gbc.gridy++;
    }

    public void setEnabled(boolean enable) {
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
                    this.obsState.getClass().getDeclaredMethod(stateMethod, String.class).invoke(this.obsState, textField.getText());
                    SceneSwitcherOptions.save();
                } catch (Throwable ex) {
                    Jingle.logError("(SceneSwitcher) Failed to invoke provided method:", ex);
                }
            }
        });
    }

    protected void addRemoveActionListener() {
        this.removeButton.addActionListener(e -> {
            int choice = JOptionPane.showConfirmDialog(null,
                    "Are you sure you want to remove '" + this.obsState.getName() + "'?",
                    "Remove " + this.obsState.getName() + "?", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (choice == JOptionPane.YES_OPTION) {
                SceneSwitcherOptions.getInstance().obsStates.remove(obsState);
                SceneSwitcherOptions.save();
                this.panel.updateScenesPanel();
            }
        });
    }

    protected static class Labels extends ObsStateElement {
        private static final JLabel NAME_LABEL = new JLabel("State Name:");
        private static final JButton ACTIVE_SCENE_BUTTON = new JButton("Active Scene:");
        private static final JButton TOGGLED_SOURCES_BUTTON = new JButton("Toggled Sources:");
        private static final JButton DIMENSIONS_BUTTON = new JButton("Game Size:");

        protected Labels(SceneSwitcherGUI panel) {
            super(panel, new ObsState("label", "0x0", "", ""));
        }

        @Override
        public void build() {
            ACTIVE_SCENE_BUTTON.setBackground(Color.DARK_GRAY);
            TOGGLED_SOURCES_BUTTON.setBackground(Color.DARK_GRAY);
            DIMENSIONS_BUTTON.setBackground(Color.DARK_GRAY);

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


            SceneSwitcherGUI.gbc.fill = GridBagConstraints.NONE;
            this.panel.getScenesPanel().add(NAME_LABEL, SceneSwitcherGUI.gbc);
            this.panel.getScenesPanel().add(ACTIVE_SCENE_BUTTON, SceneSwitcherGUI.gbc);
            this.panel.getScenesPanel().add(TOGGLED_SOURCES_BUTTON, SceneSwitcherGUI.gbc);
            this.panel.getScenesPanel().add(DIMENSIONS_BUTTON, SceneSwitcherGUI.gbc);
            SceneSwitcherGUI.gbc.fill = GridBagConstraints.HORIZONTAL;
            SceneSwitcherGUI.gbc.gridy++;
        }
    }

    protected static class DefaultState extends ObsStateElement {
        protected DefaultState(SceneSwitcherGUI panel, ObsState obsState) {
            super(panel, obsState);
        }

        @Override
        public void build() {
            this.addNewChangeListener(this.sceneNameField, s -> !s.trim().isEmpty(), "setActiveScene");
            this.addNewChangeListener(this.toggledSourcesField, ObsState::isValidSourcesListString, "setToggledSources");

            this.panel.getScenesPanel().add(this.obsStateIdLabel, SceneSwitcherGUI.gbc);
            this.panel.getScenesPanel().add(this.sceneNameField, SceneSwitcherGUI.gbc);
            this.panel.getScenesPanel().add(this.toggledSourcesField, SceneSwitcherGUI.gbc);
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
