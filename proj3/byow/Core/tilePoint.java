package byow.Core;

import java.util.Comparator;

public class tilePoint {
    public int x;
    public int y;
    public tilePoint(int xCord,int yCord) {
        x = xCord;
        y = yCord;
    }
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof tilePoint)) {
            return false;
        }
        tilePoint oTile = (tilePoint) o;
        if (this.x == oTile.x && this.y == oTile.y) {
            return true;
        }
        return false;
    }
    @Override
    public int hashCode()
    {
        return 10 * this.x + 11 * this.y;
    }


}