package com.noomtech.jsw.game.gameobjects;

import com.noomtech.jsw.game.gameobjects.objects.JSW;
import org.junit.Test;

import java.awt.*;
import java.lang.reflect.Field;
import java.util.Collections;

public class JSWTest {

    //@todo tests broken - fix them
    /**
     * Move the JSW forward in small increments that are less than the num pixels per frame change,
     * checking whether a new frame has been correctly set at each stage
     */
    @Test
    public void test1() throws Exception {

        int numPixelsPerMovement = 10;
        JSW jsw = new JSW(new Rectangle(20,20,5,5), Collections.EMPTY_MAP);
        Field f1 = JSW.class.getDeclaredField("pixelsPerFrameChange");
        f1.setAccessible(true);
        f1.setInt(jsw, numPixelsPerMovement);

        checkVals(jsw, 0, false);

        //Walk right

        jsw.setLocation((int)(jsw.getX() + numPixelsPerMovement * 0.1), jsw.getY());
        jsw.reactToMove();

        checkVals(jsw, 0, false);

        jsw.setLocation((int)(jsw.getX() + numPixelsPerMovement * 0.8), jsw.getY());
        jsw.reactToMove();

        checkVals(jsw, 0, false);

        jsw.setLocation(jsw.getX() + (int)(numPixelsPerMovement * 0.1), jsw.getY());
        jsw.reactToMove();

        checkVals(jsw, 1, false);

        jsw.setLocation((int)(jsw.getX() + numPixelsPerMovement * 0.1), jsw.getY());
        jsw.reactToMove();

        checkVals(jsw, 1, false);

        jsw.setLocation((int)(jsw.getX() + numPixelsPerMovement * 0.8), jsw.getY());
        jsw.reactToMove();

        checkVals(jsw, 1, false);

        jsw.setLocation(jsw.getX() + (int)(numPixelsPerMovement * 0.1), jsw.getY());
        jsw.reactToMove();

        checkVals(jsw, 2, false);

        jsw.setLocation((int)(jsw.getX() + numPixelsPerMovement * 0.1), jsw.getY());
        jsw.reactToMove();

        checkVals(jsw, 2, false);

        jsw.setLocation((int)(jsw.getX() + numPixelsPerMovement * 0.8), jsw.getY());
        jsw.reactToMove();

        checkVals(jsw, 2, false);

        jsw.setLocation(jsw.getX() + (int)(numPixelsPerMovement * 0.1), jsw.getY());
        jsw.reactToMove();

        checkVals(jsw, 0, false);

        //Walk left

        jsw.setLocation((int)(jsw.getX() - numPixelsPerMovement * 0.1), jsw.getY());
        jsw.reactToMove();

        checkVals(jsw, 0, true);

        jsw.setLocation((int)(jsw.getX() - numPixelsPerMovement * 0.8), jsw.getY());
        jsw.reactToMove();

        checkVals(jsw, 0, true);

        jsw.setLocation(jsw.getX() - (int)(numPixelsPerMovement * 0.1), jsw.getY());
        jsw.reactToMove();

        checkVals(jsw, 1, true);

        jsw.setLocation((int)(jsw.getX() - numPixelsPerMovement * 0.1), jsw.getY());
        jsw.reactToMove();

        checkVals(jsw, 1, true);

        jsw.setLocation((int)(jsw.getX() - numPixelsPerMovement * 0.8), jsw.getY());
        jsw.reactToMove();

        checkVals(jsw, 1, true);

        jsw.setLocation(jsw.getX() - (int)(numPixelsPerMovement * 0.1), jsw.getY());
        jsw.reactToMove();

        checkVals(jsw, 2, true);

        jsw.setLocation((int)(jsw.getX() - numPixelsPerMovement * 0.1), jsw.getY());
        jsw.reactToMove();

        checkVals(jsw, 2, true);

        jsw.setLocation((int)(jsw.getX() - numPixelsPerMovement * 0.8), jsw.getY());
        jsw.reactToMove();

        checkVals(jsw, 2, true);

        jsw.setLocation(jsw.getX() - (int)(numPixelsPerMovement * 0.1), jsw.getY());
        jsw.reactToMove();

        checkVals(jsw, 0, true);
    }

    /**
     * Move the JSW around within the area defined by "num pixels per frame" and make sure the frame is never changed.
     */
    @Test
    public void test2() throws Exception {

        int numPixelsPerMovement = 10;
        JSW jsw = new JSW(new Rectangle(20,20,5,5), Collections.EMPTY_MAP);
        Field f1 = JSW.class.getDeclaredField("pixelsPerFrameChange");
        f1.setAccessible(true);
        f1.setInt(jsw, numPixelsPerMovement);

        checkVals(jsw, 0, false);

        jsw.setLocation((int)(jsw.getX() + numPixelsPerMovement * 0.1), jsw.getY());
        jsw.reactToMove();

        checkVals(jsw, 0, false);

        jsw.setLocation((int)(jsw.getX() - numPixelsPerMovement * 0.1), jsw.getY());
        jsw.reactToMove();

        checkVals(jsw, 0, true);

        jsw.setLocation(jsw.getX() + (int)(numPixelsPerMovement * 0.9), jsw.getY());
        jsw.reactToMove();

        checkVals(jsw, 0, false);

        jsw.setLocation((int)(jsw.getX() - numPixelsPerMovement * 0.9), jsw.getY());
        jsw.reactToMove();

        checkVals(jsw, 0, true);
    }

    /**
     * Move the jsw using movements that are greater than the num pixels per frame and ensure that the correct frame is
     * always displayed.
     */
    @Test
    public void test3() throws Exception {

        int numPixelsPerMovement = 10;
        JSW jsw = new JSW(new Rectangle(20,20,5,5), Collections.EMPTY_MAP);
        Field f1 = JSW.class.getDeclaredField("pixelsPerFrameChange");
        f1.setAccessible(true);
        f1.setInt(jsw, numPixelsPerMovement);

        checkVals(jsw, 0, false);

        jsw.setLocation((int)(jsw.getX() + numPixelsPerMovement * 1.5), jsw.getY());
        jsw.reactToMove();

        checkVals(jsw, 1, false);

        jsw.setLocation((int)(jsw.getX() + numPixelsPerMovement * 0.5), jsw.getY());
        jsw.reactToMove();

        checkVals(jsw, 2, false);

        //Walk left
        jsw.setLocation((int)(jsw.getX() - numPixelsPerMovement * 1.5), jsw.getY());
        jsw.reactToMove();

        checkVals(jsw, 0, true);

        jsw.setLocation((int)(jsw.getX() - numPixelsPerMovement * 0.5), jsw.getY());
        jsw.reactToMove();

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
