package com.gitlab.aakumykov.gapless_audio_player;

import androidx.annotation.Nullable;

import java.util.List;

public class ChainItem {

    public static void mergeItemsIntoChain(List<ChainItem> itemsList) {
        
        int chainSize = itemsList.size();

        if (chainSize < 2)
            return;

        if (2 == chainSize) {
            ChainItem firstItem = itemsList.get(0);
            ChainItem secondItem = itemsList.get(1);
            firstItem.setPrevItem(null);
            firstItem.setNextItem(secondItem);
            secondItem.setPrevItem(firstItem);
            secondItem.setNextItem(null);
        }
        else {
            /*final int firstIndex = 0;
            final int endIndex = mPlayersChain.size() - 2;

            for (int i = firstIndex; i <= endIndex; i++) {
                Player firstPlayer = mPlayersChain.get(i);
                Player secondPlayer = mPlayersChain.get(i + 1);

                if (firstIndex == i)
                    firstPlayer.setPrevPlayer(null);

                if (endIndex == i)
                    secondPlayer.setNextPlayer(null);

                bindTwoChainElements(firstPlayer, secondPlayer);
            }*/
        }
    }

    private static void bindTwoChainElements(Player firstPlayer, Player secondPlayer) {
        firstPlayer.setNextPlayer(secondPlayer);
        secondPlayer.setPrevPlayer(firstPlayer);

        firstPlayer.setNextMediaPlayer(secondPlayer);
    }

    @Nullable
    private ChainItem mPrevItem;

    @Nullable
    private ChainItem mNextItem;

    public void setPrevItem(@Nullable ChainItem prevItem) {
        mPrevItem = prevItem;
    }

    public void setNextItem(@Nullable ChainItem nextItem) {
        mNextItem = nextItem;
    }

    @Nullable
    public ChainItem getPrevItem() {
        return mPrevItem;
    }

    @Nullable
    public ChainItem getNextItem() {
        return mNextItem;
    }

    public boolean hasPrevItem() {
        return null != mPrevItem;
    }

    public boolean hasNextItem() {
        return null != mNextItem;
    }
}
