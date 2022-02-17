package OurCNN;
 /*
  * read database
  * ReadFile(String srcPath)
  * List<DATA> loadDATA()
  */
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class ReadFile {

    private String srcPath;
    public List<DATA> images;


    public ReadFile(String srcPath) {
        this.srcPath = srcPath;
    }

    public List<DATA> loadDATA() throws IOException {
        ObjectInputStream src = new ObjectInputStream(new FileInputStream(srcPath));
    	images = new ArrayList<DATA>();
        int dataNum = src.readInt();
        try {
        	for(int i = 0; i<dataNum; i++){
				images.add( (DATA) src.readObject());
        	}
        } catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return images;
    }
}
