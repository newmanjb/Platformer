package com.noomtech.jsw.game.movement;

import com.noomtech.jsw.game.gameobjects.GameObject;

import java.awt.*;

public class CollisionAreaToGameObject {

    private Rectangle collisionArea;
    private GameObject gameObject;

    public CollisionAreaToGameObject(Rectangle collisionArea, GameObject gameObject) {
        this.collisionArea = collisionArea;
        this.gameObject = gameObject;
    }

    public Rectangle getCollisionArea() {
        return collisionArea;
    }

    public GameObject getGameObject() {
        return gameObject;
    }
}
