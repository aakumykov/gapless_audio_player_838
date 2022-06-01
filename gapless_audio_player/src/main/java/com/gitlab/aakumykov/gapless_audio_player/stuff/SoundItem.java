package com.gitlab.aakumykov.gapless_audio_player.stuff;

import androidx.annotation.NonNull;

public class SoundItem {

    @NonNull private final String mId;
    @NonNull private final String mTitle;
    @NonNull private final String mFilePath;

    public SoundItem(@NonNull String id, @NonNull String title, @NonNull String filePath) {
        mId = id;
        mTitle = title;
        mFilePath = filePath;
    }

    @NonNull
    public String getId() {
        return mId;
    }

    @NonNull
    public String getFilePath() {
        return mFilePath;
    }

    @NonNull
    public String getTitle() {
        return mTitle;
    }

    @Override
    public String toString() {
        return "SoundItem{" +
                "mId='" + mId + '\'' +
                ", mTitle='" + mTitle + '\'' +
                ", mFilePath='" + mFilePath + '\'' +
                '}';
    }
}
