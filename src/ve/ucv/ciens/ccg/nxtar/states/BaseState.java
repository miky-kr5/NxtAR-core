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

import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.ControllerListener;
import com.badlogic.gdx.controllers.PovDirection;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

public abstract class BaseState implements Screen, ControllerListener, InputProcessor {
	protected NxtARCore core;
	protected boolean stateActive;
	protected OrthographicCamera pixelPerfectCamera;
	protected Vector3 win2world;
	protected Vector2 touchPointWorldCoords;

	/* STATE METHODS */
	public abstract void onStateSet();
	public abstract void onStateUnset();

	/* SCREEN METHODS*/
	@Override
	public abstract void render(float delta);

	@Override
	public abstract void resize(int width, int height);

	@Override
	public abstract void show();

	@Override
	public abstract void hide();

	@Override
	public abstract void pause();

	@Override
	public abstract void resume();

	@Override
	public abstract void dispose();

	/* HELPER METHODS */

	protected final void unprojectTouch(int screenX, int screenY){
		win2world.set(screenX, screenY, 0.0f);
		pixelPerfectCamera.unproject(win2world);
		touchPointWorldCoords.set(win2world.x, win2world.y);
	}

	/* INPUT PROCESSOR METHODS. */
	@Override
	public boolean keyDown(int keycode){
		return false;
	};

	@Override
	public boolean keyUp(int keycode){
		return false;
	};

	@Override
	public boolean keyTyped(char character){
		return false;
	};

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button){
		return false;
	};

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button){
		return false;
	};

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer){
		return false;
	};

	@Override
	public boolean mouseMoved(int screenX, int screenY){
		return false;
	};

	@Override
	public boolean scrolled(int amount){
		return false;
	};

	/* CONTROLLER LISTENER METHODS. */
	@Override
	public void connected(Controller controller){ };

	@Override
	public void disconnected(Controller controller){ };

	@Override
	public boolean buttonDown(Controller controller, int buttonCode){
		return false;
	};

	@Override
	public boolean buttonUp(Controller controller, int buttonCode){
		return false;
	};
	@Override
	public boolean axisMoved(Controller controller, int axisCode, float value){
		return false;
	};

	@Override
	public boolean povMoved(Controller controller, int povCode, PovDirection value){
		return false;
	};

	@Override
	public boolean xSliderMoved(Controller controller, int sliderCode, boolean value){
		return false;
	};

	@Override
	public boolean ySliderMoved(Controller controller, int sliderCode, boolean value){
		return false;
	};

	@Override
	public boolean accelerometerMoved(Controller controller, int accelerometerCode, Vector3 value){
		return false;
	};
}
