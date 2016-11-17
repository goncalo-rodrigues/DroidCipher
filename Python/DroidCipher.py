import os.path
import re
import base64
import marshal
from FileOperations import decrypt_file
from FileOperations import encrypt_file
from Resources.createRSAKeys import cert_get_mock
from bluetooth_rfcomm_server import create_pc_service
from mock import mock
from PIL import Image
import qrcode
from bluetooth import read_local_bdaddr
from Crypto.Cipher import AES
from Crypto import Random
import time
from Crypto.PublicKey import RSA
from SmartphoneProxy import SmartphoneProxy


program_files_dir = os.environ['HOME'] + '/pythoncipher/'
key_size = 256




def make_first_connection(program_files_dir, key_size):
    print("Making the first connection")
    #integrity_key = Random.new().read(key_size / 8)
    #encoded_key = base64.b64encode(integrity_key)
    # TODO: Use a random generator
    #uuid = "d20782ff-ab2c-43ad-9b23-19dc63a333ef"
    #mac = read_local_bdaddr()[0]
    #qrcode_content = mac + "!" + uuid + "!" + encoded_key
    #img = qrcode.make(qrcode_content)
    #img.show()
    android_info = ("MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAty5EbYDuPAgynsBKGLqCSkU2vcboJfH+\
iLudRPDY+G+4umESJE5KYNeaYv2gm+bQMEkuhvWicziS38DbUIrGZUy0njJruhZGirW5whQv6CV5\
seYSQMpSzomyq2TDe6ZZckOUtJVnpueciI2Q40ZJ53BOWxe8ZVBdx0e2wlJRxUVc6ppqgQaSenxZ\
piDyhSeWbpwAhfKcjIO847y7RXVTYIThC5zXmEgDrHwljiHJkDs/2w+Uv7l8zkQ10B+WVsh5N239\
l/nyKnoWpLJlySIMdLe6gJZ7rF8nyTY8TFddXL8kOEpQpHsPyPsEWWUM2/SlKC04smRrWzNIei9o\
bgv6owIDAQAB","d1418830-a213-11e6-bdf4-0800200c9a66", "D8:50:E6:85:1E:41")
    print(len(base64.b64decode(android_info[0])))
    #android_info = create_pc_service(uuid, integrity_key)

    public_key = RSA.importKey(base64.b64decode(android_info[0]))#need to be tested
    pke = public_key.exportKey(format='PEM', passphrase='password', pkcs=1)
    public_key_file = open(program_files_dir + 'cert/public_key.txt', 'w')
    public_key_file.write(pke)
    public_key_file.close()

    metadata_file = open(program_files_dir + 'cert/androidMetadata.txt', 'w')
    marshal.dump([android_info[1], android_info[2]], metadata_file) #saves android_uuid and android_mac
    metadata_file.close()

    """TODO change to previous lines"""
    #cert_get_mock(program_files_dir)

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

program_files_dir = os.environ['HOME'] + '/pythoncipher/'

if not os.path.exists(program_files_dir):
    os.mkdir(program_files_dir)

print('Using ' + program_files_dir + ' as program file')

if os.path.isfile(program_files_dir + 'cert/public_key.txt') == False:
    if os.path.exists(program_files_dir + 'cert') == False:
        os.mkdir(program_files_dir + 'cert')
    make_first_connection(program_files_dir, key_size)


metadata_file = open(program_files_dir + 'cert/androidMetadata.txt', 'r')
metadata = marshal.load(metadata_file)
android_mac = metadata[1]
android_uuid = metadata[0]
#android_mac = "XXX REMOVE"
#android_uuid = "XXX REMOVE"
proxy = SmartphoneProxy(android_mac, android_uuid, key_size)
metadata_file.close()
#proxy = mock(program_files_dir)  # TODO put real socket here
files_list = []
list_files(program_files_dir, files_list)


"""============== MAIN LOOP ==============="""

print("Insert Commands, for help insert help:")
print("Commands:\nlist\nopen\ncreate\nexit\nhelp")
command = ["start"]
while command[0] != "exit":
    command = raw_input(">> ").lower().split()
    if command[0] == "help":
        print("Commands:\nlist\nopen\ncreate\nexit\nhelp")

    elif (command[0] == "list") | (command[0] == "ls"):
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
            os.remove(program_files_dir + filename)

    elif command[0] == "create":
        if len(command) == 1:
            filename = raw_input("new file name:\n>> ")
        else:
            filename = command[1]
        newf = open(program_files_dir + filename, "w")
        newf.close()
        encrypt_file(key_size, filename, program_files_dir)
        os.remove(program_files_dir + filename)
        files_list.append(filename)
        print(filename + " created!!")

    elif command[0] != "exit":
        print("that command doesn't exist, enter help")


