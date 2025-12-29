package cuoiki.helloworldgame;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper; // Đã thêm import này
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
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

    // --- Custom Menus Views ---
    private View pauseMenuView;
    private View gameOverMenuView;

    // --- High Score UI ---
    private View highScoreView;
    private ParticleBackgroundView particleView;
    private TextView tvClassicScores, tvEndlessScores;
    private boolean isHighScoreVisible = false;

    // --- BOSS UI ---
    private View bossHudView; // View chứa thanh máu XML
    private ProgressBar pbBossHealth;
    private TextView tvBossHp;

    // --- Game Logic Components ---
    private GameView gameView;
    private RecognitionManager recognitionManager;
    private SoundManager soundManager;
    private HighScoreDbHelper dbHelper;

    // --- State Management ---
    private List<GameTheme> themes = new ArrayList<>();
    private int currentThemeIndex = 0;
    private GameMode currentGameMode = GameMode.CLASSIC;
    private int currentMaxDiff = 0;

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
        recognitionManager = new RecognitionManager();
        recognitionManager.downloadModel();
        dbHelper = new HighScoreDbHelper(this);

        initThemes();
        initHighScoreView();

        // Listeners...
        rootContainer.setOnTouchListener(new OnSwipeTouchListener(this) {
            @Override
            public void onSwipeUp() {
                if (gameContainer.getVisibility() != View.VISIBLE && !isHighScoreVisible) {
                    showHighScoreScreen();
                }
            }
            @Override
            public void onSwipeDown() { if (isHighScoreVisible) hideHighScoreScreen(); }
        });

        tvHelloWorld.setOnTouchListener(new OnSwipeTouchListener(this) {
            @Override
            public void onSwipeLeft() { changeTheme(1); }
            @Override
            public void onSwipeRight() { changeTheme(-1); }
            @Override
            public void onSwipeUp() { toggleGameMode(); }
            @Override
            public void onSwipeDown() { toggleGameMode(); }
            @Override
            public void onClick() {
                soundManager.playClick();
                startOpeningAnimation();
            }
        });

        applyThemeToMenu();
    }

    private void initHighScoreView() {
        LayoutInflater inflater = LayoutInflater.from(this);
        highScoreView = inflater.inflate(R.layout.activity_layout_high_score, rootContainer, false);
        particleView = highScoreView.findViewById(R.id.particleView);
        tvClassicScores = highScoreView.findViewById(R.id.tvClassicScores);
        tvEndlessScores = highScoreView.findViewById(R.id.tvEndlessScores);
        highScoreView.setVisibility(View.INVISIBLE);
        rootContainer.addView(highScoreView);
        highScoreView.setOnTouchListener(new OnSwipeTouchListener(this) {
            @Override
            public void onSwipeDown() { hideHighScoreScreen(); }
        });
    }

    private void showHighScoreScreen() {
        if (isHighScoreVisible) return;
        isHighScoreVisible = true;

        // Lấy theme hiện tại
        GameTheme currentTheme = themes.get(currentThemeIndex);

        // Áp dụng giao diện (Màu nền, Font chữ, Màu chữ)
        highScoreView.setBackgroundColor(currentTheme.bgColor);

        // Cập nhật Font chữ cho bảng điểm
        Typeface tf = Typeface.DEFAULT;
        if (currentTheme.fontResId != 0) {
            try {
                tf = ResourcesCompat.getFont(this, currentTheme.fontResId);
            } catch (Exception e) { e.printStackTrace(); }
        }

        TextView tvTitle = highScoreView.findViewById(R.id.tvHighScoreTile);
        TextView tvLabelClassic = highScoreView.findViewById(R.id.labelClassic); // Label "Classic Mode"
        TextView tvLabelEndless = highScoreView.findViewById(R.id.labelEndless); // Label "Endless Mode"

        // Áp dụng màu và font
        int textColor = currentTheme.textColor;
        int strokeColor = currentTheme.strokeColor; // Dùng màu stroke cho điểm nhấn

        if (tvTitle != null) {
            tvTitle.setTextColor(strokeColor);
            tvTitle.setTypeface(tf);
        }
        if (tvLabelClassic != null) {
            tvLabelClassic.setTextColor(textColor);
            tvLabelClassic.setTypeface(tf);
        }
        if (tvLabelEndless != null) {
            tvLabelEndless.setTextColor(textColor);
            tvLabelEndless.setTypeface(tf);
        }

        tvClassicScores.setTextColor(textColor);
        tvClassicScores.setTypeface(tf);

        tvEndlessScores.setTextColor(textColor);
        tvEndlessScores.setTypeface(tf);

        // Cập nhật màu cho Particle
        if (particleView != null) {
            particleView.setParticleColor(currentTheme.textColor);
        }

        // Load dữ liệu theo Theme
        loadHighScoreData(currentTheme.name);

        int screenHeight = rootContainer.getHeight();
        highScoreView.setTranslationY(screenHeight);
        highScoreView.setVisibility(View.VISIBLE);
        particleView.startAnimation();
        tvHelloWorld.animate().translationY(-screenHeight).setDuration(500).start();
        highScoreView.animate().translationY(0).setDuration(500).start();
        soundManager.playClick();
    }

    private void hideHighScoreScreen() {
        if (!isHighScoreVisible) return;
        isHighScoreVisible = false;
        int screenHeight = rootContainer.getHeight();
        tvHelloWorld.animate().translationY(0).setDuration(500).start();
        highScoreView.animate().translationY(screenHeight).setDuration(500).withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        highScoreView.setVisibility(View.INVISIBLE);
                        particleView.stopAnimation();
                    }
                })
                .start();
    }

    private void loadHighScoreData(String themeName) {
        // Lấy Classic Score của Theme này
        List<String> classicScores = dbHelper.getTopScores(GameMode.CLASSIC.toString(), themeName);
        StringBuilder sbClassic = new StringBuilder();
        for (String s : classicScores) sbClassic.append(s).append("\n");
        tvClassicScores.setText(sbClassic.toString());

        // Lấy Endless Score của Theme này
        List<String> endlessScores = dbHelper.getTopScores(GameMode.ENDLESS.toString(), themeName);
        StringBuilder sbEndless = new StringBuilder();
        for (String s : endlessScores) sbEndless.append(s).append("\n");
        tvEndlessScores.setText(sbEndless.toString());
    }

