import os
import json
import pandas as pd
from pathlib import Path

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


def get_prediction_info_csv(inputfile):
    list_df = []
    for filename in os.listdir(inputfile):
        if filename.endswith("_prediction.csv"):
            df = pd.read_csv(inputfile + filename, delimiter=";")
            list_df.append(df)
    return list_df


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
for filename in os.listdir(fpath_bugreport):
    if filename.endswith("_nodes.json"):
        #print(fpath_bugreport + filename)
        with open(fpath_bugreport + filename,"r") as f:
            # load Json file
            loaded_file = json.load(f)
            tracedversions, tracedbugs, bugcomments, classes = get_bug_reports_info_json(loaded_file)
            list_tracedversions.append(tracedversions)
            list_tracedbugs.append(tracedbugs)
            list_bugcomments.append(bugcomments)
            list_classes.append(classes)
            fname = os.path.basename(filename)
            fnamestr = os.path.splitext(fname)[0]
            list_filenames.append(fnamestr)
            #f.close()
# print("\n\n list traced version node attributes: ",tracedversions)
# print("\n\n list traced bugs attributes: ",tracedbugs)
# print("\n\n list bug comments attributes: ",bugcomments)
# print("\n\n list bug classes attributes: ",classes)

df_predictions_classdiagrams = get_prediction_info_csv(fpath_predictions_classdiagrams)
#print("\n\n\n predictions dataframe: ", df_predictions_classdiagrams)

toppredictions_classdiagrams_locs = []
toppredictions_classdiagrams_index = []
toppredictions_classdiagrams_modelelement = []
for df in df_predictions_classdiagrams:
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

df_predictions_classifiers = get_prediction_info_csv(fpath_predictions_classifiers)


toppredictions_classifiers_locs = []
#toppredictions_classifiers_index = []
toppredictions_classifiers_modelelement = []
for df in df_predictions_classifiers:
    list_classes_locs = df.loc[0:29,"IsLocation"].values.tolist()
    #list_classes_index = df.loc[0:29,"index"].values.tolist()
    list_classes_modelelement = df.loc[0:29,"ModelElementID"].values.tolist()
    toppredictions_classifiers_locs.append(list_classes_locs)
    #toppredictions_classifiers_index.append(list_classes_index)
    toppredictions_classifiers_modelelement.append(list_classes_modelelement)

nodepart11 = ""
nodepart12 = ""
nodepart13 = ""
nodepart14 = ""
nodepart21 = ""
nodepart22 = ""
nodepart31 = ""
nodepart41 = ""
nodepart42 = ""
nodepart51 = ""
nodepart52 = ""


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
    
list_table1_string = []
for json_file_data in list_tracedversions:
    list_nodepart21 = []
    list_nodepart22 = []
    
    for nodes in json_file_data:
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
    for idx in range(len(nodepart31)):
        nd41 = nodepart41[idx]
    if buglocs_classifiers[idx] == 1:
        table3_string_ranked_classifiers = """
            <tr>
                <td  style="font-size: 12px; font-weight: bold; background-color:#d5e3c8"> {part41} </td>
            </tr>

            """.format(
            part41=nd41,
            
        )
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

for table_idx in range(len(list_table1_string)):
    #print(list_table1_string[table_idx])
      
    table1 = begin_table_tag + list_table1_string[table_idx] + end_table_tag
    tables_together = table1
    # beginning tags ### 
    begin_tags = html_tag + style_string + body_tag + page_header_string 
    end_tags = body_end_tag + html_end_tag
    finalpage_string = begin_tags + tables_together + end_tags

    pagename = list_filenames[table_idx] + ".html"
    
    print(finalpage_string)
    
    fname = output_pages_path + pagename
    #f = open("myfile.txt", "w")
    with open(fname, "w") as f:
        f.write(str(finalpage_string))
    f.close()  
    # with open(f"./buglocalization/plugins/org.sidiff.bug.localization.prediction/src/webpage/{pagename}", "w") as f: 
    #     f.write(finalpage_string)   
    # f.close() 

        