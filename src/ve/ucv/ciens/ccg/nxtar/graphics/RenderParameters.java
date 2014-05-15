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
package ve.ucv.ciens.ccg.nxtar.graphics;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;

public abstract class RenderParameters {
	private static Matrix4 modelViewProjection;
	private static Matrix4 geometricTransformation;
	private static Vector3 eyePosition;
	private static LightSource lightSource1;
	private static LightSource lightSource2;

	static{
		modelViewProjection = new Matrix4();
		geometricTransformation = new Matrix4();
		eyePosition = new Vector3(0.0f, 0.0f, 1.4142f);
		lightSource1 = new LightSource();
		lightSource2 = new LightSource();
	}

	public static synchronized void setModelViewProjectionMatrix(Matrix4 modelViewMatrix){
		modelViewProjection.set(modelViewMatrix);
	}

	public static synchronized void setTransformationMatrix(Matrix4 transformationMatrix){
		geometricTransformation.set(transformationMatrix);
	}

	public static synchronized void setEyePosition(Vector3 newEyePostition){
		eyePosition.set(newEyePostition);
	}

	public static synchronized void setLightSource1(LightSource newLightSource1){
		lightSource1.set(newLightSource1);
	}

	public static synchronized void setLightSource2(LightSource newLightSource2){
		lightSource2.set(newLightSource2);
	}

	public static synchronized Matrix4 getModelViewProjectionMatrix(){
		return modelViewProjection;
	}

	public static synchronized Matrix4 getTransformationMatrix(){
		return geometricTransformation;
	}

	public static synchronized Vector3 getEyePosition(){
		return eyePosition;
	}

	public static synchronized LightSource getLightSource1(){
		return lightSource1;
	}

	public static synchronized LightSource getLightSource2(){
		return lightSource2;
	}
}
