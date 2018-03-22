package CI346.websockets.noughtsandcrosses;

public class Game {
    private final Player p1;
    private final Player p2;
    private Player inPlay;

    public Game(Player p1, Player p2) {
        this.p1 = p1;
        this.p2 = p2;
        setInPlay(p1);
    }

    public void takeTurn() {
        setInPlay((isInPlay().equals(getP1())) ? getP2() : getP1());
    }

    public Player getP1() {
        return p1;
    }

    public Player getP2() {
        return p2;
    }

    public Player isInPlay() {
        return inPlay;
    }

    public void setInPlay(Player inPlay) {
        this.inPlay = inPlay;
    }
}
