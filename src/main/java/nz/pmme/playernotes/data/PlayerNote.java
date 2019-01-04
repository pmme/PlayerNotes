package nz.pmme.playernotes.data;

public class PlayerNote
{
    public int id;
    public String notingPlayer;
    public String player;
    public String note;

    public PlayerNote( int id, String notingPlayer, String player, String note )
    {
        this.id = id;
        this.notingPlayer = notingPlayer;
        this.player = player;
        this.note = note;
    }
}
