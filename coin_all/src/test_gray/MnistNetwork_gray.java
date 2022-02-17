package test_gray;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import OurCNN.DATA;
import OurCNN.OurDef;
import cupcnn.Network;
import cupcnn.active.ReluActivationFunc;
import cupcnn.active.SigmodActivationFunc;
import cupcnn.active.TanhActivationFunc;
import cupcnn.data.Blob;
import cupcnn.data.BlobParams;
import cupcnn.layer.ConvolutionLayer;
import cupcnn.layer.FullConnectionLayer;
import cupcnn.layer.InputLayer;
import cupcnn.layer.PoolMaxLayer;
import cupcnn.layer.PoolMeanLayer;
import cupcnn.layer.SoftMaxLayer;
import cupcnn.loss.CrossEntropyLoss;
import cupcnn.loss.LogLikeHoodLoss;
import cupcnn.optimizer.SGDOptimizer;

public class MnistNetwork_gray {
	Network network;
	SGDOptimizer optimizer;
	FileWriter fw_loss = null;
	
//	private void buildFcNetwork(){
//		//¸ønetworkÌí¼ÓÍøÂç²ã
//		InputLayer layer1 = new InputLayer(network,new BlobParams(network.getBatch(),1, OurDef.DigRows, OurDef.DigCols));
//		network.addLayer(layer1);
//		FullConnectionLayer layer2 = new FullConnectionLayer(network,new BlobParams(network.getBatch(),784,1,1));
//		layer2.setActivationFunc(new ReluActivationFunc());
//		network.addLayer(layer2);
//		FullConnectionLayer layer3 = new FullConnectionLayer(network,new BlobParams(network.getBatch(),100,1,1));
//		layer3.setActivationFunc(new ReluActivationFunc());
//		network.addLayer(layer3);
//		FullConnectionLayer layer4 = new FullConnectionLayer(network,new BlobParams(network.getBatch(),30,1,1));
//		layer4.setActivationFunc(new SigmodActivationFunc());
//		network.addLayer(layer4);
//		FullConnectionLayer layer4_2 = new FullConnectionLayer(network,new BlobParams(network.getBatch(),30,1,1));
//		layer4_2.setActivationFunc(new SigmodActivationFunc());
//		network.addLayer(layer4_2);
//		FullConnectionLayer layer5 = new FullConnectionLayer(network,new BlobParams(network.getBatch(),10,1,1));
//		layer5.setActivationFunc(new ReluActivationFunc());
//		network.addLayer(layer5);
//		FullConnectionLayer layer6 = new FullConnectionLayer(network,new BlobParams(network.getBatch(),10,1,1));
//		layer6.setActivationFunc(new ReluActivationFunc());
//		network.addLayer(layer6);
//		SoftMaxLayer sflayer = new SoftMaxLayer(network,new BlobParams(network.getBatch(),10,1,1));
//		network.addLayer(sflayer);
//	}
	
	int input_picture_length= OurDef.CNNDigCols;
	int input_picture_height = OurDef.CNNDigRows;
	int input_picture_level = OurDef.CNNLevel;  

