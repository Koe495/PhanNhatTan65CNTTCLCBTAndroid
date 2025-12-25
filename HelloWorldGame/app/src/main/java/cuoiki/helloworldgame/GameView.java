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
import android.widget.Toast;

import androidx.core.content.res.ResourcesCompat;

import com.google.mlkit.vision.digitalink.recognition.Ink;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

public class GameView extends View {

    // --- CONSTANTS ---
    private static final String ENEMIES_CHARS = "_/>^ZNMUJC";
    private static final String TARGET_FULL = "helloworld";
    private static final int PHASE_SKIP_DIFF = 40;
    private static final long BASE_SPAWN_DELAY = 2000;
    private static final long JOKER_SPAWN_DELAY = 2000;
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

    public interface GameOverListener {
        void onScoreUpdate(int score);
        void onDiffUpdate(int diff);
        void onGameOver();
        void onGameWin();
        void onPhase2Start();
        void onPauseRequest();
    }

    public GameView(Context context) {
        super(context);
        init();
    }

    private void init() {
        textPaint.setTextSize(80);
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

        currentTheme = new GameTheme("Default", Color.WHITE, Color.BLACK, Color.BLUE, 0, R.raw.carefree, R.raw.azali_phase2, R.raw.pop, R.raw.pop2, R.raw.azali_phase2);
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
                        // Gọi hàm pauseGame tập trung để xử lý cả nhạc và QTE
                        pauseGame();
                        if (listener != null) listener.onPauseRequest();
                    }
                    return true;
                }
                return false;
            }
        });
    }

    public void setGameOverListener(GameOverListener listener) { this.listener = listener; }
    public void setRecognitionManager(RecognitionManager manager) { this.recognitionManager = manager; }
    public void setSoundManager(SoundManager soundManager) { this.soundManager = soundManager; }

    // --- CẬP NHẬT LOGIC PAUSE/RESUME ---
    public void pauseGame() {
        isPaused = true;
        // Dừng nhạc nền khi pause
        if (soundManager != null) {
            soundManager.pauseBackground();
        }
    }

    public void resumeGame() {
        isPaused = false;
        // Tiếp tục nhạc nền khi resume
        if (soundManager != null) {
            soundManager.resumeBackground();
        }
    }

    public void setGameConfig(GameTheme theme, GameMode mode) {
        this.currentTheme = theme;
        this.currentMode = mode;
        isJokerMode = theme.name.equals("Joker");

        if (isJokerMode) {
            jokerLogic = new JokerGameLogic(getContext());
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
        final Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (!isGameOver || isVictory) {
                    updateGame();
                    invalidate();
                    handler.postDelayed(this, 16);
                }
            }
        });
    }

    private void updateGame() {
        // Cập nhật Particles (vẫn cho phép chạy hiệu ứng nổ còn sót lại hoặc dừng tuỳ ý, ở đây tôi để chạy)
        Iterator<Particle> pIter = particles.iterator();
        while (pIter.hasNext()) {
            Particle p = pIter.next();
            p.update();
            if (p.isDead()) pIter.remove();
        }

        // --- CẬP NHẬT: Ngăn QTE update nếu đang Pause ---
        if (qteManager != null && currentMode == GameMode.ENDLESS && currentTheme.name.equalsIgnoreCase("Undertale")) {
            if (!isPaused) { // Chỉ update QTE khi KHÔNG pause
                qteManager.update(0.0166667f);
            }
        }

        // Nếu đang pause thì dừng toàn bộ logic game phía dưới (sinh quái, di chuyển quái)
        if (isVictory || isPaused) return;

        if (isJokerMode) {
            int damage = jokerLogic.update(screenHeight);
            if (damage > 0) {
                score -= damage;
                if (listener != null) listener.onScoreUpdate(score);
                if (score <= 0) {
                    isGameOver = true;
                    if (listener != null) listener.onGameOver();
                }
            }
            if (System.currentTimeMillis() - lastSpawnTime > JOKER_SPAWN_DELAY) {
                jokerLogic.trySpawnEnemy(screenWidth);
                lastSpawnTime = System.currentTimeMillis();
            }
        } else {
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

        fallingChars.add(new FallingChar(charToSpawn, random.nextInt(screenWidth - 100) + 50, 0, finalSpeed, isTargetChar));
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
                float startY = screenHeight - 150;
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

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (isPaused || isGameOver || isVictory) return true;

        if (gestureDetector.onTouchEvent(event)) {
            inkBuilder = Ink.builder();
            strokeBuilder = null;
            currentPath.reset();
            invalidate();
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

                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        currentPath.reset();
                        invalidate();
                    }
                }, 200);
                break;
        }

        invalidate();
        return true;
    }

    private void checkMatch(String recognizedText) {
        String textRaw = recognizedText;
        String textUpper = recognizedText.toUpperCase();

        if (isJokerMode) {
            boolean hit = jokerLogic.checkMatch(recognizedText);
            if (hit && soundManager != null) soundManager.playExplodeNormal();
        } else {
            Iterator<FallingChar> iter = fallingChars.iterator();
            while (iter.hasNext()) {
                FallingChar fc = iter.next();
                boolean isMatch = isMatchingChar(fc, textRaw, textUpper);

                if (isMatch) {
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
                    break;
                }
            }
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
                }
            });
        }

        if (isJokerMode && jokerLogic != null) {
            jokerLogic = new JokerGameLogic(getContext());
        }

        if (listener != null) listener.onScoreUpdate(score);
        invalidate();

        lastSpawnTime = System.currentTimeMillis();
    }

    private boolean isMatchingChar(FallingChar fc, String textRaw, String textUpper) {
        if (fc.isTarget) {
            if (textRaw.equals("0")) return fc.character.equals("O");
            return fc.character.equalsIgnoreCase(textRaw);
        } else {
            String enemy = fc.character;
            switch (enemy) {
                case "_": return textRaw.equals("_") || textRaw.equals("-");
                case "^": return textRaw.equals("^") || textRaw.equals("1") || textRaw.equals("A");
                case "/": return textRaw.equals("/") || textRaw.equals("1") || textRaw.equals("l") || textUpper.equals("I");
                case "(": return textRaw.equals("(");
                case ")": return textRaw.equals(")");
                case "<": return textRaw.equals("<");
                case ">": return textRaw.equals(">");
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
                Toast.makeText(getContext(), "+1 HP", Toast.LENGTH_SHORT).show();
            }
            return;
        }

        if ((gamePhase == 1 && collectedIndex == 4) || (gamePhase == 1 && diff == PHASE_SKIP_DIFF)) {
            gamePhase = 2;
            diff = PHASE_SKIP_DIFF;
            if (listener != null) listener.onPhase2Start();
            Toast.makeText(getContext(), "YOU WANT HELL?", Toast.LENGTH_SHORT).show();
        }

        if (collectedIndex >= TARGET_FULL.length()) {
            if (soundManager != null) soundManager.playHeal();
            isGameOver = true;
            isVictory = true;
            if (listener != null) listener.onGameWin();
        }
    }

    private void spawnExplosion(float x, float y, int color, int count, float sizeBase) {
        for (int i = 0; i < count; i++) {
            particles.add(new Particle(x, y, color, sizeBase + random.nextInt(10)));
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        isGameOver = true;
        isPaused = true;
    }
}