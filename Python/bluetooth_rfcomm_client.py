import bluetooth
import base64
from Colors import colors
from ConnectionException import ConnectionException


def connect_to_phone_service(server_address, uuid):
    service = bluetooth.find_service(address=server_address, uuid=uuid)
    if len(service) == 0:
        if wantRetryConnecToService() == "yes":
            return connect_to_phone_service(server_address, uuid)
        return None

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
    print("Communication ID: " + base64.b64encode(nonce))
    socket.send(message)
    response = str(socket.recv(1024))
    if response == nonce:
        return

    raise ConnectionException(colors.RED + "Could not send the communicationKey!!!" + colors.RESET)


def request_file_key(socket, double_encrypted_file_key):
    message = chr(1) + double_encrypted_file_key

    if socket == None:
        raise bluetooth.BluetoothError

    socket.send(message)

    response = socket.recv(1024)

    if len(response) > 1:
        return response

    raise bluetooth.BluetoothError

#################
# Aux Functions #
#################
def wantRetryConnecToService():
    input = ""

    while True:
        print(colors.RED + "ERROR: smartphone's service is down, please press START on the application" + colors.RESET)
        input = raw_input("Do you want to try again? [yes/no]:\n>> ").lower()

        if input == "yes" or input == "no":
            break

        print("The input was not recognized. Please try again.")

    return input
