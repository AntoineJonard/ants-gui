package model;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

/**
 * directions representing the cardinal points
 *
 * @author Antoine Jonard
 *
 * */
public enum Direction {
    NORD(0, 0, -1), NORD_EST(1, 1, -1), EST(2,1,0), SUD_EST(3,1,1), SUD(4,0,1), SUD_OUEST(5,-1,1), OUEST(6,-1,0), NORD_OUEST(7,-1,-1);
    /** index of the direction*/
    final int no;
    /** director vector*/
    final Point v;

    /**
     * @param _no index of the direction
     * @param _x horizontal vector coordinate
     * @param _y vertical vector coordinate
     */
    Direction(int _no,int _x, int _y){no = _no; v = new Point (_x, _y);}


    /**
     * @param dir base direction
     * @return inverse direction */
    static Direction getInverse(Direction dir)
    {
        Direction [] tab = Direction.values();
        int index = (dir.no + 4) % 8;
        return tab[index];
    }

    /**
     * @param dir base direction
     * @return 3 neighboring directions*/
    static Direction[] getFrontDirections(Direction dir)
    {
        Direction[] dir3 = new Direction[3];
        Direction[] tab = Direction.values();
        int j=0;
        for(int i=-1; i<=1; i++)
        {
            int index = (dir.no + i + 8) % 8;
            dir3[j++] = tab[index];
        }
        return dir3;
    }

    /**
     * @param p initial point
     * @param d direction vector which will be applied on the initial point to get new point
     * @return the next point
     **/
    static Point getNextPoint(Point p, Direction d)
    {
        return new Point( p.x + d.v.x, p.y + d.v.y);
    }

    /**
     * @return random direction
     **/
    static Direction randomDirection()
    {
        Direction[]tab = Direction.values();
        int i = (int)(Math.random()*tab.length);
        return tab[i];
    }

    /**
     * Return an ordered list of directions from the base direction, and to the left or right
     */
    static List<Direction> asidesDirections(Direction from, boolean rotationDirection){
        List<Direction> asidesDirections = new ArrayList<>();
        Direction [] tab = Direction.values();

        for (int i = 1 ; i < 8 ; i++){
            asidesDirections.add(tab[(from.no + (rotationDirection?1:-1)*i+8)%8]);
        }

        return  asidesDirections;
    }

}


