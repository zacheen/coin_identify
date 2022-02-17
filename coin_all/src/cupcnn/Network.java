package cupcnn;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import cupcnn.data.Blob;
import cupcnn.data.BlobParams;
import cupcnn.layer.ConvolutionLayer;
import cupcnn.layer.FullConnectionLayer;
import cupcnn.layer.InputLayer;
import cupcnn.layer.Layer;
import cupcnn.layer.PoolMaxLayer;
import cupcnn.layer.PoolMeanLayer;
import cupcnn.layer.SoftMaxLayer;
import cupcnn.loss.LogLikeHoodLoss;
import cupcnn.loss.Loss;
import cupcnn.optimizer.Optimizer;
import cupcnn.optimizer.SGDOptimizer;



public class Network{
	public static String MODEL_BEGIN = "BEGIN";
	public static String MODEL_END = "END";
	private List<Blob> datas;
	private List<Blob> diffs;
	private List<Layer> layers;
	private Loss loss;
	private Optimizer optimizer;
	private int batch = 1;
	
	public Network(){
		datas = new ArrayList<Blob>();
		diffs = new ArrayList<Blob>();
		layers = new ArrayList<Layer>();
	}
	/*
	 *��Ӵ����Ĳ�
	 */
	public void addLayer(Layer layer){
		layers.add(layer);
	}
	
	/*
	 * ��ȡdatas
	 */
	public List<Blob> getDatas(){
		return datas;
	}
	/*
	 * ��ȡdiffs
	 */
	public List<Blob> getDiffs(){
		return diffs;
	}
	/*
	 * ��ȡLayers
	 */
	public List<Layer> getLayers(){
		return layers;
	}

	
	public void setLoss(Loss loss){
		this.loss = loss;
//		System.out.println("set loss : " + this.loss);
	}

	
	public void setBatch(int batch){
		this.batch = batch;
	}
	
	public int getBatch(){
		return this.batch;
	}
	
	public void setOptimizer(Optimizer optimizer){
		this.optimizer = optimizer;
	}
	
	public void prepare(){
		for(int i=0;i<layers.size();i++){
			BlobParams layerParams = layers.get(i).getLayerParames();
			assert (layerParams.getNumbers()>0 && layerParams.getChannels()>0 && layerParams.getHeight()>0 && layerParams.getWidth() >0):"prapare---layer params error";
			Blob data = new Blob(batch,layerParams.getChannels(),layerParams.getHeight(),layerParams.getWidth());
			datas.add(data);
			Blob diff = new Blob(data.getNumbers(),data.getChannels(),data.getHeight(),data.getWidth());
			diffs.add(diff);
			layers.get(i).setId(i);
			layers.get(i).prepare();
		}
	}
	
	
	public void forward(){
		for(int i=0;i<layers.size();i++){
			layers.get(i).forward();
		}
	}
	

	
	public void backward(){
		for(int i=layers.size()-1;i>-1;i--){
			layers.get(i).backward();
			//ʹ���Ż������²���
			optimizer.update(layers.get(i).getParamsList(), layers.get(i).getGradientList());
		}
	}
	
	public double train(Blob inputData,Blob labelData){
		double lossValue = 0.0;
		Layer first = layers.get(0);
		assert first instanceof InputLayer:"input layer error";
		((InputLayer)first).setInputData(inputData);
	
		//ǰ�򴫲�
		forward();
		lossValue = this.loss.loss(labelData, datas.get(datas.size()-1));
		//���������diff
		loss.diff(labelData, datas.get(datas.size()-1), diffs.get(diffs.size()-1));
		//���촫��
		backward();
		
		return lossValue;
	}
	
	public Blob predict(Blob inputData){
		Layer first = layers.get(0);
		assert first instanceof InputLayer:"input layer error";
		((InputLayer)first).setInputData(inputData);
	
		//ǰ�򴫲�
		forward();
		//�������һ�������
		return datas.get(datas.size()-1);
	}
	
