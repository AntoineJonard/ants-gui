package model;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import javafx.scene.shape.Circle;

/**A ant which evolve on the ground, looking for food and helped by the pheromones*
 *
 * @author Antoine Jonard
 */
public class Ant {
    /**ant position*/
    Point p;
    /**ant direction*/
    private Direction direction;
    /**ant state (looking for food, bringing back food, dropping pheromones, ...)*/
    private AntState state;
    /**current number of pheromone the ant can drop (refilling at the nest)*/
    private double  dosePhero;

    /**total dose which can be dropped by an ant before reseting to the nest*/
    public static final double dosePheroMax = 400d;
    /**food dose that can be carry by the ant*/
    public static final double doseFood = 30d;

    /**Ground where the ant evolve*/
    private Ground ground;
    /**size of the ground*/
    private int groundSize;
    /**distance done by the ant in one iteration*/
    private int step;
    /** direction taken if obstacle encounter */
    private Direction avoidingDirection=null;

    /**random used to determine the next direction of a ant when looking for food*/
    private Random random;

    /**graphical representation of the ant*/
    private Circle antDraw;

    public Ant(){}

    /**create a new ant
     * @param _x initial x coordinate
     * @param _y initial y coordinate
     * @param _ground ant's ground
     */
    public Ant(int _x, int _y, Ground _ground)
    {
        p = new Point(_x, _y);
        ground =_ground;
        groundSize = ground.getSize();
        direction = Direction.randomDirection();
        state = AntState.SEARCHING;
        random = new Random();
        dosePhero = Ant.dosePheroMax;
    }

    /**ant do different actions according to it state*/
    public void evolve()
    {
        Cell[][] grille = ground.getGrid();
        int x = p.x;
        int y = p.y;
        switch(state)
        {
            case SEARCHING:
                direction = getBestDirection(); // go to where there is the most food, pheromones or eventually random
                if(grille[x][y].getFood()<=0) move(); // move if possible
                if(grille[x][y].getFood()>0) // food => change state to TAKING
                    state = AntState.TAKING;
                break;
            case TAKING:
                grille[x][y].removeFood();// take a dose of food
                direction = Direction.getInverse(direction);// come back
                move();
                state = AntState.COMMING_BACK; // next state : COMING BACK
                break;
            case COMMING_BACK:
                if(dosePhero>0) {
                    grille[x][y].addPheromone(dosePhero); // drop pheromone and decrease the quantity it can drop
                    dosePhero-=11;
                }
                direction = getBestDirectionNest(); // search direction to the nest
                move();
                if (grille[x][y].isNest())  // ant in the nest => end of an iteration, reset state
                    state = AntState.RESET;
                break;
            case RESET:
                dosePhero = Ant.dosePheroMax; // la fourmi regenere sa dose de pheromone
                direction = Direction.getInverse(direction); //la fourmi fait demi-tour
                direction = getBestDirection();// recherche la meilleure direction
                move();// et avance
                state = AntState.SEARCHING; // elle passe a l'état suivant
                break;
        }
    }


    /**search the direction where there is the most food, pheromone, or eventually random direction
     * in front of it (maximum 45° turn).*/
    private Direction getBestDirection()
    {
        Direction bestDirection = direction;
        double bestPhero = 0d;
        double bestFood = 0d;
        Direction[] dirAround = Direction.getFrontDirections(direction);
        for(Direction dir:dirAround) // looks for food
        {
            double food = getFoodNextLocation(dir);
            if(food>bestFood) {bestFood=food; bestDirection=dir;}
        }
        if(bestFood==0)
        {
            for(Direction dir:dirAround)  // looks for pheromone
            {
                double phero = getPheroNextLocation(dir);
                if(phero>bestPhero) {bestPhero=phero; bestDirection=dir;}
            }
            bestDirection = getDirection(bestDirection, bestPhero, dirAround);
        }
        return bestDirection;
    }

    /**search the best direction to come back to the nest, else return a random direction in front of the ant
     * @return the direction to the location where there is the most pheromone*/
    private Direction getBestDirectionNest()
    {
        Direction bestDirection = direction;
        double bestNest = 0d;
        Direction[] dirAround = Direction.getFrontDirections(direction);

        for(Direction dir:dirAround)
        {
            double nestSmelling = getNestSmellNextLocation(dir);
            if(nestSmelling>bestNest) {bestNest=nestSmelling; bestDirection=dir;}
        }
        bestDirection = getDirection(bestDirection, bestNest, dirAround);
        return bestDirection;
    }

