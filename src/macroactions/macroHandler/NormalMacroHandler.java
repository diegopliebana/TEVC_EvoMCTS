package macroactions.macroHandler;

import core.game.StateObservation;
import macroactions.MacroAction;
import macroactions.macroFeed.IMacroFeed;
import ontology.Types;
import tools.ElapsedCpuTimer;

import java.util.ArrayList;

/**
 * This version grows the same tree while the previous macro-action is being executed.
 * Created by dperez on 27/11/2014.
 */
public class NormalMacroHandler extends MacroHandler {


    public NormalMacroHandler(IMacroFeed mf, IMacroUser agent)
    {
        macroFeed = mf;
        this.agent = agent;
    }

    @Override
    public Types.ACTIONS act(StateObservation stateObs, ElapsedCpuTimer elapsedTimer)
    {
        if(currentMacro != null)
        {
            int curpos=stateObs.getGameTick();
            MacroAction macroHelp = currentMacro.copy();
            while(!macroHelp.isFinished())
            {
                stateObs.advance(macroHelp.next());
            }
            //System.out.println("Advancing game state from " + curpos + " to " + stateObs.getGameTick());
        }

        //Reset the tree if no current macroactions-action or it is finished.
        if(currentMacro == null || currentMacro.cursor==1)
        {
            setReward(agent.getReward());

            //System.out.println("Creating a new tree...");
            setNewActions(stateObs.getAvailableActions());

            agent.init(stateObs);
        }

        //Run MCTS for another cycle
        //System.out.println("Executing tree from " + mctsPlayer.m_root.rootState.getGameTick());

        int action = agent.run(elapsedTimer);

        if(currentMacro == null || currentMacro.isFinished())
        {
            //It's time to determine the next macroactions action.
            currentMacro = actions[action].copy();
            currentMacro.reset();
            //System.out.println("New macroactions-action set:");
            //currentMacro.print();
        }

        //... and return the next action.
        //System.out.println("Returning action index " + currentMacro.cursor + ": " + currentMacro.peek());
        return currentMacro.next();
    }
}
