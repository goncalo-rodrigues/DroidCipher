import bluetooth
import base64


def connect_to_phone_service(server_address, uuid):
    service = bluetooth.find_service(address=server_address, uuid=uuid)
    service = service[0]

    socket = bluetooth.BluetoothSocket(bluetooth.RFCOMM)
    try:
        socket.connect((server_address, service["port"]))
        print("MAC: " + server_address + "\nPort: " + str(service["port"]))
    except bluetooth.BluetoothError as e:
        print("Got an error: " + e.message)
        return None

    return socket


def exchange_communication_key(socket, encrypted_iv_communication_key, nonce):
    message = chr(0) + encrypted_iv_communication_key
    print("message:" + base64.b64encode(message)+ "len(message)" + str(len(message)))

    print("message with communication key send")
    socket.send(message)
    print("message with communication received by android")
    response = str(socket.recv(1024))
    print("response received")
    if response == nonce:
        return

    print("ERROR: Could not send the communication key!!!")


def request_file_key(socket, double_encrypted_file_key):
    NUMBER_OF_TRIES = 5
    try_number = 0

    message = chr(1) + double_encrypted_file_key

    while try_number < NUMBER_OF_TRIES:
        socket.send(message)

        response = socket.recv(1024)

        if len(response) > 1:
            return response

        try_number += 1

    print("ERROR: Could not receive file key!!!")

    # The program cannot go on. This error will crash the program.
    non_existing_statement()
