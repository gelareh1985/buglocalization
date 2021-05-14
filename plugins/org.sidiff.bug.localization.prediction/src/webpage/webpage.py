import os
import json
import pandas as pd
from pathlib import Path
import numpy as np

print("Hello World!")
########################################################################################
# *********************************** functions ****************************************
########################################################################################
# define functions ###


def get_bug_reports_info_json(inputfile):
    list_TracedVersion = []
    list_TracedBugReport = []
    list_BugReportComment = []
    list_Class = []

    for node in inputfile:
        if node['__labels__'] == ":TracedVersion":
            listnodes = get_attributes(node)
            list_TracedVersion.append(listnodes)
            
        elif node['__labels__'] == ":TracedBugReport":
            listnodes = get_attributes(node)
            list_TracedBugReport.append(listnodes)

        elif node['__labels__'] == ":BugReportComment":
            listnodes = get_attributes(node)
            list_BugReportComment.append(listnodes)

        elif node['__labels__'] == ":Class":
            listnodes = get_attributes(node)
            list_Class.append(listnodes)
    return list_TracedVersion, list_TracedBugReport, list_BugReportComment, list_Class


def get_attributes(node):
    nodes = []
    nodeattributes = ['__db__id__', '__initial__version__', '__labels__', '__last__version__', '__model__element__id__',
                      'author', 'codeVersionID', 'commitMessage', 'date', 'modelVersionID', 'assignedTo', 'bugfixCommit',
                      'bugfixTime', 'component', 'visibility', 'creationTime', 'id', 'product', 'resolution', 'severity',
                      'status', 'summary', 'creator', 'text', 'isAbstract', 'isActive', 'isFinalSpecialization', 'isLeaf', 'name']
    #print(nodeattributes)
    for attribute in nodeattributes:
        if attribute in node:
            nodes.append((attribute, node[attribute]))
    return nodes       

#7512_bug544362_15167eb4a4284d5fed7aa5bc00d7379673b4a23d_prediction.csv
#bug545475_409689eaea4fbd78315894caf8741a7fad9a693a_prediction.csv


def get_prediction_info_csv(inputfilepath):
    # list_df = []
    # for file_name in os.listdir(inputfilepath):
    # if file_name.endswith("_prediction.csv"):
    df = pd.read_csv(inputfilepath, delimiter=";")
    # list_df.append(df)
            
    return df


def get_image_page_path(inputpath,indx):
    image_path = ""
    default_imgurl = inputpath + "diagram_default.html"
    imgurl = inputpath + "diagram0" + str(indx + 1) + ".html"
    if checkurl(imgurl) is True:
        image_path = imgurl
    else:
        image_path = default_imgurl
    return image_path  


def checkurl(path):
    if os.path.isfile(path):
        return True
    else:
        return False    


def get_file_names(fpath):
    # list_filenames = []
    # fname = os.path.basename(filename)
    # fnamestr = os.path.splitext(fname)[0]
    # list_filenames.append(fnamestr)
    myfile = Path(fpath)
    fname = myfile.stem 
     
    return fname
########################################################################################
# *********************************** page info ****************************************
########################################################################################
# define tags ###


html_tag = """<html>
"""
body_tag = """<body>
"""

html_end_tag = """</html>
"""
body_end_tag = """</body>
"""
begin_tr_tag = """<tr>
"""
end_tr_tag = """</tr>
"""

begin_table_tag = """<table>
"""
end_table_tag = """</table>
"""
table0_header = """ 
<th colspan="2" style="text-align:center; font-size: 20px;"> List of Bug Reports </th>
<tr> <th style="background-color: #ff9999;"> Bug Report Number </th> <th style="background-color: #ff9999;"> Summary </th> </tr>

"""

main_page_style_string = """
<style>
html{
    font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif ;
    font-size: 17px;
    display: table;
}
table,th,td {
  border: 1px solid black;
  width: 1200;
  text-align:justify;
  word-break:break-all;
}

th{
background-color: #d19fe8;
font-size: 16px;
text-align:left;
}

td{
background-color: #FFD580;
font-size: 14px;
}

</style>
"""
# define inside of tags ###
style_string = """
<style>
html{
    font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif ;
    font-size: 17px;
    display: table;
}
table,th,td {
  border: 1px solid black;
  width: 1200;
  text-align:justify;
  word-break:break-all;
}

th{
background-color: lightskyblue;
font-size: 16px;
border: none;
text-align:left;
}

td{
background-color: #d5e3c8;
font-size: 14px;
}

.container {
  position: relative;
  text-align:center;
  padding-top: 70%;
}

.responsive-iframe {
  position: absolute;
  top: 0;
  left: 0;
  bottom: 0;
  right: 0;
  width: 100%;
  height: 100%;
  border: 2px solid #73AD21;
}

</style>
"""
page_header_string = """
<h2>Bug report Information</h2>
"""
########################################################################################
# ********************************** load json file ************************************
########################################################################################
# read Json file ###
jdtstr = "eclipse.jdt.core_data-2021-03-25_model-2021-04-06_evaluation_k2_undirected_aggregated/"
fpath_bugreport = r"D:/buglocalization_gelareh_home/evaluations/latest_version/original_predictions/"
fpath_predictions_classifiers = r"D:/buglocalization_gelareh_home/evaluations/latest_version/original_predictions/"
fpath_predictions_classdiagrams = r"D:/buglocalization_gelareh_home/evaluations/latest_version/aggregated/" + jdtstr

