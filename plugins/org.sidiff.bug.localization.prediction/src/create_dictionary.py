import pandas as pd
import libarchive
import io

# path = r'D:\workspaces\buglocalization\test\buglocations_1000.7z'
path = r'D:\workspaces\buglocalization\test\full_5000.7z'

dictionary_words = {}
dictionary_types = {}


def read_archive(path, factory):
    with libarchive.file_reader(path) as reader:
        for entry in reader:
            if entry.name.endswith(".nodelist"):
                data = b""
                
                for block in entry.get_blocks():
                    data += block
                
                yield factory(entry.name, io.BytesIO(data))

                
def load_text(file, buffer):
    return file, buffer.read().decode('UTF-8')


def text_to_words(text):
    return text.split(" ")


dictionary_words_size = len(dictionary_words)
dictionary_types_size = len(dictionary_types)

for file, text in read_archive(path, load_text):
    print(file)
    
    for line in text.splitlines():
        columns = line.split("\t")
        
        # text-label column
        if (len(columns) >= 2):
            # [get text-label column][remove quotes]
            node_text = columns[1][1:-1]
        
            # fill dictionary:
            for word in text_to_words(node_text):
                dictionary_words[word] = dictionary_words_size
                dictionary_words_size += 1
        
        # type column
        if (len(columns) >= 3):
            # [get text-label column][remove quotes]
            node_type = columns[2][1:-1]
            
            # fill dictionary:
            dictionary_types[node_type] = dictionary_types_size
            dictionary_types_size += 1
        
"""
def load_nodes(file, buffer):
    node_list_col_names = ["index", "text", "type", "tag"]
    node_data = pd.read_table(buffer, names=node_list_col_names, index_col="index")
    node_data = node_data.fillna("")
    return file, node_data 
  
for file, table in read_archive(path, load_nodes):
    print(file)
    print(table)
"""
