package com.ifewalter.android.textonmotion;

import java.io.IOException;

import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PreviewCallback;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class CameraPreview360 extends SurfaceView implements SurfaceHolder.Callback {
	SurfaceHolder mHolder;
	Camera mCamera;

	CameraPreview360(Context context) {
		super(context);

		// Install a SurfaceHolder.Callback so we get notified when the
		// underlying surface is created and destroyed.
		mHolder = getHolder();
		mHolder.addCallback(this);
		mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	}

	public void surfaceCreated(SurfaceHolder holder) {
		// The Surface has been created, acquire the camera and tell it where
		// to draw.
		mCamera = Camera.open();
		try {
			mCamera.setPreviewDisplay(holder);

			mCamera.setPreviewCallback(new PreviewCallback() {

				public void onPreviewFrame(byte[] data, Camera arg1) {
					// FileOutputStream outStream = null;
					// try {
					// outStream = new FileOutputStream(String.format(
					// MEDIA_STORAGE_LOCATION,
					// System.currentTimeMillis()));
					// outStream.write(data);
					// outStream.close();
					// } catch (FileNotFoundException e) {
					// e.printStackTrace();
					// } catch (IOException e) {
					// e.printStackTrace();
					// } finally {
					// }
					// Preview.this.invalidate();

				}
			});
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void surfaceDestroyed(SurfaceHolder holder) {
		// Surface will be destroyed when we return, so stop the preview.
		// Because the CameraDevice object is not a shared resource, it's very
		// important to release it when the activity is paused.
		try {
			mCamera.setPreviewCallback(null);
			mCamera.stopPreview();
			mCamera.release();
			// mCamera = null;
		} catch (Exception e) {

		}

	}

	public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
		// Now that the size is known, set up the camera parameters and begin
		// the preview.
		Camera.Parameters parameters = mCamera.getParameters();
		// parameters.setPreviewSize(w, h);
		try {
			parameters = mCamera.getParameters();
		//	parameters.setFocusMode(Parameters.FOCUS_MODE_AUTO);
			parameters.setFlashMode(Parameters.FLASH_MODE_ON);
			// parameters.setPreviewSize(1000, 1000);
			mCamera.setDisplayOrientation(360);
			mCamera.setParameters(parameters);
		} catch (Exception e) {

		} finally {
			try {
				mCamera.startPreview();
			} catch (Exception e) {
				try {
					mCamera.reconnect();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} finally {
					mCamera.startPreview();
				}
			}
		}
	}

}

