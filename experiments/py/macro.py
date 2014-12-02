import pylab
import numpy as np
import matplotlib.pyplot as plt
from os import listdir
from os.path import isfile, join

import operator
from matplotlib.pyplot import errorbar


testOne = False
doPdf = True
#dataFilesPath = '../macroTests/bandit1235/'
#numMacroLengths = 1
#NumRepetitions = 100
#macros = [1]

dataFilesPath = '../macroTests/'
numMacroLengths = 4
NumRepetitions = 100
macros = [1,2,3,5]


def errorfill(x, y, yerr, color=None, alpha_fill=0.3, ax=None):
    ax = ax if ax is not None else plt.gca()
    if color is None:
        color = ax._get_lines.color_cycle.next()
    if np.isscalar(yerr) or len(yerr) == len(y):
        ymin = [a - b for a,b in zip(y, yerr)] 
        ymax = [sum(pair) for pair in zip(y, yerr)] 
    elif len(yerr) == 2:
        ymin, ymax = yerr

    ax.plot(x, y, color=color)
    print ymin
    print y
    print ymax
    ax.fill_between(x, ymax, ymin, color=color, alpha=alpha_fill)


def drawBars(avg, err, title, ylim, output_file):
    #Create a figure
    fig = pylab.figure()

    #Add a subplot (Grid of plots 1x1, adding plot 1)
    ax = fig.add_subplot(111)

    ind = np.arange(1)
    width = 0.2
    colors = ['w','grey','w','black']
    hatches = ['//',':',':','.']
    rects = [0 for x in range(numMacroLengths)]
    for i in range(numMacroLengths):

        val = 0
        val_err = 0
        if i < len(avg[j]):
            val = avg[j][i]
            val_err = err[j][i]

        rects[i] = ax.bar(ind+width*i, val, width, color=colors[i], yerr=val_err, edgecolor='black', hatch=hatches[i], ecolor='black')

        height = rects[i][0].get_height()
        text = '%.2f (%.2f)'%(val,val_err)
        plt.text(rects[i][0].get_x()+rects[i][0].get_width()/2., 1.05*height, text, ha='center', va='bottom')
        #print allWinAverages[j][i]

    ax.set_xticks(ind+width*2)
    maps_txt = [games[j]]
    ax.set_xticklabels(maps_txt)

    plt.legend(macros,  shadow=True, fancybox=True, loc=1)

    #Titles and labels
    plt.title(title)
    plt.xlabel("Macro-action length")
    plt.ylabel("Average (Std. error)")

    if ylim != None:
        plt.ylim(ylim)

    if testOne:
        plt.show()
    elif doPdf:
        fig.savefig(output_file)


###-- Define input data --###

###-- FOLDER: leftRight



if testOne:
    filenames = ['eggomania_lvl0_MacroOLMCTS.Agent.txt']
else:
    filenames = [ f for f in listdir(dataFilesPath) if (isfile(join(dataFilesPath,f)) and (not f.startswith('.')) ) ]

###-- User config section --###

TotGames = numMacroLengths * NumRepetitions

#Init memory structures

averages = [[] for x in xrange(len(filenames))]
std_devs = [[] for x in xrange(len(filenames))]
std_errs = [[] for x in xrange(len(filenames))]
games = []

totalWins = []

allWinAverages = [[] for x in xrange(len(filenames))] #[0] * len(filenames)
allTimeStepsAverages = [[] for x in xrange(len(filenames))]
allScoresAverages = [[] for x in xrange(len(filenames))]


allWinStdErr = [[] for x in xrange(len(filenames))] #[0] * len(filenames)
allScoresStdErr = [[] for x in xrange(len(filenames))]
allTimeStepsStdErr = [[] for x in xrange(len(filenames))]

for j in xrange(len(filenames)):

    filename = filenames[j]

    p = filename.index("_")
    games.append(filename[0:p])

    datafile = dataFilesPath + filename

    print 'loading', datafile
    r = pylab.loadtxt(datafile, comments='#', delimiter=',')

    if len(r) != TotGames:
        print "File:", filename, "experiment not finished."
    else:
        #print "File:", filename, "experiment ended."
        victories = [[] for x in range(numMacroLengths)]
        scores = [[] for x in range(numMacroLengths)]
        timeSteps = [[] for x in range(numMacroLengths)]

        nRow = 0

        for row in r:
            macroL = nRow / NumRepetitions
            vict = row[3]
            timeS = row[2]
            score = row[4]

            victories[macroL].append(vict)
            scores[macroL].append(score)
            if vict == 1:
                timeSteps[macroL].append(timeS)

            totalWins.append(vict)

            nRow+=1

        for k in range(numMacroLengths):

            allWinAverages[j].append(np.average(victories[k]))
            allWinStdErr[j].append(np.std(victories[k]) / np.sqrt(len(victories[k])))

            allScoresAverages[j].append(np.average(scores[k]))
            allScoresStdErr[j].append(np.std(scores[k]) / np.sqrt(len(scores[k])))

            if len(timeSteps[k]) > 0:
                allTimeStepsAverages[j].append(np.average(timeSteps[k]))
                allTimeStepsStdErr[j].append(np.std(timeSteps[k]) / np.sqrt(len(timeSteps[k])))
            else:
                allTimeStepsAverages[j].append(0)
                allTimeStepsStdErr[j].append(0)



        drawBars(allWinAverages, allWinStdErr, 'Average of victories', [0,1.5], dataFilesPath+"out/"+games[j]+"_ratewin.pdf")
        drawBars(allScoresAverages, allScoresStdErr, 'Average of score', None, dataFilesPath+"out/"+games[j]+"_score.pdf")
        drawBars(allTimeStepsAverages, allTimeStepsStdErr, 'Average of time steps', None, dataFilesPath+"out/"+games[j]+"_timesSteps.pdf")


text = 'Total wins average: %.2f (%.2f)'%(np.average(totalWins),np.std(totalWins) / np.sqrt(len(totalWins)))
print text