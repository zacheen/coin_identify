package coin_server;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.Callable;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.opencv.core.Mat;

import OurCNN.DATA;

public class Send_mat_to_py implements Callable<Integer>{
	private BufferedOutputStream output_stream;
    private DataInputStream input_stream;
    private Lock lock;
    private byte[] byte_to_send;
    private Integer pre;
    
    public Send_mat_to_py(Lock lock , Mat mat) {
		super();
		this.lock = lock;
		byte_to_send = new DATA(0,mat).data;
	}

	@Override
	public Integer call() throws Exception {
		System.out.println(" lock");
		lock.lock();
		System.out.println(" get process");
		try {
            Socket socket = new Socket("localhost",8547);

            output_stream = new BufferedOutputStream(socket.getOutputStream());
            input_stream = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
            try {
                if (socket.isConnected()) {
//                    int data_length = pass.data.length;
//                    System.out.println("data.length:  " +data_length + " height: "  + " width: " );
//                    output_stream.writeInt(data_length);
                    
//                    for(int ii = 0;ii<64;ii++) {
//                    	System.out.println(pass.data[8191-ii] & 0xFF);
//                    }
                	System.out.println("java send to py len : "+byte_to_send.length);
                	
                    output_stream.write(byte_to_send);
                    output_stream.flush();

                    System.out.println("waiting for predict");
                    String predict = input_stream.readLine();
                    System.out.println("java recieve from py is " + predict);
                    
                    // add the rectangle
                    pre = Integer.valueOf(predict);
                }
            }catch (Exception e){
            	System.out.println("error : in run() can not connect to server");
            }
            
            output_stream.close();
            input_stream.close();
            socket.close();
            //socket.shutdownOutput();//关闭输出流

        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }finally{
        	lock.unlock();
        	System.out.println("unlock");
        }
		return pre;
	}
}
