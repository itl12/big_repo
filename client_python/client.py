import os
import struct 
import socket
import threading
import time
import tkinter as tk


class Client:
    
    
    
    def __init__(self):

        self.connected = False
        self.server_ip = '192.168.0.106'
        self.server_port = 8080
        # self.server_ip = 'localhost'
        # self.server_port = 9999

        
        # Create the main window
        self.window = tk.Tk()
        self.window.title("Centered Button")

        # Set the window size
        self.window.geometry("500x500")

        # Create a frame to contain the button
        self.frame = tk.Frame(self.window)

        # Create a button and add it to the frame
        self.button = tk.Button(self.frame, text="Connect to server", command=self.connect_to_server, width=20, height=5)

        # Place the button in the center of the frame
        self.button.pack(pady=10)

        # Create a text label and add it to the frame
        self.text_label = tk.Label(self.frame, font=24,text="This is a text label")
        self.text_label.pack(pady=(0, 100))

        # Place the frame in the center of the window
        self.frame.place(relx=0.5, rely=0.5, anchor=tk.CENTER)

        # Start the Tkinter event loop
        self.window.mainloop()


    def button_click(self):
        print("Button clicked")
        self.text_label.config(text="connecting to server")


    def connect_to_server(self):
    

        if not self.connected:
            def connect():
                try:
                    # Create a TCP socket
                    self.client_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)

                    # Connect to the server
                    self.client_socket.connect((self.server_ip, self.server_port))
                    print("Connected to the server")
                    self.connected = True

                    # Update button text and label
                    self.button.config(text="Disconnect")
                    self.text_label.config(text=f"Connected to {self.server_ip}:{self.server_port}")
                    
                    self.send_num_of_files()

                    self.recvAcknowledge()

                    
                    file_path = 'movie.mkv'  
                    # file_path = 'textfile.txt'  
                    file_size = self.get_file_size(file_path)
                    print("File size:", file_size, "bytes")

                    self.send_filesize(file_size)
                    self.recvAcknowledge()
                    self.send_filename(file_path)
                    self.recvAcknowledge()
                    # self.send_text_file_data(file_path)
                    self.send_binary_file_data(file_path)
                    self.recvAcknowledge()

                except Exception as e:
                    print("Ann error occurred12:", e)
                    self.button.config(text="Try again!")
                    self.text_label.config(text=f"Couldn't connected to {self.server_ip}:{self.server_port}")
                

            # Start the connection attempt in a separate thread
            threading.Thread(target=connect, daemon=True).start()
        else:
            try:
                # Close the connection
                self.client_socket.close()
                print("Disconnected from the server")
                self.connected = False

                # Update button text and label
                self.button.config(text="Connect")
                self.text_label.config(text="Disconnected")
            except Exception as e:
                print("An error occurred7:", e)


    def send_num_of_files(self):
        num = 1
        data = num.to_bytes(4, byteorder='big')
        try:
            
            num = self.client_socket.send(data)
        except ConnectionError:
            print("Connection was lost!")
        except Exception as e:
            print("An error occurredd6:", e)


    def recvAcknowledge(self):
        try:
            data = self.client_socket.recv(1024)
            if data:
                print('received ac')
        except Exception as e:
            print("An error occurredd00:", e)

        
    def send_filesize(self, file_size):

        file_size = file_size.to_bytes(8, byteorder='big')
        try:
            data = self.client_socket.send(file_size)
        except Exception as e:
            print("An e1rror occured ", e)


    def get_file_size(self, file_path):
        # Open the file in binary mode
        with open(file_path, 'rb') as file:
            # Get the size of the file in bytes
            file_size = os.path.getsize(file_path)
        return file_size
    

    def send_filename(self, file_name):
        # Encode the filename string into bytes using UTF-8 encoding
        file_name_bytes = file_name.encode('utf-8')
        try:
            self.client_socket.send(file_name_bytes)
        except Exception as e:
            print("An e2rror occured ", e)


    def send_file_data(self, file_path):
        try:
            # Open the file in binary mode
            with open(file_path, 'rb') as file:
                file_size = os.path.getsize(file_path)
                total_bytes_sent = 0
                
                while total_bytes_sent < file_size:
                    # Read 1024 bytes of data from the file
                    chunk = file.read(1024)
                    
                    # Send the chunk over the socket
                    self.client_socket.send(chunk)
                    
                    # Update the total bytes sent
                    total_bytes_sent += len(chunk)
                    
                    print(f"Sent {total_bytes_sent}/{file_size} bytes")
                    
                print("File data sent successfully")
        except Exception as e:
            print("An error occurred4:", e)



    def send_text_file_data(self, file_path):
        try:
            # Open the file in text mode
            with open(file_path, 'r') as file:
                file_size = os.path.getsize(file_path)
                total_bytes_sent = 0
                
                while total_bytes_sent < file_size:
                    # Read 1024 bytes of data from the file
                    chunk = file.read(1024)
                    
                    # Send the chunk over the socket
                    self.client_socket.send(chunk.encode('utf-8'))
                    
                    # Update the total bytes sent
                    total_bytes_sent += len(chunk)
                    
                    print(f"Sent {total_bytes_sent}/{file_size} bytes")
                    
                print("File data sent successfully")
        except Exception as e:
            print("An error occurred5:", e)

            
    def send_binary_file_data(self, file_path):
        try:
            # Open the file in binary mode
            with open(file_path, 'rb') as file:
                file_size = os.path.getsize(file_path)
                total_bytes_sent = 0
                
                while total_bytes_sent < file_size:
                    # Read 1024 bytes of data from the file
                    chunk = file.read(1024)
                    
                    # Send the chunk over the socket
                    self.client_socket.sendall(chunk)
                    
                    # Update the total bytes sent
                    total_bytes_sent += len(chunk)
                    
                    # print(f"Sent {total_bytes_sent}/{file_size} bytes")
                    
                print("File data sent successfully")
        except Exception as e:
            print("An error 9occurred:", e)
                


client = Client()


