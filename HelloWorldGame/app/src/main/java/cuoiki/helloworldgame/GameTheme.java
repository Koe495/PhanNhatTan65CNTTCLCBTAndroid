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

    public GameTheme(String name, int bgColor, int textColor, int strokeColor, int fontResId,
                     int musicResId, int musicPhase2ResId,
                     int sfxNormalId, int sfxTargetId, int musicEndlessId) {
        this.name = name;
        this.bgColor = bgColor;
        this.textColor = textColor;
        this.strokeColor = strokeColor;
        this.fontResId = fontResId;
        this.musicResId = musicResId;
        this.musicPhase2ResId = musicPhase2ResId;
        this.sfxNormalId = sfxNormalId;
        this.sfxTargetId = sfxTargetId;
        this.musicEndlessId = musicEndlessId;
    }
}