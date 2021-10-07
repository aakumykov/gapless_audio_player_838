package com.gitlab.aakumykov.gapless_audio_player;

import java.util.List;

public interface iAudioPlayer {

    void play(List<SoundItem> soundItemList);
    void stop();
    void pause(boolean fromUser);
    void resume();
    void next();
    void prev();
    void seekTo(int positionMilliseconds);

    boolean isInitialized();
    boolean isPlaying();

    String getTitle();
    Progress getProgress();
    SoundItem getSoundItem();

}
