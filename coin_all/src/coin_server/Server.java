package coin_server;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;

import Socket.Send_to_py;
import coin.DealImg;
import cut_coin.CoinCut;

public class Server {
	static {
		
		Properties props = System.getProperties();
        System.out.println("os.name : "+props.get("os.name"));

        String libName = null;
        String OS = props.get("os.name").toString();
        if (OS.contains("indow")) {
            libName = "\\opencv_java300.dll";
            String opencvpath = System.getProperty("user.dir") + "";
            System.out.println("user.dir : "+opencvpath);
            System.load(opencvpath + libName);
            System.out.println("load lib from : "+opencvpath + libName);
        } else if (OS.contains("inux")) {
            libName = "/libopencv_java300.so";
            System.load("/usr/lib/libopencv_java300.so");
        } else {
        	System.out.println("error at loading lib (judging system)");
        }
        //System.loadLibrary(libName);
		
		/*
		String libPath = System.getProperty("java.library.path");
		System.out.println("libPath : "+libPath);
		*/
		
//		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        
        
	}
	
	ServerSocket serverSocket;
	List<Socket> socketlist;
	
	static Lock lock = new ReentrantLock();
	
	final static int server_port = 4567;
	final static int buffer_size = 64;
	
	public Server() {
		super();
		this.serverSocket = null;
		this.socketlist = new ArrayList<>();
	}



	public void start_serving() {
		print_local_IP();

		Thread th_close;

		try{
			serverSocket = new ServerSocket(server_port);
		}catch (Exception e){
			e.printStackTrace();
		}
		
		try{
			th_close=new Thread(Judge_Close);                
			th_close.start();                                
			
			while(!serverSocket.isClosed()) {
				waitNewSocket();
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	
	
	private void print_local_IP(){
		String site = "localhost";
		try
		{
			InetAddress host = InetAddress.getByName(site);
			System.out.println("get host name : " + host.getHostName());
			System.out.println("host IP address : " + host.getHostAddress());
			System.out.println("local host address : " + InetAddress.getLocalHost());
		}
		catch(Exception e)
		{
			System.out.println("error at getting IP !") ;
		}
	}

	private Runnable Judge_Close=new Runnable(){    
		@Override
		public void run() {                          
			try{
				while(true){
					Thread.sleep(2000);         
					try{
						for(Socket close:socketlist){
							if(isServerClose(close))        
								socketlist.remove(close);
						}
					}catch(Exception e){
						System.out.println("here is wrong that two process change at once but I dont know where wrong and no thing matter");
					}
				}
			}catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	};
	
	private Boolean isServerClose(Socket socket){    
		try{  
			socket.sendUrgentData(0);        
			return false;                     //false
		}catch(Exception e){
			return true;                      //true
		}
	}

	// 2.
	public void waitNewSocket() {
		try {
			//ready to let client connect in
			Socket socket = serverSocket.accept();
			// exe here mean there is a new client connect in
			createNewThread(socket);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	

	public void createNewThread(final Socket socket) {
		Thread t = new Thread(new Runnable() {
			
			@Override
			public void run() {
				// in thread so every thing have to done here
				try{
					
					socketlist.add(socket);
				
					if (socket.isConnected()) {
						//cut code
						BufferedInputStream buffer_in = new BufferedInputStream(socket.getInputStream());
						DataInputStream input_stream = new DataInputStream(buffer_in);
						BufferedOutputStream buffer_out = new BufferedOutputStream(socket.getOutputStream());
						DataOutputStream output_stream = new DataOutputStream(buffer_out);
						long data_length = input_stream.readInt();
						System.out.println("recieve len : " + data_length);

						byte[] bytes = new byte[(int)data_length];
						
						for(int i = 0;i < data_length;){
							int rend_len = input_stream.read(bytes,i,(int)data_length-i);
							i = i + rend_len;
							//System.out.println("recieve bytes len: "+rend_len +" i = "+i);
						}
											
						
//						int rend_len = input_stream.read(bytes,0,);
//						System.out.println("recieve bytes len: "+rend_len);
						
						
						//save
						{
							try {
								//String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
								//FileOutputStream outputStream  =new FileOutputStream(new File("server_recieve_"+timeStamp+".jpg"));
								//outputStream.write(bytes);
								//outputStream.flush();
								//outputStream.close();
							} catch (Exception e) {
								e.printStackTrace();
							}
						}

						// finish geting the var of Image
						
						Mat mat = Imgcodecs.imdecode(new MatOfByte(bytes), Imgcodecs.CV_LOAD_IMAGE_UNCHANGED);
						System.out.println("mat size : " + mat.cols()+" * "+mat.rows());
						//Imgcodecs.imwrite("/home/ubuntu/mat.jpg", mat);
//						test.showResult(mat);
						
						SimpleDateFormat sdFormat = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss");
						
						System.out.println(sdFormat.format(new Date()));
						CoinCut coin_cut = new CoinCut();
						List<Mat> cut_out = coin_cut.from(mat,CoinCut.METHOD.NORMAL);
						System.out.println(sdFormat.format(new Date()));
						
						
						System.out.println("cut out : "+cut_out.size());
						int counting = 0;
						
						ArrayList<FutureTask<Integer>> list = new ArrayList<FutureTask<Integer>>();
						
						for(Mat cut_pic : cut_out) {
//							Imgcodecs.imwrite("cut_out_go_into_predict"+ counting +".jpg", cut_pic);
							Mat lab = DealImg.doByMatLB(cut_pic);
							Callable<Integer> thread = new Send_mat_to_py(lock,lab);
							FutureTask<Integer> task = new FutureTask<Integer>(thread);
						    // Start thread.
						    Thread t = new Thread(task);
						    t.start();
						    // Add to list.
						    list.add(task);
						}
						
						ArrayList<Integer> all_predict = new ArrayList<Integer>();
						for(FutureTask<Integer> task : list) {
						    try {
//						    	System.out.println("task.get()"+task.get());
						        all_predict.add(task.get());
						    } catch (Exception e) {
						        e.printStackTrace();
						    }
						}
						
						//send back to app
						Mat draw_pic = coin_cut.drawAnswer(all_predict);
						//Imgcodecs.imwrite("draw.jpg", draw_pic);
						//zacheen.Cut_coin_shih_Main.showResult(draw_pic);
						
						MatOfByte return_byte = new MatOfByte();
						Imgcodecs.imencode(".jpg", draw_pic, return_byte);
						
						byte[] pass = return_byte.toArray();
						System.out.println("send to app : "+ pass.length);
						output_stream.writeInt(pass.length);
						output_stream.write(pass);
					}
				}catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		//
		t.start();
	}
}
