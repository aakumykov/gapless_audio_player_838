package com.gitlab.aakumykov.gapless_audio_player.classes;

import androidx.annotation.NonNull;

import com.gitlab.aakumykov.gapless_audio_player.ChainItem;

public class SampleItem extends ChainItem {

    private final String mTitle;

    public SampleItem(String title) {
        mTitle = title;
    }

    @NonNull
    @Override
    public String getTitle() {
        return mTitle;
    }
}
