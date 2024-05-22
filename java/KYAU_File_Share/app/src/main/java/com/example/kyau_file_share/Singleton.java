package com.example.kyau_file_share;

import java.net.Socket;

import kotlin.jvm.Synchronized;

public class Singleton {

    private static Singleton instance;
    public static Socket socket;
    public static String ip;

    private Singleton(){ }

    public static synchronized Singleton  getInstance(){
        if(instance == null){
            instance = new Singleton();
        }
        return instance;
    }



}
