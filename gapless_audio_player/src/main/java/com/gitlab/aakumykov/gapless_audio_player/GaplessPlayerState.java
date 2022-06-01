package com.gitlab.aakumykov.gapless_audio_player;

import com.gitlab.aakumykov.exception_utils_module.ExceptionUtils;

public class GaplessPlayerState {

    public final Mode mode;


    public GaplessPlayerState(Mode mode) {
        this.mode = mode;
    }

    public static class Inactive extends GaplessPlayerState {
        public Inactive() {
            super(Mode.INACTIVE);
        }
    }

    public static class Started extends GaplessPlayerState {

        private final SoundItem mSoundItem;

        public Started(SoundItem soundItem) {
            super(Mode.STARTED);
            mSoundItem = soundItem;
        }

        public SoundItem getSoundItem() {
            return mSoundItem;
        }
    }

    public static class Stopped extends GaplessPlayerState {
        public Stopped() {
            super(Mode.STOPPED);
        }
    }

    public static class Paused extends GaplessPlayerState {
        public Paused() {
            super(Mode.PAUSED);
        }
    }

    public static class Resumed extends GaplessPlayerState {
        public Resumed() {
            super(Mode.RESUMED);
        }
    }

    public static class NoNextTrack extends GaplessPlayerState {
        public NoNextTrack() {
            super(Mode.NO_NEXT_TRACK);
        }
    }

    public static class NoPrevTrack extends GaplessPlayerState {
        public NoPrevTrack() {
            super(Mode.NO_PREV_TRACK);
        }
    }

    public static class NothingToPlay extends GaplessPlayerState {
        public NothingToPlay() {
            super(Mode.NOTHING_TO_PLAY);
        }
    }

    public static class PreparingError extends ErrorPlayerState {

        private final SoundItem mSoundItem;

        public PreparingError(Exception e, SoundItem soundItem) {
            super(Mode.PREPARING_ERROR, e);
            mSoundItem = soundItem;
        }

        public SoundItem getSoundItem() {
            return mSoundItem;
        }
    }

    public static class PlayingError extends ErrorPlayerState {

        private final SoundItem mSoundItem;

        public PlayingError(Exception e, SoundItem soundItem) {
            super(Mode.PLAYING_ERROR, e);
            mSoundItem = soundItem;
        }

        public SoundItem getSoundItem() {
            return mSoundItem;
        }
    }



    private static class ErrorPlayerState extends GaplessPlayerState {

        private final Exception mException;

        public ErrorPlayerState(Mode mode, Exception e) {
            super(mode);
            mException = e;
        }

        public Exception getException() {
            return mException;
        }

        public String getErrorMessage() {
            return ExceptionUtils.getErrorMessage(mException);
        }
    }


    public enum Mode {
        INACTIVE,

        STARTED,
        STOPPED,

        PAUSED,
        RESUMED,

        NO_NEXT_TRACK,
        NO_PREV_TRACK,
        NOTHING_TO_PLAY,

        PREPARING_ERROR,
        PLAYING_ERROR,
    }
}
