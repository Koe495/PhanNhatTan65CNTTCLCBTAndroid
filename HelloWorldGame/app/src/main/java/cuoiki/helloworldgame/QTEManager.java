package cuoiki.helloworldgame;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.view.MotionEvent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class QTEManager {

    public interface QTEListener {
        void onLaserFire(Path laserPath);
        void onQTEFail();
    }

    // Class nội bộ lưu trữ các sự kiện cố định (scripted)
    private static class ScheduledQTE {
        String type;
        float spawnTime;
        float x, y;
        float angle;

        public ScheduledQTE(String t, float time, float x, float y, float angle) {
            this.type = t; this.spawnTime = time; this.x = x; this.y = y; this.angle = angle;
        }
    }

    private Context context;
    private int screenWidth, screenHeight;
    private float centerX, centerY;

    private List<QTEObject> activeQTEs = new ArrayList<>();
    private List<ScheduledQTE> timeline = new ArrayList<>();
    private QTEListener listener;
    private SoundManager soundManager;

    private Bitmap blasterPre, blasterPost;
    private float gameTime = 0;
    private boolean isGameOver = false; // Trạng thái game

    // --- Biến cho hệ thống Random Spawn ---
    private Random random = new Random();
    private static final float START_AUTO_SPAWN_TIME = 35.0f; // Bắt đầu sau giây 35
    private float FIXED_BLASTER_SPAWN_RATE = 2.0f;
    private float RANDOM_BLASTER_SPAWN_RATE = 5.0f;
    private float nextRandomSpawnTime = START_AUTO_SPAWN_TIME; // Thời điểm spawn con tiếp theo
    // -------------------------------------

    private Paint laserCorePaint;
    private Paint laserGlowPaint;

    public QTEManager(Context context, int screenWidth, int screenHeight) {
        this.context = context;
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        this.centerX = screenWidth / 2f;
        this.centerY = screenHeight / 2f;

        loadResources();
        initPaints();
        initTimeline(); // Sự kiện cố định
    }

    public void setListener(QTEListener listener) {
        this.listener = listener;
    }

    public void setSoundManager(SoundManager soundManager) {
        this.soundManager = soundManager;
    }

    // Hàm gọi khi thua hoặc thoát để dừng spawn
    public void setGameOver(boolean gameOver) {
        this.isGameOver = gameOver;
        if (gameOver) {
            activeQTEs.clear(); // Xóa hết QTE đang hiển thị nếu muốn
        }
    }

    private void loadResources() {
        Bitmap pre = BitmapFactory.decodeResource(context.getResources(), R.drawable.blaster_ready);
        Bitmap post = BitmapFactory.decodeResource(context.getResources(), R.drawable.blaster_fire);

        // Resize ảnh nếu cần (giữ nguyên logic của bạn)
        blasterPre = Bitmap.createScaledBitmap(pre, 420, 550, true);
        blasterPost = Bitmap.createScaledBitmap(post, 420, 550, true);
    }

    private void initPaints() {
        laserCorePaint = new Paint();
        laserCorePaint.setColor(Color.WHITE);
        laserCorePaint.setStyle(Paint.Style.FILL);
        laserCorePaint.setAntiAlias(true);

        laserGlowPaint = new Paint();
        laserGlowPaint.setColor(Color.CYAN);
        laserGlowPaint.setStyle(Paint.Style.STROKE);
        laserGlowPaint.setStrokeJoin(Paint.Join.ROUND);
        laserGlowPaint.setStrokeCap(Paint.Cap.ROUND);
        laserGlowPaint.setMaskFilter(new BlurMaskFilter(30, BlurMaskFilter.Blur.NORMAL));
    }

    private void initTimeline() {
        // Các sự kiện cố định ban đầu (nếu vẫn muốn giữ)
        timeline.add(new ScheduledQTE("BLASTER1", 33.2f, screenWidth / 2f, screenHeight - 300f, 0f));

        // Test cases cũ
        timeline.add(new ScheduledQTE("BLASTER1", 3.0f, screenWidth/2f, 300f, 180f));
        timeline.add(new ScheduledQTE("BLASTER1", 5.0f, screenWidth - 100f, screenHeight / 2f, 270f));
        timeline.add(new ScheduledQTE("BLASTER1", 10.0f, 100f, screenHeight - 300f, 45f));
        timeline.add(new ScheduledQTE("BLASTER1", 15.0f, 100f, screenHeight / 2f, 90f));
    }

    public void update(float deltaTime) {
        if (isGameOver) return; // Dừng logic nếu game over

        gameTime += deltaTime;

        // 1. Xử lý Timeline cố định (Scripted events)
        Iterator<ScheduledQTE> scheduleIter = timeline.iterator();
        while (scheduleIter.hasNext()) {
            ScheduledQTE s = scheduleIter.next();
            if (gameTime >= s.spawnTime) {
                spawnFixedQTE(s);
                scheduleIter.remove();
            }
        }

        // 2. Xử lý Auto Random Spawn (Sau giây 35)
        if (gameTime >= START_AUTO_SPAWN_TIME) {
            if (gameTime >= nextRandomSpawnTime) {
                spawnRandomBlaster();

                // Random thời gian cho lần kế tiếp:
                float interval = FIXED_BLASTER_SPAWN_RATE + random.nextFloat() * RANDOM_BLASTER_SPAWN_RATE;
                nextRandomSpawnTime = gameTime + interval;
            }
        }

        // 3. Update các QTE đang hoạt động
        Iterator<QTEObject> qteIter = activeQTEs.iterator();
        while (qteIter.hasNext()) {
            QTEObject qte = qteIter.next();
            qte.update(deltaTime);

            if (qte.state == QTEObject.State.FINISHED) {
                qteIter.remove();
            }
            else if (qte.state == QTEObject.State.EXPIRED) {
                if (listener != null) listener.onQTEFail();
                qteIter.remove();
            }
            else if (qte.state == QTEObject.State.FIRING) {
                if (listener != null) {
                    listener.onLaserFire(qte.getLaserPath());
                }
            }
        }
    }

    // Logic spawn theo timeline
    private void spawnFixedQTE(ScheduledQTE s) {
        if (s.type.equals("BLASTER1")) {
            createBlaster(s.x, s.y, s.angle);
        }
    }

    // Logic spawn ngẫu nhiên mới
    private void spawnRandomBlaster() {
        // 1. Định nghĩa bán kính ảnh Blaster1
        float halfW = 420f / 2f; // 210
        float halfH = 550f / 2f; // 275

        // 2. Padding
        float safePadX = halfW + 10f;
        float safePadY = halfH + 10f;

        int side = random.nextInt(4);
        float spawnX = 0, spawnY = 0;

        switch (side) {
            case 0: // Top (Sát mép trên)
                // X: Random trong khoảng an toàn (từ trái qua phải)
                spawnX = safePadX + random.nextFloat() * (screenWidth - 2 * safePadX);
                // Y: Cố định tại padding trên
                spawnY = safePadY;
                break;

            case 1: // Right (Sát mép phải)
                // X: Cố định tại mép phải trừ padding
                spawnX = screenWidth - safePadX;
                // Y: Random trong khoảng an toàn (từ trên xuống dưới)
                spawnY = safePadY + random.nextFloat() * (screenHeight - 2 * safePadY);
                break;

            case 2: // Bottom (Sát mép dưới)
                spawnX = safePadX + random.nextFloat() * (screenWidth - 2 * safePadX);
                spawnY = screenHeight - safePadY;
                break;

            case 3: // Left (Sát mép trái)
                spawnX = safePadX;
                spawnY = safePadY + random.nextFloat() * (screenHeight - 2 * safePadY);
                break;
        }

        // 2. Tính góc xoay hướng vào tâm màn hình
        // Math.atan2 trả về góc tính từ trục X dương (Right).
        // Hệ thống toạ độ màn hình: Y tăng dần xuống dưới.
        float deltaX = centerX - spawnX;
        float deltaY = centerY - spawnY;

        // Tính góc radian
        double angleRad = Math.atan2(deltaY, deltaX);

        // Đổi sang độ
        float angleDeg = (float) Math.toDegrees(angleRad);

        // Điều chỉnh góc:
        // Trong QTEObject, logic vẽ và laser giả định 0 độ là hướng lên trên (UP - trục Y âm).
        // atan2(0, 1) = 0 độ (Right). Để Right thành 90 độ (theo đồng hồ) hoặc khớp hệ quy chiếu,
        // ta cần +90 độ để chuyển từ hệ Atan (0=Right) sang hệ Game (0=Up).
        angleDeg += 90f;

        createBlaster(spawnX, spawnY, angleDeg);
    }

    // Hàm chung để tạo đối tượng
    private void createBlaster(float x, float y, float angle) {
        activeQTEs.add(new QTEObject(
                blasterPre, blasterPost,
                x, y, angle,
                1.5f // Thời gian tồn tại (Time to live)
        ));
        if (soundManager != null) soundManager.playBlasterAppear();
    }

    public void draw(Canvas canvas) {
        for (QTEObject qte : activeQTEs) {
            qte.draw(canvas, laserCorePaint, laserGlowPaint);
        }
    }

    public void handleTouch(MotionEvent event) {
        if (isGameOver) return; // Không nhận cảm ứng nếu game over

        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            float x = event.getX();
            float y = event.getY();

            for (QTEObject qte : activeQTEs) {
                if (qte.state == QTEObject.State.READY) {
                    if (qte.checkTouch(x, y)) {
                        qte.triggerFire();
                        if (soundManager != null) soundManager.playBlasterFire();
                        break;
                    }
                }
            }
        }
    }
}