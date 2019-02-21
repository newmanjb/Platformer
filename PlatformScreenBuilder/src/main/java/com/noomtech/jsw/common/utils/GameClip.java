package com.noomtech.jsw.common.utils;

import javax.sound.sampled.Clip;


/**
 * Holds a sound clip and a boolean which dictates whether the sound should be played once or on a continuous loop
 */
public class GameClip {

    private boolean playContinuously;
    private Clip clip;

    public GameClip(Clip clip, boolean playContinuously) {
        this.playContinuously = playContinuously;
        this.clip = clip;
    }

    public boolean playContinuously() {
        return playContinuously;
    }

    public Clip getClip() {
        return clip;
    }
}
