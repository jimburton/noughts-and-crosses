package CI346.websockets.noughtsandcrosses;

public class Player {
    private final String name;
    private boolean inGame;

    public Player(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public boolean isInGame() {
        return inGame;
    }

    public void setInGame(boolean inGame) {
        this.inGame = inGame;
    }

    @Override
    public boolean equals(Object that) {
        if(!(that instanceof Player)) {
            return false;
        }
        Player p = (Player) that;
        return this.getName().equals(p.getName());
    }
}
