/*
 * Copyright (C) 2014 Miguel Angel Astor Romero
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ve.ucv.ciens.ccg.nxtar.interfaces;

import ve.ucv.ciens.ccg.nxtar.utils.ProjectConstants;

import com.badlogic.gdx.math.Matrix3;
import com.badlogic.gdx.math.Vector3;

public interface ImageProcessor{
	public class MarkerData{
		public byte[] outFrame;
		public int[] markerCodes;
		public Vector3[] translationVectors;
		public Matrix3[] rotationMatrices;
	}

	public class CalibrationData{
		public byte[] outFrame;
		public float[] calibrationPoints;
	}

	/**
	 * <p>Finds up to {@link ProjectConstants.MAXIMUM_NUMBER_OF_MARKERS} markers in the input
	 * image and returns their codes and pose estimation in the CVMarkerData structure. The
	 * markers are higlihted in the input image.</p>
	 * 
	 * @param frame The JPEG encoded input image.
	 * @return A data structure containing the processed output image, the
	 * detected marker codes and their respective locations.
	 */
	public MarkerData findMarkersInFrame(byte[] frame);

	/**
	 * <p>Attempts to detect a checkerboard calibration pattern in the input image.
	 * If the pattenr is found the method returns an image with the pattern
	 * highlighted and the spatial location of the calibration points in the 
	 * output data structure.</p>
	 * 
	 * @param frame The JPEG encoded input image.
	 * @return A data structure containing the processed output image and the
	 * location of the calibration points. If the pattern was not found, the returnd
	 * calibration points array is null.
	 */
	public CalibrationData findCalibrationPattern(byte[] frame);

	/** 
	 * <p>Obtains the intrinsic camera parameters necesary for calibration.</p>
	 */
	public void calibrateCamera(float[][] calibrationSamples, byte[] frame);

	/**
	 * <p>Removes camera lens distortion from the input image using the
	 * camera parameters obtained by the calibrateCamera method.</p>
	 * 
	 * @return A JPEG encoded image that is the input image after distortion correction. If the
	 * camera has not been calibrated or OpenCV failed to load returns null.
	 */
	public byte[] undistortFrame(byte[] frame);

	/**
	 * <p>Indicates if OpenCV has been sucessfully initialized and used
	 * to obtain the camera parameters for calibration.</p>
	 * 
	 * @return True if and only if OpenCV initialized succesfully and calibrateCamera has been called previously.
	 */
	public boolean isCameraCalibrated();

	public float getFocalPointX();
	public float getFocalPointY();
	public float getCameraCenterX();
	public float getCameraCenterY();
}
