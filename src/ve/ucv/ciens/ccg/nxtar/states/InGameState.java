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

import java.util.Arrays;

import ve.ucv.ciens.ccg.networkdata.MotorEvent;
import ve.ucv.ciens.ccg.networkdata.MotorEvent.motor_t;
import ve.ucv.ciens.ccg.nxtar.NxtARCore;
import ve.ucv.ciens.ccg.nxtar.NxtARCore.game_states_t;
import ve.ucv.ciens.ccg.nxtar.network.monitors.MotorEventQueue;
import ve.ucv.ciens.ccg.nxtar.network.monitors.VideoFrameMonitor;
import ve.ucv.ciens.ccg.nxtar.utils.ProjectConstants;
import ve.ucv.ciens.ccg.nxtar.utils.Size;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.PovDirection;
import com.badlogic.gdx.controllers.mappings.Ouya;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

public class InGameState extends BaseState{
	private static final String TAG = "IN_GAME_STATE";
	private static final String CLASS_NAME = InGameState.class.getSimpleName();

	private static final String SHADER_PATH = "shaders/bckg/bckg";

	private NxtARCore core;

	private float u_scaling[];
	protected Sprite background;
	private Texture backgroundTexture;
	private ShaderProgram backgroundShader;

	// Cameras.
	private OrthographicCamera camera;
	private OrthographicCamera pixelPerfectCamera;

	// Video stream graphics.
	private Texture videoFrameTexture;
	private Sprite renderableVideoFrame;
	private Pixmap videoFrame;

	// Interface buttons.
	private Texture buttonTexture;
	private Sprite motorA;
	private Sprite motorB;
	private Sprite motorC;
	private Sprite motorD;

	// Button touch helper fields.
	private Vector3 win2world;
	private Vector2 touchPointWorldCoords;
	private boolean[] motorButtonsTouched;
	private int[] motorButtonsPointers;

	// Monitors.
	private VideoFrameMonitor frameMonitor;
	private MotorEventQueue queue;

	public InGameState(final NxtARCore core){
		this.core = core;
		frameMonitor = VideoFrameMonitor.getInstance();
		queue = MotorEventQueue.getInstance();

		// Set up rendering fields;
		videoFrame = null;

		// Set up the cameras.
		pixelPerfectCamera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		camera = new OrthographicCamera(1.0f, Gdx.graphics.getHeight() / Gdx.graphics.getWidth());

		if(!Ouya.runningOnOuya) setUpButtons();

		// Set up input handling support fields.
		win2world = new Vector3(0.0f, 0.0f, 0.0f);
		touchPointWorldCoords = new Vector2();
		motorButtonsTouched = new boolean[4];
		motorButtonsTouched[0] = false;
		motorButtonsTouched[1] = false;
		motorButtonsTouched[2] = false;
		motorButtonsTouched[3] = false;

		motorButtonsPointers = new int[4];
		motorButtonsPointers[0] = -1;
		motorButtonsPointers[1] = -1;
		motorButtonsPointers[2] = -1;
		motorButtonsPointers[3] = -1;

		backgroundTexture = new Texture(Gdx.files.internal("data/gfx/textures/tile_aqua.png"));
		backgroundTexture.setWrap(TextureWrap.Repeat, TextureWrap.Repeat);
		backgroundTexture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
		background = new Sprite(backgroundTexture);
		background.setSize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		background.setPosition(-(Gdx.graphics.getWidth() / 2), -(Gdx.graphics.getHeight() / 2));

		backgroundShader = new ShaderProgram(Gdx.files.internal(SHADER_PATH + ".vert"), Gdx.files.internal(SHADER_PATH + ".frag"));
		if(!backgroundShader.isCompiled()){
			Gdx.app.error(TAG, CLASS_NAME + ".MainMenuStateBase() :: Failed to compile the background shader.");
			Gdx.app.error(TAG, CLASS_NAME + backgroundShader.getLog());
			backgroundShader = null;
		}

		u_scaling = new float[2];
		u_scaling[0] = Gdx.graphics.getWidth() > Gdx.graphics.getHeight() ? 16.0f : 9.0f;
		u_scaling[1] = Gdx.graphics.getHeight() > Gdx.graphics.getWidth() ? 16.0f : 9.0f;
	}

