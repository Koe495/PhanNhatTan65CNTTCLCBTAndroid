package cuoiki.helloworldgame;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.graphics.Color;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    // --- UI Components ---
    private FrameLayout rootContainer;
    private FrameLayout gameContainer;
    private TextView tvHelloWorld;
    private TextView tvScore;
    private TextView tvDiff;

    // --- Game Logic Components ---
    private GameView gameView;
    private RecognitionManager recognitionManager;
    private SoundManager soundManager;
    private MediaPlayer mediaPlayer;

    // --- State Management ---
    private List<GameTheme> themes = new ArrayList<>();
    private int currentThemeIndex = 0;
    private GameMode currentGameMode = GameMode.STORY;
    private boolean isPhase2MusicPlaying = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        rootContainer = findViewById(R.id.rootContainer);
        gameContainer = findViewById(R.id.gameContainer);
        tvHelloWorld = findViewById(R.id.tvHelloWorld);
        tvScore = findViewById(R.id.tvScore);
        tvDiff = findViewById(R.id.tvDiff);

        // Khởi tạo tài nguyên
        initThemes();
        soundManager = new SoundManager(this);
        recognitionManager = new RecognitionManager();
        recognitionManager.downloadModel();

        // Cài đặt tương tác vuốt
        tvHelloWorld.setOnTouchListener(new OnSwipeTouchListener(this) {
            @Override
            public void onSwipeLeft() {
                changeTheme(1);
            }

            @Override
            public void onSwipeRight() {
                changeTheme(-1);
            }

            @Override
            public void onSwipeUp() {
                toggleGameMode();
            }

            @Override
            public void onSwipeDown() {
                toggleGameMode();
            }

            @Override
            public void onClick() {
                soundManager.playClick();
                startOpeningAnimation();
            }
        });

        applyThemeToMenu();
    }

    private void initThemes() {
        // Theme 1: Classic
        themes.add(new GameTheme("Classic",
                Color.WHITE, Color.BLACK, Color.RED,
                0, // Default font
                R.raw.carefree,
                R.raw.azali_phase2,
                R.raw.pop,
                R.raw.pop2,
                R.raw.azali_phase2
        ));

        // Theme 2: Undertale
        themes.add(new GameTheme("Undertale",
                Color.BLACK, Color.WHITE, Color.BLUE,
                R.font.undertale_sans,
                R.raw.undertale_phase1,
                R.raw.undertale_phase2,
                R.raw.pop,
                R.raw.pop2_undertale,
                R.raw.undertale_endless
        ));
    }

    private void changeTheme(int direction) {
        currentThemeIndex += direction;
        if (currentThemeIndex >= themes.size()) currentThemeIndex = 0;
        if (currentThemeIndex < 0) currentThemeIndex = themes.size() - 1;

        soundManager.playThemeChange();

        applyThemeToMenu();
        Toast.makeText(this, "Theme: " + themes.get(currentThemeIndex).name, Toast.LENGTH_SHORT).show();
    }

    private void toggleGameMode() {
        if (currentGameMode == GameMode.STORY) {
            currentGameMode = GameMode.ENDLESS;
        } else {
            currentGameMode = GameMode.STORY;
        }

        soundManager.playGameModeChange();

        applyThemeToMenu();

        String modeText = (currentGameMode == GameMode.ENDLESS) ? "ENDLESS MODE (Survival)" : "STORY MODE";
        Toast.makeText(this, modeText, Toast.LENGTH_SHORT).show();
    }

    private void applyThemeToMenu() {
        GameTheme theme = themes.get(currentThemeIndex);

        soundManager.loadThemeSounds(theme);
        rootContainer.setBackgroundColor(theme.bgColor);

        // Áp dụng Font
        Typeface tf = Typeface.DEFAULT_BOLD;
        if (theme.fontResId != 0) {
            try {
                tf = ResourcesCompat.getFont(this, theme.fontResId);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        tvHelloWorld.setTypeface(tf);

        // Áp dụng màu chữ theo Mode
        if (currentGameMode == GameMode.ENDLESS) {
            tvHelloWorld.setTextColor(Color.RED);
        } else {
            tvHelloWorld.setTextColor(theme.textColor);
        }
    }

    private void startOpeningAnimation() {
        float originTextSize = tvHelloWorld.getTextSize();
        int originTextColor = tvHelloWorld.getCurrentTextColor();
        Typeface originTypeface = tvHelloWorld.getTypeface();
        float spaceWidth = tvHelloWorld.getPaint().measureText(" ");

        // Ẩn chữ gốc để thay thế bằng các chữ cái rời
        tvHelloWorld.setVisibility(View.INVISIBLE);

        String text = tvHelloWorld.getText().toString();
        int[] location = new int[2];
        tvHelloWorld.getLocationOnScreen(location);

        int currentX = location[0];
        final int startY = location[1];
        final Random random = new Random();

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);

            if (c == ' ') {
                currentX += spaceWidth;
                continue;
            }

            final TextView charView = new TextView(this);
            charView.setText(String.valueOf(c));
            charView.setTextColor(originTextColor);
            charView.setTypeface(originTypeface);
            charView.setTextSize(TypedValue.COMPLEX_UNIT_PX, originTextSize);
            charView.getPaint().setFakeBoldText(tvHelloWorld.getPaint().isFakeBoldText());
            charView.setPadding(0, 0, 0, 0);
            charView.setIncludeFontPadding(false);

            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            params.leftMargin = currentX;
            params.topMargin = startY;
            params.gravity = Gravity.TOP | Gravity.START;

            rootContainer.addView(charView, params);

            charView.measure(0, 0);
            currentX += charView.getMeasuredWidth();

            float bounceOffsetY = -50f;
            ViewPropertyAnimator animator = charView.animate()
                    .translationY(bounceOffsetY)
                    .setDuration(150)
                    .setListener(null);

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
                                    rootContainer.removeView(charView);
                                }
                            });
                }
            });
        }
    }

    private void initGame() {
        gameContainer.setVisibility(View.VISIBLE);
        tvScore.setVisibility(View.VISIBLE);
        tvDiff.setVisibility(View.VISIBLE);

        final GameTheme selectedTheme = themes.get(currentThemeIndex);
        gameView = new GameView(this);

        gameView.setGameConfig(selectedTheme, currentGameMode);
        gameView.setRecognitionManager(recognitionManager);
        gameView.setSoundManager(soundManager);

        gameContainer.addView(gameView);

        // Logic chọn nhạc
        if (currentGameMode == GameMode.ENDLESS) {
            playMusic(selectedTheme.musicEndlessId);
            isPhase2MusicPlaying = true;
        } else {
            playMusic(selectedTheme.musicResId);
            isPhase2MusicPlaying = false;
        }

        gameView.setGameOverListener(new GameView.GameOverListener() {
            @Override
            public void onScoreUpdate(final int score) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tvScore.setText("HP: " + score);
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
                        applyThemeToMenu();
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
                        applyThemeToMenu();
                    }
                });
            }

            @Override
            public void onPhase2Start() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        startPhase2Effect(selectedTheme.musicPhase2ResId);
                    }
                });
            }
        });
    }

    private void startPhase2Effect(int musicId) {
        gameView.pauseGame();
        stopMusic();
        playMusic(musicId);
        isPhase2MusicPlaying = true;

        final Random random = new Random();
        final Handler handler = new Handler();
        final long effectDuration = 3000;
        final long interval = 60;
        final long startTime = System.currentTimeMillis();

        handler.post(new Runnable() {
            @Override
            public void run() {
                if (System.currentTimeMillis() - startTime >= effectDuration) {
                    gameView.resumeGame();
                    return;
                }
                spawnRisingRedChar(random);
                handler.postDelayed(this, interval);
            }
        });
    }

    private void spawnRisingRedChar(Random random) {
        // Debug:
        android.util.Log.d("Effect", "Đang tạo chữ bay");

        final TextView charView = new TextView(this);
        String chars = "!@#$%^&HELLhellHELL";
        charView.setText(String.valueOf(chars.charAt(random.nextInt(chars.length()))));

        charView.setTextSize(20);
        charView.setTypeface(null, Typeface.BOLD);

        charView.setTextColor(themes.get(currentThemeIndex).strokeColor);

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.gravity = Gravity.BOTTOM | Gravity.START;

        // Random vị trí ngang
        if (rootContainer.getWidth() > 0) {
            params.leftMargin = random.nextInt(rootContainer.getWidth() - 100);
        }

        rootContainer.addView(charView, params);

        charView.bringToFront();

        charView.setElevation(1000f);

        // Hiệu ứng bay lên
        charView.animate()
                .translationY(-rootContainer.getHeight()) // Bay từ đáy lên đỉnh
                .rotation(random.nextInt(360))
                .setDuration(500 + random.nextInt(500))
                .withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        rootContainer.removeView(charView);
                    }
                })
                .start();
    }

    private void playMusic(int resourceId) {
        stopMusic();
        mediaPlayer = MediaPlayer.create(this, resourceId);
        if (mediaPlayer != null) {
            mediaPlayer.setLooping(true);
            mediaPlayer.start();
        }
    }

    private void stopMusic() {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.release();
            mediaPlayer = null;
        }
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
        if (soundManager != null) {
            soundManager.release();
        }
        if (recognitionManager != null) {
            recognitionManager.close();
        }
    }
}