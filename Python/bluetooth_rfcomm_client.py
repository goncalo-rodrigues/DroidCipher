import bluetooth
from Colors import colors
from ConnectionException import *
import threading


# To be used in the rssi thread
result = False


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
    socket.send(message)
    response = str(socket.recv(1024))
    if response == nonce:
        return

    print("ERROR: Could not send the communication key!!!")


def request_file_key(socket, double_encrypted_file_key):
    NUMBER_OF_TRIES = 5
    try_number = 0

    message = chr(1) + double_encrypted_file_key

    if socket == None:
        raise bluetooth.btcommon.BluetoothError

    while try_number < NUMBER_OF_TRIES:
        socket.send(message)

        response = socket.recv(1024)

        if len(response) > 1:
            return response

        try_number += 1

    print("ERROR: Could not receive file key!!!")

    # The program cannot go on. This error will crash the program.
    non_existing_statement()


def request_rssi(server_address, uuid, encrypted_timestamp):
    TIMEOUT_INTERVAL = 15  # This value is in seconds
    RESPONSE_SIZE = 100

    socket = connect_to_phone_service(server_address, uuid)

    if not socket:
        raise ConnectionException("Request RSSI: The returned socket was None")

    # This will use a completely different service
    socket.send(encrypted_timestamp)

    # Sets and starts the timer
    thread = threading.Thread(target=wait_rssi_response, args=(socket, RESPONSE_SIZE))
    thread.start()
    thread.join(TIMEOUT_INTERVAL)

    if (not result) or len(result) == 1:
        raise ConnectionException("Request RSSI: Wrong response")

    return result


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


def wait_rssi_response(socket, response_size):
    global result
    result = False
    result = socket.recv(response_size)


def response_timeout_handler(signum, frame):
    print("Cannot connection to the smartphone! Timer expired!")

    # TODO: Close the other thread that is showing the UI
