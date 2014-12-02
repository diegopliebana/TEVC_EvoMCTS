import pylab
import scipy
import numpy as math
import matplotlib.pyplot as plt
from matplotlib.pyplot import errorbar
from scipy.stats.mstats import kruskalwallis as anova
from scipy.stats import shapiro as normality_test
from scipy.stats import mannwhitneyu as nonparam
from scipy.stats import ttest_ind as ttest
import csv
import os


def numWaypoints(map):
    if(map in [0,3,7,11,12,14,17]):
        return 30
    elif(map in [2,5,6,9,10,13,15,18]):
        return 40 
    elif(map in [1,4,8,16,19]):
        return 50
    return -1

NumberOfMaps = 20
MatchesPerMap = 10

N = NumberOfMaps + 1
M = MatchesPerMap + 1
scores = [[] for i in range(N)]
averages = [[] for i in range(N)]
stddevs = [[] for i in range(N)]
stderrors = [[] for i in range(N)]

#overallScores = [[0 for i in range(N)],[0 for i in range(N)]]
k = -1;

results_dir = "data"
waypointsVariable = True

#allFiles = os.listdir(results_dir)
#comparing = ['CMA-15','GA-15', 'MCTS-15', 'UCT-15']
#allFiles = ['CMA-10.csv','CMA-15.csv','CMA-20.csv','GA-10.csv','GA-15.csv','GA-20.csv',
#           'MCTS-10.csv','MCTS-15.csv','MCTS-20.csv','UCT-10.csv','UCT-15.csv','UCT-20.csv',] 
 
allFiles = ['csv_MCTS-8-15-SteppingEv-FloodFillTSP.csv', 'csv_MCTS-8-15-SteppingEv-TSPNearest.csv', 
           'csv_MCTS-12-10-SteppingEv-FloodFillTSP.csv', 'csv_MCTS-6-20-SteppingEv-FloodFillTSP.csv']
#allFiles = ['csv_MCTS-8-15-SteppingEv-FloodFillTSP.csv', 'csv_DFS-4-15-SteppingEv-FloodFillTSP.csv', 
#           'csv_MC-12-10-SteppingEv-FloodFillTSP.csv']

mal = [15]

overallScores = []
for item in range(len(allFiles)):
    overallScores.append([0 for i in range(N)])

for files in allFiles:
    if files.endswith(".csv"):
        k+=1;
        print results_dir + '/' + files + str(k)
        csvReader = csv.reader(open(results_dir + '/' + files, 'rb'), delimiter=',');
        #l_experim
        experiment = [[] for i in range(N)]
        i = 1;
        j = 1;
        for row in csvReader:
             
            #score = 1000000*float(row[0])-float(row[1])
            #if(int(row[0]) == 0): 
            #    row[0] = 0.00001;
            #score = float(row[1])/float(row[0])
            
            thisMap = int((j-1)/MatchesPerMap)
            toCompare = numWaypoints(thisMap)
                
            
            if(int(row[1]) == toCompare):
            #if(int(row[1]) > toCompare*0.5):
                
                score = float(row[2])/float(row[1])
                
                #print k, i, j, ":", score
                overallScores[k][i] +=score
                
                experiment[0].append(score)
                experiment[i].append(score)
            else:
                print 'Skipping match', j, 'of run', files, ', waypoints:', row[1]    
                
            if(j%MatchesPerMap==0):
                if(len(experiment[i]) == 0):
                    experiment[i].append(0)
                scores[i].append(experiment[i])
                averages[i].append([])
                stddevs[i].append([])
                stderrors[i].append([])
                
                i+=1
                
            j+=1;
        
        g,p_normal = normality_test(experiment[0])
        normal = "not normal"
        scores[0].append(experiment[0]);
        averages[0].append([])
        stddevs[0].append([])
        stderrors[0].append([])        
        
