from Crypto.PublicKey import RSA
from Crypto import Random

KEYSIZE = 256 * 8

def readfile(filename):
    fh = open(filename, 'rb')
    string = fh.read()
    fh.close()
    return string

def writefile(filename, string):
    fh = open(filename, 'wb')
    fh.write(string)
    fh.close()

random_generator = Random.new().read
RSAkey = RSA.generate(KEYSIZE,
                      randfunc=random_generator,
                      progress_func=None,
                      e=65537)
public_key = RSAkey.publickey()

# Export the public key
pke = public_key.exportKey(format='PEM', passphrase='password', pkcs=1)
writefile('protect/public_key.txt', pke)

# Export the private key
pke = RSAkey.exportKey(format='PEM', passphrase='password', pkcs=1)
writefile('protect/private_key.txt', pke)
