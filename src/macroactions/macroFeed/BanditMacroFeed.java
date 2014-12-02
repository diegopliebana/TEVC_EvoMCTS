package macroactions.macroFeed;

import tools.Utils;

import java.util.Random;

/**
 * Created by dperez on 24/11/2014.
 */
public class BanditMacroFeed implements IMacroFeed
{
    public double K = Math.sqrt(2);
    public double epsilon = 1e-6;
    public Random mRnd;

    public int numBandits;
    public int macroLengths[];
    double[] acumValue;
    int[] n;
    int bigN;
    int lastSelectedIdx;

    protected static double[] bounds = new double[]{Double.MAX_VALUE, -Double.MAX_VALUE};

    public BanditMacroFeed(int[] lengths)
    {
        numBandits = lengths.length;
        mRnd = new Random();
        macroLengths = new int[numBandits];
        System.arraycopy(lengths, 0, macroLengths, 0, numBandits);
        bigN = 0;
        acumValue = new double[numBandits];
        n = new int[numBandits];
        lastSelectedIdx = -1;
    }

    @Override
    public int getNextLength() {
        double bestValue = -Double.MAX_VALUE;
        lastSelectedIdx = -1;
        for(int i = 0; i < numBandits; ++i)
        {

            double q = acumValue[i] / (n[i] + this.epsilon);
            q =  Utils.normalise(q, bounds[0], bounds[1]);

            double uctValue = q +
                    K * Math.sqrt(Math.log(bigN + 1) / (n[i] + this.epsilon));

            //break ties randomly.
            uctValue = Utils.noise(uctValue, this.epsilon, this.mRnd.nextDouble());
            if (uctValue > bestValue) {
                lastSelectedIdx = i;
                bestValue = uctValue;
            }
        }

        if(lastSelectedIdx == -1)
        {
            //Give it random
            lastSelectedIdx = mRnd.nextInt(numBandits);
        }

        return macroLengths[lastSelectedIdx];

    }

    @Override
    public void setReward(double reward) {
        acumValue[lastSelectedIdx] += reward;
        bigN++;
        n[lastSelectedIdx]++;

        if(reward < bounds[0])
            bounds[0] = reward;
        if(reward > bounds[1])
            bounds[1] = reward;

        //System.out.println("Last Tree with ML=" + macroLengths[lastSelectedIdx] + ", reward: " + reward +
        //                  ", bounds: (" + bounds[0] + "," + bounds[1] + ")");
    }
}
