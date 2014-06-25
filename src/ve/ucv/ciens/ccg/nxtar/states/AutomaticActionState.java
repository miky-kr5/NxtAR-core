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
import ve.ucv.ciens.ccg.nxtar.game.AutomaticActionPerformerBase;
import ve.ucv.ciens.ccg.nxtar.game.GameGlobals;
import ve.ucv.ciens.ccg.nxtar.game.AutomaticActionPerformerBase.automatic_action_t;
import ve.ucv.ciens.ccg.nxtar.graphics.CustomPerspectiveCamera;
import ve.ucv.ciens.ccg.nxtar.interfaces.ImageProcessor.MarkerData;
import ve.ucv.ciens.ccg.nxtar.network.SensorReportThread;
import ve.ucv.ciens.ccg.nxtar.network.monitors.MotorEventQueue;
import ve.ucv.ciens.ccg.nxtar.network.monitors.VideoFrameMonitor;
import ve.ucv.ciens.ccg.nxtar.systems.MarkerPositioningSystem;
import ve.ucv.ciens.ccg.nxtar.systems.MarkerRenderingSystem;
import ve.ucv.ciens.ccg.nxtar.utils.ProjectConstants;
import ve.ucv.ciens.ccg.nxtar.utils.Utils;

import com.artemis.World;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.mappings.Ouya;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;

public class AutomaticActionState extends BaseState{
	private static final String  TAG                    = "AUTOMATIC_STATE";
	private static final String  CLASS_NAME             = AutomaticActionState.class.getSimpleName();
	private static final String  BACKGROUND_SHADER_PATH = "shaders/bckg/bckg";
	private static final float   NEAR                   = 0.01f;
	private static final float   FAR                    = 100.0f;

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
	private boolean                         ignoreBackKey;
	private boolean                         automaticActionEnabled;
	private AutomaticActionPerformerBase    automaticActionPerformer;
	private automatic_action_t              previousAction;

	// Cameras.
	private OrthographicCamera              unitaryOrthographicCamera;
	private OrthographicCamera              pixelPerfectOrthographicCamera;
	private CustomPerspectiveCamera         perspectiveCamera;

	// Video stream graphics.
	private Texture                         videoFrameTexture;
	private Sprite                          renderableVideoFrame;
	private Pixmap                          videoFrame;

	// Gui elements.
	private Texture                         startButtonEnabledTexture;
	private Texture                         startButtonDisabledTexture;
	private Texture                         startButtonPressedTexture;
	private NinePatch                       startButtonEnabled9p;
	private NinePatch                       startButtonDisabled9p;
	private NinePatch                       startButtonPressed9p;
	private BitmapFont                      font;
	private TextButton                      startButton;
	private Rectangle                       startButtonBBox;
	private boolean                         startButtonPressed;
	private Texture                         ouyaOButtonTexture;
	private Sprite                          ouyaOButton;
	private boolean                         oButtonPressed;
	private boolean                         aButtonPressed;

	// Button touch helper fields.
	private boolean[]                       buttonsTouched;
	private int[]                           buttonPointers;
	private boolean[]                       gamepadButtonPressed;

	// Monitors.
	private VideoFrameMonitor               frameMonitor;
	private MotorEventQueue                 queue;
	private SensorReportThread              sensorThread;

