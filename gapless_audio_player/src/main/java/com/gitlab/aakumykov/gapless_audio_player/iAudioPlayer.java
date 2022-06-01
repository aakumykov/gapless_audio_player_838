package com.gitlab.aakumykov.gapless_audio_player;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

import io.reactivex.Observable;

public interface iAudioPlayer {

    Observable<GaplessPlayerState> getPlayerStateObservable();

    void play(@NonNull List<SoundItem> soundItemList);
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

}
