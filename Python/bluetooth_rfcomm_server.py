from bluetooth import *


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

    # TODO: Check which buffer sizes are the right ones
    public_key = client_sock.recv(1024)
    hash = client_sock.recv(1024)
    android_uuid = str(client_sock.recv(1024))
    android_mac = str(client_sock.recv(1024))

    # TODO: Check the public key's integrity. If there's a problem, it should try again.

    client_sock.send("OK")

    client_sock.close()
    stop_advertising(server_sock)
    server_sock.close()

    return (public_key, android_uuid, android_mac)
