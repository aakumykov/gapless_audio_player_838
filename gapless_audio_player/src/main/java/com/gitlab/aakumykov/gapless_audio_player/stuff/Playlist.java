package com.gitlab.aakumykov.gapless_audio_player.stuff;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class Playlist {

    private final List<PlaylistItem> mItemsList = new ArrayList<>();
    private boolean mIsFirstFill = true;
    @Nullable private PlaylistItem mActiveItem;


    public Playlist() {

    }


    public void addIfFirstFill(@NonNull SoundItem soundItem) throws IllegalStateException {
        if (mIsFirstFill)
            mItemsList.add(new PlaylistItem(soundItem));
        else
            throw new IllegalStateException("Плейлист уже закрыт для добавления элементов.");
    }

    public void finishCreation() {
        mIsFirstFill = false;
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

        if (null == mActiveItem)
            throw new IllegalStateException("Во внутренем списке воспроизведения не найден " +
                    "текущий активный элемент. Список: " + mItemsList +
                    "Активный элемент, который там должен быть: "+ mActiveItem);
    }

    // TODO: убрать, это используется только в тесте
    @Nullable
    public SoundItem getActiveItem() {
        return (null != mActiveItem) ?
                mActiveItem.getSoundItem() :
                null;
    }

    // TODO: убрать, это используется только в тесте
    public boolean isFirstFill() {
        return mIsFirstFill;
    }

    public void reset() {
        mIsFirstFill = true;
        mActiveItem = null;
        mItemsList.clear();
    }

    public List<SoundItem> getList() {
        return mItemsList
                .stream()
                .map(PlaylistItem::getSoundItem)
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
                    .map(PlaylistItem::getSoundItem)
                    .collect(Collectors.toList());
        }
    }

    public boolean hasPrevItem() {
        if (null == mActiveItem)
            throw new IllegalStateException("Не установлен активный элемент плейлиста.");

        return null != mActiveItem.getPrevItem();
    }

    public boolean hasNextItem() {
        if (null == mActiveItem)
            throw new IllegalStateException("Не установлен активный элемент плейлиста.");

        return null != mActiveItem.getNextItem();
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
