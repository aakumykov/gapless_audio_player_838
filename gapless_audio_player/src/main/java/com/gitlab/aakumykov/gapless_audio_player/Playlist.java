package com.gitlab.aakumykov.gapless_audio_player;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class Playlist {

    private final List<PlaylistItem> mItemsList = new ArrayList<>();
    private boolean mIsFilled = false;
    private PlaylistItem mActiveItem;


    public Playlist() {

    }


    public void addIfNotFilled(@NonNull SoundItem soundItem) {
        if (!mIsFilled)
            mItemsList.add(new PlaylistItem(soundItem));
    }

    public void markAsFilled() {
        mIsFilled = true;
    }

    public void setActiveItem(@NonNull SoundItem soundItem) {
        mActiveItem = new PlaylistItem(soundItem);
    }

    public void reset() {
        mIsFilled = false;
        mActiveItem = null;
        mItemsList.clear();
    }

    public List<SoundItem> getUnshiftedList() {

        int activeItemIndex = mItemsList.indexOf(mActiveItem);

        /*if (activeItemIndex < 0) {
            throw new IllegalStateException("Active item not found in list.");
        }
        if (0 == activeItemIndex) {
            return mItemsList.stream().map(playlistItem -> new SoundItem(
                    playlistItem.getTitle(),
                    playlistItem.getFilePath()
            )).collect(Collectors.toList());
        }
        else {
            return mItemsList.stream().skip(activeItemIndex-1).collect(Collectors.toList());
        }*/

        return null;
    }

    public boolean hasPrevItem() {
        return (null != mActiveItem && null != mActiveItem.getPrevItem());
    }


    private static class PlaylistItem extends ChainItem {

        @Nullable private ChainItem mPrevItem;
        @Nullable private ChainItem mNextItem;

        public PlaylistItem(@NonNull ChainItem soundItem) {

        }

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
    }
}
