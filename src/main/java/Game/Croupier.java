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
    public int bigBlind=50;
    public int smallBlind=25;
    public int bigBlindPosition=0;
    public int activePlayer=0;
    public int maxBet=bigBlind;
    public int firstPlayerInCycle;
    public int currentPlayingPlayers=numberOfPlayers;
    public int pot=0;
    Hand[] playersHand;
    CheckHand checker = new CheckHand();
    Scanner sc = new Scanner(System.in);
    public Croupier(int x)
    {
        numberOfPlayers = x;
        makeDeck();
        makeHands();
        game();
    }
    //###########################################################################################################
    //METODY GŁÓWNE KRUPIERA
    //###########################################################################################################
    public void game()
    {
        int whichState=0;
        isPlayerPlayable();
        cleanTable();
        dealCards();
        showHands();
        preFlop();
        do{
            dealCommunityCardsAndInitiateBetting(); //flop, turn, river
            //showCardsOnTable();
            whichState++;
        }while(currentPlayingPlayers>1 && whichState < 3);
        if(currentPlayingPlayers>1)
        {
            checkForAllHands();
            extractTheWinner();
        }
    }
    private void dealCommunityCardsAndInitiateBetting ()
    {
        int tempPosition=checkCurrentPlayingPlayers();
        if(currentPlayingPlayers>1) {
            dealCardsToTable();
            showCardsOnTable();
            //activePlayer=firstPlayerInCycle;
            do {
                int tempMaxBet=maxBet;
                int tempPot = pot;
                playerAction();
                if((tempPot+maxBet)>(pot+tempMaxBet)) {
                    firstPlayerInCycle = activePlayer;
                }
                activePlayer=(activePlayer+1)%numberOfPlayers;
            } while (activePlayer != (firstPlayerInCycle+1)%numberOfPlayers);
        }
        else {
            distributePot(tempPosition);
        }
    }
    private int checkCurrentPlayingPlayers()
    {
        currentPlayingPlayers=0;
        int tempPosition=0;
        for (int i = 0; i < numberOfPlayers; i++) {
            if(playersHand[i].isInCurrentRound) {
                currentPlayingPlayers++;
                tempPosition = i;
            }
        }
        return tempPosition;
    }
    private void distributePot(int idOfPlayer)
    {
        playersHand[idOfPlayer].amountOfMoney+=pot;
        pot=0;
    }
    private void getBlinds()
    {
        if(playersHand[bigBlindPosition].amountOfMoney<bigBlind)
        {
            pot+=playersHand[bigBlindPosition].amountOfMoney;
            playersHand[bigBlindPosition].actualBet=playersHand[bigBlindPosition].amountOfMoney;
            playersHand[bigBlindPosition].amountOfMoney=0;
            playersHand[bigBlindPosition].isInCurrentRound=true;
        }
        else
        {
            pot+=bigBlind;
            playersHand[bigBlindPosition].actualBet=bigBlind;
            playersHand[bigBlindPosition].amountOfMoney-=bigBlind;
            playersHand[bigBlindPosition].isInCurrentRound=true;
        }
        int smallBlindPosition=(bigBlindPosition-1+numberOfPlayers)%numberOfPlayers;
        if(playersHand[smallBlindPosition].amountOfMoney<smallBlind)
        {
            pot+=playersHand[smallBlindPosition].amountOfMoney;
            playersHand[smallBlindPosition].actualBet=playersHand[smallBlindPosition].amountOfMoney;
            playersHand[smallBlindPosition].amountOfMoney=0;
            playersHand[smallBlindPosition].isInCurrentRound=true;
        }
        else
        {
            pot+=smallBlind;
            playersHand[smallBlindPosition].actualBet=smallBlind;
            playersHand[smallBlindPosition].amountOfMoney-=smallBlind;
            playersHand[smallBlindPosition].isInCurrentRound=true;
        }
        bigBlindPosition=(bigBlindPosition+1)%numberOfPlayers;
        activePlayer=bigBlindPosition;
    }
    private void preFlop()
    {
        firstPlayerInCycle=bigBlindPosition;
        getBlinds();
        do
        {
            int tempMaxBet=maxBet;
            int tempPot=pot;
            playerAction();
            if((tempPot+maxBet)>(pot+tempMaxBet))
            {
                firstPlayerInCycle=activePlayer;
            }
            activePlayer=(activePlayer+1)%numberOfPlayers;
        }while(activePlayer!=(firstPlayerInCycle+1)%numberOfPlayers);
    }
    private void playerAction()
    {
        if(playersHand[activePlayer].isInCurrentRound) {
            if(playersHand[activePlayer].isAllIn)
            {
                //Gracz wszedl all in, nic nie może zrobic.
            }
            else
            {
                int x;
                boolean goodChoice = false;
                do {
                    System.out.println("Fold - 1, Bet - 2, Raise - 3, All in - 4, Check - 5\n");
                    int diff = maxBet - playersHand[activePlayer].actualBet;
                    System.out.println("Gracz o id " +playersHand[activePlayer].playerId);
                    System.out.println("Masz do wplacenia zaklad o wysokosci: " + diff + "\nTwoje pozostale pieniadze: " + playersHand[activePlayer].amountOfMoney);
                    x = sc.nextInt();
                    if (playersHand[activePlayer].amountOfMoney < diff) {
                        switch (x) {
                            case 1:
                                fold();
                                goodChoice=true;
                                break;
                            case 4:
                                allIn();
                                goodChoice=true;
                                break;
                            default:
                                System.out.println("Dokonaj poprawnego wyboru!");
                                break;
                        }
                    } else {
                        switch (x) {
                            case 1:
                                fold();
                                goodChoice=true;
                                break;
                            case 2:
                                bet(diff);
                                goodChoice=true;
                                break;
                            case 3:
                                raise();
                                goodChoice=true;
                                break;
                            case 4:
                                allIn();
                                goodChoice=true;
                                break;
                            case 5:
                                if(diff==0) {
                                    check();
                                    goodChoice = true;
                                }else {
                                    System.out.println("Nie możesz czekac, poniewaz nie wyrownales do najwyzszego zakladu.\n");
                                }
                                break;
                            default:
                                System.out.println("Dokonaj poprawnego wyboru!");
                                break;
                        }
                    }
                } while (goodChoice == false);
            }
            //activePlayer = (activePlayer + 1) % numberOfPlayers;
        }
    }
    private void check()
    {
        return;
    }
    private void bet(int diff)
    {
        playersHand[activePlayer].actualBet+=diff;
        playersHand[activePlayer].amountOfMoney-=diff;
        pot+=diff;
    }
    private void fold()
    {
        playersHand[activePlayer].isInCurrentRound=false;
    }
    private void raise()
    {
        int raiseAmount;
        do {
            System.out.println("Podaj kwote, jaka chcesz przebic zaklad: \n");
            raiseAmount=sc.nextInt();
        }while(raiseAmount>playersHand[activePlayer].amountOfMoney && raiseAmount>maxBet);
        playersHand[activePlayer].amountOfMoney-=raiseAmount;
        pot+=raiseAmount;
        maxBet=raiseAmount;
    }
    private void allIn()
    {
        if(playersHand[activePlayer].amountOfMoney<maxBet)
        {
            pot+=playersHand[activePlayer].amountOfMoney;
            playersHand[activePlayer].amountOfMoney=0;
        }
        else
        {
            pot+=playersHand[activePlayer].amountOfMoney;
            maxBet+=playersHand[activePlayer].amountOfMoney;
            playersHand[activePlayer].amountOfMoney=0;
        }
        playersHand[activePlayer].isAllIn=true;
    }
    private void isPlayerPlayable()
    {
        if(playersHand==null)
            return;
        int countNonPlayablePlayers=0;
        for (int i = 0; i < numberOfPlayers; i++) {
            if(playersHand[i].amountOfMoney<=0)
            {
                countNonPlayablePlayers++;
                playersHand[i].rankOfHand=-1;
            }
        }
        System.out.println("Odpadlo: "+countNonPlayablePlayers+", graczy");
        if(countNonPlayablePlayers>0) {
            Arrays.sort(playersHand);
            playersHand = Arrays.copyOfRange(playersHand, countNonPlayablePlayers-1, numberOfPlayers-1);
            numberOfPlayers=numberOfPlayers-countNonPlayablePlayers;
        }
    }
    private void checkForAllHands()
    {
        checker.getIdOfCardsOnTable(table);
        for (int i = 0; i < numberOfPlayers; i++) {
            if(playersHand[i].isInCurrentRound) {
                System.out.println("REKA GRACZA " + (i + 1));
                checker.getIdOfCardsFromHand(playersHand[i].hand);
                checker.combineAndSort();
                playersHand[i].rankOfHand = checker.checkAll();
            }
        }
        Arrays.sort(playersHand);
    }
    private void extractTheWinner() //moze inna nazwa
    {
        int winnersCount=1;
        for (int i = numberOfPlayers-1; i >= 1; i--) {
            if(playersHand[i].compareTo(playersHand[i-1])==0 && playersHand[i].isInCurrentRound && playersHand[i-1].isInCurrentRound)
            {
                winnersCount++;
            }
            else {
                break;
            }
        }
        System.out.println("Wygralo: "+winnersCount+" graczy");
        for (int i = numberOfPlayers-1; i >= numberOfPlayers-winnersCount; i--) {
            System.out.println("Wygrala reka "+playersHand[i].playerId+" z reka: "+playersHand[i].toString()+playersHand[i].rankOfHand);
        }
        if(winnersCount==1)
        {
            distributePot(numberOfPlayers-1);
        }
        else {
            int tempAward=pot/winnersCount;
            for (int i = 0; i < winnersCount-1; i++) {
                playersHand[numberOfPlayers-i-1].amountOfMoney+=tempAward;
            }
            pot=0;
        }
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
    //###########################################################################################################
    //KONIEC METOD GŁÓWNYCH KRUPIERA
    //###########################################################################################################

    //###########################################################################################################
    //METODY ZWIĄZANE Z RĘKOMA GRACZY
    //###########################################################################################################
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
                        playersHand[j].isInCurrentRound=true;
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
    private void makeHands()
    {
        playersHand = new Hand[numberOfPlayers];
        for (int i = 0; i < numberOfPlayers; i++) {
            playersHand[i] = new Hand(i+1);
        }
    }
    //###########################################################################################################
    //KONIEC METOD ZWIĄZANYCH Z RĘKOMA GRACZY
    //###########################################################################################################

    //###########################################################################################################
    //METODY ZWIĄZANE ZE STOŁEM
    //###########################################################################################################
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
    //###########################################################################################################
    //KONIEC METOD ZWIĄZANYCH ZE STOŁEM
    //###########################################################################################################

    //###########################################################################################################
    //PODKLASY, ORAZ FUNKCJE NADPISANE
    //###########################################################################################################
    class Hand implements Comparable<Hand>{
        Map<Integer, Card> hand = new LinkedHashMap<Integer, Card>();
        int playerId;
        int rankOfHand;
        int amountOfMoney=500; //pula pieniedzy gracza
        int actualBet=0;
        boolean isAllIn=false;
        boolean isInCurrentRound;
        Hand(int i)
        {
            playerId=i;
            rankOfHand=0;
        }

        @Override
        public String toString()
        {
            StringBuilder sb = new StringBuilder();
            sb.append("Reka gracza: ").append(playerId).append("\n");
            for(int key: hand.keySet())
            {
                sb.append(hand.get(key).toString()).append(" ");
            }
            return sb.toString();
        }
        @Override
        public int compareTo(Hand x)
        {
            if(this.rankOfHand==-1 || this.isInCurrentRound==false)
                return -1;
            if(x.rankOfHand==-1 || x.isInCurrentRound==false)
                return 1;
            if (this.rankOfHand == (x.rankOfHand)) {
                int key = Collections.max(this.hand.keySet());
                int idOfFigure=this.hand.get(key).idOfFigure;
                int keySecond = Collections.max(x.hand.keySet());
                int idOfFigureSecond=x.hand.get(keySecond).idOfFigure;
                if(idOfFigure == idOfFigureSecond)
                    return 0;
                else if(idOfFigure > idOfFigureSecond)
                    return 1;
                else
                    return -1;
            }
            else if ((this.rankOfHand) > (x.rankOfHand))
                return 1;
            else
                return -1;
        }
    }
    public String convertWithStream(Map<Integer, ?> map) {
        String mapAsString = map.keySet().stream()
                .map(key -> key + "=" + map.get(key))
                .collect(Collectors.joining(", ", "{", "}"));
        return mapAsString;
    }

}