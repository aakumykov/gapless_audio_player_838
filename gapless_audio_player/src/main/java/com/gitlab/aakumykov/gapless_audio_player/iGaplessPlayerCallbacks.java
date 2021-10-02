package com.gitlab.aakumykov.gapless_audio_player;

import androidx.annotation.NonNull;

public interface iGaplessPlayerCallbacks {

    void onStarted(@NonNull SoundItem soundItem);
    void onStopped();

    void onPaused();
    void onResumed();

    void onNoNextTracks();
    void onNoPrevTracks();

    void onPreparingError(@NonNull SoundItem soundItem, @NonNull String errorMsg);
    void onPlayingError(@NonNull SoundItem soundItem, @NonNull String errorMsg);
    void onCommonError(@NonNull String errorMsg);
}
