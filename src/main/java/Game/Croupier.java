package Game;

import net.GameClient;
import net.GameServer;

import javax.swing.*;
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

public class Croupier{
    public static volatile String playerActionMessage="fold";
    public static volatile boolean isRunning=false;
    public final Object waitFor2Players = new Object();
    public final Object waitForMessage = new Object();
    private GameClient socketClient;
    private GameServer socketServer;
    private static Croupier instance;
    public volatile int numberOfPlayers;
    public static String[] figures = {"Dwojki", "Trojki", "Czworki", "Piatki", "Szostki", "Siodemki", "Osemki", "Dziewiatki", "Dziesiatki"
            , "Jupki", "Damy", "Krole", "Asy"};
    public static String[] colors = {"Pik", "Kier", "Trefl", "Karo"};
    Map<Integer, Card> deck = new TreeMap<Integer, Card>();
    public Map<Integer, Card> table = new LinkedHashMap<>();
    public volatile int bigBlind=50;
    public volatile int smallBlind=25;
    public volatile int bigBlindPosition=0;
    public volatile int activePlayer=0;
    public volatile int maxBet=bigBlind;
    public volatile int firstPlayerInCycle;
    public volatile int currentPlayingPlayers=numberOfPlayers;
    public volatile int pot=0;
    private volatile Hand[] playersHand;
    private volatile Hand[] waitingPlayers;
    CheckHand checker = new CheckHand();
    Scanner sc = new Scanner(System.in);
    /*public Croupier(int x)
    {
        numberOfPlayers = x;
        makeDeck();
        //showDeck();//wyj
        makeHands();
        //game();
    }*/
    private Croupier()
    {
    }
    public static Croupier getInstance()
    {
        if(instance == null)
        {
            instance = new Croupier();
        }
        return instance;
    }
    //###########################################################################################################
    //METODY DO KOMUNIKACJI Z SERWEREM
    //###########################################################################################################

    public void waitForAtLeast2Players() throws InterruptedException {
        while(playersHand == null )
        {
            synchronized (waitFor2Players) {
                waitFor2Players.wait();
            }
            addWaitingPlayersToGame();
        }
        isRunning=true;
    }
    public void addWaitingPlayersToGame()
    {
        int numberOfWaitingPlayers=waitingPlayers.length;
        if(numberOfWaitingPlayers>0)
        {
            if(playersHand == null)
            {
                playersHand = new Hand[numberOfWaitingPlayers];
                playersHand = waitingPlayers;
                waitingPlayers = null;
            }
            else if(playersHand.length<5) {
                int numberOfCurrentPlayers=playersHand.length;
                Hand[] temporary;
                int temporaryLength=5-numberOfCurrentPlayers;
                if(numberOfCurrentPlayers+numberOfWaitingPlayers<5) {
                    temporary = new Hand[numberOfCurrentPlayers + numberOfWaitingPlayers];
                }
                else {
                    temporary = new Hand[5];
                }
                System.arraycopy(playersHand, 0, temporary, 0, numberOfCurrentPlayers);
                System.arraycopy(waitingPlayers, 0, temporary, numberOfCurrentPlayers, temporaryLength);
                System.arraycopy(waitingPlayers, temporaryLength, waitingPlayers, 0, numberOfWaitingPlayers-temporaryLength);
            }
        }
    }
    public void addPlayerToQueue(int Id, String nick, int amountOfMoney)
    {
        if(waitingPlayers == null)
        {
            waitingPlayers=new Hand[1];
            waitingPlayers[0]=new Hand(Id);
            waitingPlayers[0].playerName=nick;
            waitingPlayers[0].amountOfMoney=amountOfMoney;
        }
        else if(waitingPlayers.length<5){
            int numberOfWaitingPlayers=waitingPlayers.length;
            Hand[] temporary = new Hand[numberOfWaitingPlayers+1];
            System.arraycopy(waitingPlayers, 0, temporary, 0, numberOfWaitingPlayers);
            temporary[numberOfWaitingPlayers]=new Hand(Id);
            temporary[numberOfWaitingPlayers].amountOfMoney=amountOfMoney;
            temporary[numberOfWaitingPlayers].playerName=nick;
            waitingPlayers=temporary;
            if(waitingPlayers.length==2)
            {
                synchronized (waitFor2Players)
                {
                    waitFor2Players.notifyAll();
                }
            }
        }
        else {
            System.out.println("Za duzo graczy w kolejce");
        }
    }

