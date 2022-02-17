package cut_coin;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

import cut_coin.CoinCut.METHOD;

public class BuildCoin_Main {
	private static String Src = "D:\\dont_move\\coin\\undeal";
	private static String Dst_cut = "D:\\dont_move\\coin\\cut";
	
	private static String Dst_ori = "D:\\dont_move\\coin\\temp_original";
	private static String Dst_dst = "D:\\dont_move\\coin\\temp";
	private static String test_ori = "D:\\dont_move\\coin\\test_original";
	private static String test_dst = "D:\\dont_move\\coin\\test";
//	
//	private static String Src = "/home/ubuntu/coin/undeal";
//	private static String Dst_cut = "/home/ubuntu/coin/coin\\cut";
//	
//	private static String Dst_ori = "/home/ubuntu/coin/original";
//	private static String Dst_dst = "/home/ubuntu/coin/train";
//	private static String test_ori = "/home/ubuntu/coin/test_original";
//	private static String test_dst = "/home/ubuntu/coin/test";
	
	private static SimpleDateFormat ft = new SimpleDateFormat ("yyyyMMddhhmmssSS");
	
	public static void main(String[] args) throws InterruptedException {
		readAllFile(Src);
//		read_orginal(Dst_ori,Dst_dst);
//		read_orginal(test_ori,test_dst);
	}

	public static void readAllFile(String path) throws InterruptedException{	//record all the data image from path
		final String val = "01"; // {"01","05","10","50"};
		final String side = "1"; // {"0"=head,"1"};
		
		System.out.println("start cut pic in : "+path);
		
		File file = new File(path);
		if(!file.exists()){
			return;
		}
		if(file.isDirectory()){	//get all leaf dir and get file by recursion
			for(String fileName:file.list()){
				readAllFile(path + "/" + fileName);
			}
		}
		else {	//get file
			if(path.toString().contains(".jpg") || path.toString().contains(".JPG")){
				String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss_SSS").format(new Date());
				
				CoinCut coinCut_non_static = new CoinCut();
				String method = null;
				int count = 0;
				for(int ii = 0;ii<1;ii++) {
					List<Mat> cut_out = null;
					if(ii==0) {
						cut_out = coinCut_non_static.from(path,CoinCut.METHOD.NORMAL);
						method = "normal";
					}else if(ii==1){
						cut_out = coinCut_non_static.from(path,CoinCut.METHOD.MODEL);
						method = "model";
					}else {
						System.out.println("error : ii = 2");
					}
					
					int i = 0;
					for (Mat mat : cut_out) {
						i++;
						String save_at = Dst_dst + "/"+ val +"/"+ side +"/"+ val + "_"+ side +"_"+timeStamp +"_"+method+"_"+ i +".jpg";
						//System.out.println(save_at);
						count++;
						Imgcodecs.imwrite(save_at , mat);
					}	
				}
				
				if(count != 0) {
					String ori_DST = Dst_ori + "/"+ val +"/"+ side +"/"+ val + "_"+ side +"_"+ timeStamp +".jpg";
					System.out.println(ori_DST);
					Imgcodecs.imwrite(ori_DST , Imgcodecs.imread(path));
				}
				
				
				try{
					File file2 = new File(path);
					if(file2.delete()){
						//System.out.println(file2.getName() + " is deleted!");
					}else{
						System.out.println("Delete operation is failed.");
					}
				}catch(Exception e){
					e.printStackTrace();
				}
			}
			else{// ignore the not jpg file
				return ;
			}
		}
		return ;
	}
	
	public static void read_orginal(String src,String dst) throws InterruptedException{	//record all the data image from path
		final String[] val = {"01","05","10","50"};
		final String[] side = {"0","1"};
		for (String string_val : val) {
			for (String string_side : side) {
				String file_place = src+"\\"+string_val+"\\"+string_side;
				
				File file = new File(file_place);
				if(!file.exists()){
					continue;
				}
				for(String fileName:file.list()){
					String file_name = file_place +"\\"+ fileName;
					System.out.println(file_name);
					if(file_name.toString().contains(".jpg") || file_name.toString().contains(".JPG")){
						CoinCut coinCut_non_static = new CoinCut();
						List<Mat> cut_out = coinCut_non_static.from(file_name,CoinCut.METHOD.NORMAL);
						int i = 0;
						for (Mat mat : cut_out) {
							i++;
							String save_at = dst + "/"+string_val+"/"+string_side +"/"+ fileName.split("\\.")[0] +"_"+ i +".jpg";
							//System.out.println(save_at);
							Imgcodecs.imwrite(save_at , mat);
						}						
					}
					else{// ignore the not jpg file
						continue ;
					}
				}
			}
		}
	}
}
