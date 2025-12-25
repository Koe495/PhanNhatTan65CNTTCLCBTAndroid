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
    private ArrayList<FallingChar> fallingSuits = new ArrayList<>();
    private Context context;
    private Random random = new Random();

    // UI Config
    private Paint cardPaint = new Paint();
    private Paint textPaint = new Paint();

    // --- Biến chứa ảnh chất bài ---
    private Bitmap bmpHeart, bmpDiamond, bmpClub, bmpSpade;
    private int iconSize = 100;

    public JokerGameLogic(Context context) {
        this.context = context;
        deckManager = new JokerDeckManager(context);
        deckManager.drawCards(5);

        // textPaint.setColor(Color.WHITE); // Bỏ
        // textPaint.setTextSize(60); // Bỏ
        // textPaint.setTypeface(Typeface.DEFAULT_BOLD);

        initSuitIcons(); // Gọi hàm tải ảnh
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
            deckManager.drawCards(5);
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
        // 1. Tạo kẻ địch
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

        // 2. tạo bài trên tay
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

        // --------------------------------------------------------

        if (textUpper.equals("9") || textUpper.equals("Q")) {
            boolean has9 = !deckManager.findCardsByRank("9").isEmpty();
            boolean hasQ = !deckManager.findCardsByRank("Q").isEmpty();

            if (has9 && hasQ) {
                // TRƯỜNG HỢP 1: Có cả 9 và Q
                // vẽ q hoặc 9 thì ưu tiên 9
                textUpper = "9";
            }
            else if (!has9 && hasQ) {
                // TRƯỜNG HỢP 2: Không có 9, chỉ có Q
                // vẽ 9 ưu tiên Q (biến 9 thành Q)
                textUpper = "Q";
            }
            else if (has9 && !hasQ) {
                // TRƯỜNG HỢP 3: Không có Q, chỉ có 9
                // vẽ q ưu tiên 9 (biến Q thành 9)
                textUpper = "9";
            }
        }
        if (textUpper.equals("0") || textUpper.equals("X") || textUpper.equals("O")) {
            textUpper = "10";
        }

        // --------------------------------------------------------

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
                hitAny = true;
                break;
            }
        }

        if (deckManager.getHand().size() < 3) {
            deckManager.drawCards(2);
        }

        return hitAny;
    }
}