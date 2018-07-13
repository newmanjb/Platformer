package com.noomtech.platformscreen.gameobjects;

import com.noomtech.platformscreen.movement.PlayerMovementType;
import org.junit.Test;

import java.awt.*;
import java.lang.reflect.Field;

public class JSWTest {


    @Test
    public void testMovement() throws Exception {

        int numPixelsPerMovement = 10;
        JSW jsw = new JSW(new Rectangle(20,20,5,5));
        Field f1 = JSW.class.getDeclaredField("pixelsPerFrameChange");
        f1.setAccessible(true);
        f1.setInt(jsw, numPixelsPerMovement);

        checkVals(jsw, 0, false);

        jsw.setLocation((int)(jsw.getX() + numPixelsPerMovement * 0.1), jsw.getY());
        jsw.reactToMove(PlayerMovementType.WALK_RIGHT);

        checkVals(jsw, 0, false);

        jsw.setLocation((int)(jsw.getX() + numPixelsPerMovement * 0.8), jsw.getY());
        jsw.reactToMove(PlayerMovementType.WALK_RIGHT);

        checkVals(jsw, 0, false);

        jsw.setLocation(jsw.getX() + (int)(numPixelsPerMovement * 0.1), jsw.getY());
        jsw.reactToMove(PlayerMovementType.WALK_RIGHT);

        checkVals(jsw, 1, false);

        jsw.setLocation((int)(jsw.getX() + numPixelsPerMovement * 0.1), jsw.getY());
        jsw.reactToMove(PlayerMovementType.WALK_RIGHT);

        checkVals(jsw, 1, false);

        jsw.setLocation((int)(jsw.getX() + numPixelsPerMovement * 0.8), jsw.getY());
        jsw.reactToMove(PlayerMovementType.WALK_RIGHT);

        checkVals(jsw, 1, false);

        jsw.setLocation(jsw.getX() + (int)(numPixelsPerMovement * 0.1), jsw.getY());
        jsw.reactToMove(PlayerMovementType.WALK_RIGHT);

        checkVals(jsw, 2, false);

        jsw.setLocation((int)(jsw.getX() + numPixelsPerMovement * 0.1), jsw.getY());
        jsw.reactToMove(PlayerMovementType.WALK_RIGHT);

        checkVals(jsw, 2, false);

        jsw.setLocation((int)(jsw.getX() + numPixelsPerMovement * 0.8), jsw.getY());
        jsw.reactToMove(PlayerMovementType.WALK_RIGHT);

        checkVals(jsw, 2, false);

        jsw.setLocation(jsw.getX() + (int)(numPixelsPerMovement * 0.1), jsw.getY());
        jsw.reactToMove(PlayerMovementType.WALK_RIGHT);

        checkVals(jsw, 0, false);

        //Walk left
        jsw.setLocation((int)(jsw.getX() - numPixelsPerMovement * 0.1), jsw.getY());
        jsw.reactToMove(PlayerMovementType.WALK_LEFT);

        checkVals(jsw, 0, true);

        jsw.setLocation((int)(jsw.getX() - numPixelsPerMovement * 0.8), jsw.getY());
        jsw.reactToMove(PlayerMovementType.WALK_LEFT);

        checkVals(jsw, 0, true);

        jsw.setLocation(jsw.getX() - (int)(numPixelsPerMovement * 0.1), jsw.getY());
        jsw.reactToMove(PlayerMovementType.WALK_LEFT);

        checkVals(jsw, 1, true);

        jsw.setLocation((int)(jsw.getX() - numPixelsPerMovement * 0.1), jsw.getY());
        jsw.reactToMove(PlayerMovementType.WALK_LEFT);

        checkVals(jsw, 1, true);

        jsw.setLocation((int)(jsw.getX() - numPixelsPerMovement * 0.8), jsw.getY());
        jsw.reactToMove(PlayerMovementType.WALK_LEFT);

        checkVals(jsw, 1, true);

        jsw.setLocation(jsw.getX() - (int)(numPixelsPerMovement * 0.1), jsw.getY());
        jsw.reactToMove(PlayerMovementType.WALK_LEFT);

        checkVals(jsw, 2, true);

        jsw.setLocation((int)(jsw.getX() - numPixelsPerMovement * 0.1), jsw.getY());
        jsw.reactToMove(PlayerMovementType.WALK_LEFT);

        checkVals(jsw, 2, true);

        jsw.setLocation((int)(jsw.getX() - numPixelsPerMovement * 0.8), jsw.getY());
        jsw.reactToMove(PlayerMovementType.WALK_LEFT);

        checkVals(jsw, 2, true);

        jsw.setLocation(jsw.getX() - (int)(numPixelsPerMovement * 0.1), jsw.getY());
        jsw.reactToMove(PlayerMovementType.WALK_LEFT);

        checkVals(jsw, 0, true);
    }


