package com.gitlab.aakumykov.gapless_audio_player;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.hamcrest.MatcherAssert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class PlayerTest {

    private Player mPlayer1;
    private Player mPlayer2;

    private SoundItem mSoundItem1;
    private SoundItem mSoundItem2;


    @Before
    public void setUp() throws Exception {
        mSoundItem1 = new SoundItem("sound1", "Название песни 1", "/путь/к/файлу1.mp3");
        mSoundItem2 = new SoundItem("sound2", "Название песни 2", "/путь/к/файлу2.mp3");

        mPlayer1 = new Player(mSoundItem1);
        mPlayer2 = new Player(mSoundItem2);
    }

    @After
    public void tearDown() throws Exception {
        if (null != mPlayer1)
            mPlayer1.release();

        if (null != mPlayer2)
            mPlayer2.release();
    }


    @Test
    public void start() {
        mPlayer1.start();

        assertTrue(mPlayer1.isPlaying());
        assertTrue(mPlayer1.isNotStopped());
        assertFalse(mPlayer1.isPaused());
    }

    @Test
    public void stop() {
        mPlayer1.stop();

        assertFalse(mPlayer1.isPlaying());
        assertFalse(mPlayer1.isNotStopped());
        assertFalse(mPlayer1.isPaused());
    }

    @Test
    public void pause() {
        mPlayer1.pause();

        assertFalse(mPlayer1.isPlaying());
        assertTrue(mPlayer1.isNotStopped());
        assertTrue(mPlayer1.isPaused());
    }

    @Test
    public void isPlaying() {
        mPlayer1.start();
        assertTrue(mPlayer1.isPlaying());

        mPlayer1.pause();
        assertFalse(mPlayer1.isPlaying());

        mPlayer1.stop();
        assertFalse(mPlayer1.isPlaying());
    }

    @Test
    public void isPaused() {
        mPlayer1.start();
        assertFalse(mPlayer1.isPaused());

        mPlayer1.pause();
        assertTrue(mPlayer1.isPaused());

        mPlayer1.stop();
        assertFalse(mPlayer1.isPaused());
    }

    @Test
    public void isNotStopped() {
        mPlayer1.start();
        assertTrue(mPlayer1.isNotStopped());

        // TODO: а если убрать start() ?
        mPlayer1.start();
        mPlayer1.stop();
        assertFalse(mPlayer1.isNotStopped());

        mPlayer1.start();
        mPlayer1.pause();
        assertTrue(mPlayer1.isNotStopped());
    }

    @Test
    public void setNextPlayer() {
        mPlayer1.setNextPlayer(mPlayer2);
        assertSame(mPlayer2, mPlayer1.getNextPlayer());
    }

    @Test
    public void getNextPlayer() {
        // Идентично тесту "setNextPlayer"
    }

    @Test
    public void setPrevPlayer() {
        mPlayer2.setPrevPlayer(mPlayer1);
        assertSame(mPlayer1, mPlayer2.getPrevPlayer());
    }

    @Test
    public void getPrevPlayer() {
        // Идентично тесту "setPrevPlayer"
    }

    @Test
    public void getSoundItem() {
        assertEquals(mSoundItem1, mPlayer1.getSoundItem());
        assertEquals(mSoundItem2, mPlayer2.getSoundItem());
    }

    @Test
    public void testToString() {
        MatcherAssert.assertThat(mPlayer1.toString(), instanceOf(String.class));
    }


}