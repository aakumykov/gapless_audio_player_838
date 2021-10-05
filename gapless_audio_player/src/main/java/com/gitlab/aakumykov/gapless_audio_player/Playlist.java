package com.gitlab.aakumykov.gapless_audio_player;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Playlist {

    private final List<SoundItem> mOriginalItemsList;
    private final List<SoundItem> mCurrentItemsList = new ArrayList<>();


    public Playlist(List<SoundItem> originalItemsList) {
        mOriginalItemsList = new ArrayList<>(originalItemsList);
    }

    public void setActiveList(List<SoundItem> list) {
        mCurrentItemsList.clear();
        mCurrentItemsList.addAll(list);
    }

    public List<SoundItem> getActiveList() {
        return mCurrentItemsList;
    }

    public List<SoundItem> getUnshiftedListFrom(SoundItem soundItem) {

        int listSize = mOriginalItemsList.indexOf(soundItem);

        int prevItemIndex = listSize - 1;

        if (prevItemIndex < 0)
            prevItemIndex = 0;

        if (prevItemIndex > 0)
            return mOriginalItemsList.stream().skip(listSize-prevItemIndex).collect(Collectors.toList());
        else
            return new ArrayList<>(mOriginalItemsList);
    }

    public boolean hasPrevItemFrom(@Nullable SoundItem currentSoundItem) {
        if (null == currentSoundItem)
            return false;
        return mOriginalItemsList.indexOf(currentSoundItem) - 1 >= 0;
    }
}