	@Override
	public void render(float delta){
		byte[] frame;
		byte[] prevFrame = null;
		Size dimensions = null;

		Gdx.gl.glClearColor(1, 1, 1, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		core.batch.setProjectionMatrix(pixelPerfectCamera.combined);
		core.batch.begin();{
			if(backgroundShader != null){
				core.batch.setShader(backgroundShader);
				backgroundShader.setUniform2fv("u_scaling", u_scaling, 0, 2);
			}
			background.draw(core.batch);
			if(backgroundShader != null) core.batch.setShader(null);
		}core.batch.end();

		frame = frameMonitor.getCurrentFrame();
		if(frame != null && !Arrays.equals(frame, prevFrame)){
			dimensions = frameMonitor.getFrameDimensions();
			videoFrame = new Pixmap(frame, 0, dimensions.getWidth() * dimensions.getHeight());
			videoFrameTexture = new Texture(videoFrame);
			videoFrameTexture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
			videoFrame.dispose();

			TextureRegion region = new TextureRegion(videoFrameTexture, 0, 0, dimensions.getWidth(), dimensions.getHeight());

			renderableVideoFrame = new Sprite(region);
			renderableVideoFrame.setOrigin(renderableVideoFrame.getWidth() / 2, renderableVideoFrame.getHeight() / 2);
			if(!Ouya.runningOnOuya){
				renderableVideoFrame.setSize(1.0f, renderableVideoFrame.getHeight() / renderableVideoFrame.getWidth() );
				renderableVideoFrame.rotate90(true);
				renderableVideoFrame.translate(-renderableVideoFrame.getWidth() / 2, 0.5f - renderableVideoFrame.getHeight());
			}else{
				float xSize = Gdx.graphics.getHeight() * (dimensions.getWidth() / dimensions.getHeight());
				renderableVideoFrame.setSize(xSize * ProjectConstants.OVERSCAN, Gdx.graphics.getHeight() * ProjectConstants.OVERSCAN);
				renderableVideoFrame.rotate90(true);
				renderableVideoFrame.translate(-renderableVideoFrame.getWidth() / 2, -renderableVideoFrame.getHeight() / 2);
			}

			if(!Ouya.runningOnOuya){
				core.batch.setProjectionMatrix(camera.combined);
			}else{
				core.batch.setProjectionMatrix(pixelPerfectCamera.combined);
			}
			core.batch.begin();{
				renderableVideoFrame.draw(core.batch);
			}core.batch.end();

			videoFrameTexture.dispose();
		}

		core.batch.setProjectionMatrix(pixelPerfectCamera.combined);
		core.batch.begin();{
			if(!Ouya.runningOnOuya){
				motorA.draw(core.batch);
				motorB.draw(core.batch);
				motorC.draw(core.batch);
				motorD.draw(core.batch);
			}
		}core.batch.end();

		prevFrame = frame;
	}

	@Override
	public void resize(int width, int height){
		// TODO Auto-generated method stub

	}

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
		if(videoFrameTexture != null)
			videoFrameTexture.dispose();
		if(buttonTexture != null)
			buttonTexture.dispose();
		backgroundTexture.dispose();
		if(backgroundShader != null) backgroundShader.dispose();
	}

	/*;;;;;;;;;;;;;;;;;;
	  ; HELPER METHODS ;
	  ;;;;;;;;;;;;;;;;;;*/

	@Override
	public void onStateSet(){
		stateActive = true;
		Gdx.input.setInputProcessor(this);
		Gdx.input.setCatchBackKey(true);
		Gdx.input.setCatchMenuKey(true);
	}

	@Override
	public void onStateUnset(){
		stateActive = false;
		Gdx.input.setInputProcessor(null);
		Gdx.input.setCatchBackKey(false);
		Gdx.input.setCatchMenuKey(false);
	}

