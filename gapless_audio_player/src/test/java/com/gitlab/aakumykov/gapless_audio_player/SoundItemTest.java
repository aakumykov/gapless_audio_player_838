package com.gitlab.aakumykov.gapless_audio_player;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

public class SoundItemTest {

    private final String mTitle = "Музыка";
    private final String mFilePath = "/path/to/music/file.mp3";
    private SoundItem mSoundItem;

    @Before
    public void setUp() throws Exception {
        mSoundItem = new SoundItem(mTitle, mFilePath);
    }

    @Test
    public void getFilePath() {
        assertEquals(mFilePath, mSoundItem.getFilePath());
    }

    @Test
    public void getTitle() {
        assertEquals(mTitle, mSoundItem.getTitle());
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Test
    public void testToString() {
        mSoundItem.toString();
    }
}