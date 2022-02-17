package coin;

/* this class just packet some method about deal our input image
 * 
 * 	ver		time		by		description
 * ---------------------------------------------
 *	1.0		????		GC		第一版 
 *	2.0		180830		GC		第二版 更改資料格式 捨棄小波轉換
 *	3.0		180917		GC		新增static method doByMat()
 *	3.1		181004		GC		修正resize方法 
 *	3.2		181007		GC		改回雙通道ver
 *	4.00	181027		GC		添加high pass處理 //及未完成處理
 */


import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import OurCNN.OurDef;

public class DealImg {
	static {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
	}
//	public static void main(String[] args){
//		zacheen.test.showResult(doByPath("C:\\coin\\old_undeal_cut\\01\\0\\20180915_012115_5960.jpg"));
//	}
	
	public static Mat doByPath(String imgPath){
		return doByMat(Imgcodecs.imread(imgPath));
	}
	
	public static Mat doByMat(Mat input_picture){
		if(OurDef.ModeSelect.equals("GRAY")){		//gray mode
			return doByMatGRAY(input_picture);
		}
		else if(OurDef.ModeSelect.equals("LB")){		// LB mode
			return doByMatLB(input_picture);
		}
		else{		//Other unknown mode type
			return doByMatUnknown(input_picture);
		}
	}
	
//	public static Mat doByMat(Mat input_picture){// edge ver
//		//Mat	input_picture = Imgcodecs.imread(imgPath);      //read the picture
//		List<Mat> outputC = new ArrayList<Mat>();
//		Mat output_picture = new Mat();
//		Mat temp = new Mat();
//		Mat temp2 = new Mat();
//		
////		Imgproc.resize(input_picture, output_picture, new Size(OurDef.ImgCols, OurDef.ImgRows), 0, 0, Imgproc.INTER_AREA);
////		OurImage.ourBGR2LB(output_picture,output_picture,0,255);
////		Core.split(output_picture, outputC);
////		//OurImage.DMT(outputC.get(0) , temp, OurDef.DMTLayer);
////		OurImage.equalize(outputC.get(0), temp, 0);
////		//OurImage.ourEdge(outputC.get(0) , temp, false);		//edges dect
////		Imgproc.resize(temp, temp2, new Size(OurDef.ImgCols, OurDef.ImgRows), 0, 0, Imgproc.INTER_AREA);;
////		//temp2.convertTo(temp, OurDef.ImgType);
//		//outputC.set(0, temp2);
//		//Core.merge(outputC, output_picture);
//		OurImage.ourBGR2LB(input_picture,output_picture,0,255);
//		Core.split(output_picture, outputC);
//		{
//			temp = outputC.get(0);
//			//OurImage.ourEdge(temp , temp, false);		//edges dect
//			//test.showResult(temp);
//			double target = (temp.width()/OurDef.ImgCols);
//			int targetI =  (int) target;
//			targetI /= 1;
//			if((targetI%2) == 0)
//				targetI += 1;
//			Imgproc.medianBlur(temp, temp, targetI);
//			//Imgproc.Canny(temp, temp, 40, 50);
//			OurImage.ourEdge(temp, temp);
//			target /= 3;
//			if(target <= 3){
//				target = 3;
//			}
//			Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(target, target));
//			Imgproc.dilate(temp, temp, kernel);
//			Imgproc.erode(temp, temp, kernel);
//			//test.showResult(temp);
//			Imgproc.resize(temp, temp2, new Size(OurDef.ImgCols, OurDef.ImgRows), 0, 0, Imgproc.INTER_AREA);
//		}
//		
//		
//		temp = outputC.get(1);
//		Imgproc.resize(temp, temp, new Size(OurDef.ImgCols, OurDef.ImgRows), 0, 0, Imgproc.INTER_AREA);
//		output_picture = new Mat(OurDef.DigRows, OurDef.DigCols, OurDef.ImgType);
//		temp2.copyTo(output_picture.submat(0, OurDef.ImgRows, 0, OurDef.ImgCols));
//		temp.copyTo(output_picture.submat(0, OurDef.DigRows, OurDef.ImgCols, OurDef.DigCols));
//		
//		temp = null;
//		temp2 = null;
//		outputC = null;
//		return output_picture;
//	}
//	
	public static Mat doByMatGRAY(Mat input_picture){//old version doByMat (GRAY mode)
		//Mat	input_picture = Imgcodecs.imread(imgPath);      //read the picture
				List<Mat> outputC = new ArrayList<Mat>();
				Mat output_picture = new Mat();
				Mat temp = new Mat();
				Mat temp2 = new Mat();
				Imgproc.cvtColor(input_picture, temp, Imgproc.COLOR_BGR2GRAY);
				Imgproc.resize(temp, output_picture, new Size(OurDef.ImgCols, OurDef.ImgRows), 0, 0, Imgproc.INTER_AREA);
				//high pass
				//Imgproc.blur(temp, temp, new Size(3,3));
				//Imgproc.medianBlur(temp, temp3, 3);
				Imgproc.GaussianBlur(temp, temp2, new Size(11, 11), 0, 0);
				Core.addWeighted(temp, 1.0, temp2, -1.0, 127, temp);
				OurImage.equalize(output_picture, output_picture, 0);
				return output_picture;
	}
	
