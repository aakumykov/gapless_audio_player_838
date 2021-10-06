package com.gitlab.aakumykov.gapless_audio_player;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Playlist {

    private final List<SoundItem> mOriginalItemsList = new ArrayList<>();
    private boolean mIsFilled = false;


    public Playlist() {

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
        int prevItemIndex = mOriginalItemsList.indexOf(currentSoundItem) - 1;
        return prevItemIndex >= 0;
    }

    public void addAtFirstTime(@NonNull SoundItem soundItem) {
        if (!mIsFilled)
            mOriginalItemsList.add(soundItem);
    }

    public void markAsFilled() {
        mIsFilled = true;
    }
}
