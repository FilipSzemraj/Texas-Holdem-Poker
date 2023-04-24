package Game;
import Game.Croupier.*;

import java.util.*;

public class CheckHand {

    Card[] cardsOnTable;
    Card[] cardsInHand;
    Card[] allCards;

    public CheckHand()
    {
        cardsOnTable=new Card[5];
        cardsInHand=new Card[2];
        allCards=new Card[7];
    }

    public void getIdOfCardsOnTable(Map<Integer, Card> table)
    {
        /*int i=0;
        for(int key : table.keySet())
        {
            this.table[i][0]=(key+4-1)/4; // 1-dwojki, ..., 13-asy
            this.table[i][1]=key%4; //0-Karo, 1-Pik, 2-Kier, 3-Trefl
            i++;
        }*/
        int i=0;
        for(int key : table.keySet())
        {
            cardsOnTable[i]=table.get(key);
            i++;
        }

    }
    public void getIdOfCardsFromHand(Map<Integer, Card> hand)
    {
        /*int i=0;
        for(int key : hand.keySet())
        {
            this.hand[i][0]=(key+4-1)/4; // 1-dwojki, ..., 13-asy
            this.hand[i][1]=key%4; //0-Karo, 1-Pik, 2-Kier, 3-Trefl
            i++;
        }*/
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
        checkAll();
        //for(Card obj : allCards)
        //{
        //   System.out.println(obj);
        //}

    }
    private void checkAll()
    {
        if(checkForRoyalFlush() || checkForStraightFlush() || checkForFourOfAKind()
                || checkForFullHouse() || checkForFlush() || checkForStraightFlush() || checkForThreeOfAKind()
                || checkForTwoPair() || checkForPair())
        {
            System.out.println("\n");
        }
        else {
            checkForHighestCard();
        }
    }


    private boolean checkForRoyalFlush()
    {
        if(checkForStraightFlush() && (allCards[4].idOfFigure == 12
                || allCards[5].idOfFigure == 12 ||
                allCards[6].idOfFigure == 12))
        {
            return true;
        }
        return false;
    }
    private boolean checkForStraightFlush()
    {
        if(checkForStraight()&&checkForFlush()) {
            System.out.println("Straight flush!\n");
            return true;
        }
        return false;
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
            System.out.println("Czworka - " + allCards[position - 3].toString() +" "+ allCards[position - 2].toString()
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
    private boolean checkForFlush()
    {
        int[] idOfColorCount= {0,0,0,0};
        for(Card card: allCards)
        {
            int temp=card.idOfColor;
            idOfColorCount[temp]++;
        }
        for(int i=0;i<4;i++)
        {
            if(idOfColorCount[i]==5)
            {
                System.out.println("Kolor - "+Croupier.colors[i]);
                return true;
            }
        }
        return false;
    }
    private boolean checkForStraight()
    {
        int n = allCards.length;
        int check=0;

        for (int i = 0; i < n; i++) { // iterujemy po wszystkich elementach tablicy
            boolean isStraight = true;
            for (int j = 1; j <= 4; j++) { // sprawdzamy 4 kolejne elementy
                int currIndex = (i+j) % n; // indeks kolejnego elementu, uwzględniając cykliczność tablicy
                int prevIndex = (i+j-1) % n; // indeks poprzedniego elementu, uwzględniając cykliczność tablicy
                if (allCards[currIndex].idOfFigure != allCards[prevIndex].idOfFigure + 1) { // sprawdzamy, czy wartość pola idOfFigure jest o 1 większa od poprzednika
                    isStraight = false;
                    break; // przerywamy pętlę, jeśli sekwencja nie jest już poprawna
                }
            }
            if (isStraight) {
                // znaleziono sekwencję pięciu kolejnych elementów z wartościami pola idOfFigure zwiększonymi o 1
                System.out.println("Znaleziono sekwencję pięciu kolejnych kart: " + allCards[i] + ", " + allCards[(i+1) % n] + ", " + allCards[(i+2) % n]
                        + ", " + allCards[(i+3) % n] + ", " + allCards[(i+4) % n]);
                check=1;
                break; // przerywamy pętlę, jeśli znaleziono sekwencję
            }
        }
        if(check==1)
            return true;
        return false;

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
    private void checkForHighestCard()
    {
        System.out.println(allCards[6].toString());
    }

}
