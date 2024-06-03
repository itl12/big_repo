package com.example.kyau_file_share;

import android.net.Uri;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

import kotlin.jvm.Synchronized;

public class Singleton {

    private static Singleton instance;
    public static Socket socket; // while receiving files
    public static ServerSocket serverSocket; // while sending files

    public static String ip;
    public static List<Uri> uris;

    private Singleton(){ }

    public static synchronized Singleton  getInstance(){
        if(instance == null){
            instance = new Singleton();
        }
        return instance;
    }



}
