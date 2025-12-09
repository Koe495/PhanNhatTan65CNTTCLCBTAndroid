package cuoiki.helloworldgame;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Handler;
import android.os.Looper;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.google.mlkit.vision.digitalink.recognition.Ink;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

public class GameView extends View {

    // --- HẰNG SỐ CẤU HÌNH ---
    private static final String ENEMIES_CHARS = "_/<>^ZNMUJC";
    private static final String TARGET_FULL = "helloworld";
    private static final int PHASE_SKIP_DIFF = 40;
    private static final long BASE_SPAWN_DELAY = 2000;
    private static final int TARGET_SPAWN_RATE = 60;

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

    // --- TOOLS ---
    private Random random = new Random();
    private Ink.Builder inkBuilder = Ink.builder();
    private Ink.Stroke.Builder strokeBuilder;
    private RecognitionManager recognitionManager;
    private SoundManager soundManager;
    private GameOverListener listener;

    private int screenWidth, screenHeight;
    private long lastSpawnTime = 0;
    private float baseSpeedParam = 3;

    // --- INTERFACES ---
    public interface GameOverListener {
        void onScoreUpdate(int score);
        void onDiffUpdate(int diff);
        void onGameOver();
        void onGameWin();
        void onPhase2Start();
    }

    public void setGameOverListener(GameOverListener listener) { this.listener = listener; }
    public void setRecognitionManager(RecognitionManager manager) { this.recognitionManager = manager; }
    public void setSoundManager(SoundManager soundManager) { this.soundManager = soundManager; }
    public void pauseGame() { isPaused = true; }
    public void resumeGame() { isPaused = false; }

    public GameView(Context context) {
        super(context);
        init();
    }

    private void init() {
        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize(80);
        textPaint.setFakeBoldText(true);

        drawPaint.setColor(Color.BLUE);
        drawPaint.setStrokeWidth(15);
        drawPaint.setStyle(Paint.Style.STROKE);
        drawPaint.setStrokeJoin(Paint.Join.ROUND);

        hudPaintActive.setColor(Color.RED);
        hudPaintActive.setTextSize(50);
        hudPaintActive.setFakeBoldText(true);

        hudPaintInactive.setColor(Color.LTGRAY);
        hudPaintInactive.setTextSize(50);
        hudPaintInactive.setFakeBoldText(true);

        particlePaint.setStyle(Paint.Style.FILL);

        gamePhase = 1;
        startGameLoop();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        screenWidth = w;
        screenHeight = h;
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
        // Hiệu ứng nổ
        Iterator<Particle> pIter = particles.iterator();
        while (pIter.hasNext()) {
            Particle p = pIter.next();
            p.update();
            if (p.isDead()) pIter.remove();
        }

        // Logic thắng cuộc
        if (isVictory) {
            return;
        }

        // Logic game chính
        if (gamePhase == 2) drawPaint.setColor(Color.RED);
        if (isPaused) return;

        // Tính toán tốc độ spawn
        long currentSpawnDelay = Math.max(500, BASE_SPAWN_DELAY - (diff * 30L));

        if (System.currentTimeMillis() - lastSpawnTime > currentSpawnDelay) {
            spawnEnemy();
            lastSpawnTime = System.currentTimeMillis();
        }

        // Cập nhật vị trí rơi
        updateFallingChars();
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
        boolean shouldSpawnTarget = !isTargetAlreadyOnScreen
                && random.nextInt(100) < TARGET_SPAWN_RATE
                && collectedIndex < TARGET_FULL.length();

        if (shouldSpawnTarget) {
            char nextNeed = TARGET_FULL.charAt(collectedIndex);
            boolean canSpawn = true;

            // Chặn spawn nếu ở cuối Phase 1
            if (collectedIndex >= 4 && gamePhase == 1) {
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

        // Tạo đối tượng FallingChar
        fallingChars.add(new FallingChar(charToSpawn,
                random.nextInt(screenWidth - 100) + 50,
                0,
                finalSpeed,
                isTargetChar));
    }

    private void updateFallingChars() {
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

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Vẽ HUD
        float startX = 50;
        float startY = screenHeight - 150;
        float spacing = 60;

        for (int i = 0; i < TARGET_FULL.length(); i++) {
            String c = String.valueOf(TARGET_FULL.charAt(i));
            if (i < collectedIndex) canvas.drawText(c, startX + (i * spacing), startY, hudPaintActive);
            else canvas.drawText(c, startX + (i * spacing), startY, hudPaintInactive);
        }

        // Vẽ chữ rơi
        for (FallingChar fc : fallingChars) {
            if (fc.isTarget) textPaint.setColor(Color.RED);
            else textPaint.setColor(Color.BLACK);
            canvas.drawText(fc.character, fc.x, fc.y, textPaint);
        }

        // Vẽ hiệu ứng nổ
        for (Particle p : particles) {
            particlePaint.setColor(p.color);
            particlePaint.setAlpha(p.alpha);
            canvas.drawCircle(p.x, p.y, p.size, particlePaint);
        }

        textPaint.setColor(Color.BLACK);
        canvas.drawPath(currentPath, drawPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
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
                if (strokeBuilder != null) strokeBuilder.addPoint(Ink.Point.create(x, y, t));
                break;
            case MotionEvent.ACTION_UP:
                if (strokeBuilder != null) {
                    strokeBuilder.addPoint(Ink.Point.create(x, y, t));
                    inkBuilder.addStroke(strokeBuilder.build());
                    if (recognitionManager != null) {
                        recognitionManager.recognize(inkBuilder.build(), result -> checkMatch(result));
                    }
                    inkBuilder = Ink.builder();
                    strokeBuilder = null;
                }
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    currentPath.reset();
                    invalidate();
                }, 200);
                break;
        }
        invalidate();
        return true;
    }

    private void checkMatch(String recognizedText) {
        String textRaw = recognizedText;
        String textUpper = recognizedText.toUpperCase();

        Iterator<FallingChar> iter = fallingChars.iterator();
        while (iter.hasNext()) {
            FallingChar fc = iter.next();

            // Đối chiếu ký tự
            boolean isMatch = isMatchingChar(fc, textRaw, textUpper);

            if (isMatch) {
                float hitX = fc.x;
                float hitY = fc.y - 30;

                iter.remove(); // Xóa khỏi màn hình

                // Tăng độ khó
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

    // Hàm đối chiếu ký tự
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

    private void handleTargetHit(float hitX, float hitY) {
        collectedIndex++;
        if (soundManager != null) soundManager.playExplodeTarget();
        spawnExplosion(hitX, hitY, Color.RED, 20, 8);

        // Chuyển Phase
        if ((gamePhase == 1 && collectedIndex == 4) || (gamePhase == 1 && diff == PHASE_SKIP_DIFF)) {
            gamePhase = 2;
            diff = PHASE_SKIP_DIFF;
            if (listener != null) listener.onPhase2Start();
            Toast.makeText(getContext(), "YOU WANT HELL?", Toast.LENGTH_SHORT).show();
        }

        // Thắng Game
        if (collectedIndex >= TARGET_FULL.length()) {
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
}