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

import com.gitlab.aakumykov.app.databinding.ActivityWorkingExampleBinding;
import com.gitlab.aakumykov.gapless_audio_player.ErrorCode;
import com.gitlab.aakumykov.gapless_audio_player.GaplessAudioPlayer;
import com.gitlab.aakumykov.gapless_audio_player.Progress;
import com.gitlab.aakumykov.gapless_audio_player.SoundItem;
import com.gitlab.aakumykov.gapless_audio_player.iAudioPlayer;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.RuntimePermissions;

@RuntimePermissions
public class WorkingExampleActivity extends AppCompatActivity
        implements SeekBar.OnSeekBarChangeListener, iAudioPlayer.Callbacks
{
    private static final String TAG = WorkingExampleActivity.class.getSimpleName();
    private static final String EMPTY_STRING = "";
    private ActivityWorkingExampleBinding mViewBinding;
    private iAudioPlayer mAudioPlayer;
    private boolean mProgressTrackingEnabled = false;

    private final String[] mMusicList = {

            "Gazebo1.mp3",
            "Gazebo20.mp3",
            "Gazebo30.mp3",
            "Africa.mp3",
            "Rhythm.mp3",
            "Кортнев.mp3",
            "Конец Фильма - Огни.mp3"

/*            "440hz.mp3",
            "500hz.mp3",
            "600hz.mp3",
            "700hz.mp3",
            "800hz.mp3"*/
    };

    private AudioManager mAudioManager;
    private String mMusicDir;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mViewBinding = ActivityWorkingExampleBinding.inflate(getLayoutInflater());
        setContentView(mViewBinding.getRoot());

        mViewBinding.startButton.setOnClickListener(this::onPlayButtonClicked);
        mViewBinding.stopButton.setOnClickListener(this::onStopButtonClicked);
        mViewBinding.prevButton.setOnClickListener(this::onPrevButtonClicked);
        mViewBinding.nextButton.setOnClickListener(this::onNextButtonClicked);

        mViewBinding.increaseVolumeButton.setOnClickListener(this::onIncreaseVolumeButtonClicked);
        mViewBinding.decreaseVolumeButton.setOnClickListener(this::onDecreaseVolumeButtonClicked);

        mViewBinding.seekBar.setOnSeekBarChangeListener(this);
        disableSeekBar();

        mAudioPlayer = new GaplessAudioPlayer(this);

        mAudioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);

        prepareMusicDir();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        WorkingExampleActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mAudioPlayer.pause(false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mAudioPlayer.isPlaying())
            mAudioPlayer.resume();
    }


    @NeedsPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
    void playMusicList() {

        List<SoundItem> soundItemList = Stream.of(mMusicList).map(fileName ->
                new SoundItem(
                    fileName,
                    mMusicDir + "/" + fileName
                )
        ).collect(Collectors.toList());

        mAudioPlayer.play(soundItemList);
    }


    private void prepareMusicDir() {
        mMusicDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS).toString();

        mViewBinding.musicDirView.setText(
                getString(R.string.music_dir, mMusicDir)
        );
    }


    private void onPlayButtonClicked(View view) {

        if (mAudioPlayer.isInitialized()) {
            if (mAudioPlayer.isPlaying())
                mAudioPlayer.pause(true);
            else
                mAudioPlayer.resume();
        }
        else {
            WorkingExampleActivityPermissionsDispatcher.playMusicListWithPermissionCheck(this);
        }
    }

    private void onStopButtonClicked(View view) {
        if (mAudioPlayer.isInitialized())
            mAudioPlayer.stop();
    }

    private void onNextButtonClicked(View view) {
        mAudioPlayer.next();
    }

    private void onPrevButtonClicked(View view) {
        mAudioPlayer.prev();
    }

    private void onIncreaseVolumeButtonClicked(View view) {
        mAudioManager.adjustVolume(AudioManager.ADJUST_RAISE, AudioManager.FLAG_PLAY_SOUND);
        showMusicVolumeLevel();
    }

    private void onDecreaseVolumeButtonClicked(View view) {
        mAudioManager.adjustVolume(AudioManager.ADJUST_LOWER, AudioManager.FLAG_PLAY_SOUND);
        showMusicVolumeLevel();
    }

    private void showMusicVolumeLevel() {
        mViewBinding.soundVolumeView.setText(String.valueOf(mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC)));
        mViewBinding.soundVolumeView.postDelayed(this::hideMusicVolumeLevel, 1000);
    }

    private void hideMusicVolumeLevel() {
        mViewBinding.soundVolumeView.setText(EMPTY_STRING);
    }


    // iAudioPlayer.Callbacks
    @Override
    public void onStarted(@NonNull SoundItem soundItem) {
//        startProgressTracking();
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
    public void onPreparingError(@NonNull SoundItem soundItem, @NonNull String errorMsg) {
        showToast(getString(R.string.preparing_error, soundItem.getTitle()));
    }

    @Override
    public void onPlayingError(@NonNull SoundItem soundItem, @NonNull String errorMsg) {
        showToast(getString(R.string.playing_error, soundItem.getTitle(), errorMsg));
    }

    @Override
    public void onCommonError(@NonNull ErrorCode errorCode, @Nullable String errorDetails) {

        String errorMsg;

        switch (errorCode) {
            case NOTHING_TO_PLAY:
                errorMsg = getString(R.string.nothing_to_play);
                break;
            default:
                errorMsg = getString(R.string.unknown_error);
        }

        if (null != errorDetails)
            errorMsg = getString(R.string.detailed_error, errorMsg, errorDetails);

        showError(errorMsg);
    }


    // SeekBar.OnSeekBarChangeListener
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (fromUser)
            mAudioPlayer.seekTo(progress);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }


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