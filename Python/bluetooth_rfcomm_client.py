from bluetooth import *

# The used server address is the one from Nexus 7
server_address = "D8:50:E6:85:1E:41"
port = 1
sock=BluetoothSocket( RFCOMM )
sock.connect((server_address, port))

sock.send("hello!!")

sock.close()