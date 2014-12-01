package macroactions.macroHandler;

import core.game.StateObservation;
import macroactions.MacroAction;
import ontology.Types;
import tools.ElapsedCpuTimer;

import java.util.ArrayList;

/**
 * Created by dperez on 27/11/2014.
 */
public interface IMacroUser
{
    public ArrayList<Types.ACTIONS> getAvailableActions();
    public double getReward();
    public void init(StateObservation stateObs);
    public int run(ElapsedCpuTimer elapsedTimer);
    public void setActions(MacroAction[] actions, int macroActionLength);
}
