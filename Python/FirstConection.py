import sys
from Crypto import Random
import base64
import uuid
import bluetooth
import marshal
import qrcode
import subprocess
from bluetooth_rfcomm_server import *
from Crypto.PublicKey import RSA
from Crypto.Hash import HMAC
from Crypto.Hash import SHA512
from Colors import colors

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

program_files_dir = sys.argv[1]
key_size = int(sys.argv[2])
#make_first_connection(program_files_dir, key_size)

if os.path.exists(program_files_dir + 'cert') == False:
    os.mkdir(program_files_dir + 'cert')#,745)


exchanged = False
while exchanged == False:
    print("Making the first connection")
    integrity_key = Random.new().read(key_size / 8)
    encoded_key = base64.b64encode(integrity_key)
    random_uuid = str(uuid.uuid1())
    mac = add_left_zeros(bluetooth.read_local_bdaddr()[0])
    qrcode_content = mac + random_uuid + encoded_key

    filename = "qrcode"
    p = create_qrcode(qrcode_content, filename)

    android_info = create_pc_service(random_uuid)

    h = HMAC.new(integrity_key, android_info[2]+android_info[3]+android_info[4], SHA512).digest()
    if android_info[1] == h:
        integrity_preserved(android_info[0])
        exchanged = True
    else:
        integrity_changed(android_info[0])
        print(colors.RED + "X----> Android public key's integrity is not OK" + colors.RESET)

    p.kill()
    os.system("shred -u " + filename)

public_key = RSA.importKey(base64.b64decode(android_info[2]))
pke = public_key.exportKey(format='PEM', passphrase='password', pkcs=1)
public_key_file = open(program_files_dir + 'cert/public_key.txt', 'w')
public_key_file.write(pke)
public_key_file.close()

metadata_file = open(program_files_dir + 'cert/androidMetadata.txt', 'w')
marshal.dump([android_info[3], android_info[4]], metadata_file)  # saves android_uuid and android_mac
metadata_file.close()
os.system('xterm -e "sudo chattr +i ' + program_files_dir + 'cert' + '"')

