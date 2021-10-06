package com.gitlab.aakumykov.gapless_audio_player;

import androidx.annotation.NonNull;

public class SoundItem {

    @NonNull private final String mTitle;
    @NonNull private final String mFilePath;
    private boolean mIsCorrupted;

    public SoundItem(@NonNull String title, @NonNull String filePath) {
        this.mTitle = title;
        this.mFilePath = filePath;
    }

    @NonNull
    public String getFilePath() {
        return mFilePath;
    }

    @NonNull
    public String getTitle() {
        return mTitle;
    }

//    @NonNull @Override
//    public String toString() {
//        return SoundItem.class.getSimpleName() + " { " +
//                "title: " + mTitle + ", " +
//                "filePath: " + mFilePath +
//                " }";
//    }



    public void markIsCorrupted() {
        mIsCorrupted = true;
    }

    public boolean isCorrupted() {
        return mIsCorrupted;
    }
}
