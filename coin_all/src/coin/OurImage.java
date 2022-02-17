package coin;
/* this is a structure for the DB way in RGB to LB 
 * 
 * 	ver		time		by		description
 * ---------------------------------------------
 *	1.0		????		GC		�Ĥ@�� 
 *	1.1		181009		GC		bgr2lb ����Ŷ�
 */

/******
 * this class is some method to deal the coin data.
 * there're not be necessary,but may be able to promote the identify rate when few data in learning
 * 
 * Mat ourEdge(Mat src, Mat dst)		//make edge by my method 
 * Mat negative(Mat src)				//return the negative image of src
 * Mat negative(Mat src, Mat dst)		//return the negative image of src & save in dst
 * 
 * Mat toPolar(Mat input, Mat dst) // ��JMat �^�Ƿ��y�Ф覡�x�s��dst 
 * Mat ourBGR2LB (Mat input, Mat dst);	//��JBGR Mat input ��XLAB��LB 8U2C Mat dst
 * Mat ourBGR2LB (Mat input,Mat dst,float threshold1,float threshold2); //��JBGR Mat ��XLAB��LB 8U2C MAT
 * 					// ��X�� < threshold1  ���k0 , ��X�� > threshold2 �k255
 * Mat findEdge(Mat input, Mat dst, Size blur, double threshold1, double threshold2)
 * ��J ��q�D input �H blur�ѼƨM�w���ƭ��n threshold�M�w�֭� ��X��dst
 * 
 * Mat findEdge4LB(Mat input, Mat dst, Size blur, double threshold1, double threshold2)
 *  ��J ���q�D input �u��t�ƲĤ@�q�D �H blur�ѼƨM�w���ƭ��n threshold�M�w�֭� ��X��dst
 * 
 * Mat equalize(Mat input, Mat dst,int ch)
 *  ��input������ch�q�D���Ť� ��X�^�ǤJdst
 * 
 * Mat DMT (Mat input, Mat dst, int layer)
 *  ��input(8U) �i�� layer�� �p�i�ഫ �� ��X��dst(�ର32F)
 * 
 * Mat IDMT (Mat input, Mat dst, int layer)
 *  ��input(32F) �i�� layer�� �f�p�i�ഫ �� ��X��dst(�ର8U)
 *  
 */

import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import cut_coin.LBData;

public class OurImage {
	final static int ROW = 0;
	final static int COL = 1;
	final static int mask = 0xFF;	//for byte to unsigned int
	private static LBData[] lbData = new LBData[0x1000000];	//for DPway of BGR2LB
	final static double circle = 1;	//toPolar ��X���
	final static float[] erodeMask = {	//a kernel for erode ,the argu not be adjusted carefully
			1/28,	1/28,	1/28,
			1/28,	30/28,	1/28,
			1/28,	1/28,	1/28
	};
	final static boolean EdgeBinary = true;
	final static boolean EdgeOrigin = false;
	static {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
	}

	/**********************
	 * 
	 * Mat ourEdge(Mat src, Mat dst)		//make edge by my method 
	 * Mat ourEdge(Mat src, Mat dst, boolean type)		//make edge by my method 
	 * Mat negative(Mat src)				//return the negative image of src
	 * Mat negative(Mat src, Mat dst)		//return the negative image of src & save in dst
	 * 
	 * 
	 * ********************************/
	
	public static Mat ourEdge(Mat src, Mat dst){
		return ourEdge(src, dst, EdgeBinary);	// default binary method
	}
	
	public static Mat ourEdge(Mat src, Mat dst, boolean type){
		if(src.empty()){
			throw new IllegalArgumentException("ourEdge: src is empty");
		}
		if(src.depth() != CvType.CV_8U){
			throw new IllegalArgumentException("ourEdge: src depth is not be 8U");
		}
		
		//test.showResult(src);
		Mat clone_mat = src.clone();
//		Imgcodecs.imwrite("OurImage000.jpg", clone_mat);
		/*Mat negative = new Mat(src.size(), src.type(), new Scalar(255));
		Core.subtract(negative, clone, negative);
		Imgproc.erode(negative, negative, new Mat());
		Core.add(clone, negative, dst);
		*/
		Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(3,3));
		/*Imgproc.dilate(clone, clone, kernel);
		kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(9,9));
		Imgproc.erode(clone, clone, kernel);
		Core.subtract(src, clone, dst);
		Imgproc.threshold(dst, dst, 2,255,Imgproc.THRESH_BINARY);
		*/
		Mat after_erode = new Mat();
		Imgproc.erode(clone_mat, after_erode, kernel);
		
//		Imgcodecs.imwrite("OurImage001_0a.jpg", src);
//		Imgcodecs.imwrite("OurImage001_0b.jpg", clone_mat);
//		Imgcodecs.imwrite("OurImage001_0c.jpg", after_erode);
		
