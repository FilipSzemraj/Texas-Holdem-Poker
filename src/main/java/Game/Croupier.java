package Game;

import java.util.*;
import java.util.stream.Collectors;

class GenerateRandom{
    static final int min=1;
    static final int max=52;
    public static int getRandomInt()
    {
        int random_int = (int)Math.floor(Math.random()*(max-min+1)+min);
        return random_int;
    }
}
class Card implements Comparable<Card>{
    private final String nameOfFigure;
    final String colorName;
    int idOfFigure;
    int idOfColor;
    Card(String x, String y, int k, int l)
    {
        nameOfFigure=x;
        colorName=y;
        idOfFigure=k;
        idOfColor=l;
    }
    @Override
    public String toString()
    {
        return nameOfFigure+" "+colorName;
    }
    @Override
    public int compareTo(Card x)
    {
        if (this.idOfFigure == (x.idOfFigure))
            return 0;
        else if ((this.idOfFigure) > (x.idOfFigure))
            return 1;
        else
            return -1;
    }
}

public class Croupier{
    int numberOfPlayers;
    static String[] figures = {"Dwojki", "Trojki", "Czworki", "Piatki", "Szostki", "Siodemki", "Osemki", "Dziewiatki", "Dziesiatki"
            , "Jupki", "Damy", "Krole", "Asy"};
    static String[] colors = {"Pik", "Kier", "Trefl", "Karo"};

    Map<Integer, Card> deck = new TreeMap<Integer, Card>();
    Map<Integer, Card> table = new LinkedHashMap<>();

    Hand[] playersHand;
    CheckHand checker = new CheckHand();

    public Croupier(int x)
    {
        numberOfPlayers = x;
        makeDeck();
        makeHands();
        dealCards();
        showDeck();
        showHands();
        System.out.println("KARTY NA STOLE");
        dealCardsToTable();
        showCardsOnTable();
        dealCardsToTable();
        showCardsOnTable();
        dealCardsToTable();
        showCardsOnTable();
        System.out.println("SPRAWDZENIE DLA KAZDEJ REKI");
        routineForAllHands();
    }

    private void routineForAllHands()
    {
        checker.getIdOfCardsOnTable(table);
        for (int i = 0; i < numberOfPlayers; i++) {
            System.out.println("REKA GRACZA "+(i+1));
            checker.getIdOfCardsFromHand(playersHand[i].hand);
            checker.combineAndSort();
        }
    }
    private void checkHands()
    {
        checker.getIdOfCardsOnTable(table);
        checker.getIdOfCardsFromHand(playersHand[0].hand);
        checker.combineAndSort();
    }
    private void makeDeck()
    {
        int i, j, k=1;

        for(i=0;i<=12;i++)
        {
            for(j=0;j<=3;j++)
            {
                deck.put(k, new Card(figures[i], colors[j], i, j));
                k++;
            }
        }
    }
    private void showDeck()
    {
        Set<Map.Entry<Integer, Card>> entrySet = deck.entrySet();
        for(Map.Entry<Integer, Card> entry: entrySet)
        {
            System.out.println(entry.getKey()+" : "+entry.getValue().toString());
        }
        System.out.println(deck.size());
    }
    class Hand {
        Map<Integer, Card> hand = new LinkedHashMap<Integer, Card>();
    }
    private void dealCards()
    {
        for (int i = 0; i < 2; i++)
        {
            for (int j = 0; j < numberOfPlayers; j++) {
                boolean check=true;
                while(check)
                {
                    int random = GenerateRandom.getRandomInt();
                    if(deck.containsKey(random))
                    {
                        Card temporary = deck.get(random);
                        playersHand[j].hand.put(random, temporary);
                        deck.remove(random);
                        check = false;
                    }
                }
            }
        }
    }

    private void showHands()
    {
        for (int j = 0; j < numberOfPlayers; j++) {
            System.out.println(playersHand[j].hand.toString());
        }

    }
    private int[] returnIdOfCardsInHand(int i)
    {
        int[] id=new int[2];
        int x=0;
        for(int key: playersHand[i].hand.keySet())
        {
            id[x]=key;
            x++;
        }
        return id;
    }

    private void makeHands()
    {
        playersHand = new Hand[numberOfPlayers];
        for (int i = 0; i < numberOfPlayers; i++) {
            playersHand[i] = new Hand();
        }
    }

    private void cleanTable()
    {
        Set<Map.Entry<Integer, Card>> entrySet = table.entrySet();
        for(Map.Entry<Integer, Card> entry: entrySet)
        {
            int temporaryId=entry.getKey();
            Card temporaryCard=entry.getValue();
            deck.put(temporaryId, temporaryCard);
        }
        table.clear();
        for (int i = 0; i < numberOfPlayers; i++) {
            entrySet = playersHand[i].hand.entrySet();
            for(Map.Entry<Integer, Card> entry: entrySet)
            {
                int temporaryId=entry.getKey();
                Card temporaryCard=entry.getValue();
                deck.put(temporaryId, temporaryCard);
            }
            playersHand[i].hand.clear();
        }
    }

    private void dealCardsToTableInsideFunction(int i)
    {
        for (int j = 0; j < i; j++) {
            boolean check=true;
            while(check)
            {
                int random = GenerateRandom.getRandomInt();
                if(deck.containsKey(random))
                {
                    Card temporary = deck.get(random);
                    table.put(random, temporary);
                    deck.remove(random);
                    check = false;
                }
            }
        }
    }
    private void dealCardsToTable()
    {
        int cardsOnTable = table.size();
        switch(cardsOnTable)
        {
            case 0:
                dealCardsToTableInsideFunction(3);
                break;
            case 3:
                dealCardsToTableInsideFunction(1);
                break;
            case 4:
                dealCardsToTableInsideFunction(1);
                break;
            case 5:
                cleanTable();
                break;
            default:
                System.out.println("Na stole jest "+cardsOnTable+", dodatkowe wykładanie kart jest nie możliwe");
        }
    }

    private void showCardsOnTable()
    {
        String WhatIsOnTable = convertWithStream(table);
        System.out.println(WhatIsOnTable);
    }

    public String convertWithStream(Map<Integer, ?> map) {
        String mapAsString = map.keySet().stream()
                .map(key -> key + "=" + map.get(key))
                .collect(Collectors.joining(", ", "{", "}"));
        return mapAsString;
    }

}