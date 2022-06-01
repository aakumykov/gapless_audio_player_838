package com.gitlab.aakumykov.gapless_audio_player.stuff;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

public abstract class ChainItem {

    public static void mergeItemsIntoChain(List<? extends ChainItem> itemsList) {

        int chainSize = itemsList.size();

        if (chainSize < 2)
            return;

        if (2 == chainSize) {
            bindTwoChainElements(itemsList.get(0), itemsList.get(1));
        }
        else {
            final int firstIndex = 0;
            final int endIndex = itemsList.size() - 2;

            for (int i = firstIndex; i <= endIndex; i++)
            {
                ChainItem firstItem = itemsList.get(i);
                ChainItem secondItem = itemsList.get(i + 1);

                if (firstIndex == i)
                    firstItem.setPrevItem(null);

                if (endIndex == i)
                    secondItem.setNextItem(null);

                bindTwoChainElements(firstItem, secondItem);
            }
        }
    }

    private static void bindTwoChainElements(ChainItem firstItem, ChainItem secondItem) {
        firstItem.setNextItem(secondItem);
        secondItem.setPrevItem(firstItem);
    }


    @Nullable private ChainItem mPrevItem;
    @Nullable private ChainItem mNextItem;


    @NonNull
    public abstract String getTitle();

    public final void setPrevItem(@Nullable ChainItem prevItem) {
        mPrevItem = prevItem;
    }

    public final void setNextItem(@Nullable ChainItem nextItem) {
        mNextItem = nextItem;
    }


    @Nullable
    public final ChainItem getPrevItem() {
        return mPrevItem;
    }

    @Nullable
    public final ChainItem getNextItem() {
        return mNextItem;
    }


    public final boolean hasPrevItem() {
        return null != mPrevItem;
    }

    public final boolean hasNextItem() {
        return null != mNextItem;
    }


    @NonNull @Override
    public String toString() {

        String namePrev = (null != mPrevItem) ? mPrevItem.getTitle() : "null";
        String nameNext = (null != mNextItem) ? mNextItem.getTitle() : "null";

        return "[CHAIN: (" + namePrev + ") <--> (" + getTitle() + ") <--> (" + nameNext + ")]";
    }
}