	private void buildConvNetwork(){
		int input_layer_level = input_picture_level;  
		int input_layer_length= input_picture_length;
		int input_layer_height = input_picture_height;
		int pooling_size = 2;
		int convolve_mask_number = 6*input_layer_level;
		int convolve_mask_number2 = convolve_mask_number*2;
		int convolve_mask_size = 5; //5*5

		int now_layer_length = input_layer_length;
		int now_layer_height = input_layer_height;
		
		
		InputLayer layer1 = new InputLayer(network,new BlobParams(network.getBatch(),input_layer_level,now_layer_length,now_layer_height));
		network.addLayer(layer1);
		
		ConvolutionLayer conv1 = new ConvolutionLayer(network,new BlobParams(network.getBatch(),convolve_mask_number,now_layer_length,now_layer_height),new BlobParams(1,convolve_mask_number,convolve_mask_size,convolve_mask_size));
		conv1.setActivationFunc(new ReluActivationFunc());
		network.addLayer(conv1);
		////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		now_layer_length = now_layer_length/pooling_size;
		now_layer_height = now_layer_height/pooling_size;
		
		PoolMaxLayer pool1 = new PoolMaxLayer(network,new BlobParams(network.getBatch(),convolve_mask_number,now_layer_length,now_layer_height),new BlobParams(1,convolve_mask_number,pooling_size,pooling_size),pooling_size,pooling_size);
		network.addLayer(pool1);
		
		convolve_mask_number = convolve_mask_number*2;
		ConvolutionLayer conv2 = new ConvolutionLayer(network,new BlobParams(network.getBatch(),convolve_mask_number,now_layer_length,now_layer_height),new BlobParams(1,convolve_mask_number,convolve_mask_size,convolve_mask_size));
		conv2.setActivationFunc(new ReluActivationFunc());
		network.addLayer(conv2);
		
		////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		now_layer_length = now_layer_length/pooling_size;
		now_layer_height = now_layer_height/pooling_size;
		
		PoolMaxLayer pool2 = new PoolMaxLayer(network,new BlobParams(network.getBatch(),convolve_mask_number,now_layer_length,now_layer_height),new BlobParams(1,convolve_mask_number,pooling_size,pooling_size),pooling_size,pooling_size);
		network.addLayer(pool2);

		
		
		FullConnectionLayer fc1 = new FullConnectionLayer(network,new BlobParams(network.getBatch(),1024,1,1));
		fc1.setActivationFunc(new ReluActivationFunc());
		network.addLayer(fc1);
		
		FullConnectionLayer fc2 = new FullConnectionLayer(network,new BlobParams(network.getBatch(),64,1,1));
		fc2.setActivationFunc(new ReluActivationFunc());
		network.addLayer(fc2);
		
		FullConnectionLayer fc3 = new FullConnectionLayer(network,new BlobParams(network.getBatch(),30,1,1));
		fc3.setActivationFunc(new ReluActivationFunc());
		network.addLayer(fc3);
		
		FullConnectionLayer fc3_2 = new FullConnectionLayer(network,new BlobParams(network.getBatch(),30,1,1));
		fc3_2.setActivationFunc(new ReluActivationFunc());
		network.addLayer(fc3_2);
		
		FullConnectionLayer fc4 = new FullConnectionLayer(network,new BlobParams(network.getBatch(),10,1,1));
		fc4.setActivationFunc(new ReluActivationFunc());
		network.addLayer(fc4);
		
		FullConnectionLayer fc5 = new FullConnectionLayer(network,new BlobParams(network.getBatch(),10,1,1));
		fc5.setActivationFunc(new ReluActivationFunc());
		network.addLayer(fc5);
		
		SoftMaxLayer sflayer = new SoftMaxLayer(network,new BlobParams(network.getBatch(),10,1,1));
		network.addLayer(sflayer);
		
	}
	public void buildNetwork(double learning_rate){
		network = new Network();
		network.setBatch(100);  //equal to Blob.numbers
		
		network.setLoss(new LogLikeHoodLoss());
		//network.setLoss(new CrossEntropyLoss());
		
//		double learning_rate = 0.00032768;
		optimizer = new SGDOptimizer(learning_rate);
		network.setOptimizer(optimizer);
		
		//buildFcNetwork();
		buildConvNetwork();

		network.prepare();
	}
	
	public List<Blob> buildBlobByImageList(List<DATA> imageList,int start,int batch,int channel,int height,int width){
		Blob input = new Blob(batch,channel,height,width);
		// build the picture
		Blob label = new Blob(batch,network.getDatas().get(network.getDatas().size()-1).get3DSize(),1,1);
		// build the lable
		label.fillValue(0);
		double[] blobData = input.getData();
		double[] labelData = label.getData();
		for(int i=start;i<(batch+start);i++){
			DATA img = imageList.get(i);
			byte[] imgData = img.data;
//			try {   check input data
//				OurCNN.Database.checkbyte(imgData);
//				Thread.sleep(5000);
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
			assert img.data.length== input.get3DSize():"buildBlobByImageList -- blob size error";
//			System.out.println(blobData.length+"  "+(((i-start)*input.get3DSize())+imgData.length));
//			System.out.println((i-start) + " "+ input.get3DSize() + " "+ imgData.length);
			for(int j=0;j<imgData.length;j++){
				blobData[(i-start)*input.get3DSize()+j] = (imgData[j]&0xff)/256.0;
			}
			int labelValue = img.label;
			for(int j=0;j<label.get3DSize();j++){
				if(j==labelValue){
					labelData[(i-start)*label.get3DSize()+j] = 1;
				}
			}
		}
		List<Blob> inputAndLabel = new ArrayList<Blob>();
		inputAndLabel.add(input);
		inputAndLabel.add(label);
		return inputAndLabel;
	}
	
	private int getMaxIndexInArray(double[] data){
		int maxIndex = 0;
		double maxValue = 0;
		for(int i=0;i<data.length;i++){
			if(maxValue<data[i]){
				maxValue = data[i];
				maxIndex = i;
			}
		}
		return maxIndex;
	}
	
	private int[] getBatchOutputLabel(double[] data){
		int[] outLabels = new int[network.getDatas().get(network.getDatas().size()-1).getNumbers()];
		int outDataSize = network.getDatas().get(network.getDatas().size()-1).get3DSize();
		for(int n=0;n<outLabels.length;n++){
			int maxIndex = 0;
			double maxValue = 0;
			for(int i=0;i<outDataSize;i++){
				if(maxValue<data[n*outDataSize+i]){
					maxValue = data[n*outDataSize+i];
					maxIndex = i;
				}	
			}
			outLabels[n] = maxIndex;
		}
		return outLabels;
	}
	
