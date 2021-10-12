package com.gitlab.aakumykov.gapless_audio_player;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import com.gitlab.aakumykov.gapless_audio_player.stubs.SampleItem;

import org.junit.Before;
import org.junit.Test;

public class ChainItem_SimpleTest {

    private SampleItem mSampleItem1;
    private SampleItem mSampleItem2;
    private final static String TITLE1 = "Элемент1";
    private final static String TITLE2 = "Элемент2";


    @Before
    public void setUp() throws Exception {
        mSampleItem1 = new SampleItem(TITLE1);
        mSampleItem2 = new SampleItem(TITLE2);
    }


    @Test
    public void getTitle() {
        assertEquals(TITLE1, mSampleItem1.getTitle());
    }

    @Test
    public void setPrevItem() {
        mSampleItem2.setPrevItem(mSampleItem1);
        assertSame(mSampleItem1, mSampleItem2.getPrevItem());
    }

    @Test
    public void setNextItem() {
        mSampleItem1.setNextItem(mSampleItem2);
        assertSame(mSampleItem2, mSampleItem1.getNextItem());
    }

    @Test
    public void getPrevItem() {
        mSampleItem1.setPrevItem(mSampleItem2);
        assertSame(mSampleItem2, mSampleItem1.getPrevItem());
    }

    @Test
    public void getNextItem() {
        mSampleItem1.setNextItem(mSampleItem2);
        assertSame(mSampleItem1.getNextItem(), mSampleItem2);
    }

    @Test
    public void hasPrevItem() {
        mSampleItem1.setPrevItem(mSampleItem2);
        assertTrue(mSampleItem1.hasPrevItem());
    }

    @Test
    public void hasNextItem() {
        mSampleItem1.setNextItem(mSampleItem2);
        assertTrue(mSampleItem1.hasNextItem());
    }

    @Test
    public void testToString() {
        assertTrue(mSampleItem1.toString() instanceof String);
    }
}