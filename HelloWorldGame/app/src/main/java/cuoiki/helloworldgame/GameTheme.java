package cuoiki.helloworldgame;

public class GameTheme {
    String name;
    int bgColor;
    int textColor;
    int strokeColor;
    int fontResId;
    int musicResId;
    int musicPhase2ResId;
    int sfxNormalId;        // pop1
    int sfxTargetId;        // pop2
    int musicEndlessId;


    // --- MÀU SẮC RIÊNG CHO HIGH SCORE SCREEN ---
    public int hsBackgroundColor;   // Màu nền màn hình HS
    public int hsParticleColor;     // Màu hạt bay
    public int hsTitleColor;        // Màu tiêu đề
    public int hsLabelClassicColor; // Màu chữ "Classic Mode"
    public int hsLabelEndlessColor; // Màu chữ "Endless Mode"
    public int hsScoreClassicColor; // Màu danh sách điểm Classic
    public int hsScoreEndlessColor; // Màu danh sách điểm Endless

    public GameTheme(String name, int bgColor, int textColor, int strokeColor, int fontResId,
                     int musicResId, int musicPhase2ResId, int sfxExplodeId, int sfxExplodeTargetId, int musicEndlessId,
                     // Các tham số cho High Score
                     int hsBackgroundColor, int hsParticleColor, int hsTitleColor,
                     int hsLabelClassicColor, int hsLabelEndlessColor,
                     int hsScoreClassicColor, int hsScoreEndlessColor) {
        this.name = name;
        this.textColor = textColor;
        this.bgColor = bgColor;
        this.strokeColor = strokeColor;
        this.fontResId = fontResId;
        this.musicResId = musicResId;
        this.musicPhase2ResId = musicPhase2ResId;
        this.sfxNormalId = sfxExplodeId;
        this.sfxTargetId = sfxExplodeTargetId;
        this.musicEndlessId = musicEndlessId;

        this.hsBackgroundColor = hsBackgroundColor;
        this.hsParticleColor = hsParticleColor;
        this.hsTitleColor = hsTitleColor;
        this.hsLabelClassicColor = hsLabelClassicColor;
        this.hsLabelEndlessColor = hsLabelEndlessColor;
        this.hsScoreClassicColor = hsScoreClassicColor;
        this.hsScoreEndlessColor = hsScoreEndlessColor;
    }
}