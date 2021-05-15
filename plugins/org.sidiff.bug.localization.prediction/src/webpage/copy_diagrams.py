'''
@author: gelareh.meidanipour@uni-siegen.de, manuel.ohrndorf@uni-siegen.de
'''
import os
from pathlib import Path
from shutil import copyfile

if __name__ == '__main__':
    
    plugin_directory = Path(os.path.dirname(os.path.abspath(__file__))).parent.parent
    
    source_folder = "eclipse.jdt.core_data-2021-03-25_model-2021-04-06_diagram_ranking"
    source_path = str(plugin_directory) + "/evaluation/" + source_folder + "/"
    
    target_folder = "eclipse.jdt.core_data-2021-03-25_model-2021-04-06_diagram"
    target_path = str(plugin_directory) + "/evaluation/" + target_folder + "/"
    
    Path(target_path).mkdir(parents=True, exist_ok=True)
    
    for diagram_folder_name in os.listdir(source_path):
        source_diagram_folder_path = source_path + diagram_folder_name + "/"
        target_diagram_folder_path = target_path + diagram_folder_name + "/"
        
        if os.path.isdir(source_diagram_folder_path):
            Path(target_path + diagram_folder_name).mkdir(parents=True, exist_ok=True)
            
            for diagram_file in os.listdir(source_diagram_folder_path):
                if diagram_file.endswith('.html'):
                    #WORKAROUND: Remove absolute paths
                    with open(source_diagram_folder_path + diagram_file, 'r') as file:
                        html = file.read().replace('file:///' + source_diagram_folder_path.replace('\\', '/'), '')
                    with open(source_diagram_folder_path + diagram_file, 'w') as file:
                        file.write(html)
                    
                    # Copy HTML    
                    copyfile(source_diagram_folder_path + diagram_file, 
                             target_diagram_folder_path + diagram_file)
                if diagram_file.endswith('.svg'):
                    # Copy SVG:
                    copyfile(source_diagram_folder_path + diagram_file, 
                             target_diagram_folder_path + diagram_file)
