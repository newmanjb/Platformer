package com.noomtech.jsw.editor.gui;

import com.noomtech.jsw.common.utils.db.DatabaseAdapter;
import com.noomtech.jsw.common.utils.db.MongoDBAdapter;
import com.noomtech.jsw.editor.building_blocks.RootObject;
import com.noomtech.jsw.editor.gui.userinput_processing.MouseMovementHandler;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;


public class MainFrame extends JFrame {


    private DatabaseAdapter dbAdapter;
    private MouseMovementHandler controller;


    public MainFrame() throws Exception {

        setTitle("Collision Editor");

        dbAdapter = MongoDBAdapter.getInstance();

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        screenSize.height = (int)Math.rint(screenSize.height * 0.92);
        getContentPane().setSize(screenSize);
        getContentPane().setMinimumSize(screenSize);
        getContentPane().setPreferredSize(screenSize);
        List<RootObject> data = dbAdapter.loadEditorObjects();
        DrawingPanel view = new DrawingPanel(data);
        controller = new MouseMovementHandler(view, data);
        view.addMouseListener(controller);
        view.addMouseMotionListener(controller);

        setLayout(new GridBagLayout());
        GridBagConstraints gbc0 = new GridBagConstraints();
        gbc0.gridx = 0;
        gbc0.gridy = 0;
        gbc0.fill = GridBagConstraints.BOTH;
        gbc0.weightx = 1.0;
        gbc0.weighty = 1.0;
        getContentPane().add(view, gbc0);

        JPanel buttonPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc1 = new GridBagConstraints();
        gbc1.gridx = 0;
        gbc1.gridy = 0;
        gbc1.insets = new Insets(0,0,0, 10);

        JButton saveButton = new JButton("Save");
        saveButton.addActionListener(new SaveActionListener());
        buttonPanel.add(saveButton, gbc1);

        gbc1.gridx++;
        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                controller.refresh();
            }
        });
        buttonPanel.add(refreshButton, gbc1);

        gbc0.gridy = 1;
        gbc0.fill = GridBagConstraints.NONE;
        gbc0.weightx = 0.0;
        gbc0.weighty = 0.0;
        getContentPane().add(buttonPanel, gbc0);

        setLocation(new Point(0,0));

        pack();

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setVisible(true);
    }

    private class SaveActionListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            try {
                dbAdapter.save(controller);
                controller.clearUpdates();
            }
            catch(Exception ex)  {
                System.out.println("Save did not run properly");
                ex.printStackTrace();
            }
        }
    }
}
