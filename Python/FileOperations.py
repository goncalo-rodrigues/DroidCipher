from Crypto.Cipher import AES
from Crypto import Random
import os.path
import marshal
import base64
from Crypto.Cipher import PKCS1_OAEP
from Crypto.PublicKey import RSA
from Crypto.Hash import SHA256
from Colors import colors



def encrypt_file(keySize, filename, path):

    out_file_name = path + filename + '.encrypted'
    out_temporary_name = path + filename + '.temp'
    metadata_file_name = path + filename + '.meta'
    metadata_temporary_name = path + filename + '.metatemp'

    input_file = open(path + filename, 'rb')
    output_file = open(out_temporary_name, 'wb')
    metadata_file = open(metadata_temporary_name, 'w')

    """saves info to later decipher"""
    file_size = os.path.getsize(path + filename)
    key = Random.new().read(keySize/8)
    print("created file key:" + base64.b64encode(key))
    public_key = RSA.importKey(open(path +'cert/public_key.txt').read(), passphrase='password')
    asymmetric_cipher = PKCS1_OAEP.new(public_key, hashAlgo = SHA256)#mudar isto para sha256
    h = SHA256.new()
    h.update(key)
    hashedkey = h.digest()
    cipher_key = asymmetric_cipher.encrypt(key)
    iv = Random.new().read(AES.block_size)
    marshal.dump([file_size, cipher_key, iv, hashedkey], metadata_file)

    cipher = AES.new(key, AES.MODE_CBC, iv)

    while True:
        chunk = input_file.read(AES.block_size)
        if len(chunk) == 0:
            break
        elif len(chunk) % 16 != 0:
            chunk += ' ' * (16 - len(chunk) % 16)

        output_file.write(cipher.encrypt(chunk))

    input_file.close()
    output_file.close()
    metadata_file.close()

    if os.path.isfile(metadata_file_name):
        os.system("shred -u " + metadata_file_name)
    os.rename(metadata_temporary_name, metadata_file_name)
    if os.path.isfile(out_file_name):
        os.system("shred -u " + out_file_name)
    os.rename(out_temporary_name, out_file_name)


def decrypt_file(filename, path, socket):

    in_file_name = path + filename + '.encrypted'
    out_temporary_name = path + filename + '.temp'
    metadata_file_name = path + filename + '.meta'

    input_file = open(in_file_name, 'rb')
    output_file = open(out_temporary_name, 'wb')
    metadata_file = open(metadata_file_name, 'r')

    metadata = marshal.load(metadata_file)
    """saves info to later decipher"""
    file_size = metadata[0]
    encrypted_key = metadata[1]
    iv = metadata[2]
    hashedkey = metadata[3]

    key = socket.decrypt_key(encrypted_key)
    if key == None:
        input_file.close()
        output_file.close()
        metadata_file.close()
        print(colors.RED + "X----> Some problem in geting the key from android !!!" + colors.RESET)
        return

    h = SHA256.new()
    h.update(key)
    newhashedkey = h.digest()
    if newhashedkey != hashedkey:
        input_file.close()
        output_file.close()
        metadata_file.close()
        print(colors.RED+"X----> the key received is not equal to the one created !!!"+colors.RESET)
        return

    decipher = AES.new(key, AES.MODE_CBC, iv)

    while True:
        chunk = input_file.read(AES.block_size)
        if len(chunk) == 0:
            break
        output_file.write(decipher.decrypt(chunk))

    output_file.truncate(file_size)

    input_file.close()
    output_file.close()
    metadata_file.close()

    if os.path.isfile(path + filename):
        os.system("shred -u " + path + filename)
    os.rename(out_temporary_name, path + filename)

