package me.disheiX.switcher.gui;

import javax.swing.*;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import me.disheiX.switcher.state.ObsState;
import org.apache.logging.log4j.Level;

import me.disheiX.switcher.SceneSwitcherOptions;
import me.disheiX.switcher.SceneSwitcher;
import xyz.duncanruns.jingle.Jingle;
import java.awt.*;
import xyz.duncanruns.jingle.script.CustomizableManager;
import xyz.duncanruns.jingle.util.ExceptionUtil;
import xyz.duncanruns.jingle.util.KeyboardUtil;

import java.util.function.Function;

public class SceneSwitcherGUI extends JPanel {
    public static final GridBagConstraints gbc = new GridBagConstraints();
    public static final GridConstraints gc = new GridConstraints();

    private final JPanel mainPanel;
    private final JPanel enablePanel;
    private final JPanel scenesPanel;
    private final JPanel copyPathPanel;

    public SceneSwitcherGUI() {
        this.mainPanel = new JPanel(new GridLayoutManager(3, 1));
        this.enablePanel = new JPanel(new GridBagLayout());
        this.scenesPanel = new JPanel(new GridBagLayout());
        this.copyPathPanel = new JPanel(new GridBagLayout());

        gbc.insets = new Insets(2, 2, 2, 2);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        this.createEnableCheckBox();
        this.createScenesPanel();
        this.createCopyPathButton();

        this.updateScenesPanel();

        this.add(this.mainPanel);

        gc.setRow(0);
        this.mainPanel.add(this.enablePanel, gc);
        gc.setRow(1);
        this.mainPanel.add(this.scenesPanel, gc);
        gc.setRow(2);
        this.mainPanel.add(this.copyPathPanel, gc);

        this.setVisible(true);
    }

    private void createEnableCheckBox() {
        SceneSwitcherOptions options = SceneSwitcherOptions.getInstance();

        JLabel enabledLabel = new JLabel("Enable Scene Switcher?");
        JCheckBox enabledCheckBox = new JCheckBox();

        enabledCheckBox.setSelected(options.enabled);
        enabledCheckBox.addActionListener(e -> {
            options.enabled = enabledCheckBox.isSelected();
            SceneSwitcherOptions.save();
            this.updateScenesPanel();
            Jingle.log(Level.INFO, options.enabled ? "(SceneSwitcher) Scene Switcher is now active." : "(SceneSwitcher) Scene Switcher is no longer active.");
        });

        this.enablePanel.add(enabledLabel, gbc);
        this.enablePanel.add(enabledCheckBox, gbc);
    }

    private void createScenesPanel() {
        this.scenesPanel.setBorder(BorderFactory.createTitledBorder("OBS States"));
    }

    private void createCopyPathButton() {
        JButton copyPathButton = new JButton("Copy script path to clipboard");

        copyPathButton.addActionListener(e -> {
            try {
                KeyboardUtil.copyToClipboard(SceneSwitcher.getLuaScriptPath().toAbsolutePath().toString());
                Jingle.log(Level.INFO, "(SceneSwitcher) Copied lua script path to clipboard");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this.mainPanel, "(SceneSwitcher) Failed to copy to clipboard: " + ExceptionUtil.toDetailedString(ex));
            }
        });

        this.copyPathPanel.add(copyPathButton, gbc);
    }

    public void updateScenesPanel() {
        SceneSwitcherOptions options = SceneSwitcherOptions.getInstance();

        this.scenesPanel.removeAll();
        gbc.gridy = 0;

        new ObsStateElement.Labels().build(this);

        for (ObsState obsState: options.obsStates) {
            new ObsStateElement(obsState).build(this);
        }

        this.addAddStateButton();

        this.mainPanel.revalidate();
        this.mainPanel.repaint();
    }

    private void addAddStateButton() {
        SceneSwitcherOptions options = SceneSwitcherOptions.getInstance();
        JButton addSceneButton = new JButton("Add State");

        addSceneButton.addActionListener(e -> {
            Function<String, Object> askNameFunc = s -> JOptionPane.showInputDialog(
                    this.mainPanel,
                    s + "Enter new OBS State Label:",
                    "Scene Switcher: New OBS State",
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    null,
                    ""
            );

            Object nameAnsObj = askNameFunc.apply("");
            if (nameAnsObj == null) return;
            while (SceneSwitcherOptions.matchingExistingName(nameAnsObj.toString()) && nameAnsObj.toString().trim().isEmpty()) {
                nameAnsObj = askNameFunc.apply("Invalid input!\n");
            }

            Object dimensionsAnsObj = "0x0";

            if (!nameAnsObj.toString().equals("Walling")) {
                Object initialSelection = CustomizableManager.get("Resizing", nameAnsObj.toString());

                Function<String, Object> askDimensionsFunc = s -> JOptionPane.showInputDialog(
                        this.mainPanel,
                        s + "Enter the width and height resize dimensions for new OBS State separated by an 'x' (eg. \"250x1080\"):",
                        "Scene Switcher: New OBS State",
                        JOptionPane.QUESTION_MESSAGE,
                        null,
                        null,
                        initialSelection == null ? "250x1080" : initialSelection.toString()
                );

                dimensionsAnsObj = askDimensionsFunc.apply("");
                if (dimensionsAnsObj == null) return;
                while (!ObsState.isValidDimensionsString(dimensionsAnsObj.toString())) {
                    dimensionsAnsObj = askDimensionsFunc.apply(dimensionsAnsObj.toString().trim().isEmpty() ? "Invalid input!\n" : "Those resize dimensions are already being used!\n");
                }
            }

            options.obsStates.add(new ObsState(nameAnsObj.toString(), dimensionsAnsObj.toString(), "Playing", ""));
            SceneSwitcherOptions.save();
            this.updateScenesPanel();
        });
        this.scenesPanel.add(addSceneButton, gbc);
        addSceneButton.setEnabled(options.enabled);
    }

    public JPanel getScenesPanel() {
        return this.scenesPanel;
    }
}
