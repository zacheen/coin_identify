package cut_coin;

/* this is a structure for the DB way in RGB to LB 
 * 
 * 	ver		time		by		description
 * ---------------------------------------------
 *	1.0		????		GC		²Ä¤@ª© 
 */
public class LBData {
	private byte[] output = new byte[2];
	
	public byte[] getOutput(){
		return output;
	}
	public byte[] getOutput(byte[] input ,int index){
		System.arraycopy(output, 0, input, index, 2);
		return output;
	}
	public void set(byte[] input){
		System.arraycopy(input, 0, output, 0, 2);
	}
	public void set(byte[] input, int index){
		System.arraycopy(input, index, output, 0, 2);
	}
}
