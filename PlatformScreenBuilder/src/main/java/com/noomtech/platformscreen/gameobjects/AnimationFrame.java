package com.noomtech.platformscreen.gameobjects;

import java.awt.Rectangle;
import java.awt.Graphics;


/**
 * Draws a frame for an animation
 * @see AnimatedGameObject
 * @author Joshua Newman
 */
public interface AnimationFrame {

    void draw(Graphics g, Rectangle collisionArea);
}
