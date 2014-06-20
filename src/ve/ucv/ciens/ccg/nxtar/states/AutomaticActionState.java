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
import ve.ucv.ciens.ccg.nxtar.graphics.CustomPerspectiveCamera;
import ve.ucv.ciens.ccg.nxtar.interfaces.ImageProcessor.MarkerData;
import ve.ucv.ciens.ccg.nxtar.network.monitors.VideoFrameMonitor;
import ve.ucv.ciens.ccg.nxtar.systems.AnimationSystem;
import ve.ucv.ciens.ccg.nxtar.systems.CollisionDetectionSystem;
import ve.ucv.ciens.ccg.nxtar.systems.FadeEffectRenderingSystem;
import ve.ucv.ciens.ccg.nxtar.systems.GeometrySystem;
import ve.ucv.ciens.ccg.nxtar.systems.MarkerPositioningSystem;
import ve.ucv.ciens.ccg.nxtar.systems.MarkerRenderingSystem;
import ve.ucv.ciens.ccg.nxtar.systems.ObjectRenderingSystem;
import ve.ucv.ciens.ccg.nxtar.systems.RobotArmPositioningSystem;
import ve.ucv.ciens.ccg.nxtar.utils.GameSettings;
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

public class AutomaticActionState extends BaseState{
	private static final String  TAG                    = "IN_GAME_STATE";
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
	private ObjectRenderingSystem           objectRenderingSystem;
	private RobotArmPositioningSystem       robotArmPositioningSystem;
	private FadeEffectRenderingSystem       fadeEffectRenderingSystem;

	// Cameras.
	private OrthographicCamera              unitaryOrthographicCamera;
	private OrthographicCamera              pixelPerfectOrthographicCamera;
	private CustomPerspectiveCamera         perspectiveCamera;

	// Video stream graphics.
	private Texture                         videoFrameTexture;
	private Sprite                          renderableVideoFrame;
	private Pixmap                          videoFrame;

	// Button touch helper fields.
	private boolean[]                       buttonsTouched;
	private int[]                           buttonPointers;
	private boolean[]                       gamepadButtonPressed;

	// Monitors.
	private VideoFrameMonitor               frameMonitor;
	//	private MotorEventQueue                 queue;
	//	private SensorReportThread              sensorThread;

	public AutomaticActionState(final NxtARCore core){
		this.core = core;
		frameMonitor = VideoFrameMonitor.getInstance();
		//		queue = MotorEventQueue.getInstance();
		//		sensorThread = SensorReportThread.getInstance();

		// Set up rendering fields;
		videoFrame = null;

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

		// Set up the game world.
		gameWorld = GameSettings.getGameWorld();

		robotArmPositioningSystem = new RobotArmPositioningSystem();
		markerRenderingSystem     = new MarkerRenderingSystem(modelBatch);
		objectRenderingSystem     = new ObjectRenderingSystem(modelBatch);
		fadeEffectRenderingSystem = new FadeEffectRenderingSystem();

		gameWorld.setSystem(new MarkerPositioningSystem());
		gameWorld.setSystem(robotArmPositioningSystem, Ouya.runningOnOuya);
		gameWorld.setSystem(new GeometrySystem());
		gameWorld.setSystem(new AnimationSystem());
		gameWorld.setSystem(new CollisionDetectionSystem());
		gameWorld.setSystem(GameSettings.getGameLogicSystem());
		gameWorld.setSystem(markerRenderingSystem, true);
		gameWorld.setSystem(objectRenderingSystem, true);
		gameWorld.setSystem(fadeEffectRenderingSystem, true);

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

		data = null;
	}

	@Override
	public void dispose(){
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

		fadeEffectRenderingSystem.dispose();
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

	/*;;;;;;;;;;;;;;;;;;;;;;;;;;;
	  ; INPUT PROCESSOR METHODS ;
	  ;;;;;;;;;;;;;;;;;;;;;;;;;;;*/

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button){
		if(!Ouya.runningOnOuya){
			win2world.set(screenX, screenY, 0.0f);
			unitaryOrthographicCamera.unproject(win2world);
			touchPointWorldCoords.set(win2world.x * Gdx.graphics.getWidth(), win2world.y * Gdx.graphics.getHeight());
		}

		return false;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button){
		if(!Ouya.runningOnOuya){
			win2world.set(screenX, screenY, 0.0f);
			unitaryOrthographicCamera.unproject(win2world);
			touchPointWorldCoords.set(win2world.x * Gdx.graphics.getWidth(), win2world.y * Gdx.graphics.getHeight());
		}

		return false;
	}

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer){
		if(!Ouya.runningOnOuya){
			win2world.set(screenX, screenY, 0.0f);
			unitaryOrthographicCamera.unproject(win2world);
			touchPointWorldCoords.set(win2world.x * Gdx.graphics.getWidth(), win2world.y * Gdx.graphics.getHeight());
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
		if(stateActive){
			Gdx.app.log(TAG, CLASS_NAME + ".buttonDown() :: " + controller.getName() + " :: " + Integer.toString(buttonCode));

			if(buttonCode == Ouya.BUTTON_O){

			}

			return true;
		}else{
			return false;
		}
	}

	@Override
	public boolean buttonUp(Controller controller, int buttonCode){
		if(stateActive){
			Gdx.app.log(TAG, CLASS_NAME + ".buttonDown() :: " + controller.getName() + " :: " + Integer.toString(buttonCode));

			if(buttonCode == Ouya.BUTTON_O){ }

			return true;
		}else{
			return false;
		}
	}
}