    /**
     * Decide where the ant can go with all of the data. If the ant can access the cell in the best direction everything
     * goes well, otherwise a new direction is choosen to get around the obstacle.
     * @param bestDirection the best possible direction according to phero/nest odor/food odor
     * @param bestValue the value of the best direction
     * @param dirAround the direction around
     * @return the computed best direction => final choice of direction for the iteration
     */
    private Direction getDirection(Direction bestDirection, double bestValue, Direction[] dirAround) {
        if(bestValue==0) //no best direction
        {
            avoidingDirection = null;
            ArrayList<Direction> listDir = possibleNextDirections(dirAround);
            if(!listDir.isEmpty())
                bestDirection = listDir.get(random.nextInt(listDir.size()));
            else // if possible directly come back
                bestDirection = Direction.getInverse(direction);
        }else if (getNextCell(bestDirection).isObstacle()){ // best direction can't be accessed (obstacle)

            if (avoidingDirection != null){
                if (getNextCell(avoidingDirection).isObstacle()){
                    avoidingDirection = null;
                }else{
                    return avoidingDirection;
                }
            }

            Direction dir1=null, dir2=null;
            List<Direction> possiblesDir1 = Direction.asidesDirections(bestDirection,true);
            Iterator<Direction> iteratorPossiblesDir1 = possiblesDir1.iterator();
            List<Direction> possiblesDir2 = Direction.asidesDirections(bestDirection,false);
            Iterator<Direction> iteratorPossiblesDir2 = possiblesDir2.iterator();
            Cell nextCell1 = null;
            Cell nextCell2 = null;
            while (dir1 == null){
                Direction nextDirection = iteratorPossiblesDir1.next();
                nextCell1 = getNextCell(nextDirection);
                dir1 = nextCell1 != null && nextCell1.isAccessible()?nextDirection:null;
            }
            while (dir2 == null){
                Direction nextDirection = iteratorPossiblesDir2.next();
                nextCell2 = getNextCell(nextDirection);
                dir2 = nextCell2 != null && nextCell2.isAccessible()?nextDirection:null;
            }
            if (avoidingDirection == null) {
                // Choose a random direction to avoir the obstacle according to pheromone dose in both direction
                double phero1 = nextCell1.getPheromone();
                double phero2 = nextCell2.getPheromone();
                double randomValue = Math.random()*(phero1+phero2);
                // Give a chance to explore instead of follow pheromones
                avoidingDirection = new Random().nextInt(10) <=3 ? randomValue > phero1 ? dir1:dir2 : randomValue < phero1 ? dir1:dir2;
            }
            bestDirection = avoidingDirection;
        }else {
          avoidingDirection = null;
        }
        return bestDirection;
    }


    /**return a list of possible directions (no ant, no obstacles, no map border)
     * @param directions directions array where the ant could want to go
     * @return list of possible directions*/
    private ArrayList<Direction> possibleNextDirections(Direction []directions)
    {
        ArrayList<Direction> liste = new ArrayList<>();
        for(Direction dir:directions)
        {
            Cell cell = getNextCell(dir);
            if(cell != null && cell.isAccessible())
                liste.add(dir);
        }
        return liste;
    }


    /**move the ant to the nest location according to the direction*/
    private void move()
    {
        Cell cell = getNextCell(direction);
        if(cell!=null)
        {
            p.x = cell.getX();
            p.y = cell.getY();
            antDraw.setCenterX((p.x+1) * step);
            antDraw.setCenterY((p.y+2) * step);
        }
    }


    /**return the pheromone dose in the direction indicated
     * @param dir direction where we want to check
     * @return the pheromone dose*/
    private double getPheroNextLocation(Direction dir)
    {
        double phero = -1;
        Cell cell = getNextCell(dir);
        if(cell!=null)
            phero = cell.getPheromone();
        return phero;
    }

    /**return the food dose in the direction indicated
     * @param dir direction where we want to check
     * @return the food dose*/
    private double getFoodNextLocation(Direction dir)
    {
        double food = -1;
        Cell cell = getNextCell(dir);
        if(cell != null)food = cell.getFood();
        return food;
    }

    /**return the nest smelling dose in the direction indicated
     * @param dir direction where we want to check
     * @return the pheromone dose*/
    private double getNestSmellNextLocation(Direction dir)
    {
        double nestSmelling = -1;
        Cell cell = getNextCell(dir);
        if(cell!=null)
            nestSmelling = cell.getNestSmelling();
        return nestSmelling;
    }


    /**location in the direction indicated
     * @param dir the direction
     * @return the cell*/
    private Cell getNextCell(Direction dir)
    {
        Cell cell = null;
        Point newPoint = Direction.getNextPoint(p, dir);
        if ((newPoint.x>=0 && newPoint.x < groundSize) && (newPoint.y>=0 && newPoint.y< groundSize))
        {
            Cell[][] grid = ground.getGrid();
            cell = grid[newPoint.x][newPoint.y];
        }
        return cell;
    }

    public Circle getAntDraw() {
        return antDraw;
    }

    public void setAntDraw(Circle antDraw) {
        this.antDraw = antDraw;
    }

    public void setStep(int step) {
        this.step = step;
    }

}
