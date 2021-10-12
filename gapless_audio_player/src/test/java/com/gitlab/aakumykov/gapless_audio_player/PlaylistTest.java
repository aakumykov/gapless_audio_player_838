package com.gitlab.aakumykov.gapless_audio_player;

import org.junit.Before;
import org.junit.Test;

public class PlaylistTest {

    private Playlist mPlaylist;

    @Before
    public void setUp() throws Exception {
        SoundItem[] soundItems = new SoundItem[] {
                new SoundItem("Трек-1", "/путь/к/файлу-1.mp3"),
                new SoundItem("Трек-2", "/путь/к/файлу-2.mp3"),
                new SoundItem("Трек-3", "/путь/к/файлу-3.mp3")
        };

        mPlaylist = new Playlist();

        for (SoundItem soundItem : soundItems)
            mPlaylist.addIfNotYetFinished(soundItem);

        mPlaylist.finishCreation();
    }

    @Test
    public void addIfNotFinished() {

    }

    @Test
    public void notAddIfFinished() {

    }

    @Test
    public void finishCreation() {
        
    }

    @Test
    public void setActiveItem() {
    }

    @Test
    public void reset() {
    }

    @Test
    public void getList() {
    }

    @Test
    public void getUnshiftedList() {
    }

    @Test
    public void hasPrevItem() {
    }

    @Test
    public void hasNextItem() {
    }

    @Test
    public void hasItems() {
    }
}