import json
import os
from pathlib import Path
from typing import List

import pandas as pd

classdiagram_ranking_path = "D:/buglocalization_gelareh_home/evaluations/latest_version/"\
    "eclipse.jdt.core_data-2021-03-25_model-2021-04-06_diagram_ranking/"
classifier_ranking_path = "D:/buglocalization_gelareh_home/evaluations/latest_version/original_predictions/"
webpage_path = "C:/Users/gelareh/git/buglocalization/plugins/org.sidiff.bug.localization.prediction/src/webpage/jdt/"


class Ranking:
    def __init__(self, classdiagram_ranking_file, classifier_ranking_path) -> None:
        self.classdiagram_ranking_file = classdiagram_ranking_file
        self.classifier_ranking_file = classifier_ranking_path + Path(classdiagram_ranking_file).name
        self.classdiagram_ranking = self.load_csv(self.classdiagram_ranking_file)
        self.classifier_ranking = self.load_csv(self.classifier_ranking_file)
        self.bugreport_file = classifier_ranking_path + self.get_bugreport_name() + "_nodes.json"
        self.bugreport = self.load_json(self.bugreport_file)
        self.bugreport_version = self.get_bug_report_version()
        self.bugreport_info = self.get_bug_report_info()
        self.bugreport_comments = self.get_bug_report_comment()
        self.bugreport_locations = self.get_bug_report_location()

    def load_csv(self, file_path):
        return pd.read_csv(file_path, delimiter=";")

    def load_json(self, file_path):
        with open(file_path, "r") as f:
            loaded_file = json.load(f)
        return loaded_file

    def get_bugreport_name(self):
        classdiagram_ranking_filename = Path(self.classdiagram_ranking_file).name
        return classdiagram_ranking_filename[:classdiagram_ranking_filename.rfind("_")]

    def get_diagram_file(self, diagram_ranking_idx):
        return self.get_bugreport_name() + "/" + "{:04d}".format(diagram_ranking_idx) + "_diagram.html"

    def get_bug_report_version(self):
        for node in self.bugreport:
            if node['__labels__'] == ":TracedVersion":
                return node

    def get_bug_report_info(self):
        for node in self.bugreport:
            if node['__labels__'] == ":TracedBugReport":
                return node

    def get_bug_report_number(self):
        return self.bugreport_info["id"]

    def get_bug_report_summary(self):
        return self.bugreport_info["summary"]

    def get_bug_report_creation_time(self):
        return self.bugreport_info["creationTime"]

    def get_bug_report_status(self):
        return self.bugreport_info["status"]

    def get_bug_fix_commit_message(self):
        return self.bugreport_info["bugfixCommit"]

    def get_bug_fix_commit_time(self):
        return self.bugreport_info["bugfixTime"]

    def get_bug_report_comment(self):
        list_BugReportComment = []
        for node in self.bugreport:
            if node['__labels__'] == ":BugReportComment":
                list_BugReportComment.append(node)
        return list_BugReportComment

    def get_bug_report_comment_time(self, comment):
        return comment["creationTime"]

    def get_bug_report_comment_description(self, comment):
        return comment["text"]

    def get_bug_report_location(self):
        list_Class = []
        for node in self.bugreport:
            if node['__labels__'] == ":Class":
                list_Class.append(node)
        return list_Class

    def get_html_file(self):
        return self.get_bugreport_name() + ".html"


class BugListingPage:
    def __init__(self, webpage_path, rankings) -> None:
        self.webpage_path = webpage_path
        self.rankings: List[Ranking] = rankings

    def write(self):
        Path(self.webpage_path).mkdir(parents=True, exist_ok=True)
        with open(self.webpage_path + "index.html", "w", encoding="utf-8") as f:
            f.write(self.create())
            f.close()

    def create(self):
        return self.create_header() + self.create_bug_list() + self.create_footer()

    def create_header(self):
        return """
<html>
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
<body>
<h2>Bug report Information</h2>
"""

    def create_bug_list(self):
        bug_list = """ 
<table> 
    <th colspan="2" style="text-align:center; font-size: 20px;"> List of Bug Reports </th>
    <tr> 
        <th style="background-color: #ff9999;"> Bug Report Number </th> 
        <th style="background-color: #ff9999;"> Summary </th> 
    </tr>
"""
        for ranking in self.rankings:
            bug_list += """
    <tr>
        <td  style="width: 50%; ">  <a href="{bugreport_page_url}" target=""> {bugreport_id} </a>  </td>
        <td  style="width: 50%; "> {bugreport_summary} </td>
    </tr>
            """.format(
                bugreport_page_url=ranking.get_html_file(),
                bugreport_id=ranking.get_bug_report_number(),
                bugreport_summary=ranking.get_bug_report_summary(),
            )
        bug_list += """
</table>
"""
        return bug_list

    def create_footer(self):
        return """
</body>
</html>
"""


