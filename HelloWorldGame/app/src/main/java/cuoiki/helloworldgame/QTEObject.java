package cuoiki.helloworldgame;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;

public class QTEObject {

    public enum State { READY, FIRING, FINISHED, EXPIRED }

    // Cấu hình
    private Bitmap bmpReady, bmpFire;
    private float x, y;
    private float angle; // Độ i i
    private float timeToLive;
    private float maxLifeTime;

    // Trạng thái Animation
    public State state = State.READY;
    private float fireTimer = 0;
    private float totalFireDuration = 0.5f;

    // Hiệu ứng Recoil & Laser
    private float currentRecoil = 0;
    private float laserWidth = 0;
    private Path laserPath = new Path();

    public QTEObject(Bitmap ready, Bitmap fire, float x, float y, float angle, float ttl) {
        this.bmpReady = ready;
        this.bmpFire = fire;
        this.x = x;
        this.y = y;
        this.angle = angle;
        this.timeToLive = ttl;
        this.maxLifeTime = ttl;
    }

    public void triggerFire() {
        state = State.FIRING;
        fireTimer = 0;
        timeToLive = 999;
    }

    public void update(float dt) {
        if (state == State.READY) {
            timeToLive -= dt;
            if (timeToLive <= 0) {
                state = State.EXPIRED;
            }
        }
        else if (state == State.FIRING) {
            fireTimer += dt;

            // 1. Tính toán Recoil
            float progress = fireTimer / totalFireDuration;
            if (progress < 0.2f) {
                currentRecoil = (progress / 0.2f) * 60f;
            } else {
                currentRecoil = 60f * (1 - (progress - 0.2f) / 0.8f);
            }

            // 2. Tính toán độ rộng Laser
            float maxLaserWidth = 400f;

            if (progress < 0.1f) {
                laserWidth = (progress / 0.1f) * maxLaserWidth;
            } else {
                laserWidth = maxLaserWidth * (1 - (progress - 0.1f) / 0.9f);
            }

            // 3. Cập nhật vùng va chạm Laser (Path)
            updateLaserPath();

            if (fireTimer >= totalFireDuration) {
                state = State.FINISHED;
            }
        }
    }

    private void updateLaserPath() {
        laserPath.reset();
        if (laserWidth <= 0) return;

        double rad = Math.toRadians(angle);
        float dirX = (float) Math.sin(rad);
        float dirY = -(float) Math.cos(rad);

        float startX = x - (dirX * currentRecoil);
        float startY = y - (dirY * currentRecoil);

        float beamLength = 2500f;
        float endX = startX + (dirX * beamLength);
        float endY = startY + (dirY * beamLength);

        float perpX = -dirY;
        float perpY = dirX;

        float halfW = laserWidth / 2;

        laserPath.moveTo(startX - perpX * halfW, startY - perpY * halfW);
        laserPath.lineTo(endX - perpX * halfW, endY - perpY * halfW);
        laserPath.lineTo(endX + perpX * halfW, endY + perpY * halfW);
        laserPath.lineTo(startX + perpX * halfW, startY + perpY * halfW);
        laserPath.close();
    }

    public void draw(Canvas canvas, Paint corePaint, Paint glowPaint) {
        canvas.save();

        double rad = Math.toRadians(angle);
        float drawX = x - (float)(Math.sin(rad) * currentRecoil);
        float drawY = y + (float)(Math.cos(rad) * currentRecoil);

        canvas.rotate(angle, drawX, drawY);

        // 2. Vẽ tia Laser
        if (state == State.FIRING) {
            float halfW = laserWidth / 2;
            glowPaint.setStrokeWidth(laserWidth + 30); // Tăng viền glow
            canvas.drawLine(drawX, drawY - 80, drawX, drawY - 2000, glowPaint);
            canvas.drawRect(drawX - halfW, drawY - 2000, drawX + halfW, drawY - 50, corePaint);
        }

        // 3. Vẽ Blaster
        Bitmap bmp = (state == State.FIRING) ? bmpFire : bmpReady;
        canvas.drawBitmap(bmp, drawX - bmp.getWidth()/2f, drawY - bmp.getHeight()/2f, null);

        // 4. Vẽ chữ TAP
        if (state == State.READY) {
            drawTapHint(canvas, drawX, drawY);
        }

        canvas.restore();
    }

    private void drawTapHint(Canvas canvas, float cx, float cy) {
        Paint p = new Paint();
        p.setColor(0xAAFFFFFF);
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(5);
        p.setTextSize(50); // To hơn chút
        p.setTextAlign(Paint.Align.CENTER);
        p.setFakeBoldText(true);

        canvas.drawText("TAP", cx, cy + 100, p);
    }

    public boolean checkTouch(float touchX, float touchY) {
        // Kiểm tra khoảng cách chạm với tâm Blaster
        // Bán kính vùng chạm khoảng 250px (Vì blaster rộng 420)
        float hitRadius = 250f;
        float dist = (float) Math.hypot(touchX - x, touchY - y);

        return dist <= hitRadius;
    }

    public Path getLaserPath() {
        return laserPath;
    }
}