#sc = []        
for i in xrange(1,len(scores)):
    m = min(len(scores[i][0]),len(scores[i][1]),len(scores[i][2]),len(scores[i][3]))
    if(m == 0):
        m = 1
    
    #CHANGE HERE TO ADD MORE ALGORITHMS:
    sc = [[0 for x in range(m)],[0 for x in range(m)],[0 for x in range(m)],[0 for x in range(m)]]

    for j in range(m):
        #LINE PER ALGORITHM:
        sc[0][j] = scores[i][0][j]
        sc[1][j] = scores[i][1][j] 
        sc[2][j] = scores[i][2][j]
        sc[3][j] = scores[i][3][j] 

#for i in range(len(scores)):
    for j in range(len(allFiles)):
        #=======================================================================
        # av = math.average(scores[i][j])
        # stddev = math.std(scores[i][j])
        # stderr = stddev / math.sqrt(len(scores[i][j]));
        # averages[i][j] = av 
        # stddevs[i][j] = stddev
        # stderrors[i][j] = stderr
        #=======================================================================
        if (len(sc[j]) > 0):
            av = math.average(sc[j])
            stddev = math.std(sc[j])
            stderr = stddev / math.sqrt(len(sc[j]))
        else:
            av = 0
            stddev = 0
            stderr = 0
                    
        averages[i][j] = av 
        stddevs[i][j] = stddev
        stderrors[i][j] = stderr



#Create a figure
fig = pylab.figure()

#Add a subplot (Grid of plots 1x1, adding plot 1)
ax = fig.add_subplot(111)  
 
   
avg_plot = []
err_plot = []
NumberOfMapsInPlot = 20
StartingMapPlot = 0
N_ALGORITHMS = 4   
for i in range(N_ALGORITHMS):
    avg_plot.append([0 for x in range(NumberOfMapsInPlot)])
    err_plot.append([0 for x in range(NumberOfMapsInPlot)])
    for j in range(NumberOfMapsInPlot):
        avg_plot[i][j] = averages[StartingMapPlot+j+1][i]
        err_plot[i][j] = stderrors[StartingMapPlot+j+1][i]
        
#THIS IS TO PLOT BARS
ind = math.arange(NumberOfMapsInPlot)  
width = 0.2  
colors = ['w','grey','w','black']
hatches = ['//',':',':','.']
rects = [0 for x in range(N_ALGORITHMS)]
for i in range(N_ALGORITHMS):
    rects[i] = ax.bar(ind+width*i,avg_plot[i],width,color=colors[i],yerr=err_plot[i], edgecolor='black', hatch=hatches[i], ecolor='black')
    
ax.set_xticks(ind+width*2)
maps_txt = []
for j in range(NumberOfMapsInPlot):
    #ss = 'Map ' + str(StartingMapPlot+j+1)
    ss = str(StartingMapPlot+j+1)
    maps_txt.append(ss)

ax.set_xticklabels(maps_txt)
    

##Add the legend
plt.legend(('MCTS-8-15-Stepping-PEFF', 'MCTS-8-15-Stepping-Near', 'MCTS-12-10-Stepping-PEFF', 'MCTS-6-20-Stepping-PEFF'),  shadow=True, fancybox=True, loc=2)
#plt.legend(('MCTS-8-15-Stepping-PEFF', 'MCTS-8-15-Stepping-Near', 'MCTS-12-10-Stepping-PEFF'),  shadow=True, fancybox=True, loc=2)
#plt.legend(('MCTS-8-15-Stepping-PEFF', 'DFS-4-15-SteppingEv-PEFF', 'MC-12-10-SteppingEv-PEFF'),  shadow=True, fancybox=True, loc=2) 

#Titles and labels
plt.title('Ratio per map')
plt.xlabel("Map")
plt.ylabel("Ratio")

#plt.xlim([8,22])
plt.ylim([0,200]) #175

fig.set_size_inches(15,5)
fig.savefig("inout/barsBest4.pdf")
fig.savefig("inout/barsBest4.ps")

# And show it:
plt.show()
