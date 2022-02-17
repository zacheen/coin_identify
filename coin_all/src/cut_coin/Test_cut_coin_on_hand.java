package cut_coin;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

public class Test_cut_coin_on_hand {
	static String dst_hand = "D:\\dont_move\\coin\\cut_hand";
	
	public static void main(String[] args) {
		String path = "D:\\dont_move\\coin\\undeal_hand";
		cut_hand(path);
	}
	
	static void cut_hand(String path) {
		File file = new File(path);
		if(!file.exists()){
			return;
		}
		if(file.isDirectory()){	//get all leaf dir and get file by recursion
			for(String fileName:file.list()){
				try {
					cut_hand(path + "\\" + fileName);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		else {	//get file
			if(path.toString().contains(".jpg") || path.toString().contains(".JPG")){
				String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss_SSS").format(new Date());
				CoinCut coinCut_non_static = new CoinCut();
				List<Mat> cut_out = coinCut_non_static.from(path,false);
				int i = 0;
				for (Mat mat : cut_out) {
					if(mat.width()<128){
						continue;
					}
					if(mat.height()<128){
						continue;
					}
					i++;
					String save_at = dst_hand+"\\"+timeStamp +"_"+ i +".jpg";
					System.out.println(save_at);
					Imgcodecs.imwrite(save_at , mat);
				}	
			}
			else{// ignore the not jpg file
				return ;
			}
		}
	}

}
