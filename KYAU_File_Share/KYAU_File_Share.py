import threading
import time
import tkinter as tk
from tkinter import font
from tkinter.ttk import Progressbar
from tkinterdnd2 import DND_FILES, TkinterDnD
import os
import socket
import struct
from datetime import datetime

class KYAU_File_Share:
    def __init__(self):
        self.client_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        self.server_socket = None
        self.connected = False
        self.is_sending = False
        self.dropped_files = set()
        self.local_ip = self.get_local_ip()
        self.create_window()

    def on_close(self):
        print("Window is closing. Running cleanup tasks...")
        self.is_sending = False
        self.receiving = False
        self.root.destroy()

    def create_window(self):
        def switch_to_sender_view():
            main_frame.pack_forget()
            sender_frame.pack(fill='both', expand=True)

        def switch_to_receiver_view():
            main_frame.pack_forget()
            receiver_frame.pack(fill='both', expand=True)
            self.local_ip = self.get_local_ip()
            self.receiver_ip_label.config(text=f"Local IP: {self.local_ip}")
            self.progress_receiver["value"] = 0
            threading.Thread(target=self.start_server).start()

        def switch_to_main_view():
            self.receiving = False
            self.is_sending = False
            self.connected = False
            self.output_box1.config(text="")
            self.output_box2.config(text="")
            self.connect_button.config(state='active')
            self.progress["value"] = 0
            self.root.update_idletasks()
            try:
                self.client_socket.close()
                if self.server_socket:
                    self.server_socket.close()
            except Exception as e:
                pass
            sender_frame.pack_forget()
            receiver_frame.pack_forget()
            main_frame.pack(fill='both', expand=True)

        def drop(event):
            files = self.root.tk.splitlist(event.data)
            for file in files:
                self.dropped_files.add(file)
                filename = os.path.basename(file)
                current_text = self.output_box1.cget("text")
                if current_text:
                    self.output_box1.config(text=current_text + "\n" + filename)
                else:
                    self.output_box1.config(text=filename)

        

        def send_file():
            pin = sender_ip_entry.get()
            print(pin)
            self.is_sending = True
            try:
                with socket.socket(socket.AF_INET, socket.SOCK_DGRAM) as s:
                    s.connect(('8.8.8.8', 80))
                    self.local_ip = s.getsockname()[0]
                    s.close()
            except Exception as e:
                self.local_ip = '127.0.0.1'

            segments = self.local_ip.split('.')
            if len(segments) == 4:
                self.local_ip = '.'.join(segments[:3]) + '.'
            while self.is_sending:
                try:
                    ip = pin if len(pin) > 3 else (self.local_ip + pin)
                    self.client_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
                    self.client_socket.connect((ip, 8000))
                    if self.client_socket:
                        print("Connected to the server")
                        current_text = self.output_box2.cget("text")
                        self.output_box2.config(text=current_text + "\n" + "Connected!\n")
                        self.connected = True
                        break
                except Exception as e:
                    # print(e)
                    pass
            
            while self.is_sending:
                try:
                    file = self.dropped_files.pop()
                    self.recvAck()
                    self.sendFilename(file)
                    self.recvAck()
                    self.sendFilesize(file)
                    self.recvAck()
                    self.sendFiledata(file)
                except Exception as e:
                    pass
            try:
                self.client_socket.close()
            except Exception as e:
                print(e, ' nusrat9089')

        def start_send_file_thread():
            if not self.connected:
                threading.Thread(target=send_file).start()

        self.root = TkinterDnD.Tk()
        self.root.title("KYAU - File Share")
        self.root.geometry("930x600")
        self.root.configure(bg='#4b3f72')
        self.root.protocol("WM_DELETE_WINDOW", self.on_close)

        main_frame = tk.Frame(self.root, bg='#4b3f72')
        main_frame.pack(fill='both', expand=True)

        custom_font = font.Font(family="Helvetica", size=24, weight="bold")
        label = tk.Label(main_frame, text="Khwaja Yunus Ali University", bg='#4b3f72', fg='#000000', font=custom_font)
        label.pack(pady=40)

        button_frame = tk.Frame(main_frame, bg='#4b3f72')
        button_frame.pack(pady=20)

        button_style = {
            "bg": "#95c8d8",
            "fg": "#000000",
            "activebackground": "#87aeb5",
            "width": 10,
            "height": 2,
            "font": ("Helvetica", 14)
        }

        sender_button = tk.Button(button_frame, text="Sender", command=switch_to_sender_view, **button_style)
        receiver_button = tk.Button(button_frame, text="Receiver", command=switch_to_receiver_view, **button_style)

        sender_button.grid(row=0, column=0, padx=20)
        receiver_button.grid(row=0, column=1, padx=20)
        
        sender_frame = tk.Frame(self.root, bg='#4b3f72')

        sender_ip_label = tk.Label(sender_frame, text="Enter PIN ", bg='#4b3f72', fg='#000000', font=("Helvetica", 12))
        sender_ip_label.pack(pady=5)

        sender_ip_entry = tk.Entry(sender_frame,  font=("Helvetica", 14))
        sender_ip_entry.pack(pady=5)
        sender_ip_entry.insert(0, "105")

        self.output_frame = tk.Frame(sender_frame, bg='#4b3f72')
        self.output_frame.pack(pady=10)

        self.output_box1 = tk.Label(self.output_frame, bg='#404040', fg='#ffffff', width=60, height=20, relief="solid", bd=2, text="", anchor="nw", justify="left")
        self.output_box1.grid(row=0, column=0, padx=10)

        self.output_box2 = tk.Label(self.output_frame, bg='#95c8d8', width=20, height=10, relief="solid", bd=2, font=font.Font(size=20))
        self.output_box2.grid(row=0, column=1, padx=10)

        button_frame2 = tk.Frame(sender_frame, bg='#4b3f72')
        button_frame2.pack(pady=20)

        self.connect_button = tk.Button(button_frame2, text="Connect", bg="#32cd32", fg="#000000", activebackground="#87aeb5", width=10, height=2, font=("Helvetica", 14), command=start_send_file_thread)
        self.connect_button.grid(row=0, column=0, padx=20)

        cancel_button = tk.Button(button_frame2, text="Cancel", bg="#95c8d8", fg="#000000", activebackground="#87aeb5", width=10, height=2, font=("Helvetica", 14), command=switch_to_main_view)
        cancel_button.grid(row=0, column=1, padx=20)

        self.progress = Progressbar(sender_frame, orient="horizontal", length=400, mode='determinate')
        self.progress.pack(pady=10)

        sender_frame.drop_target_register(DND_FILES)
        sender_frame.dnd_bind('<<Drop>>', drop)
        
        # Receiver frame
        receiver_frame = tk.Frame(self.root, bg='#4b3f72')

        self.receiver_ip_label = tk.Label(receiver_frame, text=f"Local IP: {self.local_ip}", bg='#4b3f72', fg='#000000', font=("Helvetica", 18))
        self.receiver_ip_label.pack(pady=5)

        self.receiver_output_box = tk.Label(receiver_frame, bg='#404040', fg='#ffffff', width=80, height=20, relief="solid", bd=2, text="", anchor="nw", justify="left")
        self.receiver_output_box.pack(pady=10)

        button_frame3 = tk.Frame(receiver_frame, bg='#4b3f72')
        button_frame3.pack(pady=20)

        cancel_button_receiver = tk.Button(button_frame3, text="Cancel", bg="#95c8d8", fg="#000000", activebackground="#87aeb5", width=10, height=2, font=("Helvetica", 14), command=switch_to_main_view)
        cancel_button_receiver.grid(row=0, column=0, padx=20)

        self.progress_receiver = Progressbar(receiver_frame, orient="horizontal", length=400, mode='determinate')
        self.progress_receiver.pack(pady=10)

        self.root.mainloop()

    def get_local_ip(self):
        try:
            with socket.socket(socket.AF_INET, socket.SOCK_DGRAM) as s:
                s.connect(('8.8.8.8', 8000))
                ip = s.getsockname()[0]
                s.close()
                print("ip", ip)
                return ip
        except Exception as e:
            return '127.0.0.1'

    def start_server(self):
        self.server_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        self.server_socket.bind(('', 8000))
        self.server_socket.listen(1)
        print("Server is listening on port 8000")

        while True:
            try:
                self.client_socket, address = self.server_socket.accept()
                print(f"Connection from {address}")
                self.receiving = True
                self.receiver_output_box.config(text = "Connected.\n")
                folder = self.create_folder_with_timestamp()
                threading.Thread(target=self.receive_file, args=(self.client_socket,folder)).start()
                break 
            except Exception as e:
                print(e)
                break

    def create_folder_with_timestamp(self):
        """Creates a folder with the current date and time in YYYY-MM-DD_HH-MM-SS-AMPM format."""
        now = datetime.now()
        hour = now.hour
        minute = now.minute
        second = now.second
        meridian = "AM" if hour < 12 else "PM"

        # Handle hours crossing midday (12 becomes 12PM or 0 becomes 12AM)
        if hour == 0:
            hour = 12
        elif hour > 12:
            hour -= 12

        timestamp = f"{hour:02d}-{minute:02d}-{second:02d}-{meridian}    {now.year:04d}-{now.month:02d}-{now.day:02d}"
        folder_name = os.path.join(os.getcwd(), timestamp)

        try:
            # Create directory with parents if necessary
            os.makedirs(folder_name)
            print(f"Folder created: {folder_name}")
            return folder_name
        except OSError as e:
            print(f"Error creating folder: {e}")


