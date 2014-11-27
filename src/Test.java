import core.ArcadeMachine;

import java.util.Random;

/**
 * Created with IntelliJ IDEA.
 * User: Diego
 * Date: 04/10/13
 * Time: 16:29
 * This is a Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 */
public class Test
{

    public static void main(String[] args)
    {
        //Available controllers:
        String sampleRandomController = "controllers.sampleRandom.Agent";
        String sampleOneStepController = "controllers.sampleonesteplookahead.Agent";
        String sampleMCTSController = "controllers.sampleMCTS.Agent";
        String sampleGAController = "controllers.sampleGA.Agent";
        //String controller = "FastEvoMCTS.Agent";
        String controller = "TEVC_MCTS.Agent";
        String controller_ol = "sampleOLMCTS.Agent";
        String controller_mol = "MacroOLMCTS.Agent";
        String pathfinder = "controllers.pathfinder.Agent";

        //Available games:
        String gamesPath = "examples/gridphysics/";
        //String games[] = new String[]{"aliens", "boulderdash", "butterflies", "chase", "frogs",
        //       "missilecommand", "portals", "sokoban", "survivezombies", "zelda"};
        //String games[] = new String[]{"camelRace", "digdug", "firestorms", "infection", "firecaster",
        //                             "overload", "pacman", "seaquest", "whackamole", "eggomania"};
		
        String games[] = new String[]{"aliens", "boulderdash", "butterflies", "chase", "frogs",
                                      "missilecommand", "portals", "sokoban", "survivezombies", "zelda",
                                      "camelRace", "digdug", "firestorms", "infection", "firecaster",
                                      "overload", "pacman", "seaquest", "whackamole", "eggomania",
                                      "circle", "leftright"};



        //String games[] = new String[]{"aliens", "butterflies", "chase", "missilecommand", "survivezombies",
        //                              "camelRace", "infection", "seaquest", "whackamole", "eggomania"};

        //Other settings
        boolean visuals = true;
        String recordActionsFile = null; //where to record the actions executed. null if not to save.
        int seed = new Random().nextInt();
        //System.out.println("Seed = " + seed);
        String wkDir = System.getProperty("user.dir");
        String filename = wkDir.substring(wkDir.lastIndexOf("\\")+1) + ".csv";
        //String filename = wkDir.substring(wkDir.lastIndexOf("\\")+1) + ".txt";

        //Game and level to play

        int gameIdx = 3;
        int levelIdx = 0; //level names from 0 to 4 (game_lvlN.txt).
        String game = gamesPath + games[gameIdx] + ".txt";
        String level1 = gamesPath + games[gameIdx] + "_lvl" + levelIdx +".txt";

        // 1. This starts a game, in a level, played by a human.
        //ArcadeMachine.playOneGame(game, level1, recordActionsFile, seed);

        // 2. This plays a game in a level by the controller.
        //ArcadeMachine.runOneGame(game, level1, visuals, sampleMCTSController, recordActionsFile, seed);
        //ArcadeMachine.runOneGame(game, level1, visuals, controller, recordActionsFile, seed);
        //ArcadeMachine.runOneGame(game, level1, visuals, controller_ol, recordActionsFile, seed);
        //ArcadeMachine.runOneGame(game, level1, visuals, controller_mol, recordActionsFile, seed);


        // 3. This replays a game from an action file previously recorded
        //String readActionsFile = "actionsFile_aliens_lvl0.txt";  //This example is for
        //ArcadeMachine.replayGame(game, level1, visuals, readActionsFile);

        // 4. This plays a single game, in N levels, M times :
        //String level2 = gamesPath + games[gameIdx] + "_lvl" + 1 +".txt";//

        //int M = 100;
        //int RLmin = 95;     //rollout lenghts to be tested from RLmin to RLmax, where RLmin >= 1
        //int RLmax = 100;
        //boolean isFixedTest = false;

        //int M = 1000;   //number of repeats (only for statistical accuracy)
        //int[] RL = new int[]{1,5,10,15,20,25,30,35,40,45,50,55,60,65,70,75,80,85,90,95,100};

        //int M = 50; //1000;   //number of repeats (only for statistical accuracy)
        //int[] RL = new int[]{10};

        //ArcadeMachine.runGamesN(game, level1, M, RLmin, RLmax, controller, isFixedTest, seed, filename);
        //ArcadeMachine.runGamesN(game, level1, M, RLmin, RLmax, sampleMCTSController, isFixedTest, seed, filename);
        //ArcadeMachine.runGamesN(game, level1, M, RL, sampleMCTSController, isFixedTest, seed, filename);
        //ArcadeMachine.runGamesN(game, level1, M, RL, controller, isFixedTest, seed, filename);

        //RightLeft_2014_11_20(gamesPath, games, controller, sampleMCTSController, seed, filename);
        //Circle_2014_11_21(gamesPath, games, controller, sampleMCTSController, seed, filename);
        Chase_2014_11_21(gamesPath, games, controller, sampleMCTSController, seed);
        //ChaseTest_2014_11_26(gamesPath, games, controller, sampleMCTSController, seed);


        /*int M = 100;
        boolean isFixedTest = true;
        int RLmin = 1;
        int RLmax = 20; //15; //20;
        for(int i = 14; i > 0; --i)
        //for(int i = 10; i > 0; --i)
        {
            //System.out.println(wkDir.substring(wkDir.lastIndexOf("\\")+1));
            //filename = wkDir.substring(wkDir.lastIndexOf("\\")+1)  + "_" + controller + "_Dist" + i + ".txt";
            filename = wkDir.substring(wkDir.lastIndexOf("\\")+1)  + "_" + controller + "_Dist" + i + ".txt";
            level1 = gamesPath + games[gameIdx] + "_lvl" + i +".txt";
            System.out.println("filename: " + filename + ", levelFile: " + level1);
            //ArcadeMachine.runGamesN(game, level1, M, RLmin, RLmax, controller, isFixedTest, seed, filename);
            ArcadeMachine.runGamesN(game, level1, M, RLmin, RLmax, sampleMCTSController, isFixedTest, seed, filename);
        }*/

        //5. This plays N games, in the first L levels, M times each. Actions to file optional (set saveActions to true).
        /*int N = 30, L = 1, M = 1;
        boolean saveActions = false;
        String[] levels = new String[L];
        String[] actionFiles = new String[L*M];
        for(int i = 0; i < N; ++i)
        {
            int actionIdx = 0;
            game = gamesPath + games[i] + ".txt";
            for(int j = 0; j < L; ++j){
                levels[j] = gamesPath + games[i] + "_lvl" + j +".txt";
                if(saveActions) for(int k = 0; k < M; ++k)
                    actionFiles[actionIdx++] = "actions_game_" + i + "_level_" + j + "_" + k + ".txt";
            }
            ArcadeMachine.runGames(game, levels, M, controller, saveActions? actionFiles:null, seed);
        }
                     */


        //runNMacro(gamesPath, games, controller_mol, args);

    }