	private void testInner(Blob input,Blob label){
		Blob output = network.predict(input);
		int[] calOutLabels = getBatchOutputLabel(output.getData());
		int[] realLabels = getBatchOutputLabel(label.getData());
		assert calOutLabels.length == realLabels.length:"network train---calOutLabels.length == realLabels.length error";
		int correctCount = 0;
		for(int kk=0;kk<calOutLabels.length;kk++){
			if(calOutLabels[kk] == realLabels[kk]){
				correctCount++;
			}
		}
		double accuracy = correctCount/(1.0*realLabels.length);
		System.out.println("accuracy is "+accuracy);
		try {
			fw_loss.write("accuracy is "+accuracy +"\n");
			fw_loss.flush();
		} catch (IOException e1) {
			System.out.println("when writing fail");
			e1.printStackTrace();
			System.exit(1);
		}
	}
	
	
	public void train(List<DATA> imgList,int epoes,int now_epoch){
		int batch = network.getBatch();
		double loclaLr = optimizer.getLr();
		for(int e=0;e<epoes;e++){
			Collections.shuffle(imgList);
			double lossValue = 1;
			for(int i=0;i<imgList.size()-batch;i+=batch){
				List<Blob> inputAndLabel = buildBlobByImageList(imgList,i,batch,input_picture_level,input_picture_length,input_picture_height);
				lossValue = network.train(inputAndLabel.get(0), inputAndLabel.get(1));
				
				if(i>batch && i/batch%50==0){
					System.out.print("epoe: "+(now_epoch+e)+" lossValue: "+lossValue+"  "+" lr: "+optimizer.getLr()+"  ");
					try {
						fw_loss.write("epoe: "+(now_epoch+e)+" lossValue: "+lossValue+"  "+" lr: "+optimizer.getLr()+"  ");
						fw_loss.flush();
					} catch (IOException e1) {
						System.out.println("when writing fail");
						e1.printStackTrace();
						System.exit(1);
					}
					testInner(inputAndLabel.get(0), inputAndLabel.get(1));
				} 
				
				if(lossValue<0.015) {
					String timeStamp = new SimpleDateFormat("MMddHHmmss_").format(new Date());
					DecimalFormat df=new DecimalFormat("#.#####");
					String loss = df.format(lossValue).split("\\.")[1];
					saveModel(OurDef.good_model_dst + timeStamp + loss + ".model");
				}
			}
			
			if(loclaLr > 0.001 || (lossValue < 0.2 && loclaLr > 0.0001)){
				loclaLr*=0.8;
				optimizer.setLr(loclaLr);
			}
		}
	}
	

	static final int coin_value_table[] = {0,1,1,5,5,10,10,50,50};
	public void test(List<DATA> imgList){
		FileWriter fw = null;
		try {
			fw = new FileWriter(OurDef.write_dst_compare);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("begin test ");
		int batch = network.getBatch();
		//batch should be 1 , and its really 1 , but I dont know where change it

		int correctCount = 0;
		int real_correct_count = 0;
		int i = 0;
		for(i=0;i<=imgList.size()-batch;i+=batch){
			List<Blob> inputAndLabel = buildBlobByImageList(imgList,i,batch,input_picture_level,input_picture_length,input_picture_height);
			Blob output = network.predict(inputAndLabel.get(0));
			int[] calOutLabels = getBatchOutputLabel(output.getData());
			int[] realLabels = getBatchOutputLabel(inputAndLabel.get(1).getData());
			for(int kk=0;kk<calOutLabels.length;kk++){
				// usually calOutLabels.length is 1
				try {
					fw.write((i+1) + "  cal: " +calOutLabels[kk] + "  real: " +  realLabels[kk]);
					if(coin_value_table[calOutLabels[kk]]==coin_value_table[realLabels[kk]]) {
						real_correct_count++;
					}else {
						fw.write("  real error");
					}
					fw.write("\n");
					fw.flush();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if(calOutLabels[kk] == realLabels[kk]){
					correctCount++;
				}
				
			}
		}
		
		try {
			fw.flush();
			fw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		double accuracy = correctCount/(1.0*i+batch-1);
		System.out.println("test accuracy is "+accuracy+" correctCount "+correctCount);
		System.out.println("real test accuracy is "+real_correct_count/(1.0*i+batch-1)+" correctCount "+real_correct_count);
	}
	
	public void saveModel(String name){
		network.saveModel(name);
	}
	
	public void loadModel(String name){
		network = new Network();
		network.loadModel(name);
		network.prepare();
	}
	
	public void loadModel_again(String name,double learning_rate){
		network = new Network();
		network.setBatch(100);
		network.setLoss(new LogLikeHoodLoss());
		optimizer = new SGDOptimizer(learning_rate);
		network.setOptimizer(optimizer);
		network.loadModel(name);
		network.prepare();
	}
	
	public void prepare_write(FileWriter fw){
		this.fw_loss = fw;
	}
}
