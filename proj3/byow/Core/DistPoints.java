package byow.Core;

public class DistPoints implements Comparable<DistPoints> {
    public tilePoint location;
    public DistPoints edgeTo;
    public int distTo;
    public DistPoints(tilePoint loc, DistPoints edge, int dist) {
        location = loc;
        edgeTo = edge;
        distTo = dist;
    }

    @Override
    public int compareTo(DistPoints o) {
        if (this.distTo < o.distTo) {
            return -1;
        }
        if (this.distTo > o.distTo) {
            return 1;
        }
        return 0;
    }
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof DistPoints)) {
            return false;
        }
        DistPoints oTile = (DistPoints) o;
        if (this.location.equals(oTile.location)) {
            return true;
        }
        return false;
    }
}