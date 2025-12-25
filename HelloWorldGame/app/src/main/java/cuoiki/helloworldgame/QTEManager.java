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

public class QTEManager {

    public interface QTEListener {
        void onLaserFire(Path laserPath);
        void onQTEFail();
    }

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
    private List<QTEObject> activeQTEs = new ArrayList<>();
    private List<ScheduledQTE> timeline = new ArrayList<>();
    private QTEListener listener;
    private SoundManager soundManager;

    private Bitmap blasterPre, blasterPost;
    private float gameTime = 0;

    private Paint laserCorePaint;
    private Paint laserGlowPaint;

    public QTEManager(Context context, int screenWidth, int screenHeight) {
        this.context = context;
        loadResources();
        initPaints();
        initTimeline(screenWidth, screenHeight);
    }

    public void setListener(QTEListener listener) {
        this.listener = listener;
    }

    public void setSoundManager(SoundManager soundManager) {
        this.soundManager = soundManager;
    }

    private void loadResources() {
        Bitmap pre = BitmapFactory.decodeResource(context.getResources(), R.drawable.blaster_ready);
        Bitmap post = BitmapFactory.decodeResource(context.getResources(), R.drawable.blaster_fire);

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
        laserGlowPaint.setMaskFilter(new BlurMaskFilter(30, BlurMaskFilter.Blur.NORMAL)); // Tăng độ nhoè vì laser to hơn
    }

    private void initTimeline(int w, int h) {
        timeline.add(new ScheduledQTE("BLASTER1", 34.0f, w / 2f, h - 300f, 0f));

        // Test cases
        timeline.add(new ScheduledQTE("BLASTER1", 3.0f, w/2f, 300f, 180f));
        timeline.add(new ScheduledQTE("BLASTER1", 8.0f, w - 100f, h / 2f, 270f));
        timeline.add(new ScheduledQTE("BLASTER1", 5.0f, 100f, h - 300f, 45f));
        timeline.add(new ScheduledQTE("BLASTER1", 10.0f, 100f, h / 2f, 90f));
    }

    public void update(float deltaTime) {
        gameTime += deltaTime;

        Iterator<ScheduledQTE> scheduleIter = timeline.iterator();
        while (scheduleIter.hasNext()) {
            ScheduledQTE s = scheduleIter.next();
            if (gameTime >= s.spawnTime) {
                spawnQTE(s);
                scheduleIter.remove();
            }
        }

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

    private void spawnQTE(ScheduledQTE s) {
        if (s.type.equals("BLASTER1")) {
            activeQTEs.add(new QTEObject(
                    blasterPre, blasterPost,
                    s.x, s.y, s.angle,
                    1.5f // Thời gian chờ click
            ));
            if (soundManager != null) soundManager.playBlasterAppear();
        }
    }

    public void draw(Canvas canvas) {
        for (QTEObject qte : activeQTEs) {
            qte.draw(canvas, laserCorePaint, laserGlowPaint);
        }
    }

    public void handleTouch(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            float x = event.getX();
            float y = event.getY();

            for (QTEObject qte : activeQTEs) {
                if (qte.state == QTEObject.State.READY) {
                    // Kiểm tra chạm vào Blaster
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