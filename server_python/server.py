import socket

class Server:
    def __init__(self, address, port):
        self.server_address = (address, port)
        self.server_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        self.server_socket.bind(self.server_address)
        self.server_socket.listen(1)
        print("Server initialized on address:", self.server_address)

    def wait_for_connection(self):
        print("Waiting for a connection...")
        client_socket, client_address = self.server_socket.accept()
        print("Connection from:", client_address)
        return client_socket, client_address

    def close(self):
        self.server_socket.close()
        print("Server socket closed.")


    def sendAck(self):
        try:
            data = 'A' * 1024
            client_socket.send()
        except Exception as e:
            print("An error occurredd00:", e)

    def recvAck(self):
        try:
            data = self.client_socket.recv(1024)
            if data:
                print('received ac')
        except Exception as e:
            print("An error occurredd00:", e)

    def recvFile(self):
        self.recvAck()

# Usage
if __name__ == "__main__":
    server = Server('192.168.0.108', 8000)
    client_socket, client_address = server.wait_for_connection()
    # Perform operations with client_socket here
    server.recvFile()