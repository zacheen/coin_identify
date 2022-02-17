package cut_coin;
/*版本說明
 * 	ver		time		by		description
 * ---------------------------------------------
 *	1.0		180829		GC		第一版 可達成0.6~0.8印幣找出率	1元表現較差		對模糊及光源極度不均勻時無法作用  
 *	2.0		??????				第二版 添加寫檔及優化color convert
 *	3.0		180914		GC		第三版 還原coin cut out品質, 添加 WRITE_FILE參數決定是否寫檔, 取得位置資訊method
 *  4.0		180927		GC		第四版 添加model用方法 請再呼叫from()前 將MODEL_CUT轉true
 *  4.5		180928		GC		微調 model version 參數, 提高精確度
 *  4.6		181002		GC		微調 model version 參數, 提高精確度2
 *  5.0		181101		GC		新建method both 並修改用法
 *  5.1		181112		GC		改變model version threshold方法
 */

/******
 * 	List<Mat> from(String src)	//由src(路徑)檔案中 找出硬幣(Mat) 存於List中
 *	List<Mat> from(Mat src)	//由src(Mat)檔案中 找出硬幣(Mat) 存於List中
 *	List<Rect> getRect()	//取得上次from()中 取的硬幣的位置區域資訊
 *	Mat getOriginImage()	//取得上次from()中用於尋找硬幣的原圖資訊
 *	Mat drawAnswer()		//輸出 帶偵測位置的原圖修改結果
 *	Mat drawAnswer(List<Integer> labels)		//輸出 帶偵測答案的原圖修改結果
 *	Mat drawAnswer(Mat img, List<Rect> pois, List<Integer> labels)		//輸出 帶偵測答案的原圖修改結果
 ***/

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import OurCNN.OurDef;
import coin.OurImage;

public class CoinCut {
	static {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
	}
	final static boolean WRITE_FILE = false;	//want from() to save file or not
	
	public static enum METHOD{//cut method
		MODEL(0),	// cut model version
		NORMAL(1),	// cut normal version
		BOTH(2);	// cut model ver. first and if no get then use normal ver.
		
		private int value;	 
	    private METHOD(int value) {
	        this.value = value;
	    }
	    public int getValue() {
	        return this.value;
	    }
	}
	public METHOD defaultMethod = METHOD.BOTH;	// the method default
	//public static boolean MODEL_CUT = false;
	//static String WRITE_DST = "C:\\coin\\server_recieve_cut\\";
	static String WRITE_DST = "C:\\coin\\tempCut\\";
	
	final static int counting_size = 90000;
	
	final static int Btype = 0;
	final static int Htype = 1;
	final static int Atype = 3;
	
	final static int BoundSpace = 10;
	final static int min_picture_size = 20;
	
	private static MatOfPoint model = null;	//circle model for matchShape()
	
	//final static int method1 = 0;
	//final static int method2 = 1;
	
