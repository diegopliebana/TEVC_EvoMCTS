package FastEvoMCTS;

import core.game.StateObservation;
import tools.ElapsedCpuTimer;
import tools.Vector2d;

import java.util.Random;

/**
 * Created with IntelliJ IDEA.
 * User: Diego
 * Date: 07/11/13
 * Time: 17:13
 */
public class SingleMCTSPlayer
{
    /**
     * Root of the tree.
     */
    //public SingleTreeNode m_root;
    //public SingleTreeNodeOL m_root;
    public TreeNode m_root;

    /**
     * Random generator.
     */
    public Random m_rnd;

    public TunableRoller roller;
    public FitVectorSource source;
    public Memory memory;

    public static int[][] m_hitsMap;

    /**
     * Creates the MCTS player with a sampleRandom generator object.
     * @param a_rnd sampleRandom generator object.
     */
    public SingleMCTSPlayer(Random a_rnd, TunableRoller roller, Memory memory)
    {
        this.memory = memory;
        m_rnd = a_rnd;

//        if(Config.USE_OPEN_LOOP)
//            m_root = new SingleTreeNodeOL(a_rnd, roller, memory);
//        else
            m_root = new SingleTreeNode(a_rnd, roller, memory);

        m_root.childIdx = -1;
        this.roller = roller;
    }

    /**
     * Inits the tree with the new observation state in the root.
     * @param a_gameState current state of the game.
     * @param features of the game state
     */
    public void init(StateObservation a_gameState, FeatureExtraction features)
    {
        int nActions = a_gameState.getAvailableActions().size();

        if(Config.USE_FORGET_ASTAR_CACHE && a_gameState.getGameTick() > 0 &&  a_gameState.getGameTick() % Config.FORGET_TIME == 0)
        {
            features.emptyAstarCache();
        }

        if(Config.TOGGLE_ASTAR && a_gameState.getGameTick() > 0 && a_gameState.getGameTick() % Config.FORGET_TIME == 0)
        {
            Config.USE_ASTAR = !Config.USE_ASTAR;
        }

        if(Config.USE_MEMORY)
            features.setMemory(memory);

        roller.init(a_gameState, features);  //init the roller first.
        //if(Config.USE_W_VECTOR)
        {
            String[] featureNames = roller.featureNames(a_gameState);
            int order = FitVectorSource.MAX_BEST;

            //if(source == null) //uncomment this line to not initialize the ES every game cycle.

            if(source == null)
                 if(Config.ES_TYPE == Config.ONE_PLUS_ONE)
                    source = new VariableRandomMutHillClimber(featureNames, order, nActions, memory, m_rnd);
                if(Config.ES_TYPE == Config.MU_PLUS_ONE)
                    source = new ES(featureNames, order, nActions, memory, m_rnd);
            else
            {
                if(!Config.KEEP_EVO && (a_gameState.getGameTick() % Config.EVO_FORGET_TIME == 0))
                {
                    //must be re-init every W_VECTOR_FORGET_TIME cycles.
                    if(Config.ES_TYPE == Config.ONE_PLUS_ONE)
                        source = new VariableRandomMutHillClimber(featureNames, order, nActions, memory, m_rnd);
                    if(Config.ES_TYPE == Config.MU_PLUS_ONE)
                        source = new ES(featureNames, order, nActions, memory, m_rnd);
                }
            }
        }

        //Set the game observation to a newly roo//t node.
        if(Config.USE_OPEN_LOOP)
            m_root = new SingleTreeNodeOL(m_rnd, roller, memory);
        else
            m_root = new SingleTreeNode(m_rnd, roller, memory);

        m_root.state = a_gameState;
        m_root.childIdx = -1;

        if(Config.COMPUTE_HIT_MAP)
        {
            Vector2d worldDim = new Vector2d(a_gameState.getWorldDimension().width, a_gameState.getWorldDimension().height);
            int blockSize = a_gameState.getBlockSize();
            m_hitsMap = new int[(int) (worldDim.x/blockSize)][(int) (worldDim.x/blockSize)];
        }

    }

    /**
     * Runs MCTS to decide the action to take. It does not reset the tree.
     * @param elapsedTimer Timer when the action returned is due.
     * @return the action to execute in the game.
     */
    public int run(ElapsedCpuTimer elapsedTimer)
    {
        //Do the search within the available time.
        m_root.mctsSearch(elapsedTimer, roller, source);

        //Determine the best action to take and return it.
        int action = m_root.mostVisitedAction();
        //int action = m_root.bestAction();
        //int action = m_root.bestBiasedAction();

        return action;
    }

}
