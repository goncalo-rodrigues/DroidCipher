import bluetooth

# The used server address is the one from Nexus 7
server_address = "D8:50:E6:85:1E:41"
uuid = "d1418830-a213-11e6-bdf4-0800200c9a66"
service = bluetooth.find_service(address = server_address, uuid = uuid)
service = service[0]

socket = bluetooth.BluetoothSocket(bluetooth.RFCOMM)
try:
    socket.connect((server_address, service["port"]))
    print("MAC: " + server_address + "\nPort: " + str(service["port"]))
except bluetooth.BluetoothError as e:
    print("Got an error: " + e)

socket.send("HELLO WORLD!")

socket.close()