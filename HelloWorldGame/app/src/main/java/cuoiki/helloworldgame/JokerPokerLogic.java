package cuoiki.helloworldgame;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JokerPokerLogic {

    public static class HandResult {
        public String name;
        public int bonusDamage;
        public boolean isSpecial;

        public HandResult(String name, int bonusDamage, boolean isSpecial) {
            this.name = name;
            this.bonusDamage = bonusDamage;
            this.isSpecial = isSpecial;
        }
    }

    public static HandResult checkHand(List<Card> playedCards) {
        if (playedCards.size() < 2) return new HandResult("None", 0, false);

        int size = playedCards.size();
        Map<String, Integer> rankCounts = new HashMap<>();
        Map<String, Integer> suitCounts = new HashMap<>();
        List<Integer> values = new ArrayList<>();

        for (Card c : playedCards) {
            rankCounts.put(c.rank, rankCounts.getOrDefault(c.rank, 0) + 1);
            suitCounts.put(c.suit.name, suitCounts.getOrDefault(c.suit.name, 0) + 1);
            values.add(getRankValue(c.rank));
        }
        Collections.sort(values);

        // Kiểm tra Flush (Cùng chất - tối thiểu 5 lá)
        boolean isFlush = false;
        for (int count : suitCounts.values()) {
            if (count >= 5) isFlush = true;
        }

        // Kiểm tra Straight (Sảnh - tối thiểu 5 lá liên tiếp)
        boolean isStraight = false;
        if (values.size() >= 5) {
            int uniqueCount = 1;
            for (int i = 0; i < values.size() - 1; i++) {
                if (values.get(i + 1) == values.get(i) + 1) uniqueCount++;
                else if (values.get(i + 1) != values.get(i)) uniqueCount = 1;
                if (uniqueCount >= 5) isStraight = true;
            }
        }

        // Đếm các bộ trùng (Pair, Three, Four)
        int pairs = 0, threes = 0, fours = 0;
        for (int count : rankCounts.values()) {
            if (count == 2) pairs++;
            if (count == 3) threes++;
            if (count >= 4) fours++;
        }

        // Xét ưu tiên từ cao xuống thấp
        if (isFlush && isStraight) return new HandResult("Straight Flush", 25, true);
        if (fours > 0) return new HandResult("Four of a Kind", 16, true);
        if (threes > 0 && pairs > 0) return new HandResult("Full House", 14, true);
        if (isFlush) return new HandResult("Flush", 12, true);
        if (isStraight) return new HandResult("Straight", 10, true);
        if (threes > 0) return new HandResult("Three of a Kind", 6, true);
        if (pairs >= 2) return new HandResult("Two Pair", 4, true);
        if (pairs == 1) return new HandResult("Pair", 2, true);

        return new HandResult("None", 0, false);
    }

    private static int getRankValue(String rank) {
        switch (rank.toUpperCase()) {
            case "A": return 14;
            case "K": return 13;
            case "Q": return 12;
            case "J": return 11;
            case "10": return 10;
            default: return Integer.parseInt(rank);
        }
    }
}