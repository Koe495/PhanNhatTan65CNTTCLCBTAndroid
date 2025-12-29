package cuoiki.helloworldgame;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Region;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Looper;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import androidx.core.content.res.ResourcesCompat;

import com.google.mlkit.vision.digitalink.recognition.Ink;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

public class GameView extends View {

    // --- CONSTANTS ---
    private static final String ENEMIES_CHARS = "_()^ZNMv";
    private static final String TARGET_FULL = "helloworld";
    private static final int PHASE_SKIP_DIFF = 40;
    private static final long BASE_SPAWN_DELAY = 2000;
    private static final long JOKER_SPAWN_DELAY = 1000;
    private static final int TARGET_SPAWN_RATE = 20;

    // --- PAINTS ---
    private Paint textPaint = new Paint();
    private Paint drawPaint = new Paint();
    private Paint hudPaintActive = new Paint();
    private Paint hudPaintInactive = new Paint();
    private Paint particlePaint = new Paint();

    // --- GAME OBJECTS ---
    private ArrayList<FallingChar> fallingChars = new ArrayList<>();
    private ArrayList<Particle> particles = new ArrayList<>();
    private Path currentPath = new Path();

    // --- GAME STATE ---
    private int score = 10;
    private int diff = 0;
    private int collectedIndex = 0;
    private int gamePhase = 1;
    private boolean isGameOver = false;
    private boolean isVictory = false;
    private boolean isPaused = false;
    private boolean isJokerMode = false;

    // Timer
    private long lastSpawnTime = 0;

    private float baseSpeedParam = 3;
    private int screenWidth, screenHeight;

    // --- CONFIGURATION ---
    private GameTheme currentTheme;
    private GameMode currentMode;

    // --- UTILS ---
    private Random random = new Random();
    private Ink.Builder inkBuilder = Ink.builder();
    private Ink.Stroke.Builder strokeBuilder;
    private RecognitionManager recognitionManager;
    private SoundManager soundManager;
    private QTEManager qteManager;
    private GameOverListener listener;
    private GestureDetector gestureDetector;
    private JokerGameLogic jokerLogic;

    // --- HANDLER QUẢN LÝ UI & DRAWING ---
    // Dùng handler này để quản lý việc xóa nét vẽ và game loop
    private Handler uiHandler = new Handler(Looper.getMainLooper());

    // Runnable để xóa nét vẽ sau một khoảng trễ (dùng cho trường hợp vẽ sai)
    private Runnable clearPathRunnable = new Runnable() {
        @Override
        public void run() {
            currentPath.reset();
            invalidate();
        }
    };

    public interface GameOverListener {
        void onScoreUpdate(int score);
        void onDiffUpdate(int diff);
        void onGameOver();
        void onGameWin();
        void onPhase2Start();
        void onPauseRequest();
        void onBossHpUpdate(int currentHp, int maxHp);
    }

    public GameView(Context context) {
        super(context);
        init();
    }

