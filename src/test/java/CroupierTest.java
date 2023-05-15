import Game.Croupier;
import org.junit.Test;
import static org.junit.Assert.*;

public class CroupierTest {
    Croupier krupier = new Croupier(5);
    @Test
    public void checkIfPlayerWithAllInHaveAction_oneWinner_true()
    {
        //given
        krupier.dealCardsWithAssumptions_checkWhenWinningCardsOnTableAndMattersTheHighestCard();

        //when
        krupier.setIsInCurrentRoundForAllPlayers();
        krupier.addMoneyToThePot_ForOneWinner();
        krupier.checkCurrentPlayingPlayers_makePublic();
        krupier.checkForAllHands_makePublic();
        int winnersAmountOfMoney=krupier.extractTheWinner_makePublic();

        //then
        assertEquals(900, winnersAmountOfMoney);
    }
    @Test
    public void checkIfPlayerWithAllInHaveAction_ThreeWinners_true()
    {
        //given
        krupier.dealCardsWithAssumptions_checkWhenWinningCardsOnTableAndMattersTheHighestCard();

        //when
        krupier.setIsInCurrentRoundForAllPlayers();
        krupier.addMoneyToThePot_ForTwoWinners();
        krupier.checkCurrentPlayingPlayers_makePublic();
        krupier.checkForAllHands_makePublic();
        int winnersAmountOfMoney=krupier.extractTheWinner_makePublic();

        //then
        assertEquals(700, winnersAmountOfMoney);
    }
}
