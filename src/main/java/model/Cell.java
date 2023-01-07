package model;

import java.awt.*;

/**
 * This class represent one cell of the ground, one location.
 *
 * @author  Antoine Jonard
 */
public class Cell
{
    /**coordinates of the cell in the ground*/
    private int x,y;
    /** evaporation degree (%)*/
    public static double evaporation  = 0.25;
    /** diffusion degree (%)*/
    public static double diffusion = 0.0026;
    /** below this threshold, the pheromone is not considered by ants anymore*/
    public static int pheroNullExponent = 55;
    /**pheromones*/
    private double pheromone;
    /**food quantity*/
    private int food;
    /**is the cell part of the nest*/
    private boolean nest;
    /**the cell is occupied by an obstacle*/
    private boolean obstacle;
    /**nest odor*/
    private double nestSmelling;
    /**is there still food in the cell ?*/
    private boolean emptyNow;

    /** cell grid from the ground*/
    static Cell[][] grid;

    /**cells attributes changed recently*/
    public boolean hasJustChanged;

    private FoodManager fm;

    public Cell(){}

    /** create a default cell by placing it into the grid*/
    public Cell(Cell [][] grille, int x, int y, FoodManager fm)
    {
        this.fm = fm;
        Cell.grid = grille;
        this.x = x; this.y = y;
        hasJustChanged = true;
        nest = false;
    }

    /**
     * diffuse the pheromone to the cells around
     * simplified version : the diffused pheromone is not removed on the center cell
     */
    void diffuse()
    {
        if(pheromone!=0)
        {
            int size = Cell.grid.length;
            for(int i=-1; i<2; i++)
            {
                int xx = x+i;
                for(int j=-1; j<2; j++)
                {
                    if (i==0 && j==0) continue;
                    int yy = y+j;
                    if(xx<0 || xx>size-1) continue;
                    if(yy<0 || yy>size-1) continue;
                    Cell cell = Cell.grid[xx][yy];
                    double phero = cell.getPheromone();
                    cell.setPheromone(phero + pheromone * Cell.diffusion);
                    cell.hasJustChanged = true;
                }
            }
        }
    }

    /** make some pheromone of the cell disappear with the time*/
    void evaporate()
    {
        hasJustChanged = false;
        if (pheromone!=0)
        {
            pheromone = pheromone * (1d - Cell.evaporation);
            if (!isPheromone()) pheromone = 0;
            hasJustChanged = true;
        }
    }

    public boolean isPheromone() {
        return pheromone >= Math.pow(2,-pheroNullExponent);
    }

    /**
     * remove food taken by an ant
     */
    void removeFood()
    {
        fm.takeFood(Math.min(food, Ant.doseFoodCarried));
        food -= Ant.doseFoodCarried;
        if(food <0){
            food =0;
            emptyNow =true;
        }
        hasJustChanged = true;
    }

    /**
     * @return the food of the cell
     */
    public int getFood() {
        return food;
    }

    /**
     * @param food the nourriture to set
     */
    public void setFood(int food) {
        this.food = food;
    }

    /**
     * @return the pheromone
     */
    public double getPheromone() {
        return pheromone;
    }

    /**
     * @param _pheromone the pheromone to set
     */
    public void setPheromone(double _pheromone) {
        hasJustChanged = (pheromone==0d || _pheromone==0d);
        pheromone = _pheromone;
    }

    public void addPheromone(double pheromone) {
        hasJustChanged = (pheromone==0d);
        this.pheromone += pheromone;
    }

    public Cell getNextCell(Direction dir){
        Cell cell = null;
        Point newPoint = Direction.getNextPoint(new Point(x,y), dir);
        if ((newPoint.x>=0 && newPoint.x < grid.length) && (newPoint.y>=0 && newPoint.y< grid.length))
        {
            cell = grid[newPoint.x][newPoint.y];
        }
        return cell;
    }

    /**
     * @return is the cell a nest
     */
    public boolean isNest() {
        return nest;
    }

    /**
     * @param nest the nest to set
     */
    public void setNest(boolean nest) {
        this.nest = nest;
    }

    public boolean isObstacle(){
        return obstacle;
    }

    public void setObstacle(boolean obstacle){
        this.obstacle = obstacle;
    }

    /**
     * @return the x of the cell in the grid
     */
    public int getX() {
        return x;
    }

    /**
     * @return the y of the cell in the grid
     */
    public int getY() {
        return y;
    }

    /**
     * @return the nest smelling of the cell
     */
    public double getNestSmelling() {
        return nestSmelling;
    }

    /**
     * @param nestSmelling to set
     */
    public void setNestSmelling(double nestSmelling) {
        this.nestSmelling = nestSmelling;
    }

    /**
     * @return the emptyNow
     */
    public boolean isEmptyNow() {
        return emptyNow;
    }

    /**
     * @param emptyNow the emptyNow to set
     */
    public void setEmptyNow(boolean emptyNow) {
        this.emptyNow = emptyNow;
    }

    public boolean isAccessible(){
        return !isObstacle() && !isNest();
    }

}