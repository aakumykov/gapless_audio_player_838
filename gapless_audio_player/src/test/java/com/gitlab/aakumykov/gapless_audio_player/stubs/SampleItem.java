package com.gitlab.aakumykov.gapless_audio_player.stubs;

import androidx.annotation.NonNull;

import com.gitlab.aakumykov.gapless_audio_player.stuff.ChainItem;

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
