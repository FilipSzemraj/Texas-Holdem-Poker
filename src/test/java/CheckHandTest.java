import Game.Card;
import Game.CheckHand;
import org.junit.Test;

import static Game.CheckHand.*;
import static org.junit.Assert.*;

import java.util.LinkedHashMap;
import java.util.Map;

public class CheckHandTest {

    CheckHand checker=new CheckHand();
    @Test
    public void checkAll_CheckForRoyalFlush_True()
    {
        //given
        Map<Integer, Card> table = new LinkedHashMap<>();
        Map<Integer, Card> hand = new LinkedHashMap<>();
        //karty na stole
        table.put(25, new Card("Osemki", "Pik", 6, 0));
        table.put(29, new Card("Dziewiatki", "Pik", 7, 0));
        table.put(33, new Card("Dziesiatki", "Pik", 8, 0));
        table.put(37, new Card("Jupki", "Pik", 9, 0));
        table.put(41, new Card("Damy", "Pik", 10, 0));
        //karty w rece
        hand.put(45, new Card("Krole", "Pik", 11, 0));
        hand.put(49, new Card("Asy", "Pik", 12, 0));

        //when
        checker.getIdOfCardsOnTable(table);
        checker.getIdOfCardsFromHand(hand);
        checker.combineAndSort();
        //czy posortowane rosnaco
        checker.showCards();

        //then
        assertEquals(ROYAL_FLUSH, checker.checkAll());

    }

    @Test
    public void checkAll_checkForStraightFlush_True()
    {
        //given
        Map<Integer, Card> table = new LinkedHashMap<>();
        Map<Integer, Card> hand = new LinkedHashMap<>();
        //karty na stole
        table.put(25, new Card("Osemki", "Pik", 6, 0));
        table.put(29, new Card("Dziewiatki", "Pik", 7, 0));
        table.put(33, new Card("Dziesiatki", "Pik", 8, 0));
        table.put(37, new Card("Jupki", "Pik", 9, 0));
        table.put(41, new Card("Damy", "Pik", 10, 0));
        //karty w rece
        hand.put(45, new Card("Krole", "Pik", 11, 0));
        hand.put(21, new Card("Siodemki", "Pik", 5, 0));

        //when
        checker.getIdOfCardsOnTable(table);
        checker.getIdOfCardsFromHand(hand);
        checker.combineAndSort();
        //czy posortowane rosnaco
        checker.showCards();

        //then
        assertEquals(STRAIGHT_FLUSH, checker.checkAll());

    }

    @Test
    public void checkAll_checkForFourOfKind_True()
    {
        //given
        Map<Integer, Card> table = new LinkedHashMap<>();
        Map<Integer, Card> hand = new LinkedHashMap<>();
        //karty na stole
        table.put(25, new Card("Osemki", "Pik", 6, 0));
        table.put(29, new Card("Osemki", "Kier", 6, 1));
        table.put(33, new Card("Osemki", "Trefl", 6, 2));
        table.put(37, new Card("Jupki", "Pik", 9, 0));
        table.put(41, new Card("Damy", "Pik", 10, 0));
        //karty w rece
        hand.put(45, new Card("Osemki", "Karo", 6, 3));
        hand.put(21, new Card("Siodemki", "Pik", 5, 0));

        //when
        checker.getIdOfCardsOnTable(table);
        checker.getIdOfCardsFromHand(hand);
        checker.combineAndSort();
        //czy posortowane rosnaco
        checker.showCards();

        //then
        assertEquals(FOUR_OF_A_KIND, checker.checkAll());
    }

