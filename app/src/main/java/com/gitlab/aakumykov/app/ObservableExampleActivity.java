package com.gitlab.aakumykov.app;

import android.Manifest;
import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.gitlab.aakumykov.app.databinding.ActivityDemoBinding;
import com.gitlab.aakumykov.gapless_audio_player.GaplessAudioPlayer;
import com.gitlab.aakumykov.gapless_audio_player.GaplessPlayerState;
import com.gitlab.aakumykov.gapless_audio_player.stuff.Progress;
import com.gitlab.aakumykov.gapless_audio_player.stuff.SoundItem;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import io.reactivex.disposables.CompositeDisposable;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.RuntimePermissions;

@RuntimePermissions
public class ObservableExampleActivity extends AppCompatActivity
        implements SeekBar.OnSeekBarChangeListener
{
    private static final String EMPTY_STRING = "";
    private ActivityDemoBinding mViewBinding;
    private GaplessAudioPlayer mGaplessAudioPlayer;
    private boolean mProgressTrackingEnabled = false;


    private AudioManager mAudioManager;
    private final CompositeDisposable mCompositeDisposable = new CompositeDisposable();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mViewBinding = ActivityDemoBinding.inflate(getLayoutInflater());
        setContentView(mViewBinding.getRoot());

        mViewBinding.demoLabelView.setText(R.string.observable_example);

        mViewBinding.startButton.setOnClickListener(this::onPlayButtonClicked);
        mViewBinding.stopButton.setOnClickListener(this::onStopButtonClicked);
        mViewBinding.prevButton.setOnClickListener(this::onPrevButtonClicked);
        mViewBinding.nextButton.setOnClickListener(this::onNextButtonClicked);

        mViewBinding.increaseVolumeButton.setOnClickListener(this::onIncreaseVolumeButtonClicked);
        mViewBinding.decreaseVolumeButton.setOnClickListener(this::onDecreaseVolumeButtonClicked);

        mViewBinding.seekBar.setOnSeekBarChangeListener(this);
        disableSeekBar();

        mGaplessAudioPlayer = new GaplessAudioPlayer();

        mAudioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);

        subscribeToAudioPlayer();
        prepareMusicDir();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mCompositeDisposable.clear();
    }

    private void subscribeToAudioPlayer() {
        mCompositeDisposable.add(
                mGaplessAudioPlayer.getPlayerStateObservable().subscribe(this::onNewPlayerState)
        );
    }

    @Override
    protected void onPause() {
        super.onPause();
        mGaplessAudioPlayer.pause(false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mGaplessAudioPlayer.isPlaying())
            mGaplessAudioPlayer.resume();
    }



    private void onPlayButtonClicked(View view) {

        if (mGaplessAudioPlayer.isInitialized()) {
            if (mGaplessAudioPlayer.isPlaying())
                mGaplessAudioPlayer.pause(true);
            else
                mGaplessAudioPlayer.resume();
        }
        else {
//            playMusicList();
            ObservableExampleActivityPermissionsDispatcher.playMusicListWithPermissionCheck(this);
        }
    }

    private void onStopButtonClicked(View view) {
        if (mGaplessAudioPlayer.isInitialized())
            mGaplessAudioPlayer.stop();
    }

    private void onNextButtonClicked(View view) {
        mGaplessAudioPlayer.next();
    }

    private void onPrevButtonClicked(View view) {
        mGaplessAudioPlayer.prev();
    }

    private void onIncreaseVolumeButtonClicked(View view) {
        mAudioManager.adjustVolume(AudioManager.ADJUST_RAISE, AudioManager.FLAG_PLAY_SOUND);
        showMusicVolumeLevel();
    }

    private void onDecreaseVolumeButtonClicked(View view) {
        mAudioManager.adjustVolume(AudioManager.ADJUST_LOWER, AudioManager.FLAG_PLAY_SOUND);
        showMusicVolumeLevel();
    }


    // Запрос разрешений
    // TODO: реагировать на отказ
    @NeedsPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
    void playMusicList() {

        String dirName = Environment
                .getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                .getAbsolutePath();

        @Nullable
        File[] mp3files = new File(dirName).listFiles(file -> {
            String fileName = file.getName();
            return fileName.matches("^.+\\.mp3$");
        });

        List<SoundItem> soundItemList = new ArrayList<>();

        if (null != mp3files) {
            for (File soundFile : mp3files) {
                String fileName = soundFile.getName();
                soundItemList.add(new SoundItem(
                        UUID.randomUUID().toString(),
                        fileName,
                        dirName + "/" + fileName
                ));
            }
        }

        mGaplessAudioPlayer.play(soundItemList);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        ObservableExampleActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }



    private void onNewPlayerState(GaplessPlayerState playerState) {

        switch (playerState.mode) {
            case INACTIVE:
                break;

            case STARTED:
                onStarted((GaplessPlayerState.Started) playerState);
                break;

            case STOPPED:
                onStopped();
                break;

            case PAUSED:
                onPaused();
                break;

            case RESUMED:
                onResumed();
                break;

            case NO_NEXT_TRACK:
                onNoNextTrack();
                break;

            case NO_PREV_TRACK:
                onNoPrevTrack();
                break;

            case NOTHING_TO_PLAY:
                onNothingToPlay();
                break;

            case PREPARING_ERROR:
                onPreparingError((GaplessPlayerState.PreparingError) playerState);
                break;

            case PLAYING_ERROR:
                onPlayingError((GaplessPlayerState.PlayingError) playerState);
                break;

            default:
                throw new IllegalArgumentException("Unknown player state: "+playerState);
        }
    }


    private void onStarted(GaplessPlayerState.Started startedPlayerState) {
        startProgressTracking();
        showPauseButton();
        hideError();
        showTrackName(startedPlayerState.getSoundItem());
        enableSeekBar();
    }

    private void onStopped() {
        stopProgressTracking();
        showPlayButton();
        hideTrackName();
        disableSeekBar();
    }

    private void onPaused() {
        showPlayButton();
    }

    private void onResumed() {
        showPauseButton();
    }

    private void onNoNextTrack() {
        showToast(getString(R.string.no_more_tracks));
    }

    private void onNoPrevTrack() {
        showToast(getString(R.string.this_is_first_track));
    }

    private void onNothingToPlay() {
        showToast(getString(R.string.nothing_to_play));
        showError(getString(R.string.nothing_to_play));
    }

    private void onPreparingError(GaplessPlayerState.PreparingError preparingErrorPlayerState) {
        String errorMsg = getString(
                R.string.preparing_error,
                preparingErrorPlayerState.getSoundItem().getTitle()
        );
        showToast(errorMsg);
        showError(errorMsg);
    }

    private void onPlayingError(GaplessPlayerState.PlayingError playingErrorPlayerState) {
        String errorMsg = getString(R.string.playing_error,
                playingErrorPlayerState.getSoundItem().getTitle(),
                playingErrorPlayerState.getErrorMessage()
        );
        showToast(errorMsg);
        showError(errorMsg);
    }
    

    // SeekBar.OnSeekBarChangeListener
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (fromUser)
            mGaplessAudioPlayer.seekTo(progress);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }



    // Разные внутренние методы
    private void prepareMusicDir() {

        String musicDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS).toString();

        mViewBinding.musicDirView.setText(
                getString(R.string.music_dir, musicDir)
        );
    }


    private void showMusicVolumeLevel() {
        mViewBinding.soundVolumeView.setText(String.valueOf(mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC)));
        mViewBinding.soundVolumeView.postDelayed(this::hideMusicVolumeLevel, 1000);
    }

    private void hideMusicVolumeLevel() {
        mViewBinding.soundVolumeView.setText(EMPTY_STRING);
    }


    private void startProgressTracking() {
        mProgressTrackingEnabled = true;
        trackProgress();
    }

    private void stopProgressTracking() {
        mProgressTrackingEnabled = false;
    }

    private void trackProgress() {

        Progress progress = mGaplessAudioPlayer.getProgress();

        if (null != progress) {
            mViewBinding.seekBar.setProgress(progress.getPosition());
            mViewBinding.seekBar.setMax(progress.getDuration());
        }

        if (mProgressTrackingEnabled)
            getWindow().getDecorView().postDelayed(this::trackProgress, 100);
    }


    private void showToast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }

    private void showTrackName(@NonNull SoundItem soundItem) {
        mViewBinding.trackNameView.setText(soundItem.getTitle());
    }

    private void hideTrackName() {
        mViewBinding.trackNameView.setText(EMPTY_STRING);
    }

    private void showError(@NonNull String errorMsg) {
        mViewBinding.errorView.setText(errorMsg);
        mViewBinding.errorView.setVisibility(View.VISIBLE);
    }

    private void hideError() {
        mViewBinding.errorView.setText(EMPTY_STRING);
    }


    private void showPlayButton() {
        mViewBinding.startButton.setImageResource(R.drawable.ic_play);
    }

    private void showPauseButton() {
        mViewBinding.startButton.setImageResource(R.drawable.ic_pause);
    }

    private void enableSeekBar() {
        mViewBinding.seekBar.setEnabled(true);
    }

    private void disableSeekBar() {
        mViewBinding.seekBar.setProgress(0);
        mViewBinding.seekBar.setEnabled(false);
    }
}