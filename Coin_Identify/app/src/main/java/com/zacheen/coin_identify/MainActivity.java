package com.zacheen.coin_identify;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.MeteringRectangle;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.ImageReader;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.TextureView;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static android.support.v4.math.MathUtils.clamp;

public class MainActivity extends AppCompatActivity {

    private static final SparseIntArray ORIENTATION = new SparseIntArray();

    static {
        ORIENTATION.append(Surface.ROTATION_0, 90);
        ORIENTATION.append(Surface.ROTATION_90, 0);
        ORIENTATION.append(Surface.ROTATION_180, 270);
        ORIENTATION.append(Surface.ROTATION_270, 180);
    }

    private String mCameraId;
    private Size mPreviewSize;
    private Size mCaptureSize;
    private HandlerThread mCameraThread;
    private Handler mCameraHandler;
    private CameraDevice mCameraDevice;
    private TextureView mTextureView;
    private ImageReader mImageReader;
    private CaptureRequest.Builder mCaptureRequestBuilder;
    private CaptureRequest mCaptureRequest;
    private CameraCaptureSession mCameraCaptureSession;

    //  new on onCreate
    private TextureView.SurfaceTextureListener mTextureListener ;
    private CameraDevice.StateCallback mStateCallback ;
    private CameraCaptureSession.CaptureCallback mCaptureCallback;

