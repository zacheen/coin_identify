package OurCNN;
/*版本說明
 * 	ver		time		by		description
 * ---------------------------------------------
 *	1.0		??????		GC		
 *	2.0		180917		GC		新增360系列method
 *	2.1		181004		GC		修正resize方法
 *	2.2		181007		GC		修正checkDATA為雙通道ver
 *  2.3		181009		GC		修正database創建消耗記憶體		
 *  2.32	181012		GC		規避database360 stream使用過多
 *  2.33	181017		GC		database bug修正
 */

import java.io.BufferedOutputStream;

/* this class is to create the database for CNN
 * void main()						//create train database
 * void build(String srcPath, String dstPath)		//create database in dstPath by the data from srcPath
 * void build360(String srcPath, String dstPath)	//360ver build
 * List<DATA> DATA360(String)						//建 N筆旋轉DATA
 * void checkDATA(DATA)								//教查單筆DATA
 * void checkDATABase(String)						//教查DATABase內容
 */

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import coin.DealImg;

public class Database {
	private static String trainSrc = OurDef.trainSrc;
	private static String trainDst = OurDef.trainDst;
	private static String testSrc = OurDef.testSrc;
	private static String testDst = OurDef.testDst;
	final private static int bufferBound = 100;
	final private static int CloseBound = 10000;
	private static int bufferCount = 0;
	static String number = OurDef.ImgCols + "";
	
	static String train_dst_byte = "D:\\dont_move\\coin\\train_for_python_byte.data" + number;
	static String train_dst_int = "D:\\dont_move\\coin\\train_for_python_int.data" + number;
	
	static String test_dst_byte = "D:\\dont_move\\coin\\test_for_python_byte.data" + number;
	static String test_dst_int = "D:\\dont_move\\coin\\test_for_python_int.data" + number;
	
	static String one_src = "D:\\dont_move\\coin\\one";
	static String one_dst_byte = "D:\\dont_move\\coin\\one_for_python_byte.data" + number;
	static String one_dst_int = "D:\\dont_move\\coin\\one_for_python_int.data" + number;
	
	static {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
	}
	
	public static void main(String[] args) {	//class test

		{//build train database
			//build(trainSrc, trainDst);
			//build(testSrc, testDst);
		}
		{//build test database
			build_python(trainSrc,train_dst_byte,train_dst_int);
//			build_python(testSrc,test_dst_byte,test_dst_int);
//			build_python(one_src,one_dst_byte,one_dst_int);
		}
		
//		{//check one DATA
//			DATA testD = new DATA("D:\\dont_move\\coin\\final_data\\test\\01\\0\\01_0_20181006_201201_930_1.jpg");
//			for(int i = 0; i<1 ;i++) {
//				System.out.print(i+" : ");
//				System.out.println(testD.data[16514]&0xFF);
//				System.out.println(testD.data[16515]&0xFF);
//			}
//			checkDATA(testD);
//			testD = new DATA("C:\\coin\\tempCut\\20181002_180931_7943.jpg");
//			checkDATA(testD);
//		}
		{// check data base
//			try {
//				checkDATABase(testDst);
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
		}

//		{//check DealImg
//			Mat img = Imgcodecs.imread("C:\\coin\\old_undeal_cut\\50\\1\\20180914_152742_2650.jpg");
//			img = DealImg.doByMat(img);
//		}
	}
	
