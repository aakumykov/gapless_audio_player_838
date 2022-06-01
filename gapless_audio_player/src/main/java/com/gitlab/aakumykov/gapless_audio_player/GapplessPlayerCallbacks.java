package com.gitlab.aakumykov.gapless_audio_player;

import androidx.annotation.NonNull;

import com.gitlab.aakumykov.gapless_audio_player.stuff.SoundItem;

public interface GapplessPlayerCallbacks {

    void onStarted(@NonNull SoundItem soundItem);

    void onStopped();

    void onPaused();

    void onResumed();

    void onProgress(int position, int duration);

    void onNoNextTracks();

    void onNoPrevTracks();

    void onNothingToPlay();

    void onPreparingError(@NonNull SoundItem soundItem, @NonNull String errorMsg);

    void onPlayingError(@NonNull SoundItem soundItem, @NonNull String errorMsg);
}
