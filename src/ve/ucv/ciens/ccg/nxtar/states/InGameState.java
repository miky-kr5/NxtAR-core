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

import ve.ucv.ciens.ccg.networkdata.MotorEvent;
import ve.ucv.ciens.ccg.networkdata.MotorEvent.motor_t;
import ve.ucv.ciens.ccg.nxtar.NxtARCore;
import ve.ucv.ciens.ccg.nxtar.NxtARCore.game_states_t;
import ve.ucv.ciens.ccg.nxtar.graphics.CustomPerspectiveCamera;
import ve.ucv.ciens.ccg.nxtar.interfaces.ImageProcessor.MarkerData;
import ve.ucv.ciens.ccg.nxtar.network.monitors.MotorEventQueue;
import ve.ucv.ciens.ccg.nxtar.network.monitors.VideoFrameMonitor;
import ve.ucv.ciens.ccg.nxtar.systems.AnimationSystem;
import ve.ucv.ciens.ccg.nxtar.systems.GeometrySystem;
import ve.ucv.ciens.ccg.nxtar.systems.MarkerPositioningSystem;
import ve.ucv.ciens.ccg.nxtar.systems.MarkerRenderingSystem;
import ve.ucv.ciens.ccg.nxtar.systems.ObjectPositioningSystem;
import ve.ucv.ciens.ccg.nxtar.systems.ObjectRenderingSystem;
import ve.ucv.ciens.ccg.nxtar.utils.GameSettings;
import ve.ucv.ciens.ccg.nxtar.utils.ProjectConstants;

import com.artemis.World;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.mappings.Ouya;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

public class InGameState extends BaseState{
	private static final String  TAG                    = "IN_GAME_STATE";
	private static final String  CLASS_NAME             = InGameState.class.getSimpleName();
	private static final String  BACKGROUND_SHADER_PATH = "shaders/bckg/bckg";
	private static final float   NEAR                   = 0.01f;
	private static final float   FAR                    = 100.0f;

	// Background related fields.
	private float                           uScaling[];
	protected Sprite                        background;
	private Texture                         backgroundTexture;
	private ShaderProgram                   backgroundShader;

	// 3D rendering fields.
	private ModelBatch                      modelBatch;
	private FrameBuffer                     frameBuffer;
	private Sprite                          frameBufferSprite;

	// Game world objects.
	private World                           gameWorld;
	private MarkerRenderingSystem           markerRenderingSystem;
	private ObjectRenderingSystem           objectRenderingSystem;

	// Cameras.
	private OrthographicCamera              unitaryOrthoCamera;
	private OrthographicCamera              pixelPerfectOrthoCamera;
	private CustomPerspectiveCamera         perspectiveCamera;

	// Video stream graphics.
	private Texture                         videoFrameTexture;
	private Sprite                          renderableVideoFrame;
	private Pixmap                          videoFrame;

	// Interface buttons.
	private Texture                         buttonTexture;
	private Texture                         buttonTexture2;
	private Sprite                          motorA;
	private Sprite                          motorB;
	private Sprite                          motorC;
	private Sprite                          motorD;
	private Sprite                          headA;
	private Sprite                          headB;
	private Sprite                          headC;

	// Button touch helper fields.
	private boolean[]                       motorButtonsTouched;
	private int[]                           motorButtonsPointers;
	private boolean[]                       motorGamepadButtonPressed;

	// Monitors.
	private VideoFrameMonitor               frameMonitor;
	private MotorEventQueue                 queue;

