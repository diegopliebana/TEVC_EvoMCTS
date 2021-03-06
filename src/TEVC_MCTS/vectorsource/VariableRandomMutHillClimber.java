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
 * (1+1)-ES
 */
public class VariableRandomMutHillClimber extends FitVectorSource
{
    double noiseDev = 0.1;
    static double noiseFac = 1.5;
    static double oneFifthRule = Math.pow(noiseFac,0.25);

    public double weightsRange[] = new double[]{-10.0,10.0};
    public double stdDevRange[] = new double[]{0.1,10.0};

    public int nEqualFitnessThreshold = 1000;
    public int nEqualFitness = 0;
    public double lastFitness = -Double.MAX_VALUE;

    //Following 1-5th rule: https://hal.inria.fr/inria-00430515/file/wk2037-auger.pdf
    //No restarts nor stopping criteria implemented: short evolutions, linear approximation (well behaved),
    //harder to hit local minimum, and need to restart


    Memory memory;

    public VariableRandomMutHillClimber(String[] features, int order,
                                        int nActions, Memory memory, Random rnd) {
        this.memory = memory;
        this.nFeatures = 0;
        this.nActions = nActions;
        this.order = order;
        this.rand = rnd;
        namesGenomeMapping = new HashMap<String, Integer>();
        bestScore = Double.NEGATIVE_INFINITY * order;

        for(String f : features)
        {
            addFeature(f, false);
        }

        bestYet = new double[ nDim() ];
        for (int i=0; i<  bestYet.length; i++)
            bestYet[i] = nextRandom();
    }

    protected void addFeature(String featureName, boolean updateBestInd)
    {
        super.addFeature(featureName,updateBestInd);

        if(Config.USE_MEMORY)
            memory.addInfoType(featureName);
    }


    @Override
    public boolean returnFitness(NavFeatureSource features, ArrayList<StateObservation> states,
                              ArrayList<Integer> actions,
                              double fitness) {
        boolean success = false;
        nEvals++;
        if (order * fitness > bestScore * order) {
            bestYet = proposed;
            bestScore = fitness;
            // success so increase the noiseDev
            noiseDev *= noiseFac;

            //System.out.println("New best fitness: " + fitness + " " + noiseDev);
            success = true;
        //} else {
        }else if (order * fitness < bestScore * order) {
            // failure so decrease noiseDev
            noiseDev /= oneFifthRule;

            //System.out.println("No new best fitness: " + fitness + ", best still: " + bestScore + " " + noiseDev);
        }

        if(lastFitness == fitness)
        {
            nEqualFitness++;
        }else nEqualFitness = 0;

        lastFitness = fitness;

        if(nEqualFitness > nEqualFitnessThreshold)
        {
            noiseDev *= noiseFac;
            //System.out.println("Flat fitness landscape: " + lastFitness + " x" + nEqualFitness + " new noiseDev: " + noiseDev);
            nEqualFitness = 0;
        }

        noiseDev = Math.max(stdDevRange[0], Math.min(stdDevRange[1], noiseDev));

        return success;
    }

    @Override
    public double[] getNext(String[] features) {

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
                proposed[i] = bestYet[weightPos] + rand.nextGaussian() * noiseDev;
                proposed[i] = Math.max(weightsRange[0], Math.min(weightsRange[1], proposed[i]));


                i++;
            }
        }
        return proposed;
    }



}
