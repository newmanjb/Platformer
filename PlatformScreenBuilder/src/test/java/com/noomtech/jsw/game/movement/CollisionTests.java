package com.noomtech.jsw.game.movement;

import com.noomtech.jsw.game.gameobjects.GameObject;

public class CollisionTests {


    /**
     * Define 2 rectangular game objects and move one across the top, down the side, along the bottom and up the other side
     * making sure that the {@link GameObject#checkIfLHSIsTouching(GameObject[][])}, {@link GameObject#checkIfRHSIsTouching(GameObject[][])},
     * {@link GameObject#checkIfTopIsTouching(GameObject[][])} and {@link GameObject#checkIfBottomIsTouching(GameObject[][])}
     * methods function correctly.
     */
//    @Test
//    public void test1() throws Exception {
//
//        GameObject go1 = new Nasty(new Rectangle(0,0, 10, 15), null);
//        GameObject p1 = new Platform(new Rectangle(10,15, 10, 21), null);
//
//        GameObject[][] playingFieldUp = new GameObject[100][];
//        GameObject[][] playingFieldDown = new GameObject[100][];
//        GameObject[][] playingFieldLeft = new GameObject[200][];
//        GameObject[][] playingFieldRight = new GameObject[200][];
//
//        playingFieldDown[p1.getY()] = new GameObject[]{p1};
//        playingFieldUp[p1.getY() + (p1.getHeight() - 1)] = new GameObject[]{p1};
//        playingFieldLeft[p1.getX() + (p1.getWidth() - 1)] = new GameObject[]{p1};
//        playingFieldRight[p1.getX()] = new GameObject[]{p1};
//
//        assert(go1.checkIfBottomIsTouching(playingFieldDown) == null);
//        assert(go1.checkIfTopIsTouching(playingFieldUp) == null);
//        assert(go1.checkIfLHSIsTouching(playingFieldLeft) == null);
//        assert(go1.checkIfRHSIsTouching(playingFieldRight) == null);
//
//        for(int x = 1; x < p1.getLocation().x + p1.getWidth(); x++) {
//            go1.setLocation(x, go1.getY());
//            assert(go1.checkIfBottomIsTouching(playingFieldDown) == p1);
//            assert(go1.checkIfTopIsTouching(playingFieldUp) == null);
//            assert(go1.checkIfLHSIsTouching(playingFieldLeft) == null);
//            assert(go1.checkIfRHSIsTouching(playingFieldRight) == null);
//        }
//
//        go1.setLocation(go1.getX()+1, go1.getY());
//        assert(go1.checkIfBottomIsTouching(playingFieldDown) == null);
//        assert(go1.checkIfTopIsTouching(playingFieldUp) == null);
//        assert(go1.checkIfLHSIsTouching(playingFieldLeft) == null);
//        assert(go1.checkIfRHSIsTouching(playingFieldRight) == null);
//
//        for(int y = 1; y < p1.getLocation().y + p1.getHeight(); y++) {
//            go1.setLocation(go1.getX(), y);
//            assert(go1.checkIfLHSIsTouching(playingFieldLeft) == p1);
//            assert(go1.checkIfTopIsTouching(playingFieldUp) == null);
//            assert(go1.checkIfBottomIsTouching(playingFieldDown) == null);
//            assert(go1.checkIfRHSIsTouching(playingFieldRight) == null);
//        }
//
//        go1.setLocation(go1.getX(), go1.getY() + 1);
//        assert(go1.checkIfLHSIsTouching(playingFieldLeft) == null);
//        assert(go1.checkIfTopIsTouching(playingFieldUp) == null);
//        assert(go1.checkIfBottomIsTouching(playingFieldDown) == null);
//        assert(go1.checkIfRHSIsTouching(playingFieldRight) == null);
//
//
//        for(int x = p1.getLocation().x + 1; x > 0; x--) {
//            go1.setLocation(x, go1.getY());
//            assert(go1.checkIfTopIsTouching(playingFieldUp) == p1);
//            assert(go1.checkIfBottomIsTouching(playingFieldDown) == null);
//            assert(go1.checkIfLHSIsTouching(playingFieldLeft) == null);
//            assert(go1.checkIfRHSIsTouching(playingFieldRight) == null);
//        }
//
//        go1.setLocation(go1.getX() -  1, go1.getY());
//        assert(go1.checkIfLHSIsTouching(playingFieldLeft) == null);
//        assert(go1.checkIfTopIsTouching(playingFieldUp) == null);
//        assert(go1.checkIfBottomIsTouching(playingFieldDown) == null);
//        assert(go1.checkIfRHSIsTouching(playingFieldRight) == null);
//
//        for(int y = go1.getY() - 1; y > 0; y--) {
//            go1.setLocation(go1.getX(), y);
//            assert(go1.checkIfRHSIsTouching(playingFieldRight) == p1);
//            assert(go1.checkIfLHSIsTouching(playingFieldLeft) == null);
//            assert(go1.checkIfTopIsTouching(playingFieldUp) == null);
//            assert(go1.checkIfBottomIsTouching(playingFieldDown) == null);
//        }
//
//        go1.setLocation(go1.getX(), go1.getY() - 1);
//        assert(go1.getX() == 0);
//        assert(go1.getY() == 0);
//        assert(go1.checkIfLHSIsTouching(playingFieldLeft) == null);
//        assert(go1.checkIfTopIsTouching(playingFieldUp) == null);
//        assert(go1.checkIfBottomIsTouching(playingFieldDown) == null);
//        assert(go1.checkIfRHSIsTouching(playingFieldRight) == null);
//
//    }
//
//    /**
//     * As for {@link #test1()} except that the game object is moved across and down, not touching anything
//     * most of the time
//     */
//    @Test
//    public void test2() throws Exception {
//
//        GameObject go1 = new Nasty(new Rectangle(10,0, 10, 10), null);
//        GameObject p1 = new Platform(new Rectangle(10,10, 10, 10), null);
//
//        GameObject[][] playingFieldUp = new GameObject[100][];
//        GameObject[][] playingFieldDown = new GameObject[100][];
//        GameObject[][] playingFieldLeft = new GameObject[200][];
//        GameObject[][] playingFieldRight = new GameObject[200][];
//
//        playingFieldDown[p1.getY()] = new GameObject[]{p1};
//        playingFieldUp[p1.getY() + (p1.getHeight() - 1)] = new GameObject[]{p1};
//        playingFieldLeft[p1.getX() + (p1.getWidth() - 1)] = new GameObject[]{p1};
//        playingFieldRight[p1.getX()] = new GameObject[]{p1};
//
//        assert(go1.checkIfBottomIsTouching(playingFieldDown) == p1);
//        assert(go1.checkIfTopIsTouching(playingFieldUp) == null);
//        assert(go1.checkIfLHSIsTouching(playingFieldLeft) == null);
//        assert(go1.checkIfRHSIsTouching(playingFieldRight) == null);
//
//        //Move it to the right by 10 and then move it to the right by 10 again, making sure it's not seen to be
//        //touching anything
//
//        go1.setLocation(go1.getX() + 10, go1.getY());
//        int limit = go1.getX() + 10;
//        for(int x = go1.getX(); x < limit; x++) {
//            go1.setLocation(x, go1.getY());
//            assert(go1.checkIfBottomIsTouching(playingFieldDown) == null);
//            assert(go1.checkIfTopIsTouching(playingFieldUp) == null);
//            assert(go1.checkIfLHSIsTouching(playingFieldLeft) == null);
//            assert(go1.checkIfRHSIsTouching(playingFieldRight) == null);
//        }
//
//        //Move it down by 10 making sure it's not seen to be
//        //touching anything
//        limit = go1.getY() + 10;
//        for(int y = go1.getY(); y < limit; y++) {
//            go1.setLocation(go1.getX(), y);
//            assert(go1.checkIfBottomIsTouching(playingFieldDown) == null);
//            assert(go1.checkIfTopIsTouching(playingFieldUp) == null);
//            assert(go1.checkIfLHSIsTouching(playingFieldLeft) == null);
//            assert(go1.checkIfRHSIsTouching(playingFieldRight) == null);
//        }
//    }
//
//    /**
//     * Tests the same methods as {@link #test1()} using edge cases by defining a game object and moving it into in and
//     * out of the corners of the screen and out of the defined area.
//     */
//    @Test
//    public void test3() throws Exception {
//
//        GameObject go1 = new Nasty(new Rectangle(89,1, 10, 10), null);
//        GameObject p1 = new Platform(new Rectangle(10,10, 10, 10), null);
//
//        GameObject[][] playingFieldUp = new GameObject[100][];
//        GameObject[][] playingFieldDown = new GameObject[100][];
//        GameObject[][] playingFieldLeft = new GameObject[100][];
//        GameObject[][] playingFieldRight = new GameObject[100][];
//
//        playingFieldDown[p1.getY()] = new GameObject[]{p1};
//        playingFieldUp[p1.getY() + (p1.getHeight() - 1)] = new GameObject[]{p1};
//        playingFieldLeft[p1.getX() + (p1.getWidth() - 1)] = new GameObject[]{p1};
//        playingFieldRight[p1.getX()] = new GameObject[]{p1};
//
//        assert(go1.checkIfBottomIsTouching(playingFieldDown) == null);
//        assert(go1.checkIfTopIsTouching(playingFieldUp) == null);
//        assert(go1.checkIfLHSIsTouching(playingFieldLeft) == null);
//        assert(go1.checkIfRHSIsTouching(playingFieldRight) == null);
//
//        go1.setLocation(go1.getX() + 1, go1.getY());
//        assert(go1.checkIfBottomIsTouching(playingFieldDown) == null);
//        assert(go1.checkIfTopIsTouching(playingFieldUp) == null);
//        assert(go1.checkIfLHSIsTouching(playingFieldLeft) == null);
//        assert(go1.checkIfRHSIsTouching(playingFieldRight) == null);
//
//        go1.setLocation(go1.getX(), go1.getY() - 1);
//        assert(go1.checkIfBottomIsTouching(playingFieldDown) == null);
//        assert(go1.checkIfTopIsTouching(playingFieldUp) == null);
//        assert(go1.checkIfLHSIsTouching(playingFieldLeft) == null);
//        assert(go1.checkIfRHSIsTouching(playingFieldRight) == null);
//
//        //Make the RHS of the game object out of bounds and make sure there's an IllegalStateException
//        go1.setLocation(go1.getX() + 1, go1.getY());
//        boolean correctExceptionCaught = false;
//        try {
//            go1.checkIfRHSIsTouching(playingFieldRight);
//        }
//        catch(IllegalStateException e) {
//            correctExceptionCaught = true;
//        }
//        assert(correctExceptionCaught);
//
//        //Set the x location back to what it was
//        go1.setLocation(go1.getX() + 1, go1.getY());
//
//        //Put the top of the game object out of bounds
//        go1.setLocation(go1.getX(), go1.getY() - 1);
//        correctExceptionCaught = false;
//        try {
//            go1.checkIfTopIsTouching(playingFieldRight);
//        }
//        catch(IllegalStateException e) {
//            correctExceptionCaught = true;
//        }
//        assert(correctExceptionCaught);
//
//        //Put the game object into the bottom left corner and do the same tests with the bottom and lhs
//        //of the game object
//        go1.setLocation(1, 99 - go1.getHeight());
//        assert(go1.checkIfBottomIsTouching(playingFieldDown) == null);
//        assert(go1.checkIfTopIsTouching(playingFieldUp) == null);
//        assert(go1.checkIfLHSIsTouching(playingFieldLeft) == null);
//        assert(go1.checkIfRHSIsTouching(playingFieldRight) == null);
//
//        go1.setLocation(go1.getX() - 1, go1.getY());
//        assert(go1.checkIfBottomIsTouching(playingFieldDown) == null);
//        assert(go1.checkIfTopIsTouching(playingFieldUp) == null);
//        assert(go1.checkIfLHSIsTouching(playingFieldLeft) == null);
//        assert(go1.checkIfRHSIsTouching(playingFieldRight) == null);
//
//        go1.setLocation(go1.getX(), go1.getY() + 1);
//        assert(go1.checkIfBottomIsTouching(playingFieldDown) == null);
//        assert(go1.checkIfTopIsTouching(playingFieldUp) == null);
//        assert(go1.checkIfLHSIsTouching(playingFieldLeft) == null);
//        assert(go1.checkIfRHSIsTouching(playingFieldRight) == null);
//
//
//        go1.setLocation(go1.getX() - 1, go1.getY());
//        correctExceptionCaught = false;
//        try {
//            go1.checkIfLHSIsTouching(playingFieldLeft);
//        }
//        catch(IllegalStateException e) {
//            correctExceptionCaught = true;
//        }
//        assert(correctExceptionCaught);
//
//        go1.setLocation(go1.getX() + 1, go1.getY());
//        go1.setLocation(go1.getX(), go1.getY() + 1);
//        correctExceptionCaught = false;
//        try {
//            go1.checkIfBottomIsTouching(playingFieldDown);
//        }
//        catch(IllegalStateException e) {
//            correctExceptionCaught = true;
//        }
//        assert(correctExceptionCaught);
//    }
}