	public static void build(String srcPath, String dstPath) {  // create database by path
		ArrayList fileList = new ArrayList<DATA>();
		fileList = readAllFile(srcPath);
		ObjectOutputStream dataBase;
		int dataAmount ;	//to record data amount
		if((dataAmount = fileList.size()) < 0){
			System.out.println("build data base error");
		}
		else{
			try {
				dataBase = new ObjectOutputStream(new BufferedOutputStream(new  FileOutputStream(dstPath)));
				dataBase.writeInt(dataAmount);
				for(Object one:fileList.toArray()){
					dataBase.writeObject(one);
					bufferCount ++;
					if((bufferCount % bufferBound) == 0){
						dataBase.flush();
					}
					if(bufferCount > CloseBound){
						dataBase.close();
						dataBase = new ObjectOutputStream(new BufferedOutputStream(new  FileOutputStream(dstPath, true))){
							public void writeStreamHeader(){} //複寫 避免重複寫入檔頭 
						};
						bufferCount = 0;
					}
				}
				dataBase.close();
				System.out.println("create database success :" + dstPath);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
	
	public static void build_python(String srcPath,String dst_byte,String dst_int) {  // create database by path
		ArrayList<DATA> fileList = new ArrayList<DATA>();
		fileList = readAllFile(srcPath);
		
		int dataAmount ;	//to record data amount
		if((dataAmount = fileList.size()) < 0){
			System.out.println("build data base error");
		}
		else{
			try {
				FileOutputStream out_byte  =new FileOutputStream(new File(dst_byte));
				FileWriter out_int = new FileWriter(dst_int);
				
				for(DATA one:fileList){
					out_byte.write(one.data);
//					for(int iii = 4000;iii<4128;iii++) {
//						//System.out.println(Integer.toHexString(one.data[iii]));
//						System.out.println(one.data[iii] & 0xFF);
//					}
					out_int.write(one.label+" ");
					
//					{
//						bufferCount ++;
//						if((bufferCount % bufferBound) == 0){
//							dataBase.flush();
//						}
//						if(bufferCount > CloseBound){
//							dataBase.close();
//							dataBase = new ObjectOutputStream(new BufferedOutputStream(new  FileOutputStream(dstPath, true))){
//								public void writeStreamHeader(){} //複寫 避免重複寫入檔頭 
//							};
//							bufferCount = 0;
//						}
//					}
					{
						out_byte.flush();
						out_int.flush();
					}
					
				}
				out_byte.close();
				out_int.close();
				System.out.println("create database success");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
	
	public static ArrayList readAllFile(String path){	//record all the data image from path
		ArrayList fileList = new ArrayList<DATA>();
		File file = new File(path);
		if(file.isDirectory()){	//get all leaf dir and get file by recursion
			for(String fileName:file.list()){
				fileList.addAll(readAllFile(path + "\\" + fileName));
			}
		}
		else {	//get file
			if(path.toString().contains(".jpg")){
				//fileList.add(path.toString());
				//System.out.println("get " + path);
				fileList.add(new DATA(path));
				//fileList.addAll(DATA360(new DATA(path)));
				return fileList;
			}
			else{// ignore the not jpg file
				return fileList;
			}
		}
		return fileList;
	}
	
	public static void build360(String srcPath, String dstPath) {  // create database by path
		ArrayList<String> fileList = new ArrayList<String>();
		fileList = readAllFile360(srcPath);
		ObjectOutputStream dataBase;
		int dataAmount ;	//to record data amount
		if((dataAmount = fileList.size()) <= 0){
			System.out.println("build data base error");
		}
		else{
			try {
				dataBase = new ObjectOutputStream(new BufferedOutputStream(new  FileOutputStream(dstPath)));
				dataBase.writeInt(dataAmount);
				for(String onePath:fileList){
					List<DATA> pass = DATA360(onePath);
					for(DATA oneDATA : pass){
						dataBase.writeObject(oneDATA);
						bufferCount ++;
						if((bufferCount % bufferBound) == 0){
							dataBase.flush();
						}
						if(bufferCount > CloseBound){
							dataBase.close();
							dataBase = new ObjectOutputStream(new BufferedOutputStream(new  FileOutputStream(dstPath, true))){
								public void writeStreamHeader(){} //複寫 避免重複寫入檔頭 
							};
							bufferCount = 0;
						}
					}
				}
				dataBase.close();
				System.out.println("create database success :" + dstPath);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
	
	public static ArrayList<String> readAllFile360(String path){	//record all the data image from path
		ArrayList<String> fileList = new ArrayList<String>();
		File file = new File(path);
		if(file.isDirectory()){	//get all leaf dir and get file by recursion
			for(String fileName:file.list()){
				fileList.addAll(readAllFile360(path + "\\" + fileName));
			}
		}
		else {	//get file
			if(path.toString().contains(".jpg")){
				//fileList.add(path.toString());
				//System.out.println("get " + path);
				//fileList.add(new DATA(path));
				fileList.add(path);
				return fileList;
			}
			else{// ignore the not jpg file
				return fileList;
			}
		}
		return fileList;
	}
	
	public static List<DATA> DATA360(String path){
		List<DATA> outputs = new ArrayList<DATA>();
		Mat origin = Imgcodecs.imread(path);
		Imgproc.resize(origin, origin, new Size(OurDef.ImgRows, OurDef.ImgCols), 0, 0, Imgproc.INTER_AREA);
		int label = DATA.takeLabel(path);
		System.out.println("get label : " + label +" data360 from " + path);
		System.out.println("1");
		Mat rotAngle;
		for(double i=0;i<360;i += 5.0){
			Mat rotOne = new Mat();
			rotAngle = Imgproc.getRotationMatrix2D(new Point(origin.rows()/2.0,origin.cols()/2.0), i , 1.0);
			Imgproc.warpAffine(origin, rotOne, rotAngle, origin.size(), Imgproc.INTER_LINEAR, Core.BORDER_CONSTANT, new Scalar( 128, 128, 128));
			rotAngle = null;
			outputs.add(new DATA(label, DealImg.doByMat(origin)));
			rotOne = null;
		}
		origin.release();
		origin = null;
		return outputs;
	}

	public static void checkDATABase(String path) throws IOException{
		ObjectInputStream src = new ObjectInputStream(new FileInputStream(path));
		List<DATA> images = new ArrayList<DATA>();
	    int dataNum = src.readInt();
	    try {
	       for(int i = 0; i<dataNum; i++){
	    	   images.add( (DATA) src.readObject());
	       }
	    } catch (ClassNotFoundException e) {
	    	// TODO Auto-generated catch block
	    	e.printStackTrace();
		}
	    
	    DATA t;
	    Scanner scan = new Scanner(System.in);
	    while(true){
	    	System.out.println("please input number 0 ~ " + (images.size()-1));
	    	int index = scan.nextInt();
	    	if(index < 0)
	    		break;
	    	if(index > images.size())
	    		continue;
	    	checkDATA(images.get(index));
	    	
	    }
	    t = null;
	}
	
	public static void checkDATA(DATA one){
		Mat two = new Mat(OurDef.ImgRows, OurDef.ImgCols, OurDef.ImgDealType);
		two.put(0, 0, one.data);
		List<Mat> mv = new ArrayList<Mat>();
		System.out.println(one.label);
		Imgproc.resize(two, two, new Size(OurDef.ImgCols * 5, OurDef.ImgRows * 5));
		Core.split(two, mv);
		//zacheen.test.showResult(two);
		for(Mat gets: mv){
			//zacheen.Cut_coin_shih_Main.showResult(gets);
		}
	}
}