    @Test
    public void testDither() throws Exception {

        int numPixelsPerMovement = 10;
        JSW jsw = new JSW(new Rectangle(20,20,5,5));
        Field f1 = JSW.class.getDeclaredField("pixelsPerFrameChange");
        f1.setAccessible(true);
        f1.setInt(jsw, numPixelsPerMovement);

        checkVals(jsw, 0, false);

        jsw.setLocation((int)(jsw.getX() + numPixelsPerMovement * 0.1), jsw.getY());
        jsw.reactToMove(PlayerMovementType.WALK_RIGHT);

        checkVals(jsw, 0, false);

        jsw.setLocation((int)(jsw.getX() - numPixelsPerMovement * 0.1), jsw.getY());
        jsw.reactToMove(PlayerMovementType.WALK_LEFT);

        checkVals(jsw, 0, true);

        jsw.setLocation(jsw.getX() + (int)(numPixelsPerMovement * 0.9), jsw.getY());
        jsw.reactToMove(PlayerMovementType.WALK_RIGHT);

        checkVals(jsw, 0, false);

        jsw.setLocation((int)(jsw.getX() - numPixelsPerMovement * 0.9), jsw.getY());
        jsw.reactToMove(PlayerMovementType.WALK_LEFT);

        checkVals(jsw, 0, true);
    }


    @Test
    public void testLargerMovementsThanPixelsPerMovement() throws Exception {

        int numPixelsPerMovement = 10;
        JSW jsw = new JSW(new Rectangle(20,20,5,5));
        Field f1 = JSW.class.getDeclaredField("pixelsPerFrameChange");
        f1.setAccessible(true);
        f1.setInt(jsw, numPixelsPerMovement);

        checkVals(jsw, 0, false);

        jsw.setLocation((int)(jsw.getX() + numPixelsPerMovement * 1.5), jsw.getY());
        jsw.reactToMove(PlayerMovementType.WALK_RIGHT);

        checkVals(jsw, 1, false);

        jsw.setLocation((int)(jsw.getX() + numPixelsPerMovement * 0.5), jsw.getY());
        jsw.reactToMove(PlayerMovementType.WALK_RIGHT);

        checkVals(jsw, 2, false);

        //Walk left
        jsw.setLocation((int)(jsw.getX() - numPixelsPerMovement * 1.5), jsw.getY());
        jsw.reactToMove(PlayerMovementType.WALK_LEFT);

        checkVals(jsw, 0, true);

        jsw.setLocation((int)(jsw.getX() - numPixelsPerMovement * 0.5), jsw.getY());
        jsw.reactToMove(PlayerMovementType.WALK_LEFT);

        checkVals(jsw, 1, true);
    }

    private void checkVals(JSW jsw, int expectedFrameIdx, boolean isLeft) throws NoSuchFieldException, IllegalAccessException {
        Field f1 = JSW.class.getDeclaredField("leftFrames");
        f1.setAccessible(true);
        AnimationFrame[] left = (AnimationFrame[])f1.get(jsw);
        Field f2 = JSW.class.getDeclaredField("rightFrames");
        f2.setAccessible(true);
        AnimationFrame[] right = (AnimationFrame[])f2.get(jsw);
        Field f3 = JSW.class.getDeclaredField("currentFrameList");
        f3.setAccessible(true);
        AnimationFrame[] currentFrameList = (AnimationFrame[])f3.get(jsw);
        Field f4 = JSW.class.getDeclaredField("currentFrameIdx");
        f4.setAccessible(true);
        int frameIdx = f4.getInt(jsw);

        assert(frameIdx == expectedFrameIdx);
        if(isLeft) {
            assert (currentFrameList == left);
        }
        else {
            assert(currentFrameList == right);
        }
    }
}