// --- XML BOSS HUD ---
    private void showBossHud(int maxHp) {
        if (bossHudView != null) return;

        LayoutInflater inflater = LayoutInflater.from(this);
        bossHudView = inflater.inflate(R.layout.layout_boss_hud, rootContainer, false);

        pbBossHealth = bossHudView.findViewById(R.id.pbBossHealth);
        tvBossHp = bossHudView.findViewById(R.id.tvBossHp);

        pbBossHealth.setMax(maxHp);
        pbBossHealth.setProgress(maxHp);
        tvBossHp.setText(maxHp + "/" + maxHp);

        rootContainer.addView(bossHudView);
    }

    private void updateBossHud(int currentHp, int maxHp) {
        if (pbBossHealth != null) {
            pbBossHealth.setProgress(currentHp, true);
            tvBossHp.setText(currentHp + "/" + maxHp);

            // Nếu Boss chết, ẩn đi
            if (currentHp <= 0) {
                hideBossHud();
            }
        }
    }

    private void hideBossHud() {
        if (bossHudView != null) {
            rootContainer.removeView(bossHudView);
            bossHudView = null;
            pbBossHealth = null;
            tvBossHp = null;
        }
    }

    private void returnToMenu() {
        hideBossHud();
        if (pauseMenuView != null) rootContainer.removeView(pauseMenuView);
        if (gameOverMenuView != null) rootContainer.removeView(gameOverMenuView);
        pauseMenuView = null;
        gameOverMenuView = null;

        gameContainer.removeAllViews();
        gameContainer.setVisibility(View.GONE);
        tvScore.setVisibility(View.GONE);
        tvDiff.setVisibility(View.GONE);
        tvHelloWorld.setVisibility(View.VISIBLE);
        tvHelloWorld.setTranslationY(0);
        applyThemeToMenu();
    }

    // --- SHOW PAUSE MENU ---
    private void showPauseMenu() {// Kiểm tra nếu đã có menu thì không tạo thêm
        if (pauseMenuView != null) {
            pauseMenuView.bringToFront(); // Đảm bảo nó nổi lên trên
            return;
        }

        LayoutInflater inflater = LayoutInflater.from(this);
        // Sử dụng rootContainer làm parent
        pauseMenuView = inflater.inflate(R.layout.layout_pause_menu, rootContainer, false);
        rootContainer.addView(pauseMenuView);
        pauseMenuView.bringToFront();

        // Ánh xạ buttons
        Button btnResume = pauseMenuView.findViewById(R.id.btnResume);
        Button btnRestart = pauseMenuView.findViewById(R.id.btnRestart);
        Button btnMenu = pauseMenuView.findViewById(R.id.btnMenu);

        // Resume Button
        btnResume.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rootContainer.removeView(pauseMenuView);
                pauseMenuView = null;
                if (gameView != null) {
                    gameView.resumeGame(); // Đảm bảo GameView đã xử lý logic isPaused = false
                }
            }
        });

        // Restart Button
        btnRestart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rootContainer.removeView(pauseMenuView);
                pauseMenuView = null;
                restartGameLogic();
            }
        });

        // Main Menu Button
        btnMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (gameView != null) {
                    gameView.pauseGame();
                }
                returnToMenu();
            }
        });
    }


    // --- SHOW GAME OVER MENU ---
    private void showGameOverDialog(boolean isWin) {
        if (gameOverMenuView != null) rootContainer.removeView(gameOverMenuView);
        LayoutInflater inflater = LayoutInflater.from(this);
        gameOverMenuView = inflater.inflate(R.layout.layout_game_over, rootContainer, false);
        rootContainer.addView(gameOverMenuView);
        TextView tvTitle = gameOverMenuView.findViewById(R.id.tvTitle);
        TextView tvCurrentResult = gameOverMenuView.findViewById(R.id.tvCurrentResult);
        TextView tvHighScoreList = gameOverMenuView.findViewById(R.id.tvHighScoreList);
        Button btnReplay = gameOverMenuView.findViewById(R.id.btnReplay);
        Button btnMenu = gameOverMenuView.findViewById(R.id.btnMenu);

        if (isWin) {
            tvTitle.setText("YOU WIN!");
            tvTitle.setTextColor(Color.GREEN);
        } else {
            tvTitle.setText("GAME OVER");
            tvTitle.setTextColor(Color.RED);
        }
        tvCurrentResult.setText("Max Difficulty: " + currentMaxDiff);

        // Replay Button
        btnReplay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rootContainer.removeView(gameOverMenuView);
                gameOverMenuView = null;
                // Xoá game cũ, init lại
                gameContainer.removeAllViews();
                initGame();
            }
        });

        // Menu Button
        btnMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                returnToMenu();
            }
        });

    }

    // Hàm hỗ trợ Restart
    private void restartGameLogic() {
        gameView.restartGame();
        soundManager.stopBackground();
        GameTheme theme = themes.get(currentThemeIndex);
        if (currentGameMode == GameMode.ENDLESS) {
            soundManager.playBackground(theme.musicEndlessId);
        } else {
            soundManager.playBackground(theme.musicResId);
        }
    }

    private void initThemes() {
        themes.add(new GameTheme("Classic", Color.WHITE, Color.BLACK, Color.RED, 0, R.raw.carefree, R.raw.azali_phase2, R.raw.pop, R.raw.pop2, R.raw.azali_phase2));
        themes.add(new GameTheme("Undertale", Color.BLACK, Color.WHITE, Color.BLUE, R.font.undertale_sans, R.raw.undertale_phase1, R.raw.undertale_phase2, R.raw.pop, R.raw.pop2_undertale, R.raw.undertale_endless));
        themes.add(new GameTheme("Joker", Color.WHITE, Color.BLACK, Color.RED, R.font.imfellenglish_regular, R.raw.joker_menu_music, R.raw.joker_menu_music, R.raw.pop, R.raw.pop2, R.raw.joker_menu_music));
    }

    private void changeTheme(int direction) {
        if (isHighScoreVisible) return;
        currentThemeIndex += direction;
        if (currentThemeIndex >= themes.size()) currentThemeIndex = 0;
        if (currentThemeIndex < 0) currentThemeIndex = themes.size() - 1;
        soundManager.playThemeChange();
        applyThemeToMenu();
    }

    private void toggleGameMode() {
        if (isHighScoreVisible) return;
        if (currentGameMode == GameMode.CLASSIC) currentGameMode = GameMode.ENDLESS;
        else currentGameMode = GameMode.CLASSIC;
        soundManager.playGameModeChange();
        applyThemeToMenu();
        String modeText = (currentGameMode == GameMode.ENDLESS) ? "ENDLESS MODE (Survival)" : "STORY MODE";
    }

    private void applyThemeToMenu() {
        GameTheme theme = themes.get(currentThemeIndex);

        soundManager.loadThemeSounds(theme);

        rootContainer.setBackgroundColor(theme.bgColor);

        tvHelloWorld.setBackground(null);
        tvHelloWorld.setText("hello world");

        Typeface tf = Typeface.DEFAULT;
        if (theme.fontResId != 0) {
            try {
                tf = ResourcesCompat.getFont(this, theme.fontResId);
            } catch (Exception e) { e.printStackTrace(); }
        }
        tvHelloWorld.setTypeface(tf);

        if (currentGameMode == GameMode.ENDLESS) {
            tvHelloWorld.setTextColor(Color.RED);
        } else {
            tvHelloWorld.setTextColor(theme.textColor);
        }
    }
    private void startOpeningAnimation() {
        // Lấy thông tin từ TextView gốc
        float originTextSize = tvHelloWorld.getTextSize();
        int originTextColor = tvHelloWorld.getCurrentTextColor();
        Typeface originTypeface = tvHelloWorld.getTypeface();
        android.text.Layout layout = tvHelloWorld.getLayout();
        String text = tvHelloWorld.getText().toString();

        // Tính toán vị trí
        int[] tvLocation = new int[2];
        tvHelloWorld.getLocationInWindow(tvLocation);

        int[] rootLocation = new int[2];
        rootContainer.getLocationInWindow(rootLocation);

        // Tính toán tọa độ ban đầu
        float tempBaseX = tvLocation[0] - rootLocation[0];
        float tempBaseY = tvLocation[1] - rootLocation[1];

        // Cộng thêm padding
        tempBaseX += tvHelloWorld.getPaddingLeft();
        tempBaseY += tvHelloWorld.getPaddingTop();

        // Tạo biến final để dùng trong Inner Class (Runnable)
        final float baseX = tempBaseX;
        final float baseY = tempBaseY;

        // Ẩn view gốc
        tvHelloWorld.setVisibility(View.INVISIBLE);

        final Random random = new Random();

        // 3. Tạo từng ký tự
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);

            if (c == ' ') continue;

            // Lấy vị trí X, Y chuẩn từ Layout
            float charX = layout.getPrimaryHorizontal(i);
            float charY = layout.getLineTop(0);

            final TextView charView = new TextView(this);
            charView.setText(String.valueOf(c));
            charView.setTextColor(originTextColor);
            charView.setTypeface(originTypeface);
            charView.setTextSize(TypedValue.COMPLEX_UNIT_PX, originTextSize);
            charView.getPaint().setFakeBoldText(tvHelloWorld.getPaint().isFakeBoldText());

            charView.setIncludeFontPadding(false);
            charView.setPadding(0, 0, 0, 0);

            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);

            // Thiết lập vị trí: Base + Offset của ký tự
            params.leftMargin = (int) (baseX + charX);
            params.topMargin = (int) (baseY + charY);
            params.gravity = Gravity.TOP | Gravity.START;

            rootContainer.addView(charView, params);

            ViewPropertyAnimator animator = charView.animate()
                    .translationY(-50f)
                    .setDuration(150)
                    .setListener(null);

            final int finalI = i;

            animator.withEndAction(new Runnable() {
                @Override
                public void run() {
                    charView.animate()
                            .translationY(rootContainer.getHeight() - baseY)
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

    // --- INIT GAME ---
    private void initGame() {
        gameContainer.setVisibility(View.VISIBLE);
        tvScore.setVisibility(View.VISIBLE);
        tvDiff.setVisibility(View.VISIBLE);

        if (currentGameMode == GameMode.ENDLESS) currentMaxDiff = 20;
        else currentMaxDiff = 0;

        final GameTheme selectedTheme = themes.get(currentThemeIndex);
        gameView = new GameView(this);
        gameView.setGameConfig(selectedTheme, currentGameMode);
        gameView.setRecognitionManager(recognitionManager);
        gameView.setSoundManager(soundManager);
        gameContainer.addView(gameView);

        if (currentGameMode == GameMode.ENDLESS) {
            soundManager.playBackground(selectedTheme.musicEndlessId);
        } else {
            soundManager.playBackground(selectedTheme.musicResId);
        }

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
            public void onGameOver() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (soundManager != null) soundManager.stopBackground();
                        if (dbHelper != null) {
                            // TRUYỀN THÊM: selectedTheme.name
                            dbHelper.addHighScore(gameView.getScore(), currentMaxDiff, currentGameMode.toString(), selectedTheme.name);
                        }
                        // Hiện Dialog thua cuộc (isWin = false)
                        showGameOverDialog(false);
                    }
                });
            }

            @Override
            public void onGameWin() {
                // XỬ LÝ KHI THẮNG JOKER MODE
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (soundManager != null) {
                            soundManager.stopBackground();
                            if (dbHelper != null) {
                                // TRUYỀN THÊM: selectedTheme.name
                                dbHelper.addHighScore(gameView.getScore(), currentMaxDiff, currentGameMode.toString(), selectedTheme.name);
                            }
                            // soundManager.playWinSound(); // Nếu có âm thanh thắng cuộc
                        }
                        showGameOverDialog(true);
                    }
                });
            }

            @Override
            public void onDiffUpdate(int diff) {
                currentMaxDiff = diff;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tvDiff.setText("Diff: " + currentMaxDiff);
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

            @Override
            public void onPauseRequest() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showPauseMenu();
                    }
                });
            }

            @Override
            public void onBossHpUpdate(int currentHp, int maxHp) {
                runOnUiThread(() -> {
                    // Nếu maxHp > 0, tức là có Boss -> Hiện HUD
                    if (maxHp > 0) {
                        if (bossHudView == null) showBossHud(maxHp); // Hiện nếu chưa có
                        updateBossHud(currentHp, maxHp); // Cập nhật
                    } else {
                        // Nếu maxHp = 0 -> Không có Boss -> Ẩn HUD
                        hideBossHud();
                    }
                });
            }
        });

    }

    private void startPhase2Effect(int musicId) {
        gameView.pauseGame();
        soundManager.stopBackground();
        soundManager.playBackground(musicId);
        final Random random = new Random();

        final Handler handler = new Handler(Looper.getMainLooper());

        handler.post(new Runnable() {
            long startTime = System.currentTimeMillis();
            @Override
            public void run() {
                if (System.currentTimeMillis() - startTime >= 3000) {
                    gameView.resumeGame();
                    return;
                }
                spawnRisingRedChar(random);
                handler.postDelayed(this, 20);
            }
        });
    }

    private void spawnRisingRedChar(Random random) {
        final TextView charView = new TextView(this);
        String chars = "!@#$%^&HELLhellHELL";
        charView.setText(String.valueOf(chars.charAt(random.nextInt(chars.length()))));
        charView.setTextSize(20);
        charView.setTypeface(null, Typeface.BOLD);
        charView.setTextColor(themes.get(currentThemeIndex).strokeColor);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.BOTTOM | Gravity.START;

        // Tính toán tọa độ X
        int leftMargin = random.nextInt(rootContainer.getWidth() - 100);
        params.leftMargin = leftMargin;
        rootContainer.addView(charView, params);

        if (gameView != null) {
            gameView.spawnExplosion(leftMargin, rootContainer.getHeight(), themes.get(currentThemeIndex).strokeColor, 10, 10);
        }

        charView.bringToFront();
        charView.setElevation(1000f);
        charView.animate().translationY(-rootContainer.getHeight()).rotation(random.nextInt(360)).setDuration(500 + random.nextInt(500)).withEndAction(() -> rootContainer.removeView(charView)).start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (soundManager != null) soundManager.pauseBackground();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (soundManager != null) soundManager.resumeBackground();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (soundManager != null) soundManager.release();
        if (recognitionManager != null) recognitionManager.close();
        if (dbHelper != null) dbHelper.close();
    }
}