	public AutomaticActionState(final NxtARCore core) throws IllegalStateException, IllegalArgumentException{
		if(core == null)
			throw new IllegalArgumentException(CLASS_NAME + ": Core is null.");

		this.core                = core;
		frameMonitor             = VideoFrameMonitor.getInstance();
		queue                    = MotorEventQueue.getInstance();
		sensorThread             = SensorReportThread.getInstance();
		ignoreBackKey            = false;
		videoFrame               = null;
		aButtonPressed           = false;
		automaticActionEnabled   = false;
		startButtonPressed       = false;
		automaticActionPerformer = GameGlobals.getAutomaticActionPerformer();
		previousAction           = automatic_action_t.NO_ACTION;

		// Set up the cameras.
		pixelPerfectOrthographicCamera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		unitaryOrthographicCamera = new OrthographicCamera(1.0f, Gdx.graphics.getHeight() / Gdx.graphics.getWidth());

		// Set up input handling support fields.
		win2world = new Vector3(0.0f, 0.0f, 0.0f);
		touchPointWorldCoords = new Vector2();

		buttonsTouched = new boolean[1];
		buttonsTouched[0] = false;

		buttonPointers = new int[1];
		buttonPointers[0] = -1;

		gamepadButtonPressed = new boolean[1];
		gamepadButtonPressed[0] = false;

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

		// Create the gui.
		setUpButton();

		// Set up the game world.
		gameWorld = GameGlobals.getGameWorld();
		markerRenderingSystem = gameWorld.getSystem(MarkerRenderingSystem.class);

		if(markerRenderingSystem == null)
			throw new IllegalStateException(CLASS_NAME + ": Essential marker rendering system is null.");
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
			if(automaticActionEnabled)
				performAutomaticAction(data);

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
				renderableVideoFrame.setSize(xSize * ProjectConstants.OVERSCAN, Utils.getScreenHeightWithOverscan());
				renderableVideoFrame.rotate90(true);
				renderableVideoFrame.translate(-renderableVideoFrame.getWidth() / 2, -renderableVideoFrame.getHeight() / 2);

				frameBufferSprite.setSize(xSize * ProjectConstants.OVERSCAN, Utils.getScreenHeightWithOverscan());
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

		core.batch.setProjectionMatrix(pixelPerfectOrthographicCamera.combined);
		core.batch.begin();{
			startButton.draw(core.batch, 1.0f);
			if(Ouya.runningOnOuya)
				ouyaOButton.draw(core.batch);
		}core.batch.end();

		data = null;
	}

	@Override
	public void pause(){
		automaticActionPerformer.reset();
		startButton.setDisabled(false);
		ignoreBackKey = false;
		automaticActionEnabled = false;
	}

	@Override
	public void dispose(){
		SensorReportThread.freeInstance();

		if(font != null)
			font.dispose();

		if(ouyaOButtonTexture != null)
			ouyaOButtonTexture.dispose();

		if(startButtonEnabledTexture != null)
			startButtonEnabledTexture.dispose();

		if(startButtonDisabledTexture != null)
			startButtonDisabledTexture.dispose();

		if(startButtonPressedTexture != null)
			startButtonPressedTexture.dispose();

		if(modelBatch != null)
			modelBatch.dispose();

		if(videoFrameTexture != null)
			videoFrameTexture.dispose();

		if(backgroundTexture != null)
			backgroundTexture.dispose();

		if(backgroundShader != null)
			backgroundShader.dispose();

		if(frameBuffer != null)
			frameBuffer.dispose();
	}

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

	/*;;;;;;;;;;;;;;;;;;
	  ; HELPER METHODS ;
	  ;;;;;;;;;;;;;;;;;;*/

