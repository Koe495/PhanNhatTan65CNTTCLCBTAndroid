package cuoiki.helloworldgame;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class JokerDeckManager {
    private List<Card> deck = new ArrayList<>();
    private List<Card> discardPile = new ArrayList<>();
    private List<Card> playerHand = new ArrayList<>();
    private Context context;
    private SoundManager soundManager;

    public JokerDeckManager(Context context) {
        this.context = context;
        initDeck();
    }
    public void setSoundManager(SoundManager sm) {
        this.soundManager = sm;
    }
    private void initDeck() {
        deck.clear();

        // Load tấm ảnh lớn từ Resources
        Bitmap atlas = BitmapFactory.decodeResource(context.getResources(), R.drawable.cards_atlas);

        if (atlas == null) return;

        // Tính kích thước một lá bài
        int cols = 13; // 13 lá mỗi dòng
        int rows = 4;  // 4 chất
        int cardWidth = atlas.getWidth() / cols;
        int cardHeight = atlas.getHeight() / rows;

        String[] ranks = {"a", "2", "3", "4", "5", "6", "7", "8", "9", "10", "j", "q", "k"};

        Suit[] suits = {Suit.HEART, Suit.SPADE, Suit.CLUB, Suit.DIAMOND};

        // Vòng lặp cắt ảnh
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                // Cắt ảnh con từ ảnh lớn
                // createBitmap(source, x, y, width, height)
                Bitmap cardImage = Bitmap.createBitmap(
                        atlas,
                        col * cardWidth, // Vị trí X
                        row * cardHeight, // Vị trí Y
                        cardWidth,
                        cardHeight
                );

                // Tạo đối tượng Card
                deck.add(new Card(ranks[col].toUpperCase(), suits[row], cardImage));
            }
        }

        reshuffle();
    }

    public void reshuffle() {
        deck.addAll(discardPile);
        discardPile.clear();
        Collections.shuffle(deck);
    }

    public void drawCards(int count) {
        for (int i = 0; i < count; i++) {
            if (deck.isEmpty()) {
                if (discardPile.isEmpty()) break;
                reshuffle();
            }
            Card card = deck.remove(0);
            playerHand.add(card);
        }
        if (soundManager != null) {
            soundManager.playCardDraw();
        }
    }

    public List<Card> getHand() { return playerHand; }

    public void removeCardFromHand(Card card) {
        if (playerHand.remove(card)) {
            discardPile.add(card);
        }
    }

    public List<Card> findCardsByRank(String rankInput) {
        List<Card> matches = new ArrayList<>();
        for (Card c : playerHand) {
            if (c.rank.equalsIgnoreCase(rankInput)) {
                matches.add(c);
            }
        }
        return matches;
    }
}