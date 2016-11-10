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

def cert_get_mock(path):
    random_generator = Random.new().read
    RSAkey = RSA.generate(KEYSIZE,
                          randfunc=random_generator,
                          progress_func=None,
                          e=65537)
    public_key = RSAkey.publickey()

    # Export the public key
    pke = public_key.exportKey(format='PEM', passphrase='password', pkcs=1)
<<<<<<< Updated upstream
    writefile(path+'cert/public_key.txt', pke)

    # Export the private key
    pke = RSAkey.exportKey(format='PEM', passphrase='password', pkcs=1)
    writefile(path+'private_key.txt', pke)
=======
    writefile('/home/goncalo/pythoncipher/cert/public_key.txt', pke)

    # Export the private key
    pke = RSAkey.exportKey(format='PEM', passphrase='password', pkcs=1)
    writefile('/home/goncalo/pyhoncipher/private_key.txt', pke)
>>>>>>> Stashed changes
