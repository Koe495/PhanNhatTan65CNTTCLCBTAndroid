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
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
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

    // --- JOKER BOSS UI (XML) ---
    private View bossHudView;
    private ProgressBar pbBossHealth;
    private TextView tvBossHp;

    // --- JOKER GAMEPLAY UI ---
    private View jokerGameplayView;
    private LinearLayout layoutHistoryContainer;
    private LinearLayout layoutHandContainer;
    private TextView tvLastComboName;

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

        GameTheme currentTheme = themes.get(currentThemeIndex);

        // --- ÁP DỤNG CÁC MÀU SẮC RIÊNG CỦA HIGH SCORE ---

        // Nền
        highScoreView.setBackgroundColor(currentTheme.hsBackgroundColor);

        // Font
        Typeface tf = Typeface.DEFAULT;
        if (currentTheme.fontResId != 0) {
            try {
                tf = ResourcesCompat.getFont(this, currentTheme.fontResId);
            } catch (Exception e) { e.printStackTrace(); }
        }

        // Tiêu đề và Nhãn
        TextView tvTitle = highScoreView.findViewById(R.id.tvHighScoreTile); // Đảm bảo ID đúng trong XML
        TextView tvLabelClassic = highScoreView.findViewById(R.id.labelClassic);
        TextView tvLabelEndless = highScoreView.findViewById(R.id.labelEndless);

        if (tvTitle != null) {
            tvTitle.setTextColor(currentTheme.hsTitleColor);
            tvTitle.setTypeface(tf);
        }
        if (tvLabelClassic != null) {
            tvLabelClassic.setTextColor(currentTheme.hsLabelClassicColor);
            tvLabelClassic.setTypeface(tf);
        }
        if (tvLabelEndless != null) {
            tvLabelEndless.setTextColor(currentTheme.hsLabelEndlessColor);
            tvLabelEndless.setTypeface(tf);
        }

        // Nội dung điểm số
        tvClassicScores.setTextColor(currentTheme.hsScoreClassicColor);
        tvClassicScores.setTypeface(tf);

        tvEndlessScores.setTextColor(currentTheme.hsScoreEndlessColor);
        tvEndlessScores.setTypeface(tf);

        // Particle
        if (particleView != null) {
            particleView.setParticleColor(currentTheme.hsParticleColor);
        }

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

    // --- JOKER UI LOGIC (MỚI) ---

    private void showJokerGameplayUI() {
        if (jokerGameplayView == null) {
            LayoutInflater inflater = LayoutInflater.from(this);
            // Nạp layout XML chứa các container bài
            jokerGameplayView = inflater.inflate(R.layout.layout_joker_gameplay, rootContainer, false);
            rootContainer.addView(jokerGameplayView); // Thêm vào root để đè lên GameView

            // Ánh xạ các thành phần trong XML
            layoutHistoryContainer = jokerGameplayView.findViewById(R.id.layoutHistoryContainer);
            layoutHandContainer = jokerGameplayView.findViewById(R.id.layoutHandContainer);
            tvLastComboName = jokerGameplayView.findViewById(R.id.tvLastComboName);
        }
        jokerGameplayView.setVisibility(View.VISIBLE);
    }

    private void hideJokerGameplayUI() {
        if (jokerGameplayView != null) {
            jokerGameplayView.setVisibility(View.GONE);
        }
    }

    // Hàm cập nhật bài trên tay
    private void updateJokerHandUI(List<Card> hand) {
        if (layoutHandContainer == null) return;
        layoutHandContainer.removeAllViews(); // Xóa bài cũ

        for (Card c : hand) {
            ImageView iv = new ImageView(this);
            if (c.bitmap != null) {
                iv.setImageBitmap(c.bitmap);
            } else {
                iv.setBackgroundColor(Color.WHITE); // Fallback nếu lỗi ảnh
            }

            // Kích thước bài (convert dp sang px)
            int width = dpToPx(60); // 60dp
            int height = dpToPx(84); // 84dp
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width, height);
            params.setMargins(dpToPx(4), 0, dpToPx(4), 0); // Khoảng cách giữa các lá bài
            iv.setLayoutParams(params);

            layoutHandContainer.addView(iv);
        }
    }

    // Hàm cập nhật lịch sử combo
    private void updateJokerHistoryUI(List<Card> history, String lastCombo) {
        if (layoutHistoryContainer == null) return;

        if (tvLastComboName != null) {
            tvLastComboName.setText(lastCombo);
        }

        layoutHistoryContainer.removeAllViews();

        for (int i = 0; i < history.size(); i++) {
            final int index = i;
            Card c = history.get(i);
            ImageView iv = new ImageView(this);
            if (c.bitmap != null) {
                iv.setImageBitmap(c.bitmap);
            }

            int width = dpToPx(40); // Nhỏ hơn bài trên tay
            int height = dpToPx(56);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width, height);
            params.setMargins(0, dpToPx(4), 0, dpToPx(4)); // Xếp dọc, cách nhau 4dp
            iv.setLayoutParams(params);

            // --- XỬ LÝ CLICK VÀO BÀI LỊCH SỬ ĐỂ XÓA ---
            iv.setOnClickListener(v -> {
                if (gameView != null && gameView.getJokerLogic() != null) {
                    gameView.getJokerLogic().removeCardFromHistory(index);
                    // Sound effect nhỏ khi xóa bài (optional)
                    // soundManager.playClick();
                }
            });

            layoutHistoryContainer.addView(iv);
        }
    }

    // Hàm tiện ích đổi dp sang px
    private int dpToPx(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }

    // Hàm gắn kết nối giữa Logic và UI
    private void attachJokerListener() {
        if (gameView != null && gameView.getJokerLogic() != null) {
            gameView.getJokerLogic().setUIListener(new JokerGameLogic.JokerUIListener() {
                @Override
                public void onHandUpdate(List<Card> hand) {
                    runOnUiThread(() -> updateJokerHandUI(hand));
                }

                @Override
                public void onHistoryUpdate(List<Card> history, String lastCombo) {
                    runOnUiThread(() -> updateJokerHistoryUI(history, lastCombo));
                }
            });
        }
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
        hideJokerGameplayUI();
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

        TextView btnResume = pauseMenuView.findViewById(R.id.btnResume);
        TextView btnRestart = pauseMenuView.findViewById(R.id.btnRestart);
        TextView btnMenu = pauseMenuView.findViewById(R.id.btnMenu);

        // Animation UI
        // none

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
        TextView btnReplay = gameOverMenuView.findViewById(R.id.btnReplay);
        TextView btnMenu = gameOverMenuView.findViewById(R.id.btnMenu);

        if (isWin) {
            tvTitle.setText("YOU WIN!");
            tvTitle.setTextColor(Color.GREEN);
        } else {
            tvTitle.setText("GAME OVER");
            tvTitle.setTextColor(Color.RED);
        }
        tvCurrentResult.setText("Purified · " + currentMaxDiff);

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
        if (theme.name.equals("Joker")) {
            attachJokerListener();
        }
    }

    private void initThemes() {
        themes.add(new GameTheme(
                "Classic", // tên
                Color.WHITE, // nền trò chơi
                Color.BLACK, // màu chữ
                Color.RED, // màu mục tiêu
                0, // font chữ
                R.raw.carefree, // nhạc p1
                R.raw.azali_phase2, // nhạc p2
                R.raw.pop, // sfx 1
                R.raw.pop2, // sfx 2
                R.raw.azali_phase2, // nhạc endless
                Color.WHITE, // màu nền HS
                Color.BLACK, // màu hạt
                Color.BLACK, // màu tiêu đề
                Color.BLACK, // màu label classic
                Color.parseColor("#8B0000"), // màu label endless
                Color.BLACK, // màu điểm classic
                Color.BLACK)); // màu điểm endless

        themes.add(new GameTheme("Undertale", Color.BLACK, Color.WHITE, Color.BLUE, R.font.undertale_sans, R.raw.undertale_phase1, R.raw.undertale_phase2, R.raw.pop, R.raw.pop2_undertale, R.raw.undertale_endless
        , Color.BLACK, Color.WHITE, Color.WHITE, Color.WHITE, Color.BLUE, Color.WHITE, Color.WHITE));
        themes.add(new GameTheme("Joker", Color.parseColor("#292929"), Color.parseColor("#B0818E"), Color.RED, R.font.imfellenglish_regular, R.raw.joker_menu_music, R.raw.joker_menu_music, R.raw.pop, R.raw.pop2, R.raw.joker_menu_music
        , Color.parseColor("#292929"), Color.parseColor("#B0818E"), Color.parseColor("#B0818E"), Color.parseColor("#B0818E"), Color.parseColor("#B0818E"), Color.parseColor("#B0818E"), Color.parseColor("#B0818E")));
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

        // --- CẤU HÌNH GIAO DIỆN JOKER ---
        if (selectedTheme.name.equals("Joker")) {
            showJokerGameplayUI(); // Hiện khung bài XML
            attachJokerListener(); // Gắn kết nối dữ liệu
        } else {
            hideJokerGameplayUI();
        }

        // --- CẤU HÌNH GAME CƠ BẢN ---
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