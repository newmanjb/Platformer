package com.noomtech.jsw.game.gameobjects;

import java.awt.Rectangle;
import java.awt.Graphics;


/**
 * Draws a frame for an animation
 * @see MovingGameObject
 * @author Joshua Newman
 */
//@todo draw images up-front to save processing time
public interface AnimationFrame {

    void draw(Graphics g, Rectangle collisionArea);
}
