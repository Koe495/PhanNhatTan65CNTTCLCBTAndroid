package cuoiki.helloworldgame;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class JokerGameLogic {
    private JokerDeckManager deckManager;
    private JokerBoss boss;
    private List<Card> playedCardsHistory = new ArrayList<>();
    private Paint historyPaint = new Paint();
    private String lastComboName = "";
    private ArrayList<FallingChar> fallingSuits = new ArrayList<>();
    private Context context;
    private Random random = new Random();

    // UI Config
    private Paint cardPaint = new Paint();
    private Paint textPaint = new Paint();

    // --- Biến chứa ảnh chất bài ---
    private Bitmap bmpHeart, bmpDiamond, bmpClub, bmpSpade;
    private int iconSize = 100;

    // Thông số đối tượng
    private int MAX_PLAYER_CARDS = 6;
    private int MIN_PLAYER_CARDS = 4;
    private int CARDS_DRAWN_PER_TURN = 2;
    private int MAX_PLAYED_CARDS_HISTORY = 5;

    public JokerGameLogic(Context context, GameMode mode) {
        this.context = context;
        deckManager = new JokerDeckManager(context);
        deckManager.drawCards(5);

        initSuitIcons();

        // Thiết lập máu Boss tùy theo chế độ
        int initialBossHp;
        if (mode == GameMode.ENDLESS) {
            initialBossHp = 200; // Endless bắt đầu nhẹ nhàng hơn
        } else {
            initialBossHp = 500; // Classic giữ nguyên độ khó
        }

        boss = new JokerBoss(
                context,
                initialBossHp,
                R.drawable.joker_boss_1);
    }

    // --- Tải và resize ảnh chất bài ---
    private void initSuitIcons() {
        // Load ảnh gốc từ resources
        Bitmap originalHeart = BitmapFactory.decodeResource(context.getResources(), R.drawable.suit_heart);
        Bitmap originalDiamond = BitmapFactory.decodeResource(context.getResources(), R.drawable.suit_diamond);
        Bitmap originalClub = BitmapFactory.decodeResource(context.getResources(), R.drawable.suit_club);
        Bitmap originalSpade = BitmapFactory.decodeResource(context.getResources(), R.drawable.suit_spade);

        // Resize ảnh về kích thước chuẩn (100x100)
        if (originalHeart != null) bmpHeart = Bitmap.createScaledBitmap(originalHeart, iconSize, iconSize, true);
        if (originalDiamond != null) bmpDiamond = Bitmap.createScaledBitmap(originalDiamond, iconSize, iconSize, true);
        if (originalClub != null) bmpClub = Bitmap.createScaledBitmap(originalClub, iconSize, iconSize, true);
        if (originalSpade != null) bmpSpade = Bitmap.createScaledBitmap(originalSpade, iconSize, iconSize, true);
    }
    // --- TÍNH SÁT THƯƠNG LÁ BÀI ---
    private int getCardPower(String rank) {
        rank = rank.toLowerCase();
        switch (rank) {
            case "a": return 1;
            case "j":
            case "q":
            case "k":
            case "10": return 10;
            default:
                try {
                    return Integer.parseInt(rank); // Các lá 2, 3, ... 9
                } catch (NumberFormatException e) {
                    return 1;
                }
        }
    }

    // --- LOGIC SPAWN ---
    public void trySpawnEnemy(int screenWidth) {
        List<Card> hand = deckManager.getHand();
        if (hand.isEmpty()) {
            deckManager.drawCards(MAX_PLAYER_CARDS);
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

    // --- LOGIC VẼ ---
    public void draw(Canvas canvas, int screenWidth, int screenHeight) {
        if (boss != null) {
            boss.draw(canvas, screenWidth, screenHeight);
        }
        // Tạo kẻ địch
        for (FallingChar fc : fallingSuits) {
            Bitmap bitmapToDraw = null;

            // Kiểm tra tên chất trong fallingChar để chọn ảnh đúng
            if (fc.character.equalsIgnoreCase("heart")) {
                bitmapToDraw = bmpHeart;
            } else if (fc.character.equalsIgnoreCase("diamond")) {
                bitmapToDraw = bmpDiamond;
            } else if (fc.character.equalsIgnoreCase("club")) {
                bitmapToDraw = bmpClub;
            } else if (fc.character.equalsIgnoreCase("spade")) {
                bitmapToDraw = bmpSpade;
            }

            if (bitmapToDraw != null) {
                // Vẽ ảnh tại vị trí x, y.
                // Trừ đi một nửa kích thước để tâm ảnh nằm đúng tọa độ của kẻ địch.
                canvas.drawBitmap(bitmapToDraw, fc.x - iconSize / 2f, fc.y - iconSize / 2f, null);
            }
        }

        // Tạo combo bài
        int historyX = 20;
        int historyY = 200;
        int historyCardW = 80;
        int historyCardH = 110;

        historyPaint.setColor(Color.YELLOW);
        historyPaint.setTextSize(40);
        canvas.drawText("Combo List:", historyX, historyY - 20, historyPaint);

        for (int i = 0; i < playedCardsHistory.size(); i++) {
            Card c = playedCardsHistory.get(i);
            android.graphics.Rect dst = new android.graphics.Rect(
                    historyX,
                    historyY + (i * (historyCardH + 10)),
                    historyX + historyCardW,
                    historyY + (i * (historyCardH + 10)) + historyCardH
            );
            if (c.bitmap != null) {
                canvas.drawBitmap(c.bitmap, null, dst, null);
            }
        }

        // Vẽ tên combo vừa đạt được
        if (!lastComboName.isEmpty()) {
            historyPaint.setColor(Color.CYAN);
            canvas.drawText(lastComboName, historyX, historyY - 60, historyPaint);
        }
        // tạo bài trên tay
        List<Card> hand = deckManager.getHand();
        int displayCardWidth = 150;
        int displayCardHeight = 210;
        int startX = (screenWidth - (hand.size() * (displayCardWidth + 20))) / 2;
        int y = screenHeight - displayCardHeight - 50;

        for (int i = 0; i < hand.size(); i++) {
            Card c = hand.get(i);
            if (c.bitmap != null) {
                android.graphics.Rect dstRect = new android.graphics.Rect(startX, y, startX + displayCardWidth, y + displayCardHeight);
                canvas.drawBitmap(c.bitmap, null, dstRect, null);
            } else {
                cardPaint.setColor(Color.WHITE);
                canvas.drawRect(startX, y, startX + displayCardWidth, y + displayCardHeight, cardPaint);
            }
            startX += displayCardWidth + 20;
        }
    }

    // --- LOGIC UPDATE ---
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

    // --- LOGIC CHECK MATCH ---
    public boolean checkMatch(String recognizedText) {
        String textRaw = recognizedText;
        String textUpper = recognizedText.toUpperCase();

        if (textUpper.equals("0") || textUpper.equals("X") || textUpper.equals("O")) {
            textUpper = "10";
        }

        // 1. Tìm các lá bài trùng Rank
        List<Card> matchingRankCards = deckManager.findCardsByRank(textUpper);
        if (matchingRankCards.isEmpty()) return false;

        boolean hitAny = false;

        // 2. Duyệt qua từng lá bài (Copy lại logic sát thương diện rộng)
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
                if (playedCardsHistory.size() >= MAX_PLAYED_CARDS_HISTORY) {
                    playedCardsHistory.remove(0);
                }
                // 1. Thêm vào danh sách lịch sử để xét bộ
                playedCardsHistory.add(card);

                // 2. Sát thương cơ bản
                if (boss != null) {
                    boss.takeDamage(1);
                }

                // 3. Kiểm tra bộ đặc biệt (Poker Hand)
                JokerPokerLogic.HandResult result = JokerPokerLogic.checkHand(playedCardsHistory);
                if (result.isSpecial) {
                    // Nếu là bộ đặc biệt, gây thêm sát thương và reset danh sách
                    if (boss != null) {
                        boss.takeDamage(result.bonusDamage);
                    }
                    lastComboName = result.name + " (+" + result.bonusDamage + " DMG)";
                    playedCardsHistory.clear(); // Reset sau khi đạt bộ
                }

                hitAny = true;
                break;
            }

        }

        if (deckManager.getHand().size() < MIN_PLAYER_CARDS) {
            deckManager.drawCards(CARDS_DRAWN_PER_TURN);
        }

        return hitAny;
    }

//     Xử lý khi người chơi nhấn vào màn hình.
//     *Kiểm tra xem tọa độ nhấn có trùng với lá bài nào trong lịch sử không.

    public boolean handleTouch(float touchX, float touchY) {
        // Tọa độ phải khớp với logic vẽ trong hàm draw()
        int historyX = 20;
        int historyY = 200;
        int historyCardW = 80;
        int historyCardH = 110;

        for (int i = 0; i < playedCardsHistory.size(); i++) {
            // Tính toán Rect (vùng va chạm) của từng lá bài lịch sử
            int top = historyY + (i * (historyCardH + 10));
            int bottom = top + historyCardH;
            int left = historyX;
            int right = left + historyCardW;

            // Nếu tọa độ nhấn nằm trong Rect của lá bài
            if (touchX >= left && touchX <= right && touchY >= top && touchY <= bottom) {
                playedCardsHistory.remove(i);
                lastComboName = "Card Removed"; // Thông báo nhỏ
                return true;
            }
        }
        return false;
    }

    public JokerBoss getBoss() {
        return this.boss;
    }
}