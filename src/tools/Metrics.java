package tools;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;

/**
 * Created by Hruska on 13.12.2014.
 */
public final class Metrics {

    //-- defines and constants --//

    public static final int NUM_METRICS = 20*100;

    public static final String[] FORMAT =
            {
                    "%6.3f",
            };

    public static final int NUM_FILES = 1;
    public static final int F_SHORT = 0;

    private static final String[] FILE_LABELS =
            {
                    "__summaryShort",
            };

    public static final double defaultConfidenceFactor = 1.96;
        //1.645 for 90%
        //1.96  for 95%
        //2.58  fro 99%
    public static final int defaultPrintDetail = 0;    //possible values 0,1,2
    public static final int defaultPrintDepth = 0;     //possible values 0,1,2

    //-- configuration --//

    public static double confidenceFactor = defaultConfidenceFactor;
    public static boolean[] printIgnoreMetrics = new boolean[NUM_METRICS];  //all elements set to all false at initStats()

    //-- stats structures --//

    public static double[]          lastResults;            //last batch of results <- MUST BE SET FROM OUTSIDE
    public static boolean[]         ignoreResults;          //array of flags to mark which values to ignore when adding lastResults

    public static StatSummary[][][] resultsLevels;             //avgLevels by repeats, confidence by repeats

    public static StatSummary[][]   resultsGames;           //avgGames by levels, confidence by repeats of level batches
    public static StatSummary[][]   resultsGamesDev;        //deviation by levels for each game (calculated from last averaged values, relative to mean)

    public static StatSummary[]     resultsSummary;                 //avg by all levels for all games, confidence by repeats of game batches
    public static StatSummary[]     resultsSummaryDevGames;         //deviation by games (calculated from last averaged values, relative to mean)
    public static StatSummary[]     resultsSummaryAvgDevLevels;     //avg of resultsGamesDev by games

    public static boolean isInitialized = false;

    //-- output files --//

    public static PrintStream[] outFiles = new PrintStream[NUM_FILES];
    private static boolean filesCreated = false;

    //-- procedures --//

    public static void initStats(int numGames, int numLevelsPerGame)
    {

        lastResults = new double[NUM_METRICS];
        ignoreResults = new boolean[NUM_METRICS];

        resultsLevels = new StatSummary[numGames][numLevelsPerGame][NUM_METRICS];
        resultsGames = new StatSummary[numGames][NUM_METRICS];
        resultsGamesDev = new StatSummary[numGames][NUM_METRICS];
        resultsSummary = new StatSummary[NUM_METRICS];
        resultsSummaryAvgDevLevels = new StatSummary[NUM_METRICS];
        resultsSummaryDevGames = new StatSummary[NUM_METRICS];

        for(int g = 0; g < numGames; g++)
            for(int l = 0; l < numLevelsPerGame; l++)
                for(int m = 0; m < NUM_METRICS; m++)
                    resultsLevels[g][l][m] = new StatSummary();

        for(int g = 0; g < numGames; g++)
            for(int m = 0; m < NUM_METRICS; m++) {
                resultsGames[g][m] = new StatSummary();
                resultsGamesDev[g][m] = new StatSummary();
            }

        for(int m = 0; m < NUM_METRICS; m++) {
            resultsSummary[m] = new StatSummary();
            resultsSummaryAvgDevLevels[m] = new StatSummary();
            resultsSummaryDevGames[m] = new StatSummary();
            printIgnoreMetrics[m] = false;
        }

        isInitialized = true;
    }

    public static void initFiles(String baseFilename, String filenameEnding)
    {
        try {
            //Create output file
            for(int f = 0; f < NUM_FILES; f++)
                outFiles[f] = new PrintStream(new FileOutputStream(new File(baseFilename+FILE_LABELS[f]+filenameEnding)));
            filesCreated = true;
        }
        catch(Exception e) {
            System.out.println(e);
        }

    }

    public static void closeFiles()
    {
        if(filesCreated)
        {
            try {
                for(int f = 0; f < NUM_FILES; f++)
                    outFiles[f].close();
            }
            catch(Exception e) {
                System.out.println(e);
            }
        }
    }

