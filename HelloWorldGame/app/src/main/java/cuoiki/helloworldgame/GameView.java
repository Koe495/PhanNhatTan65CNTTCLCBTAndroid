package cuoiki.helloworldgame;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;
import com.google.mlkit.vision.digitalink.Ink;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

public class GameView extends View {

    private Paint textPaint = new Paint();
    private Paint drawPaint = new Paint();

    // Paint riêng cho HUD dòng helloword
    private Paint hudPaintActive = new Paint();
    private Paint hudPaintInactive = new Paint();

    private ArrayList<FallingChar> fallingChars = new ArrayList<>();
    private Path currentPath = new Path();
    private int score = 10;
    private int diff = 0;
    private int phaseSkipDiff = 30;
    private boolean isGameOver = false;
    private Random random = new Random();
    private GameOverListener listener;

    private Ink.Builder inkBuilder = Ink.builder();
    private Ink.Stroke.Builder strokeBuilder;
    private RecognitionManager recognitionManager;

    private int screenWidth, screenHeight;
    private long lastSpawnTime = 0;
    private final String ENEMIES_CHARS = "_/<>^ZNMUJC"; // Quái thường
    private final String TARGET_FULL = "helloworld";   // Mục tiêu chính
    private int collectedIndex = 0; // Tổng số mục tiêu đã ghép

    public interface GameOverListener {
        void onScoreUpdate(int score);
        void onDiffUpdate(int diff);
        void onGameOver();
        void onGameWin();
    }

    public void setGameOverListener(GameOverListener listener) {
        this.listener = listener;
    }

    public void setRecognitionManager(RecognitionManager manager) {
        this.recognitionManager = manager;
    }

    public GameView(Context context) {
        super(context);
        init();
    }

    private void init() {
        // 1. Cấu hình chữ rơi
        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize(80);
        textPaint.setFakeBoldText(true);

        // 2. Cấu hình nét vẽ
        drawPaint.setColor(Color.BLUE);
        drawPaint.setStrokeWidth(15);
        drawPaint.setStyle(Paint.Style.STROKE);
        drawPaint.setStrokeJoin(Paint.Join.ROUND);

        // 3. Cấu hình hiển thị tiến độ
        hudPaintActive.setColor(Color.RED); // Chữ đã nhặt được màu đỏ
        hudPaintActive.setTextSize(50);
        hudPaintActive.setFakeBoldText(true);

        hudPaintInactive.setColor(Color.LTGRAY); // Chữ chưa nhặt màu xám
        hudPaintInactive.setTextSize(50);
        hudPaintInactive.setFakeBoldText(true);

        startGameLoop();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        screenWidth = w;
        screenHeight = h;
    }

    private class FallingChar {
        String character;
        float x, y;
        float speed;
        boolean isTarget; // Đánh dấu đây là chữ cái cần thu thập hay quái vật

        FallingChar(String c, float x, float y, float s, boolean isTarget) {
            this.character = c;
            this.x = x;
            this.y = y;
            this.speed = s;
            this.isTarget = isTarget;
        }
    }

