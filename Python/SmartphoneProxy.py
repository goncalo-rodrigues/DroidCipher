import bluetooth
import os
import time
import datetime
from Crypto import Random
from Crypto.Cipher import PKCS1_OAEP
from Crypto.PublicKey import RSA
from Crypto.Hash import SHA256
from Crypto.Cipher import AES
from bluetooth_rfcomm_client import connect_to_phone_service
from bluetooth_rfcomm_client import exchange_communication_key
from bluetooth_rfcomm_client import request_file_key
from bluetooth_rfcomm_client import request_rssi
from Crypto.Cipher import AES
from Colors import colors
from ConnectionException import ConnectionException
from MessageUtilities import data_copy


class SmartphoneProxy:

    def __init__(self, android_mac, android_uuid_files, android_uuid_rssi, keySize):
        self.android_mac = android_mac
        self.android_uuid_files = android_uuid_files
        self.android_uuid_rssi = android_uuid_rssi
        self.keySize = keySize
        self.socket = connect_to_phone_service(android_mac, android_uuid_files)
        if self.socket == None:
            raise ConnectionException("SmartphoneProxy init")
        self.communication_key = Random.new().read(keySize/8)
        self.iv = Random.new().read(AES.block_size)
        self.sendCommunicationKey()

    def reconect(self):
        self.socket = connect_to_phone_service(self.android_mac, self.android_uuid_files)
        if self.socket == None:
            print(colors.RED + "ERROR: couldn't reconnect" + colors.RESET)
            return
        self.communication_key = Random.new().read(self.keySize / 8)
        self.iv = Random.new().read(AES.block_size)
        self.sendCommunicationKey()

    # mock that should be replaced
    def decrypt_key(self, encrypted_key):
        output = None
        padded_text = encodePKCS7(encrypted_key)

        e = AES.new(self.communication_key, AES.MODE_CBC, self.iv)
        cipher_text = e.encrypt(padded_text)
        try:
            encrypted = request_file_key(self.socket, cipher_text)
            e = AES.new(self.communication_key, AES.MODE_CBC, self.iv)
            decrypted = e.decrypt(encrypted)
            output = decodePKCS7(decrypted)
        except bluetooth.btcommon.BluetoothError:
            if wantRetry() == "yes":
                self.reconect()
                return self.decrypt_key(encrypted_key)
        return output

    def sendCommunicationKey(self):
        public_key = RSA.importKey(open(os.environ['HOME'] + '/pythoncipher/' + 'cert/public_key.txt').read(), passphrase='password')
        asymmetric_cipher = PKCS1_OAEP.new(public_key, hashAlgo=SHA256)
        nonce = Random.new().read(4)
        encrypted_message = asymmetric_cipher.encrypt(nonce + self.iv + self.communication_key)
        exchange_communication_key(self.socket, encrypted_message, nonce)

    def measure_rssi(self):
        TIME_BETWEEN_CHECKS = 5  # It is measured in seconds
        #MAX_DISTANCE = TODO

        # It should run for the entire session
        while True:
            time.sleep(TIME_BETWEEN_CHECKS)
            timestamp = datetime.datetime.now().strftime('%Y-%m-%d %H:%M:%S')
            padded_ts = encodePKCS7(timestamp)
            e = AES.new(self.communication_key, AES.MODE_CBC, self.iv)
            encrypted_ts = e.encrypt(padded_ts)

            # Starts the real signal strength check
            encrypted_rssi = request_rssi(self.android_mac, self.android_uuid_rssi, encrypted_ts)

            decrypted = e.decrypt(encrypted_rssi)
            output = decodePKCS7(decrypted)

            ts_size = len(timestamp)
            recv_ts = data_copy(output, 0, ts_size)
            rssi_value = str(data_copy(output, ts_size, 10))

            if(recv_ts != timestamp):
                raise ConnectionException("Measure RSSI: The received timestamp was wrong")

            rssi_value = int(rssi_value)

            print("\t\t\t\t\t\tRSSI VALUE: " + str(rssi_value))

            # TODO: Confirm the scale of rssi


# ---------------AUX functions-------------

def encodePKCS7(text):
    text_length = len(text)
    amount_to_pad = AES.block_size - (text_length % AES.block_size)
    if amount_to_pad == 0:
        amount_to_pad = AES.block_size
    pad = chr(amount_to_pad)
    return text + pad * amount_to_pad


def decodePKCS7(text):
    pad = ord(text[-1])
    return text[:-pad]


def wantRetry():
    input = ""

    while True:
        print(colors.RED + "ERROR: could not contact the smartphone" + colors.RESET)
        input = raw_input("Do you want to try reconnecting to the smartphone? [yes/no]:\n>> ").lower()

        if input == "yes" or input == "no":
            break

        print("The input was not recognized. Please try again.")

    return input