    public static void resetLastResults(){
        for(int m = 0; m < NUM_METRICS; m++) {
            lastResults[m] = 0.0;
            ignoreResults[m] = false;
        }
    }

    public static void updateStats(int game, int level, double[] resultsVector)
    {
        //no safety checks -> keep sure that resultsVector.length == NUM_METRICS

        //update results for each level
        for(int m = 0; m < NUM_METRICS; m++) {
            if(!ignoreResults[m]) {

                //average and deviation by levels
                resultsLevels[game][level][m].add(resultsVector[m]);
                resultsLevels[game][level][m].mean();
                resultsLevels[game][level][m].conf(confidenceFactor);

                //average by games
                resultsGames[game][m].add(resultsVector[m]);
                resultsGames[game][m].conf(confidenceFactor);

                //average overall
                resultsSummary[m].add(resultsVector[m]);
                resultsSummary[m].conf(confidenceFactor);

            }
        }

        //check if level is last
        if(level == (resultsLevels[game].length - 1))
        {
            //update results for each game
            for(int m = 0; m < NUM_METRICS; m++) {
                if(!ignoreResults[m]) {

                    //deviation by levels
                    resultsGamesDev[game][m].reset();
                    for (int l = 0; l < resultsLevels[game].length; l++) {
                        double value = resultsLevels[game][l][m].getMean();
                        if(!(Double.isNaN(value)) && !(Double.isInfinite(value)))
                            resultsGamesDev[game][m].add(value);
                    }
                    resultsGamesDev[game][m].sdPop();

                }
            }

            //check if game is last
            if(game == (resultsGames.length - 1))
            {
                //update summary results
                for(int m = 0; m < NUM_METRICS; m++) {
                    if(!ignoreResults[m]) {

                        //deviation by games
                        resultsSummaryDevGames[m].reset();
                        for (int g = 0; g < resultsGames.length; g++) {
                            double value = resultsGames[g][m].getMean();
                            if (!(Double.isNaN(value)) && !(Double.isInfinite(value)))
                                resultsSummaryDevGames[m].add(value);
                        }
                        resultsSummaryDevGames[m].sdPop();

                        //average of deviations by levels across games
                        resultsSummaryAvgDevLevels[m].reset();
                        for (int g = 0; g < resultsGamesDev.length; g++) {
                            double value = resultsGamesDev[g][m].getSdRel();
                            if (!(Double.isNaN(value)) && !(Double.isInfinite(value)))
                                resultsSummaryAvgDevLevels[m].add(value);
                        }
                        resultsSummaryAvgDevLevels[m].mean();

                    }
                }

            }

        }

        //-- relevant values to print out / write to file --//
        //resultsAll[game][level][metric].getMean();    //updated after each level repeat
        //resultsAll[game][level][metric].getConf();    //updated after each level repeat
        //
        //resultsGames[game][metric].getMean();         //updated after each level repeat
        //resultsGames[game][metric].getConf();         //updated after each level repeat
        //resultsGamesDev[game][metric].getSdRel();        //updated after each batch of levels
        //
        //resultsSummary[metric].getMean();                 //updated after each each level repeat
        //resultsSummary[metric].getConf();                 //updated after each each level repeat
        //resultsSummaryDevGames[metrics].getSdRel();       //updated after each batch of games
        //resultsSummaryAvgDevLevels[metrics].getMean();    //updated after each batch of games
    }

    /**
     * Print stats on standard output
     * @param printer Target output PrintStream
     * @param depth Depth of output (valid values: 0, 1, 2)
     * @param detail Level of detail for each metric (valid values: 0, 1, 2)
     */
    public static void print(PrintStream printer, int depth, int detail, String firstStringLine)
    {
        printer.format("%s",firstStringLine);

        //print stats summary
        printSummary(printer, detail);

        //print stats by games
        if(depth > 0)
            printByGames(printer, detail);

        //prints stats by levels
        if (depth > 1)
            printByLevels(printer, detail);

        printer.format("\n");
    }
    public static void print() { print(System.out, defaultPrintDepth, defaultPrintDetail, ""); }
    public static void print(int depth, int detail, String firstStringLine) { print(System.out, depth , detail, firstStringLine ); }

