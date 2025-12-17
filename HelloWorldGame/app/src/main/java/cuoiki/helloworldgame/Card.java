package cuoiki.helloworldgame;

import android.graphics.Bitmap;

public class Card {
    public enum Suit {
        HEART("heart"),
        SPADE("pike"),
        CLOVER("clover"),
        TILE("tile");

        public final String name;
        Suit(String name) { this.name = name; }
    }

    public String rank; // "A", "2"..."K"
    public Suit suit;
    public Bitmap bitmap;

    public Card(String rank, Suit suit, Bitmap bitmap) {
        this.rank = rank;
        this.suit = suit;
        this.bitmap = bitmap;
    }
}