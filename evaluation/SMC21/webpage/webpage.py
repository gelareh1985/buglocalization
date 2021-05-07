import json

print("Hello World!")


def get_info_json(inputfile):
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

### define tags ###
begin_tr_tag = """<tr>
"""
end_tr_tag = """</tr>
"""

begin_table_tag = """<table>
"""
end_table_tag = """</table>
"""



# read Json file
fpath = "./buglocalization/evaluation/SMC21/webpage/7729_bug545475_28f53155d592e8d12991fab6d60706a44adb05e0_nodes.json"
f = open(fpath, 'r')

# load Json file
loaded_file = json.load(f)
tracedversions, tracedbugs, bugcomments, classes = get_info_json(loaded_file)
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
    nodepart42 = dict(nodes).get('resolution')

for nodes in tracedversions:
    nodepart21 = dict(nodes).get('commitMessage')
    nodepart22 = dict(nodes).get('date')

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
background-color: white;
font-size: 14px;
}

.container {
  position: relative;
  text-align:center;
  padding-top: 40%;
}

.responsive-iframe {
  position: absolute;
  top: 0;
  left: 0;
  bottom: 0;
  right: 0;
  width: 100%;
  height: 100%;
  border: 3px solid #73AD21;
}

.center {
  margin: auto;
  position: relative;
  width: 60%;
  border: 3px solid #73AD21;
  padding: 10px;
}

.link-text{
  padding-top:5%;
  padding-bottom:5%;
  text-align:center;

}

</style>
"""
toppage_string = """
<h2>Bug report</h2>
<p>
Our bug localization tool IdentiBug takes advantage of the information related to the Bug Report files from Eclipse JDT Core project repository.
</p>
"""
table_string_constant_rows = """
  <th colspan="4"> Bug Report </th>
  <tr>
     <td colspan="1" style="background-color:#d5e3c8"> Bug Report Number </td>
     <td colspan="3" style="background-color:#f7fff0"> {part11} </td>
  </tr>
  <tr>
     <td colspan="1" style="background-color:#d5e3c8"> Summary </td>
     <td colspan="3" style="background-color:#f7fff0"> {part12} </td>
  </tr>
  <tr>
     <td colspan="1" style="background-color:#d5e3c8""> Creation Time </td>
     <td colspan="3" style="background-color:#f7fff0"> {part13} </td>
  </tr>
  <tr>
     <td colspan="1" style="background-color:#d5e3c8"> Status </td>
     <td colspan="3" style="background-color:#f7fff0"> {part14} </td>
  </tr>

  <th colspan="4"> Bug Fix </th>
  <tr>
     <td colspan="1" style="background-color:#d5e3c8"> Bugfix Commit </td>
     <td colspan="3" style="background-color:#f7fff0"> {part21} </td>
  </tr>
  <tr>
     <td scolspan="1" tyle="background-color:#d5e3c8"> Bugfix Time </td>
     <td colspan="3" style="background-color:#f7fff0"> {part22} </td>
  </tr>
""".format(

    part11=nodepart11,
    part12=nodepart12,
    part13=nodepart13,
    part14=nodepart14,
    part21=nodepart21,
    part22=nodepart22,
)

table_string_ranked_classdiagrams_header = """
<th colspan="4"> Ranked List of Class Diagrams </th>
"""

table_string_ranked_classifiers_header = """
<th colspan="4"> Ranked List of Classifiers </th>
"""

table_string_discussion_header = """
<th colspan="4"> Discussion </th>
"""
list_discussion = []
for nodes in bugcomments:
    nodepart51 = dict(nodes).get('creationTime')
    nodepart52 = dict(nodes).get('text')

    table_string_discussion = """
    <tr>
        <td colspan="1" style="font-size: 12px; background-color:#d5e3c8"> Creation Time </td>
        <td colspan="3" rowspan="" style="font-size: 12px; background-color:#d5e3c8"> {part51} </td>
    </tr>
    <tr> <td colspan="4" style="font-size: 12px; background-color:#f7fff0"> {part52} </td> </tr>
    """.format(
        part51=nodepart51,
        part52=nodepart52,
    )
    list_discussion.append(table_string_discussion)

num_model_elements = 0
for nodes in classes:
    modelelement = dict(nodes).get('__model__element__id__')
    if modelelement != None and modelelement != "" and modelelement != []:
        num_model_elements += 1

strtop="""
<tr>
<td colspan="1" style="font-size: 10px ;background-color:#d5e3c8">
"""
strbottom="""
</td>
<td colspan="3" style="background-color:#d5e3c8"> 
        <div class="container">
        <iframe class="responsive-iframe" src="diagram01.html" name="myIframe">
            <a href="diagram01.html">SELFHTML</a>
        </iframe>
        </div>
</td>
</tr>
"""

list_classdiagrams = []
list_classifiers = []
for nodes in classes:
    nodepart31 = dict(nodes).get('__model__element__id__')
    nodepart41 = dict(nodes).get('__model__element__id__')
    nodepart31_lastpart = nodepart31.split('/')[-1]

    listitems= """
      <ul>
         <li> <a href={part31} target="myIframe"> {part32} </a> </li>
      </ul>   
    """.format(
        part31=nodepart31,
        part32=nodepart31_lastpart,
    )

    list_classdiagrams.append(listitems)

    table_string_ranked_classifiers = """
        <tr>
            <td colspan="3" style="font-size: 12px; background-color:#d5e3c8"> {part41} </td>
            <td colspan="1" style="font-size: 12px; background-color:#f7fff0"> {part42} </td>
        </tr>

        """.format(
        part41=nodepart41,
        part42=nodepart42,
    )
    list_classifiers.append(table_string_ranked_classifiers)

print("*********************************************************************")

html_tag = """<html>
"""
body_tag = """<body>
"""

html_end_tag = """</html>
"""
body_end_tag = """</body>
"""

column_spans_str="""
<colgroup>
        <col span="1" style="width: auto;">
        <col span="1" style="width: auto;">
        <col span="1" style="width: auto;">
        <col span="1" style="width: auto;">

</colgroup>
"""

begin_tags = html_tag + style_string + body_tag + toppage_string + begin_table_tag + column_spans_str
end_tags = end_table_tag + body_end_tag + html_end_tag
#### half page top ###
halfpagetop_string = begin_tags +  table_string_constant_rows


table_classdiagram_str = ""
for classdiagramst in list_classdiagrams:
    table_classdiagram_str += classdiagramst

table_ranked_classdiagrams_str = strtop + table_classdiagram_str + strbottom 

table_classifier_str = ""
for classifierst in list_classifiers:
    table_classifier_str += classifierst

table_classdiagrams = table_string_ranked_classdiagrams_header + table_ranked_classdiagrams_str
table_classifiers = table_string_ranked_classifiers_header + table_classifier_str


table_discussions_str = ""
for discussion in list_discussion:
    table_discussions_str += discussion

table_discussions = table_string_discussion_header + table_discussions_str
#print("\n discussions:\n",table_discussions)
#### half page bottom ###
halfpagebottom_string = table_classdiagrams + table_classifiers + table_discussions

### final page ###
finalpage_string = halfpagetop_string + halfpagebottom_string + end_tags
#print("FinalPage String: ", finalpage_string)

filename = "index.html"
with open(f"./buglocalization/evaluation/SMC21/webpage/{filename}", "w") as f:
    f.write(finalpage_string)
