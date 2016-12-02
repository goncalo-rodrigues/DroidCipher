import os.path
import re
import marshal
from FileOperations import decrypt_file
from FileOperations import encrypt_file
from bluetooth_rfcomm_server import *
from SmartphoneProxy import SmartphoneProxy


key_size = 256

def list_files(path, file_list):
    print("Existing Files:")
    for file_in_dir in os.listdir(path):
        if file_in_dir.endswith(".encrypted"):
            cleaned_name = re.sub('\.encrypted$', '', file_in_dir)
            print(cleaned_name)
            files_list.append(cleaned_name)


def lst_contains(lst, something):
    for el in lst:
        if el == something:
            return True
    return False


"""========================================"""
"""                THE MAIN                """
"""========================================"""

program_files_dir = os.environ['HOME'] + '/pythoncipher/'

if not os.path.exists(program_files_dir):
    os.mkdir(program_files_dir)

print('Using ' + program_files_dir + ' as program file')

if os.path.isfile(program_files_dir + 'cert/public_key.txt') == False:
    os.system('xterm -e "sudo python FirstConection.py ' + program_files_dir +' '+ str(key_size) + '"')


metadata_file = open(program_files_dir + 'cert/androidMetadata.txt', 'r')
metadata = marshal.load(metadata_file)
android_mac = metadata[1]
android_uuid = metadata[0]
proxy = SmartphoneProxy(android_mac, android_uuid, key_size)
metadata_file.close()
files_list = []
list_files(program_files_dir, files_list)


"""============== MAIN LOOP ==============="""

print("Insert Commands, for help insert help:")
print("Commands:\nlist\nopen\ncreate\ndelete\nexit\nhelp")
while True:
    command = raw_input(">> ").strip().split()

    if len(command) == 0:
        continue

    # This will allow the filenames to be case sensitive
    command[0] = command[0].lower()

    if command[0] == "help":
        print("Commands:\nlist\nopen\ncreate\nexit\nhelp")

    elif (command[0] == "list") | (command[0] == "ls"):
        if len(files_list) == 0:
            print("There are no files.")
        else:
            list_files(program_files_dir, files_list)

    elif command[0] == "open":
        if len(command) == 1:
            filename = raw_input("Insert the file to open:\n>> ")
        else:
            filename = command[1]
        if filename in files_list:
            decrypt_file(filename, program_files_dir, proxy)
            timestamp = os.stat(program_files_dir + filename).st_mtime
            os.system('xterm -e "nano ' + program_files_dir + filename + '"')
            timestamp2 = os.stat(program_files_dir + filename).st_mtime
            if timestamp2 != timestamp:
                encrypt_file(key_size, filename, program_files_dir)
            os.system("shred -u " + program_files_dir + filename)
        else:
            print("The given file does not exist. Please try again.")

    elif command[0] == "create":
        if len(command) == 1:
            filename = raw_input("New file name:\n>> ")
        else:
            filename = command[1]

        if lst_contains(files_list, filename):
            print("The given file already exists. Please try again.")
            continue

        newf = open(program_files_dir + filename, "w")
        newf.close()
        encrypt_file(key_size, filename, program_files_dir)
        os.system("shred -u " + program_files_dir + filename)
        files_list.append(filename)
        print(filename + " was created!")

    elif command[0] == "delete":
        if len(command) == 1:
            filename = raw_input("Name of the desired file:\n>> ")
        else:
            filename = command[1]

        if not lst_contains(files_list, filename):
            print("The given file does not exist. Please try again.")
            continue

        path = program_files_dir + filename
        os.system("shred -u " + path + ".encrypted")
        os.system("shred -u " + path + ".meta")

        files_list = [file for file in files_list if file != filename]
        print(filename + " was deleted.")

    elif command[0] == "exit":
        break

    else:
        print("That command does not exist. To see the commands, enter help.")
