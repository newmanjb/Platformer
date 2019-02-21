package com.noomtech.jsw.common.utils;

import javax.sound.sampled.Clip;
import java.util.Map;


/**
 * Plays the game's sounds.
 * @see CommonUtils#getSounds()
 * @see GameClip
 */
public class SoundPlayer {


    private final Map<String,GameClip> SOUND_MAP;

    private SoundPlayer() {
        SOUND_MAP = CommonUtils.getSounds();
    }
    private static final class INSTANCE_HOLDER {
        private static final SoundPlayer INSTANCE = new SoundPlayer();
    }

    public static SoundPlayer getInstance() {
        return INSTANCE_HOLDER.INSTANCE;
    }

    public void startSound(String name) {
        GameClip gameClip = SOUND_MAP.get(name);
        if(gameClip != null) {
            if(gameClip.playContinuously()) {
                gameClip.getClip().loop(Clip.LOOP_CONTINUOUSLY);
            }
            else {
                gameClip.getClip().loop(0);
                gameClip.getClip().setFramePosition(0);
            }
        }
    }

    public void stopSound(String name) {
        GameClip gameClip = SOUND_MAP.get(name);
        if(gameClip != null) {
            gameClip.getClip().stop();
            gameClip.getClip().setFramePosition(0);
        }
    }
}
