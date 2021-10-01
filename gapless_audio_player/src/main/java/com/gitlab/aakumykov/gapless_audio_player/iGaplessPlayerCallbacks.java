package com.gitlab.aakumykov.gapless_audio_player;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public interface iGaplessPlayerCallbacks {

    void onStarted(@NonNull SoundItem soundItem);
    void onStopped();

    void onPaused();
    void onResumed();

    void onNoNextTracks();
    void onNoPrevTracks();

    void onError(int errorCode, @NonNull String errorMsg, @NonNull SoundItem soundItem);
}
