package cuoiki.helloworldgame;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.media.SoundPool;

public class SoundManager {
    private SoundPool soundPool;
    private MediaPlayer backgroundPlayer; // Dùng để phát nhạc nền
    private Context context;

    // SFX IDs
    private int soundClickId;
    private int soundExplodeNormalId;
    private int soundExplodeTargetId;
    private int soundThemeChangeId;
    private int soundGameModeChangeId;
    private int soundPagesFlipId;
    private int soundBookCloseId;
    private int soundHealId;
    private int soundQTEAppearId;
    private int soundFiringId;
    private int jokerCardDrawId;
    private int jokerCardRemoveId;
    private int jokerCardComboId;
    private boolean isJokerSoundLoaded = false;

    public SoundManager(Context context) {
        this.context = context;
        initSoundPool();

        soundClickId = soundPool.load(context, R.raw.click, 1);
        soundThemeChangeId = soundPool.load(context, R.raw.select, 1);
        soundGameModeChangeId = soundPool.load(context, R.raw.select, 1);
        soundPagesFlipId = soundPool.load(context,R.raw.pages_flip, 1);
        soundBookCloseId = soundPool.load(context, R.raw.book_close, 1);
        soundHealId = soundPool.load(context, R.raw.heal, 1);
        soundQTEAppearId = soundPool.load(context, R.raw.undertale_create_qte, 1);
        soundFiringId = soundPool.load(context, R.raw.blaster_firing, 1);
    }

    private void initSoundPool() {
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();

        soundPool = new SoundPool.Builder()
                .setMaxStreams(10)
                .setAudioAttributes(audioAttributes)
                .build();
    }

    public void loadThemeSounds(GameTheme theme) {
        if (soundExplodeNormalId != 0) soundPool.unload(soundExplodeNormalId);
        if (soundExplodeTargetId != 0) soundPool.unload(soundExplodeTargetId);

        soundExplodeNormalId = soundPool.load(context, theme.sfxNormalId, 1);
        soundExplodeTargetId = soundPool.load(context, theme.sfxTargetId, 1);
    }
    public void loadJokerSounds() {
        if (isJokerSoundLoaded) return; // Tránh load lại nhiều lần

        jokerCardDrawId = soundPool.load(context, R.raw.card_draw, 1);
        jokerCardRemoveId = soundPool.load(context, R.raw.card_remove, 1);
        jokerCardComboId = soundPool.load(context, R.raw.card_combo, 1);

        isJokerSoundLoaded = true;
    }

    // --- BACKGROUND MUSIC CONTROL ---

    public void playBackground(int resId) {
        stopBackground();
        backgroundPlayer = MediaPlayer.create(context, resId);
        if (backgroundPlayer != null) {
            backgroundPlayer.setLooping(true); // Nhạc nền luôn lặp lại
            backgroundPlayer.start();
        }
    }

    public void pauseBackground() {
        if (backgroundPlayer != null && backgroundPlayer.isPlaying()) {
            backgroundPlayer.pause();
        }
    }

    public void resumeBackground() {
        if (backgroundPlayer != null && !backgroundPlayer.isPlaying()) {
            // Chỉ resume nếu player còn tồn tại (chưa bị release)
            backgroundPlayer.start();
        }
    }

    public void stopBackground() {
        if (backgroundPlayer != null) {
            if (backgroundPlayer.isPlaying()) {
                backgroundPlayer.stop();
            }
            backgroundPlayer.release();
            backgroundPlayer = null;
        }
    }

    // --- SFX METHODS ---

    public void playClick() {
        if (soundClickId != 0) {
            soundPool.play(soundClickId, 1f, 1f, 0, 0, 1f);
        }
    }

    public void playThemeChange() {
        if (soundThemeChangeId != 0) {
            soundPool.play(soundThemeChangeId, 1f, 1f, 1, 0, 1f);
        }
    }

    public void playGameModeChange() {
        if (soundGameModeChangeId != 0) {
            soundPool.play(soundGameModeChangeId, 1f, 1f, 1, 0, 1f);
        }
    }

    public void playPagesFlip() {
        if (soundPagesFlipId != 0){
            soundPool.play(soundPagesFlipId, 1f, 1f, 1, 0, 1f);
        }
    }

    public void playBookClose() {
        if (soundBookCloseId != 0) {
            soundPool.play(soundBookCloseId, 1f, 1f, 1, 0, 1f);
        }
    }

    // --- NORMAL / TARGET)

    public void playExplodeNormal() {
        if (soundExplodeNormalId != 0) {
            soundPool.play(soundExplodeNormalId, 0.8f, 0.8f, 1, 0, 1f + (float)(Math.random() * 0.2));
        }
    }
    public void playHeal() {
        if (soundHealId != 0) {
            soundPool.play(soundHealId, 1f, 1f, 1, 0, 1f);
        }
    }

    public void playExplodeTarget() {
        if (soundExplodeTargetId != 0) {
            soundPool.play(soundExplodeTargetId, 1f, 1f, 1, 0, 1f);
        }
    }

    // --- BLASTER / QTE SOUNDS ---
    public void playBlasterAppear() {
        if (soundQTEAppearId != 0) {
            soundPool.play(soundQTEAppearId, 0.8f, 0.8f, 1, 0, 1f);
        }
    }

    public void playBlasterFire() {
        if (soundFiringId != 0) {
            soundPool.play(soundFiringId, 1f, 1f, 2, 0, 1f);
        }
    }

    // --- CARD SFX PLAYERS ---
    public void playCardDraw() {
        if (jokerCardDrawId != 0) soundPool.play(jokerCardDrawId, 1f, 1f, 0, 0, 1f);
    }

    public void playCardRemove() {
        if (jokerCardRemoveId != 0) soundPool.play(jokerCardRemoveId, 0.8f, 0.8f, 0, 0, 1f);
    }

    public void playCardCombo() {
        if (jokerCardComboId != 0) soundPool.play(jokerCardComboId, 1f, 1f, 0, 0, 1f);
    }

    public void release() {
        if (soundPool != null) {
            soundPool.release();
            soundPool = null;
        }
        stopBackground();
    }
}