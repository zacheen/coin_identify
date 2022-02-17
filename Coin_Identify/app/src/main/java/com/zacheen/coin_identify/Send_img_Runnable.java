package com.zacheen.coin_identify;

import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;

public class Send_img_Runnable implements Runnable {
    private DataOutputStream output_stream;
    private DataInputStream input_stream;
    private String tmp;                    //做為接收時的緩存
    String server_ip;
    int server_port;

    private Socket clientSocket;
    private byte[] data;

    public Send_img_Runnable(String server_ip, int server_port, byte[] data){
        try {
            this.clientSocket = new Socket(server_ip, server_port);
            Log.i("network_success","success to get Socket");
        }catch (Exception e){
            e.printStackTrace();
            Log.i("network error","in Send_img_Runnable constructor" + e.toString());
        }
        this.data = data;
    }

    public void run() {
        try{
            //取得網路輸出串流
            input_stream = new DataInputStream(new BufferedInputStream(clientSocket.getInputStream()));
            output_stream = new DataOutputStream(new BufferedOutputStream(clientSocket.getOutputStream()));
            // 當連線後
            try {
                if (clientSocket.isConnected()) {
                    int data_length = this.data.length;
                    Log.i("network_send_var", "data.length:  " +data_length + " height: "  + " width: " );

                    output_stream.writeInt(data_length);
                    Log.i("network_send", this.data + "");

                    output_stream.write(this.data);
                    output_stream.flush();

                    if(input_stream.readInt()==1){
                        Log.i("network_success", "recieve is 1  success to send a picture");
                    }else{
                        Log.i("network_error", "recieve is not 1");
                    }
                }
            }catch (Exception e){
                Log.i("network error","in run()-run can not connect to server");
            }
            output_stream.close();
            clientSocket.close();
            Log.i("network send", "send a picture finish");
        }catch(Exception e){
            //當斷線時會跳到catch,可以在這裡寫上斷開連線後的處理
            e.printStackTrace();
            Log.e("network","in run() Socket連線 = "+e.toString());
            try {
                clientSocket.close();
            }catch (Exception ee) {
                Log.i("network error","in run() - catch"+ee.toString());
            }
        }
        //end this thread
    }
}
