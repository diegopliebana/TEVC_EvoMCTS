package macroactions.macroHandler;

import core.game.StateObservation;
import macroactions.MacroAction;
import macroactions.macroFeed.IMacroFeed;
import ontology.Types;
import tools.ElapsedCpuTimer;

import java.util.ArrayList;

/**
 * In this version of the macro action handler, the agent re-inits the search algorithm
 * at every act() call. A new macro-action is chosen every cycle from the macroFeed.
 * Created by dperez on 27/11/2014.
 */
public class OneStepMacroHandler extends MacroHandler
{

    MacroAction bestSoFar;
    double bestFitness;

    public OneStepMacroHandler(IMacroFeed mf, IMacroUser agent)
    {
        macroFeed = mf;
        this.agent = agent;
        bestSoFar = null;
        bestFitness = -Double.MAX_VALUE;
    }


    @Override
    public Types.ACTIONS act(StateObservation stateObs, ElapsedCpuTimer elapsedTimer)
    {
        //Advance the game state with the current macro-action.
        if(currentMacro != null)
        {
            //int curpos=stateObs.getGameTick();
            MacroAction macroHelp = currentMacro.copy();
            while(!macroHelp.isFinished())
            {
                stateObs.advance(macroHelp.next());
            }
            //System.out.println("Advancing game state from " + curpos + " to " + stateObs.getGameTick());
        }

        //We ALWAYS reset the tree.
        setReward(agent.getReward());

        //System.out.println("Creating a new tree...");
        setNewActions(agent.getAvailableActions());

        agent.init(stateObs);

        //Run MCTS for another cycle
        System.out.println("Executing tree from " + stateObs.getGameTick() + ", MAC_LENGTH: " + MACROACTION_LENGTH);

        int action = agent.run(elapsedTimer);

        checkForBest(action);

        if(currentMacro == null || currentMacro.isFinished())
        {
            //It's time to determine the next macroactions action. As we had different trees, we need to
            //find out which one is the one to take the action from... and then take it.
            currentMacro = bestSoFar;
            currentMacro.reset();
            this.resetBest();
            //System.out.println("New macroactions-action set:");
            currentMacro.print();
        }

        //... and return the next action.
        System.out.println("Returning action index " + currentMacro.cursor + ": " + currentMacro.peek());
        return currentMacro.next();
    }

    protected void resetBest()
    {
        bestSoFar = null;
        bestFitness = -Double.MAX_VALUE;
    }

    protected void checkForBest(int action)
    {
        MacroAction lastBestMacroAction = actions[action].copy();
        double lastTreeFitness = agent.getReward();

        if(lastTreeFitness > bestFitness)
        {
            bestFitness = lastTreeFitness;
            bestSoFar = lastBestMacroAction;
        }
    }

}