	public InGameState(final NxtARCore core){
		this.core = core;
		frameMonitor = VideoFrameMonitor.getInstance();
		queue = MotorEventQueue.getInstance();

		// Set up rendering fields;
		videoFrame = null;

		// Set up the cameras.
		pixelPerfectOrthoCamera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		unitaryOrthoCamera = new OrthographicCamera(1.0f, Gdx.graphics.getHeight() / Gdx.graphics.getWidth());

		if(!Ouya.runningOnOuya) setUpButtons();

		// Set up input handling support fields.
		win2world = new Vector3(0.0f, 0.0f, 0.0f);
		touchPointWorldCoords = new Vector2();

		motorButtonsTouched = new boolean[7];
		motorButtonsTouched[0] = false;
		motorButtonsTouched[1] = false;
		motorButtonsTouched[2] = false;
		motorButtonsTouched[3] = false;
		motorButtonsTouched[4] = false;
		motorButtonsTouched[5] = false;
		motorButtonsTouched[6] = false;

		motorButtonsPointers = new int[7];
		motorButtonsPointers[0] = -1;
		motorButtonsPointers[1] = -1;
		motorButtonsPointers[2] = -1;
		motorButtonsPointers[3] = -1;
		motorButtonsPointers[4] = -1;
		motorButtonsPointers[5] = -1;
		motorButtonsPointers[6] = -1;

		motorGamepadButtonPressed = new boolean[7];
		motorGamepadButtonPressed[0] = false;
		motorGamepadButtonPressed[1] = false;
		motorGamepadButtonPressed[2] = false;
		motorGamepadButtonPressed[3] = false;
		motorGamepadButtonPressed[4] = false;
		motorGamepadButtonPressed[5] = false;
		motorGamepadButtonPressed[6] = false;

		// Set up the background.
		backgroundTexture = new Texture(Gdx.files.internal("data/gfx/textures/tile_aqua.png"));
		backgroundTexture.setWrap(TextureWrap.Repeat, TextureWrap.Repeat);
		backgroundTexture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
		background = new Sprite(backgroundTexture);
		background.setSize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		background.setPosition(-(Gdx.graphics.getWidth() / 2), -(Gdx.graphics.getHeight() / 2));

		// Set up the shader.
		backgroundShader = new ShaderProgram(Gdx.files.internal(BACKGROUND_SHADER_PATH + "_vert.glsl"), Gdx.files.internal(BACKGROUND_SHADER_PATH + "_frag.glsl"));
		if(!backgroundShader.isCompiled()){
			Gdx.app.error(TAG, CLASS_NAME + ".MainMenuStateBase() :: Failed to compile the background shader.");
			Gdx.app.error(TAG, CLASS_NAME + backgroundShader.getLog());
			backgroundShader = null;
		}

		uScaling = new float[2];
		uScaling[0] = Gdx.graphics.getWidth() > Gdx.graphics.getHeight() ? 16.0f : 9.0f;
		uScaling[1] = Gdx.graphics.getHeight() > Gdx.graphics.getWidth() ? 16.0f : 9.0f;

		// Set up the 3D rendering.
		modelBatch = new ModelBatch();
		frameBuffer = null;
		perspectiveCamera = null;
		frameBufferSprite = null;

		// Set up the game world.
		gameWorld = GameSettings.getGameWorld();

		gameWorld.setSystem(new MarkerPositioningSystem());
		gameWorld.setSystem(new ObjectPositioningSystem(), true);
		gameWorld.setSystem(new GeometrySystem());
		gameWorld.setSystem(new AnimationSystem());
		// TODO: Make and add object-marker collision detection system.
		//gameWorld.setSystem(GameSettings.gameLogicSystem);

		markerRenderingSystem = new MarkerRenderingSystem(modelBatch);
		objectRenderingSystem = new ObjectRenderingSystem(modelBatch);
		gameWorld.setSystem(markerRenderingSystem, true);
		gameWorld.setSystem(objectRenderingSystem, true);

		gameWorld.initialize();
	}

	/*;;;;;;;;;;;;;;;;;;;;;;
	  ; BASE STATE METHODS ;
	  ;;;;;;;;;;;;;;;;;;;;;;*/