class BugReportPage:
    def __init__(self, ranking, webpage_path) -> None:
        self.ranking = ranking
        self.webpage_path = webpage_path

    def write(self):
        with open(self.webpage_path + self.ranking.get_html_file(), "w", encoding="utf-8") as f:
            f.write(self.create())
            f.close()

    def create(self):
        return self.create_header() + self.create_classdiagram_ranking() + self.create_classifier_ranking() + self.create_footer()

    def create_header(self):
        return """
<html>
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
<body>

<h2>Bug report Information</h2> 
"""

    def create_classdiagram_ranking(self):
        classdiagram_ranking = """
<table>
<th colspan="2"> Ranked List of Class Diagrams </th>
<tr>
    <td  style="width: 20%; font-size: 10px; background-color:#d5e3c8"> 
    <ul>
"""
        for ranking_index, ranking_position in self.ranking.classdiagram_ranking.iterrows():
            model_element_id = ranking_position["ModelElementID"]
            model_element_name = model_element_id.split('/')[-1]
            is_relevant_location = ranking_position["IsLocation"]
            start_isrelevant = "<strong>" if is_relevant_location == 1 else ""
            end_isrelevant = "</strong>" if is_relevant_location == 1 else ""

            classdiagram_ranking += """
             <li> <a href="{classdiagram_page_url}" target="diagramview"> {start_isrelevant} {model_element_name} {end_isrelevant} </a> </li>
            """.format(
                classdiagram_page_url=self.ranking.get_diagram_file(ranking_index),
                model_element_name=model_element_name,
                start_isrelevant=start_isrelevant,
                end_isrelevant=end_isrelevant,
            )

        classdiagram_ranking += """
    </ul> 
    </td>
    <td  rowspan="30" style="width: 80%;  background-color:#d5e3c8"> 
        <div class="container">
        <iframe class="responsive-iframe" src={initial_classdiagram_page_url} name="diagramview">
        </iframe>
        </div>
    </td>
</tr>
</table>
        """.format(
            initial_classdiagram_page_url=self.ranking.get_diagram_file(0),
        )

        return classdiagram_ranking

    def create_classifier_ranking(self):
        classifier_ranking = """
<table>
<th colspan="2"> Ranked List of Classifiers </th>
<tr>
    <td  style="width: 20%; font-size: 10px; background-color:#d5e3c8"> 
    <ul>
"""
        for ranking_index, ranking_position in self.ranking.classifier_ranking.iterrows():
            model_element_id = ranking_position["ModelElementID"]
            model_element_name = model_element_id.split('/')[-1]
            is_relevant_location = ranking_position["IsLocation"]
            start_isrelevant = "<strong>" if is_relevant_location == 1 else ""
            end_isrelevant = "</strong>" if is_relevant_location == 1 else ""

            classifier_ranking += """
             <li> {start_isrelevant} {model_element_name} {end_isrelevant} </li>
            """.format(
                model_element_name=model_element_name,
                start_isrelevant=start_isrelevant,
                end_isrelevant=end_isrelevant,
            )

        classifier_ranking += """
    </ul> 
    </td>
</tr>
</table>
"""

        return classifier_ranking

    def create_footer(self):
        return """
</body>
</html>
"""


if __name__ == '__main__':

    rankings = []
    for classdiagram_ranking_file in os.listdir(classdiagram_ranking_path):
        if classdiagram_ranking_file.endswith("_prediction.csv"):
            ranking = Ranking(classdiagram_ranking_path + classdiagram_ranking_file, classifier_ranking_path)
            rankings.append(ranking)

            bug_report_page = BugReportPage(ranking,webpage_path)
            bug_report_page.write()

    bug_listing_page = BugListingPage(webpage_path, rankings)
    bug_listing_page.write()