    //
    Rect rect;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        rect = new Rect();
        rect.top = 50;
        rect.bottom = 100;
        rect.left = 50;
        rect.right = 100;
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        mTextureListener = new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                //当SurefaceTexture可用的时候，设置相机参数并打开相机
                setupCamera(width, height);
                openCamera();
            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                return false;
            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surface) {

            }
        };

        mStateCallback = new CameraDevice.StateCallback() {
            @Override
            public void onOpened(@NonNull CameraDevice camera) {
                mCameraDevice = camera;
                startPreview();
            }

            @Override
            public void onDisconnected(@NonNull CameraDevice camera) {
                camera.close();
                mCameraDevice = null;
            }

            @Override
            public void onError(@NonNull CameraDevice camera, int error) {
                camera.close();
                mCameraDevice = null;
            }
        };

        mCaptureCallback = new CameraCaptureSession.CaptureCallback() {
            @Override
            public void onCaptureProgressed(@NonNull CameraCaptureSession session,@NonNull  CaptureRequest request,@NonNull  CaptureResult partialResult) {
            }

            @Override
            public void onCaptureCompleted(@NonNull CameraCaptureSession session,@NonNull  CaptureRequest request,@NonNull  TotalCaptureResult result) {
                capture();
            }
        };

        mTextureView = (TextureView) findViewById(R.id.textureView);

        // finish go to onResume
    }

    // call when onResume
    @Override
    protected void onResume() {
        super.onResume();
        startCameraThread();
        if (!mTextureView.isAvailable()) {
            mTextureView.setSurfaceTextureListener(mTextureListener);
        } else {
            //fisrt time I think will go through here
            // and the app recover
            startPreview();
        }
    }

    private void startCameraThread() {
        mCameraThread = new HandlerThread("CameraThread");
        mCameraThread.start();
        mCameraHandler = new Handler(mCameraThread.getLooper());
    }

    private void startPreview() {
        SurfaceTexture mSurfaceTexture = mTextureView.getSurfaceTexture();
        mSurfaceTexture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
        Surface previewSurface = new Surface(mSurfaceTexture);
        try {
            mCaptureRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            mCaptureRequestBuilder.addTarget(previewSurface);
            mCaptureRequestBuilder.set(CaptureRequest.FLASH_MODE,CaptureRequest.FLASH_MODE_TORCH);
            mCameraDevice.createCaptureSession(Arrays.asList(previewSurface, mImageReader.getSurface()), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession session) {
                    try {
                        mCaptureRequest = mCaptureRequestBuilder.build();
                        mCameraCaptureSession = session;
                        mCameraCaptureSession.setRepeatingRequest(mCaptureRequest, null, mCameraHandler);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onConfigureFailed(CameraCaptureSession session) {

                }
            }, mCameraHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }
    // end call when onResume

    // permission
    private static final int REQUEST_PERMISSION_CAMERA_CODE = 1;
    public void request_permission(){
        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        if(permissionCheck == PackageManager.PERMISSION_DENIED){
            String[] permissions = {Manifest.permission.CAMERA};
            requestPermissions(permissions, 0);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch(requestCode){
            case REQUEST_PERMISSION_CAMERA_CODE :
                ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.CAMERA},REQUEST_PERMISSION_CAMERA_CODE);
                int grantResult = grantResults[0];
                boolean granted = (grantResult == PackageManager.PERMISSION_GRANTED);
                Log.i("permission", "onRequestPermissionsResult granted=" + granted);
        }
    }
// permission end

    // call by onSurfaceTextureAvailable
    private void setupCamera(int width, int height) {
        //获取摄像头的管理者CameraManager
        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            for (String cameraId : manager.getCameraIdList()) {
                CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);

                Integer facing = characteristics.get(CameraCharacteristics.LENS_FACING);
                //此处默认打开后置摄像头  其他跳過
                if (facing != null && facing == CameraCharacteristics.LENS_FACING_FRONT)
                    continue;

                //flash light
                Boolean available = characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE);
                Boolean mFlashSupported = available == null ? false : available;
                if(mFlashSupported==true) {
                    manager.setTorchMode(cameraId, true);
                    Log.i("flash_light","turn flash_light on");
                }else{
                    Log.i("flash_light"," can not open flash_light ");
                }
                //获取StreamConfigurationMap，它是管理摄像头支持的所有输出格式和尺寸
                StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                assert map != null;

                //根据TextureView的尺寸设置预览尺寸
                mPreviewSize = getOptimalSize(map.getOutputSizes(SurfaceTexture.class), width, height);

                //获取相机支持的最大拍照尺寸
                mCaptureSize = Collections.max(Arrays.asList(map.getOutputSizes(ImageFormat.JPEG)), new Comparator<Size>() {
                    @Override
                    public int compare(Size lhs, Size rhs) {
                        return Long.signum(lhs.getWidth() * lhs.getHeight() - rhs.getHeight() * rhs.getWidth());
                    }
                });

                //此ImageReader用于拍照所需
                setupImageReader();
                mCameraId = cameraId;
                break;
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    //选择sizeMap中大于并且最接近width和height的size
    private Size getOptimalSize(Size[] sizeMap, int width, int height) {
        List<Size> sizeList = new ArrayList<>();
        for (Size option : sizeMap) {
            if (width > height) {
                if (option.getWidth() > width && option.getHeight() > height) {
                    sizeList.add(option);
                }
            } else {
                if (option.getWidth() > height && option.getHeight() > width) {
                    sizeList.add(option);
                }
            }
        }
        if (sizeList.size() > 0) {
            return Collections.min(sizeList, new Comparator<Size>() {
                @Override
                public int compare(Size lhs, Size rhs) {
                    return Long.signum(lhs.getWidth() * lhs.getHeight() - rhs.getWidth() * rhs.getHeight());
                }
            });
        }
        return sizeMap[0];
    }

    //  call by onSurfaceTextureAvailable
    private void openCamera() {
        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            if(ActivityCompat.checkSelfPermission(this,Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED){
                return;
            }
            manager.openCamera(mCameraId, mStateCallback, mCameraHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void lockFocus() {
        ExecutorService executorService = Executors.newCachedThreadPool();
        executorService.submit(new Callable_for_speak("you have take a picture",0,this));
        executorService.shutdown();

        try {
            mCaptureRequestBuilder.set(CaptureRequest.FLASH_MODE,CaptureRequest.FLASH_MODE_TORCH);
            mCaptureRequestBuilder.set(CaptureRequest.CONTROL_AF_REGIONS, new MeteringRectangle[] {new MeteringRectangle(rect, 1000)});
            mCaptureRequestBuilder.set(CaptureRequest.CONTROL_AF_REGIONS, new MeteringRectangle[] {new MeteringRectangle(rect, 1000)});
            mCaptureRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_AUTO);
            mCaptureRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_START);
            mCaptureRequestBuilder.set(CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER, CameraMetadata.CONTROL_AE_PRECAPTURE_TRIGGER_START);
            mCameraCaptureSession.capture(mCaptureRequestBuilder.build(), mCaptureCallback, mCameraHandler);

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }
//  end    be call when touching scream  ( in  xml id:photoButton)

    //when call onCaptureCompleted()  // suppose to be call when finish takeing picture
    private void capture() {
        try {
            final CaptureRequest.Builder mCaptureBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            int rotation = getWindowManager().getDefaultDisplay().getRotation();
            mCaptureBuilder.addTarget(mImageReader.getSurface());
            mCaptureBuilder.set(CaptureRequest.JPEG_ORIENTATION, ORIENTATION.get(rotation));
            mCaptureBuilder.set(CaptureRequest.FLASH_MODE,CaptureRequest.FLASH_MODE_TORCH);
            CameraCaptureSession.CaptureCallback CaptureCallback = new CameraCaptureSession.CaptureCallback() {
                @Override
                public void onCaptureCompleted(@NonNull CameraCaptureSession session,@NonNull  CaptureRequest request,@NonNull  TotalCaptureResult result) {
                    Toast.makeText(getApplicationContext(), "Image Saved!", Toast.LENGTH_SHORT).show();
                    unLockFocus();
                }
            };
            mCameraCaptureSession.stopRepeating();
            mCameraCaptureSession.capture(mCaptureBuilder.build(), CaptureCallback, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void unLockFocus() {
        try {
            mCaptureRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_CANCEL);
            //mCaptureRequestBuilder.set(CaptureRequest.FLASH_MODE,CaptureRequest.FLASH_MODE_TORCH);
            //mCameraCaptureSession.capture(mCaptureRequestBuilder.build(), null, mCameraHandler);
            // here didnt build then do nothing
            mCameraCaptureSession.setRepeatingRequest(mCaptureRequest, null, mCameraHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mCameraCaptureSession != null) {
            mCameraCaptureSession.close();
            mCameraCaptureSession = null;
        }

        if (mCameraDevice != null) {
            mCameraDevice.close();
            mCameraDevice = null;
        }

        if (mImageReader != null) {
            mImageReader.close();
            mImageReader = null;
        }
    }

    // have call in setupCamera
    private void setupImageReader() {
        //2代表ImageReader中最多可以获取两帧图像流
        mImageReader = ImageReader.newInstance(mCaptureSize.getWidth(), mCaptureSize.getHeight(),
                ImageFormat.JPEG, 2);
        mImageReader.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener() {
            @Override
            public void onImageAvailable(ImageReader reader) {
                mCameraHandler.post(new ImageSaver_Runnable(reader.acquireNextImage()));
            }
        }, mCameraHandler);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        final int action = event.getAction();
        if(action == MotionEvent.ACTION_DOWN) {
            double x = event.getX(), y = event.getY(), tmp;
            Log.i("touch", "touch scream at " + x + " , " + y);
// 取出来的图像如果有旋转角度的话，则需要将宽高交换下
            int realPreviewWidth = mPreviewSize.getWidth(), realPreviewHeight = mPreviewSize.getHeight();
            Log.i("mPreviewSize", "mPreviewSize " + realPreviewWidth + " , " + realPreviewWidth);
//        if (90 == mDisplayRotate || 270 == mDisplayRotate) {
//            realPreviewWidth = mPreviewSize.getWidth();
//            realPreviewHeight = mPreviewSize.getHeight();
//        }

// 计算摄像头取出的图像相对于view放大了多少，以及有多少偏移
            double imgScale = 1.0, verticalOffset = 0, horizontalOffset = 0;
            if (realPreviewHeight * mTextureView.getWidth() > realPreviewWidth * mTextureView.getHeight()) {
                imgScale = mTextureView.getWidth() * 1.0 / realPreviewWidth;
                verticalOffset = (realPreviewHeight - mTextureView.getHeight() / imgScale) / 2;
            } else {
                imgScale = mTextureView.getHeight() * 1.0 / realPreviewHeight;
                horizontalOffset = (realPreviewWidth - mTextureView.getWidth() / imgScale) / 2;
            }

// 将点击的坐标转换为图像上的坐标
            x = x / imgScale + horizontalOffset;
            y = y / imgScale + verticalOffset;
//        if (90 == mDisplayRotate) {
            tmp = x;
            x = y;
            y = mPreviewSize.getHeight() - tmp;
//        } else if (270 == mDisplayRotate) {
//            tmp = x; x = mPreviewSize.getWidth() - y; y = tmp;
//        }

// 计算取到的图像相对于裁剪区域的缩放系数，以及位移
            Rect cropRegion = mCaptureRequest.get(CaptureRequest.SCALER_CROP_REGION);
            if (null == cropRegion) {
                Log.e("error", "can't get crop region");
                //cropRegion = mActiveArraySize;
            }

            int cropWidth = cropRegion.width(), cropHeight = cropRegion.height();
            if (mPreviewSize.getHeight() * cropWidth > mPreviewSize.getWidth() * cropHeight) {
                imgScale = cropHeight * 1.0 / mPreviewSize.getHeight();
                verticalOffset = 0;
                horizontalOffset = (cropWidth - imgScale * mPreviewSize.getWidth()) / 2;
            } else {
                imgScale = cropWidth * 1.0 / mPreviewSize.getWidth();
                horizontalOffset = 0;
                verticalOffset = (cropHeight - imgScale * mPreviewSize.getHeight()) / 2;
            }

//将点击区域相对于app取到的图像坐标，转化为相对于成像区域的坐标：
// 将点击区域相对于图像的坐标，转化为相对于成像区域的坐标
            x = x * imgScale + horizontalOffset + cropRegion.left;
            y = y * imgScale + verticalOffset + cropRegion.top;

//按照对焦区域为成像区域的0.1倍计算对焦的矩形：
            double tapAreaRatio = 0.1;
            rect = new Rect();
            rect.left = clamp((int) (x - tapAreaRatio / 2 * cropRegion.width()), 0, cropRegion.width());
            rect.right = clamp((int) (x + tapAreaRatio / 2 * cropRegion.width()), 0, cropRegion.width());
            rect.top = clamp((int) (y - tapAreaRatio / 2 * cropRegion.height()), 0, cropRegion.height());
            rect.bottom = clamp((int) (y + tapAreaRatio / 2 * cropRegion.height()), 0, cropRegion.height());

            lockFocus();
        }
        return super.onTouchEvent(event);
    }
}