#fname = "7508_bug545475_409689eaea4fbd78315894caf8741a7fad9a693a_nodes.json"
#f = open(fpath_bugreport + fname, 'r')
#C:\Users\
image_page_path = "C:/Users/gelareh/git/buglocalization/plugins/org.sidiff.bug.localization.prediction/src/webpage/image_html_pages/"
output_pages_path = "C:/Users/gelareh/git/buglocalization/plugins/org.sidiff.bug.localization.prediction/src/webpage/bugreport_pages/"
#filepath = fpath_bugreport + fname
list_tracedversions = []
list_tracedbugs = []
list_bugcomments = []
list_classes = []
list_filenames = []
for file_name in os.listdir(fpath_bugreport):
    if file_name.endswith("_nodes.json"):
        #print(fpath_bugreport + filename)
        with open(fpath_bugreport + file_name,"r") as f:
            # load Json file
            loaded_file = json.load(f)
            tracedversions, tracedbugs, bugcomments, classes = get_bug_reports_info_json(loaded_file)
            list_tracedversions.append(tracedversions)
            list_tracedbugs.append(tracedbugs)
            list_bugcomments.append(bugcomments)
            list_classes.append(classes)
            myfilename = get_file_names(fpath=file_name)
            list_filenames.append(myfilename)
        #f.close()
# print("\n\n list traced version node attributes: ",tracedversions)
# print("\n\n list traced bugs attributes: ",tracedbugs)
# print("\n\n list bug comments attributes: ",bugcomments)
# print("\n\n list bug classes attributes: ",classes)
list_classdiagrams_names = []
for file_name in os.listdir(fpath_predictions_classdiagrams):
    if file_name.endswith("_prediction.csv"):
        classdiagram_name = get_file_names(fpath=file_name)
        list_classdiagrams_names.append(classdiagram_name)

list_classifiers_names = []
for file_name in os.listdir(fpath_predictions_classifiers):
    if file_name.endswith("_prediction.csv"):
        classifier_name = get_file_names(fpath=file_name)  
        list_classifiers_names.append(classifier_name)
       
fname_predictions_exisiting_bugreports = []    
for cname in list_classifiers_names:
    if cname in list_classdiagrams_names:
        fname_predictions_exisiting_bugreports.append(cname)
    else:
        fname_predictions_exisiting_bugreports.append("not_found")   

df_predictions_classdiagrams = []
df_predictions_classifiers = []
empty_df = pd.DataFrame() 
for fname in fname_predictions_exisiting_bugreports:
    if fname != "not_found":
        fpath_classdiagrams = fpath_predictions_classdiagrams + fname + ".csv"
        fpath_classifiers = fpath_predictions_classifiers + fname + ".csv"
        predictions_classdiagrams = get_prediction_info_csv(fpath_classdiagrams)
        predictions_classifiers = get_prediction_info_csv(fpath_classifiers)
        df_predictions_classdiagrams.append(predictions_classdiagrams)
        df_predictions_classifiers.append(predictions_classifiers)
    elif fname == "not_found":    
        df_predictions_classdiagrams.append(empty_df)
        df_predictions_classifiers.append(empty_df)

toppredictions_classdiagrams_locs = []
toppredictions_classdiagrams_index = []
toppredictions_classdiagrams_modelelement = []
for df in df_predictions_classdiagrams:
    if df.empty:
        list_diags_locs = "not_found"
        list_diags_index = "not_found"
        list_diags_modelelement = "not_found"
        toppredictions_classdiagrams_locs.append(list_diags_locs)
        toppredictions_classdiagrams_index.append(list_diags_index)
        toppredictions_classdiagrams_modelelement.append(list_diags_modelelement)
    elif not df.empty:    
        #buglocs=df_predictions.loc[df_predictions.IsLocation==1]
        list_diags_locs = df.loc[0:29,"IsLocation"].values.tolist()
        list_diags_index = df.loc[0:29,"index"].values.tolist()
        list_diags_modelelement = df.loc[0:29,"ModelElementID"].values.tolist()
        toppredictions_classdiagrams_locs.append(list_diags_locs)
        toppredictions_classdiagrams_index.append(list_diags_index)
        toppredictions_classdiagrams_modelelement.append(list_diags_modelelement)

    print(df)
