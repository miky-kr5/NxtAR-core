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

	public MarkerData findMarkersInFrame(byte[] frame);
	public CalibrationData findCalibrationPattern(byte[] frame);
	public void calibrateCamera(float[][] calibrationSamples, byte[] frame);
	public byte[] undistortFrame(byte[] frame);
	public boolean isCameraCalibrated();
}
