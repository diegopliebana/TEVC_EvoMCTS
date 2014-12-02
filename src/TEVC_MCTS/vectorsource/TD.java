package TEVC_MCTS.vectorsource;

import TEVC_MCTS.features.NavFeatureSource;
import TEVC_MCTS.utils.Memory;
import core.game.StateObservation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;

/**
 * Created by Diego on 29/10/14.
 */
public class TD extends FitVectorSource{

    private double[][] Q_values;
    private double learning_rate = 0.1;
    //private double[] feature_max;
    //private double[] feature_min;

    private double reward_min = Double.POSITIVE_INFINITY;
    private double reward_max = Double.NEGATIVE_INFINITY;

    public TD(String[] features, int order,
              int nActions, Memory memory, Random rnd) {

        /*** LEAVE THIS AS IT IS ***/
        namesGenomeMapping = new HashMap<String, Integer>();
        this.nFeatures = 0;
        this.nActions = nActions;
        this.order = order;
        this.rand = rnd;

        for(String f : features)
        {
            addFeature(f, false);
        }
        bestYet = new double[ nDim() ];

        /******************************/
        //ADD STUFF FROM THIS POINT ON.


        //System.out.println("=============");
        //System.out.println(nFeatures);

        //System.out.println(nActions);
        //System.exit(1);
        // Initialise Q_value arrays
        Q_values = new double[nFeatures][nActions];

        //System.out.println(rnd);
        for (int i = 0; i < nFeatures; i++) {
            for (int j = 0; j < nActions; j++) {
                Q_values[i][j] = (rnd.nextDouble() - 0.5)*0.0001;
            }
        }




        //This is quite optional
        for (int i=0; i<  bestYet.length; i++)
            bestYet[i] = nextRandom();
    }




    @Override
    public double[] getNext(String[] features) {

        proposed = new double[nDim()];

        int i=0;
        int j =0;
        for(String feature : features)
        {
           //
            //get the position in bestYet where its gene is located.
            //int featurePosition = namesGenomeMapping.get(feature);

            for(int actIdx = 0; actIdx < nActions; actIdx++)
            {
                //Position of this weight in the array of weights.
                //int weightPos = featurePosition*nActions + actIdx;


                /** MODIFY THIS TO GIVE A VALUE TO THIS WEIGHT **/
//                System.out.println(weightPos);
//                System.out.println(i);
//                System.out.println(actIdx);
//
//                System.out.println("proposed.length" + proposed.length);
//                System.out.println("Q_values.length" + Q_values.length);
//                System.out.println("Q_values[0].length" + Q_values[0].length);
//
//                System.out.println("==========");
                proposed[i] = Q_values[j][actIdx];



                i++;
            }
            j++;
        }

        return proposed;
    }

    @Override
    public boolean returnFitness(NavFeatureSource features, ArrayList<StateObservation> states, ArrayList<Integer> actions, double fitnessVal) {
        //fitnessVal is the fitness value for this weight vector.
        //States and actions are the sequence of states and actions found and used during the rollout.


       if(fitnessVal == 0.0){
            return false;
        }
        if(fitnessVal!=Double.NEGATIVE_INFINITY)   {

        this.reward_max = Math.max(fitnessVal, this.reward_max);
        this.reward_min = Math.min(fitnessVal, this.reward_min);

        double normalised_reward = (fitnessVal - this.reward_min)/(reward_max- reward_min) -0.5 ;
        //normalised_reward +=2;


        normalised_reward   *= 2;
        if(this.reward_max == this.reward_min) {
            normalised_reward = 0;
        }


        //normalised_reward = fitnessVal;
        //normalised_reward = fitnessVal/1000.0;
        //System.out.println(fitnessVal);
        //if(normalised_reward ==.0

        int idx = 0;
        for(StateObservation stObs : states)
        {
            //This is how you get the features of one of the states:
            double[] stateFeatures = features.getFeatureVectorAsArray(stObs);
            int action = actions.get(idx);

            //System.out.print("action: " + action + ", weights: " );
            //for(double w : stateFeatures) System.out.print(w + ", ");
            //System.out.println("fitness: " + fitnessVal);

            double prediction = 0;

            for (int i = 0; i < stateFeatures.length; i++) {
                prediction += stateFeatures[i]*Q_values[i][action];
            }


            double error = prediction - normalised_reward;

//
            StringBuffer buffer = new StringBuffer();
            buffer.append("Error = ");
            buffer.append(error);
            buffer.append(", ");
            buffer.append("Prediction = ");
            buffer.append(prediction);
            buffer.append(", ");
            buffer.append("reward = ");
            buffer.append(normalised_reward);
            buffer.append(", ");
            buffer.append("action = ");
            buffer.append(action);
            buffer.append(", ");

            buffer.append("statefeatures = ");
            buffer.append(Arrays.toString(stateFeatures));
            buffer.append(", ");

            buffer.append("weights = ");

            for (int i = 0; i < stateFeatures.length; i++) {
               buffer.append(Q_values[i][action]);
               buffer.append(", ");

            }
            //System.out.println(buffer);

            for (int i = 0; i < stateFeatures.length; i++) {
                //self.theta = self.theta - self.alpha*error*X[i]
                //System.out.println("Before, " + Q_values[i][action]);
                Q_values[i][action] = Q_values[i][action] - learning_rate*error*stateFeatures[i];
                //System.out.println("After, " + Q_values[i][action]);


            }

            //System.out.println("Prediction, " + prediction);

            idx++;
        }

        //System.out.println(fitnessVal + ", "  + normalised_reward );
        }

        return false; //no worries about the return, it's used for debug
    }
}
