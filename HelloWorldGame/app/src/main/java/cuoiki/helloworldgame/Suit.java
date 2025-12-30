package cuoiki.helloworldgame;

public enum Suit {
    HEART("heart"),
    DIAMOND("diamond"),
    CLUB("club"),
    SPADE("spade");

    public final String name;

    Suit(String name) {
        this.name = name;
    }
}
