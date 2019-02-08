package com.noomtech.jsw.game.gameobjects;

import java.awt.Rectangle;
import java.awt.Graphics;


/**
 * Draws a frame for a game object's state
 * @see GameObject
 * @author Joshua Newman
 */
//@todo draw images up-front to save processing time
public interface GameObjectStateFrame {

    void draw(Graphics g, Rectangle area);
}
