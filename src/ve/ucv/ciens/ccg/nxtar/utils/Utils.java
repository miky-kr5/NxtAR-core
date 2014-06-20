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
package ve.ucv.ciens.ccg.nxtar.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Peripheral;
import com.badlogic.gdx.math.Vector3;

/**
 * Assorted common auxiliary functions.
 */
public abstract class Utils{
	private static final float MIN_PITCH   = -80.0f;
	private static final float MAX_PITCH   = 5.0f;
	private static final float MIN_AZIMUTH = -155.0f;
	private static final float MAX_AZIMUTH = -40.0f;

	/**
	 * <p>Converts a libGDX {@link Vector3} to a String representation form easy logging.</p>
	 * 
	 * @param v The vector to convert.
	 * @return A string representation of the form "(v.x, v.y, v.z)".
	 */
	public static String vector2String(Vector3 v){
		return "(" + Float.toString(v.x) + ", " + Float.toString(v.y) + ", " + Float.toString(v.z) + ")";
	}

	/**
	 * @return The width of the screen accounting for screen overscan.
	 */
	public static int getScreenWidth(){
		return (int)(Gdx.graphics.getWidth() * ProjectConstants.OVERSCAN);
	}

	/**
	 * @return The height of the screen accounting for screen overscan.
	 */
	public static int getScreenHeight(){
		return (int)(Gdx.graphics.getHeight() * ProjectConstants.OVERSCAN);
	}

	/**
	 * <p>Checks if the device's orientation is available and wihtin some arbitrary ranges.</p>
	 * 
	 * @return True if the device can detect it's orientation and it's within range. False otherwise.
	 */
	public static boolean isDeviceRollValid(){
		boolean rollValid = Gdx.input.isPeripheralAvailable(Peripheral.Accelerometer) && Gdx.input.isPeripheralAvailable(Peripheral.Compass);
		float azimuth, pitch;

		if(rollValid){
			azimuth = Gdx.input.getAzimuth();
			pitch = Gdx.input.getPitch();

			if(pitch < MIN_PITCH || pitch > MAX_PITCH)
				rollValid = false;

			if(rollValid && (azimuth < MIN_AZIMUTH || azimuth > MAX_AZIMUTH))
				rollValid = false;
		}

		return rollValid;
	}
}
