package OurCNN;
/*版本說明
 * 	ver		time		by		description
 * ---------------------------------------------
 *	1.0		??????		GC		
 *	2.0		180917		GC		新增static method takeLabel(path)
 *	2.1		181004		GC		修正resize 方法 
 *	2.2		181012		GC		應對ImgCh變動修正
 */

/* this is a structure of one data in our train data base
 * DATA(String path) // create DATA by image path
 * DATA(int label, Mat src) // create DATA by label and image Mat
 * set(int label, Mat src)	//set DATA
 * set(int label, byte[] src)	//set DATA
 * int takeLabel(String path)	//get the label by image file path
 */

import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import java.io.Serializable;
import coin.DealImg;

public class DATA implements Serializable{
	public int label;
	public byte[] data;
	
	public DATA(String path){	//create DATA by Path. if renew labels ,please renew here
		int label = takeLabel(path);
		Mat imageData = DealImg.doByPath(path);
		//System.out.println("label : " + label);
		set(label, imageData);
		//System.out.println("get label : " + label +" data from " + path);
	}
	
	public DATA(int label, Mat src){
		set(label, src);	
		//System.out.println("get label : " + label +" data from ?");
	}
	
	private void set(int label, Mat src){
		
		if(src.empty()){
			throw new IllegalArgumentException("src is empty");
		}
		Mat temp = new Mat();
		if(src.channels() == OurDef.ImgCh){
			Imgproc.resize(src, temp, new Size(OurDef.ImgCols, OurDef.ImgRows), 0, 0, Imgproc.INTER_AREA);
		}
		else{
			throw new IllegalArgumentException("src.channels() " + src.channels() + " != OurDef.ImgCh " + OurDef.ImgCh);
		}
//		Mat temp = new Mat();
//		Imgproc.resize(src, temp, new Size(OurDef.ImgCols, OurDef.ImgRows), 0, 0, Imgproc.INTER_AREA);
		
		byte[] data = new byte [OurDef.DigCols * OurDef.DigRows];
		temp.get(0, 0, data);
		
		set(label, data);
	}
	
	private void set(int label, byte[] data){
		this.label = label;
		if(data.length != OurDef.DigCols * OurDef.DigRows){
			throw new IllegalArgumentException("DATA:data size is unexpected");
		}
		//this.data = new byte [OurDef.DigCols * OurDef.DigRows];
		this.data = data.clone();	
	}
	
	public static int takeLabel(String path){
		int label = OurDef.Unknown;
		String[] pathList = path.split("\\\\");
		String coinValue = pathList[pathList.length - 3];
		String coinType = pathList[pathList.length -2];
		if(coinValue.equals("01")){
			if(coinType.equals("0"))
				label = OurDef.TWD_01_0;
			else if(coinType.equals("1"))
				label = OurDef.TWD_01_1;
		}
		else if(coinValue.equals("05")){
			if(coinType.equals("0"))
				label = OurDef.TWD_05_0;
			else if(coinType.equals("1"))
				label = OurDef.TWD_05_1;
		}
		else if(coinValue.equals("10")){
			if(coinType.equals("0"))
				label = OurDef.TWD_10_0;
			else if(coinType.equals("1"))
				label = OurDef.TWD_10_1;
		}
		else if(coinValue.equals("50")){
			if(coinType.equals("0"))
				label = OurDef.TWD_50_0;
			else if(coinType.equals("1"))
				label = OurDef.TWD_50_1;
		}
		return label;
	}
	
}