	private void setUpButton(){
		TextButtonStyle       textButtonStyle;
		FreeTypeFontGenerator fontGenerator;
		FreeTypeFontParameter fontParameters;

		// Create the start button background.
		startButtonEnabledTexture  = new Texture(Gdx.files.internal("data/gfx/gui/Anonymous_Pill_Button_Yellow.png"));
		startButtonEnabled9p       = new NinePatch(new TextureRegion(startButtonEnabledTexture, 0, 0, startButtonEnabledTexture.getWidth(), startButtonEnabledTexture.getHeight()), 49, 49, 45, 45);
		startButtonDisabledTexture = new Texture(Gdx.files.internal("data/gfx/gui/Anonymous_Pill_Button_Cyan.png"));
		startButtonDisabled9p      = new NinePatch(new TextureRegion(startButtonDisabledTexture, 0, 0, startButtonDisabledTexture.getWidth(), startButtonDisabledTexture.getHeight()), 49, 49, 45, 45);
		startButtonPressedTexture  = new Texture(Gdx.files.internal("data/gfx/gui/Anonymous_Pill_Button_Blue.png"));
		startButtonPressed9p       = new NinePatch(new TextureRegion(startButtonPressedTexture, 0, 0, startButtonPressedTexture.getWidth(), startButtonPressedTexture.getHeight()), 49, 49, 45, 45);

		// Create the start button font.
		fontParameters            = new FreeTypeFontParameter();
		fontParameters.characters = ProjectConstants.FONT_CHARS;
		fontParameters.size       = ProjectConstants.MENU_BUTTON_FONT_SIZE;
		fontParameters.flip       = false;
		fontGenerator             = new FreeTypeFontGenerator(Gdx.files.internal("data/fonts/d-puntillas-B-to-tiptoe.ttf"));
		font                      = fontGenerator.generateFont(fontParameters);
		fontGenerator.dispose();

		// Create the start button.
		textButtonStyle                   = new TextButtonStyle();
		textButtonStyle.font              = font;
		textButtonStyle.up                = new NinePatchDrawable(startButtonEnabled9p);
		textButtonStyle.checked           = new NinePatchDrawable(startButtonPressed9p);
		textButtonStyle.disabled          = new NinePatchDrawable(startButtonDisabled9p);
		textButtonStyle.fontColor         = new Color(Color.BLACK);
		textButtonStyle.downFontColor     = new Color(Color.WHITE);
		textButtonStyle.disabledFontColor = new Color(Color.BLACK);

		startButton = new TextButton("Start automatic action", textButtonStyle);
		startButton.setText("Start automatic action");
		startButton.setDisabled(false);
		startButtonBBox = new Rectangle(0, 0, startButton.getWidth(), startButton.getHeight());
		startButton.setPosition(-(startButton.getWidth() / 2), -(Gdx.graphics.getHeight() / 2) + 10);
		startButtonBBox.setPosition(startButton.getX(), startButton.getY());

		// Set OUYA's O button.
		if(Ouya.runningOnOuya){
			ouyaOButtonTexture = new Texture("data/gfx/gui/OUYA_O.png");
			ouyaOButton = new Sprite(ouyaOButtonTexture);
			ouyaOButton.setSize(ouyaOButton.getWidth() * 0.6f, ouyaOButton.getHeight() * 0.6f);
			oButtonPressed = false;
			ouyaOButton.setPosition(startButton.getX() - ouyaOButton.getWidth() - 20, startButton.getY() + (ouyaOButton.getHeight() / 2));
		}else{
			ouyaOButtonTexture = null;
		}
	}

	private void performAutomaticAction(MarkerData data){
		MotorEvent event1 = null;
		MotorEvent event2 = null;
		automatic_action_t nextAction;

		try{
			if(!automaticActionPerformer.performAutomaticAction(sensorThread.getLightSensorReading(), data)){
				nextAction = automaticActionPerformer.getNextAction();

				if(nextAction != previousAction){
					switch(nextAction){
					case GO_BACKWARDS:
						event1 = new MotorEvent();
						event2 = new MotorEvent();
						event1.setMotor(motor_t.MOTOR_A);
						event1.setPower((byte)-20);
						event2.setMotor(motor_t.MOTOR_C);
						event2.setPower((byte)-20);
						break;

					case GO_FORWARD:
						event1 = new MotorEvent();
						event2 = new MotorEvent();
						event1.setMotor(motor_t.MOTOR_A);
						event1.setPower((byte)20);
						event2.setMotor(motor_t.MOTOR_C);
						event2.setPower((byte)20);
						break;

					case STOP:
						event1 = new MotorEvent();
						event2 = new MotorEvent();
						event1.setMotor(motor_t.MOTOR_A);
						event1.setPower((byte)0);
						event2.setMotor(motor_t.MOTOR_C);
						event2.setPower((byte)0);
						break;

					case ROTATE_90:
						event1 = new MotorEvent();
						event1.setMotor(motor_t.ROTATE_90);
						event1.setPower((byte)20);
						break;

					case RECENTER:
						event1 = new MotorEvent();
						event1.setMotor(motor_t.RECENTER);
						event1.setPower((byte)20);
						break;

					case TURN_LEFT:
						event1 = new MotorEvent();
						event1.setMotor(motor_t.MOTOR_A);
						event1.setPower((byte)20);
						break;

					case TURN_RIGHT:
						event1 = new MotorEvent();
						event1.setMotor(motor_t.MOTOR_C);
						event1.setPower((byte)20);
						break;

					case BACKWARDS_LEFT:
						event1 = new MotorEvent();
						event1.setMotor(motor_t.MOTOR_C);
						event1.setPower((byte)-20);
						break;

					case BACKWARDS_RIGHT:
						event1 = new MotorEvent();
						event1.setMotor(motor_t.MOTOR_A);
						event1.setPower((byte)-20);
						break;

					case LOOK_RIGHT:
						event1 = new MotorEvent();
						event1.setMotor(motor_t.MOTOR_B);
						event1.setPower((byte)25);
						break;

					case LOOK_LEFT:
						event1 = new MotorEvent();
						event1.setMotor(motor_t.MOTOR_B);
						event1.setPower((byte)-25);
						break;

					case STOP_LOOKING:
						event1 = new MotorEvent();
						event1.setMotor(motor_t.MOTOR_B);
						event1.setPower((byte)0);
						break;

					case NO_ACTION:
					default:
						break;
					}

					if(event1 != null)
						queue.addEvent(event1);
					if(event2 != null)
						queue.addEvent(event2);

					previousAction = nextAction;
				}else{
					Gdx.app.log(TAG, CLASS_NAME + ".performAutomaticAction(): Skipping repeated action.");
				}

			}else{
				startButton.setDisabled(false);
				ignoreBackKey = false;
				automaticActionEnabled = false;
				core.nextState = game_states_t.SUMMARY;
			}

		}catch(IllegalArgumentException e){
			Gdx.app.log(TAG, CLASS_NAME + ".performAutomaticAction(): Received IllegalArgumentException: ", e);

			startButton.setDisabled(false);
			ignoreBackKey = false;
			automaticActionEnabled = false;
			core.nextState = game_states_t.MAIN_MENU;

		}catch(IllegalStateException e){
			Gdx.app.log(TAG, CLASS_NAME + ".performAutomaticAction(): Received IllegalStateException: ", e);

			startButton.setDisabled(false);
			ignoreBackKey = false;
			automaticActionEnabled = false;
			core.nextState = game_states_t.MAIN_MENU;
		}
	}

