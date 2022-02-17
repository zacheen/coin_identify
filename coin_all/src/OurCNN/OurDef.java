package OurCNN;
/*版本說明
 * 	ver		time		by		description
 * ---------------------------------------------
 *	1.0		??????		GC		
 *	2.0		180917		GC		新增ImgDealType
 *	3.0		??????		zacheen	新增環境歧異選擇
 *	3.1		181012		GC		gary data mode
 *	3.2		181101		GC		新增 畫框框的粗細參數
 */

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;

/**
 * this class is for some define value in our data
 *
 */
import org.opencv.core.CvType;
import org.opencv.core.Rect;

public class OurDef {
	/* here is the modes of our trainning data */
	public final static String ModeSelect = "LB";	//mode please refer to OurDef.ModeCH
	
	public final static Map<String,Integer> ModeCH = new HashMap<String, Integer>(){{
		//declare the mode of data can be selected
		put("GRAY", 1);		//gray mode
		put("LB", 2);		//LB mode , our origin mode
		put("BGR", 3);		//BGR mode, the jpg mode
	}};
	/* here is how to draw answer */
	public final static int frameThickness = 30;
	public final static Rect LabelRect = new Rect(0, 0, 1200, 200);
	public final static double fontScale = 5;
	public final static int fontThick = (int) (LabelRect.width * 0.01);
	
	/* here is the target image size of origin image */
	public final static int ImgCh = ModeCH.get(ModeSelect);	// the number of image channels
	public final static int ImgCols = 128;	
	public final static int ImgRows = ImgCols;
	public final static int ImgType = CvType.CV_8U;
	public final static int ImgDealType = CvType.makeType(ImgType, ImgCh);
	
	/* here is the target data size of the image in the learning data */
	public final static int DMTLayer = 2;		//just used for DWT, not used in project now
	public final static int DigCols = ImgCols * ImgCh;
	public final static int DigRows = ImgRows;

	/* here is the target data size of the image in the learning data */
	public final static int CNNLevel = ImgCh;
	public final static int CNNDigCols = ImgCols;
	public final static int CNNDigRows = ImgRows;
	
	/* here is the labels in our trainning data */
	public final static int Unknown = 0;
	public final static int TWD_01_0 = 1;
	public final static int TWD_01_1 = 2;
	public final static int TWD_05_0 = 3;
	public final static int TWD_05_1 = 4;
	public final static int TWD_10_0 = 5;
	public final static int TWD_10_1 = 6;
	public final static int TWD_50_0 = 7;
	public final static int TWD_50_1 = 8;
	public static String Label(int label){
		switch(label){
			case OurDef.Unknown:
				return "Not $ ";
			case OurDef.TWD_01_0:
				return "NT$ 1 (F)";
			case OurDef.TWD_01_1:
				return "NT$ 1 (B)";
			case OurDef.TWD_05_0:
				return "NT$ 5 (F)";
			case OurDef.TWD_05_1:
				return "NT$ 5 (B)";
			case OurDef.TWD_10_0:
				return "NT$ 10 (F)";
			case OurDef.TWD_10_1:
				return "NT$ 10 (B)";
			case OurDef.TWD_50_0:
				return "NT$ 50 (F)";
			case OurDef.TWD_50_1:
				return "NT$ 50 (B)";
			default:
				return "Untag $";	
		}
	}
	
	/* the path about data and database */
	public final static String[] OS = {"Linux","win_za","win_gc"};
	
	public static  String trainSrc = null,trainDst = null,testDst = null,testSrc = null,module_dst = null,write_dst_loss = null,write_dst_compare = null,good_model_dst = null;
	
	public static double learning_rate;
	public static int train_again;
	public static int epoch;

	static {
		Scanner scn = new Scanner(System.in);
		
		String os_is = OS[1];
		int model = 5;
		learning_rate = 0.02;
		train_again = 0;
		epoch = 100;
		
		Properties props = System.getProperties();
        System.out.println("os.name : "+props.get("os.name"));

        String OS_name = props.get("os.name").toString();
        if (OS_name.contains("inux")) {
        	os_is = OS[0];
        } else if (OS_name.contains("indow")) {
        	System.out.print("OS (1-2 1=Win_za 2=win_GC) : ");
    		try {
    			os_is = OS[scn.nextInt()];
    			System.out.println(os_is);
    		} catch (Exception e) {
    			os_is = "Linux";
    		}
        }
        
		
		
		/*
		System.out.print("which module(int) : ");
		try {
			model = scn.nextInt();
		} catch (Exception e) {
			
		}
		
		System.out.print("learning rate (double) : ");
		try {
			learning_rate = scn.nextDouble();
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("go to default learning rate = 0.02");
		}
		
		System.out.print("train_again?? (1 == again): ");
		try {
			train_again = scn.nextInt();
		} catch (Exception e) {
			System.out.println("go to default train_again = 0");
		}
		System.out.print("epoch : ");
		try {
			epoch = scn.nextInt();
		} catch (Exception e) {
			System.out.println("go to default epoch = 10");
		}
		*/
		
		if(os_is.equals("Linux")) {
			trainSrc = "/home/ubuntu/train";
			trainDst = "/home/ubuntu/train.data";
			testSrc = "/home/ubuntu/test";
			testDst = "/home/ubuntu/test.data";
			module_dst = "/home/ubuntu/CNN"+ model +".model";
			good_model_dst = "/home/ubuntu/good/";
			write_dst_loss = "/home/ubuntu/result_loss"+ model +".txt";
			write_dst_compare = "/home/ubuntu/compare"+ model +".txt";
		}else if(os_is.equals("win_za")){
			trainSrc = "D:\\dont_move\\coin\\train";
			trainDst = "D:\\dont_move\\coin\\train.data";
			testSrc = "D:\\dont_move\\coin\\test";
			testDst = "D:\\dont_move\\coin\\test.data";
			module_dst = "D:\\dont_move\\coin\\CNN"+ model +".model";
			good_model_dst = "D:\\dont_move\\coin\\good_model\\";
			write_dst_loss = "D:\\dont_move\\coin\\result_loss"+ model +".txt";
			write_dst_compare = "D:\\dont_move\\coin\\compare"+ model +".txt";
		}else {
			trainSrc = "C:\\coin\\train";
			trainDst = "C:\\coin\\train.data";
			testSrc = "C:\\coin\\bug\\bugTest";
			testDst = "C:\\coin\\test.data";
			module_dst = "C:\\coin\\CNN"+ model +".model";
			good_model_dst = "D:\\dont_move\\coin\\good_model\\";
			write_dst_loss = "C:\\coin\\result_loss"+ model +".txt";
			write_dst_compare = "C:\\coin\\compare"+ model +".txt";
		}
		
	}
	
}
