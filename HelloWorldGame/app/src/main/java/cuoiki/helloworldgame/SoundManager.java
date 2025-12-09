package cuoiki.helloworldgame;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.util.Log;

import java.util.HashMap; // Dùng Map để quản lý trạng thái load từng file

public class SoundManager {
    private SoundPool soundPool;
    private int soundClick;
    private int soundExplodeNormal;
    private int soundExplodeTarget;

    // Biến kiểm tra từng âm thanh đã load xong chưa
    private boolean isClickReady = false;
    private boolean isNormalReady = false;
    private boolean isTargetReady = false;

    public SoundManager(Context context) {
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();

        soundPool = new SoundPool.Builder()
                .setMaxStreams(10)
                .setAudioAttributes(audioAttributes)
                .build();

        // Nạp file và Log ra ID để kiểm tra
        soundClick = soundPool.load(context, R.raw.click, 1);
        Log.d("AUDIO_DEBUG", "ID Click: " + soundClick);

        soundExplodeNormal = soundPool.load(context, R.raw.pop, 1);
        Log.d("AUDIO_DEBUG", "ID Pop Normal: " + soundExplodeNormal);

        // --- KIỂM TRA KỸ DÒNG NÀY ---
        soundExplodeTarget = soundPool.load(context, R.raw.pop2, 1);
        Log.d("AUDIO_DEBUG", "ID Pop Target (pop2): " + soundExplodeTarget);

        // Lắng nghe sự kiện load xong từng file
        soundPool.setOnLoadCompleteListener((soundPool, sampleId, status) -> {
            if (status == 0) {
                // Status 0 nghĩa là thành công
                if (sampleId == soundClick) isClickReady = true;
                if (sampleId == soundExplodeNormal) isNormalReady = true;
                if (sampleId == soundExplodeTarget) {
                    isTargetReady = true;
                    Log.d("AUDIO_DEBUG", "Pop2 đã tải xong và sẵn sàng!");
                }
            } else {
                Log.e("AUDIO_DEBUG", "Lỗi tải âm thanh ID: " + sampleId + " - Mã lỗi: " + status);
            }
        });
    }

    public void playClick() {
        if (isClickReady) {
            soundPool.play(soundClick, 1.0f, 1.0f, 1, 0, 1.0f);
        }
    }

    public void playExplodeNormal() {
        if (isNormalReady) {
            soundPool.play(soundExplodeNormal, 0.6f, 0.6f, 1, 0, 1.0f);
        }
    }

    public void playExplodeTarget() {
        // Log để xem hàm này có được gọi không
        Log.d("AUDIO_DEBUG", "Gọi lệnh playExplodeTarget. Ready? " + isTargetReady);

        if (isTargetReady) {
            soundPool.play(soundExplodeTarget, 1.0f, 1.0f, 1, 0, 1.0f);
        } else {
            Log.e("AUDIO_DEBUG", "Pop2 chưa sẵn sàng hoặc file bị lỗi!");
        }
    }

    public void release() {
        if (soundPool != null) {
            soundPool.release();
            soundPool = null;
        }
    }
}