#!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!   Receiving Functions From here   !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!

    def receive_file(self, client_socket, folder):
        while self.receiving:
            try:
                self.sendAck()
                file_name = self.recvFileName()
                if not file_name:
                    break
                self.recvFilesize()
                self.recvFileData(folder)
                self.sendAck()
            except Exception as e:
                print(e)
        try:
            client_socket.close()
        except Exception as e:
            print(e, ' imran102')



    def recvFileName(self):
        try:
            size = self.client_socket.recv(1)[0]
            self.file_name = self.client_socket.recv(size).decode()
            print(f"Received file name: {self.file_name}")
            current_text = self.receiver_output_box.cget("text")
            self.receiver_output_box.config(text=f"{current_text}\n{self.file_name} is now receiving.")
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

    
    def recvFileData(self, folder):
        try:
            totalRecv = 0
            with open(os.path.join(folder,self.file_name), 'wb') as file:
                while totalRecv < self.file_size:
                    left = self.file_size - totalRecv
                    chunkSize = left if left < 1024000 else 1024000
                    chunk = self.client_socket.recv(chunkSize)
                    if not chunk:
                        break  # socket closed or error
                    file.write(chunk)
                    totalRecv += len(chunk)
                    progress = (totalRecv / self.file_size) * 100
                    self.progress_receiver["value"] = progress
                    self.root.update_idletasks()
            print("Binary file receive completed.\n")
            current_text = self.receiver_output_box.cget("text")
            self.receiver_output_box.config(text=f"{current_text}\nSuccess.\n")
        except Exception as e:
            print("Error:", e)
            import traceback
            traceback.print_exc()


