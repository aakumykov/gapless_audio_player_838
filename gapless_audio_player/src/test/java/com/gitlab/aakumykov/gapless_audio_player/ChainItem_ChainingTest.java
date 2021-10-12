package com.gitlab.aakumykov.gapless_audio_player;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class ChainItem_ChainingTest {

    private SampleItem mFirstItem;
    private SampleItem mMiddleItem;
    private SampleItem mLastItem;


    @Before
    public void setUp() throws Exception {

        mFirstItem = new SampleItem("Начальный элемент");
        mMiddleItem = new SampleItem("Средний элемент");
        mLastItem = new SampleItem("Конечный элемент");

        List<SampleItem> sampleItemList = Arrays.asList(
                mFirstItem,
                mMiddleItem,
                mLastItem
        );

        ChainItem.mergeItemsIntoChain(sampleItemList);
    }

    // Первый элемент
    @Test
    public void firstItemHasNoPrevItem() {
        assertNull(mFirstItem.getPrevItem());
    }

    @Test
    public void firstItemHasNextItem() {
        assertSame(mMiddleItem, mFirstItem.getNextItem());
    }


    // Средний элемент
    @Test
    public void middleItemHasPrevItem() {
        assertSame(mFirstItem, mMiddleItem.getPrevItem());
    }

    @Test
    public void middleItemHasNextItem() {
        assertSame(mLastItem, mMiddleItem.getNextItem());
    }

    // Конечный элемент
    @Test
    public void lastItemHasPrevItem() {
        assertSame(mMiddleItem, mLastItem.getPrevItem());
    }

    @Test
    public void lastItemHasNoNextItem() {
        assertNull(mLastItem.getNextItem());
    }
}