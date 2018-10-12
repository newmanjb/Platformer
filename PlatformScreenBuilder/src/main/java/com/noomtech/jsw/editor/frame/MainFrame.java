package com.noomtech.jsw.editor.frame;

import com.noomtech.jsw.common.utils.db.CassandraDBAdapter;
import com.noomtech.jsw.common.utils.db.DatabaseAdapter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


public class MainFrame extends JFrame {


    private DrawingPanel drawingPanel;
    private DatabaseAdapter dbAdapter;


    public MainFrame() throws Exception {

        setTitle("Collision Editor");

        dbAdapter = CassandraDBAdapter.getInstance();

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        screenSize.height = (int)Math.rint(screenSize.height * 0.92);
        getContentPane().setSize(screenSize);
        getContentPane().setMinimumSize(screenSize);
        getContentPane().setPreferredSize(screenSize);
        drawingPanel = new DrawingPanel(dbAdapter.loadEditorObjects());
        setLayout(new GridBagLayout());
        GridBagConstraints gbc0 = new GridBagConstraints();
        gbc0.gridx = 0;
        gbc0.gridy = 0;
        gbc0.fill = GridBagConstraints.BOTH;
        gbc0.weightx = 1.0;
        gbc0.weighty = 1.0;
        getContentPane().add(drawingPanel, gbc0);

        JPanel buttonPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc1 = new GridBagConstraints();
        gbc1.gridx = 0;
        gbc1.gridy = 0;

        JButton saveButton = new JButton("Save");
        saveButton.addActionListener(new SaveActionListener());
        buttonPanel.add(saveButton, gbc1);

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
                dbAdapter.save(drawingPanel);
            }
            catch(Exception ex)  {
                System.out.println("Save did not run properly");
                ex.printStackTrace();
            }
        }
    }
}