	private Mat originImage = new Mat();
	private double resizeRate = 1.0;
	private List<Rect> coinRect = new ArrayList<Rect>();
	/**
	 *	CoinCut()			//初始化 製作circle model
	 *	List<Mat> from(String src)
	 *	List<Mat> from(Mat src)
	 *	List<Mat> from1(Mat src)				//get coins program
	 *	List<Mat> from2(Mat src, Mat gray)		//get coins program with high dilate part for improving the coin find rate
	 *  Mat negative(Mat src)				//return the negative image of src
	 *  Mat ourEdge(Mat src, Mat dst)		//make edge by my method 
	 *  Mat findEdge(Mat src, int type)		//deal edges from src(image)
	 *  Mat coinROI(Mat edges)				// creat roi by edges
	 *  Mat coinROI2(Mat shape)				// creat roi by shape		//not use now
	 *  List<Mat> cutCoinOut(Mat src, Mat roi)	//get coins by src(image) & roi
	 *  
	 */
	
	
	public CoinCut(){
		// make a circle model for match
		if(model == null){
			Mat temp = Mat.zeros(new Size(51, 51), CvType.CV_8U);
			Imgproc.circle(temp, new Point(25, 25), 24, new Scalar(255));
			List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
			Imgproc.findContours(temp, contours, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
			model = contours.get(0);
		}
		
		//test.showResult(model, "circle");
	}
	
	public List<Rect> getRect(){
		return coinRect;
	}
	
	public Mat getOriginImage(){
		return originImage;
	}
	
	public List<Mat> from(String src){
		return from(Imgcodecs.imread(src), defaultMethod);
	}

	public List<Mat> from(String src,boolean MODEL){
		return from(Imgcodecs.imread(src), MODEL);
	}
	
	public List<Mat> from(String src,METHOD method){
		return from(Imgcodecs.imread(src), method);
	}
	
	public List<Mat> from(Mat src,boolean ModelMode){
		if(ModelMode==true){
			return from(src, METHOD.MODEL);
		}
		else{
			return from(src, METHOD.NORMAL);
		}
	}
	
	public List<Mat> from(Mat src,METHOD cut_method){
		//List<Mat> pass = from1(src);
		List<Mat> pass = new ArrayList<>();
		src.copyTo(originImage);
		switch(cut_method){
			case MODEL:
				pass = fromModelSimple(src);
				break;
			case NORMAL:
				pass = fromHQ1(src);
				break;
			case BOTH:
				pass = fromModelSimple(src);
				if(pass.isEmpty())
					pass = fromHQ1(src);
		}
		
		if(WRITE_FILE){
			String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss_SSS").format(new Date());
			int i = 0;
			for (Mat mat : pass) {
				Imgcodecs.imwrite( WRITE_DST + timeStamp + i +".jpg", mat);
				i++;
			}
		}
		System.gc();			//release memory leak
		return pass;
	}
	
	
	
//	private List<Mat> from1(Mat src){
//		Mat img = src.clone();
//		double pass= (double)Math.pow(((img.cols() * img.rows())/counting_size),(0.5));
//		//first resize to appropriate size
//		if(pass<=1){
//			pass = 1;
//		}
//		Imgproc.resize(img, img, new Size(img.cols()/pass, img.rows()/pass));
//		resizeRate = 1.0;			//normal record resize rate
//		///first resize to appropriate size
//		
//		{
//			//test.showResult(img,"origin");
//			
//			//List<Mat> imgS = new ArrayList<Mat>();
//			Mat dealImg = new Mat();
//			
//			/*Imgproc.cvtColor(img, dealImg, Imgproc.COLOR_BGR2Lab);
//			
//			Core.split(dealImg, imgS);
//			//test.showResult(imgS.get(1), "AO");
//			
//			dealImg = findEdge(imgS.get(1), Atype);
//			//test.showResult(dealImg, "A");
//			*/
//			BGR2A.run(img, dealImg);
//			Mat img2A = dealImg.clone();
//			dealImg = findEdge(dealImg, Atype);
//			
//			/*Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(7,7));
//			Imgproc.dilate(dealImg, dealImg, kernel);
//			kernel = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(5,5));
//			Imgproc.erode(dealImg, dealImg, kernel);
//			*/
//			Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(5,5));
//			Imgproc.dilate(dealImg, dealImg, kernel);
//			Imgproc.dilate(dealImg, dealImg, kernel);
//			Imgproc.dilate(dealImg, dealImg, kernel);
//			Imgproc.erode(dealImg, dealImg, kernel);
//			kernel = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(3,3));
//			Imgproc.erode(dealImg, dealImg, kernel);
//			Imgproc.erode(dealImg, dealImg, kernel);
//			//test.showResult(dealImg, "A_mod");
//			
//			dealImg = coinROI(dealImg);
//			//test.showResult(dealImg, "ROI_A");
//			
//			kernel = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(7,7));
//			Imgproc.erode(dealImg, dealImg, kernel);
//			Imgproc.erode(dealImg, dealImg, kernel);
//			kernel = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(5,5));
//			Imgproc.dilate(dealImg, dealImg, kernel);
//			Imgproc.dilate(dealImg, dealImg, kernel);
//			
//			//test.showResult(dealImg, "ROI_mod");
//			
//			/*
//			Mat roiCoin = new Mat();
//			img.copyTo(roiCoin, dealImg);
//			test.showResult(roiCoin, "ROI_coin");
//			*/
//			//Imgproc.resize(dealImg, dealImg, new Size(src.cols(), src.rows()));
//			Mat backImg = img.clone();
//			img.setTo(new Scalar(128,128,128), negative(dealImg));
//			
//			List<Mat> coins = cutCoinOut(img,dealImg);
//			if(coins.size() == 0){
//				coins = from2(backImg, img2A);
//			}
//			/*for (Mat mat : coins) {
//				test.showResult(mat);
//			}*/
//			return coins;
//		}
//	}
//	
//	private List<Mat> from2(Mat src, Mat gray){
//		Mat img = src.clone();
//		{
//			Mat dealImg = findEdge(gray, Atype);
//			Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(7,7));
//			Imgproc.dilate(dealImg, dealImg, kernel);
//			Imgproc.dilate(dealImg, dealImg, kernel);
//			Imgproc.dilate(dealImg, dealImg, kernel);
//			Imgproc.erode(dealImg, dealImg, kernel);
//			//test.showResult(dealImg, "A_mod2");
//			
//			dealImg = coinROI(dealImg);
//			//test.showResult(dealImg, "ROI_A2");
//			
//			kernel = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(9,9));
//			Imgproc.erode(dealImg, dealImg, kernel);
//			Imgproc.erode(dealImg, dealImg, kernel);
//			Imgproc.erode(dealImg, dealImg, kernel);
//			kernel = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(5,5));
//			Imgproc.dilate(dealImg, dealImg, kernel);
//			Imgproc.dilate(dealImg, dealImg, kernel);
//			
//			//test.showResult(dealImg, "ROI_mod2");
//			
//			img.setTo(new Scalar(128,128,128), negative(dealImg));
//			
//			List<Mat> coins = cutCoinOut(img,dealImg);
//			return coins;
//		}
//	}
	
	private List<Mat> fromHQ1(Mat src){// high quality output version from1()
		Mat img = src.clone();
		double pass = (double)Math.pow(((img.cols() * img.rows())/counting_size),(0.5));
		//first resize to appropriate size
		if(pass<=1){
			pass = 1;
		}
		Imgproc.resize(img, img, new Size(img.cols()/pass, img.rows()/pass));
		resizeRate = pass;		//record the resize Rate
		///first resize to appropriate size
		
		{
			//test.showResult(img,"origin");
			
			//List<Mat> imgS = new ArrayList<Mat>();
			Mat dealImg = new Mat();
			
			/*Imgproc.cvtColor(img, dealImg, Imgproc.COLOR_BGR2Lab);
			
			Core.split(dealImg, imgS);
			//test.showResult(imgS.get(1), "AO");
			
			dealImg = findEdge(imgS.get(1), Atype);
			//test.showResult(dealImg, "A");
			*/
			BGR2A.run(img, dealImg);
			//test.showResult(dealImg, "origin covert to A channel");
			Mat img2A2edge = findEdge(dealImg, Atype);
			img2A2edge.copyTo(dealImg);
			//test.showResult(dealImg, "the edges");
			/*Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(7,7));
			Imgproc.dilate(dealImg, dealImg, kernel);
			kernel = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(5,5));
			Imgproc.erode(dealImg, dealImg, kernel);
			*/
			Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(5,5));
			Imgproc.dilate(dealImg, dealImg, kernel);
			Imgproc.dilate(dealImg, dealImg, kernel);
			Imgproc.dilate(dealImg, dealImg, kernel);
			Imgproc.erode(dealImg, dealImg, kernel);
			Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(3,3)).copyTo(kernel);
			Imgproc.erode(dealImg, dealImg, kernel);
			Imgproc.erode(dealImg, dealImg, kernel);
			//test.showResult(dealImg, "A_mod");
			
			coinROI(dealImg).copyTo(dealImg);
			//test.showResult(dealImg, "ROI_A");
			
			Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(7,7)).copyTo(kernel);
			Imgproc.erode(dealImg, dealImg, kernel);
			Imgproc.erode(dealImg, dealImg, kernel);
			Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(5,5)).copyTo(kernel);
			Imgproc.dilate(dealImg, dealImg, kernel);
			Imgproc.dilate(dealImg, dealImg, kernel);
			
			//test.showResult(dealImg, "ROI_mod");
			
			/*
			Mat roiCoin = new Mat();
			img.copyTo(roiCoin, dealImg);
			test.showResult(roiCoin, "ROI_coin");
			*/
			//Imgproc.resize(dealImg, dealImg, new Size(src.cols(), src.rows()));
			Mat backImg = src.clone();
			//negative(dealImg).copyTo(dealImg);
			Imgproc.resize(dealImg, dealImg, backImg.size());
			backImg.setTo(new Scalar(128,128,128), negative(dealImg));
			
			List<Mat> coins = cutCoinOut(backImg,dealImg);
			
			if(coins.size() == 0){
				coins.addAll(fromHQ2(src, img2A2edge));
			}
			/*for (Mat mat : coins) {
				test.showResult(mat);
			}*/
			img.release();
			dealImg.release();
			backImg.release();
			img2A2edge.release();
			return coins;
		}
	}
	
	private List<Mat> fromHQ2(Mat src, Mat grayEdge){// high quality output version from2()
		Mat img = src.clone();
		{
			Mat dealImg = grayEdge.clone();
			Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(7,7));
			Imgproc.dilate(dealImg, dealImg, kernel);
			Imgproc.dilate(dealImg, dealImg, kernel);
			Imgproc.dilate(dealImg, dealImg, kernel);
			Imgproc.erode(dealImg, dealImg, kernel);
			//test.showResult(dealImg, "A_mod2");
			
			coinROI(dealImg).copyTo(dealImg);
			//test.showResult(dealImg, "ROI_A2");
			
			Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(9,9)).copyTo(kernel);
			Imgproc.erode(dealImg, dealImg, kernel);
			Imgproc.erode(dealImg, dealImg, kernel);
			Imgproc.erode(dealImg, dealImg, kernel);
			Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(5,5)).copyTo(kernel);
			Imgproc.dilate(dealImg, dealImg, kernel);
			Imgproc.dilate(dealImg, dealImg, kernel);
			
			//test.showResult(dealImg, "ROI_mod2");
			//negative(dealImg).copyTo(dealImg);;
			Imgproc.resize(dealImg, dealImg, img.size());
			img.setTo(new Scalar(128,128,128), negative(dealImg));
			
			List<Mat> coins = cutCoinOut(img,dealImg);
			img.release();
			dealImg.release();
			return coins;
		}
	}
	
