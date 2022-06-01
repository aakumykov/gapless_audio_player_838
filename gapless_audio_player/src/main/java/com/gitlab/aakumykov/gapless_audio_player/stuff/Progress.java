package com.gitlab.aakumykov.gapless_audio_player.stuff;

public class Progress {

    private final int mPosition;
    private final int mDuration;

    public Progress(int mPosition, int mDuration) {
        this.mPosition = mPosition;
        this.mDuration = mDuration;
    }

    public int getPosition() {
        return mPosition;
    }

    public int getDuration() {
        return mDuration;
    }
}
