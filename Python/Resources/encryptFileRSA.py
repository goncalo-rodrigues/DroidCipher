from Crypto.Signature import PKCS1_v1_5
from Crypto.Hash import SHA256
from Crypto.PublicKey import RSA
import cPickle

def readfile(filename):
    fh = open(filename, 'rb')
    string = fh.read()
    fh.close()
    return string

def writefile(filename, string):
    fh = open(filename, 'wb')
    fh.write(string)
    fh.close()

def write_serial(filename, data):
    fh = open(filename, 'wb')
    cPickle.dump(data, fh, protocol=cPickle.HIGHEST_PROTOCOL)
    fh.close()

PASSPHRASE_PRIVATE = 'password'
plainfile = 'olaADEUSbomDIAboaNOITE'

RSAkey = readfile('protect/private_key.txt')
RSAkey = RSA.importKey(RSAkey, passphrase='password')

h = SHA256.new(plainfile)
signer = PKCS1_v1_5.new(RSAkey)
signature = signer.sign(h)

# Save signature
write_serial('signature.pkl', signature)

# Encrypt file
write_serial('encryptedfile.pkl', RSAkey.encrypt(plainfile, ''))
