import os.path
import re
from FileOperations import decrypt_file
from FileOperations import encrypt_file
from Resources.createRSAKeys import cert_get_mock
from Crypto import Random
from Crypto.Cipher import PKCS1_OAEP
from Crypto.PublicKey import RSA
import base64

class MyClass:
    def __init__(self, dir_path, key_size, socket):
        self.program_files_dir = dir_path
        self.key_size = key_size
        self.communication_key =""
        self.files_list = []
        self.socket = socket

    def make_first_connection(self):
        print("Making the first connection")
        """TODO chang this to use smartphone"""
        cert_get_mock(self.program_files_dir)

    def trade_communication_key(self):
        print("Trading a connection key")
        self.communication_key = Random.new().read(self.key_size / 8)
        public_key = RSA.importKey(open(self.program_files_dir + 'cert/public_key.txt').read(), passphrase='password')
        cipher = PKCS1_OAEP.new(public_key)
        encoded_key = base64.b64encode(cipher.encrypt(communication_key))
        #connection.send encoded_key




