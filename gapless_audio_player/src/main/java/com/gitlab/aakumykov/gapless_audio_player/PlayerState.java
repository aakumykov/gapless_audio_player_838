package com.gitlab.aakumykov.gapless_audio_player;

public enum PlayerState {
    INACTIVE,

    STARTED,
    PAUSED,
    RESUMED,
    STOPPED,

    NO_NEXT_TRACK,
    NO_PREV_TRACK,

    PREPARING_ERROR,
    PLAYING_ERROR,
    COMMON_ERROR
}
