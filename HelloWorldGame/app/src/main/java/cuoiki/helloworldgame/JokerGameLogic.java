package cuoiki.helloworldgame;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class JokerGameLogic {
    private JokerDeckManager deckManager;
    private JokerBoss boss;

    // Dữ liệu Game
    private List<Card> playedCardsHistory = new ArrayList<>();
    private String lastComboName = "";
    private ArrayList<FallingChar> fallingSuits = new ArrayList<>();
    private Context context;
    private Random random = new Random();
    private SoundManager soundManager;
    private JokerUIListener uiListener;

    // Tài nguyên vẽ
    private Bitmap bmpHeart, bmpDiamond, bmpClub, bmpSpade;
    private int iconSize = 100;

    // Config Game
    private int MAX_PLAYER_CARDS = 6;
    private int MIN_PLAYER_CARDS = 4;
    private int CARDS_DRAWN_PER_TURN = 2;
    private int MAX_PLAYED_CARDS_HISTORY = 5;

    // --- INTERFACE GIAO TIẾP UI ---
    public interface JokerUIListener {
        void onHandUpdate(List<Card> hand);
        void onHistoryUpdate(List<Card> history, String lastCombo);
    }

    public void setUIListener(JokerUIListener listener) {
        this.uiListener = listener;
        // Cập nhật ngay lần đầu
        notifyUI();
    }
    public void setSoundManager(SoundManager sm) {
        this.soundManager = sm;
        if (deckManager != null) {
            deckManager.setSoundManager(sm);
        }
    }
    public JokerGameLogic(Context context, GameMode mode) {
        this.context = context;
        deckManager = new JokerDeckManager(context);
        deckManager.drawCards(5);

        initSuitIcons();

        int initialBossHp = (mode == GameMode.ENDLESS) ? 50 : 250;
        boss = new JokerBoss(
                context,
                initialBossHp,
                R.drawable.joker_boss_1, // Ảnh bình thường
                R.drawable.joker_boss_2  // Ảnh khi < 50% máu
        );
    }

    // Hàm tiện ích để báo cập nhật UI
    private void notifyUI() {
        if (uiListener != null) {
            uiListener.onHandUpdate(deckManager.getHand());
            uiListener.onHistoryUpdate(playedCardsHistory, lastComboName);
        }
    }

    // --- LOGIC GAMEPLAY ---

    public void trySpawnEnemy(int screenWidth) {
        List<Card> hand = deckManager.getHand();
        if (hand.isEmpty()) {
            deckManager.drawCards(MAX_PLAYER_CARDS);
            notifyUI(); // Cập nhật bài trên tay
            return;
        }

        Card randomCard = hand.get(random.nextInt(hand.size()));
        String suitName = randomCard.suit.name;

        FallingChar enemy = new FallingChar(
                suitName,
                random.nextInt(screenWidth - 300) + 50,
                -50,
                2f,
                false
        );
        fallingSuits.add(enemy);
    }

    public boolean checkMatch(String recognizedText) {
        String textUpper = recognizedText.toUpperCase();
        // Logic sửa lỗi nhận diện
        if (textUpper.equals("0") || textUpper.equals("X") || textUpper.equals("O")) textUpper = "10";

        List<Card> matchingRankCards = deckManager.findCardsByRank(textUpper);
        if (matchingRankCards.isEmpty()) return false;

        boolean hitAny = false;

        for (Card card : matchingRankCards) {
            int killPower = getCardPower(card.rank);
            int killedCount = 0;
            boolean cardUsed = false;

            Iterator<FallingChar> enemyIter = fallingSuits.iterator();
            while (enemyIter.hasNext()) {
                FallingChar enemy = enemyIter.next();
                if (enemy.character.equalsIgnoreCase(card.suit.name)) {
                    enemyIter.remove();
                    killedCount++;
                    cardUsed = true;
                    if (killedCount >= killPower) break;
                }
            }

            if (cardUsed) {
                deckManager.removeCardFromHand(card);

                // Quản lý size lịch sử
                if (playedCardsHistory.size() >= MAX_PLAYED_CARDS_HISTORY) {
                    playedCardsHistory.remove(0);
                }
                playedCardsHistory.add(card);

                if (boss != null) boss.takeDamage(1);

                JokerPokerLogic.HandResult result = JokerPokerLogic.checkHand(playedCardsHistory);

                if (result.isSpecial) {
                    boolean triggerCombo = true;

                    // --- LOGIC HOLD HAND---
                    // Nếu là Pair hoặc Three of a Kind VÀ lịch sử chưa đầy -> HOLD
                    if (result.name.contains("Pair") || result.name.contains("Three")) {
                        if (playedCardsHistory.size() < MAX_PLAYED_CARDS_HISTORY) {
                            triggerCombo = false;
                            lastComboName = "Holding: " + result.name + "...";
                        }
                    }

                    if (triggerCombo) {
                        if (boss != null) boss.takeDamage(result.bonusDamage);
                        if (soundManager != null) soundManager.playCardCombo();
                        lastComboName = result.name + " (+" + result.bonusDamage + " DMG)";
                        playedCardsHistory.clear();
                    }
                } else {
                    lastComboName = "";
                }

                hitAny = true;
                break;
            }
        }

        if (deckManager.getHand().size() < MIN_PLAYER_CARDS) {
            deckManager.drawCards(CARDS_DRAWN_PER_TURN);
        }

        // Báo cập nhật UI sau khi đánh bài
        notifyUI();

        return hitAny;
    }

    // --- HÀM XỬ LÝ CLICK TỪ XML---
    public void removeCardFromHistory(int index) {
        if (index >= 0 && index < playedCardsHistory.size()) {
            playedCardsHistory.remove(index);
            if (soundManager != null) soundManager.playCardRemove();

            // Cập nhật lại trạng thái text sau khi xóa
            JokerPokerLogic.HandResult result = JokerPokerLogic.checkHand(playedCardsHistory);
            if (result.isSpecial && playedCardsHistory.size() < MAX_PLAYED_CARDS_HISTORY
                    && (result.name.contains("Pair") || result.name.contains("Three"))) {
                lastComboName = "Holding: " + result.name + "...";
            } else if (result.isSpecial) {
                lastComboName = "Ready: " + result.name;
            } else {
                lastComboName = "Card Removed";
            }

            notifyUI();
        }
    }

    // --- LOGIC VẼ ---
    public void draw(Canvas canvas, int screenWidth, int screenHeight) {
        // 1. Vẽ Boss
        if (boss != null) {
            boss.draw(canvas, screenWidth, screenHeight);
        }

        // 2. Vẽ Kẻ Địch (Falling Suits)
        for (FallingChar fc : fallingSuits) {
            Bitmap bitmapToDraw = null;
            if (fc.character.equalsIgnoreCase("heart")) bitmapToDraw = bmpHeart;
            else if (fc.character.equalsIgnoreCase("diamond")) bitmapToDraw = bmpDiamond;
            else if (fc.character.equalsIgnoreCase("club")) bitmapToDraw = bmpClub;
            else if (fc.character.equalsIgnoreCase("spade")) bitmapToDraw = bmpSpade;

            if (bitmapToDraw != null) {
                canvas.drawBitmap(bitmapToDraw, fc.x - iconSize / 2f, fc.y - iconSize / 2f, null);
            }
        }
    }

    public int update(int screenHeight) {
        int damageTaken = 0;
        Iterator<FallingChar> iter = fallingSuits.iterator();
        while (iter.hasNext()) {
            FallingChar fc = iter.next();
            fc.update();
            if (fc.y > screenHeight) {
                iter.remove();
                damageTaken++;
            }
        }
        return damageTaken;
    }

    public JokerBoss getBoss() {
        return this.boss;
    }

    private int getCardPower(String rank) {
        rank = rank.toLowerCase();
        switch (rank) {
            case "a": return 1;
            case "j": case "q": case "k": case "10": return 10;
            default: try { return Integer.parseInt(rank); } catch (NumberFormatException e) { return 1; }
        }
    }

    private void initSuitIcons() {
        Bitmap originalHeart = BitmapFactory.decodeResource(context.getResources(), R.drawable.suit_heart);
        Bitmap originalDiamond = BitmapFactory.decodeResource(context.getResources(), R.drawable.suit_diamond);
        Bitmap originalClub = BitmapFactory.decodeResource(context.getResources(), R.drawable.suit_club);
        Bitmap originalSpade = BitmapFactory.decodeResource(context.getResources(), R.drawable.suit_spade);

        if (originalHeart != null) bmpHeart = Bitmap.createScaledBitmap(originalHeart, iconSize, iconSize, true);
        if (originalDiamond != null) bmpDiamond = Bitmap.createScaledBitmap(originalDiamond, iconSize, iconSize, true);
        if (originalClub != null) bmpClub = Bitmap.createScaledBitmap(originalClub, iconSize, iconSize, true);
        if (originalSpade != null) bmpSpade = Bitmap.createScaledBitmap(originalSpade, iconSize, iconSize, true);
    }
}