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
import com.badlogic.gdx.math.Vector3;

public abstract class BaseState implements Screen, ControllerListener, InputProcessor {
	protected NxtARCore core;

	/* STATE METHODS */
	public abstract void onStateSet();
	
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

	/* INPUT PROCESSOR METHODS. */
	@Override
	public abstract boolean keyDown(int keycode);
	@Override
	public abstract boolean keyUp(int keycode);
	@Override
	public abstract boolean keyTyped(char character);
	@Override
	public abstract boolean touchDown(int screenX, int screenY, int pointer, int button);
	@Override
	public abstract boolean touchUp(int screenX, int screenY, int pointer, int button);
	@Override
	public abstract boolean touchDragged(int screenX, int screenY, int pointer);
	@Override
	public abstract boolean mouseMoved(int screenX, int screenY);
	@Override
	public abstract boolean scrolled(int amount);

	/* CONTROLLER LISTENER METHODS. */
	@Override
	public abstract void connected(Controller controller);
	@Override
	public abstract void disconnected(Controller controller);
	@Override
	public abstract boolean buttonDown(Controller controller, int buttonCode);
	@Override
	public abstract boolean buttonUp(Controller controller, int buttonCode);
	@Override
	public abstract boolean axisMoved(Controller controller, int axisCode, float value);
	@Override
	public abstract boolean povMoved(Controller controller, int povCode, PovDirection value);
	@Override
	public abstract boolean xSliderMoved(Controller controller, int sliderCode, boolean value);
	@Override
	public abstract boolean ySliderMoved(Controller controller, int sliderCode, boolean value);
	@Override
	public abstract boolean accelerometerMoved(Controller controller, int accelerometerCode, Vector3 value);
}
