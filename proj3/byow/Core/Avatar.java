package byow.Core;

import byow.TileEngine.Tileset;

public class Avatar {
    public int x;
    public int y;
    public int health;
    public Avatar(int xCord, int yCord) {
        this.x = xCord;
        this.y = yCord;
        this.health = 3;
    }

}