	private void setUpButtons(){
		buttonTexture = new Texture(Gdx.files.internal("data/gfx/gui/PBCrichton_Flat_Button.png"));
		buttonTexture.setFilter(TextureFilter.Linear, TextureFilter.Linear);

		TextureRegion region = new TextureRegion(buttonTexture, 0, 0, buttonTexture.getWidth(), buttonTexture.getHeight());

		motorA = new Sprite(region);
		motorA.setSize(motorA.getWidth() * 0.7f, motorA.getHeight() * 0.7f);

		motorB = new Sprite(region);
		motorB.setSize(motorB.getWidth() * 0.7f, motorB.getHeight() * 0.7f);

		motorC = new Sprite(region);
		motorC.setSize(motorC.getWidth() * 0.7f, motorC.getHeight() * 0.7f);

		motorD = new Sprite(region);
		motorD.setSize(motorD.getWidth() * 0.7f, motorD.getHeight() * 0.7f);

		motorA.setPosition(-(Gdx.graphics.getWidth() / 2) + 10, -(Gdx.graphics.getHeight() / 2) + motorB.getHeight() + 20);
		motorB.setPosition(-(Gdx.graphics.getWidth() / 2) + 20 + (motorA.getWidth() / 2), -(Gdx.graphics.getHeight() / 2) + 10);
		motorC.setPosition((Gdx.graphics.getWidth() / 2) - (1.5f * (motorD.getWidth())) - 20, -(Gdx.graphics.getHeight() / 2) + 10);
		motorD.setPosition((Gdx.graphics.getWidth() / 2) - motorD.getWidth() - 10, -(Gdx.graphics.getHeight() / 2) + 20 + motorC.getHeight());
	}

