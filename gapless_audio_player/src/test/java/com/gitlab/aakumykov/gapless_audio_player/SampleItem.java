package com.gitlab.aakumykov.gapless_audio_player;

import androidx.annotation.NonNull;

class SampleItem extends ChainItem {

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
