import bluetooth
import os
from Crypto.Cipher import PKCS1_OAEP
from Crypto.PublicKey import RSA

class SmartphoneProxy:

    def __init__(self, android_mac, android_uuid):
        self.android_mac = android_mac
        self.android_uuid = android_uuid
        #self.socket = connect_to_phone_service(android_mac, android_uuid)

    # mock that should be replaced
    def decrypt_key(self, encrypted_key):
        path = os.environ['HOME'] + '/pythoncipher/'
        private_key = RSA.importKey(open(path + 'private_key.txt').read(), passphrase='password')
        cipher = PKCS1_OAEP.new(private_key)
        decrypted = cipher.decrypt(encrypted_key)
        return decrypted


def connect_to_phone_service(server_address, uuid):
    service = bluetooth.find_service(address=server_address, uuid=uuid)
    service = service[0]

    socket = bluetooth.BluetoothSocket(bluetooth.RFCOMM)
    try:
        socket.connect((server_address, service["port"]))
        print("MAC: " + server_address + "\nPort: " + str(service["port"]))
    except bluetooth.BluetoothError as e:
        print("Got an error: " + e)
        return None

    return socket



