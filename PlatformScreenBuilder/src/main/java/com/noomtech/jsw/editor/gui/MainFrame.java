package com.noomtech.jsw.editor.gui;

import com.noomtech.jsw.common.utils.CommonUtils;
import com.noomtech.jsw.common.utils.db.DatabaseAdapter;
import com.noomtech.jsw.common.utils.db.MongoDBAdapter;
import com.noomtech.jsw.editor.building_blocks.RootObject;
import com.noomtech.jsw.editor.gui.userinput_processing.MouseMovementHandler;

import javax.swing.*;
import java.awt.*;
import java.util.List;


public class MainFrame extends JFrame {


    private final DatabaseAdapter dbAdapter = MongoDBAdapter.getInstance();
    private MouseMovementHandler controller;


    public MainFrame() {

        setTitle("Collision Editor");

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        screenSize.height = (int)Math.rint(screenSize.height * 0.92);
        getContentPane().setSize(screenSize);
        getContentPane().setMinimumSize(screenSize);
        getContentPane().setPreferredSize(screenSize);

        setLayout(new GridBagLayout());

        JPanel buttonPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbcButtonPanel = new GridBagConstraints();
        gbcButtonPanel.gridx = 0;
        gbcButtonPanel.gridy = 0;
        gbcButtonPanel.insets = new Insets(0,0,0, 10);
        JButton saveButton = new JButton("Save");
        buttonPanel.add(saveButton, gbcButtonPanel);
        gbcButtonPanel.gridx++;
        JButton refreshButton = new JButton("Refresh");
        buttonPanel.add(refreshButton, gbcButtonPanel);
        gbcButtonPanel.gridx++;
        JButton previousLevel = new JButton("Prev Level");
        buttonPanel.add(previousLevel, gbcButtonPanel);
        gbcButtonPanel.gridx++;
        JLabel levelLabel = new JLabel(Integer.toString(CommonUtils.getCurrentLevel()));
        buttonPanel.add(levelLabel, gbcButtonPanel);
        gbcButtonPanel.gridx++;
        JButton nextLevel = new JButton("Next Level");
        buttonPanel.add(nextLevel, gbcButtonPanel);

        GridBagConstraints gbcMainPanel = new GridBagConstraints();
        gbcMainPanel.gridx = 0;
        gbcMainPanel.gridy = 0;
        gbcMainPanel.fill = GridBagConstraints.BOTH;
        gbcMainPanel.weightx = 1.0;
        gbcMainPanel.weighty = 1.0;

        JPanel[] drawingPanel = new JPanel[]{buildPanelForCurrentLevel()};
        getContentPane().add(drawingPanel[0], gbcMainPanel);
        GridBagConstraints gbcAddButtonPanel = new GridBagConstraints();
        gbcAddButtonPanel.gridx = 0;
        gbcAddButtonPanel.gridy = 1;
        gbcAddButtonPanel.fill = GridBagConstraints.BOTH;
        getContentPane().add(buttonPanel, gbcAddButtonPanel);

        setLocation(new Point(0,0));

        pack();

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        refreshButton.addActionListener(ae -> {
            controller.refreshAllImages();
        });
        saveButton.addActionListener(ae -> {
            try {
                dbAdapter.save(controller);
                controller.clearUpdates();
            }
            catch(Exception ex)  {
                System.out.println("Save did not run properly");
                ex.printStackTrace();
            }
        });
        nextLevel.addActionListener(ae -> {
            CommonUtils.setCurrentLevel(CommonUtils.getCurrentLevel() + 1);
            getContentPane().remove(drawingPanel[0]);
            drawingPanel[0] = buildPanelForCurrentLevel();
            getContentPane().add(drawingPanel[0], gbcMainPanel);
            levelLabel.setText(Integer.toString(CommonUtils.getCurrentLevel()));
            pack();
        });
        previousLevel.addActionListener(ae -> {
            CommonUtils.setCurrentLevel(CommonUtils.getCurrentLevel() - 1);
            getContentPane().remove(drawingPanel[0]);
            drawingPanel[0] = buildPanelForCurrentLevel();
            getContentPane().add(drawingPanel[0], gbcMainPanel);
            levelLabel.setText(Integer.toString(CommonUtils.getCurrentLevel()));
            pack();
        });

        setVisible(true);
    }

    private JPanel buildPanelForCurrentLevel() {

        try {
            List<RootObject> data = dbAdapter.loadEditorObjectsForLevel(CommonUtils.getCurrentLevel());
            DrawingPanel view = new DrawingPanel(data);
            controller = new MouseMovementHandler(view, data);
            view.addMouseListener(controller);
            view.addMouseMotionListener(controller);
            return view;
        }
        catch(Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
        return null;
    }
}
