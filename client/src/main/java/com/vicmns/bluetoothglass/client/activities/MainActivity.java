package com.vicmns.bluetoothglass.client.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.FileObserver;
import android.provider.MediaStore;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.google.android.glass.media.CameraManager;
import com.google.android.glass.touchpad.Gesture;
import com.google.android.glass.touchpad.GestureDetector;
import com.vicmns.bluetoothglass.client.views.CameraView;

import java.io.File;


public class MainActivity extends Activity {
    private static final int TAKE_PICTURE_REQUEST = 1;
    private static final int TAKE_VIDEO_REQUEST = 2;
    public static final String SHARE_PICTURE = "picture";
    private GestureDetector mGestureDetector = null;
    private CameraView cameraView;

    private FileObserver observer;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // Initiate CameraView
        cameraView = new CameraView(this);

        // Turn on Gestures
        mGestureDetector = createGestureDetector(this);

        setContentView(cameraView);
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (cameraView != null) {
            cameraView.releaseCamera();
        }

        //setContentView(cameraView);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (cameraView != null) {
            cameraView.releaseCamera();
        }
    }

    private GestureDetector createGestureDetector(Context context) {
        GestureDetector gestureDetector = new GestureDetector(context);

        gestureDetector.setBaseListener(new GestureDetector.BaseListener() {
            @Override
            public boolean onGesture(Gesture gesture) {
                // Make sure view is initiated
                if (cameraView != null) {
                    // Tap with a single finger for photo
                    if (gesture == Gesture.TAP) {
                        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        if (intent != null) {
                            startActivityForResult(intent, TAKE_PICTURE_REQUEST);
                        }

                        return true;
                    }

                    // Tap with 2 fingers for video
                    /*else if (gesture == Gesture.TWO_TAP) {
                        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                        if (intent != null) {
                            startActivityForResult(intent, TAKE_VIDEO_REQUEST);
                        }

                        return true;
                    }*/
                }

                return false;
            }
        });

        return gestureDetector;
    }

    /*
	 * Send generic motion events to the gesture detector
	 */
    @Override
    public boolean onGenericMotionEvent(MotionEvent event)
    {
        if (mGestureDetector != null)
        {
            return mGestureDetector.onMotionEvent(event);
        }

        return false;
    }

    /*
     * (non-Javadoc)
     * @see android.app.Activity#onActivityResult(int, int, android.content.Intent)
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        // Handle photos
        if (requestCode == TAKE_PICTURE_REQUEST && resultCode == RESULT_OK)
        {
            String picturePath = data.getStringExtra(CameraManager.EXTRA_PICTURE_FILE_PATH);
            processPictureWhenReady(picturePath);
        }

        // Handle videos
        if (requestCode == TAKE_VIDEO_REQUEST && resultCode == RESULT_OK)
        {
            String picturePath = data.getStringExtra(CameraManager.EXTRA_VIDEO_FILE_PATH);
            processPictureWhenReady(picturePath);
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private void processPictureWhenReady(final String picturePath)
    {
        final File pictureFile = new File(picturePath);

        if (pictureFile.exists())
        {
            Intent shareIntent = new Intent(this, BluetoothClient.class);
            shareIntent.putExtra(SHARE_PICTURE, picturePath);
            startActivity(shareIntent);
            finish();
        }
        else
        {
            // The file does not exist yet. Before starting the file observer, you
            // can update your UI to let the user know that the application is
            // waiting for the picture (for example, by displaying the thumbnail
            // image and a progress indicator).
            ViewGroup vg = (ViewGroup)(cameraView.getParent());
            vg.removeAllViews();
            RelativeLayout layout = new RelativeLayout(this);
            ProgressBar progressBar = new ProgressBar(this,null,android.R.attr.progressBarStyleLarge);
            progressBar.setIndeterminate(true);
            progressBar.setVisibility(View.VISIBLE);
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT
                    , ViewGroup.LayoutParams.MATCH_PARENT);

            params.addRule(RelativeLayout.CENTER_IN_PARENT);
            layout.addView(progressBar,params);

            setContentView(layout);

            final File parentDirectory = pictureFile.getParentFile();

            observer = new FileObserver(parentDirectory.getPath()) {
                // Protect against additional pending events after CLOSE_WRITE is
                // handled.
                private boolean isFileWritten;

                @Override
                public void onEvent(int event, String path) {
                    if (!isFileWritten) {
                        // For safety, make sure that the file that was created in
                        // the directory is actually the one that we're expecting.
                        File affectedFile = new File(parentDirectory, path);

                        isFileWritten = (event == FileObserver.CLOSE_WRITE
                                && affectedFile.equals(pictureFile));

                        if (isFileWritten) {
                            stopWatching();

                            // Now that the file is ready, recursively call
                            // processPictureWhenReady again (on the UI thread).
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    processPictureWhenReady(picturePath);
                                }
                            });
                        }
                    }
                }
            };

            observer.startWatching();
        }
    }

}