    private void startGameLoop() {
        final Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (!isGameOver) {
                    updateGame();
                    invalidate();
                    handler.postDelayed(this, 16);
                }
            }
        });
    }

    private void updateGame() {
        // Tính toán tốc độ spawn dựa trên diff
        long currentSpawnDelay = Math.max(600, 2500 - (diff * 50));

        if (System.currentTimeMillis() - lastSpawnTime > currentSpawnDelay) {

            // LOGIC SPAWN QUÁI
            boolean isTargetAlreadyOnScreen = false;
            for (FallingChar fc : fallingChars) {
                if (fc.isTarget) {
                    isTargetAlreadyOnScreen = true;
                    break;
                }
            }
            String charToSpawn;
            boolean isTargetChar = false;
            int targetSpawnRate = 20; // tỈ lệ sinh ra mục tiêu
            // Chỉ xuất hiện chữ cái chưa có
            boolean shouldSpawnTarget = !isTargetAlreadyOnScreen
                    && random.nextInt(100) < targetSpawnRate
                    && collectedIndex < TARGET_FULL.length();

            if (shouldSpawnTarget) {
                // Lấy chữ cái tiếp theo cần thu thập
                char nextNeed = TARGET_FULL.charAt(collectedIndex);

                // Phase 1: Chỉ cho phép spawm các chữ trong "hell"
                // Phase 2: Chỉ cho phép spawn "oworld" sau khi diff >= 50

                boolean canSpawn = true;
                if (collectedIndex >= 4 && diff < phaseSkipDiff) {
                    // Đã nhặt đủ hell nhưng diff chưa đủ số skip
                    canSpawn = false;
                }

                if (canSpawn) {
                    charToSpawn = String.valueOf(nextNeed);
                    isTargetChar = true;
                    // Tô màu khác cho chữ mục tiêu để người chơi nhận biết (tuỳ chọn)
                } else {
                    // Nếu bị chặn thì spawn quái thường
                    charToSpawn = String.valueOf(ENEMIES_CHARS.charAt(random.nextInt(ENEMIES_CHARS.length())));
                }
            } else {
                // Spawn quái vật thường
                charToSpawn = String.valueOf(ENEMIES_CHARS.charAt(random.nextInt(ENEMIES_CHARS.length())));
            }

            // Tính tốc độ rơi
            float baseSpeed = 3 + (diff / 5.0f);
            float finalSpeed = baseSpeed + random.nextInt(3);

            fallingChars.add(new FallingChar(charToSpawn,
                    random.nextInt(screenWidth - 100) + 50,
                    0,
                    finalSpeed,
                    isTargetChar));

            lastSpawnTime = System.currentTimeMillis();
        }

        // Cập nhật vị trí rơi
        Iterator<FallingChar> iter = fallingChars.iterator();
        while (iter.hasNext()) {
            FallingChar fc = iter.next();
            fc.y += fc.speed;

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

        // HUD TIẾN ĐỘ CHƠI
        float startX = 50;
        float startY = screenHeight - 150;
        float spacing = 60;

        for (int i = 0; i < TARGET_FULL.length(); i++) {
            String c = String.valueOf(TARGET_FULL.charAt(i));
            // Vẽ nhạt chữ chưa nhặt, in đậm chữ đã nhặt
            if (i < collectedIndex) {
                canvas.drawText(c, startX + (i * spacing), startY, hudPaintActive);
            } else {
                canvas.drawText(c, startX + (i * spacing), startY, hudPaintInactive);
            }
        }

        // Vẽ các ký tự đang rơi
        for (FallingChar fc : fallingChars) {
            // Màu chữ của mục tiêu chính nổi bật hơn
            if (fc.isTarget) textPaint.setColor(Color.RED);
            else textPaint.setColor(Color.BLACK);

            canvas.drawText(fc.character, fc.x, fc.y, textPaint);
        }

        // Reset màu về mặc định
        textPaint.setColor(Color.BLACK);

        // Vẽ nét vẽ
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
                if (strokeBuilder != null) {
                    strokeBuilder.addPoint(Ink.Point.create(x, y, t));
                }
                break;

            case MotionEvent.ACTION_UP:
                if (strokeBuilder != null) {
                    strokeBuilder.addPoint(Ink.Point.create(x, y, t));
                    inkBuilder.addStroke(strokeBuilder.build());

                    if (recognitionManager != null) {
                        recognitionManager.recognize(inkBuilder.build(), result -> {
                            checkMatch(result);
                        });
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
        // 1. Chuẩn hóa kết quả từ AI
        String textRaw = recognizedText;
        String textUpper = recognizedText.toUpperCase(); // Chuyển về chữ hoa để so sánh chữ cái

        Iterator<FallingChar> iter = fallingChars.iterator();
        while (iter.hasNext()) {
            FallingChar fc = iter.next();
            boolean isMatch = false;

            // Mục tiêu chính
            if (fc.isTarget) {
                if (fc.character.equals(textRaw)) isMatch = true;
            }
            // Quái thường
            else {
                String enemy = fc.character;

                switch (enemy) {
                    case "_":
                        if (textRaw.equals("_") || textRaw.equals("-")) isMatch = true;
                        break;

                    case "^":
                        if (textRaw.equals("^") || textRaw.equals("1") || textRaw.equals("A")) isMatch = true;
                        break;

                    case "/":
                        if (textRaw.equals("/") || textRaw.equals("1") || textRaw.equals("l") || textUpper.equals("I")) isMatch = true;
                        break;

                    case "(":
                        if (textRaw.equals("(")) isMatch = true;
                        break;

                    case ")":
                        if (textRaw.equals(")")) isMatch = true;
                        break;

                    case "<":
                        if (textRaw.equals("<")) isMatch = true;
                        break;

                    case ">":
                        if (textRaw.equals(">")) isMatch = true;
                        break;

                    default:
                        if (enemy.equals(textUpper)) isMatch = true;
                        break;
                }
            }

            if (isMatch) {
                iter.remove();

                // LOGIC GAME

                if (fc.isTarget) {
                    collectedIndex++;

                    // Nếu vừa ghép đủ "hell" (index = 4)
                    if (collectedIndex == 4) {
                        diff = phaseSkipDiff;
                        Toast.makeText(getContext(), "YOU WANT HELL?", Toast.LENGTH_SHORT).show();
                    }

                    // Nếu đã ghép đủ "helloworld" (index = 10)
                    if (collectedIndex >= TARGET_FULL.length()) {
                        isGameOver = true;
                        if (listener != null) listener.onGameWin(); // Thắng rồi đó
                    }

                } else {
                    // Diệt quái thường
                    if (listener != null) {
                        listener.onDiffUpdate(diff);
                    }
                    diff++;
                }
                break;
            }
        }
    }
}