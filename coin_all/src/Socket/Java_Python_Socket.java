package Socket;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.opencv.core.Core;

public class Java_Python_Socket {
	static {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
	}
	
	public static void main(String[] args) {
		Lock lock = new ReentrantLock();
		
		for(int i = 0;i<3;i++) {		
			Thread t = new Thread(new Send_to_py(i,lock));
			t.start();
		}
	}
}
