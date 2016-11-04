import os.path
import re
from Python.FileOperations import decrypt_file
from Python.FileOperations import encrypt_file
from Resources.createRSAKeys import cert_get_mock
from subprocess import call

def make_first_connection():
    print("Making the first connection")
    """TODO chang this to use smartphone"""
    cert_get_mock()


def trade_communication_key():
    print("Trading a connection key")


def list_files(path, file_list):
    print("Existing Files:")
    for file_in_dir in os.listdir(path):
        if file_in_dir.endswith(".encrypted"):
            cleaned_name = re.sub('\.encrypted$', '', file_in_dir)
            print(cleaned_name)
            files_list.append(cleaned_name)



"""========================================"""
"""                THE MAIN                """
"""========================================"""

program_files_dir ='/home/diogo/pyhoncipher/'
print('Using ' + program_files_dir +' as program file')

if os.path.isfile(program_files_dir +'cert/public_key.txt')== False:
    if os.path.exists(program_files_dir + 'cert') == False:
        os.mkdir(program_files_dir + 'cert')
    make_first_connection()

trade_communication_key()

files_list = []
list_files(program_files_dir, files_list)

print("Insert Commands, for help insert help:")
print("Commands:\nlist\nopen\ncreate\nexit\nhelp")
command = "start"
while command != "exit":
    command = raw_input(">>").lower()

    if command == "help":
        print("Commands:\nlist\nopen\ncreate\nexit\nhelp")

    elif (command == "list") | (command =="ls"):
        list_files(program_files_dir, files_list)

    elif command == "open":
        filename = raw_input("Insert the file to open:\n>>")
        if filename in files_list:
            decrypt_file(filename, program_files_dir)
            timestamp = os.stat(program_files_dir + filename).st_mtime
            os.system('xterm -e "nano ' + program_files_dir + filename + '"')
            timestamp2 = os.stat(program_files_dir + filename).st_mtime
            if timestamp2 != timestamp:
                encrypt_file(256, filename, program_files_dir)
            os.remove(program_files_dir + filename)

    elif command == "create":
        filename = raw_input("new file name:\n>>")
        newf = open(program_files_dir + filename, "w")
        newf.close();
        encrypt_file(256, filename, program_files_dir)
        os.remove(program_files_dir + filename)
        files_list.append(filename)
        print(filename +" created!!")

    elif command != "exit":
        print("that command don't exist, enter help")


"""
filename = raw_input("Insert the file to encrypt:\n")

 filename = raw_input("Insert the file to encrypt:\n")
if filename in files_list:
    encrypt_file(256, filename, program_files_dir)
    print("file encrypted!!")

    raw_input("Press ENTER to decrypt:\n")

    decrypt_file(filename, program_files_dir)
    print("file decrypted!!")

else:
    print("that file dont exist")
"""

"""use the file tryout"""