#print(toppredictions_classdiagrams_locs,"\n",toppredictions_classdiagrams_index,"\n","length: ",
#     len(toppredictions_classdiagrams_modelelement),toppredictions_classdiagrams_modelelement)

toppredictions_classifiers_locs = []
#toppredictions_classifiers_index = []
toppredictions_classifiers_modelelement = []
for df in df_predictions_classifiers:

    if df.empty:
        list_classes_locs = "not_found"
        #list_diags_index = "not_found"
        list_classes_modelelement = "not_found"
        toppredictions_classifiers_locs.append(list_classes_locs)
        #toppredictions_classdiagrams_index.append(list_diags_index)
        toppredictions_classifiers_modelelement.append(list_classes_modelelement)
    elif not df.empty: 
        list_classes_locs = df.loc[0:29,"IsLocation"].values.tolist()
        #list_classes_index = df.loc[0:29,"index"].values.tolist()
        list_classes_modelelement = df.loc[0:29,"ModelElementID"].values.tolist()
        toppredictions_classifiers_locs.append(list_classes_locs)
        #toppredictions_classifiers_index.append(list_classes_index)
        toppredictions_classifiers_modelelement.append(list_classes_modelelement)
        
      
list_nodespart11 = []
list_nodespart12 = []
list_nodespart13 = []
list_nodespart14 = []
for json_file_data in list_tracedbugs:
    nodespart11 = []
    nodespart12 = []
    nodespart13 = []
    nodespart14 = []
    for nodes in json_file_data:
        
        nodepart11 = str(dict(nodes).get('id'))
        nodepart12 = str(dict(nodes).get('summary'))
        nodepart13 = str(dict(nodes).get('creationTime'))
        nodepart14 = str(dict(nodes).get('status'))
        #nodepart21=dict(nodes).get('bugfixCommit')
        #nodepart42 = dict(nodes).get('resolution')
        nodespart11.append(nodepart11)
        nodespart12.append(nodepart12)
        nodespart13.append(nodepart13)
        nodespart14.append(nodepart14)
    list_nodespart11.append(nodespart11)
    list_nodespart12.append(nodespart12)
    list_nodespart13.append(nodespart13)
    list_nodespart14.append(nodespart14)


list_html_file_names = []    
for fname in list_filenames:
    f1 = fname.split('_')[0]
    f2 = fname.split('_')[1] 
    f3 = fname.split('_')[2]
    final_name = f1 + "_" + f2 + "_" + f3 + "_" + "prediction" + ".html"

    list_html_file_names.append(final_name)

list_table1_string = []
list_main_page_string = []
for json_file_data_idx in range(len(list_tracedversions)):
    list_nodepart21 = []
    list_nodepart22 = []
    
    nodepart11 = list_nodespart11[json_file_data_idx][0]
    nodepart12 = list_nodespart12[json_file_data_idx][0]
    nodepart13 = list_nodespart13[json_file_data_idx][0]
    nodepart14 = list_nodespart14[json_file_data_idx][0]

    nodepart00 = list_html_file_names[json_file_data_idx]

    for nodes in list_tracedversions[json_file_data_idx]:
       
        nodepart21 = str(dict(nodes).get('commitMessage'))
        nodepart22 = str(dict(nodes).get('date'))
        list_nodepart21.append(nodepart21)
        list_nodepart22.append(nodepart22)
        
    table1_string = """
    <th colspan="2"> Bug Report </th>
    <tr>
        <td  style="width: 20%; background-color:#d5e3c8;"> Bug Report Number </td>
        <td  style="width: 80%; background-color:#f7fff0;"> {part11} </td>
    </tr>
    <tr>
        <td  style="width: 20%; background-color:#d5e3c8;"> Summary </td>
        <td  style="width: 80%; background-color:#f7fff0;"> {part12} </td>
    </tr>
    <tr>
        <td  style="width: 20%; background-color:#d5e3c8;"> Creation Time </td>
        <td  style="width: 80%; background-color:#f7fff0;"> {part13} </td>
    </tr>
    <tr>
        <td  style="width: 20%; background-color:#d5e3c8;"> Status </td>
        <td  style="width: 80%; background-color:#f7fff0;"> {part14} </td>
    </tr>

    <th colspan="2"> Bug Fix </th>
    <tr>
        <td  style="width: 20%; background-color:#d5e3c8;"> Bugfix Commit </td>
        <td  style="width: 80%; background-color:#f7fff0;"> {part21} </td>
    </tr>
    <tr>
        <td  tyle="width: 20%; background-color:#d5e3c8;"> Bugfix Time </td>
        <td  style="width: 80%; background-color:#f7fff0;"> {part22} </td>
    </tr>
    """.format(

        part11=nodepart11,
        part12=nodepart12,
        part13=nodepart13,
        part14=nodepart14,
        part21=nodepart21,
        part22=nodepart22,
    )
    list_table1_string.append(table1_string)
    
    link_tag_str = """ 
    """
    mainpg_string = """
    <tr>
        <td  style="width: 50%; ">  <a href="./bugreport_pages/{htmlname}" target=""> {part01} </a>  </td>
        <td  style="width: 50%; "> {part02} </td>
    </tr>

    """.format(
        htmlname=nodepart00,
        part01=nodepart11,
        part02=nodepart12,
    )
    list_main_page_string.append(mainpg_string)
    
        