	/*;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
	  ; BEGIN INPUT PROCESSOR METHODS ;
	  ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;*/

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button){
		MotorEvent event;

		if(!Ouya.runningOnOuya){
			win2world.set(screenX, screenY, 0.0f);
			camera.unproject(win2world);
			touchPointWorldCoords.set(win2world.x * Gdx.graphics.getWidth(), win2world.y * Gdx.graphics.getHeight());

			Gdx.app.log(TAG, CLASS_NAME + String.format(".touchDown(%d, %d, %d, %d)", screenX, screenY, pointer, button));
			Gdx.app.log(TAG, CLASS_NAME + String.format(".touchDown() :: Unprojected touch point: (%f, %f)", touchPointWorldCoords.x, touchPointWorldCoords.y));

			if(motorA.getBoundingRectangle().contains(touchPointWorldCoords)){
				Gdx.app.log(TAG, CLASS_NAME + ".touchDown() :: Motor A button pressed");

				// Enqueue the event corresponding to this button if the opposing button is not pressed already.
				if(!motorButtonsTouched[1]){
					motorButtonsTouched[0] = true;
					motorButtonsPointers[0] = pointer;

					event = new MotorEvent();
					event.setMotor(motor_t.MOTOR_C);
					event.setPower((byte)100);
					queue.addEvent(event);
				}

			}else if(motorB.getBoundingRectangle().contains(touchPointWorldCoords)){
				Gdx.app.log(TAG, CLASS_NAME + ".touchDown() :: Motor B button pressed");

				// Enqueue the event corresponding to this button if the opposing button is not pressed already.
				if(!motorButtonsTouched[0]){
					motorButtonsTouched[1] = true;
					motorButtonsPointers[1] = pointer;

					event = new MotorEvent();
					event.setMotor(motor_t.MOTOR_C);
					event.setPower((byte)-100);
					queue.addEvent(event);
				}

			}else if(motorC.getBoundingRectangle().contains(touchPointWorldCoords)){
				Gdx.app.log(TAG, CLASS_NAME + ".touchDown() :: Motor C button pressed");

				// Enqueue the event corresponding to this button if the opposing button is not pressed already.
				if(!motorButtonsTouched[3]){
					motorButtonsTouched[2] = true;
					motorButtonsPointers[2] = pointer;

					event = new MotorEvent();
					event.setMotor(motor_t.MOTOR_A);
					event.setPower((byte)-100);
					queue.addEvent(event);
				}

			}else if(motorD.getBoundingRectangle().contains(touchPointWorldCoords)){
				Gdx.app.log(TAG, CLASS_NAME + ".touchDown() :: Motor D button pressed");

				// Enqueue the event corresponding to this button if the opposing button is not pressed already.
				if(!motorButtonsTouched[2]){
					motorButtonsTouched[3] = true;
					motorButtonsPointers[3] = pointer;

					event = new MotorEvent();
					event.setMotor(motor_t.MOTOR_A);
					event.setPower((byte)100);
					queue.addEvent(event);
				}

			}
		}
		return true;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button){
		MotorEvent event;

		if(!Ouya.runningOnOuya){
			win2world.set(screenX, screenY, 0.0f);
			camera.unproject(win2world);
			touchPointWorldCoords.set(win2world.x * Gdx.graphics.getWidth(), win2world.y * Gdx.graphics.getHeight());

			Gdx.app.log(TAG, CLASS_NAME + String.format(".touchUp(%d, %d, %d, %d)", screenX, screenY, pointer, button));
			Gdx.app.log(TAG, CLASS_NAME + String.format(".touchUp() :: Unprojected touch point: (%f, %f)", touchPointWorldCoords.x, touchPointWorldCoords.y));

			if(motorA.getBoundingRectangle().contains(touchPointWorldCoords)){
				Gdx.app.log(TAG, CLASS_NAME + ".touchUp() :: Motor A button released");


				// Enqueue the event corresponding to releasing this button if the opposing button is not pressed already.
				if(!motorButtonsTouched[1]){
					motorButtonsPointers[0] = -1;
					motorButtonsTouched[0] = false;

					event = new MotorEvent();
					event.setMotor(motor_t.MOTOR_C);
					event.setPower((byte) 0);
					queue.addEvent(event);
				}

			}else if(motorB.getBoundingRectangle().contains(touchPointWorldCoords)){
				Gdx.app.log(TAG, CLASS_NAME + ".touchUp() :: Motor B button released");

				// Enqueue the event corresponding to releasing this button if the opposing button is not pressed already.
				if(!motorButtonsTouched[0]){
					motorButtonsPointers[1] = -1;
					motorButtonsTouched[1] = false;

					event = new MotorEvent();
					event.setMotor(motor_t.MOTOR_C);
					event.setPower((byte) 0);
					queue.addEvent(event);
				}

			}else if(motorC.getBoundingRectangle().contains(touchPointWorldCoords)){
				Gdx.app.log(TAG, CLASS_NAME + ".touchUp() :: Motor C button released");

				// Enqueue the event corresponding to releasing this button if the opposing button is not pressed already.
				if(!motorButtonsTouched[3]){
					motorButtonsPointers[2] = -1;
					motorButtonsTouched[2] = false;

					event = new MotorEvent();
					event.setMotor(motor_t.MOTOR_A);
					event.setPower((byte) 0);
					queue.addEvent(event);
				}

			}else if(motorD.getBoundingRectangle().contains(touchPointWorldCoords)){
				Gdx.app.log(TAG, CLASS_NAME + ".touchUp() :: Motor D button released");

				// Enqueue the event corresponding to releasing this button if the opposing button is not pressed already.
				if(!motorButtonsTouched[2]){
					motorButtonsPointers[3] = -1;
					motorButtonsTouched[3] = false;

					event = new MotorEvent();
					event.setMotor(motor_t.MOTOR_A);
					event.setPower((byte) 0);
					queue.addEvent(event);
				}

			}
		}
		return true;
	}

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer){
		MotorEvent event;

		if(!Ouya.runningOnOuya){
			win2world.set(screenX, screenY, 0.0f);
			camera.unproject(win2world);
			touchPointWorldCoords.set(win2world.x * Gdx.graphics.getWidth(), win2world.y * Gdx.graphics.getHeight());

			if(pointer == motorButtonsPointers[0] && !motorA.getBoundingRectangle().contains(touchPointWorldCoords)){
				Gdx.app.log(TAG, CLASS_NAME + ".touchDragged() :: Motor A button released");

				// Enqueue the event corresponding to releasing this button if the opposing button is not pressed already.
				if(!motorButtonsTouched[1]){
					motorButtonsPointers[0] = -1;
					motorButtonsTouched[0] = false;

					event = new MotorEvent();
					event.setMotor(motor_t.MOTOR_C);
					event.setPower((byte) 0);
					queue.addEvent(event);
				}

			}else if(pointer == motorButtonsPointers[1] && !motorB.getBoundingRectangle().contains(touchPointWorldCoords)){
				Gdx.app.log(TAG, CLASS_NAME + ".touchDragged() :: Motor B button released");

				// Enqueue the event corresponding to releasing this button if the opposing button is not pressed already.
				if(!motorButtonsTouched[0]){
					motorButtonsPointers[1] = -1;
					motorButtonsTouched[1] = false;

					event = new MotorEvent();
					event.setMotor(motor_t.MOTOR_C);
					event.setPower((byte) 0);
					queue.addEvent(event);
				}

			}else if(pointer == motorButtonsPointers[2] && !motorC.getBoundingRectangle().contains(touchPointWorldCoords)){
				Gdx.app.log(TAG, CLASS_NAME + ".touchDragged() :: Motor C button released");

				// Enqueue the event corresponding to releasing this button if the opposing button is not pressed already.
				if(!motorButtonsTouched[3]){
					motorButtonsPointers[2] = -1;
					motorButtonsTouched[2] = false;

					event = new MotorEvent();
					event.setMotor(motor_t.MOTOR_A);
					event.setPower((byte) 0);
					queue.addEvent(event);
				}

			}else if(pointer == motorButtonsPointers[3] && !motorD.getBoundingRectangle().contains(touchPointWorldCoords)){
				Gdx.app.log(TAG, CLASS_NAME + ".touchDragged() :: Motor D button released");

				// Enqueue the event corresponding to releasing this button if the opposing button is not pressed already.
				if(!motorButtonsTouched[2]){
					motorButtonsPointers[3] = -1;
					motorButtonsTouched[3] = false;

					event = new MotorEvent();
					event.setMotor(motor_t.MOTOR_A);
					event.setPower((byte) 0);
					queue.addEvent(event);
				}

			}
		}
		return true;
	}

	@Override
	public boolean keyDown(int keycode){
		if(keycode == Input.Keys.BACK){
			core.nextState = game_states_t.MAIN_MENU;
			return true;
		}
		return false;
	}

	/*;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
	  ; BEGIN CONTROLLER LISTENER METHODS ;
	  ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;*/

	@Override
	public boolean buttonDown(Controller controller, int buttonCode){
		if(stateActive){
			Gdx.app.log(TAG, CLASS_NAME + ".buttonDown() :: " + controller.getName() + " :: " + Integer.toString(buttonCode));
			return true;
		}else{
			return false;
		}
	}

	@Override
	public boolean buttonUp(Controller controller, int buttonCode){
		if(stateActive){
			Gdx.app.log(TAG, CLASS_NAME + ".buttonDown() :: " + controller.getName() + " :: " + Integer.toString(buttonCode));
			return true;
		}else{
			return false;
		}
	}

	@Override
	public boolean axisMoved(Controller controller, int axisCode, float value){
		if(stateActive){
			if(value >= Ouya.STICK_DEADZONE){
				if(axisCode == Ouya.AXIS_LEFT_TRIGGER){
					Gdx.app.log(TAG, CLASS_NAME + ".axisMoved() :: LEFT TRIGGER pressed.");
				}
				if(axisCode == Ouya.AXIS_RIGHT_TRIGGER){
					Gdx.app.log(TAG, CLASS_NAME + ".axisMoved() :: RIGHT TRIGGER pressed."); 
				}
			}
			return true;
		}else{
			return false;
		}
	}

	@Override
	public void connected(Controller controller){ }

	@Override
	public void disconnected(Controller controller){ }

	/*;;;;;;;;;;;;;;;;;;;;;;;;;;;
	  ; UNUSED LISTENER METHODS ;
	  ;;;;;;;;;;;;;;;;;;;;;;;;;;;*/

	@Override
	public boolean mouseMoved(int screenX, int screenY){
		return false;
	}

	@Override
	public boolean keyUp(int keycode){
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean keyTyped(char character){
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean povMoved(Controller controller, int povCode,
			PovDirection value){
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean xSliderMoved(Controller controller, int sliderCode,
			boolean value){
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean ySliderMoved(Controller controller, int sliderCode,
			boolean value){
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean accelerometerMoved(Controller controller,
			int accelerometerCode, Vector3 value){
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean scrolled(int amount){
		// TODO Auto-generated method stub
		return false;
	}
}
