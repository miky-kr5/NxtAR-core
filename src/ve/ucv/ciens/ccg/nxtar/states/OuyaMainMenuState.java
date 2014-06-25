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

import ve.ucv.ciens.ccg.nxtar.NxtARCore;
import ve.ucv.ciens.ccg.nxtar.NxtARCore.game_states_t;
import ve.ucv.ciens.ccg.nxtar.utils.Utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.mappings.Ouya;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class OuyaMainMenuState extends MainMenuStateBase{
	private static final String CLASS_NAME = OuyaMainMenuState.class.getSimpleName();

	private Texture ouyaOButtonTexture;
	private Sprite ouyaOButton;
	private boolean oButtonPressed;
	private int oButtonSelection;
	private float ledYPos;

	public OuyaMainMenuState(final NxtARCore core) throws IllegalArgumentException{
		super();

		if(core == null)
			throw new IllegalArgumentException(CLASS_NAME + ": Core is null.");

		this.core = core;

		// Set buttons.
		startButton.setPosition(-(startButton.getWidth() / 2), -(startButton.getHeight() / 2));
		startButtonBBox.setPosition(startButton.getX(), startButton.getY());
		calibrationButton.setPosition(-(calibrationButton.getWidth() / 2), (startButton.getY() + startButton.getHeight()) + 10);
		calibrationButtonBBox.setPosition(calibrationButton.getX(), calibrationButton.getY());
		autoButton.setPosition(-(autoButton.getWidth() / 2), (startButton.getY() - startButton.getHeight()) - 10);
		autoButtonBBox.setPosition(autoButton.getX(), autoButton.getY());

		//Set leds.
		ledYPos = -(Utils.getScreenHeightWithOverscan() / 2) + 10;
		cameraCalibratedLedOn.setSize(cameraCalibratedLedOn.getWidth() * 0.5f, cameraCalibratedLedOn.getHeight() * 0.5f);
		cameraCalibratedLedOn.setPosition(-cameraCalibratedLedOn.getWidth() - 5, ledYPos);
		cameraCalibratedLedOff.setSize(cameraCalibratedLedOff.getWidth() * 0.5f, cameraCalibratedLedOff.getHeight() * 0.5f);
		cameraCalibratedLedOff.setPosition(-cameraCalibratedLedOff.getWidth() - 5, ledYPos);
		assetsLoadedLedOn.setSize(assetsLoadedLedOn.getWidth() * 0.5f, assetsLoadedLedOn.getHeight() * 0.5f);
		assetsLoadedLedOn.setPosition(5, ledYPos);
		assetsLoadedLedOff.setSize(assetsLoadedLedOff.getWidth() * 0.5f, assetsLoadedLedOff.getHeight() * 0.5f);
		assetsLoadedLedOff.setPosition(5, ledYPos);

		// Set OUYA's O button.
		ouyaOButtonTexture = new Texture("data/gfx/gui/OUYA_O.png");
		TextureRegion region = new TextureRegion(ouyaOButtonTexture, ouyaOButtonTexture.getWidth(), ouyaOButtonTexture.getHeight());
		ouyaOButton = new Sprite(region);
		ouyaOButton.setSize(ouyaOButton.getWidth() * 0.6f, ouyaOButton.getHeight() * 0.6f);
		oButtonSelection = 0;
		oButtonPressed = false;
	}

	@Override
	public void render(float delta) {
		Gdx.gl.glClearColor(1, 1, 1, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		core.batch.setProjectionMatrix(pixelPerfectCamera.combined);
		core.batch.begin();{
			// Render background.
			core.batch.disableBlending();
			drawBackground(core.batch);
			core.batch.enableBlending();

			// Render leds.
			if(cameraCalibrated) cameraCalibratedLedOn.draw(core.batch);
			else cameraCalibratedLedOff.draw(core.batch);
			if(assetsLoaded) assetsLoadedLedOn.draw(core.batch);
			else assetsLoadedLedOff.draw(core.batch);

			// Render buttons.
			startButton.draw(core.batch, 1.0f);
			calibrationButton.draw(core.batch, 1.0f);
			autoButton.draw(core.batch, 1.0f);

			// Render O button.
			if(oButtonSelection == 0){
				ouyaOButton.setPosition(startButton.getX() - ouyaOButton.getWidth() - 20, startButton.getY() + (ouyaOButton.getHeight() / 2));
			}else if(oButtonSelection == 1){
				ouyaOButton.setPosition(calibrationButton.getX() - ouyaOButton.getWidth() - 20, calibrationButton.getY() + (ouyaOButton.getHeight() / 2));
			}else if(oButtonSelection == 2){
				ouyaOButton.setPosition(autoButton.getX() - ouyaOButton.getWidth() - 20, autoButton.getY() + (ouyaOButton.getHeight() / 2));
			}
			ouyaOButton.draw(core.batch);

		}core.batch.end();
	}

	@Override
	public void dispose(){
		super.dispose();
		ouyaOButtonTexture.dispose();
	}

	/*;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
	  ; CONTROLLER LISTENER METHODS ;
	  ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;*/

	@Override
	public boolean buttonDown(Controller controller, int buttonCode){
		if(stateActive){
			if(buttonCode == Ouya.BUTTON_O){
				Gdx.app.log(TAG, CLASS_NAME + ".buttonDown(): O button pressed.");

				if(oButtonSelection == 0){
					if(!clientConnected){
						core.toast("Can't start the game. No client is connected.", true);
					}else if(!core.cvProc.isCameraCalibrated()){
						core.toast("Can't start the game. Camera is not calibrated.", true);
					}else{
						oButtonPressed = true;
						startButton.setChecked(true);
					}
				}else if(oButtonSelection == 1){
					if(!clientConnected){
						core.toast("Can't calibrate the camera. No client is connected.", true);
					}else{
						oButtonPressed = true;
						calibrationButton.setChecked(true);
					}
				}else if(oButtonSelection == 2){
					if(!clientConnected){
						core.toast("Can't launch automatic action. No client is connected.", true);
					}else{
						oButtonPressed = true;
						autoButton.setChecked(true);
					}
				}
			}else if(buttonCode == Ouya.BUTTON_DPAD_UP){
				Gdx.app.log(TAG, CLASS_NAME + ".buttonDown(): Dpad up button pressed.");
				oButtonSelection = oButtonSelection - 1 < 0 ? NUM_MENU_BUTTONS - 1 : oButtonSelection - 1;
			}else if(buttonCode == Ouya.BUTTON_DPAD_DOWN){
				Gdx.app.log(TAG, CLASS_NAME + ".buttonDown(): Dpad down button pressed.");
				oButtonSelection = (oButtonSelection + 1) % NUM_MENU_BUTTONS;
			}

			return true;
		}else{
			return false;
		}
	}

	@Override
	public boolean buttonUp(Controller controller, int buttonCode){
		if(stateActive){
			if(buttonCode == Ouya.BUTTON_O){
				Gdx.app.log(TAG, CLASS_NAME + ".buttonDown(): O button released.");

				if(oButtonPressed){
					oButtonPressed = false;

					if(oButtonSelection == 0){
						startButton.setChecked(false);
						core.nextState = game_states_t.IN_GAME;
					}else if(oButtonSelection == 1){
						calibrationButton.setChecked(false);
						core.nextState = game_states_t.CALIBRATION;
					}else if(oButtonSelection == 2){
						autoButton.setChecked(false);
						core.nextState = game_states_t.AUTOMATIC_ACTION;
					}
				}
			}

			return true;
		}else{
			return false;
		}
	}
}
