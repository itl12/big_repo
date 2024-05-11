import struct 
import socket
import threading
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

                    self.send_data()

                except Exception as e:
                    print("An error occurred:", e)
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
                print("An error occurred:", e)


    def send_data(self):
        num = 1234567890773748
        data = num.to_bytes(8, byteorder='big')
        try:
            self.client_socket.send(data)
        except Exception as e:
            print("An error occurred:", e)




client = Client()