# import pandas as pd
import libarchive
import io
from nltk.corpus import stopwords
from word_dictionary import WordDictionary

stop_words = set(stopwords.words('english'))

path = r"D:\files_MDEAI_original\Data_sets\buglocations_dataset\full versions/full_4000.7z"
save_path = "D:\\files_MDEAI_original\\Data_sets\\buglocations_dataset\\saved files\\dictionaries\\"

path = r"D:\files_MDEAI_original\Data_sets\buglocations_dataset/test.7z"

dictionary_words = WordDictionary(stopwords=stop_words)
dictionary_types = WordDictionary()

dictionary_words_bugreport_count = {}
dictionary_words_modelnode_count = {}


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


for file, text in read_archive(path, load_text):
    print(file)
    
    dictionary_words_count_version = {}
    
    for line in text.splitlines():
        columns = line.split("\t")
        
        # type column
        node_type = ""
        
        if (len(columns) >= 3):
            # [get text-label column][remove quotes]
            
            node_type = columns[2][1:-1]
               
            # fill dictionary:
            dictionary_types.add_label(node_type)
            
        # choose global or version local dictionary:
        dictionary_words_count_current = None
        
        if node_type == "BugReportNode" or node_type == "BugReportCommentNode":
            dictionary_words_count_current = dictionary_words_bugreport_count
        else:
            dictionary_words_count_current = dictionary_words_count_version
        
        # text-label column
        if (len(columns) >= 2):
            # [get text-label column][remove quotes]
            node_text = columns[1][1:-1]
        
            # fill dictionary:
            words = dictionary_words.add_text(node_text)
            
            # count frequency of words:
            for word in words:
                if word in dictionary_words_count_current:
                    dictionary_words_count_current[word] = dictionary_words_count_current[word] + 1
                else:
                    dictionary_words_count_current[word] = 1
    
    # count maximum frequency of words for all model nodes:
    for word, count in dictionary_words_count_version.items():
        if word in dictionary_words_modelnode_count:
            if count > dictionary_words_modelnode_count[word]:
                dictionary_words_modelnode_count[word] = count
        else:
            dictionary_words_modelnode_count[word] = count

def save_word_count(dictionary_file_name, word_counter):
    with open(dictionary_file_name + ".dictionary", 'w') as f:
        for key, value in word_counter.items():
            f.write(str(key) + '\t' + str(value) + "\n") 

dictionary_file_name = save_path + "complete_word_dict4_new"
dictionary_words.save(dictionary_file_name)

dictionary_file_name = save_path + "complete_type_dict4_new"
dictionary_types.save(dictionary_file_name)  
      
dictionary_file_name = save_path + "complete_word_dict4_bugreport_count_new"
save_word_count(dictionary_file_name, dictionary_words_bugreport_count)

dictionary_file_name = save_path + "complete_word_dict4_modelnode_count_new"
save_word_count(dictionary_file_name, dictionary_words_modelnode_count)

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
