import Game.Croupier;
import org.junit.Test;

import java.sql.SQLException;

import static org.junit.Assert.*;

public class CroupierTest {
    Croupier krupier;
    {
        krupier = Croupier.getInstance();
        krupier.firstStepInCroupier(5);
    }
    @Test
    public void checkForAllHands_oneWinnerWithMaxBet_true()
    {
        //given
        krupier.dealCardsWithAssumptions_checkWhenWinningCardsOnTableAndMattersTheHighestCard();

        //when
        krupier.addMoneyToThePot_ForEverybodyWithMaxBet();
        krupier.checkForAllHands_makePublic();
        int winnersAmountOfMoney=krupier.extractTheWinner_makePublic();

        //then
        assertEquals(900, winnersAmountOfMoney);
    }
    @Test
    public void checkForAllHands_oneWinnerWithoutMaxBet_true()
    {
        //given
        krupier.dealCardsWithAssumptions_checkWhenWinningCardsOnTableAndMattersTheHighestCard();

        //when
        krupier.addMoneyToThePot_ForLastPlayerWithoutMaxBet();
        krupier.checkForAllHands_makePublic();
        int winnersAmountOfMoney=krupier.extractTheWinner_makePublic();

        //then
        assertEquals(250, winnersAmountOfMoney);
    }

    @Test
    public void checkForAllHands_DrawTwoWinnersWithMaxBet_true()
    {
        //given
        krupier.dealCardsWithAssumptions_checkWhenTwoPlayersHaveDraw();

        //when
        krupier.addMoneyToThePot_ForEverybodyWithMaxBet();
        krupier.checkForAllHands_makePublic();
        int winnersAmountOfMoney=krupier.extractTheWinner_makePublic();

        //then
        assertEquals(650, winnersAmountOfMoney);
    }
    @Test
    public void checkForAllHands_DrawTwoWinnersOneWithoutMaxBet_true()
    {
        //given
        krupier.dealCardsWithAssumptions_checkWhenTwoPlayersHaveDraw();

        //when
        krupier.addMoneyToThePot_ForLastPlayerWithoutMaxBet();
        krupier.checkForAllHands_makePublic();
        int winnersAmountOfMoney=krupier.extractTheWinner_makePublic();

        //then
        assertEquals(125, winnersAmountOfMoney);
    }
    @Test
    public void checkForAllHandsAndTestIsPlayerPlayable_DrawTwoWinnersOneWithoutMaxBetWithOnePlayerAllIn_true() throws SQLException {
        //given
        krupier.dealCardsWithAssumptions_checkWhenTwoPlayersHaveDraw();

        //when
        krupier.addMoneyToThePot_ForLastPlayerWithoutMaxBet();
        krupier.addMoneyToThePot_changeFirstPlayerToAllIn();
        krupier.checkForAllHands_makePublic();
        int winnersAmountOfMoney=krupier.extractTheWinner_makePublic();
        krupier.isPlayerPlayable_makePublic();
        int length=krupier.numberOfPlayers;
        boolean check=false;
        System.out.println("Zostalo: "+length+", graczy");
        if(length==4 && winnersAmountOfMoney==125)
            check=true;

        //then
        assertTrue(check);
    }
    @Test
    public void checkForAllHands_OnlyOneCurrentPlayerPlayingRestFolds_true() throws SQLException {
        //given
        krupier.dealCardsWithAssumptions_checkWhenTwoPlayersHaveDraw();

        //when
        krupier.addMoneyToThePot_ForEverybodyWithMaxBet();
        krupier.addMoneyToThePot_changeLastPlayerToAllInAndRestPlayersFold();
        krupier.checkForAllHands_makePublic();
        int winnersAmountOfMoney=krupier.extractTheWinner_makePublic();
        krupier.isPlayerPlayable_makePublic();
        int length=krupier.numberOfPlayers;
        boolean check=false;
        if(length==5 && winnersAmountOfMoney==900)
            check=true;

        //then
        assertTrue(check);
    }
    @Test
    public void checkForAllHands_OnlyTwoCurrentPlayingPlayersButWinnerDoesntHaveMaxBet_true() throws SQLException {
        //given
        krupier.dealCardsWithAssumptions_checkWhenWinningCardsOnTableAndMattersTheHighestCard();

        //when
        krupier.addMoneyToThePot_ForLastPlayerWithoutMaxBet();
        krupier.addMoneyToThePot_changeLastPlayerToSmallerBetAndFoldEveryoneElseBesidesOfPlayerFour();
        krupier.checkForAllHands_makePublic();
        int winnersAmountOfMoney=krupier.extractTheWinner_makePublic();
        krupier.isPlayerPlayable_makePublic();
        int length=krupier.numberOfPlayers;
        boolean check=false;
        if(length==5 && winnersAmountOfMoney==250)
            check=true;

        //then
        assertTrue(check);
    }
}