    @Test
    public void checkAll_checkForFullHouse_True()
    {
        //given
        Map<Integer, Card> table = new LinkedHashMap<>();
        Map<Integer, Card> hand = new LinkedHashMap<>();
        //karty na stole
        table.put(25, new Card("Osemki", "Pik", 6, 0));
        table.put(29, new Card("Osemki", "Kier", 6, 1));
        table.put(33, new Card("Osemki", "Trefl", 6, 2));
        table.put(37, new Card("Jupki", "Pik", 9, 0));
        table.put(41, new Card("Damy", "Pik", 10, 0));
        //karty w rece
        hand.put(45, new Card("Jupki", "Karo", 9, 3));
        hand.put(21, new Card("Siodemki", "Pik", 5, 0));

        //when
        checker.getIdOfCardsOnTable(table);
        checker.getIdOfCardsFromHand(hand);
        checker.combineAndSort();
        //czy posortowane rosnaco
        checker.showCards();

        //then
        assertEquals(FULL_HOUSE, checker.checkAll());
    }
    @Test
    public void checkAll_checkForFullHouse_False()
    {
        //given
        Map<Integer, Card> table = new LinkedHashMap<>();
        Map<Integer, Card> hand = new LinkedHashMap<>();
        //karty na stole
        table.put(25, new Card("Osemki", "Pik", 6, 0));
        table.put(29, new Card("Osemki", "Kier", 6, 1));
        table.put(33, new Card("Osemki", "Trefl", 6, 2));
        table.put(37, new Card("Jupki", "Pik", 9, 0));
        table.put(41, new Card("Jupki", "Kier", 9, 1));
        //karty w rece
        hand.put(45, new Card("Osemki", "Karo", 6, 3));
        hand.put(21, new Card("Jupki", "Trefl", 9, 2));

        //when
        checker.getIdOfCardsOnTable(table);
        checker.getIdOfCardsFromHand(hand);
        checker.combineAndSort();
        //czy posortowane rosnaco
        checker.showCards();

        //then
        assertEquals(FOUR_OF_A_KIND, checker.checkAll());
    }

    @Test
    public void checkAll_checkForFlush_True()
    {
        //given
        Map<Integer, Card> table = new LinkedHashMap<>();
        Map<Integer, Card> hand = new LinkedHashMap<>();
        //karty na stole
        table.put(25, new Card("Osemki", "Pik", 6, 0));
        table.put(1, new Card("Dwojki", "Pik", 0, 0));
        table.put(33, new Card("Osemki", "Trefl", 6, 2));
        table.put(37, new Card("Jupki", "Pik", 9, 0));
        table.put(5, new Card("Trojki", "Pik", 1, 0));
        //karty w rece
        hand.put(45, new Card("Osemki", "Karo", 6, 3));
        hand.put(17, new Card("Szostki", "Pik", 4, 0));

        //when
        checker.getIdOfCardsOnTable(table);
        checker.getIdOfCardsFromHand(hand);
        checker.combineAndSort();
        //czy posortowane rosnaco
        checker.showCards();

        //then
        assertEquals(FLUSH, checker.checkAll());
    }

    @Test
    public void checkAll_checkForStraight_True()
    {
        //given
        Map<Integer, Card> table = new LinkedHashMap<>();
        Map<Integer, Card> hand = new LinkedHashMap<>();
        //karty na stole
        table.put(25, new Card("Osemki", "Pik", 6, 0));
        table.put(1, new Card("Dwojki", "Pik", 0, 0));
        table.put(33, new Card("Osemki", "Trefl", 6, 2));
        table.put(13, new Card("Piatki", "Trefl", 3, 2));
        table.put(5, new Card("Trojki", "Pik", 1, 0));
        //karty w rece
        hand.put(12, new Card("Czworki", "Karo", 2, 3));
        hand.put(17, new Card("Szostki", "Pik", 4, 0));

        //when
        checker.getIdOfCardsOnTable(table);
        checker.getIdOfCardsFromHand(hand);
        checker.combineAndSort();
        //czy posortowane rosnaco
        checker.showCards();

        //then
        assertEquals(STRAIGHT, checker.checkAll());
    }

