package macroactions.macroHandler;

import core.game.StateObservation;
import macroactions.MacroAction;
import tools.ElapsedCpuTimer;

/**
 * Created by dperez on 27/11/2014.
 */
public interface IMacroUser
{
    public double getReward();
    public void init(StateObservation stateObs);
    public int run(ElapsedCpuTimer elapsedTimer);
    public void setActions(MacroAction[] actions, int macroActionLength);
}
