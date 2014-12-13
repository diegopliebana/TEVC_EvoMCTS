package TEVC_MCTS.vectorsource;

import TEVC_MCTS.Config;
import TEVC_MCTS.features.NavFeatureSource;
import TEVC_MCTS.utils.Memory;
import core.game.StateObservation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

/**
 * Created by Diego on 20/03/14.
 * (u+1)-ES
 * The special case (µ + 1) is also referred to as steady-state ES
 * Rules of sigma followed here:
 * http://www.uni-oldenburg.de/fileadmin/user_upload/informatik/ag/ci/download/Evolutionary_self-adaptation_a_survey_of_operators_and_strategy_parameters.pdf
 */
public class ES extends FitVectorSource
{

    public int popSize = 10;

    double initNoiseDev = 0.1;
    static double noiseFac = 1.5; //1.02

    double lastNoiseDev;
    int lastInit;

    double[][] pop;
    double[] fitness;
    double noiseDev[];
    double worstScore;

    Memory memory;


    boolean RANGE_WEIGHTS_SIGMA = true;
    public double weightsRange[] = new double[]{-10.0,10.0};
    public double stdDevRange[] = new double[]{0.1,10.0};

    public int nEqualFitnessThreshold = 500;
    public int nEqualFitness = 0;
    public double lastFitness = -Double.MAX_VALUE;


    public ES(String[] features, int order,
        int nActions, Memory memory, Random rnd) {
            this.memory = memory;
            this.nFeatures = 0;
            this.nActions = nActions;
            this.order = order;
            this.rand = rnd;
        namesGenomeMapping = new HashMap<String, Integer>();
        bestScore = Double.NEGATIVE_INFINITY * order;
        lastInit = -1;

        for(String f : features)
        {
            addFeature(f, false);
        }

        bestYet = new double[ nDim() ];
        noiseDev = new double[bestYet.length];
        fitness = new double[popSize];
        pop = new double[popSize][bestYet.length];

        for (int i=0; i< pop.length; i++)
        {
            for (int j=0; j<bestYet.length; j++)
                pop[i][j] = nextRandom();
        }

        for (int i=0; i<  bestYet.length; i++) {
            bestYet[i] = nextRandom();
            noiseDev[i] = initNoiseDev;
        }
    }

    @Override
    public double[] getNext(String[] features) {

        if(lastInit+1 < popSize)
        {
            lastInit++;
            proposed = pop[lastInit];
            return proposed;
        }

        //Else, all initial population has been evaluated, need to recombine and create more

        int parent1 = rand.nextInt(popSize);
        int parent2 = rand.nextInt(popSize);
        while(parent1 == parent2)
            parent2 = rand.nextInt(popSize);

        proposed = new double[nDim()];
        int i=0;

        for(String feature : features)
        {
            if(!namesGenomeMapping.containsKey(feature))
            {
                //add the feature if we have never seen this one.
                addFeature(feature, true);

                //We need to create a new proposed array.
                double[] newProposed = new double[nDim()];
                System.arraycopy(proposed,0,newProposed,0,proposed.length);
                proposed = newProposed;
            }

            //get the position in bestYet where its gene is located.
            int featurePosition = namesGenomeMapping.get(feature);

            for(int actIdx = 0; actIdx < nActions; actIdx++)
            {
                int weightPos = featurePosition*nActions + actIdx;

                //calculate a new one, and place it in the same position as its feature name.
                if(rand.nextDouble() >= 0.5)
                {
                    //lastNoiseDev =  noiseDev[parent1];
                    proposed[i] = pop[parent1][weightPos] + rand.nextGaussian() * noiseDev[weightPos];
                }else{
                    //lastNoiseDev =  noiseDev[parent2];
                    proposed[i] = pop[parent2][weightPos] + rand.nextGaussian() * noiseDev[weightPos];
                }

                if(RANGE_WEIGHTS_SIGMA)
                    proposed[i] = Math.max(weightsRange[0], Math.min(weightsRange[1], proposed[i]));

                i++;
            }
        }
        return proposed;
    }


    @Override
    public boolean returnFitness(NavFeatureSource features, ArrayList<StateObservation> states,
                              ArrayList<Integer> actions,
                              double fitness) {
        nEvals++;

        int improve = 0;
        boolean newWorst = false;
        int indIdx = lastInit;

//        System.out.println("fitness: " + fitness);

        if (order * fitness > bestScore * order) {
            //System.out.println("####### ");
            //for(int i = 0; i < proposed.length; ++i) System.out.print(proposed[i] + ",");
            //System.out.print("fitness: " + fitness);
            //System.out.println(" [* New best fitness]");
            bestYet = proposed;
            bestScore = fitness;
            improve = 1;
        }

        if (order * fitness < worstScore * order)
        {
            newWorst = true;
            worstScore = fitness;
            improve = -1;
        }

        if(nEvals-1 < popSize)
            this.fitness[lastInit] = fitness;
        else
        {
            if(!newWorst)
            {
                //We need to replace the worst individual with the proposed one.
                double wFit = Double.POSITIVE_INFINITY * order;
                int wIdx = -1;

                for (int i=0; i< this.fitness.length; i++)
                {
                    if(order* this.fitness[i] <= wFit*order)
                    {
                        wIdx = i;
                        wFit = this.fitness[i];
                    }
                }

                pop[wIdx] = proposed;
                this.fitness[wIdx] = fitness;
                worstScore = wFit;
                //noiseDev[wIdx] = lastNoiseDev;
                indIdx = wIdx;
            }
        }

        if(improve == 1)
            for (int i=0; i<  bestYet.length; i++)
                noiseDev[i] *= noiseFac;   // success so increase the noiseDev

        if(improve == -1)
            for (int i=0; i<  bestYet.length; i++)
                noiseDev[i] /= noiseFac;   // failure so decrease noiseDev


        if(lastFitness == fitness)
        {
            nEqualFitness++;
        }else nEqualFitness = 0;

        lastFitness = fitness;

        if(nEqualFitness > nEqualFitnessThreshold)
        {
            for (int i=0; i<  bestYet.length; i++)
                noiseDev[i] *= noiseFac;
            //System.out.println("Flat fitness landscape: " + lastFitness + " x" + nEqualFitness + " new noiseDev: ");
            //     for (int i=0; i<  bestYet.length; i++)
            //         System.out.print(noiseDev[i] + ",");
            //System.out.println();
            nEqualFitness = 0;
        }

        if(RANGE_WEIGHTS_SIGMA)
            for (int i=0; i<  bestYet.length; i++)
                noiseDev[i] = Math.max(stdDevRange[0], Math.min(stdDevRange[1], noiseDev[i]));


        return (improve == 1);
    }


    protected void addFeature(String featureName, boolean updateIndividuals)
    {
        super.addFeature(featureName,updateIndividuals);

        if(updateIndividuals)
            updatePopulation();

        if(Config.USE_MEMORY)
            memory.addInfoType(featureName);
    }

    private void updatePopulation()
    {
        double [][]population = new double[popSize][nDim()];
        for (int i=0; i< pop.length; i++)
        {
            //copy the old best one
            System.arraycopy(pop[i], 0, population[i], 0, pop[i].length );

            //Add new weights for the new feature, one per action:
            for(int actIdx = pop[i].length; actIdx < population[i].length; actIdx++)
            {
                population[i][actIdx] = nextRandom();     //init randomly.
            }
        }

        pop = population;
    }



}