    public static void printSummary(PrintStream printer, int detail)
    {
        try {
            for (int m = 0; m < NUM_METRICS; m++) {
                if (!printIgnoreMetrics[m]) {
                    printer.format(FORMAT[0], resultsSummary[m].getMean());
                    if (detail > 0) {
                        printer.format(" " + FORMAT[0], resultsSummary[m].getConf());
                    }
                    if (detail > 1) {
                        printer.format(" %4.2f", resultsSummaryDevGames[m].getSdRel());
                        printer.format(" %4.2f", resultsSummaryAvgDevLevels[m].getMean());
                    }
                    printer.format("  ");
                }
            }
            printer.format("        ");
        }
        catch(Exception e) {
            System.out.println(e);
        }
    }

    public static void printByGames(PrintStream printer, int detail)
    {
        try {
            for (int g = 0; g < resultsLevels.length; g++) {
                for (int m = 0; m < NUM_METRICS; m++) {
                    if (!printIgnoreMetrics[m]) {
                        printer.format(FORMAT[0], resultsGames[g][m].getMean());
                        if (detail > 0) {
                            printer.format(" " + FORMAT[0], resultsGames[g][m].getConf());
                        }
                        if (detail > 1) {
                            printer.format(" %4.2f", resultsGamesDev[g][m].getSdRel());
                        }
                        printer.format("  ");
                    }
                }
                printer.format("  ");
            }
            printer.format("      ");
        }
        catch(Exception e) {
            System.out.println(e);
        }
    }

    public static void printByLevels(PrintStream printer, int detail)
    {
        try {
            for (int g = 0; g < resultsLevels.length; g++) {
                for (int l = 0; l < resultsLevels[g].length; l++) {
                    for (int m = 0; m < NUM_METRICS; m++) {
                        if (!printIgnoreMetrics[m]) {
                            printer.format(FORMAT[0], resultsLevels[g][l][m].getMean());
                            if (detail > 0) {
                                printer.format(" " + FORMAT[0], resultsLevels[g][l][m].getConf());
                            }
                            printer.format("  ");
                        }
                    }
                    printer.format("  ");
                }
                printer.format("  ");
            }
            printer.format("    ");
        }
        catch(Exception e) {
            System.out.println(e);
        }
    }

    public static void printLastResults(PrintStream printer)
    {
        try {
            for (int m = 0; m < NUM_METRICS; m++) {
                if (!printIgnoreMetrics[m]) {
                    printer.format(FORMAT[0], lastResults[m]);
                    printer.format("  ");
                }
            }
            printer.format("  ");
        }
        catch(Exception e) {
            System.out.println(e);
        }

    }

    public static void updateFiles(int r, int g, int l, String firstStringLine)
    {

        outFiles[F_SHORT].format(firstStringLine);      printSummary(outFiles[F_SHORT],0);      outFiles[F_SHORT].format("\n");
        //outFiles[F_SUMMARY].format(firstStringLine);    printSummary(outFiles[F_SUMMARY],2);    outFiles[F_SUMMARY].format("\n");
//        outFiles[F_GAMES].format(firstStringLine);      printByGames(outFiles[F_GAMES],2);      outFiles[F_GAMES].format("\n");
//        outFiles[F_LEVELS].format(firstStringLine);     printByLevels(outFiles[F_LEVELS],2);    outFiles[F_LEVELS].format("\n");
//        outFiles[F_RAWVER].format(firstStringLine);     printLastResults(outFiles[F_RAWVER]);   outFiles[F_RAWVER].format("\n");
//
//        if((l == 0)&&(g == 0))
//            outFiles[F_RAW].format(firstStringLine);
//        printLastResults(outFiles[F_RAW]);
//        if(l == resultsLevels[g].length - 1) {
//            outFiles[F_RAW].format("  ");
//            if(g == resultsLevels.length - 1)
//                outFiles[F_RAW].format("\n");
//        }

    }