		// error at here
		Core.addWeighted(src, 1.0, after_erode, -1.0, 0.0, dst);
		//Core.subtract(src, after_erode, dst);
		
//		Imgcodecs.imwrite("OurImage001_1.jpg", dst);
		if(type){	//if type == EdgeBinary,true
			Imgproc.threshold(dst, dst, 2, 255, Imgproc.THRESH_BINARY);
		}
//		Imgcodecs.imwrite("OurImage002.jpg", dst);
		/*kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(9,9));
		Imgproc.dilate(dst, dst, kernel);
		kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(5,5));
		Imgproc.erode(dst, dst, kernel);*/
		
		//test.showResult(dst, "our edge");
		return dst;
	}

	public static Mat negative(Mat src){	//return the negative image of src
		Mat dst = new Mat();
		negative(src, dst);
		return dst;
	}
	
	public static Mat negative(Mat src, Mat dst){	//return the negative image of src & save in dst
		if(src.empty()){
			throw new IllegalArgumentException("negative: src is empty");
		}
		if(src.depth() != CvType.CV_8U){
			throw new IllegalArgumentException("negative: src depth is not be 8U");
		}
		Mat negative = new Mat(src.size(), src.type(), new Scalar(255));
		Core.addWeighted(negative, 1.0, src, -1.0, 0.0, dst);
		//Core.subtract(negative, src, dst);
		return dst;
	}
	
	/**********************
	 * 
	 * Mat toPolar(Mat input,Mat dst) // ��JMat �^�Ƿ��y�Ф覡�x�s��dst 
	 * void getPix(Mat input,float row,float col,byte[] data) //�qinput���o�Ʋզs��data �]�w������k
	 * 
	 * 
	 * 
	 * 
	 * ********************************/
	
	
	public static Mat toPolar(Mat input, Mat dst){		//��J�������� Mat ��X���y��Mat
		/*��X�ϥi�A�վ�{R,L} */
		/*���w��q�D��8bit unsigned*/
		/*��float���p��q �i��double�������*/
		
		if(input.depth() >= 2){/*�`�׫D�w�� �i��ݭn�A�B�z?*/
			System.out.println("err:toPolar() not 8bit depth: " + input.depth());
			return dst;
		}
		
		if(input.rows() != input.cols()){
			System.out.println("err:toPolar() not square: " + input.rows() + "," +input.cols());
			return dst;
		}
		
		Mat output = new Mat();
		float x,y;	//(x,y)��߮y��
		float R,L;	//R����b�|(���y���a�b) L��������P��(���y�о�b����)
		float sita;//sita ���y�о��쩷��
		float x_t,y_t;//�Ȯɥήy��
		
		/*�i��ݭn�ק�*/
		byte[] data= new byte[input.channels()];//��@�������
		/*�i��ݭn�ק�*/
		
		x = (y = input.cols()/2);
		R = input.cols()/2;
		L = (float) (R * Math.PI * circle);	//2*PI*(R+0)/2 ������P�� �B�~�h�b��*1.5
		sita = 2 / R;	// 2*PI/L == 2/R �i�u��
		
		output.create((int)Math.round(R), (int)Math.ceil(L), input.type());
		
		for(int i = 0; i < R ; i++){
			for(int j = 0; j < Math.ceil(L); j++){
				x_t = (float) (x + i * Math.cos(sita * j));
				y_t = (float) (y + i * Math.sin(sita * j));
				//System.out.println("(" + x_t +"," + y_t + ")\t,(" + i +",sita " + j + ")");
				getPix(input,y_t, x_t, data);		//���I��k
				output.put(i, j, data);
			}
		}
		
		output.copyTo(dst);
		return dst;
	}
	
	public static void getPix(Mat input,float row,float col,byte[] data){//���I��k
		/*�ȥξF����Ȫk*/
		
		input.get((int)Math.round(row), (int)Math.round(col), data);
		//System.out.println("{"+(int)Math.round(row) + "," + (int)Math.round(col) +"}");
		return;
	}
	

	
	/**********************
	 * 
	 * BGR��LAB��LB 8U2C MAT
	 * Mat BGR2LB (Mat input,Mat dst);	//��JBGR Mat ��XLAB��LB 8U2C MAT  (made by lib RGBtoLAB)
	 * Mat ourBGR2LB (Mat input,Mat dst);	//��JBGR Mat ��XLAB��LB 8U2C MAT
	 * Mat ourBGR2LB (Mat input,Mat dst,float threshold1,float threshold2); //��JBGR Mat ��XLAB��LB 8U2C MAT
	 * 					// ��X�� < threshold1  ���k0 , ��X�� > threshold2 �k255
	 * 
	 * byte[] BGR2LB ���@�I�ഫRGBtoLB �ѼƦPourBGR2LB, index �Ω���w�y���I
	 * f2LB	�Ω�RGBtoLB�ഫ����
	 * gammaRGB �Ω�RGBtoLB�ഫ����
	 * 
	 * ********************************/
	public static Mat BGR2LB(Mat input,Mat dst){
		if(input.channels() !=3){
			System.out.println("err:BGR2LB() wrong channels: " + input.channels());
			return dst;
		}
		Mat output = new Mat();
		List<Mat> outputC = new ArrayList<Mat>(3);
		
		Imgproc.cvtColor(input, dst, Imgproc.COLOR_BGR2Lab);
		Core.split(dst, outputC);
		outputC.remove(1);
		Core.merge(outputC, dst);
		//System.out.println(output.depth() + "," + output.channels());
		//output.copyTo(dst);
		return dst;
	}
	
	public static Mat ourBGR2LB(Mat input,Mat dst){
		ourBGR2LB(input,dst,0,255);
		return dst;
	}
	
	public static Mat ourBGR2LB(Mat input,Mat dst,float threshold1,float threshold2){
		/* �^�� 8U2C LB��
		 * 
		 * 
		 * 
		 */
		
		int rows,cols,total,color;
		//int index;
		byte[] data;
		byte[] output;
		byte[] get = new byte[2];
		//List<Mat> inputC;		//�N��J����
		List<Mat> outputC;	//output channel
		
		
		if(input.channels() !=3){
			System.out.println("err:ourBGR2LB() wrong channels:" + input.channels());
			return dst;
		}
		
		/*inputC = new ArrayList<Mat>(3) ;	//inputC ���X3��channel
		Core.split(input,inputC);
		*/
		//outputC = new ArrayList<Mat>(2) ;
		/*�^�ǭ� �]�w*/
		
		rows = input.rows();
		cols = input.cols();
		total = (int) input.total();
		//index = 0;
		data = new byte[total * 3 ];
		output =  new byte[total * 2 ];
		input.get(0, 0, data);
		dst.create(rows, cols, CvType.CV_8UC2);
		
		for(int index = 0; index < total; index ++){
			color = 0;
			for(int i=0;i<3;i++){
				color = color << 8;
				color |= data[index*3 + i] & mask;
			}
			//System.out.println(color + " " +index);
			if(lbData[color] != null){
				lbData[color].getOutput(output,index * 2);
			}
			else{
				lbData[color] = new LBData();
				System.arraycopy(BGR2LB(data,index * 3,threshold1,threshold2), 0, output, index * 2, 2);
				lbData[color].set(output, index * 2);
			}
			
		}
		
		dst.put(0, 0, output);
		/*for(int j = 0;j < rows;j++){
			for(int i = 0;i < cols;i++){
				dst.put(j, i, BGR2LB(data,index,threshold1,threshold2));
				index += 3;
			}
		}*/
//		for(int index = 0; index < 0x1000000; index ++){
//			lbData[index] = null;
//		}
		return dst;
	}
	
	public static byte[] BGR2LB(byte[] input, int index,float threshold1,float threshold2){
		byte[] output = new byte[2];
		float L,B;
		double X,Y,Z;
		double b = (double)(input[index + 0] & mask) / 255.0,g = (double)(input[index + 1] & mask) / 255.0,r = (double)(input[index + 2] & mask) / 255.0;
		r = gammaRGB(r);
		g = gammaRGB(g);
		b = gammaRGB(b);
		
		Y = (0.212671 * r) + (0.715160 * g) + (0.072169 * b);
		Z = ((0.019334 * r) + (0.119193 * g) + (0.950227 * b)) / 1.088754;
		if ( Y > 0.008856){
			L = (float) (Math.pow(Y, 1.0 / 3.0) * 116 - 16);
		}
		else{
			L = (float) (Y * 903.3);
		}
		B = ((f2LB(Y) - f2LB(Z)) * 200) + 128;
		
		if(B < threshold1){
			B = 0;
		}
		else if (B > threshold2){
			B = 255;
		}
		
		output[0] = (byte)(L * 255 / 100);
		output[1] = (byte)(B);
		return output;
	}
	
	public static byte[] BGR2LB(byte[] input,float threshold1,float threshold2){
		byte[] output = new byte[2];
		if(input.length !=3){
			System.out.println("err:BGR2LB() pix not 3 ch: " + input.length);
			return output;
		}
		float L,B;
		double X,Y,Z;
		double b = (double)(input[0] & 0xFF) / 255.0,g = (double)(input[1] & 0xFF) / 255.0,r = (double)(input[2] & 0xFF) / 255.0;
		r = gammaRGB(r);
		g = gammaRGB(g);
		b = gammaRGB(b);
		
		Y = (0.212671 * r) + (0.715160 * g) + (0.072169 * b);
		Z = ((0.019334 * r) + (0.119193 * g) + (0.950227 * b)) / 1.088754;
		if ( Y > 0.008856){
			L = (float) (Math.pow(Y, 1.0 / 3.0) * 116 - 16);
		}
		else{
			L = (float) (Y * 903.3);
		}
		B = ((f2LB(Y) - f2LB(Z)) * 200) + 128;
		
		if(B < threshold1){
			B = 0;
		}
		else if (B > threshold2){
			B = 255;
		}
		
		output[0] = (byte)(L * 255 / 100);
		output[1] = (byte)(B);
		//System.out.println(r+"," + g +"," + b + " " + Y +"," + Z + " " + (output[0] & 0xFF) +"," + (output[1] & 0xFF));
		return output;
	}
	
	public static float f2LB(double input){
		if(input > 0.008856){
			return (float) Math.pow(input, 1.0/3.0);
		}
		else{
			return (float) (7.787 * input + 16.0 / 116.0);
		}
	}
	
	public static double gammaRGB(double input){
		if(input > 0.04045){
			return Math.pow((input + 0.055) / 1.055 , 2.4);
		}
		else{
			return input / 12.92;
		}
	}
	
	/******
	 * 
	 *  ��t�ƳB�z
	 * Mat findEdge(Mat input, Mat dst, Size blur, double threshold1, double threshold2)
	 * ��J ��q�D input �H blur�ѼƨM�w���ƭ��n threshold�M�w�֭� ��X��dst
	 * 
	 * Mat findEdge4LB(Mat input, Mat dst, Size blur, double threshold1, double threshold2)
	 *  ��J ���q�D input �u��t�ƲĤ@�q�D �H blur�ѼƨM�w���ƭ��n threshold�M�w�֭� ��X��dst
	 * 
	 * Mat equalize(Mat input, Mat dst,int ch)
	 *  ��input������ch�q�D���Ť� ��X�^�ǤJdst
	 * 
	 */
	public static Mat findEdge(Mat input, Mat dst, Size blur, double threshold1, double threshold2){
		if(input.channels() != 1){
			System.out.println("err:findEdge() input channel not 1: " + input.channels());
			return dst;
		}
		Mat output = new Mat();
		Imgproc.GaussianBlur(input, dst, blur, 0, 0);	//�ҽk�� �Ѽ�size new Size(3,3);
		Imgproc.Canny(dst, dst, threshold1, threshold2);	//���ѼƬ��֭�
		//output.copyTo(dst);
		return dst;
	}
	
	public static Mat findEdge4LB(Mat input, Mat dst, Size blur, double threshold1, double threshold2){
		if(input.channels() != 2){
			System.out.println("err:findEdge4LB() input channel not 2: " + input.channels());
			return dst;
		}
		Mat output = new Mat();
		List<Mat> inputC = new ArrayList<Mat>(2); 
		Core.split(input, inputC);
		Imgproc.GaussianBlur(inputC.get(0), inputC.get(0),blur, 0, 0);	//�ҽk�� �Ѽ�blur ex: new Size(3,3);
		Imgproc.Canny(inputC.get(0), inputC.get(0), threshold1, threshold2);	//���ѼƬ��֭�
		Core.merge(inputC, dst);
		
		//output.copyTo(dst);
		
		return dst;
	}
	
	public static Mat equalize(Mat input, Mat dst,int ch){
		if(input.channels() <= ch){
			System.out.println("err:equalize() channels: " + input.channels() + " input " + ch);
			return dst;
		}
		List<Mat> inputC = new ArrayList<Mat>(2); 
		Core.split(input, inputC);
		Imgproc.equalizeHist(inputC.get(ch), inputC.get(ch));
		Core.merge(inputC, dst);
		return dst;
	}
	
	/******
	 * 
	 * Mat DMT (Mat input, Mat dst, int layer) //�p�i����
	 *  ��input(8U) �i�� layer�� �p�i�ഫ �� ��X��dst(�ର32F)
	 * 
	 * Mat IDMT (Mat input, Mat dst, int layer) //�p�i�f����
	 *  ��input(32F) �i�� layer�� �f�p�i�ഫ �� ��X��dst(�ର8U)
	 * 
	 * void wavelet (Mat lowFilter,Mat highFilter) �ͦ��p�i��kernal unuse in our wave trans
	 * 
	 * Mat waveDecompose (Mat input,int type,Mat lowFilter,Mat highFilter)	//����
	 * use kernal some bugs!!!!
	 * Mat waveReconstruct(Mat input,int type,Mat lowFilter,Mat highFilter)	//�f����
	 * use kernal some bugs!!!!
	 * 
	 * Mat ourWaveDecompose (Mat input,int type)	//�p�i����
	 *  //type is the OurImage.ROW or OurImage.COL
	 * Mat ourWaveReconstruct(Mat input,int type)	//�f����
	 *  //type is the OurImage.ROW or OurImage.COL
	 */
	public static Mat DMT(Mat input, Mat dst, int layer){//�p�i�ഫ
		if(layer < 1){
			System.out.println("DMT: layer < 1 : " + layer);
			return dst;
		}
		Mat temp = new Mat();
		Mat temp2 = new Mat();
		Mat output = new Mat();
		float[] data = new float[input.channels()]; 
		//Mat lowFilter = new Mat();
		//Mat highFilter = new Mat();
		int tRows = input.rows();
		int tCols = input.cols();
		int rows , cols;
		
		/*for make size can be DMT*/
		int powNum = (int)Math.pow( 2, layer);
		if((tRows%powNum) > 0){
			tRows += (powNum - (tRows%powNum));
		}
		if((tCols%powNum) > 0){
			tCols += (powNum - (tCols%powNum));
		}
		//System.out.print( tRows + "," + tCols);
		temp2.create(new Size(tCols, tRows), 0);
		/**/
		
		Imgproc.resize(input, temp2, temp2.size());
		temp2.convertTo(dst, CvType.CV_32F);
		
		//wavelet(lowFilter, highFilter);
		
		rows = tRows ; 
		cols = tCols ;
		for(int depth = 1; depth <= layer ; depth++, rows /= 2, cols /= 2){
			output = dst.submat(0, rows, 0, cols);
			for(int i = 0; i < rows ; i++){
				//temp = waveDecompose(output.row(i), ROW, lowFilter, highFilter);
				temp = ourWaveDecompose(output.row(i), ROW);
				for(int j = 0; j < cols; j++){
					temp.get( 0, j, data);
					dst.put(i, j, data);
				}
			}
			
			for(int i = 0; i < cols ; i++){
				//temp = waveDecompose(output.col(i), COL, lowFilter, highFilter);
				temp = ourWaveDecompose(output.col(i), COL);
				for(int j = 0; j < rows; j++){
					temp.get( 0, j, data);
					dst.put(j, i, data);
					
				}
			}
		}
		
		//output.copyTo(dst);
		return dst;
	}
	
	public static void wavelet(Mat lowFilter,Mat highFilter){//�p�i����
		//float temp = (float) (1.0 / Math.sqrt(2.0));
		float temp = (float) 0.5;
		float[] data = new float[2]; 
		Mat.zeros( 1, 2, CvType.CV_32FC1).copyTo(lowFilter);
		Mat.zeros( 1, 2, CvType.CV_32FC1).copyTo(highFilter);
		data[0] = temp;
		data[1] = temp;
		lowFilter.put(0, 0, data);
		data[0] *= -1;
		highFilter.put(0, 0, data);
	}
	
	public static Mat waveDecompose(Mat input,int type,Mat lowFilter,Mat highFilter){//�p�i����
		Mat temp = new Mat();
		Mat dst;
		Mat dst1 = new Mat(),dst2 = new Mat();
		Mat down1 ,down2;
		int cols;
		float[] data = new float[input.channels()];
		
		switch(type){
		case ROW:
			if(input.rows() != 1){
				System.out.println("waveDecompose <ROW>: row error :" + input.rows());
				return input;
			}
			if(input.cols() < lowFilter.cols() || input.cols() < highFilter.cols()){
				System.out.println("waveDecompose <ROW>: col error :" + input.cols());
				return input;
			}
			input.copyTo(temp);
			break;
			
		case COL:
			if(input.cols() != 1){
				System.out.println("waveDecompose <COL>: col error :" + input.cols());
				return input;
			}
			if(input.rows() < lowFilter.cols() || input.rows() < highFilter.cols()){
				System.out.println("waveDecompose <COL>: row error :" + input.rows());
				return input;
			}
			temp = input.t();		//�ন��x�}
			break;
			
		default:
			System.out.println("waveDecompose: way error :" + type);
			return input;
		}
		cols = temp.cols();
		//System.out.println(lowFilter.cols() + " " + lowFilter.rows());
		
		Imgproc.filter2D(temp, dst1, -1, lowFilter );
		Imgproc.filter2D(temp, dst2, -1, highFilter);
		
		down1 = Mat.zeros(1, cols/2, temp.type());
		down2 = Mat.zeros(1, cols/2, temp.type());
		Imgproc.resize(dst1, down1, down1.size());
		Imgproc.resize(dst2, down2, down2.size());
		dst = new Mat(1, cols, input.type());
		for(int i = 0;i < cols/2 ; i++){
			down1.get(0, i, data);
			dst.put(0, i, data);
			
			down2.get(0, i, data);
			dst.put(0, i + cols/2, data);
		}
		return dst;
	}
	
	public static Mat ourWaveDecompose(Mat input,int type){//�p�i����
		Mat output ;
		int cols = (int) input.total();
		int ch = input.channels();
		float[] data = new float[cols*ch];
		float[] temp = new float[cols*ch];
		
		
		switch(type){
		case ROW:
			if(input.rows() != 1){
				System.out.println("waveDecompose <ROW>: row error :" + input.rows());
				return input;
			}/*
			if(input.cols() < lowFilter.cols() || input.cols() < highFilter.cols()){
				System.out.println("waveDecompose <ROW>: col error :" + input.cols());
				return input;
			}*/
			//input.copyTo(temp);
			break;
			
		case COL:
			if(input.cols() != 1){
				System.out.println("waveDecompose <COL>: col error :" + input.cols());
				return input;
			}/*
			if(input.rows() < lowFilter.cols() || input.rows() < highFilter.cols()){
				System.out.println("waveDecompose <COL>: row error :" + input.rows());
				return input;
			}*/
			//temp = input.t();		//�ন��x�}
			break;
			
		default:
			System.out.println("waveDecompose: way error :" + type);
			return input;
		}
		
		//System.out.println(cols + " " + ch);
		input.get(0, 0, data);
		for(int i = 0 ; (i+1) < cols ; i += 2){
			for(int j = 0 ; j < ch ; j++){
				temp[(i / 2) * ch + j] = (data[i * ch + j]/2 + data[(i+1) * ch + j]/2 );
				temp[(i + cols ) / 2 * ch + j] = ((data[(i+1) * ch + j]/2 - data[i * ch + j]/2)) + 128;
			}
		}/*
		if(cols%2){
			for(int j = 0 ; j < ch ; j++){
				temp[(cols / 2) * ch + j] = data[cols * ch + j];
				temp[(cols-1) * ch + j] =  128;
			}
		}*/
		output = new Mat(1, cols, input.type());
		output.put(0, 0, temp);
		return output;
	}
	
	public static Mat IDMT(Mat input, Mat dst, int layer){//�p�i�f�ഫ
		if(layer < 1){
			System.out.println("IDMT: layer < 1 : " + layer);
			return dst;
		}
		//Mat lowFilter = new Mat();
		//Mat highFilter = new Mat();
		int tRows = input.rows();
		int tCols = input.cols();
		int rows , cols;
		float[] data = new float[input.channels()];
		Mat temp;
		Mat output = input.clone();
		Mat outputT;
		
		//wavelet(lowFilter, highFilter);
		
		
		rows = (int) (tRows / Math.pow(2, layer-1));
		cols = (int) (tCols / Math.pow(2, layer-1));
		for(int depth = 1; depth <= layer; depth ++, rows *= 2, cols *= 2){

			System.out.println(cols/2 +"->" + cols);
			outputT = output.submat(0, rows, 0, cols);
			for(int j = 0; j < cols; j++){
				//temp = waveReconstruct(outputT.col(j), COL, lowFilter, highFilter);
				temp = ourWaveReconstruct(outputT.col(j), COL);
				for(int i = 0; i < rows; i++){
					temp.get( 0, i, data);
					//System.out.println("COL: " + data[0]);
					output.put(i, j, data);
				}
			}
			/**/
			//test.seeImg(output);
			
			for(int i = 0; i < rows; i++){
				//temp = waveReconstruct(outputT.row(i), ROW, lowFilter, highFilter);
				temp = ourWaveReconstruct(outputT.row(i), ROW);
				for(int j = 0; j < cols; j++){
					temp.get( 0, j, data);
					//System.out.println("ROW: " + data[0]);
					output.put(i, j, data);
				}
			}
		}
		
		output.convertTo(dst, CvType.CV_8U);
		return dst;
	}
	
	public static Mat waveReconstruct(Mat input,int type,Mat lowFilter,Mat highFilter){//�p�i����
		Mat temp = new Mat();
		Mat dst;
		Mat dst1 ,dst2 ;
		Mat up1 , up2;
		Mat roi1 , roi2;
		int cols;
		float[] data1 = new float[input.channels()];
		float[] data2 = new float[input.channels()];
		switch(type){
		case ROW:
			if(input.rows() != 1){
				System.out.println("waveReconstruct <ROW>: row error :" + input.rows());
				return input;
			}
			if(input.cols() < lowFilter.cols() || input.cols() < highFilter.cols()){
				System.out.println("waveReconstruct <ROW>: col error :" + input.cols());
				return input;
			}
			input.copyTo(temp);
			break;
			
		case COL:
			if(input.cols() != 1){
				System.out.println("waveReconstruct <COL>: col error :" + input.cols());
				return input;
			}
			if(input.rows() < lowFilter.cols() || input.rows() < highFilter.cols()){
				System.out.println("waveReconstruct <COL>: row error :" + input.rows());
				return input;
			}
			temp = input.t();		//�ন��x�}
			break;
			
		default:
			System.out.println("waveDecompose: way error :" + type);
			return input;
		}
		cols = temp.cols();
		//System.out.println(lowFilter.cols() + " " + lowFilter.rows());
		
		up1 = Mat.zeros(1, cols, temp.type());
		up2 = Mat.zeros(1, cols, temp.type());
		
		roi1 = new Mat(temp ,new Rect(0, 0, cols/2, 1));
		roi2 = new Mat(temp ,new Rect(cols/2, 0, cols/2, 1));
		Imgproc.resize(roi1, up1, up1.size(), 0, 0, Imgproc.INTER_CUBIC);
		Imgproc.resize(roi2, up2, up2.size(), 0, 0, Imgproc.INTER_CUBIC);
		dst1 = Mat.zeros(1, cols, temp.type());
		dst2 = Mat.zeros(1, cols, temp.type());
		Imgproc.filter2D(up1, dst1, -1, lowFilter );
		Imgproc.filter2D(up2, dst2, -1, highFilter);
		dst = Mat.zeros(1, cols, temp.type());
		
		for(int i = 0 ; i < cols ; i++){
			dst1.get(0, i, data1);
			dst2.get(0, i, data2);
			for(int j = 0 ; j < input.channels(); j++){
				data1[j] += data2[j];
			}
			dst.put(0, i, data1);
		}
		
		return dst;
	}
	
	public static Mat ourWaveReconstruct(Mat input,int type){//�p�i����
		Mat temp = new Mat();
		Mat dst ;
		//Mat up1 , up2;
		//Mat roi1 , roi2;
		int cols;
		float[] data1 = new float[input.channels()];
		float[] data2 = new float[input.channels()];
		float[] dataT = new float[input.channels()];
		switch(type){
		case ROW:
			if(input.rows() != 1){
				System.out.println("waveDecompose <ROW>: row error :" + input.rows());
				return input;
			}/*
			if(input.cols() < lowFilter.cols() || input.cols() < highFilter.cols()){
				System.out.println("waveDecompose <ROW>: col error :" + input.cols());
				return input;
			}*/
			input.copyTo(temp);
			break;
			
		case COL:
			if(input.cols() != 1){
				System.out.println("waveDecompose <COL>: col error :" + input.cols());
				return input;
			}/*
			if(input.rows() < lowFilter.cols() || input.rows() < highFilter.cols()){
				System.out.println("waveDecompose <COL>: row error :" + input.rows());
				return input;
			}*/
			temp = input.t();		//�ন��x�}
			break;
			
		default:
			System.out.println("waveDecompose: way error :" + type);
			return input;
		}
		cols = temp.cols();
		//System.out.println(lowFilter.cols() + " " + lowFilter.rows());
		dst = Mat.zeros(1, cols, temp.type());
		//roi1 = new Mat(temp ,new Rect(0, 0, cols/2, 1));
		//roi2 = new Mat(temp ,new Rect(cols/2, 0, cols/2, 1));
		
		//test.seeImg(temp);
		for(int i = 0 ; i < cols/2 ; i++){
			temp.get(0, i,data1);
			temp.get(0, i + cols/2,data2);
			//System.out.println(i + " :" + data1[0] + " , " + data2[0]);
			
			/*roi1.get(0, i,data1);
			roi2.get(0, i,data2);*/
			for(int j = 0 ; j < input.channels(); j++){
				dataT[j] = (float) (data1[j] - (data2[j] - 128));
			}
			//System.out.println( type +" " + i + " :" + data1[0] + " - " + data2[0] + " = " + dataT[0]);
			dst.put(0, i*2, dataT);		
			
			for(int j = 0 ; j < input.channels(); j++){
				dataT[j] = (float) (data1[j] + (data2[j] - 128));
			}
			//System.out.println( type +" " + i + " :" + data1[0] + " + " + data2[0] + " = " + dataT[0]);
			dst.put(0, (i*2 + 1), dataT);
		}
		
		return dst;
	}

	/*****
	 * Mat ourErode(Mat src, Mat dst, int channel)	//�I�k ������T
	 *	//channel is which channel want to be erode
	 * 	//if channel < 0 then do all channel
	 * 
	 * 
	 */
	public static Mat ourErode(Mat src, Mat dst, int channel){
		Mat erode;
		List<Mat> erodeT = new ArrayList<Mat>();
		List<Mat> inputC = new ArrayList<Mat>();
		Mat input = new Mat();
		Mat temp = new Mat();
		
		if(channel >= input.channels()){
			System.out.println("err:ourErode() input channel too more ,please less than " + input.channels());
			return dst;
		}
		else if (channel < 0){	// erode all chanel
			src.copyTo(input);
			erode = new Mat(new Size(3, 3), input.type());
			Core.split(erode, erodeT);
			for(int i = 0; i< erodeT.size(); i++){
				erodeT.get(i).put(0, 0, erodeMask);
			}
			
			Imgproc.filter2D(input, temp, -1, erode );
			temp.copyTo(dst);

			return dst;
		}
		else{					// erode specific chanel
			src.copyTo(input);
			Core.split(input, inputC);
			erode = new Mat(new Size(3, 3), CvType.CV_32F);
			erode.put(0, 0, erodeMask);
			
			Imgproc.filter2D(inputC.get(channel), temp, -1, erode );
			inputC.set(channel, temp);
			Core.merge(inputC, dst);
			return dst;
		}
		
	}
	
}
