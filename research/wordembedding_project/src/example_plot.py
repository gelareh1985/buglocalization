'''
Created on Mar 5, 2021

@author: Gelareh_mp
'''
import matplotlib.pyplot as plt
import numpy as np

# Creating dataset 
np.random.seed(150) 
data = np.random.normal(150, 25, 250) 

fig = plt.figure(figsize =(10, 7)) 

# Creating plot 
plt.boxplot(data) 

# show plot 
plt.show()

# Creating dataset 
np.random.seed(10) 

data_1 = np.random.normal(80, 30, 200) 
data_2 = np.random.normal(90, 20, 200) 
data_3 = np.random.normal(100, 10, 200) 
data_4 = np.random.normal(70, 40, 200) 
data_5 = np.random.normal(55, 25, 200)
data = [data_1, data_2, data_3, data_4, data_5] 

fig = plt.figure(figsize =(10, 7)) 

# Creating axes instance 
ax = fig.add_axes([0, 0, 1, 1]) 

# Creating plot 
bp = ax.boxplot(data) 

# show plot 
plt.show() 

value1 = [82,76,24,40,67,62,75,78,71,32,98,89,78,67,72,82,87,66,56,52]
value2=[62,5,91,25,36,32,96,95,3,90,95,32,27,55,100,15,71,11,37,21]
value3=[23,89,12,78,72,89,25,69,68,86,19,49,15,16,16,75,65,31,25,52]
value4=[59,73,70,16,81,61,88,98,10,87,29,72,16,23,72,88,78,99,75,30]
value5=[30,59,73,70,16,81,61,88,98,10,87,29,72,16,23,72,88,78,99,75]

box_plot_data=[value1,value2,value3,value4,value5]
plt.title("Distribution of Growth of Fruits")
plt.boxplot(box_plot_data,patch_artist=True,labels=['Apple','Mango','Strawberry','Orange','Watermelon'])
plt.show()

# Creating dataset 
np.random.seed(10) 
data_1 = np.random.normal(60, 30, 450) 
data_2 = np.random.normal(190, 20, 450) 
data_3 = np.random.normal(80, 50, 450) 
data_4 = np.random.normal(100, 40, 450) 
data = [data_1, data_2, data_3, data_4] 

fig = plt.figure(figsize =(10, 7)) 
ax = fig.add_subplot(111) 

# Creating axes instance 
bp = ax.boxplot(data, patch_artist = True, notch ='True', vert = 0) 

colors = ['purple', 'cyan', 'lime', 'blue'] 

for patch, color in zip(bp['boxes'], colors): patch.set_facecolor(color) 

# changing color and linewidth of whiskers 
for whisker in bp['whiskers']: whisker.set(color ='#8B008B', linewidth = 1.5, linestyle =":") 

# changing color and linewidth of caps 
for cap in bp['caps']: cap.set(color ='#8B008B', linewidth = 2) 

# changing color and linewidth of medians 
for median in bp['medians']: median.set(color ='red', linewidth = 3) 

# changing style of fliers 
for flier in bp['fliers']: flier.set(marker ='D', color ='#e7298a', alpha = 0.5) 

# x-axis labels 
ax.set_yticklabels(['data_1', 'data_2', 'data_3', 'data_4']) 

# Adding title 
plt.title("Customized Box Plot with Labels") 

# Removing top axes and right axes ticks 
ax.get_xaxis().tick_bottom() 
ax.get_yaxis().tick_left() 

# show plot 
plt.show()
