import os.path
import re
import base64
import marshal
from FileOperations import decrypt_file
from FileOperations import encrypt_file
from Resources.createRSAKeys import cert_get_mock
from bluetooth_rfcomm_server import *
from mock import mock
from PIL import Image
import qrcode
from Crypto.Cipher import AES
from Crypto import Random
import uuid
from Crypto.PublicKey import RSA
from SmartphoneProxy import SmartphoneProxy
import bluetooth
import subprocess


program_files_dir = os.environ['HOME'] + '/pythoncipher/'
key_size = 256

def add_left_zeros(mac):
    size = len(mac)
    result = ""

    if size != 17:
        n_chars = 0
        for i in range(size - 1, -1, -1):
            if ord(mac[i]) == ord(':'):
                while n_chars < 2:
                    result = "0" + result
                    n_chars += 1
                n_chars = 0
            else:
                n_chars += 1
            result = mac[i] + result
        if n_chars != 2:
            while n_chars < 2:
                result = "0" + result
                n_chars += 1
        return result
    return mac

def create_qrcode(qrcode_content, filename):
    qr = qrcode.QRCode()
    qr.add_data(qrcode_content)
    qr.make(fit=True)
    img = qr.make_image()
    image_file = open(filename, "w")
    img.save(image_file, "PNG")
    image_file.close()
    return subprocess.Popen(["display", filename])


def make_first_connection(program_files_dir, key_size):
    print("Making the first connection")
    integrity_key = Random.new().read(key_size / 8)
    encoded_key = base64.b64encode(integrity_key)
    random_uuid = str(uuid.uuid1())
    mac = add_left_zeros(bluetooth.read_local_bdaddr()[0])
    qrcode_content = mac + random_uuid + encoded_key

    filename = "qrcode"
    p = create_qrcode(qrcode_content, filename)
    android_info = create_pc_service(random_uuid)

    #TODO: Check the key's integrity
    integrity_preserved(android_info[0])

    p.kill()
    os.remove(filename)

    public_key = RSA.importKey(base64.b64decode(android_info[2]))
    pke = public_key.exportKey(format='PEM', passphrase='password', pkcs=1)
    public_key_file = open(program_files_dir + 'cert/public_key.txt', 'w')
    public_key_file.write(pke)
    public_key_file.close()

    metadata_file = open(program_files_dir + 'cert/androidMetadata.txt', 'w')
    marshal.dump([android_info[3], android_info[4]], metadata_file) #saves android_uuid and android_mac
    metadata_file.close()


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
proxy = SmartphoneProxy(android_mac, android_uuid, key_size)
metadata_file.close()
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


