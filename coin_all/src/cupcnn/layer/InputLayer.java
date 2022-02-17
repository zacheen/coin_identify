package cupcnn.layer;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import cupcnn.Network;
import cupcnn.data.Blob;
/*
 * InputLayer��Ҫ������ռ�ݵ�һ��λ�ã��ǵķ��򴫲����㷨������ʵ��
 */
import cupcnn.data.BlobParams;

public class InputLayer extends Layer{
	public static final String TYPE = "InputLayer";
	public InputLayer(Network network, BlobParams parames) {
		super(network, parames);
		// TODO Auto-generated constructor stub
	}

	@Override
	public String getType() {
		// TODO Auto-generated method stub
		return TYPE;
	}

	@Override
	public void prepare() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void forward() {
		// TODO Auto-generated method stub
		//do nothing
	}

	@Override
	public void backward() {
		// TODO Auto-generated method stub
		//do nothing
	}
	
	public void setInputData(Blob input){
		Blob curData = mNetwork.getDatas().get(id);
		input.cloneTo(curData);
	}

	@Override
	public void saveModel(ObjectOutputStream out) {
		// TODO Auto-generated method stub
		try {
			out.writeUTF(getType());
			//�����ʱ��batchҲ����layerParams��number����1����Ϊpredict��ʱ����Ϊ����ʹ�õ�ʱ�����batchһ�㶼��1
//			layerParams.setNumbers(1);
			out.writeObject(layerParams);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void loadModel(ObjectInputStream in) {
		// TODO Auto-generated method stub
		//do nothing
	}
}
