package cupcnn.data;
/*
 *cupcnn�ĺ���������
 */

import java.io.Serializable;

public class Blob implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private double[] data;  // za think this is all the data?? lable??
	private int numbers;
	private int channels;
	private int width;
	private int height;
	private int id;
	
	public Blob(int numbers,int channels,int height,int width){
		this.numbers = numbers;
		this.channels = channels;
		this.height = height;
		this.width = width;
		
		//System.out.println("total needed size : " + getSize() +  " numbers = " +numbers + " channels = " + channels + " height = " + height + " width = " + width);
		data = new double[getSize()];
	}

	

	public double getDataByParams(int numbers,int channels,int height,int width){
		return data[numbers*get3DSize()+channels*get2DSize()+height*getWidth()+width];
	}
	
	public int getIndexByParams(int numbers,int channels,int height,int width){
		return (numbers*get3DSize()+channels*get2DSize()+height*getWidth()+width);
	}
	
	public int getWidth(){
		return width;
	}
	
	public int getHeight(){
		return height;
	}
	
	public int getChannels(){
		return channels;
	}
	
	public int getNumbers(){
		return numbers;
	}
	
	public int get2DSize(){
		return width * height;
	}
	
	public int get3DSize(){
		return channels*width*height;
	}
	
	public int get4DSize(){
		return numbers*channels*width*height;
	}
	
	public int getSize(){
		return get4DSize();
	}
	
	public void setId(int id){
		this.id = id;
	}
	
	public int getId(){
		return id;
	}
	
	public double[] getData(){
		return data;
	}
	
	public void fillValue(double value){
		for(int i=0;i<data.length;i++){
			data[i] = value;
		}
	}
	
	public void cloneTo(Blob to){
		to.numbers = this.numbers;
		to.channels = this.channels;
		to.height = this.height;
		to.width = this.width;
		double[] toData = to.getData();
		for(int i=0;i<data.length;i++){
			toData[i] = this.data[i];
		}
	}
	
}
