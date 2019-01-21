package com.noomtech.jsw.editor.gui;


/**
 * Represents anything that can be save directly to the DB in its own collection.
 */
public interface Saveable {


    String getCollectionName();

    long getId();
}
