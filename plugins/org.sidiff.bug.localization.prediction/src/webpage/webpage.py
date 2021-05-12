import os
import json
import pandas as pd

print("Hello World!")
########################################################################################
###################################### functions #######################################
########################################################################################
### define functions ###
def get_bug_reports_info_json(inputfile):
    list_TracedVersion = []
    list_TracedBugReport = []
    list_BugReportComment = []
    list_Class = []
    for node in inputfile:
        if node['__labels__'] == ":TracedVersion":
           listnodes = [('__db__id__', node['__db__id__']), ('__initial__version__', node['__initial__version__']),
                        ('__labels__', node['__labels__']), ('__last__version__', node['__last__version__']),
                        ('__model__element__id__', node['__model__element__id__']), ('author', node['author']),
                        ('codeVersionID', node['codeVersionID']), ('commitMessage', node['commitMessage']),
                        ('date', node['date']), ('modelVersionID', node['modelVersionID'])]
           list_TracedVersion.append(listnodes)

        elif node['__labels__'] == ":TracedBugReport":
           listnodes = [('__db__id__', node['__db__id__']), ('__initial__version__', node['__initial__version__']),
                        ('__labels__', node['__labels__']), ('__last__version__', node['__last__version__']),
                        ('__model__element__id__', node['__model__element__id__']), ('assignedTo', node['assignedTo']),
                        ('bugfixCommit', node['bugfixCommit']), ('bugfixTime', node['bugfixTime']),
                        ('component', node['component']), ('creationTime', node['creationTime']), ('creator', node['creator']),
                        ('id', node['id']), ('product', node['product']), ('resolution', node['resolution']),
                        ('severity', node['severity']), ('status', node['status']), ('summary', node['summary'])]
           list_TracedBugReport.append(listnodes)

        elif node['__labels__'] == ":BugReportComment":
           listnodes = [('__db__id__', node['__db__id__']), ('__initial__version__', node['__initial__version__']),
                        ('__labels__', node['__labels__']), ('__last__version__', node['__last__version__']),
                        ('__model__element__id__', node['__model__element__id__']), ('creationTime', node['creationTime']),
                        ('creator', node['creator']), ('text', node['text'])]
           list_BugReportComment.append(listnodes)

        elif node['__labels__'] == ":Class":
           listnodes = [('__db__id__', node['__db__id__']), ('__initial__version__', node['__initial__version__']),
                        ('__labels__', node['__labels__']), ('__model__element__id__', node['__model__element__id__']),
                        ('isAbstract', node['isAbstract']), ('isActive', node['isActive']),
                        ('isFinalSpecialization', node['isFinalSpecialization']), ('isLeaf', node['isLeaf']),
                        ('name', node['name']), ('visibility', node['visibility'])]
           list_Class.append(listnodes)
    return list_TracedVersion, list_TracedBugReport, list_BugReportComment, list_Class

def get_prediction_info_csv(inputfile):
    df=pd.read_csv(inputfile, delimiter=";")
    return df
def get_image_page_path(inputpath,indx):
    image_path=""
    default_imgurl=inputpath + "diagram_default.html"
    imgurl=inputpath+"diagram0"+ str(indx+1) + ".html"
    if checkurl(imgurl)==True:
        image_path=imgurl
    else:
        image_path=default_imgurl
    return image_path  

def checkurl(path):
    if os.path.isfile(path):
        return True
    else:
        return False    

########################################################################################
##################################### page info ########################################
########################################################################################
### define tags ###
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

