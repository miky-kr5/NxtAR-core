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
package ve.ucv.ciens.ccg.nxtar.states;

import ve.ucv.ciens.ccg.nxtar.utils.ProjectConstants;

import com.badlogic.gdx.controllers.Controller;

public class CameraCalibrationState extends BaseState{
	private float[][] calibrationSamples;

	public CameraCalibrationState(){
		calibrationSamples = new float[ProjectConstants.CALIBRATION_SAMPLES][];
		for(int i = 0; i < calibrationSamples.length; i++){
			calibrationSamples[i] = new float[ProjectConstants.CALIBRATION_PATTERN_POINTS * 2];
		}
	}

	@Override
	public void onStateSet(){
		for(int i = 0; i < calibrationSamples.length; i++){
			for(int j = 0; j < calibrationSamples[i].length; j++){
				calibrationSamples[i][j] = 0.0f;
			}
		}
	}

	@Override
	public void onStateUnset(){ }

	@Override
	public void render(float delta){ }

	@Override
	public void resize(int width, int height){ }

	@Override
	public void show(){ }

	@Override
	public void hide(){ }

	@Override
	public void pause(){ }

	@Override
	public void resume(){ }

	@Override
	public void dispose(){ }

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button){
		return false;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button){
		return false;
	}

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer){
		return false;
	}

	@Override
	public boolean buttonDown(Controller controller, int buttonCode){
		return false;
	}

	@Override
	public boolean buttonUp(Controller controller, int buttonCode){
		return false;
	}

	@Override
	public boolean axisMoved(Controller controller, int axisCode, float value){
		return false;
	}
}