    public static void runNMacro(String gamesPath, String[] games, String controller, String args[])
    {
        int gameId = -1;
        if(args!=null && args.length==1)
        {
            gameId = Integer.parseInt(args[0]);
        }

        int NGames = 20, NRepetitions = 100;
        int macroActionLengths[] = new int[]{1,2,3,5};

        if(gameId == -1) {
            for (int i = 0; i < NGames; ++i) {

                String game = gamesPath + games[i] + ".txt";
                String level = gamesPath + games[i] + "_lvl0.txt";
                String filename = games[i] + "_lvl0_" + controller + ".txt";

                ArcadeMachine.runGamesMacroN(game, level, NRepetitions, macroActionLengths, controller, false, filename);
            }
        }else
        {
            String game = gamesPath + games[gameId] + ".txt";
            String level = gamesPath + games[gameId] + "_lvl0.txt";
            String filename = games[gameId] + "_lvl0_" + controller + ".txt";

            ArcadeMachine.runGamesMacroN(game, level, NRepetitions, macroActionLengths, controller, false, filename);
        }

    }

    public static void RightLeft_2014_11_20(String gamesPath, String[] games, String controller, String sampleMCTSController, int seed, String filename){

        int gameIdx = 21;
        int levelIdx = 0; //level names from 0 to 4 (game_lvlN.txt).
        String game = gamesPath + games[gameIdx] + ".txt";
        String level1 = gamesPath + games[gameIdx] + "_lvl" + levelIdx +".txt";

        //int M = 10000;   //number of repeats (only for statistical accuracy)
        //int RLmin = 1;     //rollout lenghts to be tested from RLmin to RLmax, where RLmin >= 1
        //int RLmax = 8;

        int M = 1000;   //number of repeats (only for statistical accuracy)
        int RLmin = 1;     //rollout lenghts to be tested from RLmin to RLmax, where RLmin >= 1
        int RLmax = 50;

        boolean isFixedTest = false;
        ArcadeMachine.runGamesN(game, level1, M, RLmin, RLmax, controller, isFixedTest, seed, filename);
        //ArcadeMachine.runGamesN(game, level1, M, RLmin, RLmax, sampleMCTSController, isFixedTest, seed, filename);
    }

