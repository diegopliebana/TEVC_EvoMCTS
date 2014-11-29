package macroactions.macroHandler;

import core.game.StateObservation;
import macroactions.MacroAction;
import macroactions.macroFeed.IMacroFeed;
import ontology.Types;
import tools.ElapsedCpuTimer;

import java.util.ArrayList;

/**
 * Created by dperez on 27/11/2014.
 */
public abstract class MacroHandler {

    public static int MACROACTION_LENGTH = 5;
    public static MacroAction[] actions;
    public MacroAction currentMacro;
    public IMacroFeed macroFeed;
    public IMacroUser agent;

    public abstract Types.ACTIONS act(StateObservation stateObs, ElapsedCpuTimer elapsedTimer);

    public void setMacroFeed(IMacroFeed mf)
    {
        this.macroFeed = mf;
    }

    public void setReward(double rw)
    {
        macroFeed.setReward(rw);
    }

    public void setNewActions(ArrayList<Types.ACTIONS> act)
    {
        MACROACTION_LENGTH = macroFeed.getNextLength();
        actions = new MacroAction[act.size()];
        if(act.size() > 0) for(int i = 0; i < actions.length; ++i)
        {
            Types.ACTIONS singleAction = act.get(i);
            Types.ACTIONS[] actionsArray = new Types.ACTIONS[MACROACTION_LENGTH];

            for(int j = 0; j < MACROACTION_LENGTH; ++j)
            {
                actionsArray[j] = singleAction;
            }

            actions[i] = new MacroAction(actionsArray);
        }
        //System.out.print(actions[0].actions.length + ",");
        agent.setActions(actions, MACROACTION_LENGTH);
    }
}