	public void saveModel(String name){
//		System.out.println("begin save model");
		ObjectOutputStream out = null;
	    try {
			out = new ObjectOutputStream(new FileOutputStream(name));
			out.writeUTF(MODEL_BEGIN);
			out.writeInt(layers.size());
//			System.out.println("write layers.size : " + layers.size());
			for(int i=0;i<layers.size();i++){
				layers.get(i).saveModel(out);
			}
			out.writeUTF(MODEL_END);
			out.flush();
			out.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    try {
			out.flush();
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    System.out.println("save model finished");
	}
	
	public void loadModel(String name){
//		System.out.println("begin load model");
		ObjectInputStream in = null;
		try {
			in = new ObjectInputStream(new FileInputStream(name));
			String begin = in.readUTF();
			if(!begin.equals(MODEL_BEGIN)){
				System.out.println("file format error");
				in.close();
				return;
			}
			int layersSize = in.readInt();
			if(layersSize<=0){
				System.out.println("no layers");
				in.close();
				return;			
			}else{
//				System.out.println(layersSize);
			}
			String layerType = null;
			for(int i=0;i<layersSize;i++){
				layerType = in.readUTF();
				if(layerType.equals(InputLayer.TYPE)){
					try {
						BlobParams layerParams = (BlobParams) in.readObject();
						InputLayer inputLayer = new InputLayer(Network.this,layerParams);
						inputLayer.loadModel(in);
						layers.add(inputLayer);
					} catch (ClassNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}else if(layerType.equals(ConvolutionLayer.TYPE)){
					try {
						BlobParams layerParams = (BlobParams) in.readObject();
						BlobParams kernelParams = (BlobParams) in.readObject();
						ConvolutionLayer conv = new ConvolutionLayer(Network.this,layerParams,kernelParams);
						conv.loadModel(in);
						layers.add(conv);
					} catch (ClassNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}else if(layerType.equals(FullConnectionLayer.TYPE)){
					try {
						BlobParams layerParams = (BlobParams) in.readObject();
						FullConnectionLayer fc = new FullConnectionLayer(Network.this,layerParams);
						fc.loadModel(in);
						layers.add(fc);
					} catch (ClassNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}else if(layerType.equals(PoolMaxLayer.TYPE)){
					try {
						BlobParams layerParams = (BlobParams) in.readObject();
						BlobParams kernelParams = (BlobParams) in.readObject();	
						int kernelHeightStride = in.readInt();
						int kernelWidthStride = in.readInt();
						PoolMaxLayer pMax = new PoolMaxLayer(Network.this,layerParams,kernelParams,kernelHeightStride,kernelWidthStride);
						pMax.loadModel(in);
						layers.add(pMax);
					} catch (ClassNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}else if(layerType.equals(PoolMeanLayer.TYPE)){
					try {
						BlobParams layerParams = (BlobParams) in.readObject();
						BlobParams kernelParams = (BlobParams) in.readObject();	
						int kernelHeightStride = in.readInt();
						int kernelWidthStride = in.readInt();
						PoolMeanLayer pMean = new PoolMeanLayer(Network.this,layerParams,kernelParams,kernelHeightStride,kernelWidthStride);
						pMean.loadModel(in);
						layers.add(pMean);
					} catch (ClassNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}else if(layerType.equals(SoftMaxLayer.TYPE)){
					try {
						BlobParams layerParams = (BlobParams) in.readObject();
						SoftMaxLayer softMax = new SoftMaxLayer(Network.this,layerParams);
						softMax.loadModel(in);
						layers.add(softMax);
					} catch (ClassNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}else{
					System.out.println("load model error");
					System.exit(-1);
				}
			}
			String end = in.readUTF();
			if(!end.equals(MODEL_END)){
				System.out.println("end is "+end+" file format error");
			}
			in.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//		System.out.println("load model finished");
	}
}