    public static void printConfiguration(String[] games, int numLevelsPerGame, String controller, boolean printToFiles)
    {
        System.out.format("\nGames[%2d]: ", games.length);
        for(int i = 0; i < games.length; i++)
            System.out.format(" %s", games[i]);
        System.out.format("\n");
        System.out.format("Levels per game: %d\n",numLevelsPerGame);
        System.out.format("Controller: %s\n\n", controller);

        if(printToFiles){
            for(int f = 0; f < NUM_FILES; f++){
                outFiles[f].format("\nGames[%2d]: ", games.length);
                for(int i = 0; i < games.length; i++)
                    outFiles[f].format(" %s", games[i]);
                outFiles[f].format("\n");
                outFiles[f].format("Levels/game: %d\n",numLevelsPerGame);
                outFiles[f].format("Controller: %s\n\n", controller);
            }
        }

    }

    public static void printHeaderFiles(String offset, String beforeLastLine)
    {
        for(int line = 2; line > 0; line--){
            outFiles[F_SHORT].format(offset); printHeaderSummary(outFiles[F_SHORT],0,line); outFiles[F_SHORT].format("\n");
        }
        outFiles[F_SHORT].format(beforeLastLine); printHeaderSummary(outFiles[F_SHORT],0,0); outFiles[F_SHORT].format("\n");

//        for(int line = 2; line > 0; line--){
//            outFiles[F_SUMMARY].format(offset); printHeaderSummary(outFiles[F_SUMMARY],2,line); outFiles[F_SUMMARY].format("\n");
//        }
//        outFiles[F_SUMMARY].format(beforeLastLine); printHeaderSummary(outFiles[F_SUMMARY],2,0); outFiles[F_SUMMARY].format("\n");

//        for(int line = 3; line > 0; line--){
//            outFiles[F_GAMES].format(offset); printHeaderByGames(outFiles[F_GAMES],2,line); outFiles[F_GAMES].format("\n");
//        }
//        outFiles[F_GAMES].format(beforeLastLine); printHeaderByGames(outFiles[F_GAMES],2,0); outFiles[F_GAMES].format("\n");
//
//        for(int line = 4; line > 0; line--){
//            outFiles[F_LEVELS].format(offset); printHeaderByLevels(outFiles[F_LEVELS],2,line); outFiles[F_LEVELS].format("\n");
//        }
//        outFiles[F_LEVELS].format(beforeLastLine); printHeaderByLevels(outFiles[F_LEVELS],2,0); outFiles[F_LEVELS].format("\n");
//
//        for(int line = 4; line > 1; line--){
//            outFiles[F_RAW].format(offset); printHeaderByLevels(outFiles[F_RAW],0,line); outFiles[F_RAW].format("\n");
//        }
//        outFiles[F_RAW].format(beforeLastLine); printHeaderByLevels(outFiles[F_RAW],0,1); outFiles[F_RAW].format("\n");
//
//        outFiles[F_RAWVER].format(beforeLastLine); printHeaderSummary(outFiles[F_RAWVER], 0, 1); outFiles[F_RAWVER].format("\n");
    }

    /**
     * Print header for stats
     * @param printer Target output PrintStream
     * @param detail Level of detail for each metric (valid values: 0, 1, 2)
     * @param depth Depth of output (valid values: 0, 1, 2)
     */
    public static void printHeader(PrintStream printer, int depth, int detail, String offset, String beforeLastLine, boolean printToFiles)
    {
        for(int d = depth + 3; d > 0; d--){
            printer.format(offset);
            printHeaderSummary(printer, detail, d);
            if(depth > 0)
                printHeaderByGames(printer, detail, d);
            if(depth > 1)
                printHeaderByLevels(printer, detail, d);
            printer.format("\n");
        }
        printer.format(beforeLastLine);
        printHeaderSummary(printer, detail, 0);
        if(depth > 0)
            printHeaderByGames(printer, detail, 0);
        if(depth > 1)
            printHeaderByLevels(printer, detail, 0);
        printer.format("\n");

        if(printToFiles)
            printHeaderFiles(offset, beforeLastLine);
    }
    public static void printHeader() { printHeader(System.out, defaultPrintDepth, defaultPrintDetail, "", "", false); }
    public static void printHeader(int depth, int detail, String offset, String beforeLastLine, boolean printToFiles) { printHeader(System.out, depth, detail, offset, beforeLastLine, printToFiles); }

