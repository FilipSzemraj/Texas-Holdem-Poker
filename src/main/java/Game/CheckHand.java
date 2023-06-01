package Game;

import java.util.*;

public class CheckHand {
    Card[] cardsOnTable;
    Card[] cardsInHand;
    Card[] allCards;
    boolean isStraight = false;
    boolean isFlush = false;
    boolean isStraightFlush = false;
    public static final int ROYAL_FLUSH=9;
    public static final int STRAIGHT_FLUSH=8;
    public static final int FOUR_OF_A_KIND=7;
    public static final int FULL_HOUSE=6;
    public static final int FLUSH=5;
    public static final int STRAIGHT=4;
    public static final int THREE_OF_A_KIND=3;
    public static final int TWO_PAIR=2;
    public static final int PAIR=1;
    public static final String[] ranksOfHand = {"Najwyzsza karta", "Para", "Dwie pary", "Trzy karty jednego rodzaju", "Strit", "Kolor", "Full House", "Cztery karty jednego rodzaju", "Strit w kolorze", "Poker"};
    public void showCards() // dla testow
    {
        for (int i = 0; i < allCards.length; i++) {
            System.out.println(allCards[i].toString());
        }
        System.out.println("\n");
    }
    public CheckHand()
    {
        cardsOnTable=new Card[5];
        cardsInHand=new Card[2];
        allCards=new Card[7];
    }
    public void getIdOfCardsOnTable(Map<Integer, Card> table)
    {
        int i=0;
        for(int key : table.keySet())
        {
            cardsOnTable[i]=table.get(key);
            i++;
        }
    }
    public void getIdOfCardsFromHand(Map<Integer, Card> hand)
    {
        int i=0;
        for(int key : hand.keySet())
        {
            cardsInHand[i]=hand.get(key);
            i++;
        }
    }
    public void combineAndSort()
    {
        System.arraycopy(cardsOnTable, 0, allCards, 0, cardsOnTable.length);
        System.arraycopy(cardsInHand, 0, allCards, cardsOnTable.length, cardsInHand.length);

        Arrays.sort(allCards);
    }
    public int checkAll()
    {
        int result=0;
        if (checkForRoyalFlush()) {
            result = ROYAL_FLUSH;
        } else if (checkIsStraightFlush()) {
            result = STRAIGHT_FLUSH;
        } else if (checkForFourOfAKind()) {
            result = FOUR_OF_A_KIND;
        } else if (checkForFullHouse()) {
            result = FULL_HOUSE;
        } else if (checkIsFlush()) {
            result = FLUSH;
        } else if (checkIsStraight()) {
            result = STRAIGHT;
        } else if (checkForThreeOfAKind()) {
            result = THREE_OF_A_KIND;
        } else if (checkForTwoPair()) {
            result = TWO_PAIR;
        } else if (checkForPair()) {
            result = PAIR;
        }
        else {
            System.out.println("Najwyzsza karta\n");
        }
        return result;
    }
    private boolean checkForRoyalFlush()
    {
        if(checkForStraightFlush() && (allCards[4].idOfFigure == 12
                || allCards[5].idOfFigure == 12 ||
                allCards[6].idOfFigure == 12))
        {
            System.out.println("POKER!\n");
            return true;
        }
        return false;
    }
    private boolean checkForStraightFlush()
    {
        int n = allCards.length;
        int check=0;
        int[] idOfColorCount={0,0,0,0};
        isStraight=false;

        for (int i = n-1; i >= 0; i--) {// iterujemy po wszystkich elementach tablicy
            int temp = allCards[i].idOfColor;
            idOfColorCount[temp]++;
            boolean isStraightFlush = true;
            boolean isStraightInFunction = true;
            for (int j = 1; j <= 4; j++) { // sprawdzamy 4 kolejne elementy
                int currIndex = (i-j+n) % n; // indeks kolejnego elementu, uwzględniając cykliczność tablicy
                int prevIndex = (i-j-1+n) % n; // indeks poprzedniego elementu, uwzględniając cykliczność tablicy

                if(allCards[currIndex].idOfFigure - 1 != allCards[prevIndex].idOfFigure)
                {
                    isStraightInFunction=false;
                    isStraightFlush=false;
                    break;
                }

                if (allCards[i].idOfColor != allCards[prevIndex].idOfColor) {
                    isStraightFlush = false;
                }

            }
            if (isStraightInFunction) {
                isStraight=true;
            }
            if (isStraightFlush) {
                // znaleziono sekwencję pięciu kolejnych elementów z wartościami pola idOfFigure zwiększonymi o 1
                System.out.println("Straight flush!: " + allCards[i] + ", " + allCards[(i-1+n) % n] + ", " + allCards[(i-2+n) % n]
                        + ", " + allCards[(i-3+n) % n] + ", " + allCards[(i-4+n) % n]);
                check=1;
                break;
                // przerywamy pętlę, jeśli znaleziono sekwencję
            }

        }
        if(check==1) {
            isStraightFlush=true;
            isFlush=true;
            return true;
        }
        else {
            isFlush=false;
            for(int i=0;i<4;i++)
            {
                if(idOfColorCount[i]==5)
                {
                    isFlush=true;
                }
            }
            isStraightFlush=false;
            return false;
        }
    }
    private boolean checkForFourOfAKind()
    {
        int check = 0;
        int position = 0;
        for (int i = 3; i < 7; i++) {
            if(allCards[i-3].idOfFigure==allCards[i-2].idOfFigure && allCards[i-2].idOfFigure==allCards[i-1].idOfFigure
                    && allCards[i].idOfFigure == allCards[i-1].idOfFigure)
            {
                check++;
                position=i;
            }
        }
        if(check==1) {
            System.out.println("Four of kind! - " + allCards[position - 3].toString() +" "+ allCards[position - 2].toString()
                    +" "+ allCards[position-1].toString() +" "+ allCards[position]);
            return true;
        }
        return false;
    }
    private boolean checkForFullHouse()
    {
        int checkThreeOfKind = 0;
        int checkPair=0;
        int positionThreeOfKind = 0;
        int positionPair=0;
        for (int i = 2; i < 7; i++) {
            if(allCards[i-2].idOfFigure==allCards[i-1].idOfFigure && allCards[i-1].idOfFigure==allCards[i].idOfFigure)
            {
                checkThreeOfKind++;
                positionThreeOfKind=i;
            }
        }
        if(checkThreeOfKind==1) {
            for(int j=1;j<7;j++)
            {
                if(j<=(positionThreeOfKind-3) || j>positionThreeOfKind)
                {
                    if(allCards[j-1].idOfFigure==allCards[j].idOfFigure)
                    {
                        checkPair++;
                        positionPair=j;
                    }
                }
            }
            if(checkPair>0)
            {
                System.out.println("Full house - "+allCards[positionThreeOfKind-2].toString()+" "+allCards[positionThreeOfKind-1].toString()+" "
                        +allCards[positionThreeOfKind].toString()+" "+allCards[positionPair-1].toString()+" "+allCards[positionPair].toString());
                return true;
            }
        }
        return false;
    }
    private boolean checkIsStraightFlush()
    {
        return isStraightFlush;
    }
    private boolean checkIsFlush()
    {
        if(isFlush)
            System.out.println("Color\n");
        return isFlush;
    }
    private boolean checkIsStraight()
    {
        if(isStraight)
            System.out.println("Straight!\n");
        return isStraight;
    }
    private boolean checkForThreeOfAKind()
    {
        int check = 0;
        int position = 0;
        for (int i = 2; i < 7; i++) {
            if(allCards[i-2].idOfFigure==allCards[i-1].idOfFigure && allCards[i-1].idOfFigure==allCards[i].idOfFigure)
            {
                check++;
                position=i;
            }
        }
        if(check==1) {
            System.out.println("Trojka - " + allCards[position - 2].toString()+" " + allCards[position - 1].toString()+" "
                    + allCards[position].toString());
            return true;
        }
        return false;
    }
    private boolean checkForTwoPair()
    {
        int check = 0;
        int position = 0;
        int position2 = 0;
        int i=0;
        while(i<6)
        {
            if(allCards[i].idOfFigure==allCards[i+1].idOfFigure)
            {
                check++;
                if(check==1)
                    position=i;
                else if(check==2)
                    position2=i;
                i+=2;
            }
            else
            {
                i++;
            }
        }
        if(check==2) {
            System.out.println("Dwie pary - " + allCards[position].toString()+ " " + allCards[position + 1].toString()
                    +" "+ allCards[position2].toString()+" " + allCards[position2 + 1]);
            return true;
        }
        return false;
    }
    private boolean checkForPair()
    {
        int check = 0;
        int position = 0;
        for (int i = 1; i < 7; i++) {
            if(allCards[i-1].idOfFigure==allCards[i].idOfFigure)
            {
                check++;
                position=i;
            }
        }
        if(check==1) {
            System.out.println("Para - " + allCards[position - 1].toString()+" " +
                    allCards[position].toString());
            return true;
        }
        return false;
    }
}
