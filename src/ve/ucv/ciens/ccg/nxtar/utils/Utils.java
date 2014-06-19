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

public abstract class Utils{
	public static String vector2String(Vector3 v){
		return "(" + Float.toString(v.x) + ", " + Float.toString(v.y) + ", " + Float.toString(v.z) + ")";
	}

	public static int getScreenWidth(){
		return (int)(Gdx.graphics.getWidth() * ProjectConstants.OVERSCAN);
	}

	public static int getScreenHeight(){
		return (int)(Gdx.graphics.getHeight() * ProjectConstants.OVERSCAN);
	}

	public static boolean isDeviceRollValid(){
		boolean rollValid = Gdx.input.isPeripheralAvailable(Peripheral.Accelerometer) && Gdx.input.isPeripheralAvailable(Peripheral.Compass);

		// TODO: Check device orientation for limits.

		return rollValid;
	}
}
