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
import com.badlogic.gdx.controllers.PovDirection;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

public class TabletMainMenuState extends MainMenuStateBase{
	protected static final String CLASS_NAME = TabletMainMenuState.class.getSimpleName();

	private OrthographicCamera pixelPerfectCamera;

	// Button touch helper fields.
	private Vector3 win2world;
	private Vector2 touchPointWorldCoords;
	private boolean startButtonTouched;
	private int startButtonTouchPointer;

	public TabletMainMenuState(final NxtARCore core){
		this.core = core;
		pixelPerfectCamera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		startButton.setPosition(-(startButton.getWidth() / 2), -(startButton.getHeight() / 2));
		startButtonBBox.setPosition(startButton.getX(), startButton.getY());

		win2world = new Vector3(0.0f, 0.0f, 0.0f);
		touchPointWorldCoords = new Vector2();

		startButtonTouched = false;
		startButtonTouchPointer = -1;

		float ledYPos = (-(Gdx.graphics.getHeight() / 2) * 0.5f) + (startButton.getY() * 0.5f);
		clientConnectedLedOn.setSize(clientConnectedLedOn.getWidth() * 0.5f, clientConnectedLedOn.getHeight() * 0.5f);
		clientConnectedLedOn.setPosition(-(clientConnectedLedOn.getWidth() / 2), ledYPos);

		clientConnectedLedOff.setSize(clientConnectedLedOff.getWidth() * 0.5f, clientConnectedLedOff.getHeight() * 0.5f);
		clientConnectedLedOff.setPosition(-(clientConnectedLedOff.getWidth() / 2), ledYPos);
	}

	@Override
	public void render(float delta){
		Gdx.gl.glClearColor(1, 1, 1, 1);
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);

		core.batch.setProjectionMatrix(pixelPerfectCamera.combined);
		core.batch.begin();{
			if(clientConnected){
				clientConnectedLedOn.draw(core.batch);
			}else{
				clientConnectedLedOff.draw(core.batch);
			}
			startButton.draw(core.batch, 1.0f);
		}core.batch.end();
	}

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
	public void dispose(){
		super.dispose();
	}

	/*;;;;;;;;;;;;;;;;;;
	  ; HELPER METHODS ;
	  ;;;;;;;;;;;;;;;;;;*/

	@Override
	public void onStateSet(){
		super.onStateSet();
	}

	private void unprojectTouch(int screenX, int screenY){
		win2world.set(screenX, screenY, 0.0f);
		pixelPerfectCamera.unproject(win2world);
		touchPointWorldCoords.set(win2world.x, win2world.y);
	}

	/*;;;;;;;;;;;;;;;;;;;;;;;;;;;
	  ; INPUT PROCESSOR METHODS ;
	  ;;;;;;;;;;;;;;;;;;;;;;;;;;;*/

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button){
		unprojectTouch(screenX, screenY);

		Gdx.app.log(TAG, CLASS_NAME + String.format(".touchDown(%d, %d, %d, %d)", screenX, screenY, pointer, button));
		Gdx.app.log(TAG, CLASS_NAME + String.format(".touchDown() :: Unprojected touch point: (%f, %f)", touchPointWorldCoords.x, touchPointWorldCoords.y));

		if(!startButton.isDisabled() && startButtonBBox.contains(touchPointWorldCoords)){
			startButton.setChecked(true);
			startButtonTouched = true;
			startButtonTouchPointer = pointer;
			core.nextState = game_states_t.IN_GAME;
			Gdx.app.log(TAG, CLASS_NAME + ".touchDown() :: Start button pressed.");
		}

		return true;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button){
		unprojectTouch(screenX, screenY);

		Gdx.app.log(TAG, CLASS_NAME + String.format(".touchUp(%d, %d, %d, %d)", screenX, screenY, pointer, button));
		Gdx.app.log(TAG, CLASS_NAME + String.format(".touchUp() :: Unprojected touch point: (%f, %f)", touchPointWorldCoords.x, touchPointWorldCoords.y));

		if(!startButton.isDisabled() && startButtonBBox.contains(touchPointWorldCoords)){
			startButton.setChecked(false);
			startButtonTouched = false;
			startButtonTouchPointer = -1;
			Gdx.app.log(TAG, CLASS_NAME + ".touchDown() :: Start button released.");
		}

		return true;
	}

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer){
		unprojectTouch(screenX, screenY);

		if(!startButton.isDisabled() && startButtonTouched && pointer == startButtonTouchPointer && !startButtonBBox.contains(touchPointWorldCoords)){
			startButtonTouchPointer = -1;
			startButtonTouched = false;
			startButton.setChecked(false);
			Gdx.app.log(TAG, CLASS_NAME + ".touchDragged() :: Start button released.");
		}

		return true;
	}


	/*;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
	  ; UNUSED CONTROLLER LISTENER METHODS ;
	  ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;*/

	@Override
	public boolean keyDown(int keycode){
		// Unused.
		return false;
	}

	@Override
	public boolean keyUp(int keycode){
		// Unused.
		return false;
	}

	@Override
	public boolean keyTyped(char character){
		// Unused.
		return false;
	}

	@Override
	public boolean mouseMoved(int screenX, int screenY){
		// Unused.
		return false;
	}

	@Override
	public boolean scrolled(int amount){
		// Unused.
		return false;
	}

	@Override
	public void connected(Controller controller){
		// Unused.
	}

	@Override
	public void disconnected(Controller controller){
		// Unused.
	}

	@Override
	public boolean buttonDown(Controller controller, int buttonCode){
		// Unused.
		return false;
	}

	@Override
	public boolean buttonUp(Controller controller, int buttonCode){
		// Unused.
		return false;
	}

	@Override
	public boolean axisMoved(Controller controller, int axisCode, float value){
		// Unused.
		return false;
	}

	@Override
	public boolean povMoved(Controller controller, int povCode, PovDirection value){
		// Unused.
		return false;
	}

	@Override
	public boolean xSliderMoved(Controller controller, int sliderCode, boolean value){
		// Unused.
		return false;
	}

	@Override
	public boolean ySliderMoved(Controller controller, int sliderCode, boolean value){
		// Unused.
		return false;
	}

	@Override
	public boolean accelerometerMoved(Controller controller, int accelerometerCode, Vector3 value){
		// Unused.
		return false;
	}
}
