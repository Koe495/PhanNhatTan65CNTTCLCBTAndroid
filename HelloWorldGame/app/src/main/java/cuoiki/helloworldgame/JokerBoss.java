package cuoiki.helloworldgame;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

public class JokerBoss {
    public int hp;
    public int maxHp;
    private Bitmap bossBitmap;
    private Paint paint = new Paint();

    public interface BossListener {
        void onHpChanged(int currentHp, int maxHp);
    }

    private BossListener listener;

    public void setListener(BossListener listener) {
        this.listener = listener;
    }

    public JokerBoss(Context context, int maxHp, int bossResId) {
        this.maxHp = maxHp;
        this.hp = maxHp;

        // Load hình ảnh boss làm nền
        Bitmap bossOri = BitmapFactory.decodeResource(context.getResources(), bossResId);
        if (bossOri != null) {
            this.bossBitmap = bossOri;
        }
        paint.setAlpha(60); // Độ mờ cho hình nền
    }

    public void takeDamage(int damage) {
        this.hp -= damage;
        if (this.hp < 0) this.hp = 0;

        if (listener != null) {
            listener.onHpChanged(this.hp, this.maxHp);
        }
    }
    public void revive(int newMaxHp) {
        this.maxHp = newMaxHp;
        this.hp = newMaxHp; // Hồi đầy máu

        if (listener != null) {
            listener.onHpChanged(this.hp, this.maxHp);
        }
    }
    public void draw(Canvas canvas, int screenWidth, int screenHeight) {
        if (bossBitmap != null) {
            Rect dstBoss = new Rect(0, 0, screenWidth, screenHeight);
            canvas.drawBitmap(bossBitmap, null, dstBoss, paint);
        }
    }

}