package MacroMCTS;

import core.game.StateObservation;
import core.player.AbstractPlayer;
import macroactions.macroFeed.ConstantMacroFeed;
import macroactions.macroFeed.IMacroFeed;
import macroactions.MacroAction;
import macroactions.macroFeed.RandomNMacroFeed;
import macroactions.macroHandler.IMacroUser;
import macroactions.macroHandler.MacroHandler;
import macroactions.macroHandler.NormalMacroHandler;
import ontology.Types;
import tools.ElapsedCpuTimer;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created with IntelliJ IDEA.
 * User: ssamot
 * Date: 14/11/13
 * Time: 21:45
 * This is a Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 */
public class Agent extends AbstractPlayer implements IMacroUser {

    public static int MCTS_ITERATIONS = 100;
    public static double K = Math.sqrt(2);
    public static double REWARD_DISCOUNT = 1.00;
    public static int ROLLOUT_DEPTH = 30; //10;


    protected SingleMCTSPlayer mctsPlayer;
    public MacroHandler macroHandler;

    public static int NUM_ACTIONS;
    public static int MACROACTION_LENGTH = 5;
    public static MacroAction[] actions;

    public ArrayList<Types.ACTIONS> extendedActions;

    /**
     * Public constructor with state observation and time due.
     * @param so state observation of the current game.
     * @param elapsedTimer Timer for the controller creation.
     */
    public Agent(StateObservation so, ElapsedCpuTimer elapsedTimer) {
//Get the actions in a static array.
        macroHandler = new NormalMacroHandler(new ConstantMacroFeed(3), this);

        //Determine the actions to use.
        ArrayList<Types.ACTIONS> act = so.getAvailableActions();
        act.add(Types.ACTIONS.ACTION_NIL);
        extendedActions = act;

        macroHandler.setNewActions(act);

        //Create the player.
        mctsPlayer = getPlayer(so, elapsedTimer);
    }

    public ArrayList<Types.ACTIONS> getAvailableActions()
    {
        return extendedActions;
    }


    public void setNewActions(MacroHandler macroHandler, StateObservation so)
    {
        this.macroHandler = macroHandler;//
        this.macroHandler.agent = this;
        //macroHandler = new OneStepMacroHandler(new RandomNMacroFeed(4), this);

        //Determine the actions to use.
        ArrayList<Types.ACTIONS> act = so.getAvailableActions();
        act.add(Types.ACTIONS.ACTION_NIL);
        extendedActions = act;

        macroHandler.setNewActions(act);
    }



    public SingleMCTSPlayer getPlayer(StateObservation so, ElapsedCpuTimer elapsedTimer) {
        return new SingleMCTSPlayer(new Random());
    }


    /**
     * Picks an action. This function is called every game step to request an
     * action from the player.
     * @param stateObs Observation of the current state.
     * @param elapsedTimer Timer when the action returned is due.
     * @return An action for the current state
     */
    public Types.ACTIONS act(StateObservation stateObs, ElapsedCpuTimer elapsedTimer) {

        return macroHandler.act(stateObs,elapsedTimer);
    }


    @Override
    public void init(StateObservation stateObs)
    {
        mctsPlayer.init(stateObs);
    }

    @Override
    public int run(ElapsedCpuTimer elapsedTimer)
    {
        return mctsPlayer.run(elapsedTimer);
    }

    @Override
    public void setActions(MacroAction[] actions, int macroActionLength) {
        this.actions = actions;
        NUM_ACTIONS = actions.length;
        MACROACTION_LENGTH = macroActionLength;
    }

    @Override
    public double getReward()
    {
        if(mctsPlayer.m_root != null)
        {
            if(mctsPlayer.m_root.state.isGameOver())
            {
                if(mctsPlayer.m_root.state.getGameWinner() == Types.WINNER.PLAYER_WINS)
                    return SingleTreeNode.HUGE_POSITIVE;
                else
                    return SingleTreeNode.HUGE_NEGATIVE;
            }

            int act = mctsPlayer.m_root.mostVisitedAction();
            SingleTreeNode bestChild = mctsPlayer.m_root.children[act];
            double rw = bestChild.nVisits;
            return rw;
        }

        return -Double.MAX_VALUE; //TODO: this might cause problems?
    }

}
