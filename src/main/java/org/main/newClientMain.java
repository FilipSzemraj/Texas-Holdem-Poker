package org.main;

public class newClientMain {
    public static void main(String[] args) {
        Main.main(args);
        //Main.main(args);
        //ogarnac zamykanie w innej kolejnosci niz odwrotna otwieranie okien klienta, teraz chyba serwer sie nei wylacza, sprawdzic!
        //czekac z usunieciem gracza do konca kolejki
        //skrocic oczekiwanie na playerAction jak opusci gre.

        //netstat -ano | findstr 1331
        //taskkill /F /T /PID 10276
    }
}
