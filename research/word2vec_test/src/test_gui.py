# import matplotlib.pyplot as plt
# from word_to_vec import generate_dictinoary_data
# from word_to_vec import generate_training_data
# from word_to_vec import train
# from word_to_vec import word_similarity_scatter_plot
# from word_to_vec import plot_epoch_loss
# 
path_to_save = r'D:\\workspaces\\buglocalization\\word2vec_test\\output_images_sample_data\\epoc_loss_plot\\'
path_to_save_scatter_plot =  r'D:\\workspaces\\buglocalization\\word2vec_test\\output_images_sample_data\\word_similarity_scatter_plot\\'
# 
# from tkinter import *
# import tkinter as tk
# from matplotlib.backends.backend_tkagg import FigureCanvasTkAgg
# from matplotlib.figure import Figure
import analysis_small_corpus as analys1
from PIL import Image, ImageTk
# #  
# # root = Tk()
# # #root.geometry("500x500")
# # root.title("Word2Vec Demo!")
# # 
# # variable1 = StringVar()  # Value saved here
# # text=[]
# # 
# # group_1 = ttk.LabelFrame(root, padx=15, pady=10,text="Small Corpus Example")
# # group_1.pack(padx=10, pady=5)
# # 
# #  
# # ttk.Label(group_1, text="Insert Text: ").grid(row=0)
# # ttk.Entry(group_1,textvariable=variable1).grid(row=0, column=1, sticky=W)
# #  
# # def myClick1():
# #     newWindow = Toplevel(root)  
# #     newWindow.title("New Window")  
# #     newWindow.geometry("200x200") 
# #     Label(newWindow,text ="This is a new window").pack()
# #          
# #     text.append(variable1.get()) 
# #     print(text)
# #     if len(variable1.get())!=0:
# #         #fig=analys1.sketch_analysis1(text)
# #         fig = new_figure()
# #         show_figure(fig)
# # #         canvas= FigureCanvasTkAgg(fig,master=newWindow) 
# # #         canvas.get_tk_widget().pack
# # #         canvas.get_tk_widget().pack(side=ttk.TOP, fill=ttk.BOTH,expand=1) 
# # #         canvas.draw()
# #         figure = Figure(figsize=(5, 4), dpi=100)
# #         plot = figure.add_subplot(1, 1, 1)
# #         canvas = FigureCanvasTkAgg(figure, root)
# #         canvas.get_tk_widget().grid(row=0, column=0)
# # ttk.btn1=Button(group_1, text = "Dim Plot",command=myClick1).grid(row=0, column=2, sticky=E)
# # 
# # def new_figure(): 
# #     #     fig = plt.figure()
# #     #     plt.plot([0, 1], [2, 3])
# #     #     plt.close(fig)
# #     #     return fig 
# #     window_size = 2
# #     epochs = 100
# #     learning_rate = 0.01
# #     text = ["Best way to success is through hardwork and persistence"]
# #     
# #     word_to_index,index_to_word,corpus,vocab_size,length_of_corpus = generate_dictinoary_data(text)
# #     training_data,training_sample_words = generate_training_data(corpus,window_size,vocab_size,word_to_index,length_of_corpus)
# #     
# #     dimensions = [5,10,15,20]
# #     loss_epoch = {}
# #     fig, axes = plt.subplots(nrows=2, ncols=2,figsize=(10,10))
# #     fig.suptitle("Plots for showing paramaters with varying dimension", fontsize=16)
# #     row=0
# #     col=0
# #     for dim in dimensions:
# #         
# #         epoch_loss,weights_1,weights_2 = train(dim,vocab_size,epochs,training_data,learning_rate)
# #         loss_epoch.update( {dim: epoch_loss} )
# #         
# #         word_similarity_scatter_plot(
# #             index_to_word,
# #             weights_1[epochs -1],
# #             'dimension_' + str(dim) + '_epochs_' + str(epochs) + '_window_size_' +str(window_size),
# #             fig,
# #             axes[row][col]
# #         )
# #         if col == 1:
# #             row += 1
# #             col = 0
# #         else:
# #             col += 1
# #     
# #     # plt.savefig(path_to_save_scatter_plot+'varying_dim' +'.png')        
# #     # plt.show()
# #     
# #     outfig=plot_epoch_loss('dim:',loss_epoch,'epochs_' + str(epochs) + '_window_size_' +str(window_size),path_to_save)
# #     return outfig
# #  
# # def show_figure(fig):
# #     # create a dummy figure and use its
# #     # manager to display "fig"
# #     dummy = plt.figure()
# #     new_manager = dummy.canvas.manager
# #     new_manager.canvas.figure = fig
# #     fig.set_canvas(new_manager.canvas)
# #     plt.show()
# #     
# 
# root.mainloop()    
# if __name__ == '__main__':
# 
#     fig = new_figure()
#     show_figure(fig)
#     plt.show()

