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
    private final Playlist mPlaylist = new Playlist();
    private final List<Player> mPlayersChain = new ArrayList<>();
    @Nullable private Player mCurrentPlayer;
    private final MediaPlayer.OnCompletionListener mCompletionListener;
    private final iGaplessPlayerCallbacks mCallbacks;
    private boolean mIsInitialized = false;
    private boolean mIsPlaying = false;

    public GaplessAudioPlayer(@NonNull iGaplessPlayerCallbacks callbacks) {

        mCallbacks = callbacks;

        mCompletionListener = this::onAudioTrackCompleted;
    }


    @Override
    public void play(@NonNull List<SoundItem> soundItemList) {

        if (soundItemList.size() > 0)
            playList(soundItemList);
        else
            nothingToPlay();
    }

    @Override
    public void pause(boolean fromUser) {
        pauseCurrentPlayer();

        if (fromUser)
            mIsPlaying = false;
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
    public synchronized void next() {
        if (null != mCurrentPlayer)
        {
            if (mPlaylist.hasNextItem()) {
                release();
                shift();
                start();
            }
            else
                mCallbacks.onNoNextTracks();
        }
    }

    @Override
    public synchronized void prev() {
        if (null != mCurrentPlayer)
        {
            if (trackIsOnBeginning())
                skipToPrevTrack();
            else
                skipToTrackBeginning();
        }
    }

    @Override
    public synchronized void seekTo(int position) {
        if (null != mCurrentPlayer)
            mCurrentPlayer.seekTo(position);
    }

    @Override
    public synchronized boolean isInitialized() {
        return mIsInitialized;
    }

    @Override
    public synchronized boolean isPlaying() {
//        return (null != mCurrentPlayer && mCurrentPlayer.isPlaying());
        return mIsPlaying;
    }

    @Override @Nullable
    public synchronized String getTitle() {
        return (null != mCurrentPlayer) ?
                mCurrentPlayer.getSoundItem().getTitle() :
                null;
    }

    @Override @Nullable
    public synchronized Progress getProgress() {
        return (null != mCurrentPlayer && !mCurrentPlayer.isStopped()) ?
                new Progress(mCurrentPlayer.getCurrentPosition(), mCurrentPlayer.getDuration()) :
                null;
    }

    @Override @Nullable
    public synchronized SoundItem getSoundItem() {
        return (null != mCurrentPlayer) ?
                mCurrentPlayer.getSoundItem() :
                null;
    }


    private synchronized void start() {
        if (null != mCurrentPlayer) {

                @NonNull SoundItem soundItem = mCurrentPlayer.getSoundItem();

                mPlaylist.setActiveItem(soundItem);

                try {
                    mCurrentPlayer.start();
                    mIsPlaying = true;
                    mCallbacks.onStarted(soundItem);
                }
                catch (Exception e) {
                    mCallbacks.onPlayingError(soundItem, ExceptionUtils.getErrorMessage(e));
                    debugLog(e);
                }
            }
    }

    private synchronized void release() {
        if (null != mCurrentPlayer) {
            mCurrentPlayer.release();
            mIsPlaying = false;
            mCallbacks.onStopped();
        }
    }

    private synchronized boolean trackIsOnBeginning() {

        if (null == mCurrentPlayer)
            throw new IllegalStateException("Плеер не инициализирован");

        int position = mCurrentPlayer.getCurrentPosition();
        debugLog("position: "+position);

        return position <= TRACK_BEGINNING_THRESHOLD_MS;
    }

    private synchronized void skipToPrevTrack() {
        if (mPlaylist.hasPrevItem()) {
            release();
            unshift();
            start();
        }
        else {
            mCallbacks.onNoPrevTracks();
        }
    }

    private synchronized void skipToTrackBeginning() {
        if (null == mCurrentPlayer)
            throw new IllegalStateException("Плеер не инициализирован");
        mCurrentPlayer.seekTo(0);
    }

    private synchronized void stopCurrentPlayer() {
        if (null != mCurrentPlayer) {
                mCurrentPlayer.stop();
                mCurrentPlayer.release();
                mCurrentPlayer = null;

                mIsInitialized = false;
                mIsPlaying = false;
                mCallbacks.onStopped();
            }
    }

    private void prepareTracksAndPlayers(@NonNull List<SoundItem> soundItemsList) {
        createPlayersAndFillPlaylist(soundItemsList);
        linkPlayersToChain();
    }

    private void createPlayersAndFillPlaylist(@NonNull List<SoundItem> soundItemsList) {

        mPlayersChain.clear();

        for (SoundItem soundItem : soundItemsList) {
            try {
                Player player = new Player(soundItem);
                player.setDataSource(soundItem.getFilePath());
                player.prepare();
                player.setOnCompletionListener(mCompletionListener);
                mPlayersChain.add(player);
                mPlaylist.addIfNotFilled(soundItem);
            }
            catch (IOException e) {
                mCallbacks.onPreparingError(soundItem, ExceptionUtils.getErrorMessage(e));
                debugLog(e);
            }
        }

        mPlaylist.markAsFilled();

        mIsInitialized = true;

        if (mPlayersChain.size() > 0)
            mCurrentPlayer = mPlayersChain.get(0);
    }

    private void clearAllPlayers() {
        for (Player player : mPlayersChain)
            player.release();
        mPlayersChain.clear();
    }

    private void clearPlaylist() {
        mPlaylist.reset();
    }

    private synchronized void shift() {
        if (null != mCurrentPlayer) {
            mCurrentPlayer = mCurrentPlayer.getNextPlayer();
        }
    }

    private synchronized void unshift() {
        if (null != mCurrentPlayer) {
            List<SoundItem> subList = mPlaylist.getUnshiftedList();
            playList(subList);
        }
    }

    private synchronized void resumeCurrentPlayer() {
        if (null != mCurrentPlayer) {
            mCurrentPlayer.start();
            mIsPlaying = true;
            mCallbacks.onResumed();
        }
    }

    private synchronized void  pauseCurrentPlayer() {
        if (null != mCurrentPlayer) {
            mCurrentPlayer.pause();
            mCallbacks.onPaused();
        }
    }

    private void playList(List<SoundItem> list) {

        prepareTracksAndPlayers(list);

        if (mPlayersChain.size() > 0)
            start();
        else
            nothingToPlay();
    }


    // Объединение плееров в цепочку
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



    // Обработчик MediaPlayer.OnCompletionListener
    private void onAudioTrackCompleted(@NonNull MediaPlayer mediaPlayer) {

        Player player = (Player) mediaPlayer;

        Player nextPlayer = player.getNextPlayer();

        if (null == nextPlayer)
            stop();
        else {
            mCurrentPlayer = nextPlayer;
            mPlaylist.setActiveItem(mCurrentPlayer.getSoundItem());
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
