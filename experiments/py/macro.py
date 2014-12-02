import pylab
import numpy as np
import matplotlib.pyplot as plt
from os import listdir
from os.path import isfile, join

import operator
from matplotlib.pyplot import errorbar

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
        rects[i] = ax.bar(ind+width*i,avg[j][i],width,color=colors[i],yerr=err[j][i], edgecolor='black', hatch=hatches[i], ecolor='black')
        height = rects[i][0].get_height()
        text = '%.2f (%.2f)'%(avg[j][i],err[j][i])
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
    else:
        fig.savefig(output_file)


###-- Define input data --###

###-- FOLDER: leftRight

testOne = False

mypath = '../someResults/'

if testOne:
    filenames = ['eggomania_lvl0_MacroOLMCTS.Agent.txt']
else:
    filenames = [ f for f in listdir(mypath) if (isfile(join(mypath,f)) and (not f.startswith('.')) ) ]

###-- User config section --###

numMacroLengths = 4
NumRepetitions = 100
macros = [1,2,3,5]
TotGames = numMacroLengths * NumRepetitions

#Init memory structures

averages = [[] for x in xrange(len(filenames))]
std_devs = [[] for x in xrange(len(filenames))]
std_errs = [[] for x in xrange(len(filenames))]
games = []

allWinAverages = [[] for x in xrange(len(filenames))] #[0] * len(filenames)
allTimeStepsAverages = [[] for x in xrange(len(filenames))]
allScoresAverages = [[] for x in xrange(len(filenames))]


allWinStdErr = [[] for x in xrange(len(filenames))] #[0] * len(filenames)
allTimeStepsStdErr = [[] for x in xrange(len(filenames))]
allScoresStdErr = [[] for x in xrange(len(filenames))]

for j in xrange(len(filenames)):

    filename = filenames[j]

    p = filename.index("_")
    games.append(filename[0:p])

    datafile = '../someResults/' + filename

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
            timeSteps[macroL].append(timeS)

            nRow+=1

        for k in range(numMacroLengths):

            allWinAverages[j].append(np.average(victories[k]))
            allWinStdErr[j].append(np.std(victories[k]) / np.sqrt(len(victories[k])))

            allScoresAverages[j].append(np.average(scores[k]))
            allScoresStdErr[j].append(np.std(scores[k]) / np.sqrt(len(scores[k])))

            allTimeStepsAverages[j].append(np.average(timeSteps[k]))



        drawBars(allWinAverages, allWinStdErr, 'Average of victories', [0,1.5], "../someResults/out/"+games[j]+"_ratewin.pdf")
        drawBars(allScoresAverages, allScoresStdErr, 'Average of score', None, "../someResults/out/"+games[j]+"_score.pdf")

        # #Create a figure
        # fig = pylab.figure()
        #
        # #Add a subplot (Grid of plots 1x1, adding plot 1)
        # ax = fig.add_subplot(111)
        #
        # ind = np.arange(1)
        # width = 0.2
        # colors = ['w','grey','w','black']
        # hatches = ['//',':',':','.']
        # rects = [0 for x in range(numMacroLengths)]
        # for i in range(numMacroLengths):
        #     #rects[i] = ax.bar(ind+width*i,allWinAverages[j][i],width,color=colors[i],yerr=err_plot[i], edgecolor='black', hatch=hatches[i], ecolor='black')
        #     rects[i] = ax.bar(ind+width*i,allWinAverages[j][i],width,color=colors[i],yerr=allWinStdErr[j][i], edgecolor='black', hatch=hatches[i], ecolor='black')
        #     height = rects[i][0].get_height()
        #     text = '%.2f (%.2f)'%(allWinAverages[j][i],allWinStdErr[j][i])
        #     plt.text(rects[i][0].get_x()+rects[i][0].get_width()/2., 1.1*height, text, ha='center', va='bottom')
        #     #print allWinAverages[j][i]
        #
        # ax.set_xticks(ind+width*2)
        # maps_txt = [games[j]]
        # ax.set_xticklabels(maps_txt)
        #
        #
        # plt.legend(macros,  shadow=True, fancybox=True, loc=1)
        #
        # #Titles and labels
        # plt.title('Average of victories')
        # plt.xlabel("Macro-action length")
        # plt.ylabel("Average")
        #
        # plt.ylim([0,1.5])
        #
        # if testOne:
        #     plt.show()
        # else:
        #     fig.savefig("../someResults/out/"+games[j]+".pdf")

