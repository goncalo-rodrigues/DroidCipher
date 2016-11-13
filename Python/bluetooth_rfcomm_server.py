from bluetooth import *

server_sock=BluetoothSocket( RFCOMM )
server_sock.bind(("", PORT_ANY))
server_sock.listen(1)

port = server_sock.getsockname()[1]
print("listening on port " + str(port))

# This UUID must be put in the QRCode. It should also have the PC MAC address.
uuid = "d20782ff-ab2c-43ad-9b23-19dc63a333ef"

advertise_service( server_sock, "DroidCipher PC",
                   service_id = uuid,
                   service_classes = [uuid, SERIAL_PORT_CLASS],
                   profiles = [SERIAL_PORT_PROFILE],
                   provider = "DroidCipher PC",
                   description = "To be run when it is new")

client_sock, client_info = server_sock.accept()
print("Accepted connection from " + str(client_info))

data = client_sock.recv(1024)
print("Received: " + str(data))

# To see if the android device sees the response
client_sock.send("THIS IS THE RESPONSE OF THE PC!!!")

client_sock.close()
server_sock.close()