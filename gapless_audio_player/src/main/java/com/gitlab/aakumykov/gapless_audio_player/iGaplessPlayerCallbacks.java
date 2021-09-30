package com.gitlab.aakumykov.gapless_audio_player;

import androidx.annotation.NonNull;

public interface iGaplessPlayerCallbacks {
    void onStarted(@NonNull SoundItem soundItem);
    void onStopped();
    void onPaused();
    void onResumed();
    void onError(@NonNull String errorMsg);

    void onNoNextTracks();
    void onNoPrevTracks();
}
