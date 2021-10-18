package com.gitlab.aakumykov.gapless_audio_player;

import static org.junit.Assert.assertFalse;

import android.content.Context;

import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.Arrays;
import java.util.List;

public class GaplessAudioPlayerTest {

    private iAudioPlayer mAudioPlayer;
    @Mock private iAudioPlayer.Callbacks mCallbacksMock;

    private static final String TITLE1 = "Музыка1";
    private static final String TITLE2 = "Музыка2";

    private static final String FILE_PATH1 = "/path/to/file/1.mp3";
    private static final String FILE_PATH2 = "/path/to/file/2.mp3";

    private SoundItem mSoundItem0;
    private final SoundItem mSoundItem1 = new SoundItem(TITLE1, FILE_PATH1);
    private final SoundItem mSoundItem2 = new SoundItem(TITLE2, FILE_PATH2);

    private final List<SoundItem> mSoundItemList = Arrays.asList(
            mSoundItem1,
            mSoundItem2
    );


    @Before
    public void setUp() throws Exception {
        mAudioPlayer = new GaplessAudioPlayer(mCallbacksMock);

        Context context = InstrumentationRegistry.getInstrumentation().getContext();
        String cacheDir = context.getCacheDir().getAbsolutePath();
        String filesDir = context.getFilesDir().getAbsolutePath();

        String fileName = "ogni.mp3";

        String filePath0 = cacheDir + "/" + fileName;

        mSoundItem0 = new SoundItem("Огни", filePath0);
    }

    @After
    public void tearDown() throws Exception {
        mAudioPlayer.stop();
    }

    
    @Test
    public void play() {
        assertFalse(mAudioPlayer.isInitialized());


//        mAudioPlayer.play(Collections.singletonList(mSoundItem0));

//        Mockito.verify(mCallbacksMock).onStarted(mSoundItem1);
    }

    @Test
    public void pause() {
    }

    @Test
    public void resume() {
    }

    @Test
    public void stop() {
    }

    @Test
    public void next() {
    }

    @Test
    public void prev() {
    }

    @Test
    public void seekTo() {
    }

    @Test
    public void isInitialized() {
    }

    @Test
    public void isPlaying() {
    }

    @Test
    public void getTitle() {
    }

    @Test
    public void getProgress() {
    }

    @Test
    public void getSoundItem() {
    }
}