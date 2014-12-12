package TEVC_MCTS.features;

import TEVC_MCTS.pathfinder.Astar;
import TEVC_MCTS.pathfinder.Node;
import core.game.Observation;
import core.game.StateObservation;
import ontology.Types;
import tools.Vector2d;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * Created by diego on 10/03/14.
 * Features for the game Chase:
 * [VGDLRegistry] wall => 0
 * [VGDLRegistry] avatar => 1
 * [VGDLRegistry] carcass => 2
 * [VGDLRegistry] goat => 3
 * [VGDLRegistry] angry => 4
 * [VGDLRegistry] scared => 5
 */
public class ButterfliesFeatures extends NavFeatureSource
{

    private static final double HUGE_NEGATIVE = -10000000.0;
    private static final double HUGE_POSITIVE =  10000000.0;
    private int WALL = 0, BUTTERFLY = 4;

    private Vector2d avatarPos;

    private double up_down_butterfly, left_right_butterfly;

    protected ArrayList<Observation> grid[][];
    protected int block_size;
    protected double maxDist;


    public ButterfliesFeatures(StateObservation stateObs)
    {
        validAstarStatic = new ArrayList<Integer>();
        astar = new Astar(this);

        block_size = stateObs.getBlockSize();
        grid = stateObs.getObservationGrid();
        maxDist = grid.length * grid[0].length;

        calcFeatures(stateObs);
    }

    @Override
    public LinkedHashMap<String, Double> getFeatureVector()
    {
        LinkedHashMap<String, Double> features = new LinkedHashMap<String, Double>();
        features.put("bias:1", 1.0);
        features.put("up_down_butterfly:"+BUTTERFLY, up_down_butterfly);
        features.put("left_right_butterfly:"+BUTTERFLY, left_right_butterfly);
        return features;
    }


    @Override
    protected void calcFeatures(StateObservation stateObs)
    {
        if(x_arrNeig == null)
            initNeighbours(stateObs);

        avatarPos = stateObs.getAvatarPosition();

        ArrayList<Observation>[] npcPositions = stateObs.getNPCPositions(avatarPos);

        up_down_butterfly = 0;
        left_right_butterfly = 0;

        if(npcPositions != null)
        {
            for(int i = 0; i < npcPositions.length; ++i)
            {
                if(npcPositions[i].size()>0)
                {
                    Observation closestObs = npcPositions[i].get(0);
                    if(closestObs.itype == BUTTERFLY)
                    {
                        Types.ACTIONS act = astarActionThatMinimizes(avatarPos, closestObs.position, block_size, false);
                        if(act == Types.ACTIONS.ACTION_UP) {
                            up_down_butterfly = 1;}
                        if(act == Types.ACTIONS.ACTION_DOWN) {
                            up_down_butterfly = -1;}
                        if(act == Types.ACTIONS.ACTION_LEFT) {
                            left_right_butterfly = 1;}
                        if(act == Types.ACTIONS.ACTION_RIGHT) {
                            left_right_butterfly = -1;}
                    }
                }
            }
        }

    }

    public double valueFunction(StateObservation stateObs) {

        boolean gameOver = stateObs.isGameOver();
        Types.WINNER win = stateObs.getGameWinner();
        double rawScore = stateObs.getGameScore();

        if(gameOver && win == Types.WINNER.PLAYER_LOSES)
            //return HUGE_NEGATIVE;
            rawScore += HUGE_NEGATIVE;

        else if(gameOver && win == Types.WINNER.PLAYER_WINS)
            //return HUGE_POSITIVE;
            rawScore += HUGE_POSITIVE;

        return rawScore;
    }


    @Override
    public double[] getHandTunedWeights(StateObservation stateObs) {
        //Four actions, 2 features  (distance to angry, distance to scared)
        //These are constant because it is always good to increase distance with
        //angry goats and decrease it with scared ones.
        double bias = 0;

        //actions order: left, right, down, up
        return new double[]{
                bias,  0,  1,
                bias,  0, -1,
                bias,  -1,  0,
                bias,  1,  0
        };
//        return new double[]{
//                0, 0, 0, 0,
//                0, 0, 0, 0,
//                0, 0, 0, 0,
//                0, 0, 0, 0
//        };
    }


    protected void initNeighbours(StateObservation stObs)
    {
        //up, down, left, right
        x_arrNeig = new int[]{0,    0,    -1,    1};
        y_arrNeig = new int[]{-1,   1,     0,    0};
    }

    @Override
    protected Node extractNonObstacle(int x, int y)
    {
        if(x < 0 || y < 0 || x >= grid.length || y >= grid[x].length)
            return null;

        int numObs = grid[x][y].size();
        boolean isObstacle = false;
        for(int i = 0; !isObstacle && i < numObs; ++i)
        {
            Observation obs = grid[x][y].get(i);

            if(obs.itype == WALL)
            {
                isObstacle = true;
            }
        }

        if(isObstacle)
            return null;

        return new Node(new Vector2d(x,y));

    }



}