    @Test
    public void checkAll_checkForThreeOfAKind_True()
    {
        //given
        Map<Integer, Card> table = new LinkedHashMap<>();
        Map<Integer, Card> hand = new LinkedHashMap<>();
        //karty na stole
        table.put(25, new Card("Osemki", "Pik", 6, 0));
        table.put(1, new Card("Dwojki", "Pik", 0, 0));
        table.put(33, new Card("Osemki", "Trefl", 6, 2));
        table.put(13, new Card("Piatki", "Trefl", 3, 2));
        table.put(5, new Card("Trojki", "Pik", 1, 0));
        //karty w rece
        hand.put(28, new Card("Osemki", "Karo", 6, 3));
        hand.put(17, new Card("Szostki", "Pik", 4, 0));

        //when
        checker.getIdOfCardsOnTable(table);
        checker.getIdOfCardsFromHand(hand);
        checker.combineAndSort();
        //czy posortowane rosnaco
        checker.showCards();

        //then
        assertEquals(THREE_OF_A_KIND, checker.checkAll());
    }

    @Test
    public void checkAll_checkForTwoPair_True()
    {
        //given
        Map<Integer, Card> table = new LinkedHashMap<>();
        Map<Integer, Card> hand = new LinkedHashMap<>();
        //karty na stole
        table.put(25, new Card("Osemki", "Pik", 6, 0));
        table.put(1, new Card("Dwojki", "Pik", 0, 0));
        table.put(33, new Card("Osemki", "Trefl", 6, 2));
        table.put(13, new Card("Piatki", "Trefl", 3, 2));
        table.put(5, new Card("Trojki", "Pik", 1, 0));
        //karty w rece
        hand.put(8, new Card("Trojki", "Karo", 1, 3));
        hand.put(17, new Card("Szostki", "Pik", 4, 0));

        //when
        checker.getIdOfCardsOnTable(table);
        checker.getIdOfCardsFromHand(hand);
        checker.combineAndSort();
        //czy posortowane rosnaco
        checker.showCards();

        //then
        assertEquals(TWO_PAIR, checker.checkAll());
    }

    @Test
    public void checkAll_checkForPair_True()
    {
        //given
        Map<Integer, Card> table = new LinkedHashMap<>();
        Map<Integer, Card> hand = new LinkedHashMap<>();
        //karty na stole
        table.put(25, new Card("Osemki", "Pik", 6, 0));
        table.put(1, new Card("Dwojki", "Pik", 0, 0));
        table.put(33, new Card("Osemki", "Trefl", 6, 2));
        table.put(13, new Card("Piatki", "Trefl", 3, 2));
        table.put(5, new Card("Trojki", "Pik", 1, 0));
        //karty w rece
        hand.put(52, new Card("Asy", "Karo", 12, 3));
        hand.put(17, new Card("Szostki", "Pik", 4, 0));

        //when
        checker.getIdOfCardsOnTable(table);
        checker.getIdOfCardsFromHand(hand);
        checker.combineAndSort();
        //czy posortowane rosnaco
        checker.showCards();

        //then
        assertEquals(PAIR, checker.checkAll());
    }

    @Test
    public void checkAll_highestCard_True()
    {
        //given
        Map<Integer, Card> table = new LinkedHashMap<>();
        Map<Integer, Card> hand = new LinkedHashMap<>();
        //karty na stole
        table.put(25, new Card("Osemki", "Pik", 6, 0));
        table.put(1, new Card("Dwojki", "Pik", 0, 0));
        table.put(39, new Card("Jupki", "Trefl", 9, 2));
        table.put(13, new Card("Piatki", "Trefl", 3, 2));
        table.put(5, new Card("Trojki", "Pik", 1, 0));
        //karty w rece
        hand.put(52, new Card("Asy", "Karo", 12, 3));
        hand.put(17, new Card("Szostki", "Pik", 4, 0));

        //when
        checker.getIdOfCardsOnTable(table);
        checker.getIdOfCardsFromHand(hand);
        checker.combineAndSort();
        //czy posortowane rosnaco
        checker.showCards();

        //then
        assertEquals(0, checker.checkAll());
    }
}
