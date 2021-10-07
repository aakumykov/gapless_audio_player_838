package com.gitlab.aakumykov.gapless_audio_player;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

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
        ChainItem.mergeItemsIntoChain(mItemsList);
    }

    public void setActiveItem(@NonNull SoundItem soundItem) {

        mActiveItem = null;

        mItemsList
                .stream()
                .filter(playlistItem -> playlistItem.getSoundItem().equals(soundItem))
                .findFirst()
                .ifPresent(new Consumer<PlaylistItem>() {
                    @Override
                    public void accept(PlaylistItem playlistItem) {
                        mActiveItem = playlistItem;
                    }
                });
    }

    public void reset() {
        mIsFilled = false;
        mActiveItem = null;
        mItemsList.clear();
    }

    public List<SoundItem> getList() {
        return mItemsList
                .stream()
                .map(chainItem -> ((PlaylistItem) chainItem).getSoundItem())
                .collect(Collectors.toList());
    }

    public List<SoundItem> getUnshiftedList() {

        int activeItemIndex = mItemsList.indexOf(mActiveItem);

        if (activeItemIndex < 0) {
            throw new IllegalStateException("Active item not found in list.");
        }
        if (0 == activeItemIndex) {
            return getList();
        }
        else {
            return mItemsList
                    .stream()
                    .skip(activeItemIndex-1)
                    .map(chainItem -> ((PlaylistItem) chainItem).getSoundItem())
                    .collect(Collectors.toList());
        }
    }

    public boolean hasPrevItem() {
        return (null != mActiveItem && null != mActiveItem.getPrevItem());
    }

    public boolean hasNextItem() {
        return (null != mActiveItem && null != mActiveItem.getNextItem());
    }


    private static class PlaylistItem extends ChainItem {

        @NonNull private final SoundItem mSoundItem;

        public PlaylistItem(@NonNull SoundItem soundItem) {
            mSoundItem = soundItem;
        }

        @NonNull @Override
        public String getTitle() {
            return mSoundItem.getTitle();
        }

        @NonNull
        public SoundItem getSoundItem() {
            return mSoundItem;
        }
    }
}