#!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!   Receiving Functions End   !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!


    def sendAck(self):
        try:
            data = 'A' * 1024
            self.client_socket.send(data.encode('utf-16'))
        except Exception as e:
            print("An error occurred:", e)

    def recvAck(self):
        try:
            data = self.client_socket.recv(1024)
        except Exception as e:
            print("An error occurred:", e)

    def sendFilename(self, file):
        filename = os.path.basename(file)
        filename_length = len(filename)
        data = struct.pack("!I", filename_length)
        try:
            self.client_socket.send(data)
            self.client_socket.send(filename.encode())
        except Exception as e:
            print(e)

    def sendFilesize(self, file):
        try:
            filesize = os.path.getsize(file)
            data = struct.pack("!Q", filesize)
            self.client_socket.send(data)
            print(f"Sent filesize: {filesize} bytes")
            return filesize
        except Exception as e:
            print(e)
            return 0

    def sendFiledata(self, file):
        try:
            filesize = os.path.getsize(file)
            bytes_sent = 0
            with open(file, "rb") as f:
                while True:
                    data = f.read(1024000)
                    if not data:
                        break
                    self.client_socket.sendall(data)
                    bytes_sent += len(data)
                    progress = (bytes_sent / filesize) * 100
                    self.progress["value"] = progress
                    self.root.update_idletasks()
                print("File sent successfully\n")
        except Exception as e:
            print(e)
            self.output_box2.config(text="Disconnected!", font=font.Font(size=20))
            self.connect_button.config(state='disabled')

if __name__ == "__main__":
    obj = KYAU_File_Share()