    public static void Circle_2014_11_21(String gamesPath, String[] games, String controller, String sampleMCTSController, int seed, String filename){

        int gameIdx = 20;
        int levelIdx = 0; //level names from 0 to 4 (game_lvlN.txt).
        String game = gamesPath + games[gameIdx] + ".txt";
        String level1 = gamesPath + games[gameIdx] + "_lvl" + levelIdx +".txt";

        //int M = 10000;   //number of repeats (only for statistical accuracy)
        //int RLmin = 1;     //rollout lenghts to be tested from RLmin to RLmax, where RLmin >= 1
        //int RLmax = 8;

        int M = 1000;   //number of repeats (only for statistical accuracy)
        int[] RL = new int[]{5,10,15,20,25,30,35,40,45,50,55,60,65,70,75,80,85,90,95,100};

        boolean isFixedTest = false;
        ArcadeMachine.runGamesN(game, level1, M, RL, controller, isFixedTest, seed, filename);
        //ArcadeMachine.runGamesN(game, level1, M, RL, sampleMCTSController, isFixedTest, seed, filename);
    }

    public static void Chase_2014_11_21(String gamesPath, String[] games, String controller, String sampleMCTSController, int seed){

        String wkDir = System.getProperty("user.dir");
        String filename = wkDir.substring(wkDir.lastIndexOf("\\")+1) + ".txt";

        int gameIdx = 3;
        //int levelIdx = 0; //level names from 0 to 4 (game_lvlN.txt).
        String game = gamesPath + games[gameIdx] + ".txt";

        //This plays the first L levels, M times each. Actions to file optional (set saveActions to true).
        int L = 5;      //number of first L levels
        int M = 200;     //number of repeats (for statistical accuracy) of each level
        int rollOutLength = 10;

        String[] levels = new String[L];

        for(int j = 0; j < L; ++j)
            levels[j] = gamesPath + games[gameIdx] + "_lvl" + j +".txt";

        boolean isFixedTest = false;
        ArcadeMachine.runGamesLN(game, levels, M, rollOutLength, controller, isFixedTest, seed, filename);
        //ArcadeMachine.runGamesLN(game, levels, M, rollOutLength, sampleMCTSController, isFixedTest, seed, filename);

    }


    public static void ChaseTest_2014_11_26(String gamesPath, String[] games, String controller, String sampleMCTSController, int seed){

        String wkDir = System.getProperty("user.dir");
        String filename = wkDir.substring(wkDir.lastIndexOf("\\")+1) + ".txt";

        int gameIdx = 3;
        //int levelIdx = 0; //level names from 0 to 4 (game_lvlN.txt).
        String game = gamesPath + games[gameIdx] + ".txt";

        //This plays the first L levels, M times each. Actions to file optional (set saveActions to true).
        int L = 1;      //number of levels starting with lvl5
        int M = 1000;     //number of repeats (for statistical accuracy) of each level
        int rollOutLength = 10;

        String[] levels = new String[L];

        for(int j = 0; j < L; ++j)
            levels[j] = gamesPath + games[gameIdx] + "_lvl" + (5+j) +".txt";

        boolean isFixedTest = false;
        ArcadeMachine.runGamesLN(game, levels, M, rollOutLength, controller, isFixedTest, seed, filename);
        //ArcadeMachine.runGamesLN(game, levels, M, rollOutLength, sampleMCTSController, isFixedTest, seed, filename);

    }

}
