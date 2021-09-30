package com.gitlab.aakumykov.gapless_audio_player;

import android.media.MediaPlayer;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class Player extends MediaPlayer {

    @NonNull private final SoundItem mSoundItem;
    @Nullable private Player mNextPlayer;
    @Nullable private Player mPrevPlayer;
    private ePlayerState mPlayerState = ePlayerState.STOPPED;


    public Player(@NonNull SoundItem soundItem) {
        mSoundItem = soundItem;
    }

    @Override
    public void start() throws IllegalStateException {
        if (!isPlaying()) {
            super.start();
            mPlayerState = ePlayerState.PLAYING;
        }
    }

    @Override
    public void stop() throws IllegalStateException {
        if (!isStopped()) {
            super.stop();
            mPlayerState = ePlayerState.STOPPED;
        }
    }

    @Override
    public void pause() throws IllegalStateException {
        if (!isPaused()) {
            super.pause();
            mPlayerState = ePlayerState.PAUSED;
        }
    }


    public boolean isPlaying() {
        return ePlayerState.PLAYING.equals(mPlayerState);
    }

    public boolean isPaused() {
        return ePlayerState.PAUSED.equals(mPlayerState);
    }

    public boolean isStopped() {
        return ePlayerState.STOPPED.equals(mPlayerState);
    }


    public void setNextPlayer(@Nullable Player player) {
        mNextPlayer = player;
    }

    @Nullable
    public Player getNextPlayer() {
        return mNextPlayer;
    }


    public void setPrevPlayer(@Nullable Player player) {
        mPrevPlayer = player;
    }

    @Nullable
    public Player getPrevPlayer() {
        return mPrevPlayer;
    }


    @NonNull
    public SoundItem getSoundItem() {
        return mSoundItem;
    }


    @NonNull @Override
    public String toString() {

        String prevName = (null != mPrevPlayer) ?
                mPrevPlayer.getSoundItem().getTitle() :
                "NULL";

        String nextName = (null != mNextPlayer) ?
                mNextPlayer.getSoundItem().getTitle() :
                "NULL";

        return Player.class.getSimpleName() + " { " +
                getSoundItem().getTitle() +
                ", prev: " + prevName +
                ", next: " + nextName +
                " }";
    }


    private enum ePlayerState {
        PLAYING,
        PAUSED,
        STOPPED
    }
}
