package com.gitlab.aakumykov.gapless_audio_player;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;


public class PlaylistTest {

    private Playlist mPlaylist;
    private SoundItem mSoundItem1;
    private SoundItem mSoundItem2;
    private SoundItem mSoundItem3;


    @Before
    public void setUp() throws Exception {

        mSoundItem1 = new SoundItem("Музыка-1", "/path/to/file1.mp3");
        mSoundItem2 = new SoundItem("Музыка-2", "/path/to/file2.mp3");
        mSoundItem3 = new SoundItem("Музыка-3", "/path/to/file3.mp3");

        mPlaylist = new Playlist();
    }


    @Test
    public void add_IfNotFinished() {
        mPlaylist.addIfNotYetFinished(mSoundItem1);

        assertTrue(mPlaylist.getList().contains(mSoundItem1));
        assertEquals(1, mPlaylist.getList().size());
    }

    @Test
    public void notAdd_IfFinished() {
        mPlaylist.finishCreation();

        assertThrows(IllegalStateException.class, () -> mPlaylist.addIfNotYetFinished(mSoundItem1));
        assertEquals(0, mPlaylist.getList().size());
    }

    @Test
    public void finishCreation() {
        mPlaylist.finishCreation();

        assertTrue(mPlaylist.isFinished());
        assertThrows(IllegalStateException.class, () -> mPlaylist.addIfNotYetFinished(mSoundItem1));
    }

    @Test
    public void setActiveItem() {
        mPlaylist.addIfNotYetFinished(mSoundItem1);
        mPlaylist.addIfNotYetFinished(mSoundItem2);
        mPlaylist.addIfNotYetFinished(mSoundItem3);

        mPlaylist.setActiveItem(mSoundItem2);

        assertSame(mSoundItem2, mPlaylist.getActiveItem());
    }

    @Test
    public void getActiveItem() {
        mPlaylist.addIfNotYetFinished(mSoundItem1);
        mPlaylist.setActiveItem(mSoundItem1);

        assertSame(mSoundItem1, mPlaylist.getActiveItem());
    }

    @Test
    public void reset() {
        mPlaylist.reset();

        assertFalse(mPlaylist.isFinished());
        assertNull(mPlaylist.getActiveItem());
        assertEquals(0, mPlaylist.getList().size());
    }

    @Test
    public void throwsExceptionIfActiveItemHasNotBeenNotSet_v1() {
        assertThrows(IllegalStateException.class, () -> mPlaylist.hasPrevItem());
    }

    @Test
    public void throwsExceptionIfActiveItemHasNotBeenNotSet_v2() {
        mPlaylist.addIfNotYetFinished(mSoundItem1);
        mPlaylist.addIfNotYetFinished(mSoundItem2);

        assertThrows(IllegalStateException.class, () -> mPlaylist.hasPrevItem());
    }

    @Test
    public void throwsExceptionIfActiveItemNotFound() {
        mPlaylist.addIfNotYetFinished(mSoundItem1);
        assertThrows(IllegalStateException.class, () -> mPlaylist.setActiveItem(mSoundItem2));
    }

    @Test
    public void hasNoPrevItemWhenSingleItemList() {
        prepareListWithOneItem();
        mPlaylist.setActiveItem(mSoundItem1);
        assertFalse(mPlaylist.hasPrevItem());
    }

    @Test
    public void hasNoNextItemWhenSingleItemList() {
        prepareListWithOneItem();
        mPlaylist.setActiveItem(mSoundItem1);
        assertFalse(mPlaylist.hasNextItem());
    }

    @Test
    public void hasNextItemForFirstITem() {
        preparePlaylistWithTwoItems();
        mPlaylist.setActiveItem(mSoundItem1);
        assertTrue(mPlaylist.hasNextItem());
    }

    @Test
    public void hasNoPrevItemForFirstItem() {
        preparePlaylistWithTwoItems();
        mPlaylist.setActiveItem(mSoundItem1);
        assertFalse(mPlaylist.hasPrevItem());
    }

    @Test
    public void hasPrevItemForSecondItem() {
        preparePlaylistWithTwoItems();
        mPlaylist.setActiveItem(mSoundItem2);
        assertTrue(mPlaylist.hasPrevItem());
    }

    @Test
    public void hasNoNextItemForSecondItem() {
        preparePlaylistWithTwoItems();
        mPlaylist.setActiveItem(mSoundItem2);
        assertFalse(mPlaylist.hasNextItem());
    }


    @Test
    public void getListBeforeFinishing() {
        mPlaylist.addIfNotYetFinished(mSoundItem1);
        assertTrue(mPlaylist.getList().contains(mSoundItem1));
        assertEquals(1, mPlaylist.getList().size());

        mPlaylist.addIfNotYetFinished(mSoundItem2);
        assertTrue(mPlaylist.getList().contains(mSoundItem2));
        assertEquals(2, mPlaylist.getList().size());

        mPlaylist.addIfNotYetFinished(mSoundItem3);
        assertTrue(mPlaylist.getList().contains(mSoundItem3));
        assertEquals(3, mPlaylist.getList().size());
    }

    @Test
    public void getListAfterFinishing() {
        mPlaylist.addIfNotYetFinished(mSoundItem1);
        mPlaylist.addIfNotYetFinished(mSoundItem2);
        mPlaylist.addIfNotYetFinished(mSoundItem3);
        mPlaylist.finishCreation();

        assertEquals(3, mPlaylist.getList().size());
    }

    @Test
    public void throwsExceptionOnUnshiftingEmptyList() {
        mPlaylist.finishCreation();
        assertThrows(IllegalStateException.class, () -> mPlaylist.getUnshiftedList());
    }

    @Test
    public void throwsExceptionWithoutActiveItem() {
        preparePlaylistWithTwoItems();
        assertThrows(IllegalStateException.class, () -> mPlaylist.getUnshiftedList());
    }

    @Test
    public void getUnshiftedListWithFirstActiveItem() {
        prepareListWithThreeItems();
        mPlaylist.setActiveItem(mSoundItem1);
        assertEquals(mPlaylist.getUnshiftedList(), mPlaylist.getList());
    }

    @Test
    public void getUnshiftedListWithSecondActiveItem() {
        prepareListWithThreeItems();
        mPlaylist.setActiveItem(mSoundItem2);
        assertEquals(mPlaylist.getUnshiftedList(), mPlaylist.getList());
    }

    @Test
    public void getUnshiftedListWithThirdActiveItem() {
        prepareListWithThreeItems();
        mPlaylist.setActiveItem(mSoundItem3);
        assertEquals(mPlaylist.getUnshiftedList(), Arrays.asList(mSoundItem2, mSoundItem3));
    }


    private void prepareListWithOneItem() {
        mPlaylist.addIfNotYetFinished(mSoundItem1);
        mPlaylist.finishCreation();
    }

    private void preparePlaylistWithTwoItems() {
        mPlaylist.reset();
        mPlaylist.addIfNotYetFinished(mSoundItem1);
        mPlaylist.addIfNotYetFinished(mSoundItem2);
        mPlaylist.finishCreation();
    }

    private void prepareListWithThreeItems() {
        mPlaylist.reset();
        mPlaylist.addIfNotYetFinished(mSoundItem1);
        mPlaylist.addIfNotYetFinished(mSoundItem2);
        mPlaylist.addIfNotYetFinished(mSoundItem3);
        mPlaylist.finishCreation();
    }
}