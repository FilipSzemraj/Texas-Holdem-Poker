package Game;



import net.GameServer;


import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.Semaphore;
import java.util.stream.Collectors;
class GenerateRandom{
    static final int min=1;
    static final int max=52;
    public static int getRandomInt()
    {
        return (int)Math.floor(Math.random()*(max-min+1)+min);
    }
}

public class Croupier{
    public static volatile String playerActionMessage="fold";
    public static volatile int raiseAmount = 0;
    public static volatile boolean isRunning=false;
    public final Object waitFor2Players = new Object();
    public final Object waitForMessage = new Object();
    public final Object waitForEndOfDelay = new Object();
    private final Semaphore waitForEndOfRound = new Semaphore(1);
    private final Semaphore waitForEndOfOperationsOnWaitingPlayers = new Semaphore(1);
    private static Croupier instance;
    public volatile int numberOfPlayers;
    public static String[] figures = {"Dwojki", "Trojki", "Czworki", "Piatki", "Szostki", "Siodemki", "Osemki", "Dziewiatki", "Dziesiatki"
            , "Jupki", "Damy", "Krole", "Asy"};
    public static String[] colors = {"Pik", "Kier", "Trefl", "Karo"};

    Map<Integer, Card> deck = new TreeMap<Integer, Card>();
    public Map<Integer, Card> table = new LinkedHashMap<>(52);
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
    public synchronized void addMoneyToThePot(int x)
    {
        //int temp = pot+x;
        pot+=x;
        String sb = "changePot-"+pot+"-";
        GameServer.getInstance().prepareAndSendDataFromCroupierToAllPlayers(sb);
    }
    public synchronized void subMoneyFromThePot(int x)
    {
        pot-=x;
    }
    public void zeroOutPot()
    {
        pot=0;
        String sb = "changePot-"+pot+"-";
        GameServer.getInstance().prepareAndSendDataFromCroupierToAllPlayers(sb);
    }
    public void setFirstPlayerInCycle(int x)
    {
        firstPlayerInCycle=x;
        System.out.println("Pierwszy gracz w cyklu to: "+playersHand[x].playerName);
        //StringBuffer sb = new StringBuffer("setFirst-"+x+"-playerName-"+playersHand[activePlayer].playerName+"-");
        //GameServer.getInstance().prepareAndSendDataFromCroupierToAllPlayers(sb.toString());
    }
    public void setMaxBet(int x)
    {
        maxBet=x;
    }
    public void setActivePlayer(int x)
    {
        activePlayer=x;
        String sb = "setActivePlayer-playerName-"+playersHand[x].playerName+"-";
        GameServer.getInstance().prepareAndSendDataFromCroupierToAllPlayers(sb);

    }

    //###########################################################################################################
    //METODY DO KOMUNIKACJI Z SERWEREM
    //###########################################################################################################