	@Override
	public void render(float delta){
		int w, h;
		byte[] frame;
		MarkerData data;
		TextureRegion region;
		float focalPointX, focalPointY, cameraCenterX, cameraCenterY;

		// Clear the screen.
		Gdx.gl.glClearColor(1, 1, 1, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		// Render the background.
		core.batch.setProjectionMatrix(pixelPerfectOrthoCamera.combined);
		core.batch.begin();{
			if(backgroundShader != null){
				core.batch.setShader(backgroundShader);
				backgroundShader.setUniform2fv("u_scaling", uScaling, 0, 2);
			}
			background.draw(core.batch);
			if(backgroundShader != null) core.batch.setShader(null);
		}core.batch.end();

		// Fetch the current video frame.
		frame = frameMonitor.getCurrentFrame();
		w = frameMonitor.getFrameDimensions().getWidth();
		h = frameMonitor.getFrameDimensions().getHeight();

		// Create the 3D perspective camera and the frame buffer object if they don't exist.
		if(perspectiveCamera == null && frameBuffer == null){
			frameBuffer = new FrameBuffer(Format.RGBA8888, w, h, true);
			frameBuffer.getColorBufferTexture().setFilter(TextureFilter.Linear, TextureFilter.Linear);

			perspectiveCamera = new CustomPerspectiveCamera(67, w, h);
			perspectiveCamera.translate(0.0f, 0.0f, 0.0f);
			perspectiveCamera.near = NEAR;
			perspectiveCamera.far = FAR;
			perspectiveCamera.lookAt(0.0f, 0.0f, -1.0f);
			perspectiveCamera.update();
		}

		// Attempt to find the markers in the current video frame.
		data = core.cvProc.findMarkersInFrame(frame);

		// If a valid frame was fetched.
		if(data != null && data.outFrame != null){
			// Set the camera to the correct projection.
			focalPointX   = core.cvProc.getFocalPointX();
			focalPointY   = core.cvProc.getFocalPointY();
			cameraCenterX = core.cvProc.getCameraCenterX();
			cameraCenterY = core.cvProc.getCameraCenterY();
			perspectiveCamera.setCustomARProjectionMatrix(focalPointX, focalPointY, cameraCenterX, cameraCenterY, NEAR, FAR, w, h);
			perspectiveCamera.update(perspectiveCamera.projection);

			// Update the game state.
			gameWorld.setDelta(Gdx.graphics.getDeltaTime() * 1000);
			gameWorld.getSystem(MarkerPositioningSystem.class).setMarkerData(data);
			gameWorld.process();

			// Decode the video frame.
			videoFrame = new Pixmap(data.outFrame, 0, w * h);
			videoFrameTexture = new Texture(videoFrame);
			videoFrameTexture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
			videoFrame.dispose();

			// Convert the decoded frame into a renderable texture.
			region = new TextureRegion(videoFrameTexture, 0, 0, w, h);
			if(renderableVideoFrame == null)
				renderableVideoFrame = new Sprite(region);
			else
				renderableVideoFrame.setRegion(region);
			renderableVideoFrame.setOrigin(renderableVideoFrame.getWidth() / 2, renderableVideoFrame.getHeight() / 2);
			renderableVideoFrame.setPosition(0, 0);

			// Set the 3D frame buffer for rendering.
			frameBuffer.begin();{
				// Set OpenGL state.
				Gdx.gl.glClearColor(0, 0, 0, 0);
				Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
				Gdx.gl.glDisable(GL20.GL_TEXTURE_2D);

				// Call rendering systems.
				markerRenderingSystem.begin(perspectiveCamera);
				markerRenderingSystem.process();
				markerRenderingSystem.end();

				objectRenderingSystem.begin(perspectiveCamera);
				objectRenderingSystem.process();
				objectRenderingSystem.end();
			}frameBuffer.end();

			// Set the frame buffer object texture to a renderable sprite.
			region = new TextureRegion(frameBuffer.getColorBufferTexture(), 0, 0, frameBuffer.getWidth(), frameBuffer.getHeight());
			region.flip(false, true);
			if(frameBufferSprite == null)
				frameBufferSprite = new Sprite(region);
			else
				frameBufferSprite.setRegion(region);
			frameBufferSprite.setOrigin(frameBufferSprite.getWidth() / 2, frameBufferSprite.getHeight() / 2);
			frameBufferSprite.setPosition(0, 0);

			// Set the position and orientation of the renderable video frame and the frame buffer.
			if(!Ouya.runningOnOuya){
				renderableVideoFrame.setSize(1.0f, renderableVideoFrame.getHeight() / renderableVideoFrame.getWidth() );
				renderableVideoFrame.rotate90(true);
				renderableVideoFrame.translate(-renderableVideoFrame.getWidth() / 2, 0.5f - renderableVideoFrame.getHeight());

				frameBufferSprite.setSize(1.0f, frameBufferSprite.getHeight() / frameBufferSprite.getWidth() );
				frameBufferSprite.rotate90(true);
				frameBufferSprite.translate(-frameBufferSprite.getWidth() / 2, 0.5f - frameBufferSprite.getHeight());
			}else{
				float xSize = Gdx.graphics.getHeight() * (w / h);
				renderableVideoFrame.setSize(xSize * ProjectConstants.OVERSCAN, Gdx.graphics.getHeight() * ProjectConstants.OVERSCAN);
				renderableVideoFrame.rotate90(true);
				renderableVideoFrame.translate(-renderableVideoFrame.getWidth() / 2, -renderableVideoFrame.getHeight() / 2);

				frameBufferSprite.setSize(xSize * ProjectConstants.OVERSCAN, Gdx.graphics.getHeight() * ProjectConstants.OVERSCAN);
				frameBufferSprite.rotate90(true);
				frameBufferSprite.translate(-frameBufferSprite.getWidth() / 2, -frameBufferSprite.getHeight() / 2);
			}

			// Set the correct camera for the device.
			if(!Ouya.runningOnOuya){
				core.batch.setProjectionMatrix(unitaryOrthoCamera.combined);
			}else{
				core.batch.setProjectionMatrix(pixelPerfectOrthoCamera.combined);
			}

			// Render the video frame and the frame buffer.
			core.batch.begin();{
				renderableVideoFrame.draw(core.batch);
				frameBufferSprite.draw(core.batch);
			}core.batch.end();

			// Clear the video frame from memory.
			videoFrameTexture.dispose();
		}

		// Render the interface buttons.
		if(!Ouya.runningOnOuya){
			core.batch.setProjectionMatrix(pixelPerfectOrthoCamera.combined);
			core.batch.begin();{
				motorA.draw(core.batch);
				motorB.draw(core.batch);
				motorC.draw(core.batch);
				motorD.draw(core.batch);
				headA.draw(core.batch);
				headB.draw(core.batch);
				headC.draw(core.batch);
			}core.batch.end();
		}

		data = null;
	}

	@Override
	public void dispose(){
		if(modelBatch != null)
			modelBatch.dispose();

		if(videoFrameTexture != null)
			videoFrameTexture.dispose();

		if(buttonTexture != null)
			buttonTexture.dispose();

		if(buttonTexture2 != null)
			buttonTexture2.dispose();

		backgroundTexture.dispose();

		if(backgroundShader != null)
			backgroundShader.dispose();

		if(frameBuffer != null)
			frameBuffer.dispose();
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

		buttonTexture2 = new Texture(Gdx.files.internal("data/gfx/gui/orange_glowy_button.png"));

		headA = new Sprite(buttonTexture2);
		headA.setSize(headA.getWidth() * 0.3f, headA.getHeight() * 0.6f);

		headB = new Sprite(buttonTexture2);
		headB.setSize(headB.getWidth() * 0.3f, headB.getHeight() * 0.6f);

		headA.setPosition(-headA.getWidth() - 10, motorA.getY() + (headA.getHeight() / 2));
		headB.setPosition(10, motorA.getY() + (headA.getHeight() / 2));

		headC = new Sprite(buttonTexture2);
		headC.setSize(headC.getWidth() * 0.3f, headC.getHeight() * 0.6f);
		headC.setPosition(-(headC.getWidth() / 2), headA.getY() - headA.getHeight() - 10);
	}

	/*;;;;;;;;;;;;;;;;;;;;;;;;;;;
	  ; INPUT PROCESSOR METHODS ;
	  ;;;;;;;;;;;;;;;;;;;;;;;;;;;*/

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button){
		MotorEvent event;

		if(!Ouya.runningOnOuya){
			win2world.set(screenX, screenY, 0.0f);
			unitaryOrthoCamera.unproject(win2world);
			touchPointWorldCoords.set(win2world.x * Gdx.graphics.getWidth(), win2world.y * Gdx.graphics.getHeight());

			Gdx.app.log(TAG, CLASS_NAME + String.format(".touchDown(%d, %d, %d, %d)", screenX, screenY, pointer, button));
			Gdx.app.log(TAG, CLASS_NAME + String.format(".touchDown() :: Unprojected touch point: (%f, %f)", touchPointWorldCoords.x, touchPointWorldCoords.y));

			if(motorA.getBoundingRectangle().contains(touchPointWorldCoords)){
				Gdx.app.log(TAG, CLASS_NAME + ".touchDown() :: Motor A button pressed");

				motorButtonsTouched[0] = true;
				motorButtonsPointers[0] = pointer;

				event = new MotorEvent();
				event.setMotor(motor_t.MOTOR_A);
				event.setPower((byte)100);
				queue.addEvent(event);

			}else if(motorB.getBoundingRectangle().contains(touchPointWorldCoords)){
				Gdx.app.log(TAG, CLASS_NAME + ".touchDown() :: Motor B button pressed");

				motorButtonsTouched[1] = true;
				motorButtonsPointers[1] = pointer;

				event = new MotorEvent();
				event.setMotor(motor_t.MOTOR_A);
				event.setPower((byte)-100);
				queue.addEvent(event);

			}else if(motorC.getBoundingRectangle().contains(touchPointWorldCoords)){
				Gdx.app.log(TAG, CLASS_NAME + ".touchDown() :: Motor C button pressed");

				motorButtonsTouched[2] = true;
				motorButtonsPointers[2] = pointer;

				event = new MotorEvent();
				event.setMotor(motor_t.MOTOR_C);
				event.setPower((byte)-100);
				queue.addEvent(event);

			}else if(motorD.getBoundingRectangle().contains(touchPointWorldCoords)){
				Gdx.app.log(TAG, CLASS_NAME + ".touchDown() :: Motor D button pressed");

				motorButtonsTouched[3] = true;
				motorButtonsPointers[3] = pointer;

				event = new MotorEvent();
				event.setMotor(motor_t.MOTOR_C);
				event.setPower((byte)100);
				queue.addEvent(event);

			}else if(headA.getBoundingRectangle().contains(touchPointWorldCoords)){
				Gdx.app.log(TAG, CLASS_NAME + ".touchDown() :: Head A button pressed");

				motorButtonsTouched[4] = true;
				motorButtonsPointers[4] = pointer;

				event = new MotorEvent();
				event.setMotor(motor_t.MOTOR_B);
				event.setPower((byte)-40);
				queue.addEvent(event);

			}else if(headB.getBoundingRectangle().contains(touchPointWorldCoords)){
				Gdx.app.log(TAG, CLASS_NAME + ".touchDown() :: Head B button pressed");

				motorButtonsTouched[5] = true;
				motorButtonsPointers[5] = pointer;

				event = new MotorEvent();
				event.setMotor(motor_t.MOTOR_B);
				event.setPower((byte)40);
				queue.addEvent(event);

			}else if(headC.getBoundingRectangle().contains(touchPointWorldCoords)){
				Gdx.app.log(TAG, CLASS_NAME + ".touchDown() :: Head C button pressed");

				if(!motorButtonsTouched[4] && !motorButtonsTouched[5]){
					motorButtonsTouched[6] = true;
					motorButtonsPointers[6] = pointer;

					event = new MotorEvent();
					event.setMotor(motor_t.RECENTER);
					event.setPower((byte)0x00);
					queue.addEvent(event);
				}
			}else{
				// TODO: Send input to the input handler system.
			}

			return true;
		}

		return false;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button){
		MotorEvent event;

		if(!Ouya.runningOnOuya){
			win2world.set(screenX, screenY, 0.0f);
			unitaryOrthoCamera.unproject(win2world);
			touchPointWorldCoords.set(win2world.x * Gdx.graphics.getWidth(), win2world.y * Gdx.graphics.getHeight());

			Gdx.app.log(TAG, CLASS_NAME + String.format(".touchUp(%d, %d, %d, %d)", screenX, screenY, pointer, button));
			Gdx.app.log(TAG, CLASS_NAME + String.format(".touchUp() :: Unprojected touch point: (%f, %f)", touchPointWorldCoords.x, touchPointWorldCoords.y));

			if(motorA.getBoundingRectangle().contains(touchPointWorldCoords)){
				Gdx.app.log(TAG, CLASS_NAME + ".touchUp() :: Motor A button released");

				motorButtonsPointers[0] = -1;
				motorButtonsTouched[0] = false;

				// Enqueue the event corresponding to releasing this button if the opposing button is not pressed already.
				if(!motorButtonsTouched[1]){
					event = new MotorEvent();
					event.setMotor(motor_t.MOTOR_A);
					event.setPower((byte) 0);
					queue.addEvent(event);
				}

			}else if(motorB.getBoundingRectangle().contains(touchPointWorldCoords)){
				Gdx.app.log(TAG, CLASS_NAME + ".touchUp() :: Motor B button released");

				motorButtonsPointers[1] = -1;
				motorButtonsTouched[1] = false;

				// Enqueue the event corresponding to releasing this button if the opposing button is not pressed already.
				if(!motorButtonsTouched[0]){
					event = new MotorEvent();
					event.setMotor(motor_t.MOTOR_A);
					event.setPower((byte) 0);
					queue.addEvent(event);
				}

			}else if(motorC.getBoundingRectangle().contains(touchPointWorldCoords)){
				Gdx.app.log(TAG, CLASS_NAME + ".touchUp() :: Motor C button released");

				motorButtonsPointers[2] = -1;
				motorButtonsTouched[2] = false;

				// Enqueue the event corresponding to releasing this button if the opposing button is not pressed already.
				if(!motorButtonsTouched[3]){
					event = new MotorEvent();
					event.setMotor(motor_t.MOTOR_C);
					event.setPower((byte) 0);
					queue.addEvent(event);
				}

			}else if(motorD.getBoundingRectangle().contains(touchPointWorldCoords)){
				Gdx.app.log(TAG, CLASS_NAME + ".touchUp() :: Motor D button released");

				motorButtonsPointers[3] = -1;
				motorButtonsTouched[3] = false;

				// Enqueue the event corresponding to releasing this button if the opposing button is not pressed already.
				if(!motorButtonsTouched[2]){
					event = new MotorEvent();
					event.setMotor(motor_t.MOTOR_C);
					event.setPower((byte) 0);
					queue.addEvent(event);
				}

			}else if(headA.getBoundingRectangle().contains(touchPointWorldCoords)){
				Gdx.app.log(TAG, CLASS_NAME + ".touchUp() :: Head A button released");

				motorButtonsPointers[4] = -1;
				motorButtonsTouched[4] = false;

				// Enqueue the event corresponding to releasing this button if the opposing button is not pressed already.
				if(!motorButtonsTouched[5]){
					event = new MotorEvent();
					event.setMotor(motor_t.MOTOR_B);
					event.setPower((byte) 0);
					queue.addEvent(event);
				}

			}else if(headB.getBoundingRectangle().contains(touchPointWorldCoords)){
				Gdx.app.log(TAG, CLASS_NAME + ".touchUp() :: Head B button released");

				motorButtonsPointers[5] = -1;
				motorButtonsTouched[5] = false;

				// Enqueue the event corresponding to releasing this button if the opposing button is not pressed already.
				if(!motorButtonsTouched[4]){
					event = new MotorEvent();
					event.setMotor(motor_t.MOTOR_B);
					event.setPower((byte) 0);
					queue.addEvent(event);
				}

			}else if(headC.getBoundingRectangle().contains(touchPointWorldCoords)){
				Gdx.app.log(TAG, CLASS_NAME + ".touchUp() :: Head C button released");

				motorButtonsPointers[6] = -1;
				motorButtonsTouched[6] = false;
			}else{
				// TODO: Pass input to the input handler system.
			}

			return true;
		}

		return false;
	}

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer){
		MotorEvent event;

		if(!Ouya.runningOnOuya){
			win2world.set(screenX, screenY, 0.0f);
			unitaryOrthoCamera.unproject(win2world);
			touchPointWorldCoords.set(win2world.x * Gdx.graphics.getWidth(), win2world.y * Gdx.graphics.getHeight());

			if(pointer == motorButtonsPointers[0] && !motorA.getBoundingRectangle().contains(touchPointWorldCoords)){
				Gdx.app.log(TAG, CLASS_NAME + ".touchDragged() :: Motor A button released");

				motorButtonsPointers[0] = -1;
				motorButtonsTouched[0] = false;

				// Enqueue the event corresponding to releasing this button if the opposing button is not pressed already.
				if(!motorButtonsTouched[1]){
					event = new MotorEvent();
					event.setMotor(motor_t.MOTOR_A);
					event.setPower((byte) 0);
					queue.addEvent(event);
				}

			}else if(pointer == motorButtonsPointers[1] && !motorB.getBoundingRectangle().contains(touchPointWorldCoords)){
				Gdx.app.log(TAG, CLASS_NAME + ".touchDragged() :: Motor B button released");

				motorButtonsPointers[1] = -1;
				motorButtonsTouched[1] = false;

				// Enqueue the event corresponding to releasing this button if the opposing button is not pressed already.
				if(!motorButtonsTouched[0]){
					event = new MotorEvent();
					event.setMotor(motor_t.MOTOR_A);
					event.setPower((byte) 0);
					queue.addEvent(event);
				}

			}else if(pointer == motorButtonsPointers[2] && !motorC.getBoundingRectangle().contains(touchPointWorldCoords)){
				Gdx.app.log(TAG, CLASS_NAME + ".touchDragged() :: Motor C button released");

				motorButtonsPointers[2] = -1;
				motorButtonsTouched[2] = false;

				// Enqueue the event corresponding to releasing this button if the opposing button is not pressed already.
				if(!motorButtonsTouched[3]){
					event = new MotorEvent();
					event.setMotor(motor_t.MOTOR_C);
					event.setPower((byte) 0);
					queue.addEvent(event);
				}

			}else if(pointer == motorButtonsPointers[3] && !motorD.getBoundingRectangle().contains(touchPointWorldCoords)){
				Gdx.app.log(TAG, CLASS_NAME + ".touchDragged() :: Motor D button released");

				motorButtonsPointers[3] = -1;
				motorButtonsTouched[3] = false;

				// Enqueue the event corresponding to releasing this button if the opposing button is not pressed already.
				if(!motorButtonsTouched[2]){
					event = new MotorEvent();
					event.setMotor(motor_t.MOTOR_C);
					event.setPower((byte) 0);
					queue.addEvent(event);
				}

			}else if(pointer == motorButtonsPointers[4] && !headA.getBoundingRectangle().contains(touchPointWorldCoords)){
				Gdx.app.log(TAG, CLASS_NAME + ".touchDragged() :: Head A button released");

				motorButtonsPointers[4] = -1;
				motorButtonsTouched[4] = false;

				// Enqueue the event corresponding to releasing this button if the opposing button is not pressed already.
				if(!motorButtonsTouched[5]){
					event = new MotorEvent();
					event.setMotor(motor_t.MOTOR_B);
					event.setPower((byte) 0);
					queue.addEvent(event);
				}

			}else if(pointer == motorButtonsPointers[5] && !headB.getBoundingRectangle().contains(touchPointWorldCoords)){
				Gdx.app.log(TAG, CLASS_NAME + ".touchDragged() :: Head B button released");

				motorButtonsPointers[5] = -1;
				motorButtonsTouched[5] = false;

				// Enqueue the event corresponding to releasing this button if the opposing button is not pressed already.
				if(!motorButtonsTouched[4]){
					event = new MotorEvent();
					event.setMotor(motor_t.MOTOR_B);
					event.setPower((byte) 0);
					queue.addEvent(event);
				}

			}else if(pointer == motorButtonsPointers[6] && !headC.getBoundingRectangle().contains(touchPointWorldCoords)){
				Gdx.app.log(TAG, CLASS_NAME + ".touchDragged() :: Head C button released");

				motorButtonsPointers[6] = -1;
				motorButtonsTouched[6] = false;
			}else{
				// TODO: Pass input to the input handler system.
			}

			return true;
		}

		return false;
	}

	@Override
	public boolean keyDown(int keycode){
		if(keycode == Input.Keys.BACK){
			core.nextState = game_states_t.MAIN_MENU;
			return true;
		}

		return false;
	}

	/*;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
	  ; CONTROLLER LISTENER METHODS ;
	  ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;*/

	@Override
	public boolean buttonDown(Controller controller, int buttonCode){
		MotorEvent event;

		if(stateActive){
			Gdx.app.log(TAG, CLASS_NAME + ".buttonDown() :: " + controller.getName() + " :: " + Integer.toString(buttonCode));

			if(buttonCode == Ouya.BUTTON_L1){
				motorGamepadButtonPressed[0] = true;

				if(!motorGamepadButtonPressed[4]){
					event = new MotorEvent();
					event.setMotor(motor_t.MOTOR_A);
					event.setPower((byte)-100);
					queue.addEvent(event);
				}

			}else if(buttonCode == Ouya.BUTTON_R1){
				motorGamepadButtonPressed[1] = true;

				if(!motorGamepadButtonPressed[5]){
					event = new MotorEvent();
					event.setMotor(motor_t.MOTOR_C);
					event.setPower((byte)-100);
					queue.addEvent(event);
				}

			}else if(buttonCode == Ouya.BUTTON_DPAD_LEFT){
				motorGamepadButtonPressed[2] = false;

				if(!motorGamepadButtonPressed[3]){
					event = new MotorEvent();
					event.setMotor(motor_t.MOTOR_B);
					event.setPower((byte)-40);
					queue.addEvent(event);
				}

			}else if(buttonCode == Ouya.BUTTON_DPAD_RIGHT){
				motorGamepadButtonPressed[3] = false;

				if(!motorGamepadButtonPressed[2]){
					event = new MotorEvent();
					event.setMotor(motor_t.MOTOR_B);
					event.setPower((byte)40);
					queue.addEvent(event);
				}

			}else if(buttonCode ==  Ouya.BUTTON_L2){
				motorGamepadButtonPressed[4] = false;

				if(!motorGamepadButtonPressed[0]){
					event = new MotorEvent();
					event.setMotor(motor_t.MOTOR_A);
					event.setPower((byte)100);
					queue.addEvent(event);
				}

			}else if(buttonCode ==  Ouya.BUTTON_R2){
				motorGamepadButtonPressed[5] = false;

				if(!motorGamepadButtonPressed[1]){
					event = new MotorEvent();
					event.setMotor(motor_t.MOTOR_C);
					event.setPower((byte)100);
					queue.addEvent(event);
				}

			}else if(buttonCode == Ouya.BUTTON_Y){
				motorGamepadButtonPressed[6] = true;

				event = new MotorEvent();
				event.setMotor(motor_t.RECENTER);
				event.setPower((byte)0x00);
				queue.addEvent(event);
			}else{
				// TODO: Pass input to the input handler system.
			}

			return true;
		}else{
			return false;
		}
	}

	@Override
	public boolean buttonUp(Controller controller, int buttonCode){
		MotorEvent event;

		if(stateActive){
			Gdx.app.log(TAG, CLASS_NAME + ".buttonDown() :: " + controller.getName() + " :: " + Integer.toString(buttonCode));

			if(buttonCode == Ouya.BUTTON_L1){
				motorGamepadButtonPressed[0] = false;

				if(!motorGamepadButtonPressed[4]){
					event = new MotorEvent();
					event.setMotor(motor_t.MOTOR_A);
					event.setPower((byte)0);
					queue.addEvent(event);
				}

			}else if(buttonCode == Ouya.BUTTON_R1){
				motorGamepadButtonPressed[1] = false;

				if(!motorGamepadButtonPressed[5]){
					event = new MotorEvent();
					event.setMotor(motor_t.MOTOR_C);
					event.setPower((byte)0);
					queue.addEvent(event);
				}

			}else if(buttonCode == Ouya.BUTTON_DPAD_LEFT){
				motorGamepadButtonPressed[2] = false;

				if(!motorGamepadButtonPressed[3]){
					event = new MotorEvent();
					event.setMotor(motor_t.MOTOR_B);
					event.setPower((byte)0);
					queue.addEvent(event);
				}
			}else if(buttonCode == Ouya.BUTTON_DPAD_RIGHT){
				motorGamepadButtonPressed[3] = false;

				if(!motorGamepadButtonPressed[2]){
					event = new MotorEvent();
					event.setMotor(motor_t.MOTOR_B);
					event.setPower((byte)0);
					queue.addEvent(event);
				}
			}else if(buttonCode ==  Ouya.BUTTON_L2){
				motorGamepadButtonPressed[4] = false;

				if(!motorGamepadButtonPressed[0]){
					event = new MotorEvent();
					event.setMotor(motor_t.MOTOR_A);
					event.setPower((byte)0);
					queue.addEvent(event);
				}

			}else if(buttonCode ==  Ouya.BUTTON_R2){
				motorGamepadButtonPressed[5] = false;

				if(!motorGamepadButtonPressed[1]){
					event = new MotorEvent();
					event.setMotor(motor_t.MOTOR_C);
					event.setPower((byte)0);
					queue.addEvent(event);
				}

			}else if(buttonCode == Ouya.BUTTON_Y){
				motorGamepadButtonPressed[6] = false;
			}else{
				// TODO: Pass input to the input handler system.
			}

			return true;
		}else{
			return false;
		}
	}

	@Override
	public boolean axisMoved(Controller controller, int axisCode, float value){
		// TODO: Pass input to the input handler system.
		return false;
	}
}