    public static void printHeaderSummary(PrintStream printer, int detail, int line)
    {
        try {
            if(line == 0){
                for (int m = 0; m < NUM_METRICS; m++) {
                    if (!printIgnoreMetrics[m]) {
                        printer.format("  mean");
                        if (detail > 0) {
                            printer.format("   c%3.0f", confidenceFactor*100);
                        }
                        if (detail > 1) {
                            printer.format(" Gstd");
                            printer.format(" Lstd");
                        }
                        printer.format("  ");
                    }
                }
                printer.format("        ");
            }else if(line == 1){
                for (int m = 0; m < NUM_METRICS; m++) {
                    if (!printIgnoreMetrics[m]) {
                        printer.format("%6d",m);
                        if (detail > 0) {
                            printer.format("       ");
                        }
                        if (detail > 1) {
                            printer.format("     ");
                            printer.format("     ");
                        }
                        printer.format("  ");
                    }
                }
                printer.format("        ");
            }else if(line == 2){
                printer.format("Summary ");
                for (int m = 0; m < NUM_METRICS; m++) {
                    if (!printIgnoreMetrics[m]) {
                        printer.format("      ");
                        if (detail > 0) {
                            printer.format("       ");
                        }
                        if (detail > 1) {
                            printer.format("     ");
                            printer.format("     ");
                        }
                        printer.format("  ");
                    }
                }
            }else{
                for (int m = 0; m < NUM_METRICS; m++) {
                    if (!printIgnoreMetrics[m]) {
                        printer.format("......");
                        if (detail > 0) {
                            printer.format(".......");
                        }
                        if (detail > 1) {
                            printer.format(".....");
                            printer.format(".....");
                        }
                        printer.format("  ");
                    }
                }
                printer.format("        ");
            }
        }
        catch(Exception e) {
            System.out.println(e);
        }
    }

    public static void printHeaderByGames(PrintStream printer, int detail, int line)
    {
        try {
            if(line == 0){
                for (int g = 0; g < resultsLevels.length; g++) {
                    for (int m = 0; m < NUM_METRICS; m++) {
                        if (!printIgnoreMetrics[m]) {
                            printer.format("  mean");
                            if (detail > 0) {
                                printer.format("   c%3.0f", confidenceFactor*100);
                            }
                            if (detail > 1) {
                                printer.format(" Lstd");
                            }
                            printer.format("  ");
                        }
                    }
                    printer.format("  ");
                }
                printer.format("      ");
            }else if(line == 1){
                for (int g = 0; g < resultsLevels.length; g++) {
                    for (int m = 0; m < NUM_METRICS; m++) {
                        if (!printIgnoreMetrics[m]) {
                            printer.format("%6d",m);
                            if (detail > 0) {
                                printer.format("       ");
                            }
                            if (detail > 1) {
                                printer.format("     ");
                            }
                            printer.format("  ");
                        }
                    }
                    printer.format("  ");
                }
                printer.format("      ");
            }else if(line == 2){
                for (int g = 0; g < resultsLevels.length; g++) {
                    printer.format("Game%02d",g+1);
                    boolean compensated = false;
                    for (int m = 0; m < NUM_METRICS; m++) {
                        if (!printIgnoreMetrics[m]) {
                            if(!compensated) {
                                printer.format("  ");
                                compensated = true;
                            }else
                                printer.format("      ");
                            if (detail > 0) {
                                printer.format("       ");
                            }
                            if (detail > 1) {
                                printer.format("     ");
                            }
                            printer.format("  ");
                        }
                    }
                }
                printer.format("      ");
            }else if(line == 3){
                printer.format("ByGames");
                boolean compensated = false;
                for (int g = 0; g < resultsLevels.length; g++) {
                    for (int m = 0; m < NUM_METRICS; m++) {
                        if (!printIgnoreMetrics[m]) {
                            if(!compensated) {
                                printer.format("     ");
                                compensated = true;
                            }else
                                printer.format("      ");
                            if (detail > 0) {
                                printer.format("       ");
                            }
                            if (detail > 1) {
                                printer.format("     ");
                            }
                            printer.format("  ");
                        }
                    }
                    printer.format("  ");
                }
            }else{
                for (int g = 0; g < resultsLevels.length; g++) {
                    for (int m = 0; m < NUM_METRICS; m++) {
                        if (!printIgnoreMetrics[m]) {
                            printer.format("......");
                            if (detail > 0) {
                                printer.format(".......");
                            }
                            if (detail > 1) {
                                printer.format(".....");
                            }
                            printer.format("  ");
                        }
                    }
                    printer.format("  ");
                }
                printer.format("      ");
            }

        }
        catch(Exception e) {
            System.out.println(e);
        }
    }

