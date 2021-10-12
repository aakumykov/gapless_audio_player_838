package com.gitlab.aakumykov.gapless_audio_player;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

public class ProgressTest {

    private final static int POSITION = 50;
    private final static int DURATION = 100;
    private Progress mProgress;

    @Before
    public void setUp() throws Exception {
        mProgress = new Progress(POSITION, DURATION);
    }

    @Test
    public void getPosition() {
        assertEquals(POSITION, mProgress.getPosition());
    }

    @Test
    public void getDuration() {
        assertEquals(DURATION, mProgress.getDuration());
    }
}