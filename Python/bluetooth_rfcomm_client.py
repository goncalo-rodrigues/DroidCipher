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
        print("Got an error: " + e)
        return None

    return socket


def exchange_communication_key(socket, encrypted_iv_communication_key,nonce):
    NUMBER_OF_TRIES = 5
    try_number = 0

    # TODO: Check if the operator ID needs to be encrypted as well
    message = chr(0) + encrypted_iv_communication_key
    print("message:" + base64.b64encode(message)+"len(message)"+ str(len(message)))
    while try_number < NUMBER_OF_TRIES:
        print("message with communication key send")
        socket.send(message)
        print("message with communication received by android")
        response = str(socket.recv(1024))
        print("response received")
        if response == nonce:
            return
        try_number += 1

    print("ERROR: Could not send the communication key!!!")

    # The program cannot go on. This error will crash the program.
    non_existing_statement()


def request_file_key(socket, double_encrypted_file_key):
    NUMBER_OF_TRIES = 5
    try_number = 0

    # TODO: Check if the operator ID needs to be encrypted as well
    message = chr(1) + double_encrypted_file_key

    while try_number < NUMBER_OF_TRIES:
        socket.send(message)

        response = socket.recv(1024)
        # TODO: Check if this will work (does one char have size == 1?)
        # The following will be in pseudo-code
        if len(response) > 1:
            return response

        try_number += 1

    print("ERROR: Could not receive file key!!!")

    # The program cannot go on. This error will crash the program.
    non_existing_statement()