	public static Mat doByMatLB(Mat input_picture){//old version doByMat (LBmode)
		//Mat	input_picture = Imgcodecs.imread(imgPath);      //read the picture
				List<Mat> outputC = new ArrayList<Mat>();
				Mat output_picture = new Mat();
				Mat temp = new Mat();
				Mat temp2 = new Mat();
				try{
					Imgproc.resize(input_picture, output_picture, new Size(OurDef.ImgCols, OurDef.ImgRows), 0, 0, Imgproc.INTER_AREA);
					OurImage.ourBGR2LB(output_picture,output_picture,0,255);
					Core.split(output_picture, outputC);
					temp = outputC.get(0);
					//zacheen.Cut_coin_shih_Main.showResult(temp);
					//OurImage.DMT(outputC.get(0) , temp, OurDef.DMTLayer);
					//zacheen.test.showResult(temp);
					Mat kernel = new Mat(3, 3, CvType.CV_32F);
	//				float[] data = {
	//						-1/2, -3/4, -1/2,
	//						-3/4, 1, -3/4,
	//						-1/2, -3/4, -1/2};
	//				kernel.put(0, 0, data);
	//				Imgproc.filter2D(temp, temp, temp.depth(), kernel);
					Mat temp3 = new Mat();
						//high pass
					//Imgproc.blur(temp, temp, new Size(3,3));
					//Imgproc.medianBlur(temp, temp3, 3);
					Imgproc.GaussianBlur(temp, temp2, new Size(11, 11), 0, 0);
					Core.addWeighted(temp, 1.0, temp2, -1.0, 127, temp);
					//Core.addWeighted(temp3, 1.0, temp, 1.0, -127, temp);
					//zacheen.test.showResult(temp);
					OurImage.equalize(temp, temp, 0);
					//OurImage.Log(temp, temp, -0.3);
					//Imgproc.blur(temp, temp, new Size(3,3));
					//Imgproc.adaptiveThreshold(temp, temp, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, 63, 50);
					//OurImage.ourEdge(outputC.get(0) , temp, false);		//edges dect
					//Imgproc.resize(temp, temp2, new Size(OurDef.ImgCols, OurDef.ImgRows), 0, 0, Imgproc.INTER_AREA);
					//temp2.convertTo(temp, OurDef.ImgType);
					outputC.set(0, temp);
					Core.merge(outputC, output_picture);
					
					//temp = outputC.get(1);
					//output_picture = new Mat(OurDef.DigRows, OurDef.DigCols, OurDef.ImgType);
					//temp2.copyTo(output_picture.submat(0, OurDef.ImgRows, 0, OurDef.ImgCols));
					//temp.copyTo(output_picture.submat(0, OurDef.DigRows, OurDef.ImgCols, OurDef.DigCols));
				}
				catch (Exception e){
					e.printStackTrace();
				}
				temp.release();
				temp2.release();
				outputC = null;
				return output_picture;
	}
	
	public static Mat doByMatUnknown(Mat input_picture){//old version doByMat (Unknown mode)
		//Mat	input_picture = Imgcodecs.imread(imgPath);      //read the picture
				List<Mat> outputC = new ArrayList<Mat>();
				Mat output_picture = new Mat();
				Mat temp = new Mat();
				Mat temp2 = new Mat();
				
				Imgproc.resize(input_picture, output_picture, new Size(OurDef.ImgCols, OurDef.ImgRows), 0, 0, Imgproc.INTER_AREA);
				Core.split(output_picture, outputC);
				for(int ch = 0; ch < outputC.size(); ch++){
					OurImage.equalize(outputC.get(ch), temp, ch);
					outputC.set(ch, temp);
				}
				Core.merge(outputC, output_picture);
				
				temp.release();
				return output_picture;
	}
}