    public String returnPlayers() throws InterruptedException {
        waitForEndOfRound.acquire();
        StringBuffer sb = new StringBuffer("waitingRoomReceive");
        sb.append("-playersInGame-");
        if(playersHand!=null)
        {
            sb.append(playersHand.length);
            for(Hand player : playersHand)
            {
                sb.append("-"+player.playerName);
            }
            sb.append("-");
        }
        else {
            sb.append("0");
        }
        waitForEndOfRound.release();
        sb.append("-waitingPlayers-");
        waitForEndOfOperationsOnWaitingPlayers.acquire();
        if(waitingPlayers!=null)
        {
            sb.append(waitingPlayers.length);
            for(Hand player : waitingPlayers)
            {
                sb.append("-"+player.playerName);
            }
            sb.append("-");
        }
        else{
            sb.append("0-");
        }
        waitForEndOfOperationsOnWaitingPlayers.release();
        return sb.toString();
    }
    public int returnActivePlayerId()
    {
        return playersHand[activePlayer].playerId;
    }
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
    public void addWaitingPlayersToGame() throws InterruptedException {
        waitForEndOfRound.acquire();
        waitForEndOfOperationsOnWaitingPlayers.acquire();
        if(waitingPlayers!=null) {
            int numberOfWaitingPlayers = waitingPlayers.length;
            if (numberOfWaitingPlayers > 0) {
                if (playersHand == null) {
                    playersHand = new Hand[numberOfWaitingPlayers];
                    playersHand = waitingPlayers;
                    waitingPlayers = null;
                } else if (playersHand.length < 5) {
                    int numberOfCurrentPlayers = playersHand.length;
                    Hand[] temporary;
                    int temporaryLength = 5 - numberOfCurrentPlayers;
                    if (numberOfCurrentPlayers + numberOfWaitingPlayers < 5) {
                        temporary = new Hand[numberOfCurrentPlayers + numberOfWaitingPlayers];
                    } else {
                        temporary = new Hand[5];
                    }
                    System.arraycopy(playersHand, 0, temporary, 0, numberOfCurrentPlayers);
                    System.arraycopy(waitingPlayers, 0, temporary, numberOfCurrentPlayers, temporaryLength);
                    System.arraycopy(waitingPlayers, temporaryLength, waitingPlayers, 0, numberOfWaitingPlayers - temporaryLength);
                    playersHand=temporary;
                }
            }
        }
        waitForEndOfOperationsOnWaitingPlayers.release();
        waitForEndOfRound.release();
    }
    public void addPlayerToQueue(int Id, String nick, int amountOfMoney) throws InterruptedException {
        waitForEndOfRound.acquire();
        waitForEndOfOperationsOnWaitingPlayers.acquire();
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
        waitForEndOfOperationsOnWaitingPlayers.release();
        waitForEndOfRound.release();
    }

    public void removePlayerFromWaitingQueue(int playerId) throws InterruptedException {
        waitForEndOfOperationsOnWaitingPlayers.acquire();
        ArrayList<Hand> tempList = new ArrayList<>(Arrays.asList(waitingPlayers));
        Iterator<Hand> iterator = tempList.iterator();
        while(iterator.hasNext())
        {
            if(iterator.next().playerId==playerId)
            {
                iterator.remove();
            }
        }
        waitingPlayers= tempList.toArray(new Hand[tempList.size()]);
        waitForEndOfOperationsOnWaitingPlayers.release();
    }
    public void removeFromWaitingQueue()
    {

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
        System.out.println("\n\n");
        showDeck();
        System.out.println("\n\n");
        makeHands();
    }
    public void initializeCroupier() throws InterruptedException, SQLException {

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
        for (int i = 0; i < numberOfPlayers; i++) {
            StringBuffer sb = new StringBuffer("initializeInformations-"+playersHand[i].playerId+"-numberOfPlayers-"+numberOfPlayers);
            int y=(i+1)%numberOfPlayers;
            int z=0;
            do{
                sb.append("-playerId-"+playersHand[y].playerId+"-playerName-"+playersHand[y].playerName+"-amountOfMoney-"+
                        playersHand[y].amountOfMoney);
                y=(y+1)%numberOfPlayers;
                z++;
            }while(z<numberOfPlayers-1);
            sb.append("-");
            GameServer.getInstance().prepareAndSendDataFromCroupierToOnePlayer(sb.toString());
        }
        game();

    }
    public void game() throws InterruptedException, SQLException {
        do {
            int whichState = 0;
            addWaitingPlayersToGame();
            waitForEndOfRound.acquire();
            isPlayerPlayable();
            if(numberOfPlayers<2)
                break;
            cleanTable();
            dealCards();
            showHands();
            preFlop(); //dodac usuwanie w odpowiednim miejscu
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
            waitForEndOfRound.release();

            synchronized (waitForEndOfDelay) {
                waitForEndOfDelay.wait();
            }
        }while(numberOfPlayers>1);
        waitForEndOfRound.release();
        StringBuffer sb = new StringBuffer("endOfGame-");
        GameServer.getInstance().prepareAndSendDataFromCroupierToAllPlayers(sb.toString());
        endOfGame();
        System.out.println("KONIEC GRY!");
    }

