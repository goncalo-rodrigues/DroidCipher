import os.path
import re
import base64
from FileOperations import decrypt_file
from FileOperations import encrypt_file
from Resources.createRSAKeys import cert_get_mock
from mock import mock
from PIL import Image
import qrcode
from Crypto.Cipher import AES
from Crypto import Random

program_files_dir ='/home/diogo/pyhoncipher/'
key_size = 256

def make_first_connection(program_files_dir, keySize):
    print("Making the first connection")
    program_files_dir = '/home/diogo/pyhoncipher/'
    integrity_key = Random.new().read(keySize / 8)
    encoded_key = base64.b64encode(integrity_key)
    img = qrcode.make(encoded_key)
    img.show()

    """TODO chang this to use smartphone"""
    cert_get_mock(program_files_dir)

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


print('Using ' + program_files_dir +' as program file')

if os.path.isfile(program_files_dir +'cert/public_key.txt')== False:
    if os.path.exists(program_files_dir + 'cert') == False:
        os.mkdir(program_files_dir + 'cert')
    make_first_connection(program_files_dir, key_size)

socket = mock(program_files_dir)#TODO put real socket here

files_list = []
list_files(program_files_dir, files_list)

img = qrcode.make('tudo o que o nuno quer')
img.show()

"""============== MAIN LOOP ==============="""

print("Insert Commands, for help insert help:")
print("Commands:\nlist\nopen\ncreate\nexit\nhelp")
command = ["start"]
while command != "exit":
    command = raw_input(">>").lower().split()
    if command[0] == "help":
        print("Commands:\nlist\nopen\ncreate\nexit\nhelp")

    elif (command[0] == "list") | (command[0] == "ls"):
        list_files(program_files_dir, files_list)

    elif command[0] == "open":
        if len(command) == 1:
            filename = raw_input("Insert the file to open:\n>>")
        else:
            filename = command[1]
        if filename in files_list:
            decrypt_file(filename, program_files_dir, socket)
            timestamp = os.stat(program_files_dir + filename).st_mtime
            os.system('xterm -e "nano ' + program_files_dir + filename + '"')
            timestamp2 = os.stat(program_files_dir + filename).st_mtime
            if timestamp2 != timestamp:
                encrypt_file(key_size, filename, program_files_dir)
            os.remove(program_files_dir + filename)

    elif command[0] == "create":
        if len(command) == 1:
            filename = raw_input("new file name:\n>>")
        else:
            filename = command[1]
        newf = open(program_files_dir + filename, "w")
        newf.close();
        encrypt_file(key_size, filename, program_files_dir)
        os.remove(program_files_dir + filename)
        files_list.append(filename)
        print(filename +" created!!")

    elif command[0] != "exit":
        print("that command don't exist, enter help")



