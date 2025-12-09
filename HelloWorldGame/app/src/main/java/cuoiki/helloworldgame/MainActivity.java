package cuoiki.helloworldgame;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import java.time.format.TextStyle;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private GameView gameView;
    private RecognitionManager recognitionManager;
    private MediaPlayer mediaPlayer;
    private SoundManager soundManager;
    private boolean isPhase2MusicPlaying = false;
    private FrameLayout rootContainer;
    private FrameLayout gameContainer;
    private TextView tvHelloWorld;
    private TextView tvScore;
    private TextView tvDiff;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.activity_main);
        rootContainer = findViewById(R.id.rootContainer);
        gameContainer = findViewById(R.id.gameContainer);
        tvHelloWorld = findViewById(R.id.tvHelloWorld);
        tvScore = findViewById(R.id.tvScore);
        tvDiff = findViewById(R.id.tvDiff);

        soundManager = new SoundManager(this);

        tvHelloWorld.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // [MỚI] Phát tiếng click
                soundManager.playClick();

                startOpeningAnimation();
            }
        });

        recognitionManager = new RecognitionManager();
        recognitionManager.downloadModel();
    }
    // Hàm phát nhạc
    private void playMusic(int resourceId) {
        // Nếu đang có nhạc thì dừng và giải phóng
        stopMusic();

        // Tạo media player mới
        mediaPlayer = MediaPlayer.create(this, resourceId);
        mediaPlayer.setLooping(true); // Lặp lại nhạc
        mediaPlayer.start();
    }

    // Hàm dừng nhạc
    private void stopMusic() {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
    private void startOpeningAnimation() {
        // Ẩn chữ gốc
        tvHelloWorld.setVisibility(View.INVISIBLE);

        String text = tvHelloWorld.getText().toString();
        int[] location = new int[2];
        tvHelloWorld.getLocationOnScreen(location);

        int currentX = location[0];
        final int startY = location[1];

        final Random random = new Random();

        // Loop tạo từng chữ cái giả
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == ' ') {
                currentX += 13; // Skip dấu cách
                continue;
            }

            final TextView charView = new TextView(this);
            charView.setText(String.valueOf(c));
            charView.setTextSize(20);
            charView.setTextColor(Color.BLACK);

            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            params.leftMargin = currentX;
            params.topMargin = startY;
            params.gravity = Gravity.TOP | Gravity.START;

            rootContainer.addView(charView, params);

            // Đo độ rộng từng chữ
            charView.measure(0, 0);
            currentX += charView.getMeasuredWidth(); // Tăng x bằng với độ rộng chữ vừa đo

            float bounceOffsetY = -50f;

            // 1. Animation nảy
            ViewPropertyAnimator animator = charView.animate()
                    .translationY(bounceOffsetY)
                    .setDuration(150)
                    .setListener(null);

            // 2. Animation rơi
            final int finalI = i;

            animator.withEndAction(new Runnable() {
                @Override
                public void run() {
                    charView.animate()
                            .translationY(rootContainer.getHeight() - startY)
                            .rotation(random.nextInt(360))
                            .alpha(0)
                            .setDuration(850 + random.nextInt(500))
                            .setListener(new AnimatorListenerAdapter() {
                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    super.onAnimationEnd(animation);
                                    if (finalI == text.length() - 1) {
                                        initGame();
                                    }
                                    // Tiêu huỷ hàng giả sau khi dùng
                                    rootContainer.removeView(charView);
                                }
                            });
                }
            });
        }
    }


    // UI GAME
    private void initGame() {
        gameContainer.setVisibility(View.VISIBLE);
        tvScore.setVisibility(View.VISIBLE);
        tvDiff.setVisibility(View.VISIBLE);

        gameView = new GameView(this);
        // Truyền AI vào GameView
        gameView.setRecognitionManager(recognitionManager);

        gameView.setSoundManager(soundManager);
        gameContainer.addView(gameView);

        playMusic(R.raw.carefree);
        isPhase2MusicPlaying = false; // Reset

        gameView.setGameOverListener(new GameView.GameOverListener() {
            @Override
            public void onScoreUpdate(final int score) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tvScore.setText("Score: " + score);
                    }
                });
            }

            @Override
            public void onDiffUpdate(final int diff) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tvDiff.setText("Diff: " + diff);
                    }
                });
            }

            @Override
            public void onGameOver() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        stopMusic();
                        Toast.makeText(MainActivity.this, "GAME OVER!", Toast.LENGTH_LONG).show();
                        gameContainer.removeAllViews();
                        tvHelloWorld.setVisibility(View.VISIBLE);
                    }
                });
            }

            @Override
            public void onGameWin() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "YOU WIN!", Toast.LENGTH_LONG).show();
                        gameContainer.removeAllViews();
                        tvHelloWorld.setVisibility(View.VISIBLE);
                        tvHelloWorld.setText("YOU WIN!");
                    }
                });
            }
            @Override
            public void onPhase2Start() {
                Log.d("PHASE2", "Triggered");
                runOnUiThread(this::startPhase2Effect);
            }

            private void startPhase2Effect() {
                // Tạm dừng game logic
                gameView.pauseGame();


                // Đổi nhac
                stopMusic();
                playMusic(R.raw.azali_phase2);
                isPhase2MusicPlaying = true;

                final Random random = new Random();

                // Spawn hiệu ứng trong 3 giây
                long effectDuration = 3500;
                long interval = 60; // spawn mỗi 60ms một chữ

                final Handler handler = new Handler();
                final long startTime = System.currentTimeMillis();

                Runnable spawnTask = new Runnable() {
                    @Override
                    public void run() {
                        long elapsed = System.currentTimeMillis() - startTime;

                        if (elapsed >= effectDuration) {
                            // Kết thúc phase effect
                            gameView.resumeGame();
                            return;
                        }

                        spawnRisingRedChar(random);
                        handler.postDelayed(this, interval);
                    }
                };
                handler.post(spawnTask);
            }
        });
    }
    private void spawnRisingRedChar(Random random) {
        final TextView charView = new TextView(this);

        char randomChar = "!@#$%^&HELLhellHELL".charAt(random.nextInt("!@#$%^&HELLhellHELL".length()));

        charView.setText(String.valueOf(randomChar));
        charView.setTextSize(32);
        charView.setTextColor(Color.RED);
        charView.setTypeface(null, android.graphics.Typeface.BOLD);

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );

        // Xuất hiện từ đáy màn hình
        params.leftMargin = random.nextInt(rootContainer.getWidth() - 50) + 25;
        params.topMargin = rootContainer.getHeight() - 50;

        rootContainer.addView(charView, params);

        charView.bringToFront();
        charView.setElevation(100f);

        charView.animate()
                .translationY(-rootContainer.getHeight())  // BAY NGƯỢC LÊN
                .rotation(random.nextInt(360))
                .setDuration(700 + random.nextInt(300))
                .alpha(0)
                .withEndAction(() -> rootContainer.removeView(charView));
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
            mediaPlayer.start();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopMusic();
        if (recognitionManager != null) {
            recognitionManager.close();
        }
    }
}