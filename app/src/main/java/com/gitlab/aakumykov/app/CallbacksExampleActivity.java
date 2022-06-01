package com.gitlab.aakumykov.app;

import android.Manifest;
import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.gitlab.aakumykov.app.databinding.ActivityDemoBinding;
import com.gitlab.aakumykov.gapless_audio_player.GaplessAudioPlayer;
import com.gitlab.aakumykov.gapless_audio_player.GaplessPlayerCallbacks;
import com.gitlab.aakumykov.gapless_audio_player.stuff.SoundItem;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.RuntimePermissions;

@RuntimePermissions
public class CallbacksExampleActivity extends AppCompatActivity
        implements SeekBar.OnSeekBarChangeListener, GaplessPlayerCallbacks
{
    private static final String EMPTY_STRING = "";
    private ActivityDemoBinding mViewBinding;
    private GaplessAudioPlayer mGaplessAudioPlayer;
//    private boolean mProgressTrackingEnabled = false;


    private AudioManager mAudioManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mViewBinding = ActivityDemoBinding.inflate(getLayoutInflater());
        setContentView(mViewBinding.getRoot());

        mViewBinding.demoLabelView.setText(R.string.callbacks_example);

        mViewBinding.startButton.setOnClickListener(this::onPlayButtonClicked);
        mViewBinding.stopButton.setOnClickListener(this::onStopButtonClicked);
        mViewBinding.prevButton.setOnClickListener(this::onPrevButtonClicked);
        mViewBinding.nextButton.setOnClickListener(this::onNextButtonClicked);

        mViewBinding.increaseVolumeButton.setOnClickListener(this::onIncreaseVolumeButtonClicked);
        mViewBinding.decreaseVolumeButton.setOnClickListener(this::onDecreaseVolumeButtonClicked);

        mViewBinding.seekBar.setOnSeekBarChangeListener(this);

        disableSeekBar();

        mGaplessAudioPlayer = new GaplessAudioPlayer(this);

        mAudioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);

        prepareMusicDir();
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
            CallbacksExampleActivityPermissionsDispatcher.playMusicListWithPermissionCheck(this);
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
        CallbacksExampleActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }



    // iAudioPlayer.Callbacks
    @Override
    public void onStarted(@NonNull SoundItem soundItem) {
        showPauseButton();
        hideError();
        showTrackName(soundItem);
        enableSeekBar();
    }

    @Override
    public void onStopped() {
        showPlayButton();
        hideTrackName();
        disableSeekBar();
    }

    @Override
    public void onPaused() {
        showPlayButton();
    }

    @Override
    public void onResumed() {
        showPauseButton();
    }

    @Override
    public void onProgress(int position, int duration) {
        Log.d("PROGRESS", String.valueOf(position));
        mViewBinding.seekBar.setMax(duration);
        mViewBinding.seekBar.setProgress(position);
    }

    @Override
    public void onNoNextTracks() {
        showToast(getString(R.string.no_more_tracks));
    }

    @Override
    public void onNoPrevTracks() {
        showToast(getString(R.string.this_is_first_track));
    }

    @Override
    public void onNothingToPlay() {
        showToast(getString(R.string.nothing_to_play));
        showError(getString(R.string.nothing_to_play));
    }

    @Override
    public void onPreparingError(@NonNull SoundItem soundItem, @NonNull String errorMsg) {
        showToast(getString(R.string.preparing_error, soundItem.getTitle()));
        showError(errorMsg);
    }

    @Override
    public void onPlayingError(@NonNull SoundItem soundItem, @NonNull String errorMsg) {
        showToast(getString(R.string.playing_error, soundItem.getTitle(), errorMsg));
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


/*
    private void startProgressTracking() {
        mProgressTrackingEnabled = true;
        trackProgress();
    }

    private void stopProgressTracking() {
        mProgressTrackingEnabled = false;
    }

    private void trackProgress() {

        Progress progress = mAudioPlayer.getProgress();

        if (null != progress) {
            mViewBinding.seekBar.setProgress(progress.getPosition());
            mViewBinding.seekBar.setMax(progress.getDuration());
        }

        if (mProgressTrackingEnabled)
            getWindow().getDecorView().postDelayed(this::trackProgress, 100);
    }
*/


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