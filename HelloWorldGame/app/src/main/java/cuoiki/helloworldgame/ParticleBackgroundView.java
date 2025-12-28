package cuoiki.helloworldgame;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class ParticleBackgroundView extends View {

    // Đổi tên class từ BlackParticle -> Particle cho tổng quát
    private class Particle {
        float x, y;
        float speed;
        float size;
        int alpha;

        Particle(float x, float y, float speed, float size) {
            this.x = x;
            this.y = y;
            this.speed = speed;
            this.size = size;
            this.alpha = 255;
        }
    }

    private List<Particle> particles = new ArrayList<>();
    private Paint paint;
    private Random random = new Random();
    private Handler handler = new Handler(Looper.getMainLooper());
    private boolean isAnimating = false;
    private long lastSpawnTime = 0;

    // Mặc định là màu đen
    private int particleColor = Color.BLACK;

    public ParticleBackgroundView(Context context) {
        super(context);
        init();
    }

    public ParticleBackgroundView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        paint = new Paint();
        paint.setColor(particleColor);
        paint.setStyle(Paint.Style.FILL);
    }

    // --- HÀM MỚI: ĐỔI MÀU PARTICLE ---
    public void setParticleColor(int color) {
        this.particleColor = color;
        // Cập nhật ngay màu cho bút vẽ
        paint.setColor(this.particleColor);
    }

    public void startAnimation() {
        if (isAnimating) return; // Tránh start chồng chéo
        isAnimating = true;
        particles.clear();
        invalidate();
    }

    public void stopAnimation() {
        isAnimating = false;
        particles.clear();
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (!isAnimating) return;

        int w = getWidth();
        int h = getHeight();

        // 1. Spawn particles mới
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastSpawnTime > 50) { // Spawn mỗi 50ms
            float size = 5 + random.nextInt(15); // Kích thước nhỏ
            float x = random.nextInt(w);
            float speed = 3 + random.nextInt(5);
            particles.add(new Particle(x, h + size, speed, size));
            lastSpawnTime = currentTime;
        }

        // 2. Update & Draw
        Iterator<Particle> iterator = particles.iterator();
        while (iterator.hasNext()) {
            Particle p = iterator.next();
            p.y -= p.speed; // Bay lên
            p.alpha -= 2;   // Mờ dần

            if (p.alpha <= 0 || p.y < -50) {
                iterator.remove();
            } else {
                // QUAN TRỌNG: Reset về màu gốc của Theme trước khi set Alpha
                paint.setColor(particleColor);
                paint.setAlpha(p.alpha); // Áp dụng độ mờ
                canvas.drawCircle(p.x, p.y, p.size, paint);
            }
        }

        // 3. Loop
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (isAnimating) {
                    invalidate();
                }
            }
        }, 16);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stopAnimation();
        handler.removeCallbacksAndMessages(null);
    }
}