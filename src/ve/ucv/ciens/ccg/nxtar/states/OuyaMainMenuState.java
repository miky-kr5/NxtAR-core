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

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.mappings.Ouya;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class OuyaMainMenuState extends MainMenuStateBase{
	private static final String CLASS_NAME = OuyaMainMenuState.class.getSimpleName();

	private OrthographicCamera pixelPerfectCamera;
	private Texture ouyaOButtonTexture;
	private Sprite ouyaOButton;
	private boolean oButtonPressed;

	public OuyaMainMenuState(final NxtARCore core){
		this.core = core;
		this.pixelPerfectCamera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

		startButton.setPosition(-(startButton.getWidth() / 2), -(startButton.getHeight() / 2));
		startButtonBBox.setPosition(startButton.getX(), startButton.getY());

		float ledYPos = (-(Gdx.graphics.getHeight() / 2) * 0.5f) + (startButton.getY() * 0.5f);
		clientConnectedLedOn.setSize(clientConnectedLedOn.getWidth() * 0.5f, clientConnectedLedOn.getHeight() * 0.5f);
		clientConnectedLedOn.setPosition(-(clientConnectedLedOn.getWidth() / 2), ledYPos);

		clientConnectedLedOff.setSize(clientConnectedLedOff.getWidth() * 0.5f, clientConnectedLedOff.getHeight() * 0.5f);
		clientConnectedLedOff.setPosition(-(clientConnectedLedOff.getWidth() / 2), ledYPos);

		ouyaOButtonTexture = new Texture("data/gfx/gui/OUYA_O.png");
		TextureRegion region = new TextureRegion(ouyaOButtonTexture, ouyaOButtonTexture.getWidth(), ouyaOButtonTexture.getHeight());
		ouyaOButton = new Sprite(region);
		ouyaOButton.setSize(ouyaOButton.getWidth() * 0.6f, ouyaOButton.getHeight() * 0.6f);
		ouyaOButton.setPosition(startButton.getX() - ouyaOButton.getWidth() - 20, startButton.getY());

		oButtonPressed = false;
	}

	@Override
	public void render(float delta) {
		Gdx.gl.glClearColor(1, 1, 1, 1);
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);

		core.batch.setProjectionMatrix(pixelPerfectCamera.combined);
		core.batch.begin();{
			core.batch.disableBlending();
			drawBackground(core.batch);
			core.batch.enableBlending();
			if(clientConnected){
				clientConnectedLedOn.draw(core.batch);
			}else{
				clientConnectedLedOff.draw(core.batch);
			}
			startButton.draw(core.batch, 1.0f);
			ouyaOButton.draw(core.batch);
		}core.batch.end();
	}

	@Override
	public void resize(int width, int height) {
		// TODO Auto-generated method stub

	}

	@Override
	public void show() {
		// TODO Auto-generated method stub

	}

	@Override
	public void hide() {
		// TODO Auto-generated method stub

	}

	@Override
	public void pause() {
		// TODO Auto-generated method stub

	}

	@Override
	public void resume() {
		// TODO Auto-generated method stub

	}

	@Override
	public void dispose(){
		super.dispose();
		ouyaOButtonTexture.dispose();
	}

	/*;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
	  ; BEGIN CONTROLLER LISTENER METHODS ;
	  ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;*/

	@Override
	public boolean buttonDown(Controller controller, int buttonCode) {
		if(stateActive){
			if(buttonCode == Ouya.BUTTON_O){
				if(!clientConnected){
					core.toast("Can't start the game. No client is connected.", true);
				}else{
					oButtonPressed = true;
					startButton.setChecked(true);
				}
			}

			return true;

		}else{
			return false;
		}
	}

	@Override
	public boolean buttonUp(Controller controller, int buttonCode) {
		if(stateActive){
			if(buttonCode == Ouya.BUTTON_O){
				if(oButtonPressed){
					oButtonPressed = false;
					startButton.setChecked(false);
					core.nextState = game_states_t.IN_GAME;
					Gdx.app.log(TAG, CLASS_NAME + ".touchDown() :: Start button released.");
				}
			}

			return true;

		}else{
			return false;
		}
	}
}
