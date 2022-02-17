package cut_coin;
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
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;

public class test {
	static {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
	}
	private static int resultNum = 0;
	final static String notFindMessage = "無法找出硬幣位置! 請嘗試重新拍攝 或 調整攝影距離 及 硬幣位置 !";
	final static boolean y = true;
	final static boolean n = false;
	
	public static void main(String[] args) {
		if(n){
			String imgPath = "C:\\coin\\undeal\\55.jpg";
			CoinCut coinCut_non_static = new CoinCut();
			List<Mat> coins = coinCut_non_static.from(imgPath,false);
			int i = 0;
			for (Mat mat : coins) {
				i++;
				Imgcodecs.imwrite("IMG"+i, mat);
			}
		}
		else{
			String srcPath = "C:\\coin\\undeal\\";
			String fileName = ".jpg";
			List<Mat> COINS = new ArrayList<Mat>();
			int count = 0;
			double notFind = 0;
			
			for(int i = 19 ; i <= 56 ; i++){
				String filePath = srcPath + i + fileName;
				CoinCut coinCut_non_static = new CoinCut();
				List<Mat> coins = coinCut_non_static.from(filePath,false);
				count ++ ;
				if( coins.size() == 0){
					notFind += 1;
					System.out.println(filePath + " find objects : " + notFindMessage);
				}
				else{
					System.out.println(filePath + " find objects : " + coins.size());
				}
				COINS.addAll(coins);
			}
			System.out.println("not find rate " + notFind/count);
			for(Mat coin : COINS){
				test.showResult(coin);
			}
		}
		
	}

	public static void showResult(Mat img) {
		resultNum++;
		showResult(img, "" + resultNum);
	}
	public static void showResult(Mat img , String title) {
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
	        frame.setTitle(title);
	        frame.setVisible(true);
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}
}
