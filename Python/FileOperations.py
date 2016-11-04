from Crypto.Cipher import AES
from Crypto import Random
import os.path
import marshal
from Crypto.Cipher import PKCS1_OAEP
from Crypto.PublicKey import RSA


def encrypt_file(keySize, filename):

    out_file_name = filename + '.encrypted'
    out_temporary_name = filename + '.temp'
    metadata_file_name = filename + '.meta'
    metadata_temporary_name = filename + '.metatemp'

    input_file = open(filename, 'rb')
    output_file = open(out_temporary_name, 'wb')
    metadata_file = open(metadata_temporary_name, 'w')

    """saves info to later decipher"""
    file_size = os.path.getsize(filename)
    key = Random.new().read(keySize/8)
    public_key = RSA.importKey(open('/home/diogo/pyhoncipher/public_key.txt').read(), passphrase='password')
    asymmetric_cipher = PKCS1_OAEP.new(public_key)
    cipher_key = asymmetric_cipher.encrypt(key)
    iv = Random.new().read(AES.block_size)
    marshal.dump([file_size, cipher_key, iv], metadata_file)

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
        os.remove(metadata_file_name)
    os.rename(metadata_temporary_name,metadata_file_name)
    if os.path.isfile(out_file_name):
        os.remove(out_file_name)
    os.rename(out_temporary_name,out_file_name)


def decrypt_file(filename):

    in_file_name = filename + '.encrypted'
    out_temporary_name = filename + '.temp'
    metadata_file_name = filename + '.meta'

    input_file = open(in_file_name, 'rb')
    output_file = open(out_temporary_name, 'wb')
    metadata_file = open(metadata_file_name, 'r')

    metadata = marshal.load(metadata_file)
    """saves info to later decipher"""
    file_size = metadata[0]
    """falta desencriptar chave"""
    encrypted_key = metadata[1]
    key = decrypt_key(encrypted_key)
    iv = metadata[2]

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

    if os.path.isfile(filename):
        os.remove(filename)
    os.rename(out_temporary_name,filename)


def decrypt_key(encrypted_key):

    private_key = RSA.importKey(open('/home/diogo/pyhoncipher/private_key.txt').read(), passphrase='password')
    cipher = PKCS1_OAEP.new(private_key)
    decrypted = cipher.decrypt(encrypted_key)
    return decrypted
