package cuoiki.helloworldgame;

public class FallingChar {
    public String character;
    public float x, y;
    public float speed;
    public boolean isTarget; // Đánh dấu đây là chữ cái cần thu thập hay quái nhỏ

    // Khởi tạo
    public FallingChar(String c, float x, float y, float s, boolean isTarget) {
        this.character = c;
        this.x = x;
        this.y = y;
        this.speed = s;
        this.isTarget = isTarget;
    }

    // Cập nhật vị trí
    public void update() {
        y += speed;
    }
}