    private void init() {
        textPaint.setTextSize(160);
        textPaint.setFakeBoldText(true);

        drawPaint.setStrokeWidth(15);
        drawPaint.setStyle(Paint.Style.STROKE);
        drawPaint.setStrokeJoin(Paint.Join.ROUND);
        drawPaint.setStrokeCap(Paint.Cap.ROUND);

        hudPaintActive.setTextSize(50);
        hudPaintActive.setFakeBoldText(true);

        hudPaintInactive.setColor(Color.LTGRAY);
        hudPaintInactive.setTextSize(50);
        hudPaintInactive.setFakeBoldText(true);

        particlePaint.setStyle(Paint.Style.FILL);

        currentTheme = new GameTheme("Classic", // tên
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
                Color.BLACK); // màu điểm endless
        currentMode = GameMode.CLASSIC;

        startGameLoop();

        // --- GESTURE DETECTOR (TRIPLE TAP) ---
        gestureDetector = new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener() {
            private int tapCount = 0;
            private long lastTapTime = 0;
            private static final int TAP_INTERVAL = 500;

            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                long currentTime = System.currentTimeMillis();

                if (currentTime - lastTapTime < TAP_INTERVAL) {
                    tapCount++;
                } else {
                    tapCount = 1;
                }

                lastTapTime = currentTime;

                if (tapCount == 3) {
                    tapCount = 0;
                    if (!isGameOver && !isVictory) {
                        pauseGame();
                        if (listener != null) listener.onPauseRequest();
                    }
                    return true;
                }
                return false;
            }
        });
    }

    public void setGameOverListener(GameOverListener listener) {
        this.listener = listener;
        if (isJokerMode && jokerLogic != null && jokerLogic.getBoss() != null) {
            listener.onBossHpUpdate(jokerLogic.getBoss().hp, jokerLogic.getBoss().maxHp);
        }
    }
    public void setRecognitionManager(RecognitionManager manager) { this.recognitionManager = manager; }
    public void setSoundManager(SoundManager soundManager) { this.soundManager = soundManager; }

    public int getScore() {
        return score;
    }
    public void pauseGame() {
        isPaused = true;
        if (soundManager != null) soundManager.pauseBackground();
    }

    public void resumeGame() {
        isPaused = false;
        if (soundManager != null) soundManager.resumeBackground();
        startGameLoop(); // Đảm bảo loop chạy lại
    }

    public void setGameConfig(GameTheme theme, GameMode mode) {
        this.currentTheme = theme;
        this.currentMode = mode;
        isJokerMode = theme.name.equals("Joker");

        if (isJokerMode) {
            // TRUYỀN MODE VÀO CONSTRUCTOR
            jokerLogic = new JokerGameLogic(getContext(), currentMode);

            if (jokerLogic.getBoss() != null) {
                jokerLogic.getBoss().setListener(new JokerBoss.BossListener() {
                    @Override
                    public void onHpChanged(int currentHp, int maxHp) {
                        if (listener != null) {
                            listener.onBossHpUpdate(currentHp, maxHp);
                        }
                    }
                });
                if (listener != null) {
                    listener.onBossHpUpdate(jokerLogic.getBoss().hp, jokerLogic.getBoss().maxHp);
                }
            }
        } else {
            jokerLogic = null;
        }

        textPaint.setColor(theme.textColor);
        drawPaint.setColor(theme.strokeColor);
        hudPaintActive.setColor(theme.strokeColor);

        if (theme.fontResId != 0) {
            try {
                Typeface tf = ResourcesCompat.getFont(getContext(), theme.fontResId);
                textPaint.setTypeface(tf);
            } catch (Exception e) { e.printStackTrace(); }
        } else {
            textPaint.setTypeface(Typeface.DEFAULT_BOLD);
        }

        if (currentMode == GameMode.ENDLESS) {
            diff = 20;
            gamePhase = 2;
        } else {
            diff = 0;
            gamePhase = 1;
        }
        if (listener != null) {
            listener.onScoreUpdate(score);
            listener.onDiffUpdate(diff);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        screenWidth = w;
        screenHeight = h;

        if (qteManager == null) {
            qteManager = new QTEManager(getContext(), w, h);
            qteManager.setSoundManager(soundManager);

            qteManager.setListener(new QTEManager.QTEListener() {
                @Override
                public void onLaserFire(Path laserPath) {
                    handleLaserCollision(laserPath);
                }

                @Override
                public void onQTEFail() {
                    score -= 5;
                    if (listener != null) listener.onScoreUpdate(score);
                    if (score <= 0) {
                        isGameOver = true;
                        if (listener != null) listener.onGameOver();
                    }
                    spawnExplosion(screenWidth/2, screenHeight/2, Color.RED, 50, 20);
                }
            });
        }
    }

    private void handleLaserCollision(Path laserPath) {
        Region region = new Region();
        RectF bounds = new RectF();
        laserPath.computeBounds(bounds, true);

        region.setPath(laserPath, new Region(0, 0, screenWidth, screenHeight));

        Iterator<FallingChar> iter = fallingChars.iterator();
        while(iter.hasNext()){
            FallingChar fc = iter.next();
            if(region.contains((int)fc.x, (int)fc.y)){
                iter.remove();
                spawnExplosion(fc.x, fc.y, Color.WHITE, 20, 15);
                score += 1;
            }
        }
        if(listener != null) listener.onScoreUpdate(score);
    }

    private void startGameLoop() {
        uiHandler.removeCallbacksAndMessages(null);

        final Runnable gameLoop = new Runnable() {
            @Override
            public void run() {
                if (!isGameOver) {
                    updateGame();
                    invalidate();
                    uiHandler.postDelayed(this, 16);
                }
            }
        };
        uiHandler.post(gameLoop);
    }

    // Hàm phụ để update particle khi đã thắng (làm màu)
    private void updateParticlesOnly() {
        Iterator<Particle> pIter = particles.iterator();
        while (pIter.hasNext()) {
            Particle p = pIter.next();
            p.update();
            if (p.isDead()) pIter.remove();
        }
    }

    private void updateGame() {
        // 1. Update Particles
        Iterator<Particle> pIter = particles.iterator();
        while (pIter.hasNext()) {
            Particle p = pIter.next();
            p.update();
            if (p.isDead()) pIter.remove();
        }

        // 2. QTE Logic (Undertale Mode)
        if (qteManager != null && currentMode == GameMode.ENDLESS && currentTheme.name.equalsIgnoreCase("Undertale")) {
            qteManager.update(0.0166667f);
        }

        if (isPaused) return;

        // 3. Game Logic (Joker hoặc Classic)
        if (isJokerMode) {
            int damage = jokerLogic.update(screenHeight);

            // KIỂM TRA BOSS CHẾT
            if (jokerLogic.getBoss() != null && jokerLogic.getBoss().hp <= 0) {

                if (currentMode == GameMode.ENDLESS) {
                    // --- LOGIC ENDLESS MỚI ---

                    // 1. Tính HP mới (gấp 1.2 lần)
                    int oldMax = jokerLogic.getBoss().maxHp;
                    int newMax = (int)(oldMax * 1.2f);

                    // 2. Hồi sinh Boss
                    jokerLogic.getBoss().revive(newMax);

                    // 3. Tăng HP cho người chơi (+1)
                    score++;
                    if (listener != null) listener.onScoreUpdate(score);

                    // 4. Hiệu ứng (Nổ + Thông báo)
                    spawnExplosion(screenWidth/2, 200, Color.YELLOW, 20, 20);

                    // 5. Play Sound heal (nếu có)
                    if (soundManager != null) soundManager.playHeal();

                } else {
                    // --- LOGIC CLASSIC CŨ ---
                    if (!isVictory) {
                        isVictory = true;
                        isGameOver = true;
                        if (listener != null) listener.onGameWin();
                    }
                    return;
                }
            }

            if (damage > 0) { score -= damage; if (listener != null) listener.onScoreUpdate(score); if (score <= 0) { isGameOver = true; if (listener != null) listener.onGameOver(); } }
            if (System.currentTimeMillis() - lastSpawnTime > JOKER_SPAWN_DELAY) { jokerLogic.trySpawnEnemy(screenWidth); lastSpawnTime = System.currentTimeMillis(); }
        }
        else {
            // Logic Classic/Endless
            long currentSpawnDelay = Math.max(500, BASE_SPAWN_DELAY - (diff * 25L));
            if (System.currentTimeMillis() - lastSpawnTime > currentSpawnDelay) {
                spawnEnemy();
                lastSpawnTime = System.currentTimeMillis();
            }

            Iterator<FallingChar> iter = fallingChars.iterator();
            while (iter.hasNext()) {
                FallingChar fc = iter.next();
                fc.update();

                if (fc.y > screenHeight) {
                    iter.remove();
                    score--;
                    if (listener != null) listener.onScoreUpdate(score);
                    if (score <= 0) {
                        isGameOver = true;
                        if (listener != null) listener.onGameOver();
                    }
                }
            }
        }
    }

    private void spawnEnemy() {
        boolean isTargetAlreadyOnScreen = false;
        for (FallingChar fc : fallingChars) {
            if (fc.isTarget) {
                isTargetAlreadyOnScreen = true;
                break;
            }
        }

        String charToSpawn;
        boolean isTargetChar = false;
        boolean shouldSpawnTarget = !isTargetAlreadyOnScreen && random.nextInt(100) < TARGET_SPAWN_RATE;

        if (shouldSpawnTarget) {
            char nextNeed = TARGET_FULL.charAt(collectedIndex % TARGET_FULL.length());
            boolean canSpawn = true;
            if (currentMode == GameMode.CLASSIC && collectedIndex >= 4 && gamePhase == 1) {
                canSpawn = false;
            }

            if (canSpawn) {
                charToSpawn = String.valueOf(nextNeed);
                isTargetChar = true;
            } else {
                charToSpawn = String.valueOf(ENEMIES_CHARS.charAt(random.nextInt(ENEMIES_CHARS.length())));
            }
        } else {
            charToSpawn = String.valueOf(ENEMIES_CHARS.charAt(random.nextInt(ENEMIES_CHARS.length())));
        }

        float baseSpeed = baseSpeedParam + (diff / 10.0f);
        float finalSpeed = baseSpeed + random.nextInt(3);

        fallingChars.add(new FallingChar(charToSpawn, random.nextInt(screenWidth - 150) + 20, 0, finalSpeed, isTargetChar));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (qteManager != null && currentMode == GameMode.ENDLESS && currentTheme.name.equalsIgnoreCase("Undertale")) {
            qteManager.draw(canvas);
        }

        if (isJokerMode) {
            jokerLogic.draw(canvas, screenWidth, screenHeight);
        } else {
            if (currentMode == GameMode.CLASSIC) {
                float startX = 50;
                float startY = screenHeight - 100;
                float spacing = 60;
                for (int i = 0; i < TARGET_FULL.length(); i++) {
                    String c = String.valueOf(TARGET_FULL.charAt(i));
                    if (i < collectedIndex) {
                        canvas.drawText(c, startX + (i * spacing), startY, hudPaintActive);
                    } else {
                        canvas.drawText(c, startX + (i * spacing), startY, hudPaintInactive);
                    }
                }
            }
            for (FallingChar fc : fallingChars) {
                if (fc.isTarget) textPaint.setColor(currentTheme.strokeColor);
                else textPaint.setColor(currentTheme.textColor);
                canvas.drawText(fc.character, fc.x, fc.y, textPaint);
            }
        }

        for (Particle p : particles) {
            particlePaint.setColor(p.color);
            particlePaint.setAlpha(p.alpha);
            canvas.drawCircle(p.x, p.y, p.size, particlePaint);
        }

        canvas.drawPath(currentPath, drawPaint);
    }

    // --- HÀM HỖ TRỢ XÓA NÉT VẼ NGAY LẬP TỨC ---
    private void resetPathInstantly() {
        uiHandler.removeCallbacks(clearPathRunnable); // Hủy lệnh xóa trễ nếu có
        currentPath.reset();
        invalidate();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (isPaused || isGameOver) return true; // Chặn touch khi game over/pause

        if (gestureDetector.onTouchEvent(event)) {
            inkBuilder = Ink.builder();
            strokeBuilder = null;
            resetPathInstantly(); // Xóa ngay nếu detect double/triple tap
            return true;
        }

        if (qteManager != null && currentMode == GameMode.ENDLESS && currentTheme.name.equalsIgnoreCase("Undertale")) {
            qteManager.handleTouch(event);
        }

        float x = event.getX();
        float y = event.getY();
        long t = event.getEventTime();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // Hủy lệnh xóa cũ ngay khi đặt tay xuống vẽ tiếp
                uiHandler.removeCallbacks(clearPathRunnable);

                currentPath.moveTo(x, y);
                strokeBuilder = Ink.Stroke.builder();
                strokeBuilder.addPoint(Ink.Point.create(x, y, t));
                break;

            case MotionEvent.ACTION_MOVE:
                currentPath.lineTo(x, y);
                if (strokeBuilder != null) {
                    strokeBuilder.addPoint(Ink.Point.create(x, y, t));
                }
                break;

            case MotionEvent.ACTION_UP:
                if (strokeBuilder != null) {
                    strokeBuilder.addPoint(Ink.Point.create(x, y, t));
                    inkBuilder.addStroke(strokeBuilder.build());

                    if (recognitionManager != null) {
                        recognitionManager.recognize(inkBuilder.build(), new RecognitionManager.RecognitionListener() {
                            @Override
                            public void onResult(String result) {
                                checkMatch(result);
                            }
                        });
                    }

                    inkBuilder = Ink.builder();
                    strokeBuilder = null;
                }

                // Mặc định vẫn chờ 200ms để xóa (nếu vẽ sai)
                // Nhưng nếu vẽ trúng, hàm checkMatch sẽ gọi resetPathInstantly() để hủy lệnh này.
                uiHandler.postDelayed(clearPathRunnable, 200);
                break;
        }

        invalidate();
        return true;
    }

    private void checkMatch(String recognizedText) {
        String textRaw = recognizedText;
        String textUpper = recognizedText.toUpperCase();
        boolean anyHit = false; // Cờ đánh dấu có trúng hay không

        if (isJokerMode) {
            boolean hit = jokerLogic.checkMatch(recognizedText);
            if (hit) {
                anyHit = true;
                if (soundManager != null) soundManager.playExplodeNormal();
            }
        } else {
            Iterator<FallingChar> iter = fallingChars.iterator();
            while (iter.hasNext()) {
                FallingChar fc = iter.next();
                boolean isMatch = isMatchingChar(fc, textRaw, textUpper);

                if (isMatch) {
                    anyHit = true; // Đánh trúng

                    float hitX = fc.x;
                    float hitY = fc.y - 30;
                    iter.remove();

                    diff++;
                    if (listener != null) listener.onDiffUpdate(diff);

                    if (fc.isTarget) {
                        handleTargetHit(hitX, hitY);
                    } else {
                        if (soundManager != null) soundManager.playExplodeNormal();
                        spawnExplosion(hitX, hitY, Color.DKGRAY, 10, 5);
                    }
                    // Chỉ diệt 1 con gần nhất/đầu tiên tìm thấy
                    break;
                }
            }
        }
        // Nếu trúng, xóa nét vẽ
        if (anyHit) {
            resetPathInstantly();
        }
    }

    public void restartGame() {
        score = 10;

        if (currentMode == GameMode.ENDLESS) {
            diff = 20;
            gamePhase = 2;
        } else {
            diff = 0;
            gamePhase = 1;
        }

        collectedIndex = 0;
        isGameOver = false;
        isVictory = false;
        isPaused = false;

        fallingChars.clear();
        particles.clear();
        currentPath.reset();

        if (qteManager != null) {
            qteManager = new QTEManager(getContext(), screenWidth, screenHeight);
            qteManager.setSoundManager(soundManager);

            qteManager.setListener(new QTEManager.QTEListener() {
                @Override
                public void onLaserFire(Path laserPath) {
                    handleLaserCollision(laserPath);
                }
                @Override
                public void onQTEFail() {
                    score -= 5;
                    if (listener != null) listener.onScoreUpdate(score);
                    if (score <= 0) {
                        isGameOver = true;
                        if (listener != null) listener.onGameOver();
                    }
                }
            });
        }

        if (isJokerMode && jokerLogic != null) {
            jokerLogic = new JokerGameLogic(getContext(), currentMode);

            if (jokerLogic.getBoss() != null) {
                jokerLogic.getBoss().setListener(new JokerBoss.BossListener() {
                    @Override
                    public void onHpChanged(int currentHp, int maxHp) {
                        if (listener != null) {
                            listener.onBossHpUpdate(currentHp, maxHp);
                        }
                    }
                });

                if (listener != null) {
                    listener.onBossHpUpdate(jokerLogic.getBoss().hp, jokerLogic.getBoss().maxHp);
                }
            }
        }

        if (listener != null) {
            listener.onScoreUpdate(score);
            listener.onDiffUpdate(diff);
        }
        invalidate();

        startGameLoop(); // Đảm bảo loop chạy lại
        lastSpawnTime = System.currentTimeMillis();
    }

    private boolean isMatchingChar(FallingChar fc, String textRaw, String textUpper) {
        if (fc.isTarget) {
            if (fc.character.equalsIgnoreCase("O")) {
                return textRaw.equals("0") ||
                        textUpper.equals("O") ||
                        textRaw.equals("6") ||
                        textUpper.equals("D");
            }

            if (fc.character.equals("l")) {
                return textRaw.equals("1") ||
                        textUpper.equals("L") ||
                        textRaw.equals("|") ||
                        textRaw.equals("\\") ||
                        textRaw.equals("/");
            }

            if (fc.character.equals("h")) {
                return textRaw.equals("n") || textRaw.equals("h");
            }

            return fc.character.equalsIgnoreCase(textRaw);
        } else {
            String enemy = fc.character;
            switch (enemy) {
                case "_": return textRaw.equals("_") || textRaw.equals("-");
                case "^": return textRaw.equals("^") || textRaw.equals("1") || textRaw.equals("A");
                case "(": return textRaw.equals("(") || textRaw.equals("<") || textRaw.equals("[") || textRaw.equals("C") || textRaw.equals("c");
                case ")": return textRaw.equals(")") || textRaw.equals(">") || textRaw.equals("]") || textRaw.equals("J") || textRaw.equals("j");
                case "v": return textRaw.equals("U") || textRaw.equals("v") || textRaw.equals("V");
                case "/": return textRaw.equals("/") || textRaw.equals("1") || textRaw.equals("l") || textUpper.equals("I");
                default: return enemy.equals(textUpper);
            }
        }
    }

    private void handleTargetHit(float x, float y) {
        collectedIndex++;
        if (soundManager != null && collectedIndex < TARGET_FULL.length()) soundManager.playExplodeTarget();
        spawnExplosion(x, y, currentTheme.strokeColor, 30, 15);

        if (currentMode == GameMode.ENDLESS) {
            if (collectedIndex >= TARGET_FULL.length()) {
                collectedIndex = 0;
                if (soundManager != null) soundManager.playHeal();
                score++;
                if (listener != null) listener.onScoreUpdate(score);
                }
            return;
        }

        if ((gamePhase == 1 && collectedIndex == 4) || (gamePhase == 1 && diff == PHASE_SKIP_DIFF)) {
            gamePhase = 2;
            diff = PHASE_SKIP_DIFF;
            if (listener != null) listener.onPhase2Start();
        }

        if (collectedIndex >= TARGET_FULL.length()) {
            if (soundManager != null) soundManager.playHeal();
            isGameOver = true;
            isVictory = true;
            if (listener != null) listener.onGameWin();
        }
    }

    public void spawnExplosion(float x, float y, int color, int count, float sizeBase) {
        // Giới hạn số lượng hạt nổ để tránh lag nếu spam nhiều
        int safeCount = Math.min(count, 20);
        for (int i = 0; i < safeCount; i++) {
            particles.add(new Particle(x, y, color, sizeBase + random.nextInt(10)));
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        uiHandler.removeCallbacksAndMessages(null);
        isGameOver = true;
        isPaused = true;
    }
    public JokerGameLogic getJokerLogic() {
        return jokerLogic;
    }
}