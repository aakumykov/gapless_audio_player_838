package com.gitlab.aakumykov.gapless_audio_player;

import com.gitlab.aakumykov.gapless_audio_player.stuff.SoundItem;

import org.junit.Before;
import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.assertEquals;

public class SoundItemTest {

    private final static String ID = UUID.randomUUID().toString();
    private final static String TITLE = "Музыка";
    private final static String FILE_PATH = "/path/to/music/file.mp3";
    private SoundItem mSoundItem;

    @Before
    public void setUp() throws Exception {
        mSoundItem = new SoundItem(ID, TITLE, FILE_PATH);
    }

    @Test
    public void getFilePath() {
        assertEquals(FILE_PATH, mSoundItem.getFilePath());
    }

    @Test
    public void getTitle() {
        assertEquals(TITLE, mSoundItem.getTitle());
    }

    @Test
    public void getId() {
        assertEquals(ID, mSoundItem.getId());
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Test
    public void testToString() {
        mSoundItem.toString();
    }
}