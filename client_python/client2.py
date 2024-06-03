import os
import socket
import struct

class Client():
    
    def __init__(self) -> None:
        self.client_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        self.connected = False

        
            


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


    def sendFilename(self):
        filename = "manik.jpg"
        filename_length = len(filename)
        data = struct.pack("!I", filename_length)  # Pack the length of the filename as a 4-byte unsigned integer in network byte order
        try:
            self.client_socket.send(data)         # Send the packed length
            self.client_socket.send(filename.encode())  # Send the filename (make sure it's encoded to bytes)
        except Exception as e:
            print(e)

        
    def sendFilesize(self):
        filename = "manik.jpg"
        
        try:
            # Open the file in binary mode to get its size
            filesize = os.path.getsize(filename)
            
            # Pack the file size as a 8-byte unsigned integer in network byte order
            data = struct.pack("!Q", filesize)
            
            # Send the packed file size
            self.client_socket.send(data)
            
            print(f"Sent filesize: {filesize} bytes")

        except Exception as e:
            print(e)

    def sendFiledata(self):
        try:
            with open("manik.jpg", "rb") as file:
                while True:
                    data = file.read(1024)  # Read 1KB at a time
                    if not data:
                        break  # End of file
                    self.client_socket.sendall(data)  # Send the data
                    
                print("File sent successfully")
        except Exception as e:
            print(e)

    def sendFile(self):
        # Connect to the server
        while self.connected != True :
            try:
                self.client_socket.connect(("192.168.0.105", 8000))
                if self.client_socket != None:
                    print("Connected to the server")
                    self.connected = True
            except Exception as e:
                pass
        
        self.recvAck()
        self.sendFilename()
        self.recvAck()
        self.sendFilesize()
        self.recvAck()
        self.sendFiledata()

        
        

if __name__ == "__main__":
    client = Client()
    client.sendFile()