    private void checkIfFirstPlayerInCycleHasChanged()
    {
        if(!playersHand[firstPlayerInCycle].isInCurrentRound) {
            do {
                setFirstPlayerInCycle(firstPlayerInCycle+1%numberOfPlayers);
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
            setActivePlayer(firstPlayerInCycle);
            do {
                int tempMaxBet=maxBet;
                int tempActualBet=playersHand[activePlayer].actualBet;
                int tempPot=pot;
                playerActionMultiplayer();
                if((pot)>(tempPot+(tempMaxBet-tempActualBet))){
                    setFirstPlayerInCycle(activePlayer);
                }
                setActivePlayer((activePlayer+1)%numberOfPlayers);
            } while (activePlayer != firstPlayerInCycle);
        }
        else {
            Arrays.sort(playersHand);
            distributePot(numberOfPlayers-1);
            StringBuffer sb = new StringBuffer("endOfRound-");
            GameServer.getInstance().prepareAndSendDataFromCroupierToAllPlayers(sb.toString());
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
        playersHand[idOfPlayer].addMoney(pot);
        playersHand[idOfPlayer].zeroOutActualBet();
        this.zeroOutPot();
    }
    private void getBlinds()
    {
        setMaxBet(bigBlind);
        if(playersHand[bigBlindPosition].amountOfMoney<bigBlind)
        {
            addMoneyToThePot(playersHand[bigBlindPosition].amountOfMoney);
            playersHand[bigBlindPosition].addActualBet(playersHand[bigBlindPosition].amountOfMoney);
            playersHand[bigBlindPosition].zeroOutMoney();
            playersHand[bigBlindPosition].setIsAllIn(true);
        }
        else
        {
            addMoneyToThePot(bigBlind);
            playersHand[bigBlindPosition].addActualBet(bigBlind);
            playersHand[bigBlindPosition].subMoney(bigBlind);
        }
        int smallBlindPosition=(bigBlindPosition-1+numberOfPlayers)%numberOfPlayers;
        if(playersHand[smallBlindPosition].amountOfMoney<smallBlind)
        {
            addMoneyToThePot(playersHand[smallBlindPosition].amountOfMoney);
            playersHand[smallBlindPosition].addActualBet(playersHand[smallBlindPosition].amountOfMoney);
            playersHand[smallBlindPosition].zeroOutMoney();
            playersHand[smallBlindPosition].setIsAllIn(true);
        }
        else
        {
            addMoneyToThePot(smallBlind);
            playersHand[smallBlindPosition].addActualBet(smallBlind);
            playersHand[smallBlindPosition].subMoney(smallBlind);
        }
        StringBuffer sb = new StringBuffer("bigBlindPosition-"+bigBlindPosition+"-bigBlind-"+bigBlind+"-smallBlindPosition-"+smallBlindPosition+"-");
        GameServer.getInstance().prepareAndSendDataFromCroupierToAllPlayers(sb.toString());
        bigBlindPosition=(bigBlindPosition+1)%numberOfPlayers;
        setActivePlayer(bigBlindPosition);
    }
    private void preFlop()
    {

        getBlinds();
        setFirstPlayerInCycle(bigBlindPosition);
        int firstPlayerInCycleNamedPreFlop=firstPlayerInCycle;
        do
        {
            int tempMaxBet=maxBet;
            int tempActualBet=playersHand[activePlayer].actualBet;
            int tempPot=pot;
            playerActionMultiplayer();
            if((pot)>(tempPot+(tempMaxBet-tempActualBet)))
            {
                firstPlayerInCycleNamedPreFlop=activePlayer;
            }
            setActivePlayer((activePlayer+1)%numberOfPlayers);
        }while(activePlayer!=firstPlayerInCycleNamedPreFlop);
    }
    private void playerActionMultiplayer()
    {
        if(playersHand[activePlayer].isInCurrentRound) {
            playerActionMessage="fold";
            if(playersHand[activePlayer].isAllIn)
            {
                //allInkkkkkk
                playerActionMessage="allIn";
            }
            else
            {
                boolean goodChoice = false;
                int diff = maxBet - playersHand[activePlayer].actualBet;
                if(playersHand[activePlayer].isInCurrentGame==false)
                {
                    playerActionMessage="fold";
                }
                if(diff==0)
                {
                    playerActionMessage="check";
                }
                GameServer.getInstance().prepareAndSendDataFromCroupierToOnePlayer("playerAction-"+playersHand[activePlayer].playerId+"-");
                do {
                    //System.out.println("Fold - 1, Bet - 2, Raise - 3, All in - 4, Check - 5\n");
                    //System.out.println("Gracz o id " + playersHand[activePlayer].playerId);
                    //System.out.println("Masz do wplacenia zaklad o wysokosci: " + diff + "\nTwoje pozostale pieniadze: " + playersHand[activePlayer].amountOfMoney);

                    synchronized (waitForMessage)
                    {

                        try {
                            waitForMessage.wait();
                        }catch(InterruptedException e)
                        {
                            e.printStackTrace();
                        }
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
                                raiseMultiplayer(diff, raiseAmount); // DODAJ RAISE AMOUNT
                                raiseAmount=0;
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
                //GameServer.getInstance().prepareAndSendDataFromCroupierToOnePlayer("player-"+activePlayer+"-yourTurn-");
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
        playersHand[activePlayer].addActualBet(diff);
        playersHand[activePlayer].subMoney(diff);
        addMoneyToThePot(diff);
        if(playersHand[activePlayer].amountOfMoney<=0)
        {
            playersHand[activePlayer].setIsAllIn(true);
        }
    }
    private void fold()
    {
        playersHand[activePlayer].setIsInCurrentRound(false);
        if(GameServer.getInstance().checkIfPlayerIsConnected(playersHand[activePlayer].playerName)==1) {
            StringBuffer sb = new StringBuffer("fold-playerName-" + playersHand[activePlayer].playerName + "-");
            GameServer.getInstance().prepareAndSendDataFromCroupierToAllPlayers(sb.toString());
        }

    }
    private void raiseMultiplayer(int diff, int raiseAmount)
    {
        playersHand[activePlayer].subMoney(raiseAmount);
        addMoneyToThePot(raiseAmount);
        setMaxBet(maxBet+(raiseAmount-diff));
        StringBuffer sb = new StringBuffer("setMaxBet-"+maxBet+"-playerName-"+playersHand[activePlayer].playerName+"-");
        GameServer.getInstance().prepareAndSendDataFromCroupierToAllPlayers(sb.toString());
        playersHand[activePlayer].addActualBet(raiseAmount);
        if(playersHand[activePlayer].amountOfMoney<=0)
        {
            playersHand[activePlayer].setIsAllIn(true);
        }
    }
    private void raise(int diff)
    {
        int raiseAmount;
        do {
            System.out.println("Podaj kwote, jaka chcesz przebic zaklad: \n");
            raiseAmount=sc.nextInt();
        }while(raiseAmount>playersHand[activePlayer].amountOfMoney || raiseAmount<diff);
        playersHand[activePlayer].subMoney(raiseAmount);
        addMoneyToThePot(raiseAmount);
        setMaxBet(maxBet+(raiseAmount-diff));
        StringBuffer sb = new StringBuffer("setMaxBet-"+maxBet+"-playerName-"+playersHand[activePlayer].playerName+"-");
        GameServer.getInstance().prepareAndSendDataFromCroupierToAllPlayers(sb.toString());
        playersHand[activePlayer].addActualBet(raiseAmount);
        if(playersHand[activePlayer].amountOfMoney<=0)
        {
            playersHand[activePlayer].setIsAllIn(true);
        }
    }
    private void allIn(int diff)
    {
        if(playersHand[activePlayer].amountOfMoney<maxBet)
        {
            addMoneyToThePot(playersHand[activePlayer].amountOfMoney);
            playersHand[activePlayer].addActualBet(playersHand[activePlayer].amountOfMoney);
            playersHand[activePlayer].zeroOutMoney();
        }
        else
        {
            addMoneyToThePot(playersHand[activePlayer].amountOfMoney);
            playersHand[activePlayer].addActualBet(playersHand[activePlayer].amountOfMoney);
            setMaxBet((maxBet-diff)+playersHand[activePlayer].amountOfMoney);
            StringBuffer sb = new StringBuffer("setMaxBet-"+maxBet+"-playerName-"+playersHand[activePlayer].playerName+"-");
            GameServer.getInstance().prepareAndSendDataFromCroupierToAllPlayers(sb.toString());
            playersHand[activePlayer].zeroOutMoney();
        }
        playersHand[activePlayer].setIsAllIn(true);
    }
    private void endOfGame() throws SQLException, InterruptedException {
        waitForEndOfRound.acquire();
        for (Hand player : playersHand)
        {
            player.exitFromGame();
        }
        isPlayerPlayable();
        waitForEndOfRound.release();
    }
    private void isPlayerPlayable() throws SQLException {
        if(playersHand==null)
            return;
        int countNonPlayablePlayers=0;
        for (int i = 0; i < numberOfPlayers; i++) {
            if(playersHand[i].amountOfMoney<=0 || playersHand[i].isInCurrentGame==false)
            {
                countNonPlayablePlayers++;
                playersHand[i].rankOfHand=-1;
            }
        }
        //System.out.println("Odpadlo: "+countNonPlayablePlayers+", graczy");
        if(countNonPlayablePlayers>0) {
            Arrays.sort(playersHand);
            for (int i = 0; i < countNonPlayablePlayers; i++) {
                GameServer.getInstance().saveDataAboutPlayer(playersHand[i].amountOfMoney, playersHand[i].playerId);
                StringBuffer sb = new StringBuffer("exitFromGame-playerName-"+playersHand[i].playerName+"-");
                GameServer.getInstance().prepareAndSendDataFromCroupierToAllPlayers(sb.toString());
            }
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
                int x=checker.checkAll();
                playersHand[i].setRankOfHand(x);

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
        StringBuffer sb = new StringBuffer("winners-"+winnersCount);
        for (int i = numberOfPlayers - 1; i >= numberOfPlayers - winnersCount; i--) {
            System.out.println("Wygrala reka " + playersHand[i].playerId + " z reka: " + playersHand[i].playerName +", z ukladem: " + playersHand[i].rankOfHand);
            sb.append("-"+playersHand[i].playerName);

        }
        sb.append("-");
        GameServer.getInstance().prepareAndSendDataFromCroupierToAllPlayers(sb.toString());
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
                                playersHand[i].zeroOutActualBet();
                                playersHand[i].setIsInCurrentRound(false);
                            } else {
                                tempAward += playersHand[numberOfPlayers - 1].actualBet;
                                playersHand[i].subActualBet(playersHand[numberOfPlayers - 1].actualBet);
                            }
                        }
                        subMoneyFromThePot(tempAward);
                        playersHand[numberOfPlayers - 1].addMoney(tempAward);
                        playersHand[numberOfPlayers - 1].setIsInCurrentRound(false);
                        setMaxBet(maxBet-playersHand[numberOfPlayers - 1].actualBet);
                        playersHand[numberOfPlayers - 1].zeroOutActualBet();
                        Arrays.sort(playersHand);
                        checkCurrentPlayingPlayers();
                        extractTheWinner();
                        return;
                    } else //jesli wygrany nie wyrownal do max zakladu, a graczy w partii jest dwoch. //x
                    {
                        int tempMoney=0;
                        for (int i = 0; i < numberOfPlayers-1; i++)
                        {
                            if (playersHand[i].actualBet > playersHand[numberOfPlayers-1].actualBet)
                            {
                                tempMoney += playersHand[numberOfPlayers-1].actualBet;
                                playersHand[i].subActualBet(playersHand[numberOfPlayers-1].actualBet);
                            }
                            else
                            {
                                tempMoney += playersHand[i].actualBet;
                                playersHand[i].zeroOutActualBet();
                                playersHand[i].setIsInCurrentRound(false);
                            }
                        }
                        tempMoney+=playersHand[numberOfPlayers-1].actualBet;
                        playersHand[numberOfPlayers-1].addMoney(tempMoney);
                        playersHand[numberOfPlayers-1].zeroOutActualBet();
                        subMoneyFromThePot(tempMoney);
                        playersHand[numberOfPlayers-2].addMoney(pot);
                        zeroOutPot();
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
                            playersHand[numberOfPlayers - i - 1].addMoney(tempSplitAward);
                        }
                        zeroOutPot();
                    }
                    else // niektorzy z wygranych nie maja maksymalnej stawki //x
                    {
                        for (int i = 0; i <= numberOfPlayers-1-winnersCount; i++) //zbieranie tymczasowej wygranej zlozonej z najmniejszego wkladu sposrod graczy, ktorzy wygrali
                        {
                            if (playersHand[i].actualBet > smallerBet)
                            {
                                tempSplitAward += smallerBet;
                                playersHand[i].subActualBet(smallerBet);
                            }
                            else
                            {
                                tempSplitAward += playersHand[i].actualBet;
                                playersHand[i].zeroOutActualBet();
                                playersHand[i].setIsInCurrentRound(false);
                            }
                        }
                        subMoneyFromThePot(tempSplitAward);
                        tempSplitAward=tempSplitAward/winnersCount;
                        for (int j = numberOfPlayers-1; j > numberOfPlayers-1-winnersCount; j--) //rozdanie tymczasowej wygranej, oraz usuniecie graczy ktorzy mieli najmniejszy zaklad sposrod wygranych //x
                        {
                            playersHand[j].addMoney(tempSplitAward);
                            if(playersHand[j].actualBet==smallerBet) {
                                playersHand[j].addMoney(playersHand[j].actualBet);
                                playersHand[j].zeroOutActualBet();
                                playersHand[j].setIsInCurrentRound(false);
                            }
                            else
                            {
                                playersHand[j].addMoney(smallerBet);
                                playersHand[j].subActualBet(smallerBet);
                            }
                        }
                        setMaxBet(maxBet-smallerBet);
                        Arrays.sort(playersHand);
                        checkCurrentPlayingPlayers();
                        extractTheWinner(); // wywolanie funkcji ponownie, z mniejsza iloscia graczy, o tych ktorzy juz zebrali swoja wygrana, az do momentu wyzerowania pot'a
                        return;
                    }
                }
            }
            StringBuffer sb = new StringBuffer("endOfRound-");
            GameServer.getInstance().prepareAndSendDataFromCroupierToAllPlayers(sb.toString());
        }
    }

