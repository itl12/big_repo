import socket

# Create a TCP/IP socket
server_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)

# Bind the socket to the address and port
server_address = ('localhost', 9999)
server_socket.bind(server_address)

# Listen for incoming connections
server_socket.listen(1)

print("Waiting for a connection...")

while True:
    # Wait for a connection
    client_socket, client_address = server_socket.accept()

    try:
        print("Connection from:", client_address)

        # Receive the integer value from the client
        data = client_socket.recv(12)  # Assuming the integer is of 4 bytes size
        received_number = int.from_bytes(data, byteorder='big')

        print("Received number:", received_number)

    finally:
        # Clean up the connection
        client_socket.close()
