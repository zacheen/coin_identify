package com.zacheen.coin_identify;

import android.media.Image;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ImageSaver_Runnable implements Runnable {
    private Image mImage;

    public ImageSaver_Runnable(Image image) {
        mImage = image;
    }

    @Override
    public void run() {
        Log.i("save","saving picture");

        // picture to byte
        ByteBuffer buffer = mImage.getPlanes()[0].getBuffer();
        byte[] data = new byte[buffer.remaining()];
        buffer.get(data);
        mImage.close();

        //send to server
        Thread thread_send = new Thread(new Send_img_Runnable("140.117.182.119",4567,data));
//        Thread thread_send = new Thread(new Send_img_Runnable("140.117.174.45",4567,data));
        thread_send.start();


        //save to sdcard


        String path = Environment.getExternalStorageDirectory() + "/Camera_coin/";
        File mImageFile = new File(path);

        // create_folder_if_there_isnt
        if (!mImageFile.exists()) {
            mImageFile.mkdir();
        }

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String fileName = path + "IMG_" + timeStamp + ".jpg";
        FileOutputStream fos = null;

        try {
            fos = new FileOutputStream(fileName);
            fos.write(data, 0, data.length);
            Log.i("save","finish saving picture ");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
