import bluetooth


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
