
from Crypto.Cipher import PKCS1_OAEP
from Crypto.PublicKey import RSA

class mock:
    def __init__(self, path):
        self.i = 1
        self.path = path

    def decrypt_key(self, encrypted_key):
        private_key = RSA.importKey(open(self.path + 'private_key.txt').read(), passphrase='password')
        cipher = PKCS1_OAEP.new(private_key)
        decrypted = cipher.decrypt(encrypted_key)
        return decrypted


def trade_communication_key(keySize, path, socket):
    print("Trading a connection key")
    """  communication_key = Random.new().read(keySize / 8)

    public_key = RSA.importKey(open(path + 'cert/public_key.txt').read(), passphrase='password')
    cipher = PKCS1_OAEP.new(public_key)
    encoded_key = base64.b64encode(cipher.encrypt(communication_key))
    socket.send(encoded_key)
    return communication_key"""