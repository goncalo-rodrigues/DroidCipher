from FileOperations import decrypt_file
from FileOperations import encrypt_file


file = raw_input("Insert the file to encrypt:\n")

encrypt_file(256, '/home/diogo/pyhoncipher/'+file)
print("file encrypted!!")

raw_input("Press ENTER to decrypt:\n")

decrypt_file('/home/diogo/pyhoncipher/'+file)
print("file decrypted!!")

"""tryout"""