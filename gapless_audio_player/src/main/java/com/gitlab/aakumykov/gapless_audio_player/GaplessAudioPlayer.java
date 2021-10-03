package com.gitlab.aakumykov.gapless_audio_player;

import android.media.MediaPlayer;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.gitlab.aakumykov.exception_utils_module.BuildConfig;
import com.gitlab.aakumykov.exception_utils_module.ExceptionUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GaplessAudioPlayer implements iAudioPlayer {

    private static final String TAG = GaplessAudioPlayer.class.getSimpleName();
    private static final int TRACK_BEGINNING_THRESHOLD_MS = 1000;
    private final Object SYNC_FLAG = new Object();
    private Playlist mPlaylist;
    private final List<Player> mPlayersChain = new ArrayList<>();
    @Nullable private Player mCurrentPlayer;
    private final MediaPlayer.OnCompletionListener mCompletionListener;
    private final iGaplessPlayerCallbacks mCallbacks;


    public GaplessAudioPlayer(@NonNull iGaplessPlayerCallbacks callbacks) {

        mCallbacks = callbacks;

        mCompletionListener = this::onAudioTrackCompleted;
    }


    @Override
    public void play(@NonNull List<SoundItem> soundItemList) {

        if (soundItemList.size() > 0) {
            createPlaylist(soundItemList);
            playList(soundItemList);
        }
        else {
            nothingToPlay();
        }
    }

    @Override
    public void pause() {
        pauseCurrentPlayer();
    }

    @Override
    public void resume() {
        resumeCurrentPlayer();
    }

    @Override
    public void stop() {
        stopCurrentPlayer();
        clearAllPlayers();
        clearPlaylist();
    }

    @Override
    public void next() {
        if (null != mCurrentPlayer) {
            if (hasNextTrack()) {
                stopCurrentPlayer();
                shiftPlayersChain();
                startCurrentPlayer();
            } else
                mCallbacks.onNoNextTracks();
        }
    }

    @Override
    public void prev() {
        if (null != mCurrentPlayer)
        {
            if (trackIsOnBeginning())
                skipToPrevTrack();
            else
                skipToTrackBeginning();
        }
    }

    @Override
    public void seekTo(int position) {
        if (null != mCurrentPlayer)
            mCurrentPlayer.seekTo(position);
    }

    @Override
    public boolean isInitialized() {
        return null != mPlaylist;
    }

    @Override
    public boolean isPlaying() {
        return (null != mCurrentPlayer && mCurrentPlayer.isPlaying());
    }

    // TODO: синхронизация
    @Override @Nullable
    public String getTitle() {
        return (null != mCurrentPlayer) ?
                mCurrentPlayer.getSoundItem().getTitle() :
                null;
    }

    @Override @Nullable
    public Progress getProgress() {
        synchronized (SYNC_FLAG) {
            return (null != mCurrentPlayer && !mCurrentPlayer.isStopped()) ?
                    new Progress(mCurrentPlayer.getCurrentPosition(), mCurrentPlayer.getDuration()) :
                    null;
        }
    }

    @Override @Nullable
    public SoundItem getSoundItem() {
        return (null != mCurrentPlayer) ?
                mCurrentPlayer.getSoundItem() :
                null;
    }



    private boolean trackIsOnBeginning() {
        if (null == mCurrentPlayer)
            throw new IllegalStateException("Плеер не инициализирован");

        int position = mCurrentPlayer.getCurrentPosition();
        debugLog("position: "+position);

        return position <= TRACK_BEGINNING_THRESHOLD_MS;
    }

    private void skipToPrevTrack() {
        if (hasPrevTrack()) {
            stopCurrentPlayer();
            unshiftPlayersChain();
            startCurrentPlayer();
        } else
            mCallbacks.onNoPrevTracks();
    }

    private void skipToTrackBeginning() {
        if (null == mCurrentPlayer)
            throw new IllegalStateException("Плеер не инициализирован");
        mCurrentPlayer.seekTo(0);
    }

    private boolean hasNextTrack() {
        return (null != mCurrentPlayer && null != mCurrentPlayer.getNextPlayer());
    }

    private boolean hasPrevTrack() {
        return null != mCurrentPlayer &&
                null != mCurrentPlayer.getPrevPlayer();
    }

    private void stopCurrentPlayer() {
        synchronized (SYNC_FLAG) {
            if (null != mCurrentPlayer) {
                mCurrentPlayer.stop();
                mCurrentPlayer.release();
                mCallbacks.onStopped();
            }
        }
    }

    private void shiftPlayersChain() {
        if (null != mCurrentPlayer)
            mCurrentPlayer = mCurrentPlayer.getNextPlayer();
    }

    private void startCurrentPlayer() {

        if (null != mCurrentPlayer) {

            @NonNull
            SoundItem soundItem = mCurrentPlayer.getSoundItem();

            try {
                mCurrentPlayer.start();
                mCallbacks.onStarted(soundItem);
            }
            catch (Exception e) {
                mCallbacks.onPlayingError(soundItem, ExceptionUtils.getErrorMessage(e));
                debugLog(e);
            }
        }
    }

    private void createPlaylist(@NonNull List<SoundItem> soundItemList) {
        mPlaylist = new Playlist(soundItemList);
    }

    private void preparePlayers() {
        createPlayers();
        linkPlayersToChain();
    }

    private void createPlayers() {
        mPlayersChain.clear();

        for (SoundItem soundItem : mPlaylist.getActiveList()) {
            try {
                Player player = new Player(soundItem);
                player.setDataSource(soundItem.getFilePath());
                player.prepare();
                player.setOnCompletionListener(mCompletionListener);
                mPlayersChain.add(player);
            }
            catch (IOException e) {
                mCallbacks.onPreparingError(soundItem, ExceptionUtils.getErrorMessage(e));
                debugLog(e);
            }
        }

        if (mPlayersChain.size() > 0)
            mCurrentPlayer = mPlayersChain.get(0);
    }

    private void clearAllPlayers() {
        for (Player player : mPlayersChain)
            player.release();
        mPlayersChain.clear();
    }

    private void clearPlaylist() {
        mPlaylist = null;
    }

    private void linkPlayersToChain() {

        int chainSize = mPlayersChain.size();

        if (chainSize < 2)
            return;

        if (2 == chainSize) {
            bindTwoChainElements(
                    mPlayersChain.get(0),
                    mPlayersChain.get(1)
            );
        }
        else {
            final int firstIndex = 0;
            final int endIndex = mPlayersChain.size() - 2;

            for (int i = firstIndex; i <= endIndex; i++) {
                Player firstPlayer = mPlayersChain.get(i);
                Player secondPlayer = mPlayersChain.get(i + 1);

                if (firstIndex == i)
                    firstPlayer.setPrevPlayer(null);

                if (endIndex == i)
                    secondPlayer.setNextPlayer(null);

                bindTwoChainElements(firstPlayer, secondPlayer);
            }
        }
    }

    private void bindTwoChainElements(Player firstPlayer, Player secondPlayer) {
        firstPlayer.setNextPlayer(secondPlayer);
        secondPlayer.setPrevPlayer(firstPlayer);

        firstPlayer.setNextMediaPlayer(secondPlayer);
    }

    private void unshiftPlayersChain() {
        if (null != mCurrentPlayer) {
            SoundItem currentSoundItem = mCurrentPlayer.getSoundItem();
            List<SoundItem> subList = mPlaylist.getUnshiftedListFrom(currentSoundItem);
            playList(subList);
        }
    }

    private void resumeCurrentPlayer() {
        if (null != mCurrentPlayer) {
            mCurrentPlayer.start();
            mCallbacks.onResumed();
        }
    }

    private void pauseCurrentPlayer() {
        if (null != mCurrentPlayer) {
            mCurrentPlayer.pause();
            mCallbacks.onPaused();
        }
    }

    private void playList(List<SoundItem> list) {

        setActiveList(list);
        preparePlayers();

        if (mPlayersChain.size() > 0)
            startCurrentPlayer();
        else
            nothingToPlay();
    }

    private void setActiveList(List<SoundItem> list) {
        mPlaylist.setActiveList(list);
    }


    // Обработчик MediaPlayer.OnCompletionListener
    private void onAudioTrackCompleted(@NonNull MediaPlayer mediaPlayer) {

        Player player = (Player) mediaPlayer;

        Player nextPlayer = player.getNextPlayer();

        if (null == nextPlayer)
            stop();
        else {
            mCurrentPlayer = nextPlayer;
            mCallbacks.onStarted(mCurrentPlayer.getSoundItem());
        }
    }


    // Вспомогательные методы
    private void debugLog(String text) {
        if (BuildConfig.DEBUG)
            Log.d(TAG, text);
    }

    private void debugLog(Throwable t) {
        if (BuildConfig.DEBUG)
            Log.e(TAG, ExceptionUtils.getErrorMessage(t), t);
    }

    private void nothingToPlay() {
        mCallbacks.onCommonError(ErrorCode.NOTHING_TO_PLAY, null);
    }
}
