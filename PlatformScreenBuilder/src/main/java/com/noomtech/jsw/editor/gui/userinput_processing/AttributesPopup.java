package com.noomtech.jsw.editor.gui.userinput_processing;

import com.noomtech.jsw.editor.building_blocks.RootObject;
import com.noomtech.jsw.editor.utils.EditorUtils;
import com.noomtech.jsw.game.gameobjects.GameObject;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

class AttributesPopup extends JDialog {
    private MouseMovementHandler PARENT;
    private static final String NEWLINE = System.getProperty("line.separator");
    AttributesPopup(final RootObject rootObject, MouseMovementHandler parent) {
        super((JFrame)null, "Attributes for game object " + rootObject.getId(), true);
        this.PARENT = parent;
        setLayout(new BorderLayout());
        JComboBox<String> classBox = new JComboBox(EditorUtils.SELECTABLE_GAME_OBJECTS);
        JTextArea textArea = new JTextArea();
        JButton saveButton = new JButton("Save");
        saveButton.addActionListener(ae -> {
            String attributeString = textArea.getText();
            Map<String,String> attributes = new HashMap<>();
            if(attributeString != null && !attributeString.equals("")) {
                String[] keyValPairs = attributeString.split("[" + NEWLINE + "]");
                for(String keyValPair : keyValPairs) {
                    //Check this in case the user has hit return at the end of typing the last property,
                    //meaning that the above split call has resulted in the last entry being an empty string
                    if(keyValPair.length() > 0) {
                        String[] keyValArray = keyValPair.split("=");
                        attributes.put(keyValArray[0], keyValArray[1]);
                    }
                }
            }
            //The type of game object is represented as the full name of the underlying class so as it can be instantiated
            //using reflection when it's loaded
            String theClass = EditorUtils.SELECTABLE_GAME_OBJECT_PACKAGE + "." + classBox.getSelectedItem();
            GameObject newGameObject;
            GameObject oldGameObject = rootObject.getGameObject();
            try {
                newGameObject = (GameObject)Class.forName(theClass).getConstructor(Rectangle.class, Map.class, long.class).newInstance(
                        rootObject.getArea(), attributes, oldGameObject.getId());
                newGameObject.setCollisionAreas(oldGameObject.getCollisionAreas());
                newGameObject.setLocation(oldGameObject.getX(), oldGameObject.getY());
            }
            catch(Exception c) {
                throw new IllegalArgumentException("Couldn't create game object", c);
            }

            rootObject.setGameObject(newGameObject);
            PARENT.record_rootObjectUpdated(rootObject);

            setVisible(false);
            dispose();
        });

        GameObject gameObject = rootObject.getGameObject();
        if(gameObject != null) {
            Map<String, String> attributes = gameObject.getAttributes();
            if (!attributes.isEmpty()) {
                StringBuilder sb = new StringBuilder();
                for (Map.Entry<String, String> entry : attributes.entrySet()) {
                    sb.append(entry.getKey() + "=" + entry.getValue() + NEWLINE);
                }
                textArea.setText(sb.substring(0, sb.length() - NEWLINE.length()));
            }

            classBox.setSelectedItem(gameObject.getClass().getSimpleName());
        }
        else {
            classBox.setSelectedItem(classBox.getItemAt(0));
        }

        add(classBox, BorderLayout.NORTH);
        add(textArea, BorderLayout.CENTER);
        add(saveButton, BorderLayout.SOUTH);
        Dimension size = new Dimension(350,350);
        setPreferredSize(size);
        setMinimumSize(size);
        setMaximumSize(size);

        pack();
    }
}

