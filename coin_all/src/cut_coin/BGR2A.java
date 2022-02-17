package cut_coin;

import java.util.List;
import org.opencv.core.CvType;
import org.opencv.core.Mat;


/* this is convert BGR color to 'A' color of LAB
 * 
 * 	ver		time		by		description
 * ---------------------------------------------
 *	1.0		180911		GC		第一版 
 *	1.1		180911		GC		debug
 */
public class BGR2A {
	final static int mask = 0xFF;	//for byte to unsigned int
	private static AColorData[] aData = new AColorData[0x1000000];	//for DPway of BGR2A
	
	/**********************
	 * 
	 * BGR轉LAB的A 8U1C MAT
	 * Mat run(Mat src,Mat dst);	//輸入BGR Mat 輸出LAB的LB 8U2C MAT  (made by lib RGBtoLAB)
	 * 
	 * byte BGR2A 對單一點轉換RGBtoA 參數同ourBGR2A, index 用於指定座標點
	 * f2A	用於RGBtoA轉換公式
	 * gammaRGB 用於RGBtoA轉換公式
	 * 
	 * ********************************/
	public static Mat run(Mat src, Mat dst){
		/* 回傳 8U1C A圖
		 */
		
		int rows,cols,total,color;
		//int index;
		byte[] data;
		byte[] output;
		
		if(src.channels() !=3){
			System.out.println("err:cvtColorBGR2A.run() wrong channels:" + src.channels());
			return dst;
		}

		rows = src.rows();
		cols = src.cols();
		total = (int) src.total();
		//index = 0;
		data = new byte[total * 3 ];
		output =  new byte[total];
		src.get(0, 0, data);
		dst.create(rows, cols, CvType.CV_8UC1);
		
		for(int index = 0; index < total; index ++){
			color = 0;
			for(int i=0;i<3;i++){
				color = color << 8;
				color |= data[index*3 + i] & mask;
			}
			//System.out.println(color + " " +index);
			if(aData[color] != null){
				output[index] = aData[color].getOutput();
			}
			else{
				aData[color] = new AColorData();
				output[index] = BGR2A(data, index * 3);
				aData[color].set(output[index]);
			}
			
		}
		
		dst.put(0, 0, output);
		return dst;
	}
	
	private static byte BGR2A(byte[] input, int index){
		byte output;
		float A;
		double X,Y,Z;
		double b = (double)(input[index + 0] & mask) / 255.0,g = (double)(input[index + 1] & mask) / 255.0,r = (double)(input[index + 2] & mask) / 255.0;
		r = gammaRGB(r);
		g = gammaRGB(g);
		b = gammaRGB(b);
		
		X = ((0.412453 * r) + (0.357580 * g) + (0.180423 * b)) / 0.950456;
		Y = (0.212671 * r) + (0.715160 * g) + (0.072169 * b);
		//Z = ((0.019334 * r) + (0.119193 * g) + (0.950227 * b)) / 1.088754;
		A = (f2A(X) - f2A(Y))*500 + 128;
		output = (byte)(A);
		return output;
	}
	
	private static float f2A(double input){
		if(input > 0.008856){
			return (float) Math.pow(input, 1.0/3.0);
		}
		else{
			return (float) (7.787 * input + 16.0 / 116.0);
		}
	}
	
	private static double gammaRGB(double input){
		if(input > 0.04045){
			return Math.pow((input + 0.055) / 1.055 , 2.4);
		}
		else{
			return input / 12.92;
		}
	}
}
