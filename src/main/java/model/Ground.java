package model;

import java.util.Arrays;
import java.util.Random;

/**
 * Ground where the ant colony will evolve
 *
 * @author Antoine Jonard
 *
 */
public class Ground {
    /** All the cells representing the ground with coordinates*/
    private final Cell [][] grid;
    /** grid size*/
    private final int size;
    /** number of ants */
    private int antsNumber;
    /** number of obstacles */
    private final int nbObstacles = 17;
    /**obstacle length*/
    private final int obstacleLength = 18;
    /** All the ants on the ground */
    private Ant[] ants;
    /** x coordinate of the nest */
    private int xNest;
    /** y coordinate of the nest */
    private int yNest;

    private FoodManager fm;

    public Ground()
    {
        grid = new  Cell[20][20];
        size = 20;
    }

    /** intialize ground attributes : instantiate cells, obstacles, nest, food, ants*/
    public Ground(int taille, int _nbFourmis, FoodManager fm)
    {
        this.fm = fm;
        this.size = taille;
        grid = new Cell[taille][taille];
        this.antsNumber = _nbFourmis ;
        init();
        initFood();
        xNest = taille/2;
        yNest = taille/2;
        initNid();
        initObstacles();
        initAnts(antsNumber);
    }

    /**
     * initialize cells
     */
    private void init()
    {
        for(int i = 0; i< size; i++)
            for(int j = 0; j< size; j++)
                grid[i][j] = new Cell(grid, i, j, fm);
    }


    /**
     * initialize food
     */
    private void initFood()
    {
        setFood((size) / 9, size / 9, 3);
        setFood((9* size) / 10, size / 9, 3);
        setFood((size) / 10, size / 2, 3);
        setFood((8* size) / 9, (3* size) / 4, 5);
        setFood((size) / 9, (8* size) / 9, 4);
    }


    /** create a food zone
     * @param xMeat x coordinate of the center of the food zone
     * @param yMeat y coordinate of the center of the food zone
     * @param zoneSize size of the food zone
     */
    private void setFood(int xMeat, int yMeat, int zoneSize)
    {
        for(int i=-zoneSize; i<=zoneSize; i++)
        {
            int xx = (xMeat + i + size) % size;
            for(int j=-zoneSize; j<=zoneSize; j++)
            {
                int yy = (yMeat + j + size) % size;
                if((Math.abs(i)+Math.abs(j))<=zoneSize)
                    grid[xx][yy].setFood(fm.putFood());
            }
        }
    }

    /** create the nest zone
     * @param xNest x coordinate of the center of the nest zone
     * @param yNest y coordinate of the center of the nest zone
     * @param nestSize size of the nest (radius) */
    private void setNest(int xNest, int yNest, int nestSize)
    {
        for(int i=-nestSize; i<=nestSize; i++)
        {
            int xx = (xNest + i + size) % size;
            for(int j=-nestSize; j<=nestSize; j++)
            {
                int yy = (yNest + j + size) % size;
                if((Math.abs(i)+Math.abs(j))<=nestSize)
                    grid[xx][yy].setNest(true);
            }
        }
    }


    /** initialize the nest by setting the zone and the odor on every cells on the ground */
    private void initNid()
    {
        grid[xNest][yNest].setNest(true);
        for(int i = 0; i< size; i++)
            for(int j = 0; j< size; j++)
            {
                Cell cell = grid[i][j];
                /** Odor from the nest */
                double nestSmelling = 40d;
                double smelling = nestSmelling / (Math.abs(xNest -cell.getX()) + Math.abs(yNest -cell.getY()));
                cell.setNestSmelling(smelling);
            }
        setNest(xNest, yNest, 2);
    }

    private void initObstacles() {
        Random random = new Random(666);
        for (int i = 0 ; i < nbObstacles ; i++){
            int startX = random.nextInt(size);
            int startY = random.nextInt(size);

            boolean vertical = random.nextBoolean();

            for (int x = startX; x < startX + (vertical?1:obstacleLength) && x < size; x++){
                for (int y = startY; y < startY + (vertical?obstacleLength:1) && y < size; y++){
                    Cell obstacleCell = grid[x][y];
                    if (obstacleCell.isNest())
                        break;
                    obstacleCell.setObstacle(true);
                }
            }
        }
    }

    /** creates all the ants with an intial position in the nest
     * @param nb ants number*/
    private void initAnts(int nb)
    {
        ants = new Ant[nb];
        for(int i=0; i<nb; i++)
        {
            ants[i] = new Ant(xNest, yNest, this);
        }
    }

    /**
     * Make all the cells updating their states : diffusing and evaporating
     */
    public void animGrid()
    {
        for(Ant f: ants) f.evolve();
        for(int i = 0; i< size; i++)
            for(int j = 0; j< size; j++)
                grid[i][j].diffuse();
        for(int i = 0; i< size; i++)
            for(int j = 0; j< size; j++)
                grid[i][j].evaporate();
    }

    /**
     * Compute the total number of turns from all ants of the simulation
     * @return total number of turns
     */
    public int getNbAntsTurns(){
        return Arrays.stream(ants).mapToInt(Ant::getTurns).sum();
    }

    /**
     * @return the grid
     */
    public Cell[][] getGrid() {
        return grid;
    }


    /**
     * @return the size
     */
    public int getSize() {
        return size;
    }

    /**
     * @return the number of ants
     */
    public int getAntsNumber() {
        return antsNumber;
    }

    /**
     * @return the xNid
     */
    public int getxNest() {
        return xNest;
    }

    /**
     * @return the yNest
     */
    public int getyNest() {
        return yNest;
    }

    /**
     * @return the ants array
     */
    public Ant[] getAnts() {
        return ants;
    }
}