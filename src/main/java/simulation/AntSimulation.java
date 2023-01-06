package simulation;

import com.github.sh0nk.matplotlib4j.Plot;
import com.github.sh0nk.matplotlib4j.PythonExecutionException;
import model.Ant;
import model.Cell;
import model.FoodManager;
import model.Ground;

import java.io.IOException;
import java.util.ArrayList;

public class AntSimulation {

    private final static int NB_TEST = 30;
    private final static int NB_SAMPLE = 40;

    public static void main(String[] args) throws PythonExecutionException, IOException {

        /*
         * Testing ant's pheromones doses impact on time and number of turns
         */

        ArrayList<Double> times = new ArrayList<>();
        ArrayList<Double> nbTurns = new ArrayList<>();
        ArrayList<Double> pheroRatios = new ArrayList<>();
        ArrayList<Double> nbIterationAvg = new ArrayList<>();
        resetVariables();

        /*
        for (int i = 0; i < NB_TEST; i++){

            nbIterationAvg.add(simulateSamples(times, nbTurns, i));

            pheroRatios.add(Ant.dosePheroDropped/Ant.dosePheroMax);

            Ant.dosePheroMax += 40;
            Ant.dosePheroDropped += 2;
        }


        plotStats(
                pheroRatios,
                nbTurns,
                "Pheromones dropped/max ratio",
                "Number of ant turns",
                "Pheromones doses impact on death circle"
        );

        plotStats(
                pheroRatios,
                times,
                "Pheromones dropped/max ratio",
                "time to get all the food to nest (ms)",
                "Pheromone dose impact on finishing time"
        );

        plotStats(
                pheroRatios,
                nbIterationAvg,
                "Pheromones dropped/max ratio",
                "Number of iteration to get all the food",
                "Pheromone dose impact on algorithm speed"
        );

        /*
         * Testing pheromones' evaporation impact on time and number of turns
         */
        /*
        times.clear();
        nbTurns.clear();
        nbIterationAvg.clear();
        ArrayList<Double> evaporation = new ArrayList<>();
        resetVariables();

        for (int i = 0; i < NB_TEST; i++){

            nbIterationAvg.add(simulateSamples(times, nbTurns, i));
            evaporation.add(Cell.evaporation);

            Cell.evaporation+=0.0025;
        }

        plotStats(
                evaporation,
                nbTurns,
                "Evaporation (%)",
                "ants number of turns",
                "Evaporation impact on death circle"
        );

        plotStats(
                evaporation,
                times,
                "Evaporation (%)",
                "time to get all the food to nest (ms)",
                "Evaporation impact finishing time"
        );

        plotStats(
                evaporation,
                nbIterationAvg,
                "Evaporation (%)",
                "Number of iteration to get all the food",
                "Evaporation impact on algorithm speed"
        );
         */

        /*
         * Testing pheromones' diffusion impact on time and number of turns
         */
        times.clear();
        nbTurns.clear();
        nbIterationAvg.clear();
        ArrayList<Double> diffusion = new ArrayList<>();
        resetVariables();

        for (int i = 0; i < NB_TEST && Cell.diffusion <= 0.004; i++){

            nbIterationAvg.add(simulateSamples(times, nbTurns, i));
            diffusion.add(Cell.diffusion);

            Cell.diffusion+=0.00025;
        }

        plotStats(
                diffusion,
                nbTurns,
                "Diffusion (%)",
                "ants number of turns",
                "Diffusion impact on death circle"
        );

        plotStats(
                diffusion,
                times,
                "Diffusion (%)",
                "time to get all the food to nest (ms)",
                "Diffusion impact on finishing time"
        );

        plotStats(
                diffusion,
                nbIterationAvg,
                "Diffusion (%)",
                "Number of iteration to get all the food",
                "Diffusion impact on algorithm speed"
        );


        /*
         * Testing pheromones' evaporation and diffusion impact on time and number of turns
         */
        times.clear();
        nbTurns.clear();
        nbIterationAvg.clear();
        ArrayList<Double> evDiffScale = new ArrayList<>();
        resetVariables();

        for (int i = 0; i < NB_TEST; i++){

            nbIterationAvg.add(simulateSamples(times, nbTurns, i));
            evDiffScale.add((double)i);

            Cell.diffusion+=0.00025;
            Cell.evaporation+=0.0025;
        }

        plotStats(
                evDiffScale,
                nbTurns,
                "Diffusion/Evaporation incrementation",
                "ants number of turns",
                "Diffusion/Evaporation pair impact on death circle"
        );

        plotStats(
                evDiffScale,
                times,
                "Diffusion/Evaporation incrementation",
                "time to get all the food to nest",
                "Diffusion/Evaporation pair impact on finishing time"
        );

        plotStats(
                evDiffScale,
                nbIterationAvg,
                "Diffusion/Evaporation incrementation",
                "Number of iteration to get all the food",
                "Diffusion/Evaporation impact on algorithm speed"
        );

    }

    private static void plotStats(ArrayList<Double> xlist,ArrayList<Double> ylist, String xlabel,String ylabel,String title) {
        Plot plt = Plot.create();
        plt.plot()
                .add(xlist, ylist)
                .linestyle("--");
        plt.xlabel(xlabel);
        plt.ylabel(ylabel);
        plt.text(0.5, 0.2, "text");
        plt.title(title);
        //plt.legend();
        try {
            plt.show();
        } catch (IOException | PythonExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    private static double simulateSamples(ArrayList<Double> stat1list, ArrayList<Double> stat2list, int testindex) {
        System.out.println("Starting test "+(testindex+1));

        double nbIteration = 0;

        double timeAvg = 0;
        double nbTurnsAvg = 0;

        for (int sampleindex = 0; sampleindex < AntSimulation.NB_SAMPLE; sampleindex++){

            System.out.println("Starting sample "+(sampleindex+1));

            FoodManager foodManager = new FoodManager();
            int antsNumber = 40;
            int tailleTerrain = 70;
            Ground ground = new Ground(tailleTerrain, antsNumber, foodManager);

            Long startTime = System.currentTimeMillis();

            while (foodManager.stillFood()){
                ground.animGrid();
                nbIteration++;
            }

            Long endTime = System.currentTimeMillis();

            timeAvg += endTime-startTime;
            nbTurnsAvg += ground.getNbAntsTurns();
        }

        timeAvg /= AntSimulation.NB_SAMPLE;
        stat1list.add(timeAvg);
        nbTurnsAvg /= AntSimulation.NB_SAMPLE;
        stat2list.add(nbTurnsAvg);

        return nbIteration/AntSimulation.NB_SAMPLE;
    }

    private static void resetVariables() {
        Ant.dosePheroMax = 400;
        Ant.dosePheroDropped = 6;
        Cell.diffusion = 0.0025;
        Cell.evaporation = 0.035;
    }

}