table2_string_ranked_classdiagrams_header = """
<th colspan="2"> Ranked List of Class Diagrams </th>
"""

table3_string_ranked_classifiers_header = """
<th colspan="2"> Ranked List of Classifiers </th>
"""

table4_string_discussion_header = """
<th colspan="2"> Discussion </th>
"""

list_discussions = []
for json_file_data in list_bugcomments:
    list_nodepart51 = []
    list_nodepart52 = []
    list_discussion = []
    for nodes in json_file_data:
        nodepart51 = str(dict(nodes).get('creationTime'))
        nodepart52 = str(dict(nodes).get('text'))
        list_nodepart51.append(nodepart51)
        list_nodepart52.append(nodepart52)

        table_string_discussion = """
        <tr>
            <td style="width: 20%; font-size: 12px; background-color:#d5e3c8"> Creation Time </td>
            <td style="width: 80%; font-size: 12px; background-color:#d5e3c8"> {part51} </td>
        </tr>
        <tr> <td colspan="2" style="font-size: 12px; background-color:#f7fff0"> {part52} </td> </tr>
        """.format(
            part51=nodepart51,
            part52=nodepart52,
        )
        list_discussion.append(table_string_discussion)
    list_discussions.append(list_discussion)
# num_model_elements = 0
# for nodes in classes:
#     modelelement = dict(nodes).get('__model__element__id__')
#     if modelelement != None and modelelement != "" and modelelement != []:
#         num_model_elements += 1
# list_diags_locs
table2_strtop = """
<tr>
<td  style="width: 20%; font-size: 10px; background-color:#d5e3c8">
"""
table2_strbottom = """
</td>
<td  rowspan="30" style="width: 80%;  background-color:#d5e3c8"> 
    <div class="container">
    <iframe class="responsive-iframe" src="{imgpageurl}" name="myIframe">
        <a href="{imgpageurl}">SELFHTML</a>
    </iframe>
    </div>
</td>
</tr>
""".format(
    imgpageurl=image_page_path + "diagram_default.html",

)

list_classdiagrams = []
for node_idx in range(len(toppredictions_classdiagrams_modelelement)):
    if toppredictions_classdiagrams_modelelement[node_idx] != "not_found":
        nodepart31 = toppredictions_classdiagrams_modelelement[node_idx]
        buglocs_classdiagrams = toppredictions_classdiagrams_locs[node_idx]

    img_path = get_image_page_path(image_page_path,node_idx)
    classdiagrams_patches = []
    for idx in range(len(nodepart31)):
        nd31 = nodepart31[idx]
        nd31_lastpart = nd31.split('/')[-1]
        if buglocs_classdiagrams[idx] == 1:
            listitems = """
            <ul>
                <li style="font-weight: bold;"> <a href="{part31}" target="myIframe"> {part32} </a> </li>
            </ul>   
            """.format(
                part31=img_path,
                part32=nd31_lastpart,
            )
            classdiagrams_patches.append(listitems)
        elif buglocs_classdiagrams[idx] == 0:
            listitems = """
            <ul>
                <li> <a href="{part31}" target="myIframe"> {part32} </a> </li>
            </ul>   
            """.format(
                part31=img_path,
                part32=nd31_lastpart,
            )
            classdiagrams_patches.append(listitems)
    list_classdiagrams.append(classdiagrams_patches)       
        
