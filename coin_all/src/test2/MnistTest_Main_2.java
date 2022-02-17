package test2;


import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

import OurCNN.DATA;
import OurCNN.OurDef;
import OurCNN.ReadFile;

public class MnistTest_Main_2 {
	static List<DATA> trains = null ;
	static List<DATA> tests = null;
	
//	static List<DigitImage> trains = null ;
//	static List<DigitImage> tests = null;
	
	public static void main(String[] args) {

		//load mnist
		ReadFile rf1=new ReadFile(OurDef.trainDst);
		ReadFile rf2=new ReadFile(OurDef.testDst);
		try {
			trains = rf1.loadDATA();
			tests = rf2.loadDATA();
		} catch (IOException e) {
			System.out.println("no this model");
			e.printStackTrace();
			System.exit(1);
		}
		
		MnistNetwork_2 mn = new MnistNetwork_2();
		FileWriter fw = null;
		try {
			fw = new FileWriter(OurDef.write_dst_loss, true);
			mn.prepare_write(fw);
		} catch (IOException e) {
			System.out.println("creating fw fail");
			e.printStackTrace();
			System.exit(1);
		}

		if(OurDef.train_again == 0) {
			mn.buildNetwork(OurDef.learning_rate);
			try{
				new SimpleDateFormat();
				String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss_SSS").format(new Date());
				fw.write(timeStamp + "  new train \n");
				System.out.println(" new train ");
				fw.flush();
			}catch(Exception e){
				System.out.println("when writing fail");
				e.printStackTrace();
				System.exit(1);
			}
		}else {
			mn.loadModel_again(OurDef.module_dst,OurDef.learning_rate);
			try{
				new SimpleDateFormat();
				String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss_SSS").format(new Date());
				fw.write(timeStamp + " train again\n");
				System.out.println("train again");
				fw.flush();
			}catch(Exception e){
				System.out.println("when writing fail");
				e.printStackTrace();
				System.exit(1);
			}
		}		
		
		for(int i = 0;i<OurDef.epoch;i++){
			mn.train(trains,1,i);
			mn.saveModel(OurDef.module_dst);
		}
		
		try{
			new SimpleDateFormat();
			String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss_SSS").format(new Date());
			fw.write(timeStamp + "  train finish\n");
			System.out.println("train finish");
			fw.flush();
		}catch(Exception e){
			System.out.println("when writing fail");
			e.printStackTrace();
			System.exit(1);
		}
		
		mn.loadModel(OurDef.module_dst);
		mn.test(tests);
		try {
			fw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
