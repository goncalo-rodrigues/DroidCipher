from Crypto.Signature import PKCS1_v1_5
from Crypto.Hash import SHA256
from Crypto.PublicKey import RSA
import cPickle

def readfile(filename):
    fh = open(filename, 'rb')
    string = fh.read()
    fh.close()
    return string

def read_serial(filename):
    fh = open(filename, 'rb')
    data = cPickle.load(fh)
    fh.close()
    return data


encodedfile = read_serial('encryptedfile.pkl')

RSAkey = readfile('protect/private_key.txt')
RSAkey = RSA.importKey(RSAkey, passphrase='password')

# Decrypt data
plaindata = RSAkey.decrypt(encodedfile)

# Verify author
h = SHA256.new(plaindata)
verifier = PKCS1_v1_5.new(RSAkey)
signature = read_serial('signature.pkl')
if verifier.verify(h, signature):
    print "The signature is authentic.\n"
    print plaindata
else:
    print "The signature is not authentic."
