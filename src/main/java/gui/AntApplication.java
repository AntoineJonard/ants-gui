package gui;

import model.Cell;
import model.Ant;
import model.Ground;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.effect.BoxBlur;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;
import model.FoodManager;

/**
 * interesting : <a href="https://twitter.com/fasc1nate/status/1590870089947303937">ant swarm without nest</a>
 *
 * Graphical JFX interface to represent an ant colony looking for food
 *
 * @author Antoine Jonard
 */
public class AntApplication extends Application {
    /** objects representing the ground where the ants will evolve */
    private Ground ground;
    /** number of ants */
    int antsNumber = 40;
    /** allow access to food evolution during simulation */
    private FoodManager foodManager;
    /** simulation speed */
    double tempo = 20;
    /** size of the ground */
    private int size;
    /** pixel size of a cell */
    private final int espace = 10;
    private  static Rectangle [][] environment;
    public static Circle[]fourmis;

    /** main (entry point of the Application) */
    public static void main(String[] args) {
        launch(args);
    }

    /**
     * Launch the graphical interface
     * @param primaryStage base scene of the application
     */
    @Override
    public void start(Stage primaryStage) {
        int tailleTerrain = 70;
        foodManager = new FoodManager();
        fourmis = new Circle[antsNumber];
        ground = new Ground(tailleTerrain, antsNumber, foodManager);
        size = ground.getSize();
        sceneConstructionForAnts( primaryStage);

    }



    /** Initialize the different graphical layers, and then draw all the object on the scene */
    void sceneConstructionForAnts(Stage primaryStage)
    {
        Group root = new Group();
        Scene scene = new Scene(root, 2*espace + size *espace, 2*espace + size *espace, Color.BLACK);
        primaryStage.setTitle("Ants searching food for the nest");
        primaryStage.setScene(scene);

        // Display the stage
        primaryStage.show();

        startGraphicalSimulation(root);
    }

    private void startGraphicalSimulation(Group root) {
        AntApplication.environment = new Rectangle[size][size];
        drawEnvironment(root);

        //launch the timer to make every element evolve at each tempo tick
        Timeline littleCycle = new Timeline(new KeyFrame(Duration.millis(tempo), event -> {
            if (foodManager.stillFood()){
                ground.animGrid();
                updateGround();
            }
        }));
        littleCycle.setCycleCount(Timeline.INDEFINITE);
        littleCycle.play();

    }


    /**
     * creation of the cells with colors according to their nature (obstacle, nest, ...)
     */
    void drawEnvironment(Group root)
    {
        Cell[][] grille = ground.getGrid();
        // base environment is green
        for(int i = 0; i< size; i++)
            for(int j = 0; j< size; j++)
            {
                AntApplication.environment[i][j] = new Rectangle((i+1)*(espace), (j+1)*(espace), espace, espace);
                AntApplication.environment[i][j].setFill(Color.DARKGREEN);
                root.getChildren().add(AntApplication.environment[i][j]);
            }
        for(int i = 0; i< size; i++)
            for(int j = 0; j< size; j++)
            {
                Cell cell = grille[i][j];
                if (cell.getFood()>0) // display food zone
                {
                    Color colFood = Color.DARKGOLDENROD;
                    double ratio = cell.getFood() / 50d;
                    colFood = colFood.interpolate(Color.BISQUE, ratio);
                    AntApplication.environment[i][j] = new Rectangle((i+1)*(espace), (j+1)*(espace), espace, espace);
                    AntApplication.environment[i][j].setFill(colFood);
                    // draw the food in top of environment
                    root.getChildren().add(AntApplication.environment[i][j]);
                }
                else
                if (cell.isNest()) // display the nest zone
                {
                    AntApplication.environment[i][j] = new Rectangle((i+1)*(espace), (j+1)*(espace), espace, espace);
                    AntApplication.environment[i][j].setFill(Color.BROWN);
                    // the nest replace the environment
                    root.getChildren().remove(AntApplication.environment[i][j] );
                    root.getChildren().add(AntApplication.environment[i][j]);
                }
                if (cell.isObstacle())
                {
                    AntApplication.environment[i][j] = new Rectangle((i+1)*(espace), (j+1)*(espace), espace, espace);
                    AntApplication.environment[i][j].setFill(Color.BLACK);
                    // replace the environment by obstacles
                    root.getChildren().remove(AntApplication.environment[i][j] );
                    root.getChildren().add(AntApplication.environment[i][j]);
                }
                if (cell.getNestSmelling() <= 0){
                    AntApplication.environment[i][j].setFill(Color.RED);
                }
            }

        // ants creation
        for(Ant f : ground.getAnts())
        {
            f.setAntDraw(new Circle(((size +3)*espace)/2 , ((size +3)*espace)/2, espace/2, Color.TOMATO));
            f.setStep(espace);
            root.getChildren().add(f.getAntDraw());
        }

        // blur effect for the style
        root.setEffect(new BoxBlur(2, 2, 5));
    }



    /** Modify the cell color according to the food and pheromone dose */
    private void updateGround() {
        Cell[][] grille = ground.getGrid();

        double pheroMaxAtm=0;
        for (int i = 0; i < size; i++)
            for (int j = 0; j < size; j++) {
                Cell cell = grille[i][j];
                if (cell.getPheromone() > pheroMaxAtm)
                    pheroMaxAtm = cell.getPheromone();
            }

        for(int i = 0; i< size; i++)
            for(int j = 0; j< size; j++)
            {
                Cell cell = grille[i][j];
                if (cell.hasJustChanged && cell.getFood()>0 && cell.isAccessible())
                {
                    Color colNouriture = Color.DARKGOLDENROD;
                    double ratio = cell.getFood() /(double) FoodManager.foodDose;
                    colNouriture = colNouriture.interpolate(Color.BISQUE, ratio);
                    AntApplication.environment[i][j].setFill(colNouriture);
                }
                if (cell.hasJustChanged && cell.isEmptyNow()  && cell.isAccessible())
                {
                    AntApplication.environment[i][j].setFill(Color.DARKGREEN );
                    cell.setEmptyNow(false);
                }
                if (cell.hasJustChanged && cell.getPheromone()>0 && cell.getFood()==0  && !cell.isNest() && cell.isAccessible()) // s'il y a une trace de phéromone
                {
                    double ratio = (Math.log(cell.getPheromone()+1) / Math.log(pheroMaxAtm+1));
                    int g = (int)(ratio * 255*10) + 150;
                    if(g>255) g=255;
                    float transparence = (float)ratio + 0.4f; if (transparence>1f) transparence = 1f;
                    Color c1 = Color.DARKGREEN;
                    Color c = c1.interpolate(Color.rgb(g,g,g), transparence);
                    AntApplication.environment[i][j].setFill(c);
                }
                if (cell.hasJustChanged && cell.getPheromone()==0 && cell.getFood()==0 && cell.isAccessible())
                {
                    AntApplication.environment[i][j].setFill(Color.DARKGREEN);
                }

                if (cell.getNestSmelling() <= 0){
                    AntApplication.environment[i][j].setFill(Color.RED);
                }
            }
    }
}