    //###########################################################################################################
    //KONIEC METOD DO KOMUNIKACJI Z SERWEREM
    //###########################################################################################################


    //###########################################################################################################
    //METODY GŁÓWNE KRUPIERA
    //###########################################################################################################
    public void firstStepInCroupier(int x){
        numberOfPlayers=x;
        makeDeck();
        makeHands();
    }
    public void initializeCroupier() throws InterruptedException {

        Thread waitingForPlayers = new Thread(() -> {
           try {
               waitForAtLeast2Players();
           } catch (InterruptedException e) {
               throw new RuntimeException(e);
           }
        });
        waitingForPlayers.start();
        waitingForPlayers.join();
        makeDeck();
        numberOfPlayers=playersHand.length;
        StringBuffer sb = new StringBuffer("initializeInformations-numberOfPlayers-"+numberOfPlayers);
        for (int i = 0; i < numberOfPlayers; i++) {
            sb.append("-playerId-"+playersHand[i].playerId+"-playerName-"+playersHand[i].playerName+"-amountOfMoney-"+
                    playersHand[i].amountOfMoney+"-");
        }
        GameServer.getInstance().prepareAndSendDataFromCroupierToAllPlayers(sb.toString());
    }
    public void game()
    {
        do {
            int whichState = 0;
            isPlayerPlayable();
            cleanTable();
            dealCards();
            showHands();
            preFlop();
            do {
                dealCommunityCardsAndInitiateBetting(); //flop, turn, river
                //showCardsOnTable();
                whichState++;
            } while (currentPlayingPlayers > 1 && whichState < 3);
            if(currentPlayingPlayers>1) {
                checkForAllHands();
                extractTheWinner();
            }
            prepareForNextRound();
        }while(numberOfPlayers>1);
    }

