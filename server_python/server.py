import socket
import struct

class Server:

    # declare some global variales
    # client_socket = None
    # client_address = None




    def __init__(self, address, port):
        self.server_address = (address, port)
        self.server_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        self.server_socket.bind(self.server_address)
        self.server_socket.listen(1)
        print("Server initialized on address:", self.server_address)

    def wait_for_connection(self):
        print("Waiting for a connection...")
        self.client_socket, self.client_address = self.server_socket.accept()
        print("Connection from:", self.client_address , "\n")
        return self.client_socket, self.client_address

    def close(self):
        self.server_socket.close()
        print("Server socket closed.")


    def sendAck(self):
        try:
            data = 'A' * 1024
            self.client_socket.send(data.encode('utf-16'))
        except Exception as e:
            print("An error occurredd00q:", e)

    def recvAck(self):
        try:
            data = self.client_socket.recv(1024)
            if data:
                print('received ac')
        except Exception as e:
            print("An error occurredd00:", e)

    def recvFileName(self):
        try:
            size = self.client_socket.recv(1)[0]
            self.file_name = self.client_socket.recv(size).decode()
            print(f"Received file name: {self.file_name}")
            return self.file_name
        except Exception as e:
            print("An error occurred while receiving the filename:", e)
            return None
        
    def recvFilesize(self):
        try:
            file_size_bytes = self.client_socket.recv(8)
            self.file_size = struct.unpack('!Q', file_size_bytes)[0]
            print(f"Received file size: {self.file_size}")

        except Exception as e:
            print("An error ", e)

    
    def recvFileData(self):
        try:
            totalRecv = 0
            if self.file_name.endswith('.txt'):
                with open("files/"+self.file_name, 'w') as file:
                    while totalRecv < self.file_size:
                        left = self.file_size - totalRecv
                        chunkSize = left if left < 1024 else 1024
                        chunk = self.client_socket.recv(chunkSize).decode()
                        if not chunk:
                            break  # socket closed or error
                        file.write(chunk)
                        totalRecv += len(chunk)
                print("Text file receive completed.\n")
            else:
                with open("files/"+self.file_name, 'wb') as file:
                    while totalRecv < self.file_size:
                        left = self.file_size - totalRecv
                        chunkSize = left if left < 1024 else 1024
                        chunk = self.client_socket.recv(chunkSize)
                        if not chunk:
                            break  # socket closed or error
                        file.write(chunk)
                        totalRecv += len(chunk)
                print("Binary file receive completed.\n")
        except Exception as e:
            print("Error:", e)
            import traceback
            traceback.print_exc()



    #!!!! Main function
    def recvFile(self):
        while True:
            self.recvAck()
            file_name = self.recvFileName()
            if not file_name:
                break
            self.recvFilesize()
            self.recvFileData()
            self.sendAck()

# Usage
if __name__ == "__main__":
    server = Server('192.168.0.108', 8000)
    client_socket, client_address = server.wait_for_connection()
    # Perform operations with client_socket here
    server.recvFile()