    public static void printHeaderByLevels(PrintStream printer, int detail, int line)
    {
        try {
            if(line == 0){
                for (int g = 0; g < resultsLevels.length; g++) {
                    for (int l = 0; l < resultsLevels[g].length; l++) {
                        for (int m = 0; m < NUM_METRICS; m++) {
                            if (!printIgnoreMetrics[m]) {
                                printer.format("  mean");
                                if (detail > 0) {
                                    printer.format("   c%3.0f", confidenceFactor*100);
                                }
                                printer.format("  ");
                            }
                        }
                        printer.format("  ");
                    }
                    printer.format("  ");
                }
                printer.format("    ");
            }else if(line == 1){
                for (int g = 0; g < resultsLevels.length; g++) {
                    for (int l = 0; l < resultsLevels[g].length; l++) {
                        for (int m = 0; m < NUM_METRICS; m++) {
                            if (!printIgnoreMetrics[m]) {
                                printer.format("%6d",m);
                                if (detail > 0) {
                                    printer.format("       ");
                                }
                                printer.format("  ");
                            }
                        }
                        printer.format("  ");
                    }
                    printer.format("  ");
                }
                printer.format("    ");
            }else if(line == 2){
                for (int g = 0; g < resultsLevels.length; g++) {
                    for (int l = 0; l < resultsLevels[g].length; l++) {
                        printer.format("Level%1d",l+1);
                        boolean compensated = false;
                        for (int m = 0; m < NUM_METRICS; m++) {
                            if (!printIgnoreMetrics[m]) {
                                if(!compensated) {
                                    printer.format("  ");
                                    compensated = true;
                                }else
                                    printer.format("      ");
                                if (detail > 0) {
                                    printer.format("       ");
                                }
                                printer.format("  ");
                            }
                        }
                    }
                    printer.format("  ");
                }
                printer.format("    ");
            }else if(line == 3){
                for (int g = 0; g < resultsLevels.length; g++) {
                    printer.format("Game%2d",g+1);
                    boolean compensated = false;
                    for (int l = 0; l < resultsLevels[g].length; l++) {
                        for (int m = 0; m < NUM_METRICS; m++) {
                            if (!printIgnoreMetrics[m]) {
                                if(!compensated) {
                                    printer.format("  ");
                                    compensated = true;
                                }else
                                    printer.format("      ");
                                if (detail > 0) {
                                    printer.format("       ");
                                }
                                printer.format("  ");
                            }
                        }
                        printer.format("  ");
                    }
                }
                printer.format("    ");
            }else if(line == 4){
                printer.format("ByLevels");
                boolean compensated = false;
                for (int g = 0; g < resultsLevels.length; g++) {
                    for (int l = 0; l < resultsLevels[g].length; l++) {
                        for (int m = 0; m < NUM_METRICS; m++) {
                            if (!printIgnoreMetrics[m]) {
                                if(!compensated) {
                                    printer.format("  ");
                                    compensated = true;
                                }else
                                    printer.format("      ");
                                if (detail > 0) {
                                    printer.format("       ");
                                }
                                printer.format("  ");
                            }
                        }
                        printer.format("  ");
                    }
                    printer.format("  ");
                }
            }else{
                for (int g = 0; g < resultsLevels.length; g++) {
                    for (int l = 0; l < resultsLevels[g].length; l++) {
                        for (int m = 0; m < NUM_METRICS; m++) {
                            if (!printIgnoreMetrics[m]) {
                                printer.format("......");
                                if (detail > 0) {
                                    printer.format(".......");
                                }
                                printer.format("..");
                            }
                        }
                        printer.format("  ");
                    }
                    printer.format("  ");
                }
                printer.format("    ");
            }
        }
        catch(Exception e) {
            System.out.println(e);
        }
    }



}