    private void checkIfFirstPlayerInCycleHasChanged()
    {
        if(!playersHand[firstPlayerInCycle].isInCurrentRound) {
            do {
                firstPlayerInCycle=firstPlayerInCycle+1%numberOfPlayers;
            } while (!playersHand[firstPlayerInCycle].isInCurrentRound);
        }
    }
    private void dealCommunityCardsAndInitiateBetting()
    {
        checkCurrentPlayingPlayers();
        if(currentPlayingPlayers>1) {
            dealCardsToTable();
            showCardsOnTable();
            checkIfFirstPlayerInCycleHasChanged();
            activePlayer=firstPlayerInCycle;
            do {
                int tempMaxBet=maxBet;
                int tempPot=pot;
                playerActionMultiplayer();
                if((tempPot+maxBet)>(pot+tempMaxBet)) {
                    firstPlayerInCycle = activePlayer;
                }
                activePlayer=(activePlayer+1)%numberOfPlayers;
            } while (activePlayer != firstPlayerInCycle);
        }
        else {
            distributePot(numberOfPlayers-1);
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
        playersHand[idOfPlayer].actualBet=0;
        //playersHand[idOfPlayer].setIsInCurrentRound(false);
        pot=0;
    }
    private void getBlinds()
    {
        if(playersHand[bigBlindPosition].amountOfMoney<bigBlind)
        {
            pot+=playersHand[bigBlindPosition].amountOfMoney;
            playersHand[bigBlindPosition].actualBet=playersHand[bigBlindPosition].amountOfMoney;
            playersHand[bigBlindPosition].amountOfMoney=0;
            playersHand[bigBlindPosition].setIsInCurrentRound(true);
        }
        else
        {
            pot+=bigBlind;
            playersHand[bigBlindPosition].actualBet=bigBlind;
            playersHand[bigBlindPosition].amountOfMoney-=bigBlind;
            playersHand[bigBlindPosition].setIsInCurrentRound(true);
        }
        int smallBlindPosition=(bigBlindPosition-1+numberOfPlayers)%numberOfPlayers;
        if(playersHand[smallBlindPosition].amountOfMoney<smallBlind)
        {
            pot+=playersHand[smallBlindPosition].amountOfMoney;
            playersHand[smallBlindPosition].actualBet=playersHand[smallBlindPosition].amountOfMoney;
            playersHand[smallBlindPosition].amountOfMoney=0;
            playersHand[smallBlindPosition].setIsInCurrentRound(true);
        }
        else
        {
            pot+=smallBlind;
            playersHand[smallBlindPosition].actualBet=smallBlind;
            playersHand[smallBlindPosition].amountOfMoney-=smallBlind;
            playersHand[smallBlindPosition].setIsInCurrentRound(true);
        }
        bigBlindPosition=(bigBlindPosition+1)%numberOfPlayers;
        activePlayer=bigBlindPosition;
    }
    private void preFlop()
    {
        firstPlayerInCycle=bigBlindPosition;
        int firstPlayerInCycleNamedPreFlop=firstPlayerInCycle;
        getBlinds();
        do
        {
            int tempMaxBet=maxBet;
            int tempPot=pot;
            playerActionMultiplayer();
            if((pot)>(tempPot+tempMaxBet))
            {
                firstPlayerInCycleNamedPreFlop=activePlayer;
            }
            activePlayer=(activePlayer+1)%numberOfPlayers;
        }while(activePlayer!=firstPlayerInCycleNamedPreFlop);
    }
    private void playerActionMultiplayer()
    {
        if(playersHand[activePlayer].isInCurrentRound) {
            if(playersHand[activePlayer].isAllIn)
            {
                //Gracz wszedl all in, nic nie może zrobic.
            }
            else
            {
                int x=0;
                boolean goodChoice = false;
                int diff = maxBet - playersHand[activePlayer].actualBet;
                playerActionMessage="fold";
                GameServer.getInstance().prepareAndSendDataFromCroupierToOnePlayer("player-"+activePlayer+"-yourTurn-");
                do {
                    System.out.println("Fold - 1, Bet - 2, Raise - 3, All in - 4, Check - 5\n");
                    System.out.println("Gracz o id " + playersHand[activePlayer].playerId);
                    System.out.println("Masz do wplacenia zaklad o wysokosci: " + diff + "\nTwoje pozostale pieniadze: " + playersHand[activePlayer].amountOfMoney);

                    synchronized (waitForMessage)
                    {

                        try {
                            waitForMessage.wait(30000);
                        }catch(InterruptedException e)
                        {
                            e.printStackTrace();
                        }
                    }

                    if(playersHand[activePlayer].actualBet==maxBet)
                    {
                        playerActionMessage="check";
                    }

                    if (playersHand[activePlayer].amountOfMoney < diff) {
                        switch (playerActionMessage) {
                            case "fold":
                                fold();
                                goodChoice=true;
                                break;
                            case "allIn":
                                allIn(diff);
                                goodChoice=true;
                                break;
                            default:
                                System.out.println("Dokonaj poprawnego wyboru!");
                                break;
                        }
                    } else {
                        switch (playerActionMessage) {
                            case "fold":
                                fold();
                                goodChoice=true;
                                break;
                            case "bet":
                                bet(diff);
                                goodChoice=true;
                                break;
                            case "raise":
                                raise(diff);
                                goodChoice=true;
                                break;
                            case "allIn":
                                allIn(diff);
                                goodChoice=true;
                                break;
                            case "check":
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
        }
    }
    private void playerAction()
    {
        if(playersHand[activePlayer].isInCurrentRound) {
            //socketClient.sendData("ping".getBytes());
            if(playersHand[activePlayer].isAllIn)
            {
                //Gracz wszedl all in, nic nie może zrobic.
            }
            else
            {
                int x=0;
                boolean goodChoice = false;
                int diff = maxBet - playersHand[activePlayer].actualBet;
                boolean validInt=false;
                GameServer.getInstance().prepareAndSendDataFromCroupierToOnePlayer("player-"+activePlayer+"-yourTurn-");
                do {
                    System.out.println("Fold - 1, Bet - 2, Raise - 3, All in - 4, Check - 5\n");
                    while(!validInt) {
                        System.out.println("Gracz o id " + playersHand[activePlayer].playerId);
                        System.out.println("Masz do wplacenia zaklad o wysokosci: " + diff + "\nTwoje pozostale pieniadze: " + playersHand[activePlayer].amountOfMoney);
                        if(sc.hasNextInt()) {
                            x = sc.nextInt();
                            validInt=true;
                        }
                        else {
                            System.out.println("Wprowadz liczbe od 1 do 5...\n");
                            sc.next();
                        }
                    }
                    validInt=false;
                    if (playersHand[activePlayer].amountOfMoney < diff) {
                        switch (x) {
                            case 1:
                                fold();
                                goodChoice=true;
                                break;
                            case 4:
                                allIn(diff);
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
                                raise(diff);
                                goodChoice=true;
                                break;
                            case 4:
                                allIn(diff);
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
        if(playersHand[activePlayer].amountOfMoney<=0)
        {
            playersHand[activePlayer].isAllIn=true;
        }
    }
    private void fold()
    {
        playersHand[activePlayer].setIsInCurrentRound(false);
    }
    private void raise(int diff)
    {
        int raiseAmount;
        do {
            System.out.println("Podaj kwote, jaka chcesz przebic zaklad: \n");
            raiseAmount=sc.nextInt();
        }while(raiseAmount>playersHand[activePlayer].amountOfMoney || raiseAmount<diff);
        playersHand[activePlayer].amountOfMoney-=raiseAmount;
        pot+=raiseAmount;
        maxBet+=(raiseAmount-diff);
        playersHand[activePlayer].actualBet+=raiseAmount;
        if(playersHand[activePlayer].amountOfMoney<=0)
        {
            playersHand[activePlayer].isAllIn=true;
        }
    }
    private void allIn(int diff)
    {
        if(playersHand[activePlayer].amountOfMoney<maxBet)
        {
            pot+=playersHand[activePlayer].amountOfMoney;
            playersHand[activePlayer].actualBet+=playersHand[activePlayer].amountOfMoney;
            playersHand[activePlayer].amountOfMoney=0;
        }
        else
        {
            pot+=playersHand[activePlayer].amountOfMoney;
            playersHand[activePlayer].actualBet+=playersHand[activePlayer].amountOfMoney;
            maxBet=(maxBet-diff)+playersHand[activePlayer].amountOfMoney;
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
            if (playersHand[i].isInCurrentRound) {
                System.out.println("REKA GRACZA " + (i + 1));
                checker.getIdOfCardsFromHand(playersHand[i].hand);
                checker.combineAndSort();
                playersHand[i].rankOfHand = checker.checkAll();
            }
        }
        Arrays.sort(playersHand);
    }
    private int countTheWinners()
    {
        int winnersCount = 1;
        for (int i = numberOfPlayers - 1; i >= 1; i--) {
            if (playersHand[i].compareTo(playersHand[i - 1]) == 0 && playersHand[i].isInCurrentRound && playersHand[i - 1].isInCurrentRound) {
                winnersCount++;
            } else {
                break;
            }
        }
        return winnersCount;
    }
    private void showTheWinners(int winnersCount)
    {
        System.out.println("Wygralo: " + winnersCount + " graczy");
        for (int i = numberOfPlayers - 1; i >= numberOfPlayers - winnersCount; i--) {
            System.out.println("Wygrala reka " + playersHand[i].playerId + " z reka: " + playersHand[i].toString() + playersHand[i].rankOfHand);
        }
    }
    private void extractTheWinner()
    {
        if(pot>0)
        {
            if(currentPlayingPlayers==1){ //x
                showTheWinners(1);
                distributePot(numberOfPlayers-1);
            }
            else
            {
                int winnersCount = countTheWinners();
                showTheWinners(winnersCount);

                if (winnersCount <= 1) //jesli jest 1 wygrany
                {
                    if (playersHand[numberOfPlayers - 1].actualBet == maxBet) //x
                    {
                        distributePot(numberOfPlayers - 1);
                    } else if (currentPlayingPlayers > 2) //jesli wygrany nie wyrownal do najwyzszego zakladu //x
                    {
                        int tempAward = playersHand[numberOfPlayers - 1].actualBet; //x
                        for (int i = 0; i < numberOfPlayers - 1; i++) {
                            if (playersHand[numberOfPlayers - 1].actualBet >= playersHand[i].actualBet) {
                                tempAward += playersHand[i].actualBet;
                                playersHand[i].actualBet = 0;
                                playersHand[i].setIsInCurrentRound(false);
                            } else {
                                tempAward += playersHand[numberOfPlayers - 1].actualBet;
                                playersHand[i].actualBet -= playersHand[numberOfPlayers - 1].actualBet;
                            }
                        }
                        pot -= tempAward;
                        playersHand[numberOfPlayers - 1].amountOfMoney += tempAward;
                        playersHand[numberOfPlayers - 1].setIsInCurrentRound(false);
                        maxBet -= playersHand[numberOfPlayers - 1].actualBet;
                        playersHand[numberOfPlayers - 1].actualBet=0;
                        Arrays.sort(playersHand);
                        checkCurrentPlayingPlayers();
                        extractTheWinner();
                    } else //jesli wygrany nie wyrownal do max zakladu, a graczy w partii jest dwoch. //x
                    {
                        int tempMoney=0;
                        for (int i = 0; i < numberOfPlayers-1; i++)
                        {
                            if (playersHand[i].actualBet > playersHand[numberOfPlayers-1].actualBet)
                            {
                                tempMoney += playersHand[numberOfPlayers-1].actualBet;
                                playersHand[i].actualBet -= playersHand[numberOfPlayers-1].actualBet;
                            }
                            else
                            {
                                tempMoney += playersHand[i].actualBet;
                                playersHand[i].actualBet = 0;
                                playersHand[i].setIsInCurrentRound(false);
                            }
                        }
                        tempMoney+=playersHand[numberOfPlayers-1].actualBet;
                        playersHand[numberOfPlayers-1].amountOfMoney+=tempMoney;
                        playersHand[numberOfPlayers-1].actualBet=0;
                        pot-=tempMoney;
                        playersHand[numberOfPlayers-2].amountOfMoney+=pot;
                        pot=0;
                    }
                }
                else//jesli remis //x
                {
                    boolean checkIfWinnersHaveMaxBet=true;
                    int smallerBet=maxBet;
                    int tempSplitAward=0;
                    for (int i = numberOfPlayers-1; i > numberOfPlayers-1-winnersCount; i--)
                    {
                        if(playersHand[i].actualBet<maxBet){
                            checkIfWinnersHaveMaxBet=false;
                            if(smallerBet>playersHand[i].actualBet) {
                                smallerBet = playersHand[i].actualBet;
                            }
                        }
                    }
                    if(checkIfWinnersHaveMaxBet) // wygrani weszli ta sama, maksymalna na stole stawka //x
                    {
                        tempSplitAward = pot / winnersCount;
                        for (int i = 0; i <= winnersCount - 1; i++) {
                            playersHand[numberOfPlayers - i - 1].amountOfMoney += tempSplitAward;
                        }
                        pot = 0;
                    }
                    else // niektorzy z wygranych nie maja maksymalnej stawki //x
                    {
                        for (int i = 0; i <= numberOfPlayers-1-winnersCount; i++) //zbieranie tymczasowej wygranej zlozonej z najmniejszego wkladu sposrod graczy, ktorzy wygrali
                        {
                            if (playersHand[i].actualBet > smallerBet)
                            {
                                tempSplitAward += smallerBet;
                                playersHand[i].actualBet -= smallerBet;
                            }
                            else
                            {
                                tempSplitAward += playersHand[i].actualBet;
                                playersHand[i].actualBet = 0;
                                playersHand[i].setIsInCurrentRound(false);
                            }
                        }
                        pot-=tempSplitAward;
                        tempSplitAward=tempSplitAward/winnersCount;
                        for (int j = numberOfPlayers-1; j > numberOfPlayers-1-winnersCount; j--) //rozdanie tymczasowej wygranej, oraz usuniecie graczy ktorzy mieli najmniejszy zaklad sposrod wygranych //x
                        {
                            playersHand[j].amountOfMoney+=tempSplitAward;
                            if(playersHand[j].actualBet==smallerBet) {
                                playersHand[j].amountOfMoney+=playersHand[j].actualBet;
                                playersHand[j].actualBet=0;
                                playersHand[j].setIsInCurrentRound(false);
                            }
                            else
                            {
                                playersHand[j].amountOfMoney+=smallerBet;
                                playersHand[j].actualBet-=smallerBet;
                            }
                        }
                        maxBet-=smallerBet;
                        Arrays.sort(playersHand);
                        checkCurrentPlayingPlayers();
                        extractTheWinner(); // wywolanie funkcji ponownie, z mniejsza iloscia graczy, o tych ktorzy juz zebrali swoja wygrana, az do momentu wyzerowania pot'a
                    }
                }
            }
        }
    }

    private void prepareForNextRound()
    {
        for (int i = 0; i <= numberOfPlayers-1; i++) {
            playersHand[i].setIsInCurrentRound(true);
            playersHand[i].actualBet=0;
            playersHand[i].isAllIn=false;
            playersHand[i].rankOfHand=0;
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
                        playersHand[j].setIsInCurrentRound(true);
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
    public class Hand implements Comparable<Hand>{
        public Map<Integer, Card> hand = new LinkedHashMap<Integer, Card>();
        int playerId;
        String playerName;
        int rankOfHand;
        int amountOfMoney=500; //pula pieniedzy gracza
        int actualBet=0;
        boolean isAllIn=false;
        boolean isInCurrentRound;
        public void setIsInCurrentRound(boolean value)
        {
            if(isInCurrentRound==value)
                return;
            Croupier krupier  = getInstance();
                isInCurrentRound = value;
                if (isInCurrentRound) {
                    if(krupier.currentPlayingPlayers<numberOfPlayers) {
                        krupier.currentPlayingPlayers++;
                    }
                    //System.out.println("Aktualna liczba graczy: " + krupier.currentPlayingPlayers);
                } else if (!isInCurrentRound) {
                    if(krupier.currentPlayingPlayers>0){
                    krupier.currentPlayingPlayers--;
                    }
                    //System.out.println("Aktualna liczba graczy: " + krupier.currentPlayingPlayers);
                }

        }
        Hand(int i)
        {
            playerId=i;
            rankOfHand=0;
            setIsInCurrentRound(true);
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
                //int idOfFigure=this.hand.get(key).idOfFigure;
                int keySecond = Collections.max(x.hand.keySet());
                //int idOfFigureSecond=x.hand.get(keySecond).idOfFigure;
                int resultOfCompare=this.hand.get(key).compareTo(x.hand.get(keySecond));
                if(resultOfCompare==0)
                    return 0;
                else if(resultOfCompare>0)
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

    //###########################################################################################################
    //KONIEC PODKLAS, ORAZ FUNKCJI NADPISANYCH
    //###########################################################################################################

    //###########################################################################################################
    //FUNKCJE ZWIAZANE Z TESTOWANIEM
    //###########################################################################################################

    public void showCardsOnTable_makePublic()
    {
        showCardsOnTable();
    }

    public void showHands_makePublic()
    {
        showHands();
    }

    public void dealCardsWithAssumptions_checkWhenWinningCardsOnTableAndMattersTheHighestCard()
    {
        cleanTable();
        int k=1;
        for (int i = 0; i < numberOfPlayers; i++) {
            for (int j = 0; j < 2; j++) {
                Card temporary = new Card(Croupier.figures[i], Croupier.colors[j], i, j);
                playersHand[i].hand.put(((i*4)+j+1), temporary);
            }
        }
        int figure=11;
        int color=0;
        for (int i = 45; i <= 49; i++) {
            Card temporary = new Card(Croupier.figures[figure], Croupier.colors[color], figure, color);
            table.put(i, temporary);
            if(i==48)
                figure++;
            color=(color+1)%3;
        }
        showHands_makePublic();
        showCardsOnTable_makePublic();

    }

    public void dealCardsWithAssumptions_checkWhenTwoPlayersHaveDraw()
    {
        cleanTable();
        int k=0;
        for (int i = 8; i < 7+numberOfPlayers; i++) {
            for (int j = 0; j < 2; j++) {
                Card temporary = new Card(Croupier.figures[i], Croupier.colors[j], i, j);
                playersHand[k].hand.put(((i * 4) + j + 1), temporary);
            }
                if(i==11)
                {
                    for (int l = 2; l < 4; l++) {
                        Card temporary = new Card(Croupier.figures[i], Croupier.colors[l], i, l);
                        playersHand[k+1].hand.put(((i*4)+l+1), temporary);
                    }
                    break;
                }

            k++;
        }
        int figure=0;
        int color=0;
        for (int i = 1; i <= 5; i++) {
            Card temporary = new Card(Croupier.figures[figure], Croupier.colors[color], figure, color);
            table.put(i, temporary);
            figure++;
            if(figure%2==0)
                figure++;
            color=(color+1)%3;
        }
        showHands_makePublic();
        showCardsOnTable_makePublic();

    }

    public void checkForAllHands_makePublic()
    {
        checkForAllHands();
    }
    public int checkAmountOfMoneyForPlayerWithId(int id)
    {
        for (int i = 0; i < numberOfPlayers; i++)
        {
            if(playersHand[i].playerId==id)
                return playersHand[i].amountOfMoney;
        }
        return -1;
    }
    public int extractTheWinner_makePublic()
    {
        extractTheWinner();
        for (int i = 0; i < numberOfPlayers; i++) {
            System.out.println("Gracz nr "+playersHand[i].playerId+", posiada: "+playersHand[i].amountOfMoney);
        }
        return checkAmountOfMoneyForPlayerWithId(5);
    }
    public void addMoneyToThePot_ForLastPlayerWithoutMaxBet()
    {
        pot=450;
        maxBet=100;
        for (int i = 0; i < numberOfPlayers-1; i++) {
            playersHand[i].actualBet=100;
            playersHand[i].amountOfMoney-=100;
        }
        playersHand[numberOfPlayers-1].actualBet=50;
        playersHand[numberOfPlayers-1].amountOfMoney=0;
        playersHand[numberOfPlayers-1].isAllIn=true;

        for (int i = 0; i < numberOfPlayers; i++) {
            System.out.println("Gracz nr "+ playersHand[i].playerId + ", wplacil: "+playersHand[i].actualBet);
        }
    }
    public void addMoneyToThePot_changeFirstPlayerToAllIn()
    {
        playersHand[0].amountOfMoney=0;
        playersHand[0].actualBet=100;
        playersHand[0].isAllIn=true;
    }
    public void addMoneyToThePot_changeLastPlayerToAllInAndRestPlayersFold()
    {

        playersHand[numberOfPlayers-1].amountOfMoney=0;
        playersHand[numberOfPlayers-1].actualBet=500;
        playersHand[numberOfPlayers-1].isAllIn=true;
        maxBet=500;
        pot+=400;
        System.out.println("Na stole jest: "+pot);
        for (int i = 0; i < numberOfPlayers-1; i++) {
            System.out.println("Gracz o id: "+playersHand[i].playerId+" zrzucil karty...");
            playersHand[i].setIsInCurrentRound(false);
        }
    }
    public void addMoneyToThePot_changeLastPlayerToSmallerBetAndFoldEveryoneElseBesidesOfPlayerFour()
    {

        playersHand[numberOfPlayers-1].amountOfMoney=0;
        playersHand[numberOfPlayers-1].isAllIn=true;
        System.out.println("Gracz: "+playersHand[numberOfPlayers-1].playerId+" wszedl allIn");
        maxBet=150;
        pot=550;
        System.out.println("Na stole jest: "+pot);
        for (int i = 0; i < numberOfPlayers-2; i++) {
            System.out.println("Gracz o id: "+playersHand[i].playerId+" zrzucil karty...");
            playersHand[i].setIsInCurrentRound(false);
        }
        System.out.println("Gracz: "+playersHand[numberOfPlayers-2].playerId+" podwyzszyl do "+maxBet);
        playersHand[numberOfPlayers-2].actualBet=150;
        playersHand[numberOfPlayers-2].amountOfMoney-=50;
    }

    public void addMoneyToThePot_ForEverybodyWithMaxBet()
    {
        pot=500;
        maxBet=100;
        for (int i = 0; i < numberOfPlayers; i++) {
            playersHand[i].actualBet=100;
            playersHand[i].amountOfMoney-=100;
        }
        for (int i = 0; i < numberOfPlayers; i++) {
            System.out.println("Gracz nr "+ i + ", wplacil: "+playersHand[i].actualBet);
        }
    }

    public void isPlayerPlayable_makePublic(){
        isPlayerPlayable();
    }
    //###########################################################################################################
    //KONIEC FUNKCJI ZWIAZANYCH Z TESTOWANIEM
    //###########################################################################################################
}