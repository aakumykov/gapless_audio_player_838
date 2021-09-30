package com.gitlab.aakumykov.gapless_audio_player;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Playlist {

    private final List<SoundItem> mOriginalItemsList;
    private final List<SoundItem> mCurrentItemsList = new ArrayList<>();
    private final List<SoundItem> mSoundItemList = new ArrayList<>();

    public Playlist(List<SoundItem> originalItemsList) {
        mOriginalItemsList = new ArrayList<>(originalItemsList);
    }

    public void add(@NonNull SoundItem soundItem) {
        mSoundItemList.add(soundItem);
    }

    public void setActiveList(List<SoundItem> list) {
        mCurrentItemsList.clear();
        mCurrentItemsList.addAll(list);
    }

    public List<SoundItem> getActiveList() {
        return mCurrentItemsList;
    }

    public List<SoundItem> getUnshiftedListFrom(SoundItem soundItem) {

        int listSize = mSoundItemList.indexOf(soundItem);

        int prevItemIndex = listSize - 1;

        if (prevItemIndex < 0)
            prevItemIndex = 0;

        if (prevItemIndex > 0)
            return mOriginalItemsList.stream().skip(listSize-prevItemIndex).collect(Collectors.toList());
        else
            return new ArrayList<>(mOriginalItemsList);
    }
}
