package org.main;

public class newServerMain {
    public static void main(String[] args) {
        serverMain.main(args);
        //setFirstPlayerInCycle
        /*
        Exception in thread "Thread-5" java.lang.ArrayIndexOutOfBoundsException: Index -1 out of bounds for length 3
	at Game.Croupier.setFirstPlayerInCycle(Croupier.java:88)
	at Game.Croupier.preFlop(Croupier.java:438)
	at Game.Croupier.game(Croupier.java:316)
	at Game.Croupier.initializeCroupier(Croupier.java:302)
	at net.GameServer.lambda$run$1(GameServer.java:99)
         */

        //netstat -ano | findstr 1331
        //taskkill /F /T /PID 10276
    }
}
