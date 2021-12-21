package com.gitlab.aakumykov.gapless_audio_player;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

public interface iAudioPlayer {

    void play(List<SoundItem> soundItemList);
    void play(@NonNull SoundItem soundItem);
    void stop();
    void pause(boolean fromUser);
    void resume();
    void next();
    void prev();
    void seekTo(int positionMilliseconds);

    boolean isInitialized();
    boolean isPlaying();

    @Nullable String getTitle();
    @Nullable Progress getProgress();
    @Nullable SoundItem getSoundItem();

    interface Callbacks {

        void onStarted(@NonNull SoundItem soundItem);
        void onStopped();

        void onPaused();
        void onResumed();

        void onProgress(int position, int duration);

        void onNoNextTracks();
        void onNoPrevTracks();

        void onPreparingError(@NonNull SoundItem soundItem, @NonNull String errorMsg);
        void onPlayingError(@NonNull SoundItem soundItem, @NonNull String errorMsg);
        void onCommonError(@NonNull ErrorCode errorCode, @Nullable String errorDetails);
    }
}
