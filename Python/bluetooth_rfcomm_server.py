from bluetooth import *


def data_copy(d_from, offset_from, size):
    d_to = str()

    if len(d_from) < offset_from + size:
        upper_bound = len(d_from)
    else:
        upper_bound = offset_from + size

    for i in range(offset_from, upper_bound):
        d_to += d_from[i]

    return d_to


def create_pc_service(uuid):
    server_sock = BluetoothSocket(RFCOMM)
    server_sock.bind(("", PORT_ANY))
    server_sock.listen(1)

    port = server_sock.getsockname()[1]
    print("listening on port " + str(port))

    advertise_service(server_sock, "DroidCipher PC",
                      service_id=uuid,
                      service_classes=[uuid, SERIAL_PORT_CLASS],
                      profiles=[SERIAL_PORT_PROFILE],
                      provider="DroidCipher PC",
                      description="To be run when it is new")

    client_sock, client_info = server_sock.accept()
    print("Accepted connection from " + str(client_info))

    full_message = client_sock.recv(550)

    hash = data_copy(full_message, 0, 64)
    android_uuid = data_copy(full_message, 64, 36)
    android_mac = data_copy(full_message, 100, 17)
    public_key = data_copy(full_message, 117, 400)

    stop_advertising(server_sock)
    server_sock.close()

    # TODO: Check the public key's integrity. If there's a problem, it should try again.
    return (client_sock, hash, public_key, android_uuid, android_mac)


def integrity_preserved(client_sock):
    client_sock.send("OK")
    client_sock.close()


def integrity_changed(client_sock):
    client_sock.send("NOK")
    client_sock.close()
