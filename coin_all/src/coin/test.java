package coin;
/* this class is not important
 * just for test some coding method
 * 
 */

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

public class test {
	public static String picture ;
	
	static {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
	}

	public static void showResult(Mat img) {
	    //Imgproc.resize(img, img, new Size(640, 480));
	    MatOfByte matOfByte = new MatOfByte();
	    Imgcodecs.imencode(".jpg", img, matOfByte);
	    byte[] byteArray = matOfByte.toArray();
	    BufferedImage bufImage = null;
	    try {
	        InputStream in = new ByteArrayInputStream(byteArray);
	        bufImage = ImageIO.read(in);
	        JFrame frame = new JFrame();
	        frame.getContentPane().add(new JLabel(new ImageIcon(bufImage)));
	        frame.pack();
	        frame.setVisible(true);
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}
	
	private static void setImg(Mat img) {
	    if(img == null){
	    	img = new Mat(new Size(7,4), CvType.CV_8UC3);
	    }
	    else{
	    	img.create(new Size(7,4), CvType.CV_8UC3);
	    }
	    byte[] input =
	    	{
	    			//
	    			127, 127, 127 ,/**/0, 0, 0 ,/**/0, 0, 0 ,/**/0, 0, 0 ,/**/
    				0, 0, 0 ,/**/0, 0, 0 ,/**/0, 0, 0 ,/**/0, 0, 0 ,/**/
    			
    			
    				0, 0, 0 ,/**/0, 0, 0 ,/**/0, 0, 0 ,/**/0, 0, 0 ,/**/
    				0, 0, 0 ,/**/0, 0, 0 ,/**/0, 0, 0 ,/**/0, 0, 0 ,/**/
    			
    			
    				0, 0, 0 ,/**/50, 50, 0 ,/**/0, 0, 0 ,/**/0, 0, 0 ,/**/
    				0, 0, 0 ,/**/0, 0, 0 ,/**/0, 0, 0 ,/**/0, 0, 0 ,/**/
    			
    			
    				0, 0, 0 ,/**/0, 0, 0 ,/**/0, 0, 0 ,/**/0, 0, 0 ,/**/
    				0, 0, 0 ,/**/0, 0, 0 ,/**/0, 0, 0 ,/**/0, 0, 0 , /**/
    			
    				0, 0, 0 ,/**/0, 0, 0 ,/**/0, 0, 0 ,/**/0, 0, 0 ,/**/
    				0, 0, 0 ,/**/0, 0, 0 ,/**/0, 0, 0 ,/**/0, 0, 0 ,/**/
    			
    			
    				0, 0, 0 ,/**/0, 0, 0 ,/**/0, 0, 0 ,/**/0, 0, 0 ,/**/
    				0, 0, 0 ,/**/0, 0, 0 ,/**/0, 0, 0 ,/**/0, 0, 0 ,/**/
    			
    			
    				0, 0, 0 ,/**/0, 0, 0 ,/**/0, 0, 0 ,/**/0, 0, 0 ,/**/
    				0, 0, 0 ,/**/0, 0, 0 ,/**/0, 0, 0 ,/**/0, 0, 0 ,/**/
    			
    			
    				0, 0, 0 ,/**/0, 0, 0 ,/**/0, 0, 0 ,/**/0, 0, 0 ,/**/
    				0, 0, 0 ,/**/0, 0, 0 ,/**/0, 0, 0 ,/**/0, 0, 0 /**/
    			
    			
	    	};
	    img.put(0, 0, input);
	}
	
	private static void chImg(Mat A,Mat B) {
		byte[] dataA = new byte[3];
		byte[] dataB = new byte[3];
		for(int i=0;i<A.cols();i++){
			for (int j=0;j<A.cols();j++){
				A.get(i, j, dataA);
				B.get(i, j, dataB);
				System.out.print("( " + ((dataA[0] - dataB[0]) & 0xff) + " , " + ((dataA[1] - dataB[1]) & 0xff) + " , " + ((dataA[2] - dataB[2]) & 0xff) + ") ");
			}
			System.out.println("");
		}
	    
	}
	
	public static void seeImg(Mat A) {
		System.out.println("");
		float[] dataA = new float[3];
		//byte[] dataB = new byte[3];
		for(int i=0;i<A.rows();i++){
			for (int j=0;j<A.cols();j++){
				A.get(i, j, dataA);
				//B.get(i, j, dataB);
				System.out.print("( " + (dataA[0]) + " , " + (dataA[1]) + " , " + (dataA[2]) + ") ");
			}
			System.out.println("");
		}
		System.out.println("**");
	}
	public static void seeImg2(Mat A) {
		byte[] dataA = new byte[3];
		//byte[] dataB = new byte[3];
		for(int i=0;i<A.cols();i++){
			for (int j=0;j<A.cols();j++){
				A.get(i, j, dataA);
				//B.get(i, j, dataB);
				System.out.print("( " + (dataA[0] & 0xff) + " , " + (dataA[1] & 0xff) + " , " + (dataA[2] & 0xff) + ") ");
			}
			System.out.println("");
		}
		System.out.println("**");
	}
	
	public static void seeImg_C2(Mat A) {
		byte[] dataA = new byte[2];
		//byte[] dataB = new byte[3];
		for(int i=0;i<A.cols();i++){
			for (int j=0;j<A.cols();j++){
				A.get(i, j, dataA);
				//B.get(i, j, dataB);
				System.out.print("( " + (dataA[0] & 0xff) + " , " + (dataA[1] & 0xff) + ") ");
			}
			System.out.println("");
		}
		System.out.println("**");
	}
	
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		long t = 0;
		/*int r,b,g;
		r = 128;
		b = 128;
		g = 128;
		byte[] input = new byte[3] ;
		input[0] = (byte) b;
		input[1] = (byte) g;
		input[2] = (byte) r;
		OurImage.BGR2LB(input,0,255);*/
		
		picture = "C:\\coin\\src\\10\\0\\10_0_1.jpg";
		Mat	input_picture = Imgcodecs.imread(picture);      //read the picture
		showResult(input_picture);
		List<Mat> outputC = new ArrayList<Mat>();
		Mat output_picture = input_picture.clone();
		
		/*
		Mat input_picture =  new Mat(),output_picture = new Mat();
		setImg(input_picture);
		input_picture.copyTo(output_picture);*/
		
		t = System.currentTimeMillis( );
		//OurImage.BGR2LB(input_picture,output_picture);
		OurImage.ourBGR2LB(input_picture,output_picture,0,255);
		//OurImage.ourBGR2LB(input_picture,output_picture,133,147);
		//Imgproc.cvtColor(input_picture, output_picture, Imgproc.COLOR_BGR2Lab);
		//OurImage.toPolar(output_picture, output_picture);
		OurImage.equalize(output_picture, output_picture, 0);
		//OurImage.equalize(output_picture, output_picture, 1);
		//OurImage.equalize(output_picture, output_picture, 2);
		//OurImage.findEdge(outputC.get(0), outputC.get(0), new Size(3,3), 50, 100);
		//OurImage.findEdge4LB(output_picture, output_picture, new Size(3,3), 10, 20);
		//seeImg_C2(output_picture);
		//OurImage.DMT(output_picture, output_picture, 2);
		//OurImage.ourErode(output_picture, output_picture, 0);
		//seeImg(output_picture);
		Core.split(output_picture, outputC);
		for(int i = 0 ; i < outputC.size();i++){
			showResult(outputC.get(i));
		}
		
		//showResult(output_picture);
		//OurImage.IDMT(output_picture, output_picture, 2);
		//OurImage.ourErode(output_picture, output_picture, 0);
		//showResult(output_picture);
		
		//OurImage.findEdge4LB(output_picture, output_picture, new Size(3,3), 10, 20);
		//chImg(input_picture,output_picture);
		//seeImg2(output_picture);
		//seeImg_C2(output_picture);
		/*Core.split(output_picture, outputC);
		for(int i = 0 ; i < outputC.size();i++){
			showResult(outputC.get(i));
		}*/
		//showResult(output_picture);
		System.out.println("time    " + (System.currentTimeMillis( ) - t));
	}
}
