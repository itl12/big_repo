import threading
import tkinter as tk
from tkinter import font
from tkinter.ttk import Progressbar
from tkinterdnd2 import DND_FILES, TkinterDnD
import os
import socket
import struct

class KYAU_File_Share:
    def __init__(self):
        self.client_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        self.connected = False
        self.is_sending = False
        self.dropped_files = set()
        self.create_window()

    def on_close(self):
        print("Window is closing. Running cleanup tasks...")
        self.is_sending = False
        self.root.destroy()

    def create_window(self):
        def switch_to_sender_view():
            main_frame.pack_forget()
            sender_frame.pack(fill='both', expand=True)

        def switch_to_main_view():
            self.is_sending = False
            self.connected = False
            self.output_box1.config(text="")
            self.output_box2.config(text="")
            self.connect_button.config(state='active')
            self.progress["value"] = 0
            self.root.update_idletasks()
            sender_frame.pack_forget()
            try:
                self.client_socket.close()
            except Exception as e:
                pass
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
            except Exception as e:
                self.local_ip = '127.0.0.1'

            segments = self.local_ip.split('.')
            if len(segments) == 4:
                self.local_ip = '.'.join(segments[:3]) + '.'
            while self.is_sending:
                try:
                    self.client_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
                    self.client_socket.connect((self.local_ip + pin, 8000))
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
        receiver_button = tk.Button(button_frame, text="Receiver", **button_style)

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
        
        self.root.mainloop()

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
