import sys
from Crypto import Random
import base64
import uuid
import bluetooth

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



sys.argv
program_files_dir = sys.argv[0]
key_size = sys.argv[1]
#make_first_connection(program_files_dir, key_size)

print("Making the first connection")
    integrity_key = Random.new().read(key_size / 8)
    encoded_key = base64.b64encode(integrity_key)
    random_uuid = str(uuid.uuid1())
    mac = add_left_zeros(bluetooth.read_local_bdaddr()[0])
    qrcode_content = mac + random_uuid + encoded_key

    # TODO: Try to close the image after receiving a message from the smartphone
    img = qrcode.make(qrcode_content)
    img.show()

    android_info = create_pc_service(random_uuid)

    #TODO: Check the key's integrity
    integrity_preserved(android_info[0])

    public_key = RSA.importKey(base64.b64decode(android_info[2]))
    pke = public_key.exportKey(format='PEM', passphrase='password', pkcs=1)
    public_key_file = open(program_files_dir + 'cert/public_key.txt', 'w')
    public_key_file.write(pke)
    public_key_file.close()

    metadata_file = open(program_files_dir + 'cert/androidMetadata.txt', 'w')
    marshal.dump([android_info[3], android_info[4]], metadata_file) #saves android_uuid and android_mac
    metadata_file.close()