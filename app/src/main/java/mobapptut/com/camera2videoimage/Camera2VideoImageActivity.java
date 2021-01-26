package mobapptut.com.camera2videoimage;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.CamcorderProfile;
import android.media.Image;
import android.media.ImageReader;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Chronometer;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class Camera2VideoImageActivity extends AppCompatActivity {

    private static final String TAG = "Camera2VideoImageActivi";

    private static final int REQUEST_CAMERA_PERMISSION_RESULT = 0;
    private static final int REQUEST_WRITE_EXTERNAL_STORAGE_PERMISSION_RESULT = 1;
    private static final int STATE_PREVIEW = 0;
    private static final int STATE_WAIT_LOCK = 1;
    private long mLastTimestamp;
    private int mCaptureState = STATE_PREVIEW;
    private int mCaptureState2 = STATE_PREVIEW;
    private AutoFitTextureView mTextureView;
    private AutoFitTextureView mTextureView2;
    private int mSwitchCamera = 0;//0 mTextureView,index 0,back camera;1 mTextureView2,index 1 ,front camera;
    Handler mHandler = new Handler();
    private TextureView.SurfaceTextureListener mSurfaceTextureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            Log.d("Texturesize","available w="+width+",h="+height);
            setupCamera(width, height);
            connectCamera();
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
            Log.d("Texturesize","changed w="+width+",h="+height);
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            Log.d("Texturesize","destroy");
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {
            //Log.d("Texturesize","update ");
        }
    };
    private TextureView.SurfaceTextureListener mSurfaceTextureListener2 = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            Log.d("Texturesize","available w="+width+",h="+height);
            setupCamera2(width, height);
            connectCamera2();
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
            Log.d("Texturesize","changed w="+width+",h="+height);
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            Log.d("Texturesize","destroy");
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {
            //Log.d("Texturesize","update ");
        }
    };
    private CameraDevice mCameraDevice;
    private CameraDevice.StateCallback mCameraDeviceStateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(CameraDevice camera) {
            Log.d(TAG,"onOpened index 0");
            Toast.makeText(getApplicationContext(),
                    "onOpened index 0 connection made!", Toast.LENGTH_SHORT).show();
            mCameraDevice = camera;
            mMediaRecorder = new MediaRecorder();
            if(mIsRecording) {
                try {
                    createVideoFileName();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                startRecord();
                mMediaRecorder.start();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mChronometer.setBase(SystemClock.elapsedRealtime());
                        mChronometer.setVisibility(View.VISIBLE);
                        mChronometer.start();
                    }
                });
            } else {
                startPreview();
            }
            Toast.makeText(getApplicationContext(),
                     "Camera index 0 connection made!", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onDisconnected(CameraDevice camera) {
            camera.close();
            mCameraDevice = null;
            Toast.makeText(getApplicationContext(),
                    "Camera index 0 disconnection !", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onError(CameraDevice camera, int error) {
            camera.close();
            mCameraDevice = null;
            Toast.makeText(getApplicationContext(),
                    "Camera index 0 error !", Toast.LENGTH_SHORT).show();
        }
    };
    private CameraDevice mCameraDevice2;
    private CameraDevice.StateCallback mCameraDeviceStateCallback2 = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(CameraDevice camera) {
            Log.d(TAG,"onOpened index 1");
            mCameraDevice2 = camera;
            mMediaRecorder2 = new MediaRecorder();
            if(mIsRecording2) {
                try {
                    createVideoFileName();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                startRecord2();
                mMediaRecorder2.start();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mChronometer2.setBase(SystemClock.elapsedRealtime());
                        mChronometer2.setVisibility(View.VISIBLE);
                        mChronometer2.start();
                    }
                });
            } else {
                startPreview2();
            }

             Toast.makeText(getApplicationContext(),
                     "onOpened index 1 connection made!", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onDisconnected(CameraDevice camera) {
            camera.close();
            mCameraDevice2 = null;
            Toast.makeText(getApplicationContext(),
                    "Camera index 1 disconnection !", Toast.LENGTH_SHORT).show();

        }

        @Override
        public void onError(CameraDevice camera, int error) {
            camera.close();
            mCameraDevice2 = null;
            Toast.makeText(getApplicationContext(),
                    "Camera index 1 error !", Toast.LENGTH_SHORT).show();
        }
    };
    private HandlerThread mBackgroundHandlerThread;
    private Handler mBackgroundHandler;

    //camera index 0
    private String mCameraId;
    private Size mPreviewSize;
    private Size mVideoSize;
    private Size mImageSize;
    private ImageReader mImageReader;
    //camera index 1
    private String mCameraId2;
    private Size mPreviewSize2;
    private Size mVideoSize2;
    private Size mImageSize2;
    private ImageReader mImageReader2;

    private final ImageReader.OnImageAvailableListener mOnImageAvailableListener = new
            ImageReader.OnImageAvailableListener() {
                @Override
                public void onImageAvailable(ImageReader reader) {
                    Log.d(TAG,"onImageAvailable="+reader);
                    mBackgroundHandler.post(new ImageSaver(reader.acquireLatestImage()));
                }
            };
    private final ImageReader.OnImageAvailableListener mOnImageAvailableListener2 = new
            ImageReader.OnImageAvailableListener() {
                @Override
                public void onImageAvailable(ImageReader reader) {
                    Log.d(TAG,"onImageAvailable="+reader);
                    mBackgroundHandler.post(new ImageSaver(reader.acquireLatestImage()));
                }
            };
    private class ImageSaver implements Runnable {

        private final Image mImage;

        public ImageSaver(Image image) {
            mImage = image;
        }

        @Override
        public void run() {
            ByteBuffer byteBuffer = mImage.getPlanes()[0].getBuffer();
            byte[] bytes = new byte[byteBuffer.remaining()];
            byteBuffer.get(bytes);

            FileOutputStream fileOutputStream = null;
            try {
                fileOutputStream = new FileOutputStream(mImageFileName);
                fileOutputStream.write(bytes);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                mImage.close();

                Intent mediaStoreUpdateIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                mediaStoreUpdateIntent.setData(Uri.fromFile(new File(mImageFileName)));
                sendBroadcast(mediaStoreUpdateIntent);

                if(fileOutputStream != null) {
                    try {
                        fileOutputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

        }
    }
    //camera index 0
    private MediaRecorder mMediaRecorder;
    private Chronometer mChronometer;
    private int mTotalRotation;
    //camera index 1
    private MediaRecorder mMediaRecorder2;
    private Chronometer mChronometer2;
    private int mTotalRotation2;

    private CameraCaptureSession mPreviewCaptureSession;
    private CameraCaptureSession.CaptureCallback mPreviewCaptureCallback = new
            CameraCaptureSession.CaptureCallback() {

                private void process(CaptureResult captureResult) {
                    switch (mCaptureState) {
                        case STATE_PREVIEW:
                            // Do nothing
                            break;
                        case STATE_WAIT_LOCK:
                            mCaptureState = STATE_PREVIEW;
                            Integer afState = captureResult.get(CaptureResult.CONTROL_AF_STATE);
                            Log.d(TAG,"afState="+afState);
//                            if(afState == CaptureResult.CONTROL_AF_STATE_FOCUSED_LOCKED ||
//                                    afState == CaptureResult.CONTROL_AF_STATE_NOT_FOCUSED_LOCKED) {
                                //Toast.makeText(getApplicationContext(), "AF Locked!", Toast.LENGTH_SHORT).show();
                                startStillCaptureRequest();
//                            }
                            break;
                    }
                }

                @Override
                public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
                    super.onCaptureCompleted(session, request, result);

                    process(result);
                }
            };
    private CameraCaptureSession mPreviewCaptureSession2;
    private CameraCaptureSession.CaptureCallback mPreviewCaptureCallback2 = new
            CameraCaptureSession.CaptureCallback() {

                private void process2(CaptureResult captureResult) {
                    switch (mCaptureState2) {
                        case STATE_PREVIEW:
                            // Do nothing
                            break;
                        case STATE_WAIT_LOCK:
                            mCaptureState2 = STATE_PREVIEW;
                            Integer afState = captureResult.get(CaptureResult.CONTROL_AF_STATE);
                            Log.d(TAG,"afState="+afState);
//                            if(afState == CaptureResult.CONTROL_AF_STATE_FOCUSED_LOCKED ||
//                                    afState == CaptureResult.CONTROL_AF_STATE_NOT_FOCUSED_LOCKED) {
                            //Toast.makeText(getApplicationContext(), "AF Locked!", Toast.LENGTH_SHORT).show();
                            startStillCaptureRequest2();
//                            }
                            break;
                    }
                }

                @Override
                public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
                    super.onCaptureCompleted(session, request, result);

                    process2(result);
                }
            };


    private CameraCaptureSession mRecordCaptureSession;
    private CameraCaptureSession.CaptureCallback mRecordCaptureCallback = new
            CameraCaptureSession.CaptureCallback() {

                private void process(CaptureResult captureResult) {
                    switch (mCaptureState) {
                        case STATE_PREVIEW:
                            // Do nothing
                            break;
                        case STATE_WAIT_LOCK:
                            mCaptureState = STATE_PREVIEW;
                            Integer afState = captureResult.get(CaptureResult.CONTROL_AF_STATE);
//                            if(afState == CaptureResult.CONTROL_AF_STATE_FOCUSED_LOCKED ||
//                                    afState == CaptureResult.CONTROL_AF_STATE_NOT_FOCUSED_LOCKED) {
                                Toast.makeText(getApplicationContext(), "AF Locked!", Toast.LENGTH_SHORT).show();
                                startStillCaptureRequest();
//                            }
                            break;
                    }
                }

                @Override
                public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
                    super.onCaptureCompleted(session, request, result);

                    process(result);
                }
            };
    private CameraCaptureSession mRecordCaptureSession2;
    private CameraCaptureSession.CaptureCallback mRecordCaptureCallback2 = new
            CameraCaptureSession.CaptureCallback() {

                private void process2(CaptureResult captureResult) {
                    switch (mCaptureState2) {
                        case STATE_PREVIEW:
                            // Do nothing
                            break;
                        case STATE_WAIT_LOCK:
                            mCaptureState2 = STATE_PREVIEW;
                            Integer afState = captureResult.get(CaptureResult.CONTROL_AF_STATE);
//                            if(afState == CaptureResult.CONTROL_AF_STATE_FOCUSED_LOCKED ||
//                                    afState == CaptureResult.CONTROL_AF_STATE_NOT_FOCUSED_LOCKED) {
                            Toast.makeText(getApplicationContext(), "AF Locked!", Toast.LENGTH_SHORT).show();
                            startStillCaptureRequest2();
//                            }
                            break;
                    }
                }

                @Override
                public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
                    super.onCaptureCompleted(session, request, result);

                    process2(result);
                }
            };
    //camera index 0
    private CaptureRequest.Builder mCaptureRequestBuilder;
    private ImageButton mRecordImageButton;
    private ImageButton mStillImageButton;
    private boolean mIsRecording = false;
    private boolean mIsTimelapse = false;
    //camera index 1
    private CaptureRequest.Builder mCaptureRequestBuilder2;
    private ImageButton mRecordImageButton2;
    private ImageButton mStillImageButton2;
    private boolean mIsRecording2 = false;
    private boolean mIsTimelapse2 = false;

    private File mVideoFolder;
    private String mVideoFileName;
    private File mImageFolder;
    private String mImageFileName;

    private static SparseIntArray ORIENTATIONS = new SparseIntArray();
    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 0);
        ORIENTATIONS.append(Surface.ROTATION_90, 90);
        ORIENTATIONS.append(Surface.ROTATION_180, 180);
        ORIENTATIONS.append(Surface.ROTATION_270, 270);
    }

    private static class CompareSizeByArea implements Comparator<Size> {

        @Override
        public int compare(Size lhs, Size rhs) {
            return Long.signum( (long)(lhs.getWidth() * lhs.getHeight()) -
                    (long)(rhs.getWidth() * rhs.getHeight()));
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                        | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        );
        setContentView(R.layout.activity_camera2_video_image);

        createVideoFolder();
        createImageFolder();

        mChronometer = (Chronometer) findViewById(R.id.chronometer);
        mChronometer2 = (Chronometer) findViewById(R.id.chronometer2);
        mTextureView = (AutoFitTextureView) findViewById(R.id.textureView);
        mTextureView2 = (AutoFitTextureView) findViewById(R.id.textureView2);
        mTextureView.setAspectRatio(480,640);
        mTextureView2.setAspectRatio(480,640);
        if(mSwitchCamera == 0)
            mTextureView.setSurfaceTextureListener(mSurfaceTextureListener);
        else
            mTextureView2.setSurfaceTextureListener(mSurfaceTextureListener2);
        mStillImageButton = (ImageButton) findViewById(R.id.cameraImageButton);
        mStillImageButton2 = (ImageButton) findViewById(R.id.cameraImageButton2);
        if(mSwitchCamera == 0) {
            mStillImageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!(mIsTimelapse || mIsRecording)) {
                        checkWriteStoragePermission();
                    }
                    lockFocus();
                }
            });
        } else {
            mStillImageButton2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!(mIsTimelapse2 || mIsRecording2)) {
                        checkWriteStoragePermission2();
                    }
                    lockFocus2();
                }
            });
        }
        mRecordImageButton = (ImageButton) findViewById(R.id.videoOnlineImageButton);
        mRecordImageButton2 = (ImageButton) findViewById(R.id.videoOnlineImageButton2);
        if(mSwitchCamera ==0) {
            mRecordImageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mIsRecording || mIsTimelapse) {
                        mChronometer.stop();
                        mChronometer.setVisibility(View.INVISIBLE);
                        mIsRecording = false;
                        mIsTimelapse = false;
                        mRecordImageButton.setImageResource(R.mipmap.btn_video_online);

                        // Starting the preview prior to stopping recording which should hopefully
                        // resolve issues being seen in Samsung devices.
                        startPreview();
                        mMediaRecorder.stop();
                        mMediaRecorder.reset();

                        Intent mediaStoreUpdateIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                        mediaStoreUpdateIntent.setData(Uri.fromFile(new File(mVideoFileName)));
                        sendBroadcast(mediaStoreUpdateIntent);

                    } else {
                        mIsRecording = true;
                        mRecordImageButton.setImageResource(R.mipmap.btn_video_busy);
                        checkWriteStoragePermission();
                    }
                }
            });
        } else {
            mRecordImageButton2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mIsRecording2 || mIsTimelapse2) {
                        mChronometer2.stop();
                        mChronometer2.setVisibility(View.INVISIBLE);
                        mIsRecording2 = false;
                        mIsTimelapse2 = false;
                        mRecordImageButton2.setImageResource(R.mipmap.btn_video_online);

                        // Starting the preview prior to stopping recording which should hopefully
                        // resolve issues being seen in Samsung devices.
                        startPreview2();
                        mMediaRecorder2.stop();
                        mMediaRecorder2.reset();

                        Intent mediaStoreUpdateIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                        mediaStoreUpdateIntent.setData(Uri.fromFile(new File(mVideoFileName)));
                        sendBroadcast(mediaStoreUpdateIntent);

                    } else {
                        mIsRecording2 = true;
                        mRecordImageButton2.setImageResource(R.mipmap.btn_video_busy);
                        checkWriteStoragePermission2();
                    }
                }
            });
        }
        if(mSwitchCamera == 0) {
            mRecordImageButton.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    mIsTimelapse = true;
                    mRecordImageButton.setImageResource(R.mipmap.btn_timelapse);
                    checkWriteStoragePermission();
                    return true;
                }
            });
        } else {
            mRecordImageButton2.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    mIsTimelapse2 = true;
                    mRecordImageButton2.setImageResource(R.mipmap.btn_timelapse);
                    checkWriteStoragePermission2();
                    return true;
                }
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        startBackgroundThread();
        Log.d(TAG,"onResume mTextureView.isAvailable()="+mTextureView.isAvailable());
        if(mSwitchCamera == 0) {
            if (mTextureView.isAvailable()) {
                Log.d(TAG, "onResume call setupCamera");
                setupCamera(mTextureView.getWidth(), mTextureView.getHeight());
                connectCamera();
            } else {
                mTextureView.setSurfaceTextureListener(mSurfaceTextureListener);
            }
        } else {
            if (mTextureView2.isAvailable()) {
                Log.d(TAG, "onResume call setupCamera");
                setupCamera2(mTextureView2.getWidth(), mTextureView2.getHeight());
                connectCamera2();
            } else {
                mTextureView2.setSurfaceTextureListener(mSurfaceTextureListener2);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == REQUEST_CAMERA_PERMISSION_RESULT) {
            if(grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getApplicationContext(),
                        "Application will not run without camera services", Toast.LENGTH_SHORT).show();
            }
            if(grantResults[1] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getApplicationContext(),
                        "Application will not have audio on record", Toast.LENGTH_SHORT).show();
            }
        }
        if(requestCode == REQUEST_WRITE_EXTERNAL_STORAGE_PERMISSION_RESULT) {
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if(mSwitchCamera == 0) {
                    if (mIsRecording || mIsTimelapse) {
                        mIsRecording = true;
                        mRecordImageButton.setImageResource(R.mipmap.btn_video_busy);
                    }
                } else {
                    if (mIsRecording2 || mIsTimelapse2) {
                        mIsRecording2 = true;
                        mRecordImageButton2.setImageResource(R.mipmap.btn_video_busy);
                    }
                }
                Toast.makeText(this,
                        "Permission successfully granted!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this,
                        "App needs to save video to run", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onPause() {
        Log.d(TAG,"onPause call cloaseCamera");
        closeCamera();
        closeCamera2();
        stopBackgroundThread();

        super.onPause();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocas) {
        super.onWindowFocusChanged(hasFocas);
        View decorView = getWindow().getDecorView();
        if(hasFocas) {
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    }

    private void setupCamera(int width, int height) {
        CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            for(String cameraId : cameraManager.getCameraIdList()){
                CameraCharacteristics cameraCharacteristics = cameraManager.getCameraCharacteristics(cameraId);
                if(cameraCharacteristics.get(CameraCharacteristics.LENS_FACING) ==
                        CameraCharacteristics.LENS_FACING_FRONT){

                    continue;
                }
                StreamConfigurationMap map = cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                int deviceOrientation = getWindowManager().getDefaultDisplay().getRotation();
                mTotalRotation = sensorToDeviceRotation(cameraCharacteristics, deviceOrientation);
                boolean swapRotation = mTotalRotation == 90 || mTotalRotation == 270;
                int rotatedWidth = width;
                int rotatedHeight = height;
                if(swapRotation) {
                    rotatedWidth = height;
                    rotatedHeight = width;
                }
                mPreviewSize = chooseOptimalSize(map.getOutputSizes(SurfaceTexture.class), rotatedWidth, rotatedHeight);
                Log.d(TAG,"mPreviewSize="+mPreviewSize);
                //mPreviewSize = new Size(1280,720);
                mVideoSize = chooseOptimalSize(map.getOutputSizes(MediaRecorder.class), rotatedWidth, rotatedHeight);
                //mVideoSize = new Size(1280,720);
                Log.d(TAG,"mVideoSize="+mVideoSize);
                mImageSize = chooseOptimalSize(map.getOutputSizes(ImageFormat.JPEG), rotatedWidth, rotatedHeight);
                //mImageSize = new Size(1280,720);
                Log.d(TAG,"mImageSize="+mImageSize);
                mImageReader = ImageReader.newInstance(mImageSize.getWidth(), mImageSize.getHeight(), ImageFormat.JPEG, 1);
                mImageReader.setOnImageAvailableListener(mOnImageAvailableListener, mBackgroundHandler);
                mCameraId = cameraId;

                return;
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }
    private void setupCamera2(int width, int height) {
        CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            for(String cameraId : cameraManager.getCameraIdList()){
                CameraCharacteristics cameraCharacteristics = cameraManager.getCameraCharacteristics(cameraId);
                if(cameraCharacteristics.get(CameraCharacteristics.LENS_FACING) ==
                        CameraCharacteristics.LENS_FACING_BACK){
                    continue;
                }
                StreamConfigurationMap map = cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                int deviceOrientation = getWindowManager().getDefaultDisplay().getRotation();
                mTotalRotation2 = sensorToDeviceRotation(cameraCharacteristics, deviceOrientation);
                boolean swapRotation = mTotalRotation2 == 90 || mTotalRotation2 == 270;
                int rotatedWidth = width;
                int rotatedHeight = height;
                if(swapRotation) {
                    rotatedWidth = height;
                    rotatedHeight = width;
                }
                mPreviewSize2 = chooseOptimalSize(map.getOutputSizes(SurfaceTexture.class), rotatedWidth, rotatedHeight);
                Log.d(TAG,"mPreviewSize2="+mPreviewSize2);
                //mPreviewSize = new Size(1280,720);
                mVideoSize2 = chooseOptimalSize(map.getOutputSizes(MediaRecorder.class), rotatedWidth, rotatedHeight);
                //mVideoSize = new Size(1280,720);
                Log.d(TAG,"mVideoSize2="+mVideoSize2);
                mImageSize2 = chooseOptimalSize(map.getOutputSizes(ImageFormat.JPEG), rotatedWidth, rotatedHeight);
                //mImageSize = new Size(1280,720);
                Log.d(TAG,"mImageSize2="+mImageSize2);
                mImageReader2 = ImageReader.newInstance(mImageSize2.getWidth(), mImageSize2.getHeight(), ImageFormat.JPEG, 1);
                mImageReader2.setOnImageAvailableListener(mOnImageAvailableListener2, mBackgroundHandler);
                mCameraId2 = cameraId;
                return;
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void connectCamera() {
        CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) ==
                        PackageManager.PERMISSION_GRANTED) {
                    cameraManager.openCamera(mCameraId, mCameraDeviceStateCallback, mBackgroundHandler);
                    //cameraManager.openCamera(mCameraId2, mCameraDeviceStateCallback2, mBackgroundHandler);
                } else {
                    if(shouldShowRequestPermissionRationale(android.Manifest.permission.CAMERA)) {
                        Toast.makeText(this,
                                "Video app required access to camera", Toast.LENGTH_SHORT).show();
                    }
                    requestPermissions(new String[] {android.Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO
                    }, REQUEST_CAMERA_PERMISSION_RESULT);
                }

            } else {
                cameraManager.openCamera(mCameraId, mCameraDeviceStateCallback, mBackgroundHandler);
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }
    private void connectCamera2() {
        CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) ==
                        PackageManager.PERMISSION_GRANTED) {
                    //cameraManager.openCamera(mCameraId, mCameraDeviceStateCallback, mBackgroundHandler);
                    cameraManager.openCamera(mCameraId2, mCameraDeviceStateCallback2, mBackgroundHandler);
                } else {
                    if(shouldShowRequestPermissionRationale(android.Manifest.permission.CAMERA)) {
                        Toast.makeText(this,
                                "Video app required access to camera", Toast.LENGTH_SHORT).show();
                    }
                    requestPermissions(new String[] {android.Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO
                    }, REQUEST_CAMERA_PERMISSION_RESULT);
                }

            } else {
                cameraManager.openCamera(mCameraId2, mCameraDeviceStateCallback, mBackgroundHandler);
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }


    private void startRecord() {

        try {
            if(mIsRecording) {
                setupMediaRecorder();
            } else if(mIsTimelapse) {
                setupTimelapse();
            }
            SurfaceTexture surfaceTexture = mTextureView.getSurfaceTexture();
            surfaceTexture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
            Surface previewSurface = new Surface(surfaceTexture);
            Surface recordSurface = mMediaRecorder.getSurface();
            mCaptureRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_RECORD);
            mCaptureRequestBuilder.addTarget(previewSurface);
            mCaptureRequestBuilder.addTarget(recordSurface);

            mCameraDevice.createCaptureSession(Arrays.asList(previewSurface, recordSurface, mImageReader.getSurface()),
                    new CameraCaptureSession.StateCallback() {
                        @Override
                        public void onConfigured(CameraCaptureSession session) {
                            mRecordCaptureSession = session;
                            try {
                                mRecordCaptureSession.setRepeatingRequest(
                                        mCaptureRequestBuilder.build(), null, null
                                );
                            } catch (CameraAccessException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onConfigureFailed(CameraCaptureSession session) {
                            Log.d(TAG, "onConfigureFailed: startRecord");
                        }
                    }, null);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void startRecord2() {

        try {
            if(mIsRecording2) {
                setupMediaRecorder2();
            } else if(mIsTimelapse2) {
                setupTimelapse2();
            }
            SurfaceTexture surfaceTexture = mTextureView2.getSurfaceTexture();
            surfaceTexture.setDefaultBufferSize(mPreviewSize2.getWidth(), mPreviewSize2.getHeight());
            Surface previewSurface = new Surface(surfaceTexture);
            Surface recordSurface = mMediaRecorder2.getSurface();
            mCaptureRequestBuilder2 = mCameraDevice2.createCaptureRequest(CameraDevice.TEMPLATE_RECORD);
            mCaptureRequestBuilder2.addTarget(previewSurface);
            mCaptureRequestBuilder2.addTarget(recordSurface);

            mCameraDevice2.createCaptureSession(Arrays.asList(previewSurface, recordSurface, mImageReader2.getSurface()),
                    new CameraCaptureSession.StateCallback() {
                        @Override
                        public void onConfigured(CameraCaptureSession session) {
                            mRecordCaptureSession2 = session;
                            try {
                                mRecordCaptureSession2.setRepeatingRequest(
                                        mCaptureRequestBuilder2.build(), null, null
                                );
                            } catch (CameraAccessException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onConfigureFailed(CameraCaptureSession session) {
                            Log.d(TAG, "onConfigureFailed: startRecord");
                        }
                    }, null);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startPreview() {
        SurfaceTexture surfaceTexture = mTextureView.getSurfaceTexture();
        surfaceTexture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
        Surface previewSurface = new Surface(surfaceTexture);

        try {
            mCaptureRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            mCaptureRequestBuilder.addTarget(previewSurface);

            mCameraDevice.createCaptureSession(Arrays.asList(previewSurface, mImageReader.getSurface()),
                    new CameraCaptureSession.StateCallback() {
                        @Override
                        public void onConfigured(CameraCaptureSession session) {
                            Log.d(TAG, "onConfigured: startPreview");
                            mPreviewCaptureSession = session;
                            try {
                                mPreviewCaptureSession.setRepeatingRequest(mCaptureRequestBuilder.build(),
                                        new CameraCaptureSession.CaptureCallback() {
                                            @Override
                                            public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
                                                Log.d(TAG,"onCaptureCompleted 1111111");
                                            }
                                        }, mBackgroundHandler);
                                mPreviewCaptureSession.setRepeatingRequest(mCaptureRequestBuilder.build(),
                                        new CameraCaptureSession.CaptureCallback() {
                                            @Override
                                            public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
                                                Log.d(TAG,"onCaptureCompleted 2222222");
                                            }
                                        }, mBackgroundHandler);

                            } catch (CameraAccessException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onConfigureFailed(CameraCaptureSession session) {
                            Log.d(TAG, "onConfigureFailed: startPreview");

                        }
                    }, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }
    private void startPreview2() {
        SurfaceTexture surfaceTexture = mTextureView2.getSurfaceTexture();
        surfaceTexture.setDefaultBufferSize(mPreviewSize2.getWidth(), mPreviewSize2.getHeight());
        Surface previewSurface = new Surface(surfaceTexture);

        try {
            mCaptureRequestBuilder2 = mCameraDevice2.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            mCaptureRequestBuilder2.addTarget(previewSurface);

            mCameraDevice2.createCaptureSession(Arrays.asList(previewSurface, mImageReader2.getSurface()),
                    new CameraCaptureSession.StateCallback() {
                        @Override
                        public void onConfigured(CameraCaptureSession session) {
                            Log.d(TAG, "onConfigured: startPreview");
                            mPreviewCaptureSession2 = session;
                            try {
                                mLastTimestamp = 0;
                                mPreviewCaptureSession2.setRepeatingRequest(mCaptureRequestBuilder2.build(),
                                //mPreviewCaptureSession2.setRepeatingBurst(Arrays.asList(mCaptureRequestBuilder2.build()),
                                    new CameraCaptureSession.CaptureCallback(){
                                        @Override
                                        public void onCaptureCompleted(CameraCaptureSession session,CaptureRequest request,TotalCaptureResult result){
//                                            long internalTime = 0;
//                                            if(mLastTimestamp ==0)
//                                                mLastTimestamp = timestamp;
//                                            else {
//                                                internalTime = timestamp - mLastTimestamp;
//                                                mLastTimestamp = timestamp;
//                                            }
                                            //Log.d(TAG,"CaptureCallback onCaptureCompleted 1111111111");
                                        }
                                        @Override
                                        public void onCaptureStarted(CameraCaptureSession session,CaptureRequest request,long timestamp,long frameNumber){
//                                                long internalTime = 0;
//                                                if(mLastTimestamp ==0)
//                                                    mLastTimestamp = timestamp;
//                                                else {
//                                                    internalTime = timestamp - mLastTimestamp;
//                                                    mLastTimestamp = timestamp;
//                                                }
                                            //Log.d(TAG,"CaptureCallback onCaptureStarted 1111111111");
                                        }

                                    }, mBackgroundHandler);
                                //mPreviewCaptureSession2.setRepeatingRequest(mCaptureRequestBuilder2.build(),
                                mPreviewCaptureSession2.setRepeatingBurst(Arrays.asList(mCaptureRequestBuilder2.build()),
                                        new CameraCaptureSession.CaptureCallback(){
                                            @Override
                                            public void onCaptureStarted(CameraCaptureSession session,CaptureRequest request,long timestamp,long frameNumber){
//                                                long internalTime = 0;
//                                                if(mLastTimestamp ==0)
//                                                    mLastTimestamp = timestamp;
//                                                else {
//                                                    internalTime = timestamp - mLastTimestamp;
//                                                    mLastTimestamp = timestamp;
//                                                }
                                                //Log.d(TAG,"CaptureCallback onCaptureStarted 222222222");
                                            }
                                            @Override
                                            public void onCaptureCompleted(CameraCaptureSession session,CaptureRequest request,TotalCaptureResult result){
//                                            long internalTime = 0;
//                                            if(mLastTimestamp ==0)
//                                                mLastTimestamp = timestamp;
//                                            else {
//                                                internalTime = timestamp - mLastTimestamp;
//                                                mLastTimestamp = timestamp;
//                                            }
                                                //Log.d(TAG,"CaptureCallback onCaptureCompleted 222222222");
                                            }
                                        }, mBackgroundHandler);
                            } catch (CameraAccessException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onConfigureFailed(CameraCaptureSession session) {
                            Log.d(TAG, "onConfigureFailed: startPreview");

                        }
                    }, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void startStillCaptureRequest() {
        try {
            if(mIsRecording) {
                mCaptureRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_VIDEO_SNAPSHOT);
            } else {
                mCaptureRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            }
            mCaptureRequestBuilder.addTarget(mImageReader.getSurface());
            mCaptureRequestBuilder.set(CaptureRequest.JPEG_ORIENTATION, mTotalRotation);

            CameraCaptureSession.CaptureCallback stillCaptureCallback = new
                    CameraCaptureSession.CaptureCallback() {
                        @Override
                        public void onCaptureStarted(CameraCaptureSession session, CaptureRequest request, long timestamp, long frameNumber) {
                            super.onCaptureStarted(session, request, timestamp, frameNumber);

                            try {
                                createImageFileName();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    };

            if(mIsRecording) {
                mRecordCaptureSession.capture(mCaptureRequestBuilder.build(), stillCaptureCallback, null);
            } else {
                mPreviewCaptureSession.capture(mCaptureRequestBuilder.build(), stillCaptureCallback, null);
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }
    private void startStillCaptureRequest2() {
        try {
            if(mIsRecording2) {
                mCaptureRequestBuilder2 = mCameraDevice2.createCaptureRequest(CameraDevice.TEMPLATE_VIDEO_SNAPSHOT);
            } else {
                mCaptureRequestBuilder2 = mCameraDevice2.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            }
            mCaptureRequestBuilder2.addTarget(mImageReader2.getSurface());
            mCaptureRequestBuilder2.set(CaptureRequest.JPEG_ORIENTATION, mTotalRotation2);

            CameraCaptureSession.CaptureCallback stillCaptureCallback = new
                    CameraCaptureSession.CaptureCallback() {
                        @Override
                        public void onCaptureStarted(CameraCaptureSession session, CaptureRequest request, long timestamp, long frameNumber) {
                            super.onCaptureStarted(session, request, timestamp, frameNumber);

                            try {
                                createImageFileName();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    };

            if(mIsRecording2) {
                mRecordCaptureSession2.capture(mCaptureRequestBuilder2.build(), stillCaptureCallback, null);
            } else {
                mPreviewCaptureSession2.capture(mCaptureRequestBuilder2.build(), stillCaptureCallback, null);
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void closeCamera() {
        if(mIsRecording || mIsTimelapse) {
            mChronometer.stop();
            mChronometer.setVisibility(View.INVISIBLE);
            mIsRecording = false;
            mIsTimelapse = false;
            mRecordImageButton.setImageResource(R.mipmap.btn_video_online);

            mMediaRecorder.stop();
            mMediaRecorder.reset();

            Intent mediaStoreUpdateIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            mediaStoreUpdateIntent.setData(Uri.fromFile(new File(mVideoFileName)));
            sendBroadcast(mediaStoreUpdateIntent);
        }
        if(mCameraDevice != null) {
            mCameraDevice.close();
            mCameraDevice = null;
        }
        if(mMediaRecorder != null) {
            mMediaRecorder.release();
            mMediaRecorder = null;
        }
    }
    private void closeCamera2() {
        if(mIsRecording2 || mIsTimelapse2) {
            mChronometer.stop();
            mChronometer.setVisibility(View.INVISIBLE);
            mIsRecording = false;
            mIsTimelapse = false;
            mRecordImageButton.setImageResource(R.mipmap.btn_video_online);

            mMediaRecorder.stop();
            mMediaRecorder.reset();

            Intent mediaStoreUpdateIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            mediaStoreUpdateIntent.setData(Uri.fromFile(new File(mVideoFileName)));
            sendBroadcast(mediaStoreUpdateIntent);
        }
        if(mCameraDevice2 != null) {
            mCameraDevice2.close();
            mCameraDevice2 = null;
        }
        if(mMediaRecorder2 != null) {
            mMediaRecorder2.release();
            mMediaRecorder2 = null;
        }
    }
    private void startBackgroundThread() {
        mBackgroundHandlerThread = new HandlerThread("Camera2VideoImage");
        mBackgroundHandlerThread.start();
        mBackgroundHandler = new Handler(mBackgroundHandlerThread.getLooper());
//        mBackgroundHandler = new Handler();
    }

    private void stopBackgroundThread() {
        mBackgroundHandlerThread.quitSafely();
        try {
            mBackgroundHandlerThread.join();
            mBackgroundHandlerThread = null;
            mBackgroundHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static int sensorToDeviceRotation(CameraCharacteristics cameraCharacteristics, int deviceOrientation) {
        int sensorOrienatation = cameraCharacteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
        deviceOrientation = ORIENTATIONS.get(deviceOrientation);
        int result = (sensorOrienatation + deviceOrientation + 360) % 360;
        Log.d(TAG,"deviceOrientation="+deviceOrientation+",sensorOrienatation="+sensorOrienatation+",result="+result);
        return result;
    }

    private static Size chooseOptimalSize(Size[] choices, int width, int height) {
        List<Size> bigEnough = new ArrayList<Size>();
        for(Size option : choices) {
            if(option.getHeight() == option.getWidth() * height / width &&
                    option.getWidth() >= width && option.getHeight() >= height) {
                bigEnough.add(option);
            }
        }
        if(bigEnough.size() > 0) {
            return Collections.min(bigEnough, new CompareSizeByArea());
        } else {
            return choices[0];
        }
    }

    private void createVideoFolder() {
        File movieFile = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
        mVideoFolder = new File(movieFile, "camera2VideoImage");
        if(!mVideoFolder.exists()) {
            mVideoFolder.mkdirs();
        }
    }

    private File createVideoFileName() throws IOException {
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String prepend = "VIDEO_" + timestamp + "_";
        File videoFile = File.createTempFile(prepend, ".mp4", mVideoFolder);
        mVideoFileName = videoFile.getAbsolutePath();
        return videoFile;
    }

    private void createImageFolder() {
        File imageFile = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
        mImageFolder = new File(imageFile, "camera2VideoImage");
        if(!mImageFolder.exists()) {
            mImageFolder.mkdirs();
        }
    }

    private File createImageFileName() throws IOException {
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String prepend = "IMAGE_" + timestamp + "_";
        File imageFile = File.createTempFile(prepend, ".jpg", mImageFolder);
        mImageFileName = imageFile.getAbsolutePath();
        return imageFile;
    }

    private void checkWriteStoragePermission() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                try {
                    createVideoFileName();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if(mIsTimelapse || mIsRecording) {
                    startRecord();
                    mMediaRecorder.start();
                    mChronometer.setBase(SystemClock.elapsedRealtime());
                    mChronometer.setVisibility(View.VISIBLE);
                    mChronometer.start();
                }
            } else {
                if(shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    Toast.makeText(this, "app needs to be able to save videos", Toast.LENGTH_SHORT).show();
                }
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_EXTERNAL_STORAGE_PERMISSION_RESULT);
            }
        } else {
            try {
                createVideoFileName();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if(mIsRecording || mIsTimelapse) {
                startRecord();
                mMediaRecorder.start();
                mChronometer.setBase(SystemClock.elapsedRealtime());
                mChronometer.setVisibility(View.VISIBLE);
                mChronometer.start();
            }
        }
    }
    private void checkWriteStoragePermission2() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                try {
                    createVideoFileName();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if(mIsTimelapse2 || mIsRecording2) {
                    startRecord2();
                    mMediaRecorder2.start();
                    mChronometer2.setBase(SystemClock.elapsedRealtime());
                    mChronometer2.setVisibility(View.VISIBLE);
                    mChronometer2.start();
                }
            } else {
                if(shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    Toast.makeText(this, "app needs to be able to save videos", Toast.LENGTH_SHORT).show();
                }
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_EXTERNAL_STORAGE_PERMISSION_RESULT);
            }
        } else {
            try {
                createVideoFileName();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if(mIsRecording2 || mIsTimelapse2) {
                startRecord2();
                mMediaRecorder2.start();
                mChronometer2.setBase(SystemClock.elapsedRealtime());
                mChronometer2.setVisibility(View.VISIBLE);
                mChronometer2.start();
            }
        }
    }

    private void setupMediaRecorder() throws IOException {
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mMediaRecorder.setOutputFile(mVideoFileName);
        mMediaRecorder.setVideoEncodingBitRate(1000000);
        mMediaRecorder.setVideoFrameRate(30);
        mMediaRecorder.setVideoSize(mVideoSize.getWidth(), mVideoSize.getHeight());
        mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        mMediaRecorder.setOrientationHint(mTotalRotation);
        mMediaRecorder.prepare();
    }

    private void setupMediaRecorder2() throws IOException {
        mMediaRecorder2.setVideoSource(MediaRecorder.VideoSource.SURFACE);
        mMediaRecorder2.setAudioSource(MediaRecorder.AudioSource.MIC);
        mMediaRecorder2.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mMediaRecorder2.setOutputFile(mVideoFileName);
        mMediaRecorder2.setVideoEncodingBitRate(1000000);
        mMediaRecorder2.setVideoFrameRate(30);
        mMediaRecorder2.setVideoSize(mVideoSize2.getWidth(), mVideoSize2.getHeight());
        mMediaRecorder2.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        mMediaRecorder2.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        mMediaRecorder2.setOrientationHint(mTotalRotation2);
        mMediaRecorder2.prepare();
    }

    private void setupTimelapse() throws IOException {
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
        mMediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_TIME_LAPSE_HIGH));
        mMediaRecorder.setOutputFile(mVideoFileName);
        mMediaRecorder.setCaptureRate(2);
        mMediaRecorder.setOrientationHint(mTotalRotation);
        mMediaRecorder.prepare();
    }
    private void setupTimelapse2() throws IOException {
        mMediaRecorder2.setVideoSource(MediaRecorder.VideoSource.SURFACE);
        mMediaRecorder2.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_TIME_LAPSE_HIGH));
        mMediaRecorder2.setOutputFile(mVideoFileName);
        mMediaRecorder2.setCaptureRate(2);
        mMediaRecorder2.setOrientationHint(mTotalRotation2);
        mMediaRecorder2.prepare();
    }

    private void lockFocus() {
        Log.d(TAG,"afState= begin");
        mCaptureState = STATE_WAIT_LOCK;
        mCaptureRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CaptureRequest.CONTROL_AF_TRIGGER_START);
        try {
            if(mIsRecording) {
                mRecordCaptureSession.capture(mCaptureRequestBuilder.build(), mRecordCaptureCallback, mBackgroundHandler);
            } else {
                mPreviewCaptureSession.capture(mCaptureRequestBuilder.build(), mPreviewCaptureCallback, mBackgroundHandler);
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }
    private void lockFocus2() {
        Log.d(TAG,"afState= begin");
        mCaptureState2 = STATE_WAIT_LOCK;
        mCaptureRequestBuilder2.set(CaptureRequest.CONTROL_AF_TRIGGER, CaptureRequest.CONTROL_AF_TRIGGER_START);
        try {
            if(mIsRecording2) {
                mRecordCaptureSession2.capture(mCaptureRequestBuilder2.build(), mRecordCaptureCallback2, mBackgroundHandler);
            } else {
                mPreviewCaptureSession2.capture(mCaptureRequestBuilder2.build(), mPreviewCaptureCallback2, mBackgroundHandler);
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }
}
