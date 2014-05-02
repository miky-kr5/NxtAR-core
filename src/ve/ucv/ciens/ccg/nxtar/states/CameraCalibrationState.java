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

import ve.ucv.ciens.ccg.nxtar.NxtARCore;
import ve.ucv.ciens.ccg.nxtar.NxtARCore.game_states_t;
import ve.ucv.ciens.ccg.nxtar.interfaces.CVProcessor.CVCalibrationData;
import ve.ucv.ciens.ccg.nxtar.network.monitors.VideoFrameMonitor;
import ve.ucv.ciens.ccg.nxtar.utils.ProjectConstants;
import ve.ucv.ciens.ccg.nxtar.utils.Size;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.controllers.Controller;
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

public class CameraCalibrationState extends BaseState{
	private static final String TAG = "IN_GAME_STATE";
	private static final String CLASS_NAME = CameraCalibrationState.class.getSimpleName();
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

	// Button touch helper fields.
	private Vector3 win2world;
	private Vector2 touchPointWorldCoords;
	private boolean[] motorButtonsTouched;
	private int[] motorButtonsPointers;
	private boolean[] motorGamepadButtonPressed;

	// Monitors.
	private VideoFrameMonitor frameMonitor;

	private float[][] calibrationSamples;

	public CameraCalibrationState(final NxtARCore core){
		this.core = core;
		frameMonitor = VideoFrameMonitor.getInstance();

		// Set up the cameras.
		pixelPerfectCamera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		camera = new OrthographicCamera(1.0f, Gdx.graphics.getHeight() / Gdx.graphics.getWidth());

		// Set up the background.
		backgroundTexture = new Texture(Gdx.files.internal("data/gfx/textures/tile_aqua.png"));
		backgroundTexture.setWrap(TextureWrap.Repeat, TextureWrap.Repeat);
		backgroundTexture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
		background = new Sprite(backgroundTexture);
		background.setSize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		background.setPosition(-(Gdx.graphics.getWidth() / 2), -(Gdx.graphics.getHeight() / 2));

		// Load the background shader.
		backgroundShader = new ShaderProgram(Gdx.files.internal(SHADER_PATH + ".vert"), Gdx.files.internal(SHADER_PATH + ".frag"));
		if(!backgroundShader.isCompiled()){
			Gdx.app.error(TAG, CLASS_NAME + ".MainMenuStateBase() :: Failed to compile the background shader.");
			Gdx.app.error(TAG, CLASS_NAME + backgroundShader.getLog());
			backgroundShader = null;
		}

		// Set up the background scaling.
		u_scaling = new float[2];
		u_scaling[0] = Gdx.graphics.getWidth() > Gdx.graphics.getHeight() ? 16.0f : 9.0f;
		u_scaling[1] = Gdx.graphics.getHeight() > Gdx.graphics.getWidth() ? 16.0f : 9.0f;

		// Initialize the calibration samples vector.
		calibrationSamples = new float[ProjectConstants.CALIBRATION_SAMPLES][];
		for(int i = 0; i < calibrationSamples.length; i++){
			calibrationSamples[i] = new float[ProjectConstants.CALIBRATION_PATTERN_POINTS * 2];
		}
	}

	@Override
	public void onStateSet(){
		Gdx.input.setInputProcessor(this);
		Gdx.input.setCatchBackKey(true);
		Gdx.input.setCatchMenuKey(true);

		for(int i = 0; i < calibrationSamples.length; i++){
			for(int j = 0; j < calibrationSamples[i].length; j++){
				calibrationSamples[i][j] = 0.0f;
			}
		}
	}

	@Override
	public void onStateUnset(){ }

	@Override
	public void render(float delta){
		byte[] frame;
		byte[] prevFrame = null;
		Size dimensions = null;

		// Clear the screen.
		Gdx.gl.glClearColor(1, 1, 1, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		Gdx.app.log(TAG, CLASS_NAME + ".render(): Frame buffer cleared.");

		// Render the background.
		core.batch.setProjectionMatrix(pixelPerfectCamera.combined);
		core.batch.begin();{
			if(backgroundShader != null){
				core.batch.setShader(backgroundShader);
				backgroundShader.setUniform2fv("u_scaling", u_scaling, 0, 2);
			}
			background.draw(core.batch);
			if(backgroundShader != null) core.batch.setShader(null);
		}core.batch.end();
		Gdx.app.log(TAG, CLASS_NAME + ".render(): Background drawn.");

		// Fetch the current video frame and find the calibration pattern in it.
		frame = frameMonitor.getCurrentFrame();
		CVCalibrationData data = core.cvProc.findCalibrationPattern(frame);

		if(frame != null && data != null && data.outFrame != null && !Arrays.equals(frame, prevFrame)){
			// If the received frame is valid and is different from the previous frame.
			// Make a texture from the frame.
			dimensions = frameMonitor.getFrameDimensions();
			videoFrame = new Pixmap(data.outFrame, 0, dimensions.getWidth() * dimensions.getHeight());
			videoFrameTexture = new Texture(videoFrame);
			videoFrameTexture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
			videoFrame.dispose();
			Gdx.app.log(TAG, CLASS_NAME + ".render(): Texture created.");

			// Set up the frame texture as a rendereable sprite.
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
			Gdx.app.log(TAG, CLASS_NAME + ".render(): Texture resized and positioned.");

			// Render the frame.
			if(!Ouya.runningOnOuya){
				core.batch.setProjectionMatrix(camera.combined);
			}else{
				core.batch.setProjectionMatrix(pixelPerfectCamera.combined);
			}
			core.batch.begin();{
				renderableVideoFrame.draw(core.batch);
			}core.batch.end();
			Gdx.app.log(TAG, CLASS_NAME + ".render(): Texture drawn.");

			// Clear texture memory.
			videoFrameTexture.dispose();
			Gdx.app.log(TAG, CLASS_NAME + ".render(): Texture released.");
		}

		// Render the user interface.
		if(!Ouya.runningOnOuya){
			core.batch.setProjectionMatrix(pixelPerfectCamera.combined);
			core.batch.begin();{
			}core.batch.end();
		}else{
		}

		// Save this frame as previous to avoid processing the same frame twice
		// when network latency is high.
		prevFrame = frame;
		Gdx.app.log(TAG, CLASS_NAME + ".render(): Render complete.");
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
		if(videoFrameTexture != null)
			videoFrameTexture.dispose();
		backgroundTexture.dispose();
		if(backgroundShader != null) backgroundShader.dispose();
	}

	@Override
	public boolean keyDown(int keycode){
		if(keycode == Input.Keys.BACK){
			core.nextState = game_states_t.MAIN_MENU;
			return true;
		}
		return false;
	}

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button){
		return false;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button){
		return false;
	}

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer){
		return false;
	}

	@Override
	public boolean buttonDown(Controller controller, int buttonCode){
		return false;
	}

	@Override
	public boolean buttonUp(Controller controller, int buttonCode){
		return false;
	}

	@Override
	public boolean axisMoved(Controller controller, int axisCode, float value){
		return false;
	}
}