from tkinter import *
import tkinter as tk
from pandas import DataFrame
import matplotlib.pyplot as plt
from matplotlib.backends.backend_tkagg import FigureCanvasTkAgg

data1 = {'Country': ['US','CA','GER','UK','FR'],
         'GDP_Per_Capita': [45000,42000,52000,49000,47000]
        }
df1 = DataFrame(data1,columns=['Country','GDP_Per_Capita'])


data2 = {'Year': [1920,1930,1940,1950,1960,1970,1980,1990,2000,2010],
         'Unemployment_Rate': [9.8,12,8,7.2,6.9,7,6.5,6.2,5.5,6.3]
        }
df2 = DataFrame(data2,columns=['Year','Unemployment_Rate'])


data3 = {'Interest_Rate': [5,5.5,6,5.5,5.25,6.5,7,8,7.5,8.5],
         'Stock_Index_Price': [1500,1520,1525,1523,1515,1540,1545,1560,1555,1565]
        }  
df3 = DataFrame(data3,columns=['Interest_Rate','Stock_Index_Price'])
 

root= tk.Tk() 

group_1 = tk.LabelFrame(root, padx=15, pady=10,text="Small Corpus Example")
group_1.pack(padx=10, pady=5)

variable1 = StringVar() 
text=[]

tk.Label(group_1, text="Insert Text: ").grid(row=0)
tk.Entry(group_1,textvariable=variable1).grid(row=0, column=1, sticky=W)

  
def myClick1():
    newWindow = Toplevel(root)  
    newWindow.title("New Window")  
    newWindow.geometry("200x200") 
    Label(newWindow,text ="This is a new window").pack()
          
    text.append(variable1.get()) 
    print(text)
    if len(variable1.get())!=0:
        #fig=analys1.sketch_analysis1(text)
#     
#         scatter3 = FigureCanvasTkAgg(fig, root) 
#         scatter3.get_tk_widget().pack(side=tk.LEFT, fill=tk.BOTH)
#         scatter3.show()
        load = Image.open(path_to_save_scatter_plot)
        render = ImageTk.PhotoImage(load)
        img = Label(newWindow, image=render)
        img.image = render
        img.place(x=0, y=0)
          
tk.btn1=Button(group_1, text = "Dim Plot",command=myClick1).grid(row=0, column=2, sticky=E)

figure1 = plt.Figure(figsize=(6,5), dpi=100)
ax1 = figure1.add_subplot(111)
bar1 = FigureCanvasTkAgg(figure1, root)
bar1.get_tk_widget().pack(side=tk.LEFT, fill=tk.BOTH)
df1 = df1[['Country','GDP_Per_Capita']].groupby('Country').sum()
df1.plot(kind='bar', legend=True, ax=ax1)
ax1.set_title('Country Vs. GDP Per Capita')

figure2 = plt.Figure(figsize=(5,4), dpi=100)
ax2 = figure2.add_subplot(111)
line2 = FigureCanvasTkAgg(figure2, root)
line2.get_tk_widget().pack(side=tk.LEFT, fill=tk.BOTH)
df2 = df2[['Year','Unemployment_Rate']].groupby('Year').sum()
df2.plot(kind='line', legend=True, ax=ax2, color='r',marker='o', fontsize=10)
ax2.set_title('Year Vs. Unemployment Rate')

figure3 = plt.Figure(figsize=(5,4), dpi=100)
ax3 = figure3.add_subplot(111)
ax3.scatter(df3['Interest_Rate'],df3['Stock_Index_Price'], color = 'g')
scatter3 = FigureCanvasTkAgg(figure3, root) 
scatter3.get_tk_widget().pack(side=tk.LEFT, fill=tk.BOTH)
ax3.legend(['Stock_Index_Price']) 
ax3.set_xlabel('Interest Rate')
ax3.set_title('Interest Rate Vs. Stock Index Price')

root.mainloop()