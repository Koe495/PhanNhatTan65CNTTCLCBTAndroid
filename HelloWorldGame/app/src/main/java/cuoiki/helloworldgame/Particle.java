package cuoiki.helloworldgame;

import java.util.Random;

public class Particle {
    public float x, y;
    public float vx, vy; // Vận tốc
    public int color;
    public int alpha;    // Độ trong suốt
    public float size;

    private static final Random random = new Random();

    public Particle(float x, float y, int color, float size) {
        this.x = x;
        this.y = y;
        this.color = color;
        this.size = size;
        this.alpha = 255;

        double angle = random.nextDouble() * 2 * Math.PI;
        float speed = random.nextFloat() * 10 + 25; // Tốc độ bay

        this.vx = (float) (Math.cos(angle) * speed);
        this.vy = (float) (Math.sin(angle) * speed);
    }

    public void update() {
        x += vx;
        y += vy;
        alpha -= 50;
        if (alpha < 0) alpha = 0;
    }

    public boolean isDead() {
        return alpha <= 0;
    }
}