//	private List<Mat> fromModel(Mat src){// model output version fromHQ1()
//		Mat img = src.clone();
//		int bigSize = (int) Math.pow(((img.cols() * img.rows())/counting_size),(0.5));
//		if((bigSize%2) == 0)
//			bigSize += 1;
//		double pass = 1.0;
//		//Imgproc.resize(img, img, new Size(img.cols()/pass, img.rows()/pass));
//		resizeRate = pass;		//record the resize Rate
//		///first resize to appropriate size
//		
//		{
//			Mat dealImg = new Mat();
//			
//			BGR2A.run(img, dealImg);
//			
//			
//			//Mat img2A2edge = findEdgeModel(dealImg, Atype);
//			Mat img2A2edge = new Mat();
//			//Imgproc.GaussianBlur(dealImg, img2A2edge, new Size(3*bigSize,3*bigSize), 0);
//			//Imgproc.medianBlur(dealImg, img2A2edge, bigSize);
//			img2A2edge = dealImg.clone();
//			Imgproc.threshold(img2A2edge, img2A2edge, 135, 255, Imgproc.THRESH_BINARY_INV);
//			//test.showResult(img2A2edge);
//			Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(3*bigSize,3*bigSize));
//			Imgproc.erode(img2A2edge, dealImg, kernel);
//			Imgproc.dilate(dealImg, dealImg, kernel);
//			//test.showResult(img2A2edge);
//			//img2A2edge.copyTo(dealImg);
//			
//			
//			Mat backImg = src.clone();
//			//Imgproc.resize(dealImg, dealImg, backImg.size());
//			//test.showResult(dealImg, "threshold");
//			dealImg = coinROIModel(dealImg, bigSize);
//			//test.showResult(dealImg, "ROI");
//			backImg.setTo(new Scalar(128,128,128), negative(dealImg));
//			
//			List<Mat> coins = cutCoinOutModel(backImg, dealImg, bigSize);
//			img.release();
//			dealImg.release();
//			backImg.release();
//			img2A2edge.release();
//			return coins;
//		}
//	}
	
	private List<Mat> fromModelSimple(Mat src){// model output version fromHQ1() & no clear edge
		Mat img = src.clone();
		
		double pass = (double)Math.pow(((img.cols() * img.rows())/counting_size),(0.5));
		//first resize to appropriate size
		if(pass<=1){
			pass = 1;
		}
		Imgproc.resize(img, img, new Size(img.cols()/pass, img.rows()/pass));
		
		int bigSize = (int) Math.pow(((img.cols() * img.rows())/counting_size),(0.5));
		if((bigSize%2) == 0)
			bigSize += 1;
		resizeRate = pass;		//record the resize Rate
		///first resize to appropriate size
		
		{
			Mat dealImg = new Mat();
			
			BGR2A.run(img, dealImg);
			
			
			//Mat img2A2edge = findEdgeModel(dealImg, Atype);
			Mat img2A2edge = new Mat();
			//Imgproc.GaussianBlur(dealImg, img2A2edge, new Size(3*bigSize,3*bigSize), 0);
			//Imgproc.medianBlur(dealImg, img2A2edge, bigSize);
			img2A2edge = dealImg.clone();
			//Imgproc.threshold(img2A2edge, img2A2edge, 135, 255, Imgproc.THRESH_BINARY_INV);
			Imgproc.GaussianBlur(dealImg, img2A2edge, new Size(3,3), 0);
			Imgproc.threshold(img2A2edge, img2A2edge, 0, 255, Imgproc.THRESH_BINARY_INV | Imgproc.THRESH_OTSU);
			
			//test.showResult(img2A2edge);
			Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_CROSS, new Size(3,3));
			Imgproc.dilate(dealImg, dealImg, kernel);
			Imgproc.erode(img2A2edge, dealImg, kernel);
			//test.showResult(img2A2edge);
			img2A2edge.copyTo(dealImg);
			
			
			Mat backImg = src.clone();
			Imgproc.resize(dealImg, dealImg, backImg.size());
			//test.showResult(dealImg, "threshold");
			dealImg = coinROIModel(dealImg, bigSize);
			//test.showResult(dealImg, "ROI");
			backImg.setTo(new Scalar(128,128,128), negative(dealImg));
			
			List<Mat> coins = cutCoinOutModel(backImg, dealImg, bigSize);
			img.release();
			dealImg.release();
			backImg.release();
			img2A2edge.release();
			return coins;
		}
	}
	
	private Mat negative(Mat src){	//return the negative image of src
		return OurImage.negative(src);
	}
	
	private Mat ourEdge(Mat src, Mat dst){
		return OurImage.ourEdge(src, dst);
	}
		
	private Mat findEdge(Mat src, int type){
		if(src.empty()){
			throw new IllegalArgumentException("findEdge: src is empty");
		}
		//test.showResult(src);
		Mat input2Gray = new Mat();
		switch(src.channels()){
		case 1:
			src.copyTo(input2Gray);
			break;
		case 3:
			Imgproc.cvtColor(src, input2Gray, Imgproc.COLOR_BGR2GRAY);   //change the picture into gray
	        break;
	    default:
	    	throw new IllegalArgumentException("findEdge: src channel is unexpected");
		}
		//Imgproc.equalizeHist(input2Gray, input2Gray);
		/*Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(7,7));
		Imgproc.medianBlur(input2Gray, input2Gray, 9);
		//Imgproc.GaussianBlur(input2Gray, input2Gray, new Size(3, 3),1);
		kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(9,9));
		Imgproc.erode(input2Gray, input2Gray, kernel);
		kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(5,5));
		Imgproc.dilate(input2Gray, input2Gray, kernel);
		*/
		
		switch(type){
		case Btype:
			ourEdge(input2Gray, input2Gray);
			//Imgproc.Canny(input2Gray, input2Gray, 2, 15);
			break;
		case Htype:
			//input2Gray.convertTo(input2Gray, CvType.CV_8S);
			{
				byte[] data = new byte[input2Gray.channels()];
				for(int i = 0 ; i < input2Gray.rows(); i++){
					for(int j = 0; j < input2Gray.cols() ; j++){
						input2Gray.get(i, j, data);
						for(int k = 0; k < input2Gray.channels(); k++){
							data[k] += 60;
						}
						input2Gray.put(i, j, data);
					}
				}
				//test.showResult(input2Gray, "H shift");
			}
			
			//ourEdge(input2Gray, input2Gray);
			//Imgproc.Canny(input2Gray, input2Gray, 3, 45);
			break;
		case Atype:
			//ourEdge(input2Gray, input2Gray);
			//Imgproc.Canny(input2Gray, input2Gray, 3, 45);
			break;
		default:
			//Imgproc.Canny(input2Gray, input2Gray, 5, 50);
		}
		//Imgproc.equalizeHist(input2Gray, input2Gray);
		
		{
				Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(7,7));
				//Imgproc.medianBlur(input2Gray, input2Gray, 5);
				Imgproc.bilateralFilter(input2Gray.clone(), input2Gray, 7, 2, 7);
				/*
				 * d the kernel size
				 * sigmaColor for near
				 * sigmaSpace for far
				 */
				//Imgproc.GaussianBlur(input2Gray, input2Gray, new Size(3, 3),1);
				
				kernel = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(9,9));
				Imgproc.erode(input2Gray, input2Gray, kernel);
				kernel = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(7,7));
				Imgproc.dilate(input2Gray, input2Gray, kernel);
				
				//test.showResult(input2Gray, "blur " + type);
		}
		ourEdge(input2Gray, input2Gray);
		/*
		kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(3,3));
		Imgproc.dilate(input2Gray, input2Gray, kernel);
		Imgproc.erode(input2Gray, input2Gray, kernel);
		*/
		return input2Gray;
	}
	