    private void prepareForNextRound()
    {
        for (int i = 0; i <= numberOfPlayers-1; i++) {
            playersHand[i].setIsInCurrentRound(true);
            playersHand[i].zeroOutActualBet();
            playersHand[i].setIsAllIn(false);;
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
                        //playersHand[j].setIsInCurrentRound(true);
                        check = false;
                        StringBuffer sb = new StringBuffer("card-"+i+"-forPlayerName-"+playersHand[j].playerName+"-numberOfCard-"+random+"-");
                        GameServer.getInstance().prepareAndSendDataAboutCardFromCroupierToAllPlayers(sb.toString());
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
        StringBuilder sb = new StringBuilder("CardsOnTable-"+table.size());
        for(int key : table.keySet())
        {
            sb.append("-"+key);
        }
        sb.append("-");
        GameServer.getInstance().prepareAndSendDataFromCroupierToAllPlayers(sb.toString());
        System.out.println(WhatIsOnTable);
    }
    //###########################################################################################################
    //KONIEC METOD ZWIĄZANYCH ZE STOŁEM
    //###########################################################################################################

    //###########################################################################################################
    //PODKLASY, ORAZ FUNKCJE NADPISANE
    //###########################################################################################################
    public void removePlayerFromGame(int id) throws InterruptedException, SQLException {
        waitForEndOfRound.acquire();
        for(Hand player: playersHand)
        {
            if(player.playerId==id)
            {
                player.exitFromGame();
                break;
            }
        }
        waitForEndOfRound.release();
    }

    public class Hand implements Comparable<Hand>{
        public Map<Integer, Card> hand = new LinkedHashMap<Integer, Card>();
        int playerId;
        String playerName;
        int rankOfHand;
        int amountOfMoney=500; //pula pieniedzy gracza
        int actualBet=0;
        boolean isAllIn=false;
        boolean isInCurrentRound;
        boolean isInCurrentGame=true;
        public void setRankOfHand(int x)
        {
            rankOfHand=x;
            StringBuffer sb = new StringBuffer("rankOfHandFor-"+playerId+"-Is-"+CheckHand.ranksOfHand[x]+"-");
            GameServer.getInstance().prepareAndSendDataFromCroupierToOnePlayer(sb.toString());
        }
        public void addActualBet(int x)
        {
            actualBet+=x;
            StringBuffer sb = new StringBuffer("actualBet-"+actualBet+"-playerName-"+playerName+"-");
            GameServer.getInstance().prepareAndSendDataFromCroupierToAllPlayers(sb.toString());
        }
        public void subActualBet(int x)
        {
            actualBet-=x;
            StringBuffer sb = new StringBuffer("actualBet-"+actualBet+"-playerName-"+playerName+"-");
            GameServer.getInstance().prepareAndSendDataFromCroupierToAllPlayers(sb.toString());
        }
        public void zeroOutActualBet()
        {
            actualBet=0;
            StringBuffer sb = new StringBuffer("actualBet-0-playerName-"+playerName+"-");
            GameServer.getInstance().prepareAndSendDataFromCroupierToAllPlayers(sb.toString());
        }
        public void exitFromGame() throws InterruptedException, SQLException {
            //
            isInCurrentGame = false;
            //StringBuffer sb = new StringBuffer("exitFromGame-playerName-"+playerName+"-");
            //GameServer.getInstance().prepareAndSendDataFromCroupierToAllPlayers(sb.toString());
        }
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
        public void setIsAllIn(boolean value)
        {
            if(isAllIn==value)
                return;
            else{
                isAllIn=value;
                StringBuffer sb = new StringBuffer("allIn-playerName-"+playerName+"-");
                GameServer.getInstance().prepareAndSendDataFromCroupierToAllPlayers(sb.toString());
            }
        }
        public void addMoney(int x)
        {
            amountOfMoney+=x;
            StringBuffer sb = new StringBuffer("amountOfMoney-"+amountOfMoney+"-playerName-"+playerName+"-");
            GameServer.getInstance().prepareAndSendDataFromCroupierToAllPlayers(sb.toString());
        }
        public void subMoney(int x)
        {
            amountOfMoney-=x;
            StringBuffer sb = new StringBuffer("amountOfMoney-"+amountOfMoney+"-playerName-"+playerName+"-");
            GameServer.getInstance().prepareAndSendDataFromCroupierToAllPlayers(sb.toString());
        }
        public void zeroOutMoney()
        {
            amountOfMoney=0;
            StringBuffer sb = new StringBuffer("amountOfMoney-0-playerName-"+playerName+"-");
            GameServer.getInstance().prepareAndSendDataFromCroupierToAllPlayers(sb.toString());
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
        playersHand[numberOfPlayers-1].setIsAllIn(true);

        for (int i = 0; i < numberOfPlayers; i++) {
            System.out.println("Gracz nr "+ playersHand[i].playerId + ", wplacil: "+playersHand[i].actualBet);
        }
    }
    public void addMoneyToThePot_changeFirstPlayerToAllIn()
    {
        playersHand[0].amountOfMoney=0;
        playersHand[0].actualBet=100;
        playersHand[0].setIsAllIn(true);
    }
    public void addMoneyToThePot_changeLastPlayerToAllInAndRestPlayersFold()
    {

        playersHand[numberOfPlayers-1].amountOfMoney=0;
        playersHand[numberOfPlayers-1].actualBet=500;
        playersHand[numberOfPlayers-1].setIsAllIn(true);
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
        playersHand[numberOfPlayers-1].setIsAllIn(true);
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

    public void isPlayerPlayable_makePublic() throws SQLException {
        isPlayerPlayable();
    }
    //###########################################################################################################
    //KONIEC FUNKCJI ZWIAZANYCH Z TESTOWANIEM
    //###########################################################################################################
}