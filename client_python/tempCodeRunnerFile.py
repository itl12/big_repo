def sendFilesize(self):
        filename = "manik.jpg"
        
        try:
            # Open the file in binary mode to get its size
            filesize = os.path.getsize(filename)
            
            # Pack the file size as a 4-byte unsigned integer in network byte order
            data = struct.pack("!Q", filesize)
            
            # Send the packed file size
            self.client_socket.send(data)
            
            print(f"Sent filesize: {filesize} bytes")

        except Exception as e:
            print(e)