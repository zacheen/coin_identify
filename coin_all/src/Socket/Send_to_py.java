package Socket;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

import OurCNN.DATA;

public class Send_to_py implements Runnable{
	private BufferedOutputStream output_stream;
    private DataInputStream input_stream;
    private Lock lock;
    
    int file_num;
    
    
    
    public Send_to_py(int file_num , Lock lock) {
		super();
		this.file_num = file_num;
		this.lock = lock;
	}



	@Override
	public void run()  {
		System.out.println(file_num +" lock");
		lock.lock();
		System.out.println(file_num +" get process");
		try {
            Socket socket = new Socket("localhost",8001);

            
            DATA pass = new DATA("D:\\dont_move\\coin\\test\\01\\0\\socket"+ file_num +".jpg");
            
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
                	
                    output_stream.write(pass.data);
                    output_stream.flush();

                    String predict = input_stream.readLine();
                    System.out.println("recieve is " + predict);
                }
            }catch (Exception e){
            	System.out.println("in run()-run can not connect to server");
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
        	System.out.println(file_num +" unlock");
        }
	}
}
