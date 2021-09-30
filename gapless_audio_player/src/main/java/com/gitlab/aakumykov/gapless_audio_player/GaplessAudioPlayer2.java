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

public class GaplessAudioPlayer2 implements MediaPlayer.OnCompletionListener {

    private static final String TAG = GaplessAudioPlayer2.class.getSimpleName();
    private final Object SYNC_FLAG = new Object();
    private Playlist mPlaylist;
    private final List<Player> mPlayersChain = new ArrayList<>();
    @Nullable private Player mCurrentPlayer;
    private final iGaplessPlayerCallbacks mCallbacks;


    public GaplessAudioPlayer2(@NonNull iGaplessPlayerCallbacks callbacks) {
        mCallbacks = callbacks;
    }


    public void play(@NonNull List<SoundItem> soundItemList) {
        createPlaylist(soundItemList);
        playList(soundItemList);
    }

    public void pause() {
        pauseCurrentPlayer();
    }

    public void resume() {
        resumeCurrentPlayer();
    }

    public void stop() {
        stopCurrentPlayer();
        clearAllPlayers();
        clearPlaylist();
    }

    public void next() {
        if (hasNextTrack()) {
            stopCurrentPlayer();
            shiftPlayersChain();
            startCurrentPlayer();
        }
        else
            mCallbacks.onNoNextTracks();
    }

    public void prev() {
        if (hasPrevTrack()) {
            stopCurrentPlayer();
            unshiftPlayersChain();
            startCurrentPlayer();
        }
        else
            mCallbacks.onNoPrevTracks();
    }

    private boolean hasNextTrack() {
        return (null != mCurrentPlayer && null != mCurrentPlayer.getNextPlayer());
    }

    private boolean hasPrevTrack() {
        return null != mCurrentPlayer &&
                null != mCurrentPlayer.getPrevPlayer();
    }

    public void seekTo(int position) {
        if (null != mCurrentPlayer)
            mCurrentPlayer.seekTo(position);
    }

    public boolean isInitialized() {
        return null != mPlaylist;
    }

    public boolean isPlaying() {
        return (null != mCurrentPlayer && mCurrentPlayer.isPlaying());
    }

    // TODO: синхронизация
    @Nullable
    public String getTitle() {
        return (null != mCurrentPlayer) ?
                mCurrentPlayer.getSoundItem().getTitle() :
                null;
    }

    @Nullable
    public Progress getProgress() {
        synchronized (SYNC_FLAG) {
            return (null != mCurrentPlayer && !mCurrentPlayer.isStopped()) ?
                    new Progress(mCurrentPlayer.getCurrentPosition(), mCurrentPlayer.getDuration()) :
                    null;
        }
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
            mCurrentPlayer.start();
            mCallbacks.onStarted(mCurrentPlayer.getSoundItem());
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
                player.setOnCompletionListener(this);
                mPlayersChain.add(player);
            }
            catch (IOException e) {
                Log.w(TAG, ExceptionUtils.getErrorMessage(e), e);
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
            List<SoundItem> subList = mPlaylist.getUnshiftedListFrom(
                    mCurrentPlayer.getSoundItem());
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
        startCurrentPlayer();
    }

    private void setActiveList(List<SoundItem> list) {
        mPlaylist.setActiveList(list);
    }


    // MediaPlayer.OnCompletionListener
    @Override
    public void onCompletion(MediaPlayer mp) {
        Player player = (Player) mp;

        Player nextPlayer = player.getNextPlayer();

        if (null == nextPlayer)
            stop();
        else {
            mCurrentPlayer = nextPlayer;
            mCallbacks.onStarted(mCurrentPlayer.getSoundItem());
        }
    }

    private void debugLog(String text) {
        if (BuildConfig.DEBUG)
            Log.d(TAG, text);
    }
}