//	private Mat findEdgeModel(Mat src, int type){
//		if(src.empty()){
//			throw new IllegalArgumentException("findEdge: src is empty");
//		}
//		//test.showResult(src);
//		Mat input2Gray = new Mat();
//		switch(src.channels()){
//		case 1:
//			src.copyTo(input2Gray);
//			break;
//		case 3:
//			Imgproc.cvtColor(src, input2Gray, Imgproc.COLOR_BGR2GRAY);   //change the picture into gray
//	        break;
//	    default:
//	    	throw new IllegalArgumentException("findEdge: src channel is unexpected");
//		}
//		
//		switch(type){
//		case Btype:
//			ourEdge(input2Gray, input2Gray);
//			//Imgproc.Canny(input2Gray, input2Gray, 2, 15);
//			break;
//		case Htype:
//			//input2Gray.convertTo(input2Gray, CvType.CV_8S);
//			{
//				byte[] data = new byte[input2Gray.channels()];
//				for(int i = 0 ; i < input2Gray.rows(); i++){
//					for(int j = 0; j < input2Gray.cols() ; j++){
//						input2Gray.get(i, j, data);
//						for(int k = 0; k < input2Gray.channels(); k++){
//							data[k] += 60;
//						}
//						input2Gray.put(i, j, data);
//					}
//				}
//				//test.showResult(input2Gray, "H shift");
//			}
//			
//			//ourEdge(input2Gray, input2Gray);
//			//Imgproc.Canny(input2Gray, input2Gray, 3, 45);
//			break;
//		case Atype:
//			//ourEdge(input2Gray, input2Gray);
//			//Imgproc.Canny(input2Gray, input2Gray, 3, 45);
//			break;
//		default:
//			//Imgproc.Canny(input2Gray, input2Gray, 5, 50);
//		}
//		ourEdge(input2Gray, input2Gray);
//		return input2Gray;
//	}
	
	private Mat coinROI(Mat edges){
		List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
		Imgproc.findContours(edges.clone(), contours, new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
		//draw
		Mat roi = Mat.zeros(edges.size(), CvType.CV_8U);
		for(int i = 0; i<contours.size(); i++){
			MatOfPoint2f NewMtx_2f = new MatOfPoint2f( contours.get(i).toArray() );
        	MatOfPoint2f approx = new MatOfPoint2f();
        	double epsilon = (0.01 * Imgproc.arcLength(NewMtx_2f, true));
        	Imgproc.approxPolyDP(NewMtx_2f,approx, epsilon, true);

        	int corners = approx.rows();
        	//System.out.println("row : " + approx.rows()+" >10 ? : "+corners);
        	if(corners < 10){
        		// delete too simple shape
        		continue;
        	}

        	// 1 first rec 
        	Rect pass = Imgproc.boundingRect(contours.get(i));
        	if(pass.x <= BoundSpace || pass.x + pass.width + BoundSpace >= edges.cols()){
        		//do nothing
        		continue;
        	}else if(pass.y <= BoundSpace || pass.y+pass.height + BoundSpace >= edges.rows()){
        		//do nothing
        		continue;
        	}else if(pass.width < min_picture_size || pass.height < min_picture_size){  //for deleting the picture too small
            	continue;
            }
        	
        	//Mat pass_input_picture = new Mat(img, pass);
        	//Mat roi = Mat.zeros(img.size(), CvType.CV_8U);
        	Imgproc.drawContours(roi, contours, i, new Scalar(255), -1);
			/*
        	//draw
	        Scalar color = new Scalar( 0, 255, 0);
	        Imgproc.drawContours(img, contours, i, color);
	        */
	    }
		
		//Imgproc.erode(roi, roi, new Mat());
		//test.showResult(img);    //show the Mat (if it is picture)
		return roi;
	}
	
	/*private Mat coinROI2(Mat shape){
		if(model == null){//make circle model
			new CoinCut();
		}
		List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
		Imgproc.findContours(shape.clone(), contours, new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
		//draw
		Mat roi = Mat.zeros(shape.size(), CvType.CV_8U);
		for(int i = 0; i<contours.size(); i++){
			MatOfPoint2f NewMtx_2f = new MatOfPoint2f( contours.get(i).toArray() );
        	MatOfPoint2f approx = new MatOfPoint2f();
        	double epsilon = (0.01 * Imgproc.arcLength(NewMtx_2f, true));
        	Imgproc.approxPolyDP(NewMtx_2f,approx, epsilon, true);

        	int corners = approx.rows();
        	//System.out.println("row : " + approx.rows()+" >10 ? : "+corners);
        	if(corners < 10){
        		// delete too simple shape
        		continue;
        	}
        	// 1 first rec 
        	Rect pass = Imgproc.boundingRect(contours.get(i));
        	if(pass.x <= BoundSpace || pass.x + pass.width + BoundSpace >= shape.cols()){
        		//delete the object too near bound
        		continue;
        	}else if(pass.y <= BoundSpace || pass.y+pass.height + BoundSpace >= shape.rows()){
        		//delete the object too near bound
        		continue;
        	}else if(pass.width < min_picture_size || pass.height < min_picture_size){  //for deleting the picture too small
            	continue;
            }
        	
        	double circleRate = Imgproc.matchShapes(contours.get(i), model, Imgproc.CV_CONTOURS_MATCH_I3, 0);
        	//System.out.println("test " + circleRate);
        	if(circleRate > 0.5){ //for deleting the object not circle
        		continue;
        	}
        	
        	Imgproc.drawContours(roi, contours, i, new Scalar(255), -1);
	    }
		//Imgproc.erode(roi, roi, new Mat());
		//test.showResult(img);    //show the Mat (if it is picture)
		return roi;
	}*/
	
	private Mat coinROIModel(Mat edges, double bigSize){// model version
		List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
		Imgproc.findContours(edges.clone(), contours, new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
		//draw
		Mat roi = Mat.zeros(edges.size(), CvType.CV_8U);
		for(int i = 0; i<contours.size(); i++){

        	// 1 first rec 
        	Rect pass = Imgproc.boundingRect(contours.get(i));
        	if(pass.x <= BoundSpace || pass.x + pass.width + BoundSpace >= edges.cols()){
        		//do nothing
        		continue;
        	}else if(pass.y <= BoundSpace || pass.y+pass.height + BoundSpace >= edges.rows()){
        		//do nothing
        		continue;
        	}else if(pass.width < min_picture_size*bigSize || pass.height < min_picture_size*bigSize){  //for deleting the picture too small
            	continue;
            }
        	
        	//Mat pass_input_picture = new Mat(img, pass);
        	//Mat roi = Mat.zeros(img.size(), CvType.CV_8U);
        	Imgproc.drawContours(roi, contours, i, new Scalar(255), -1);
			/*
        	//draw
	        Scalar color = new Scalar( 0, 255, 0);
	        Imgproc.drawContours(img, contours, i, color);
	        */
	    }
		
		//Imgproc.erode(roi, roi, new Mat());
		//test.showResult(img);    //show the Mat (if it is picture)
		return roi;
	}
	
	private List<Mat> cutCoinOut(Mat src, Mat roi){
		coinRect.clear();
			
		List<Mat> coins = new ArrayList<Mat>();
		{
			List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
			Imgproc.findContours(roi.clone(), contours, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
			for(int i = 0; i<contours.size(); i++){
	        	// 1 first rec 
	        	Rect pass = Imgproc.boundingRect(contours.get(i));
	        	if(pass.width < (min_picture_size*resizeRate) || pass.height < (min_picture_size*resizeRate)){  //for deleting the picture too small
	            	continue;
	            }

	        	double circleRate = Imgproc.matchShapes(contours.get(i), model, Imgproc.CV_CONTOURS_MATCH_I3, 0);
	        	//System.out.println(circleRate);
	        	if(circleRate > 0.05){ //for deleting the object not circle
	        		continue;
	        	}
	        	coinRect.add(pass);
	        	//2 find the rotate angle
            	MatOfPoint2f passMatOfPoint2f = new MatOfPoint2f( contours.get(i).toArray() );
            	RotatedRect passRotatedRect = Imgproc.minAreaRect(passMatOfPoint2f);
            	double passRotated_angle = passRotatedRect.angle * 1;            	
            	//2 find the rotate angle end   <passRotated_angle> is angle
            	
            	Mat roiImg = new Mat(src,pass);
            	
            	//3 rotate the picture
            	Mat afterRot_input_picture = new Mat();
            	{
            		Mat rotImage = Imgproc.getRotationMatrix2D(new Point(roiImg.width()/2.0,roiImg.height()/2.0), passRotated_angle , 1.0);
	            	
	        		Imgproc.warpAffine(roiImg, afterRot_input_picture, rotImage, passRotatedRect.size, Imgproc.INTER_LINEAR, Core.BORDER_CONSTANT, new Scalar( 128, 128, 128));
	        		double pass_Matsize = Math.max(afterRot_input_picture.width(), afterRot_input_picture.height());
	        		Imgproc.resize(afterRot_input_picture, afterRot_input_picture, new Size(pass_Matsize, pass_Matsize));
            	}
            	
            	coins.add(afterRot_input_picture);
			}
		}
		return coins;
	}

	private List<Mat> cutCoinOutModel(Mat src, Mat roi,double bigRate){
		coinRect.clear();
			
		List<Mat> coins = new ArrayList<Mat>();
		{
			List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
			Imgproc.findContours(roi.clone(), contours, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
			for(int i = 0; i<contours.size(); i++){
	        	// 1 first rec 
	        	Rect pass = Imgproc.boundingRect(contours.get(i));
	        	if(pass.width < (min_picture_size*resizeRate*bigRate) || pass.height < (min_picture_size*resizeRate*bigRate)){  //for deleting the picture too small
	            	continue;
	            }

	        	double circleRate = Imgproc.matchShapes(contours.get(i), model, Imgproc.CV_CONTOURS_MATCH_I3, 0);
	        	//System.out.println(circleRate);
	        	if(circleRate > 0.05){ //for deleting the object not circle
	        		continue;
	        	}
	        	coinRect.add(pass);
	        	//2 find the rotate angle
            	MatOfPoint2f passMatOfPoint2f = new MatOfPoint2f( contours.get(i).toArray() );
            	RotatedRect passRotatedRect = Imgproc.minAreaRect(passMatOfPoint2f);
            	double passRotated_angle = passRotatedRect.angle * 1;            	
            	//2 find the rotate angle end   <passRotated_angle> is angle
            	
            	Mat roiImg = new Mat(src,pass);
            	
            	//3 rotate the picture
            	Mat afterRot_input_picture = new Mat();
            	{
            		Mat rotImage = Imgproc.getRotationMatrix2D(new Point(roiImg.width()/2.0,roiImg.height()/2.0), passRotated_angle , 1.0);
	            	
	        		Imgproc.warpAffine(roiImg, afterRot_input_picture, rotImage, passRotatedRect.size, Imgproc.INTER_LINEAR, Core.BORDER_CONSTANT, new Scalar( 128, 128, 128));
	        		double pass_Matsize = Math.max(afterRot_input_picture.width(), afterRot_input_picture.height());
	        		Imgproc.resize(afterRot_input_picture, afterRot_input_picture, new Size(pass_Matsize, pass_Matsize));
            	}
            	
            	coins.add(afterRot_input_picture);
			}
		}
		return coins;
	}
	
	private void drawRect(Mat img, List<Rect> pois) throws Exception{
		if(img.empty()){
			throw new Exception("drawRect() img empty");
		}
		for(Rect one: pois){
			Imgproc.rectangle(img, one.br(), one.tl(), new Scalar(0,255,0), OurDef.frameThickness);
		}
		if(pois.size() == 0){
			Mat LabelROI = new Mat(img, new Rect(img.width()/4, img.height()/4, OurDef.LabelRect.width, OurDef.LabelRect.height));
			Mat LabelMask = new Mat(OurDef.LabelRect.size(), CvType.CV_8UC3, new Scalar(255,255,255));
			Mat block = new Mat(OurDef.LabelRect.size(), CvType.CV_8UC3, new Scalar(255,255,255));
			Imgproc.putText(block, "NO DETECT", new Point(block.width() * 0.125 , block.height() * 0.75), Core.FONT_HERSHEY_TRIPLEX, OurDef.fontScale, new Scalar(0,0,0), OurDef.fontThick);
			block.copyTo(LabelROI, LabelMask);
		}
	}
	
	public Mat drawAnswer(){
		return drawAnswer(originImage, coinRect);
	}
	
	public Mat drawAnswer(List<Integer> labels){
		return drawAnswer(originImage, coinRect, labels);
	}
	
	public Mat drawAnswer(Mat img, List<Rect> pois){
		try {
			Mat output = img.clone();
			drawRect(output, pois);
			return output;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public Mat drawAnswer(Mat img, List<Rect> pois, List<Integer> labels){
		try {
			Mat output = drawAnswer(img, pois);
			Mat LabelROI = null;
			Mat LabelMask = new Mat(OurDef.LabelRect.size(), CvType.CV_8UC3, new Scalar(255,255,255));
//			Point LabelPoi = new Point();
			Rect targetLoca;
			Rect LabelLoca = null;
			try{
				for(int i = 0; i < labels.size(); i++){
					targetLoca = pois.get(i);
					LabelLoca = new Rect(targetLoca.x - OurDef.frameThickness, targetLoca.y + targetLoca.height, OurDef.LabelRect.width, OurDef.LabelRect.height);
					LabelROI = new Mat(output, LabelLoca);
					drawLabel(labels.get(i)).copyTo(LabelROI, LabelMask);
					//test.showResult(drawLabel(labels.get(i)));
				}
			}
			catch (Exception e){
				e.printStackTrace();
			}
			return output;
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return img;
	}
	
	private Mat drawLabel(int label){
		Mat block = new Mat(OurDef.LabelRect.size(), CvType.CV_8UC3, new Scalar(255,255,255));
		Imgproc.putText(block, OurDef.Label(label), new Point(block.width() * 0.125 , block.height() * 0.75), Core.FONT_HERSHEY_TRIPLEX, OurDef.fontScale, new Scalar(0,0,0), OurDef.fontThick);
		return block;
	}

}
