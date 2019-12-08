package com.zju.openeye;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraActivity;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class HdActivity extends CameraActivity implements CameraBridgeViewBase.CvCameraViewListener2 {
    private static final String TAG = "OpenEye::HdActivity";
    private static final Scalar    FACE_RECT_COLOR     = new Scalar(0, 255, 0, 255);
    public static final int        JAVA_DETECTOR       = 0;
    public static final int        NATIVE_DETECTOR     = 1;

    private CameraBridgeViewBase mOpenCvCameraView;
    private Button mTakePhotoBtn;
    private File mCascadeFile;
    private CascadeClassifier mJavaDetector;
    private Mat                    mRgba;
    private Mat                    mGray;
    private int                    mDetectorType       = JAVA_DETECTOR;
    private float                  mRelativeFaceSize   = 0.2f;
    private int                    mAbsoluteFaceSize   = 0;
    private static int             savePhotoProcess    = 0;
    private static String photoPath = "";

    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg){
            super.handleMessage(msg);
            if(msg.what == 1){
                mTakePhotoBtn.performClick();
                Intent intent = new Intent(HdActivity.this, ShowActivity.class);
                intent.putExtra("photoPath", photoPath);
                startActivity(intent);
            }
        }
    };

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_hd);

        mOpenCvCameraView = findViewById(R.id.hd_activity_surface_view);

        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);

        mOpenCvCameraView.setCvCameraViewListener(this);

        mTakePhotoBtn = findViewById(R.id.hd_activity_takephoto_btn);
        initListener();
    }

    private void initListener() {
        mTakePhotoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mRgba != null) {
                    /**
                    if(!mRgba.empty()) {
                        Mat inter = new Mat(mRgba.width(), mRgba.height(), CvType.CV_8UC4);
                        Log.d(TAG,"...............1...............");
                        //将四通道的RGBA转为三通道的BGR，重要！！
                        Imgproc.cvtColor(mRgba, inter, Imgproc.COLOR_RGBA2BGR);
                        Log.d(TAG,"...............2...............");
                        saveImageToGallery(matToBitmap(inter));
                    }*/

                    if(!mRgba.empty()) {
                        Mat inter = new Mat(mRgba.width(), mRgba.height(), CvType.CV_8UC4);
                        Log.d(TAG,"...............1...............");
                        //将四通道的RGBA转为三通道的BGR，重要！！
                        Imgproc.cvtColor(mRgba, inter, Imgproc.COLOR_RGBA2BGR);
                        Log.d(TAG,"...............2...............");
                        File sdDir = null;
                        //判断是否存在机身内存
                        boolean sdCardExist = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
                        if(sdCardExist) {
                            //获得机身储存根目录
                            sdDir = Environment.getExternalStorageDirectory();
                            Log.d(TAG,"...............3...............");
                        }

                        //将拍摄准确时间作为文件名
                        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
                        String filename = sdf.format(new Date());
                        String savepath = sdDir + "/Pictures/Opencv/";
                        File f = new File(savepath);
                        if(!f.exists()){
                            f.mkdirs();
                        }
                        String filePath = sdDir + "/Pictures/Opencv/" + filename + ".png";
                        Log.d(TAG,"..............."+filePath+"...............");
                        photoPath = filePath;
                        //将转化后的BGR矩阵内容写入到文件中
                        boolean isOrnot = Imgcodecs.imwrite(filePath, inter);
                        Log.d(TAG,"..............."+isOrnot+"...............");
                        Toast.makeText(HdActivity.this, "图片保存到: "+ filePath, Toast.LENGTH_SHORT).show();
                    }

                }
            }
        });


    }

    //Mat转Bitmap
    public static Bitmap matToBitmap(Mat mat) {
        Bitmap resultBitmap = null;
        if (mat != null) {
            resultBitmap = Bitmap.createBitmap(mat.cols(), mat.rows(), Bitmap.Config.ARGB_8888);
            if (resultBitmap != null)
                Utils.matToBitmap(mat, resultBitmap);
        }
        return resultBitmap;
    }

    public static int saveImageToGallery(Bitmap bmp) {
        //生成路径
        String root = Environment.getExternalStorageDirectory().getAbsolutePath();
        String dirName = "Pictures";
        File appDir = new File(root , dirName);
        if (!appDir.exists()) {
            appDir.mkdirs();
        }
        String fileName = "bitmap.jpg";

        //获取文件
        File file = new File(appDir, fileName);
        Log.d(TAG,"..............."+file+"...............");
        photoPath = file.getAbsolutePath();
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            return 0;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return -1;
    }

    @Override
    public void onPause()
    {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        savePhotoProcess = 0;
        // 使用应用本地native库
        // OpenCVLoader.initDebug();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_4_0, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    @Override
    protected List<? extends CameraBridgeViewBase> getCameraViewList() {
        return Collections.singletonList(mOpenCvCameraView);
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        mGray = new Mat();
        mRgba = new Mat();
    }

    @Override
    public void onCameraViewStopped() {
        mGray.release();
        mRgba.release();
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        mRgba = inputFrame.rgba();
        mGray = inputFrame.gray();

        if (mAbsoluteFaceSize == 0) {
            int height = mGray.rows();
            if (Math.round(height * mRelativeFaceSize) > 0) {
                mAbsoluteFaceSize = Math.round(height * mRelativeFaceSize);
            }
        }

        MatOfRect faces = new MatOfRect();

        if (mDetectorType == JAVA_DETECTOR) {
            if (mJavaDetector != null)
                mJavaDetector.detectMultiScale(mGray, faces, 1.1, 2, 2, // TODO: objdetect.CV_HAAR_SCALE_IMAGE
                        new Size(mAbsoluteFaceSize, mAbsoluteFaceSize), new Size());
        }
        /**
         else if (mDetectorType == NATIVE_DETECTOR) {
         if (mNativeDetector != null)
         mNativeDetector.detect(mGray, faces);
         }
         */
        else {
            Log.e(TAG, "Detection method is not selected!");
        }

        Rect[] facesArray = faces.toArray();
        if (facesArray.length >= 4 && savePhotoProcess == 0) {
            savePhotoProcess = 1;
            Log.d(TAG, "face numbers : " + facesArray.length);
            new ClickThread().start();
        }

        for (int i = 0; i < facesArray.length; i++)
            Imgproc.rectangle(mRgba, facesArray[i].tl(), facesArray[i].br(), FACE_RECT_COLOR, 3);

        return mRgba;

        // return inputFrame.rgba();
    }

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");

                    try {
                        // load cascade file from application resources
                        InputStream is = getResources().openRawResource(R.raw.lbpcascade_frontalface);
                        File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
                        mCascadeFile = new File(cascadeDir, "lbpcascade_frontalface.xml");
                        FileOutputStream os = new FileOutputStream(mCascadeFile);

                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        while ((bytesRead = is.read(buffer)) != -1) {
                            os.write(buffer, 0, bytesRead);
                        }
                        is.close();
                        os.close();

                        mJavaDetector = new CascadeClassifier(mCascadeFile.getAbsolutePath());
                        if (mJavaDetector.empty()) {
                            Log.e(TAG, "Failed to load cascade classifier");
                            mJavaDetector = null;
                        } else
                            Log.i(TAG, "Loaded cascade classifier from " + mCascadeFile.getAbsolutePath());

                        cascadeDir.delete();

                    } catch (IOException e) {
                        e.printStackTrace();
                        Log.e(TAG, "Failed to load cascade. Exception thrown: " + e);
                    }

                    mOpenCvCameraView.enableView();
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    private class ClickThread extends Thread{
        @Override
        public void run() {
            super.run();
            try {
                // Thread.sleep(500);
                Log.d(TAG,"kaishi.........");

                Message message = new Message();
                message.what = 1;
                handler.sendMessage(message);
            }catch (Exception e){
                Log.d(TAG,e.getMessage());
            }
        }
    }
}
