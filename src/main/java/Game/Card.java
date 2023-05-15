package Game;

public class Card implements Comparable<Card> {
    private final String nameOfFigure;
    final String colorName;
    int idOfFigure;
    int idOfColor;

    public Card(String x, String y, int k, int l) {
        nameOfFigure = x;
        colorName = y;
        idOfFigure = k;
        idOfColor = l;
    }

    @Override
    public String toString() {
        return nameOfFigure + " " + colorName + " "+ idOfFigure+ " "+ idOfColor;
    }

    @Override
    public int compareTo(Card x) {
        if (this.idOfFigure == (x.idOfFigure))
            return 0;
        else if ((this.idOfFigure) > (x.idOfFigure))
            return 1;
        else
            return -1;
    }
}
