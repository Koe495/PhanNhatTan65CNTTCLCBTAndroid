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
    private Bitmap bossBitmap1; // Ảnh khi máu > 50%
    private Bitmap bossBitmap2; // Ảnh khi máu <= 50%
    private Paint paint = new Paint();

    public interface BossListener {
        void onHpChanged(int currentHp, int maxHp);
    }

    private BossListener listener;

    public void setListener(BossListener listener) {
        this.listener = listener;
    }

    public JokerBoss(Context context, int maxHp, int bossResId1, int bossResId2) {
        this.maxHp = maxHp;
        this.hp = maxHp;

        // Load hình ảnh boss làm nền
        Bitmap src1 = BitmapFactory.decodeResource(context.getResources(), bossResId1);
        if (src1 != null) {
            this.bossBitmap1 = src1;
        }
        Bitmap src2 = BitmapFactory.decodeResource(context.getResources(),bossResId2);
        if (src2 != null) {
            this.bossBitmap2 = src2;
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
        Bitmap currentBitmap;

        if (hp <= maxHp / 2) {
            // Nếu máu <= 50%, dùng ảnh 2 (nếu có, không thì dùng ảnh 1)
            currentBitmap = (bossBitmap2 != null) ? bossBitmap2 : bossBitmap1;
        } else {
            // Nếu máu > 50%, dùng ảnh 1
            currentBitmap = bossBitmap1;
        }

        // Vẽ ảnh đã chọn
        if (currentBitmap != null) {
            Rect dstBoss = new Rect(0, 0, screenWidth, screenHeight);
            canvas.drawBitmap(currentBitmap, null, dstBoss, paint);
        }
    }

}