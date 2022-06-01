package com.gitlab.aakumykov.gapless_audio_player;

import android.media.MediaPlayer;
import android.util.Log;
import android.util.Pair;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.gitlab.aakumykov.exception_utils_module.ExceptionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;

import io.reactivex.Observable;
import io.reactivex.subjects.BehaviorSubject;

public class GaplessAudioPlayer {

    private static final String TAG = GaplessAudioPlayer.class.getSimpleName();
    private static final int TRACK_BEGINNING_THRESHOLD_MS = 1000;
    private static final long PROGRESS_UPDATE_PERIOD_MS = 100;

    private final Playlist mPlaylist = new Playlist();

    private final MediaPlayer.OnCompletionListener mCompletionListener;
    @Nullable private GapplessPlayerCallbacks mCallbacks;

    private final List<Player> mPlayersChain = new ArrayList<>();
    @Nullable private Player mCurrentPlayer;

    private boolean mIsInitialized = false;
    private boolean mIsPlaying = false;
    private Timer mTimer;
    private TimerTask mTimerTask;

    @Nullable private BehaviorSubject<GaplessPlayerState> mPlayerStateSubject;
    @Nullable private BehaviorSubject<Pair<Integer,Integer>> mProgressSubject;


    public GaplessAudioPlayer() {
        mCompletionListener = this::onAudioTrackCompleted;
    }

    public GaplessAudioPlayer(@NonNull GapplessPlayerCallbacks callbacks) {

        mCallbacks = callbacks;

        mCompletionListener = this::onAudioTrackCompleted;
    }


    public Observable<GaplessPlayerState> getPlayerStateObservable() {
        mPlayerStateSubject = BehaviorSubject.createDefault(new GaplessPlayerState.Inactive());
        return mPlayerStateSubject;
    }

    public void play(@NonNull List<SoundItem> soundItemList) {
        playList(soundItemList);
    }

    public void play(@NonNull SoundItem soundItem) {
        play(Collections.singletonList(soundItem));
    }

    public void pause(boolean fromUser) {
        pauseCurrentPlayer();

        if (fromUser)
            mIsPlaying = false;
    }

    public void resume() {
        resumeCurrentPlayer();
    }

    public void stop() {
        stopCurrentPlayer();
        clearAllPlayers();
        clearPlaylist();
    }

    public synchronized void next() {
        if (null != mCurrentPlayer)
        {
            if (mPlaylist.hasNextItem()) {
                releaseCurrentPlayer();
                shift();
                start();
            }
            else {
                Optional.ofNullable(mCallbacks).ifPresent(GapplessPlayerCallbacks::onNoNextTracks);
                setPlayerState(new GaplessPlayerState.NoNextTrack());
            }
        }
    }

    public synchronized void prev() {
        if (null != mCurrentPlayer)
        {
            if (!isPlaying() || trackIsOnBeginning())
                skipToPrevTrack();
            else
                skipToTrackBeginning();
        }
    }

    public synchronized void seekTo(int position) {
        if (null != mCurrentPlayer)
            mCurrentPlayer.seekTo(position);
    }

    public synchronized boolean isInitialized() {
        return mIsInitialized;
    }

    public synchronized boolean isPlaying() {
//        return (null != mCurrentPlayer && mCurrentPlayer.isPlaying());
        return mIsPlaying;
    }

    @Nullable
    public synchronized String getTitle() {
        return (null != mCurrentPlayer) ?
                mCurrentPlayer.getSoundItem().getTitle() :
                null;
    }

    // TODO: убрать, чтобы избежать двойного отслеживания...
    @Nullable
    public synchronized Progress getProgress() {
        return (null != mCurrentPlayer && mCurrentPlayer.isNotStopped()) ?
                new Progress(mCurrentPlayer.getCurrentPosition(), mCurrentPlayer.getDuration()) :
                null;
    }

    @Nullable
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

                    Optional.ofNullable(mCallbacks).ifPresent(callbacks ->
                            callbacks.onStarted(soundItem));

                    setPlayerState(new GaplessPlayerState.Started(soundItem));

