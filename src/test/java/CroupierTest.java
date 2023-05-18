import Game.Croupier;
import org.junit.Test;
import static org.junit.Assert.*;

public class CroupierTest {
    Croupier krupier = new Croupier(5);
    @Test
    public void checkForAllHands_oneWinnerWithMaxBet_true()
    {
        //given
        krupier.dealCardsWithAssumptions_checkWhenWinningCardsOnTableAndMattersTheHighestCard();

        //when
        krupier.setIsInCurrentRoundForAllPlayers();
        krupier.addMoneyToThePot_ForEverybodyWithMaxBet();
        krupier.checkCurrentPlayingPlayers_makePublic();
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
        krupier.setIsInCurrentRoundForAllPlayers();
        krupier.addMoneyToThePot_ForLastPlayerWithoutMaxBet();
        krupier.checkCurrentPlayingPlayers_makePublic();
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
        krupier.setIsInCurrentRoundForAllPlayers();
        krupier.addMoneyToThePot_ForEverybodyWithMaxBet();
        krupier.checkCurrentPlayingPlayers_makePublic();
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
        krupier.setIsInCurrentRoundForAllPlayers();
        krupier.addMoneyToThePot_ForLastPlayerWithoutMaxBet();
        krupier.checkCurrentPlayingPlayers_makePublic();
        krupier.checkForAllHands_makePublic();
        int winnersAmountOfMoney=krupier.extractTheWinner_makePublic();

        //then
        assertEquals(125, winnersAmountOfMoney);
    }
    @Test
    public void checkForAllHandsAndTestIsPlayerPlayable_DrawTwoWinnersOneWithoutMaxBetWithOnePlayerAllIn_true()
    {
        //given
        krupier.dealCardsWithAssumptions_checkWhenTwoPlayersHaveDraw();

        //when
        krupier.setIsInCurrentRoundForAllPlayers();
        krupier.addMoneyToThePot_ForLastPlayerWithoutMaxBet();
        krupier.addMoneyToThePot_changeFirstPlayerToAllIn();
        krupier.checkCurrentPlayingPlayers_makePublic();
        krupier.checkForAllHands_makePublic();
        int winnersAmountOfMoney=krupier.extractTheWinner_makePublic();
        krupier.isPlayerPlayable_makePublic();
        int length=krupier.numberOfPlayers;
        boolean check=false;
        if(length==4 && winnersAmountOfMoney==125)
            check=true;

        //then
        assertTrue(check);
    }
}