### define inside of tags ###
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
<h2>Bug report</h2>
<p>
Our bug localization tool IdentiBug takes advantage of the information related to the Bug Report files from Eclipse JDT Core project repository.
</p>
"""
########################################################################################
################################### load json file #####################################
########################################################################################
# read Json file
fpath_bugreport = "./buglocalization/evaluation/SMC21/webpage/7729_bug545475_28f53155d592e8d12991fab6d60706a44adb05e0_nodes.json"
fpath_predictions="./buglocalization/evaluation/SMC21/webpage/7508_bug545475_409689eaea4fbd78315894caf8741a7fad9a693a_prediction.csv"
f = open(fpath_bugreport, 'r')

image_page_path="C:/Users/gelareh/git/buglocalization/evaluation/SMC21/webpage/"

# load Json file
loaded_file = json.load(f)
tracedversions, tracedbugs, bugcomments, classes = get_bug_reports_info_json(loaded_file)
df_predictions=get_prediction_info_csv(fpath_predictions)
#buglocs=df_predictions.loc[df_predictions.IsLocation==1]
toppredictions_locs=df_predictions.loc[0:29,"IsLocation"].values.tolist()
toppredictions_index=df_predictions.loc[0:29,"index"].values.tolist()
toppredictions_modelelement=df_predictions.loc[0:29,"ModelElementID"].values.tolist()
print(df_predictions)
print(toppredictions_locs,"\n",toppredictions_index,"\n","length: ",len(toppredictions_modelelement),toppredictions_modelelement)

# print("\n list of traced bugreport versions nodes: ", tracedversions)
# print("\n list of traced bugreport nodes: ", tracedbugs)
# print("\n list of bugreport comments nodes: ", bugcomments)
# print("\n list of classes nodes: ", classes)

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

for nodes in tracedbugs:
    nodepart11 = dict(nodes).get('id')
    nodepart12 = dict(nodes).get('summary')
    nodepart13 = dict(nodes).get('creationTime')
    nodepart14 = dict(nodes).get('status')
    #nodepart21=dict(nodes).get('bugfixCommit')
    #nodepart42 = dict(nodes).get('resolution')

for nodes in tracedversions:
    nodepart21 = dict(nodes).get('commitMessage')
    nodepart22 = dict(nodes).get('date')


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

table2_string_ranked_classdiagrams_header = """
<th colspan="2"> Ranked List of Class Diagrams </th>
"""

table3_string_ranked_classifiers_header = """
<th colspan="2"> Ranked List of Classifiers </th>
"""

table4_string_discussion_header = """
<th colspan="2"> Discussion </th>
"""

list_discussion = []
for nodes in bugcomments:
    nodepart51 = dict(nodes).get('creationTime')
    nodepart52 = dict(nodes).get('text')

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

# num_model_elements = 0
# for nodes in classes:
#     modelelement = dict(nodes).get('__model__element__id__')
#     if modelelement != None and modelelement != "" and modelelement != []:
#         num_model_elements += 1

table2_strtop="""
<tr>
<td  style="width: 20%; font-size: 10px ;background-color:#d5e3c8">
"""
table2_strbottom="""
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
    imgpageurl="C:/Users/gelareh/git/buglocalization/evaluation/SMC21/webpage/diagram_default.html",

)

list_classdiagrams = []
list_classifiers = []
for node_idx in range(len(toppredictions_modelelement)):
    nodepart31 = toppredictions_modelelement[node_idx]
    nodepart41 = toppredictions_modelelement[node_idx]
    nodepart31_lastpart = nodepart31.split('/')[-1]
    nodepart42=toppredictions_index[node_idx]
    img_path=get_image_page_path(image_page_path,node_idx)
    listitems= """
      <ul>
         <li> <a href="{part31}" target="myIframe"> {part32} </a> </li>
      </ul>   
    """.format(
        part31=img_path,
        part32=nodepart31_lastpart,
    )

    list_classdiagrams.append(listitems)

    table3_string_ranked_classifiers = """
        <tr>
            <td  style="width: 70%; font-size: 12px; background-color:#d5e3c8"> {part41} </td>
            <td  style="width: 30%; font-size: 12px; background-color:#f7fff0"> index: {part42} </td>
        </tr>

        """.format(
        part41=nodepart41,
        part42=nodepart42,
    )
    list_classifiers.append(table3_string_ranked_classifiers)

print("*********************************************************************")
########################################################################################
#################################### final page ########################################
########################################################################################
# table1
table1=begin_table_tag + table1_string + end_table_tag

# table2
table2_classdiagram_str = ""
for classdiagramst in list_classdiagrams:
    table2_classdiagram_str += classdiagramst
table2_ranked_classdiagrams_str = table2_strtop + table2_classdiagram_str + table2_strbottom 

table2_classdiagrams = begin_table_tag + table2_string_ranked_classdiagrams_header + table2_ranked_classdiagrams_str + end_table_tag

# table3
table3_classifier_str = ""
for classifierst in list_classifiers:
    table3_classifier_str += classifierst

table3_classifiers = begin_table_tag + table3_string_ranked_classifiers_header + table3_classifier_str + end_table_tag

# table4
table_discussions_str = ""
for discussion in list_discussion:
    table_discussions_str += discussion

table4_discussions = begin_table_tag + table4_string_discussion_header + table_discussions_str + end_table_tag
#print("\n discussions:\n",table_discussions)

### beginning tags ### 
begin_tags = html_tag + style_string + body_tag + page_header_string 
end_tags = body_end_tag + html_end_tag

#### all tables ###
tables_together = table1 + table2_classdiagrams + table3_classifiers + table4_discussions 

### final page ###
finalpage_string = begin_tags + tables_together + end_tags
#print("FinalPage String: ", finalpage_string)

filename = "index.html"
with open(f"./buglocalization/evaluation/SMC21/webpage/{filename}", "w") as f:
    f.write(finalpage_string)
