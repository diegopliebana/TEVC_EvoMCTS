package TEVC_MCTS.roller;

import TEVC_MCTS.features.NavFeatureSource;
import core.game.StateObservation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class VariableFeatureWeightedRoller implements TunableRoller {

    public double MAX_CAP_VALUE = 0.8;
    public double MIN_CAP_VALUE = 0.15;

    public boolean uniform  = false;

    public ArrayList<String> newFeatures;
    public HashMap<String, Integer> featuresMap;

    public double[] params;
    double[] bias;

    int nActions;
    int nFeatures;

    static Random rand;
    NavFeatureSource features;

    public VariableFeatureWeightedRoller(StateObservation state, NavFeatureSource features, Random rnd) {
        // assumes that all states have the same number of
        // actions and will not work for some games
        rand = rnd;
        this.features = features;
        init(state, features);
    }

    @Override
    public void init(StateObservation state, NavFeatureSource features)
    {
        nActions = state.getAvailableActions().size();
        nFeatures = features.getFeatureVectorAsArray(state).length;
        bias = new double[nActions];
        params = new double[nActions*nFeatures];
    }

    @Override
    public NavFeatureSource getFeatures() {
        return features;
    }

    public double[] getFeatureWeights(StateObservation gameState)
    {
        double weights[] = new double[params.length];

        //HashMap<String, Double> allFeatures = features.getFeatureVector(gameState);
        String[] keys = features.getFeatureVectorKeys(gameState); //(String[]) allFeatures.keySet().toArray();

        //Check for a new unknown feature.
        for(String k : keys)
        {
            if(!featuresMap.containsKey(k))
            {
                if(!newFeatures.contains(k))
                    newFeatures.add(k); //don't add it more than once.
            }else
            {
                int pos = featuresMap.get(k);
                for(int actIdx = 0; actIdx < nActions; actIdx++)
                {
                    int weightPos = pos*nActions + actIdx;
                    if(weightPos < weights.length)  //this shouldn't be necessary...
                        weights[weightPos] = params[weightPos];
                }

            }
        }

        return weights;
    }

    public int roll(StateObservation gameState) {
        if (uniform) return rand.nextInt(nActions);
        double[] featureWeightVector = features.getFeatureVectorAsArray(gameState);

        int highestBiasIdx = -1; //identifies which is the highest bias value
        double highestBiasValue = -Double.MAX_VALUE; //identifies which is the highest bias value

        int ix = 0; // used to step over params
        double tot = 0;
        //System.out.format("\n");
        for (int i=0; i<nActions; i++) {
            bias[i] = 0;

            for (int j=0; j<nFeatures; j++) {
                //System.out.format(" %4.2f ", featureWeightVector[j]);
                bias[i] += params[ix] * featureWeightVector[j];
                ix++;
            }

            // now replace with e^a[i]
            bias[i] = Math.exp(bias[i]);

            if(bias[i] == Double.POSITIVE_INFINITY)
                bias[i] = Double.MAX_VALUE;

            if(bias[i] > highestBiasValue)
            {
                highestBiasIdx = i;
                highestBiasValue = bias[i];
            }

            tot += bias[i];
        }

        //CAP the probabilities to emulate simulation balancing.
        //maxcap(highestBiasIdx, tot, MAX_CAP_VALUE);
        mincap(tot, MIN_CAP_VALUE);

        // now track relative cumulative probability
        double x = rand.nextDouble();

        // an accumulator
        double acc = 0;
        int action = 0;
        for ( ; action<nActions; action++) {
            acc += bias[action] / tot;
            if (x < acc) return action;
        }
        if (action == nActions) {
            action = rand.nextInt(nActions);
        }
        return action;
    }

    private void mincap(double tot, double cap)
    {
        boolean[] toUpdate = new boolean[bias.length];
        double probAcum = 0;
        int numToUpdate = 0;

        for (int i=0; i<nActions; i++) {
            double bp = bias[i]/tot;
            if (bp < cap)
            {
                probAcum += (cap - bp);
                bias[i] = cap * tot; //cap the ones with prob < cap
            }else{
                toUpdate[i] = true;
                numToUpdate++;
            }
        }

        if(numToUpdate == 0)
            return;

        double subtractChunk = probAcum / numToUpdate;
        for (int i=0; i<nActions; i++) {
            if(toUpdate[i])
            {
                bias[i] -= (subtractChunk * tot);
            }
        }


        /*double b = 0.0;
        for (int i=0; i<nActions; i++) {
            b += bias[i];
        }
        System.out.println(b/tot);*/

    }

    private void maxcap(int highestBiasIdx, double tot, double cap)
    {
        double highestProb = bias[highestBiasIdx]/tot;
        if(highestProb <= cap)
            return; //Only cap the probabilities if is greater than cap.

        double remainder = highestProb - cap;           //"extra" probability.
        double remEach = remainder / (bias.length-1);   //extra piece for each other action.

        //double b = 0.0;

        for (int i=0; i<nActions; i++) {
            if (i == highestBiasIdx)
            {
                bias[i] = cap * tot; //cap the one with the highest probability over the cap.
            }else{
                bias[i] += (remEach * tot); //Add pieces to the other action probabilities.
            }
            //b += bias[i];
        }

        //System.out.println(b/tot);
    }

    /*public int roll(StateObservation gameState) {
        if (uniform) return rand.nextInt(nActions);
        double[] featureWeightVector = features.getFeatureVectorAsArray(gameState);//getFeatureWeights(gameState);

        int ix = 0; // used to step over params
        double tot = 0;
        for (int i=0; i<nActions; i++) {
            bias[i] = 0;

            for (int j=0; j<nFeatures; j++) {
                bias[i] += params[ix] * featureWeightVector[j];
                ix++;
            }

            // now replace with e^a[i]
            bias[i] = Math.exp(bias[i]);
            tot += bias[i];
        }
        // now track relative cumulative probability
        double x = rand.nextDouble();

        // an accumulator
        double acc = 0;
        int action = 0;
        for ( ; action<nActions; action++) {
            acc += bias[action] / tot;
            if (x < acc) return action;
        }
        if (action == nActions) {
            action = rand.nextInt(nActions);
        }
        return action;
    }*/


    /*
    def softmax2(self, w):
        w = np.array(w)
        maxes = np.amax(w, axis=0)
        #maxes = maxes.reshape(maxes.shape[0], 0)
        e = np.exp(w - maxes)
        dist = e / np.sum(e, axis=0)

        return dist
    */

    public double[] getBiases (StateObservation gameState) {
        double[] biases = new double[bias.length];
        // uniform = true;
        // if (uniform || true) return biases;
        if (uniform) return biases;
        //double[] featureVector = features.getFeatureVectorAsArray(gameState);
        double[] featureWeightVector = getFeatureWeights(gameState);
        int ix = 0; // used to step over params
        double tot = 0;
        for (int i=0; i<nActions; i++) {
            bias[i] = 0;

            for (int j=0; j<nFeatures; j++) {
                bias[i] += params[ix] * featureWeightVector[j];
                ix++;
            }
            // now replace with e^a[i]
            bias[i] = Math.exp(bias[i]);
            tot += bias[i];
        }
        for (int i=0; i<biases.length; i++) {
            biases[i] = bias[i] / tot;
        }
        return biases;
    }

    @Override
    public int nDim() {
        return nActions * nFeatures;
    }

    @Override
    public void setParams(HashMap<String, Integer> featuresMap, double[] w) {

        this.featuresMap = featuresMap;
        this.newFeatures = new ArrayList<String>();

        nFeatures = featuresMap.size();
        params = new double[nDim()];
        //System.out.format("\n");
        for (int i=0; i<nDim(); i++) {
            params[i] = w[i];
            //System.out.format(" %6.4f ",params[i]);
        }
    }

    @Override
    public String[] featureNames(StateObservation state) {

        return features.getFeatureVectorKeys(state);
    }


    @Override
    public boolean newFeaturesFound() {
        return this.newFeatures.size() > 0;
    }

    @Override
    public ArrayList<String> getNewFeatures() {
        return this.newFeatures;
    }

}