list_classifiers = []
for node_idx in range(len(toppredictions_classifiers_modelelement)):

    nodepart41 = toppredictions_classifiers_modelelement[node_idx]
    buglocs_classifiers = toppredictions_classifiers_locs[node_idx]
    classifiers_patches = []
    for idx in range(len(nodepart41)):
        nd41 = nodepart41[idx]
        if buglocs_classifiers[idx] == 1:
            table3_string_ranked_classifiers = """
                <tr>
                    <td  style="font-size: 12px; font-weight: bold; background-color:#d5e3c8"> {part41} </td>
                </tr>

                """.format(
                part41=nd41,
                
            )
            classifiers_patches.append(table3_string_ranked_classifiers)
        elif buglocs_classifiers[idx] == 0:
            table3_string_ranked_classifiers = """
                <tr>
                    <td  style="font-size: 12px; background-color:#d5e3c8"> {part41} </td>
                </tr>

                """.format(
                part41=nd41,
                
            )  
            classifiers_patches.append(table3_string_ranked_classifiers)
    list_classifiers.append(classifiers_patches)
print("*********************************************************************")
########################################################################################
# ********************************* final page *****************************************
########################################################################################
# list_table1_string "./buglocalization/plugins/org.sidiff.bug.localization.prediction/src/webpage/bugreport_pages/"

table2_not_exist = """
<table>
    <th colspan="2"> Ranked List of Class Diagrams </th>
    <tr>
        <td  style="width: 20%; font-size: 22px; background-color:#d5e3c8;"> No Data Available </td>
    </tr>
</table>
"""

# table2
list_table2_classdiagram_str = []
for classdiagram_patch in list_classdiagrams:
    classdiagrams_str = ""
    for classdiagram in classdiagram_patch:
        classdiagrams_str += classdiagram
    list_table2_classdiagram_str.append(classdiagrams_str)


# table3
list_table3_classifier_str = []
for classifier_patch in list_classifiers:
    classifiers_str = ""
    for classifier in classifier_patch:
        classifiers_str += classifier
    list_table3_classifier_str.append(classifiers_str)

# table4
table_discussions_str = []
for discussion_patch in list_discussions:
    discussions_str = ""
    for discussion in discussion_patch:
        discussions_str += discussion
    table_discussions_str.append(discussions_str)
# classifiers_names, classdiagrams_names
list_table0 = []
for table_idx in range(len(list_table1_string)):
    # main page table
    table0 = begin_table_tag + list_main_page_string[table_idx] + end_table_tag
    list_table0.append(table0)
    
    # table1  
    table1 = begin_table_tag + list_table1_string[table_idx] + end_table_tag
    
    ranked_classdiagrams_str = begin_table_tag + table2_not_exist + end_table_tag
        
    if list_table2_classdiagram_str[table_idx] != "not_found":
        table2_ranked_classdiagrams_str = table2_strtop + list_table2_classdiagram_str[table_idx] + table2_strbottom 
        table2 = begin_table_tag + table2_string_ranked_classdiagrams_header + table2_ranked_classdiagrams_str + end_table_tag

    else:
        table2 = table2_not_exist

    table3 = begin_table_tag + table3_string_ranked_classifiers_header + list_table3_classifier_str[table_idx] + end_table_tag
    table4 = begin_table_tag + table4_string_discussion_header + table_discussions_str[table_idx] + end_table_tag
    tables_together = table1 + table2 + table3 + table4

    # beginning tags ### 
    begin_tags = html_tag + style_string + body_tag + page_header_string 
    end_tags = body_end_tag + html_end_tag
    finalpage_string = begin_tags + tables_together + end_tags
    
    # list_html_file_names
    #pagename = list_filenames[table_idx] + ".html"
    pagename = list_html_file_names[table_idx] 

    print(finalpage_string)
    
    fname = output_pages_path + pagename
    #f = open("myfile.txt", "w")
    with open(fname, "w", encoding="utf-8") as f:
        f.write(str(finalpage_string))
    f.close()  

table0_str = ""
for table in list_table0:
    
    table0_str += table
    #begin_table_tag + table + end_table_tag

mainpage_table = begin_table_tag + table0_header + table0_str + end_table_tag

begin_tags = html_tag + main_page_style_string + body_tag + page_header_string 
end_tags = body_end_tag + html_end_tag

final_mainpage_string = begin_tags + mainpage_table + end_tags

mainpage_path = "C:/Users/gelareh/git/buglocalization/plugins/org.sidiff.bug.localization.prediction/src/webpage/"

fname = mainpage_path + "mainpage.html"
#f = open("myfile.txt", "w")
with open(fname, "w", encoding="utf-8") as f:
    f.write(str(final_mainpage_string))
f.close() 
