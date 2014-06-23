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
import ve.ucv.ciens.ccg.nxtar.game.GameGlobals;
import ve.ucv.ciens.ccg.nxtar.graphics.CustomPerspectiveCamera;
import ve.ucv.ciens.ccg.nxtar.input.GamepadUserInput;
import ve.ucv.ciens.ccg.nxtar.input.KeyboardUserInput;
import ve.ucv.ciens.ccg.nxtar.input.TouchUserInput;
import ve.ucv.ciens.ccg.nxtar.input.UserInput;
import ve.ucv.ciens.ccg.nxtar.interfaces.ImageProcessor.MarkerData;
import ve.ucv.ciens.ccg.nxtar.network.monitors.MotorEventQueue;
import ve.ucv.ciens.ccg.nxtar.network.monitors.VideoFrameMonitor;
import ve.ucv.ciens.ccg.nxtar.systems.FadeEffectRenderingSystem;
import ve.ucv.ciens.ccg.nxtar.systems.MarkerPositioningSystem;
import ve.ucv.ciens.ccg.nxtar.systems.MarkerRenderingSystem;
import ve.ucv.ciens.ccg.nxtar.systems.ObjectRenderingSystem;
import ve.ucv.ciens.ccg.nxtar.systems.RobotArmPositioningSystem;
import ve.ucv.ciens.ccg.nxtar.utils.ProjectConstants;
import ve.ucv.ciens.ccg.nxtar.utils.Utils;

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
	//	private static final String  ALPHA_SHADER_PREFIX    = "shaders/alphaSprite/alpha";
	private static final float   NEAR                   = 0.01f;
	private static final float   FAR                    = 100.0f;

	/**
	 * <p>Represents the two possible control modes for the robot.</p>
	 */
	private enum robot_control_mode_t{
		WHEEL_CONTROL(0), ARM_CONTROL(1);

		private int value;

		private robot_control_mode_t(int value){
			this.value = value;
		}

		public int getValue(){
			return this.value;
		}
	}

	// Background related fields.
	private Sprite                          background;
	private float                           uScaling[];
	private Texture                         backgroundTexture;
	private ShaderProgram                   backgroundShader;

	// 3D rendering fields.
	private ModelBatch                      modelBatch;
	private FrameBuffer                     frameBuffer;
	private Sprite                          frameBufferSprite;

	// Game related fields.
	private World                           gameWorld;
	private MarkerRenderingSystem           markerRenderingSystem;
	private ObjectRenderingSystem           objectRenderingSystem;
	private RobotArmPositioningSystem       robotArmPositioningSystem;
	private FadeEffectRenderingSystem       fadeEffectRenderingSystem;
	private robot_control_mode_t            controlMode;

	// Cameras.
	private OrthographicCamera              unitaryOrthographicCamera;
	private OrthographicCamera              pixelPerfectOrthographicCamera;
	private CustomPerspectiveCamera         perspectiveCamera;

	// Video stream graphics.
	private Texture                         videoFrameTexture;
	private Sprite                          renderableVideoFrame;
	private Pixmap                          videoFrame;

	// Gui textures.
	private Texture                         upControlButtonTexture;
	private Texture                         downControlButtonTexture;
	private Texture                         leftControlButtonTexture;
	private Texture                         rightControlButtonTexture;
	private Texture                         headControlButtonTexture;
	private Texture                         wheelControlButtonTexture;
	private Texture                         armControlButtonTexture;
	private Texture                         correctAngleLedOnTexture;
	private Texture                         correctAngleLedOffTexture;
	private Texture                         orientationSliderTexture;

	// Gui renderable sprites.
	private Sprite                          motorAButton;
	private Sprite                          motorBButton;
	private Sprite                          motorCButton;
	private Sprite                          motorDButton;
	private Sprite                          armAButton;
	private Sprite                          armBButton;
	private Sprite                          armCButton;
	private Sprite                          armDButton;
	private Sprite                          headAButton;
	private Sprite                          headBButton;
	private Sprite                          headCButton;
	private Sprite                          wheelControlButton;
	private Sprite                          armControlButton;
	private Sprite                          correctAngleLedOnSprite;
	private Sprite                          correctAngleLedOffSprite;
	private Sprite                          orientationSlider;

	// Button touch helper fields.
	private boolean[]                       buttonsTouched;
	private int[]                           buttonPointers;
	private boolean[]                       gamepadButtonPressed;

	// Monitors.
	private VideoFrameMonitor               frameMonitor;
	private MotorEventQueue                 queue;

	public InGameState(final NxtARCore core) throws IllegalStateException, IllegalArgumentException{
		if(core == null)
			throw new IllegalArgumentException(CLASS_NAME + ": Core is null.");

		this.core = core;
		frameMonitor = VideoFrameMonitor.getInstance();
		queue = MotorEventQueue.getInstance();
		controlMode = robot_control_mode_t.WHEEL_CONTROL;

		// Set up rendering fields;
		videoFrame = null;

		// Set up the cameras.
		pixelPerfectOrthographicCamera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		unitaryOrthographicCamera = new OrthographicCamera(1.0f, Gdx.graphics.getHeight() / Gdx.graphics.getWidth());

		// Set up input handling support fields.
		win2world = new Vector3(0.0f, 0.0f, 0.0f);
		touchPointWorldCoords = new Vector2();

		buttonsTouched = new boolean[8];
		buttonsTouched[0] = false;
		buttonsTouched[1] = false;
		buttonsTouched[2] = false;
		buttonsTouched[3] = false;
		buttonsTouched[4] = false;
		buttonsTouched[5] = false;
		buttonsTouched[6] = false;
		buttonsTouched[7] = false;

		buttonPointers = new int[8];
		buttonPointers[0] = -1;
		buttonPointers[1] = -1;
		buttonPointers[2] = -1;
		buttonPointers[3] = -1;
		buttonPointers[4] = -1;
		buttonPointers[5] = -1;
		buttonPointers[6] = -1;
		buttonPointers[7] = -1;

		gamepadButtonPressed = new boolean[7];
		gamepadButtonPressed[0] = false;
		gamepadButtonPressed[1] = false;
		gamepadButtonPressed[2] = false;
		gamepadButtonPressed[3] = false;
		gamepadButtonPressed[4] = false;
		gamepadButtonPressed[5] = false;
		gamepadButtonPressed[6] = false;

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
			Gdx.app.error(TAG, CLASS_NAME + ".InGameState() :: Failed to compile the background shader.");
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

		// Set up he buttons.
		if(!Ouya.runningOnOuya)
			setUpButtons();

		// Set up the game world.
		gameWorld = GameGlobals.getGameWorld();

		robotArmPositioningSystem = gameWorld.getSystem(RobotArmPositioningSystem.class);
		markerRenderingSystem     = gameWorld.getSystem(MarkerRenderingSystem.class);
		objectRenderingSystem     = gameWorld.getSystem(ObjectRenderingSystem.class);
		fadeEffectRenderingSystem = gameWorld.getSystem(FadeEffectRenderingSystem.class);

		if(robotArmPositioningSystem == null || markerRenderingSystem == null || objectRenderingSystem == null || fadeEffectRenderingSystem == null)
			throw new IllegalStateException("One or more essential systems are null.");
	}

	/*;;;;;;;;;;;;;;;;;;;;;;
	  ; BASE STATE METHODS ;
	  ;;;;;;;;;;;;;;;;;;;;;;*/

	@Override
	public void render(float delta){
		final float MIN_SLIDER_X = correctAngleLedOnSprite != null ? -(Utils.getScreenWidth() / 2) + 5 + correctAngleLedOnSprite.getWidth() : -(Utils.getScreenWidth() / 2) + 5;
		final float MAX_SLIDER_X = correctAngleLedOnSprite != null ? (Utils.getScreenWidth() / 2) - 5 - correctAngleLedOnSprite.getWidth(): (Utils.getScreenWidth() / 2) - 5;
		int w, h;
		float t, xSliderPos;
		byte[] frame;
		MarkerData data;
		TextureRegion region;
		float focalPointX, focalPointY, cameraCenterX, cameraCenterY;

		// Clear the screen.
		Gdx.gl.glClearColor(1, 1, 1, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		// Render the background.
		core.batch.setProjectionMatrix(pixelPerfectOrthographicCamera.combined);
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

				if(controlMode.getValue() == robot_control_mode_t.ARM_CONTROL.getValue() || Ouya.runningOnOuya){
					objectRenderingSystem.begin(perspectiveCamera);
					objectRenderingSystem.process();
					objectRenderingSystem.end();
				}
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
				renderableVideoFrame.setSize(xSize * ProjectConstants.OVERSCAN, Utils.getScreenHeight());
				renderableVideoFrame.rotate90(true);
				renderableVideoFrame.translate(-renderableVideoFrame.getWidth() / 2, -renderableVideoFrame.getHeight() / 2);

				frameBufferSprite.setSize(xSize * ProjectConstants.OVERSCAN, Utils.getScreenHeight());
				frameBufferSprite.rotate90(true);
				frameBufferSprite.translate(-frameBufferSprite.getWidth() / 2, -frameBufferSprite.getHeight() / 2);
			}

			// Set the correct camera for the device.
			if(!Ouya.runningOnOuya){
				core.batch.setProjectionMatrix(unitaryOrthographicCamera.combined);
			}else{
				core.batch.setProjectionMatrix(pixelPerfectOrthographicCamera.combined);
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
			core.batch.setProjectionMatrix(pixelPerfectOrthographicCamera.combined);
			core.batch.begin();{
				// Draw control mode button.
				if(controlMode.getValue() == robot_control_mode_t.WHEEL_CONTROL.getValue()){
					// Draw motor control buttons.
					motorAButton.draw(core.batch);
					motorBButton.draw(core.batch);
					motorCButton.draw(core.batch);
					motorDButton.draw(core.batch);
					wheelControlButton.draw(core.batch);
				}else if(controlMode.getValue() == robot_control_mode_t.ARM_CONTROL.getValue()){
					// Draw arm control buttons.
					armAButton.draw(core.batch);
					armBButton.draw(core.batch);
					armCButton.draw(core.batch);
					armDButton.draw(core.batch);
					armControlButton.draw(core.batch);
				}else{
					throw new IllegalStateException("Unrecognized control mode: " + Integer.toString(controlMode.getValue()));
				}

				headAButton.draw(core.batch);
				headBButton.draw(core.batch);
				headCButton.draw(core.batch);

				// Draw device rotation led.
				if(Utils.isDeviceRollValid()){
					if(Math.abs(Gdx.input.getRoll()) < ProjectConstants.MAX_ABS_ROLL)
						correctAngleLedOnSprite.draw(core.batch);
					else
						correctAngleLedOffSprite.draw(core.batch);

					t = (Gdx.input.getRoll() + 60.0f) / 120.0f;
					xSliderPos = (MIN_SLIDER_X * t) + (MAX_SLIDER_X * (1.0f - t));
					xSliderPos = xSliderPos < MIN_SLIDER_X ? MIN_SLIDER_X : (xSliderPos > MAX_SLIDER_X ? MAX_SLIDER_X : xSliderPos);
					orientationSlider.setPosition(xSliderPos, orientationSlider.getY());
					orientationSlider.draw(core.batch);
				}else{
					correctAngleLedOffSprite.draw(core.batch);
					orientationSlider.draw(core.batch);
				}
			}core.batch.end();
		}

		fadeEffectRenderingSystem.process();

		data = null;
	}

	@Override
	public void dispose(){
		if(modelBatch != null)
			modelBatch.dispose();

		if(videoFrameTexture != null)
			videoFrameTexture.dispose();

		if(upControlButtonTexture != null)
			upControlButtonTexture.dispose();

		if(downControlButtonTexture != null)
			downControlButtonTexture.dispose();

		if(leftControlButtonTexture != null)
			leftControlButtonTexture.dispose();

		if(rightControlButtonTexture != null)
			rightControlButtonTexture.dispose();

		if(headControlButtonTexture != null)
			headControlButtonTexture.dispose();

		if(wheelControlButtonTexture != null)
			wheelControlButtonTexture.dispose();

		if(armControlButtonTexture != null)
			armControlButtonTexture.dispose();

		if(backgroundTexture != null)
			backgroundTexture.dispose();

		if(orientationSliderTexture != null)
			orientationSliderTexture.dispose();

		if(backgroundShader != null)
			backgroundShader.dispose();

		if(frameBuffer != null)
			frameBuffer.dispose();

		if(correctAngleLedOffTexture != null)
			correctAngleLedOffTexture.dispose();

		if(correctAngleLedOnTexture != null)
			correctAngleLedOnTexture.dispose();
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
		// Set the main control buttons.
		upControlButtonTexture = new Texture(Gdx.files.internal("data/gfx/gui/up_button.png"));
		downControlButtonTexture = new Texture(Gdx.files.internal("data/gfx/gui/down_button.png"));
		leftControlButtonTexture = new Texture(Gdx.files.internal("data/gfx/gui/left_button.png"));
		rightControlButtonTexture = new Texture(Gdx.files.internal("data/gfx/gui/right_button.png"));

		// Set the motor control buttons.
		motorAButton = new Sprite(upControlButtonTexture);
		motorAButton.setSize(motorAButton.getWidth() * 0.7f, motorAButton.getHeight() * 0.7f);

		motorBButton = new Sprite(downControlButtonTexture);
		motorBButton.setSize(motorBButton.getWidth() * 0.7f, motorBButton.getHeight() * 0.7f);

		motorCButton = new Sprite(downControlButtonTexture);
		motorCButton.setSize(motorCButton.getWidth() * 0.7f, motorCButton.getHeight() * 0.7f);

		motorDButton = new Sprite(upControlButtonTexture);
		motorDButton.setSize(motorDButton.getWidth() * 0.7f, motorDButton.getHeight() * 0.7f);

		motorAButton.setPosition(-(Gdx.graphics.getWidth() / 2) + 10, -(Gdx.graphics.getHeight() / 2) + motorBButton.getHeight() + 20);
		motorBButton.setPosition(-(Gdx.graphics.getWidth() / 2) + 20 + (motorAButton.getWidth() / 2), -(Gdx.graphics.getHeight() / 2) + 10);
		motorCButton.setPosition((Gdx.graphics.getWidth() / 2) - (1.5f * (motorDButton.getWidth())) - 20, -(Gdx.graphics.getHeight() / 2) + 10);
		motorDButton.setPosition((Gdx.graphics.getWidth() / 2) - motorDButton.getWidth() - 10, -(Gdx.graphics.getHeight() / 2) + 20 + motorCButton.getHeight());

		// Set up robot arm control buttons.
		armAButton = new Sprite(upControlButtonTexture);
		armAButton.setSize(armAButton.getWidth() * 0.7f, armAButton.getHeight() * 0.7f);

		armBButton = new Sprite(leftControlButtonTexture);
		armBButton.setSize(armBButton.getWidth() * 0.7f, armBButton.getHeight() * 0.7f);

		armCButton = new Sprite(rightControlButtonTexture);
		armCButton.setSize(armCButton.getWidth() * 0.7f, armCButton.getHeight() * 0.7f);

		armDButton = new Sprite(downControlButtonTexture);
		armDButton.setSize(armDButton.getWidth() * 0.7f, armDButton.getHeight() * 0.7f);

		armAButton.setPosition(-(Gdx.graphics.getWidth() / 2) + 10, -(Gdx.graphics.getHeight() / 2) + armBButton.getHeight() + 20);
		armBButton.setPosition(-(Gdx.graphics.getWidth() / 2) + 20 + (armAButton.getWidth() / 2), -(Gdx.graphics.getHeight() / 2) + 10);
		armCButton.setPosition((Gdx.graphics.getWidth() / 2) - (1.5f * (armDButton.getWidth())) - 20, -(Gdx.graphics.getHeight() / 2) + 10);
		armDButton.setPosition((Gdx.graphics.getWidth() / 2) - armDButton.getWidth() - 10, -(Gdx.graphics.getHeight() / 2) + 20 + armCButton.getHeight());

		// Set the head control buttons.
		headControlButtonTexture = new Texture(Gdx.files.internal("data/gfx/gui/orange_glowy_button.png"));

		headAButton = new Sprite(headControlButtonTexture);
		headAButton.setSize(headAButton.getWidth() * 0.3f, headAButton.getHeight() * 0.6f);
		headBButton = new Sprite(headControlButtonTexture);
		headBButton.setSize(headBButton.getWidth() * 0.3f, headBButton.getHeight() * 0.6f);

		headAButton.setPosition(-headAButton.getWidth() - 10, motorAButton.getY() + (headAButton.getHeight() / 2));
		headBButton.setPosition(10, motorAButton.getY() + (headAButton.getHeight() / 2));

		headCButton = new Sprite(headControlButtonTexture);
		headCButton.setSize(headCButton.getWidth() * 0.3f, headCButton.getHeight() * 0.6f);
		headCButton.setPosition(-(headCButton.getWidth() / 2), headAButton.getY() - headAButton.getHeight() - 10);

		// Set the control mode buttons.
		wheelControlButtonTexture = new Texture(Gdx.files.internal("data/gfx/gui/wheel.png"));
		armControlButtonTexture   = new Texture(Gdx.files.internal("data/gfx/gui/arm.png"));

		wheelControlButton = new Sprite(wheelControlButtonTexture);
		wheelControlButton.setSize(wheelControlButton.getWidth() * 0.3f, wheelControlButton.getHeight() * 0.3f);

		armControlButton = new Sprite(armControlButtonTexture);
		armControlButton.setSize(armControlButton.getWidth() * 0.3f, armControlButton.getHeight() * 0.3f);

		wheelControlButton.setPosition(-(wheelControlButton.getWidth() / 2), headCButton.getY() - headCButton.getHeight() - 15);
		armControlButton.setPosition(-(armControlButton.getWidth() / 2), headCButton.getY() - headCButton.getHeight() - 15);

		// Set up the correct angle leds.
		correctAngleLedOnTexture  = new Texture(Gdx.files.internal("data/gfx/gui/Anonymous_Button_Green.png"));
		correctAngleLedOffTexture = new Texture(Gdx.files.internal("data/gfx/gui/Anonymous_Button_Red.png"));

		correctAngleLedOnSprite = new Sprite(correctAngleLedOnTexture);
		correctAngleLedOffSprite = new Sprite(correctAngleLedOffTexture);

		correctAngleLedOnSprite.setSize(correctAngleLedOnSprite.getWidth() * 0.25f, correctAngleLedOnSprite.getHeight() * 0.25f);
		correctAngleLedOffSprite.setSize(correctAngleLedOffSprite.getWidth() * 0.25f, correctAngleLedOffSprite.getHeight() * 0.25f);

		correctAngleLedOnSprite.setPosition((Gdx.graphics.getWidth() / 2) - correctAngleLedOnSprite.getWidth() - 5, (Gdx.graphics.getHeight() / 2) - correctAngleLedOnSprite.getHeight() - 5);
		correctAngleLedOffSprite.setPosition((Gdx.graphics.getWidth() / 2) - correctAngleLedOffSprite.getWidth() - 5, (Gdx.graphics.getHeight() / 2) - correctAngleLedOffSprite.getHeight() - 5);

		// Set up orientation slider.
		orientationSliderTexture = new Texture(Gdx.files.internal("data/gfx/gui/slider_black.png"));
		orientationSlider = new Sprite(orientationSliderTexture);
		orientationSlider.setSize(orientationSlider.getWidth() * 0.25f, orientationSlider.getHeight() * 0.25f);
		orientationSlider.setPosition(-(orientationSlider.getWidth() / 2), (Utils.getScreenHeight() / 2) - orientationSlider.getHeight() - 5);
	}

	/*;;;;;;;;;;;;;;;;;;;;;;;;;;;
	  ; INPUT PROCESSOR METHODS ;
	  ;;;;;;;;;;;;;;;;;;;;;;;;;;;*/

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button){
		MotorEvent event;
		UserInput input;

		if(!Ouya.runningOnOuya){
			win2world.set(screenX, screenY, 0.0f);
			unitaryOrthographicCamera.unproject(win2world);
			touchPointWorldCoords.set(win2world.x * Gdx.graphics.getWidth(), win2world.y * Gdx.graphics.getHeight());

			Gdx.app.log(TAG, CLASS_NAME + String.format(".touchDown(%d, %d, %d, %d)", screenX, screenY, pointer, button));
			Gdx.app.log(TAG, CLASS_NAME + String.format(".touchDown() :: Unprojected touch point: (%f, %f)", touchPointWorldCoords.x, touchPointWorldCoords.y));

			if(motorAButton.getBoundingRectangle().contains(touchPointWorldCoords)){
				Gdx.app.log(TAG, CLASS_NAME + ".touchDown() :: Motor A button pressed");

				buttonsTouched[0] = true;
				buttonPointers[0] = pointer;

				if(controlMode.getValue() == robot_control_mode_t.WHEEL_CONTROL.getValue()){
					event = new MotorEvent();
					event.setMotor(motor_t.MOTOR_A);
					event.setPower((byte)100);
					queue.addEvent(event);

				}else if(controlMode.getValue() == robot_control_mode_t.ARM_CONTROL.getValue()){
					input = new KeyboardUserInput();

					((KeyboardUserInput)input).keyUp = true;
					robotArmPositioningSystem.setUserInput(input);
					robotArmPositioningSystem.process();
				}

			}else if(motorBButton.getBoundingRectangle().contains(touchPointWorldCoords)){
				Gdx.app.log(TAG, CLASS_NAME + ".touchDown() :: Motor B button pressed");

				buttonsTouched[1] = true;
				buttonPointers[1] = pointer;

				if(controlMode.getValue() == robot_control_mode_t.WHEEL_CONTROL.getValue()){
					event = new MotorEvent();
					event.setMotor(motor_t.MOTOR_A);
					event.setPower((byte)-100);
					queue.addEvent(event);

				}else if(controlMode.getValue() == robot_control_mode_t.ARM_CONTROL.getValue()){
					input = new KeyboardUserInput();
					((KeyboardUserInput)input).keyLeft = true;
					robotArmPositioningSystem.setUserInput(input);
					robotArmPositioningSystem.process();
				}

			}else if(motorCButton.getBoundingRectangle().contains(touchPointWorldCoords)){
				Gdx.app.log(TAG, CLASS_NAME + ".touchDown() :: Motor C button pressed");

				buttonsTouched[2] = true;
				buttonPointers[2] = pointer;

				if(controlMode.getValue() == robot_control_mode_t.WHEEL_CONTROL.getValue()){
					event = new MotorEvent();
					event.setMotor(motor_t.MOTOR_C);
					event.setPower((byte)-100);
					queue.addEvent(event);

				}else if(controlMode.getValue() == robot_control_mode_t.ARM_CONTROL.getValue()){
					input = new KeyboardUserInput();
					((KeyboardUserInput)input).keyRight = true;
					robotArmPositioningSystem.setUserInput(input);
					robotArmPositioningSystem.process();
				}

			}else if(motorDButton.getBoundingRectangle().contains(touchPointWorldCoords)){
				Gdx.app.log(TAG, CLASS_NAME + ".touchDown() :: Motor D button pressed");

				buttonsTouched[3] = true;
				buttonPointers[3] = pointer;

				if(controlMode.getValue() == robot_control_mode_t.WHEEL_CONTROL.getValue()){
					event = new MotorEvent();
					event.setMotor(motor_t.MOTOR_C);
					event.setPower((byte)100);
					queue.addEvent(event);

				}else if(controlMode.getValue() == robot_control_mode_t.ARM_CONTROL.getValue()){
					input = new KeyboardUserInput();

					((KeyboardUserInput)input).keyDown = true;
					robotArmPositioningSystem.setUserInput(input);
					robotArmPositioningSystem.process();
				}

			}else if(headAButton.getBoundingRectangle().contains(touchPointWorldCoords)){
				Gdx.app.log(TAG, CLASS_NAME + ".touchDown() :: Head A button pressed");

				buttonsTouched[4] = true;
				buttonPointers[4] = pointer;

				event = new MotorEvent();
				event.setMotor(motor_t.MOTOR_B);
				event.setPower((byte)-25);
				queue.addEvent(event);

			}else if(headBButton.getBoundingRectangle().contains(touchPointWorldCoords)){
				Gdx.app.log(TAG, CLASS_NAME + ".touchDown() :: Head B button pressed");

				buttonsTouched[5] = true;
				buttonPointers[5] = pointer;

				event = new MotorEvent();
				event.setMotor(motor_t.MOTOR_B);
				event.setPower((byte)25);
				queue.addEvent(event);

			}else if(headCButton.getBoundingRectangle().contains(touchPointWorldCoords)){
				Gdx.app.log(TAG, CLASS_NAME + ".touchDown() :: Head C button pressed");

				if(!buttonsTouched[4] && !buttonsTouched[5]){
					buttonsTouched[6] = true;
					buttonPointers[6] = pointer;

					event = new MotorEvent();
					event.setMotor(motor_t.RECENTER);
					event.setPower((byte)0x00);
					queue.addEvent(event);
				}

			}else if(wheelControlButton.getBoundingRectangle().contains(touchPointWorldCoords) || armControlButton.getBoundingRectangle().contains(touchPointWorldCoords)){

				buttonsTouched[7] = true;
				buttonPointers[7] = pointer;
				controlMode = controlMode.getValue() == robot_control_mode_t.WHEEL_CONTROL.getValue() ? robot_control_mode_t.ARM_CONTROL : robot_control_mode_t.WHEEL_CONTROL;

			}else{

				touchPointWorldCoords.set(win2world.x, win2world.y);

				if(frameBufferSprite != null && frameBufferSprite.getBoundingRectangle().contains(touchPointWorldCoords)){
					Gdx.app.log(TAG, CLASS_NAME + "touchDown(): Touch point inside framebuffer.");
					input = new TouchUserInput();
					robotArmPositioningSystem.setUserInput(input);

				}else{
					Gdx.app.log(TAG, CLASS_NAME + "touchDown(): Touch point outside framebuffer.");
				}
			}

			return true;
		}

		return false;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button){
		MotorEvent event;
		UserInput input;

		if(!Ouya.runningOnOuya){
			win2world.set(screenX, screenY, 0.0f);
			unitaryOrthographicCamera.unproject(win2world);
			touchPointWorldCoords.set(win2world.x * Gdx.graphics.getWidth(), win2world.y * Gdx.graphics.getHeight());

			Gdx.app.log(TAG, CLASS_NAME + String.format(".touchUp(%d, %d, %d, %d)", screenX, screenY, pointer, button));
			Gdx.app.log(TAG, CLASS_NAME + String.format(".touchUp() :: Unprojected touch point: (%f, %f)", touchPointWorldCoords.x, touchPointWorldCoords.y));

			if(motorAButton.getBoundingRectangle().contains(touchPointWorldCoords)){
				Gdx.app.log(TAG, CLASS_NAME + ".touchUp() :: Motor A button released");

				buttonPointers[0] = -1;
				buttonsTouched[0] = false;

				if(controlMode.getValue() == robot_control_mode_t.WHEEL_CONTROL.getValue()){
					// Enqueue the event corresponding to releasing this button if the opposing button is not pressed already.
					if(!buttonsTouched[1]){
						event = new MotorEvent();
						event.setMotor(motor_t.MOTOR_A);
						event.setPower((byte) 0);
						queue.addEvent(event);
					}

				}else if(controlMode.getValue() == robot_control_mode_t.ARM_CONTROL.getValue()){
					input = new KeyboardUserInput();
					((KeyboardUserInput)input).keyUp = false;
					robotArmPositioningSystem.setUserInput(input);
					robotArmPositioningSystem.process();
				}

			}else if(motorBButton.getBoundingRectangle().contains(touchPointWorldCoords)){
				Gdx.app.log(TAG, CLASS_NAME + ".touchUp() :: Motor B button released");

				buttonPointers[1] = -1;
				buttonsTouched[1] = false;

				if(controlMode.getValue() == robot_control_mode_t.WHEEL_CONTROL.getValue()){
					// Enqueue the event corresponding to releasing this button if the opposing button is not pressed already.
					if(!buttonsTouched[0]){
						event = new MotorEvent();
						event.setMotor(motor_t.MOTOR_A);
						event.setPower((byte) 0);
						queue.addEvent(event);
					}

				}else if(controlMode.getValue() == robot_control_mode_t.ARM_CONTROL.getValue()){
					input = new KeyboardUserInput();
					((KeyboardUserInput)input).keyLeft = false;
					robotArmPositioningSystem.setUserInput(input);
					robotArmPositioningSystem.process();
				}

			}else if(motorCButton.getBoundingRectangle().contains(touchPointWorldCoords)){
				Gdx.app.log(TAG, CLASS_NAME + ".touchUp() :: Motor C button released");

				buttonPointers[2] = -1;
				buttonsTouched[2] = false;

				if(controlMode.getValue() == robot_control_mode_t.WHEEL_CONTROL.getValue()){
					// Enqueue the event corresponding to releasing this button if the opposing button is not pressed already.
					if(!buttonsTouched[3]){
						event = new MotorEvent();
						event.setMotor(motor_t.MOTOR_C);
						event.setPower((byte) 0);
						queue.addEvent(event);
					}

				}else if(controlMode.getValue() == robot_control_mode_t.ARM_CONTROL.getValue()){
					input = new KeyboardUserInput();
					((KeyboardUserInput)input).keyRight = false;
					robotArmPositioningSystem.setUserInput(input);
					robotArmPositioningSystem.process();
				}

			}else if(motorDButton.getBoundingRectangle().contains(touchPointWorldCoords)){
				Gdx.app.log(TAG, CLASS_NAME + ".touchUp() :: Motor D button released");

				buttonPointers[3] = -1;
				buttonsTouched[3] = false;

				if(controlMode.getValue() == robot_control_mode_t.WHEEL_CONTROL.getValue()){
					// Enqueue the event corresponding to releasing this button if the opposing button is not pressed already.
					if(!buttonsTouched[2]){
						event = new MotorEvent();
						event.setMotor(motor_t.MOTOR_C);
						event.setPower((byte) 0);
						queue.addEvent(event);
					}

				}else if(controlMode.getValue() == robot_control_mode_t.ARM_CONTROL.getValue()){
					input = new KeyboardUserInput();
					((KeyboardUserInput)input).keyDown = false;
					robotArmPositioningSystem.setUserInput(input);
					robotArmPositioningSystem.process();
				}

			}else if(headAButton.getBoundingRectangle().contains(touchPointWorldCoords)){
				Gdx.app.log(TAG, CLASS_NAME + ".touchUp() :: Head A button released");

				buttonPointers[4] = -1;
				buttonsTouched[4] = false;

				// Enqueue the event corresponding to releasing this button if the opposing button is not pressed already.
				if(!buttonsTouched[5]){
					event = new MotorEvent();
					event.setMotor(motor_t.MOTOR_B);
					event.setPower((byte) 0);
					queue.addEvent(event);
				}

			}else if(headBButton.getBoundingRectangle().contains(touchPointWorldCoords)){
				Gdx.app.log(TAG, CLASS_NAME + ".touchUp() :: Head B button released");

				buttonPointers[5] = -1;
				buttonsTouched[5] = false;

				// Enqueue the event corresponding to releasing this button if the opposing button is not pressed already.
				if(!buttonsTouched[4]){
					event = new MotorEvent();
					event.setMotor(motor_t.MOTOR_B);
					event.setPower((byte) 0);
					queue.addEvent(event);
				}

			}else if(headCButton.getBoundingRectangle().contains(touchPointWorldCoords)){
				Gdx.app.log(TAG, CLASS_NAME + ".touchUp() :: Head C button released");

				buttonPointers[6] = -1;
				buttonsTouched[6] = false;

			}else if(wheelControlButton.getBoundingRectangle().contains(touchPointWorldCoords) || armControlButton.getBoundingRectangle().contains(touchPointWorldCoords)){
				buttonPointers[7] = -1;
				buttonsTouched[7] = false;
			}

			return true;
		}

		return false;
	}

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer){
		MotorEvent event;
		UserInput input;

		if(!Ouya.runningOnOuya){
			win2world.set(screenX, screenY, 0.0f);
			unitaryOrthographicCamera.unproject(win2world);
			touchPointWorldCoords.set(win2world.x * Gdx.graphics.getWidth(), win2world.y * Gdx.graphics.getHeight());

			if(pointer == buttonPointers[0] && !motorAButton.getBoundingRectangle().contains(touchPointWorldCoords)){
				Gdx.app.log(TAG, CLASS_NAME + ".touchDragged() :: Motor A button released");

				buttonPointers[0] = -1;
				buttonsTouched[0] = false;

				if(controlMode.getValue() == robot_control_mode_t.WHEEL_CONTROL.getValue()){
					// Enqueue the event corresponding to releasing this button if the opposing button is not pressed already.
					if(!buttonsTouched[1]){
						event = new MotorEvent();
						event.setMotor(motor_t.MOTOR_A);
						event.setPower((byte) 0);
						queue.addEvent(event);
					}

				}else if(controlMode.getValue() == robot_control_mode_t.ARM_CONTROL.getValue()){
					input = new KeyboardUserInput();
					((KeyboardUserInput)input).keyUp = false;
					robotArmPositioningSystem.setUserInput(input);
					robotArmPositioningSystem.process();
				}

			}else if(pointer == buttonPointers[1] && !motorBButton.getBoundingRectangle().contains(touchPointWorldCoords)){
				Gdx.app.log(TAG, CLASS_NAME + ".touchDragged() :: Motor B button released");

				buttonPointers[1] = -1;
				buttonsTouched[1] = false;

				if(controlMode.getValue() == robot_control_mode_t.WHEEL_CONTROL.getValue()){
					// Enqueue the event corresponding to releasing this button if the opposing button is not pressed already.
					if(!buttonsTouched[0]){
						event = new MotorEvent();
						event.setMotor(motor_t.MOTOR_A);
						event.setPower((byte) 0);
						queue.addEvent(event);
					}

				}else if(controlMode.getValue() == robot_control_mode_t.ARM_CONTROL.getValue()){
					input = new KeyboardUserInput();
					((KeyboardUserInput)input).keyLeft = false;
					robotArmPositioningSystem.setUserInput(input);
					robotArmPositioningSystem.process();
				}

			}else if(pointer == buttonPointers[2] && !motorCButton.getBoundingRectangle().contains(touchPointWorldCoords)){
				Gdx.app.log(TAG, CLASS_NAME + ".touchDragged() :: Motor C button released");

				buttonPointers[2] = -1;
				buttonsTouched[2] = false;

				if(controlMode.getValue() == robot_control_mode_t.WHEEL_CONTROL.getValue()){
					// Enqueue the event corresponding to releasing this button if the opposing button is not pressed already.
					if(!buttonsTouched[3]){
						event = new MotorEvent();
						event.setMotor(motor_t.MOTOR_C);
						event.setPower((byte) 0);
						queue.addEvent(event);
					}

				}else if(controlMode.getValue() == robot_control_mode_t.ARM_CONTROL.getValue()){
					input = new KeyboardUserInput();
					((KeyboardUserInput)input).keyRight = false;
					robotArmPositioningSystem.setUserInput(input);
					robotArmPositioningSystem.process();
				}

			}else if(pointer == buttonPointers[3] && !motorDButton.getBoundingRectangle().contains(touchPointWorldCoords)){
				Gdx.app.log(TAG, CLASS_NAME + ".touchDragged() :: Motor D button released");

				buttonPointers[3] = -1;
				buttonsTouched[3] = false;

				if(controlMode.getValue() == robot_control_mode_t.WHEEL_CONTROL.getValue()){
					// Enqueue the event corresponding to releasing this button if the opposing button is not pressed already.
					if(!buttonsTouched[2]){
						event = new MotorEvent();
						event.setMotor(motor_t.MOTOR_C);
						event.setPower((byte) 0);
						queue.addEvent(event);
					}

				}else if(controlMode.getValue() == robot_control_mode_t.ARM_CONTROL.getValue()){
					input = new KeyboardUserInput();
					((KeyboardUserInput)input).keyDown = false;
					robotArmPositioningSystem.setUserInput(input);
					robotArmPositioningSystem.process();
				}

			}else if(pointer == buttonPointers[4] && !headAButton.getBoundingRectangle().contains(touchPointWorldCoords)){
				Gdx.app.log(TAG, CLASS_NAME + ".touchDragged() :: Head A button released");

				buttonPointers[4] = -1;
				buttonsTouched[4] = false;

				// Enqueue the event corresponding to releasing this button if the opposing button is not pressed already.
				if(!buttonsTouched[5]){
					event = new MotorEvent();
					event.setMotor(motor_t.MOTOR_B);
					event.setPower((byte) 0);
					queue.addEvent(event);
				}

			}else if(pointer == buttonPointers[5] && !headBButton.getBoundingRectangle().contains(touchPointWorldCoords)){
				Gdx.app.log(TAG, CLASS_NAME + ".touchDragged() :: Head B button released");

				buttonPointers[5] = -1;
				buttonsTouched[5] = false;

				// Enqueue the event corresponding to releasing this button if the opposing button is not pressed already.
				if(!buttonsTouched[4]){
					event = new MotorEvent();
					event.setMotor(motor_t.MOTOR_B);
					event.setPower((byte) 0);
					queue.addEvent(event);
				}

			}else if(pointer == buttonPointers[6] && !headCButton.getBoundingRectangle().contains(touchPointWorldCoords)){
				Gdx.app.log(TAG, CLASS_NAME + ".touchDragged() :: Head C button released");

				buttonPointers[6] = -1;
				buttonsTouched[6] = false;

			}else if(pointer == buttonPointers[7] && !(wheelControlButton.getBoundingRectangle().contains(touchPointWorldCoords) || armControlButton.getBoundingRectangle().contains(touchPointWorldCoords))){
				buttonPointers[7] = -1;
				buttonsTouched[7] = false;
			}

			return true;
		}

		return false;
	}

	@Override
	public boolean keyDown(int keycode){
		KeyboardUserInput input = null;

		if(keycode == Input.Keys.BACK){
			core.nextState = game_states_t.MAIN_MENU;
			return true;
		}

		switch(keycode){
		case Input.Keys.LEFT:
			input = new KeyboardUserInput();
			input.keyLeft = true;
			break;
		case Input.Keys.RIGHT:
			input = new KeyboardUserInput();
			input.keyRight = true;
			break;
		case Input.Keys.UP:
			input = new KeyboardUserInput();
			input.keyUp = true;
			break;
		case Input.Keys.DOWN: 
			input = new KeyboardUserInput();
			input.keyDown = true;
			break;
		case Input.Keys.SPACE: 
			input = new KeyboardUserInput();
			input.keySpace = true;
			break;
		default: 
			return false;
		}

		if(input != null){
			robotArmPositioningSystem.setUserInput(input);
			robotArmPositioningSystem.process();
		}

		return true;
	}

	@Override
	public boolean keyUp(int keycode){
		KeyboardUserInput input = null;

		if(keycode == Input.Keys.BACK){
			core.nextState = game_states_t.MAIN_MENU;
			return true;
		}

		switch(keycode){
		case Input.Keys.LEFT:
			input = new KeyboardUserInput();
			input.keyLeft = false;
			break;
		case Input.Keys.RIGHT:
			input = new KeyboardUserInput();
			input.keyRight = false;
			break;
		case Input.Keys.UP:
			input = new KeyboardUserInput();
			input.keyUp = false;
			break;
		case Input.Keys.DOWN: 
			input = new KeyboardUserInput();
			input.keyDown = false;
			break;
		case Input.Keys.SPACE: 
			input = new KeyboardUserInput();
			input.keySpace = false;
			break;
		default: 
			return false;
		}

		if(input != null){
			robotArmPositioningSystem.setUserInput(input);
			robotArmPositioningSystem.process();
		}

		return true;
	}

	/*;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
	  ; CONTROLLER LISTENER METHODS ;
	  ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;*/

	@Override
	public boolean buttonDown(Controller controller, int buttonCode){
		MotorEvent       event;
		GamepadUserInput userInput;

		if(stateActive){
			Gdx.app.log(TAG, CLASS_NAME + ".buttonDown() :: " + controller.getName() + " :: " + Integer.toString(buttonCode));

			if(buttonCode == Ouya.BUTTON_L1){
				gamepadButtonPressed[0] = true;

				if(!gamepadButtonPressed[4]){
					event = new MotorEvent();
					event.setMotor(motor_t.MOTOR_A);
					event.setPower((byte)-100);
					queue.addEvent(event);
				}

			}else if(buttonCode == Ouya.BUTTON_R1){
				gamepadButtonPressed[1] = true;

				if(!gamepadButtonPressed[5]){
					event = new MotorEvent();
					event.setMotor(motor_t.MOTOR_C);
					event.setPower((byte)-100);
					queue.addEvent(event);
				}

			}else if(buttonCode == Ouya.BUTTON_DPAD_LEFT){
				gamepadButtonPressed[2] = false;

				if(!gamepadButtonPressed[3]){
					event = new MotorEvent();
					event.setMotor(motor_t.MOTOR_B);
					event.setPower((byte)-25);
					queue.addEvent(event);
				}

			}else if(buttonCode == Ouya.BUTTON_DPAD_RIGHT){
				gamepadButtonPressed[3] = false;

				if(!gamepadButtonPressed[2]){
					event = new MotorEvent();
					event.setMotor(motor_t.MOTOR_B);
					event.setPower((byte)25);
					queue.addEvent(event);
				}

			}else if(buttonCode ==  Ouya.BUTTON_L2){
				gamepadButtonPressed[4] = false;

				if(!gamepadButtonPressed[0]){
					event = new MotorEvent();
					event.setMotor(motor_t.MOTOR_A);
					event.setPower((byte)100);
					queue.addEvent(event);
				}

			}else if(buttonCode ==  Ouya.BUTTON_R2){
				gamepadButtonPressed[5] = false;

				if(!gamepadButtonPressed[1]){
					event = new MotorEvent();
					event.setMotor(motor_t.MOTOR_C);
					event.setPower((byte)100);
					queue.addEvent(event);
				}

			}else if(buttonCode == Ouya.BUTTON_Y){
				gamepadButtonPressed[6] = true;

				event = new MotorEvent();
				event.setMotor(motor_t.RECENTER);
				event.setPower((byte)0x00);
				queue.addEvent(event);

			}else if(buttonCode == Ouya.BUTTON_O){
				userInput = new GamepadUserInput();
				userInput.oButton = true;
				robotArmPositioningSystem.setUserInput(userInput);
				robotArmPositioningSystem.process();

			}else if(buttonCode == Ouya.BUTTON_A){
				core.nextState = game_states_t.MAIN_MENU;
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
				gamepadButtonPressed[0] = false;

				if(!gamepadButtonPressed[4]){
					event = new MotorEvent();
					event.setMotor(motor_t.MOTOR_A);
					event.setPower((byte)0);
					queue.addEvent(event);
				}

			}else if(buttonCode == Ouya.BUTTON_R1){
				gamepadButtonPressed[1] = false;

				if(!gamepadButtonPressed[5]){
					event = new MotorEvent();
					event.setMotor(motor_t.MOTOR_C);
					event.setPower((byte)0);
					queue.addEvent(event);
				}

			}else if(buttonCode == Ouya.BUTTON_DPAD_LEFT){
				gamepadButtonPressed[2] = false;

				if(!gamepadButtonPressed[3]){
					event = new MotorEvent();
					event.setMotor(motor_t.MOTOR_B);
					event.setPower((byte)0);
					queue.addEvent(event);
				}
			}else if(buttonCode == Ouya.BUTTON_DPAD_RIGHT){
				gamepadButtonPressed[3] = false;

				if(!gamepadButtonPressed[2]){
					event = new MotorEvent();
					event.setMotor(motor_t.MOTOR_B);
					event.setPower((byte)0);
					queue.addEvent(event);
				}
			}else if(buttonCode ==  Ouya.BUTTON_L2){
				gamepadButtonPressed[4] = false;

				if(!gamepadButtonPressed[0]){
					event = new MotorEvent();
					event.setMotor(motor_t.MOTOR_A);
					event.setPower((byte)0);
					queue.addEvent(event);
				}

			}else if(buttonCode ==  Ouya.BUTTON_R2){
				gamepadButtonPressed[5] = false;

				if(!gamepadButtonPressed[1]){
					event = new MotorEvent();
					event.setMotor(motor_t.MOTOR_C);
					event.setPower((byte)0);
					queue.addEvent(event);
				}

			}else if(buttonCode == Ouya.BUTTON_Y){
				gamepadButtonPressed[6] = false;
			}

			return true;
		}else{
			return false;
		}
	}

	@Override
	public boolean axisMoved(Controller controller, int axisCode, float value){
		GamepadUserInput userInput;

		if(Math.abs(value) > Ouya.STICK_DEADZONE){
			userInput = new GamepadUserInput();
			if(axisCode == Ouya.AXIS_LEFT_X){
				userInput.axisLeftX = value;
			}else if(axisCode == Ouya.AXIS_LEFT_Y){
				userInput.axisLeftY = value;
			}else if(axisCode == Ouya.AXIS_RIGHT_X){
				userInput.axisRightX = value;
			}else if(axisCode == Ouya.AXIS_RIGHT_Y){
				userInput.axisRightY = value;
			}

			robotArmPositioningSystem.setUserInput(userInput);
			robotArmPositioningSystem.process();

			return true;
		}

		return false;
	}
}