                    startProgressTracking();
                }
                catch (Exception e) {
                    Optional.ofNullable(mCallbacks).ifPresent(callbacks ->
                            callbacks.onPlayingError(soundItem, ExceptionUtils.getErrorMessage(e)));

                    setPlayerState(new GaplessPlayerState.PlayingError(e, mCurrentPlayer.getSoundItem()));

                    Log.e(TAG, "Ошибка воспроизведения", e);
                }
            }
    }

    private void setPlayerState(GaplessPlayerState state) {
        if (null != mPlayerStateSubject)
            mPlayerStateSubject.onNext(state);
    }

    private synchronized void releaseCurrentPlayer() {
        if (null != mCurrentPlayer) {
            mCurrentPlayer.release();
            mIsPlaying = false;
        }
    }

    private synchronized boolean trackIsOnBeginning() {

        if (null == mCurrentPlayer)
            throw new IllegalStateException("Плеер не инициализирован");

        return mCurrentPlayer.getCurrentPosition() <= TRACK_BEGINNING_THRESHOLD_MS;
    }

    private synchronized void skipToPrevTrack() {
        if (mPlaylist.hasPrevItem()) {
            releaseCurrentPlayer();
            List<SoundItem> unshiftedList = mPlaylist.getUnshiftedList();
            playList(unshiftedList);
        }
        else {
            Optional.ofNullable(mCallbacks).ifPresent(GapplessPlayerCallbacks::onNoPrevTracks);
            setPlayerState(new GaplessPlayerState.NoPrevTrack());
        }
    }

    private synchronized void skipToTrackBeginning() {
        if (null == mCurrentPlayer)
            throw new IllegalStateException("Плеер не инициализирован");
        mCurrentPlayer.seekTo(0);
    }

    private synchronized void stopCurrentPlayer() {
        if (null != mCurrentPlayer) {
                stopProgressTracking();

                mCurrentPlayer.stop();
                mCurrentPlayer.release();
                mCurrentPlayer = null;

                mIsInitialized = false;
                mIsPlaying = false;

                Optional.ofNullable(mCallbacks).ifPresent(GapplessPlayerCallbacks::onStopped);

                setPlayerState(new GaplessPlayerState.Stopped());
            }
    }

    private void prepareTracksAndPlayers(@NonNull List<SoundItem> soundItemsList) {
        createPlayersAndFillPlaylist(soundItemsList);
        linkPlayersToChain();
    }

    private void createPlayersAndFillPlaylist(@NonNull List<SoundItem> soundItemsList) {

        mPlayersChain.clear();

        Player player = null;

        for (SoundItem soundItem : soundItemsList) {
            try {
                player = new Player(soundItem);

                player.setDataSource(soundItem.getFilePath());
                player.prepare();

                player.setOnCompletionListener(mCompletionListener);

                mPlayersChain.add(player);

                /* Первичное заполнение плейлиста идёт параллельно
                   с созданием плееров для списка треков, которые сейчас
                   будут проигрываться. Поэтому учитывается флаг.
                 */
                if (mPlaylist.isFirstFill())
                    mPlaylist.addIfFirstFill(soundItem);
            }
            catch (Exception e) {
                if (null != player)
                    player.release();

                Optional.ofNullable(mCallbacks).ifPresent(callbacks ->
                    callbacks.onPreparingError(soundItem, ExceptionUtils.getErrorMessage(e)));

                setPlayerState(new GaplessPlayerState.PreparingError(e, soundItem));

                Log.e(TAG, "Ошибка обработки трека: "+soundItem, e);
            }
        }

        if (mPlayersChain.size() > 0) {
            mPlaylist.finishCreation();
            mCurrentPlayer = mPlayersChain.get(0);
            mIsInitialized = true;
        }
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

    private synchronized void resumeCurrentPlayer() {
        if (null != mCurrentPlayer) {
            mCurrentPlayer.start();
            mIsPlaying = true;

            Optional.ofNullable(mCallbacks).ifPresent(GapplessPlayerCallbacks::onResumed);
            setPlayerState(new GaplessPlayerState.Resumed());

            startProgressTracking();
        }
    }

    private synchronized void  pauseCurrentPlayer() {
        if (null != mCurrentPlayer) {
            mCurrentPlayer.pause();

            Optional.ofNullable(mCallbacks).ifPresent(GapplessPlayerCallbacks::onPaused);
            setPlayerState(new GaplessPlayerState.Paused());

            stopProgressTracking();
        }
    }

    private void playList(List<SoundItem> list) {

        prepareTracksAndPlayers(list);

        if (mPlayersChain.size() > 0)
            start();
        else {
            Optional.ofNullable(mCallbacks).ifPresent(GapplessPlayerCallbacks::onNothingToPlay);
            setPlayerState(new GaplessPlayerState.NothingToPlay());
        }
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


    // Методы отслеживания прогресса
    private void startProgressTracking() {

/*
        mTimerTask = new TimerTask() {
                    public void run() {
                trackProgress();
            }
        };

        mTimer = new Timer();

        mTimer.scheduleAtFixedRate(mTimerTask, 0, PROGRESS_UPDATE_PERIOD_MS);
*/
    }

    private synchronized void trackProgress() {

        if (!isPlaying()) {
            Log.w(TAG, "Плеер не играет.");
            return;
        }

        if (null == mCurrentPlayer) {
            Log.e(TAG, "mCurrentPlayer is null");
            return;
        }

        Optional.ofNullable(mCallbacks).ifPresent(callbacks ->
            callbacks.onProgress(
                mCurrentPlayer.getCurrentPosition(),
                mCurrentPlayer.getDuration()
        ));
    }

    private void stopProgressTracking() {
        if (null != mTimerTask)
            mTimerTask.cancel();
    }


    // Обработчик MediaPlayer.OnCompletionListener
    private void onAudioTrackCompleted(@NonNull MediaPlayer mediaPlayer) {

        Player player = (Player) mediaPlayer;

        Player nextPlayer = player.getNextPlayer();

        if (null == nextPlayer)
            stop();
        else {
            stopProgressTracking();

            mCurrentPlayer = nextPlayer;
            mPlaylist.setActiveItem(mCurrentPlayer.getSoundItem());

            Optional.ofNullable(mCallbacks).ifPresent(callbacks ->
                callbacks.onStarted(mCurrentPlayer.getSoundItem()));

            setPlayerState(new GaplessPlayerState.Started(mCurrentPlayer.getSoundItem()));

            startProgressTracking();
        }
    }
}