	/*;;;;;;;;;;;;;;;;;;;;;;;;;;;
	  ; INPUT PROCESSOR METHODS ;
	  ;;;;;;;;;;;;;;;;;;;;;;;;;;;*/

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button){
		if(!Ouya.runningOnOuya){
			win2world.set(screenX, screenY, 0.0f);
			unitaryOrthographicCamera.unproject(win2world);
			touchPointWorldCoords.set(win2world.x * Gdx.graphics.getWidth(), win2world.y * Gdx.graphics.getHeight());

			if(!startButton.isDisabled() && startButtonBBox.contains(touchPointWorldCoords)){
				startButtonPressed = true;
			}

		}

		return false;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button){
		if(!Ouya.runningOnOuya){
			win2world.set(screenX, screenY, 0.0f);
			unitaryOrthographicCamera.unproject(win2world);
			touchPointWorldCoords.set(win2world.x * Gdx.graphics.getWidth(), win2world.y * Gdx.graphics.getHeight());

			if(!startButton.isDisabled() && startButtonPressed && startButtonBBox.contains(touchPointWorldCoords)){
				startButton.setDisabled(true);
				ignoreBackKey = true;
				automaticActionEnabled = true;
			}

		}

		return false;
	}

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer){
		if(!Ouya.runningOnOuya){
			win2world.set(screenX, screenY, 0.0f);
			unitaryOrthographicCamera.unproject(win2world);
			touchPointWorldCoords.set(win2world.x * Gdx.graphics.getWidth(), win2world.y * Gdx.graphics.getHeight());

			if(!startButton.isDisabled() && startButtonPressed && !startButtonBBox.contains(touchPointWorldCoords)){
				startButtonPressed = false;
			}

		}

		return false;
	}

	@Override
	public boolean keyDown(int keycode){
		if(keycode == Input.Keys.BACK && !ignoreBackKey){
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
		if(stateActive){
			if(buttonCode == Ouya.BUTTON_O && !startButton.isDisabled()){
				oButtonPressed = true;
				startButton.setChecked(true);
			}else if(buttonCode == Ouya.BUTTON_A && !ignoreBackKey){
				aButtonPressed = true;
			}

			return true;
		}else{
			return false;
		}
	}

	@Override
	public boolean buttonUp(Controller controller, int buttonCode){
		if(stateActive){
			if(buttonCode == Ouya.BUTTON_O && oButtonPressed){
				if(oButtonPressed){
					oButtonPressed = false;
					startButton.setChecked(false);
					startButton.setDisabled(true);
					ignoreBackKey = true;
					automaticActionEnabled = true;
				}
			}else if(buttonCode == Ouya.BUTTON_A && aButtonPressed){
				core.nextState = game_states_t.MAIN_MENU;
			}

			return true;
		}else{
			return false;
		}
	}
}
