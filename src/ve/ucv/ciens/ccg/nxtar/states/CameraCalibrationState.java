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
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;

public class CameraCalibrationState extends BaseState{
	private static final String TAG = "CAMERA_CALIBRATION_STATE";
	private static final String CLASS_NAME = CameraCalibrationState.class.getSimpleName();
	private static final String SHADER_PATH = "shaders/bckg/bckg";

	private NxtARCore core;

	private float u_scaling[];
	protected Sprite background;
	private Texture backgroundTexture;
	private ShaderProgram backgroundShader;

	// Cameras.
	private OrthographicCamera camera;

	// Video stream graphics.
	private Texture videoFrameTexture;
	private Sprite renderableVideoFrame;
	private Pixmap videoFrame;

	// Gui components.
	private TextButton takeSampleButton;
	private Rectangle takeSampleButtonBBox;
	private Texture buttonEnabledTexture;
	private Texture buttonDisabledTexture;
	private Texture buttonPressedTexture;
	private NinePatch buttonEnabled9p;
	private NinePatch buttonDisabled9p;
	private NinePatch buttonPressed9p;
	private BitmapFont font;

	// Button touch helper fields.
	private boolean takeSampleButtonTouched;
	private int takeSampleButtonPointer;

	// Monitors.
	private VideoFrameMonitor frameMonitor;

	private float[][] calibrationSamples;
	private boolean takeSample;
	private int lastSampleTaken;

	public CameraCalibrationState(final NxtARCore core){
		TextButtonStyle tbs;
		FreeTypeFontGenerator generator;

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
			Gdx.app.error(TAG, CLASS_NAME + ".CameraCalibrationState() :: Failed to compile the background shader.");
			Gdx.app.error(TAG, CLASS_NAME + backgroundShader.getLog());
			backgroundShader = null;
		}

		// Set up the background scaling.
		u_scaling = new float[2];
		u_scaling[0] = Gdx.graphics.getWidth() > Gdx.graphics.getHeight() ? 16.0f : 9.0f;
		u_scaling[1] = Gdx.graphics.getHeight() > Gdx.graphics.getWidth() ? 16.0f : 9.0f;

		// Set up the sampling button.
		// Create the font.
		generator = new FreeTypeFontGenerator(Gdx.files.internal("data/fonts/d-puntillas-B-to-tiptoe.ttf"));
		font = generator.generateFont(ProjectConstants.MENU_BUTTON_FONT_SIZE, ProjectConstants.FONT_CHARS, false);
		generator.dispose();

		// Load the textures.
		buttonEnabledTexture = new Texture(Gdx.files.internal("data/gfx/gui/Anonymous_Pill_Button_Yellow.png"));
		buttonEnabled9p = new NinePatch(new TextureRegion(buttonEnabledTexture, 0, 0, buttonEnabledTexture.getWidth(), buttonEnabledTexture.getHeight()), 49, 49, 45, 45);
		buttonDisabledTexture = new Texture(Gdx.files.internal("data/gfx/gui/Anonymous_Pill_Button_Cyan.png"));
		buttonDisabled9p = new NinePatch(new TextureRegion(buttonDisabledTexture, 0, 0, buttonDisabledTexture.getWidth(), buttonDisabledTexture.getHeight()), 49, 49, 45, 45);
		buttonPressedTexture = new Texture(Gdx.files.internal("data/gfx/gui/Anonymous_Pill_Button_Blue.png"));
		buttonPressed9p = new NinePatch(new TextureRegion(buttonPressedTexture, 0, 0, buttonPressedTexture.getWidth(), buttonPressedTexture.getHeight()), 49, 49, 45, 45);

		// Create the button style.
		tbs = new TextButtonStyle();
		tbs.font = font;
		tbs.up = new NinePatchDrawable(buttonEnabled9p);
		tbs.checked = new NinePatchDrawable(buttonPressed9p);
		tbs.disabled = new NinePatchDrawable(buttonDisabled9p);
		tbs.disabledFontColor = new Color(0, 0, 0, 1);

		// Create the button itself.
		takeSampleButton = new TextButton("Take calibration sample", tbs);
		takeSampleButton.setText("Take calibration sample");
		takeSampleButton.setDisabled(true);
		takeSampleButtonBBox = new Rectangle(0, 0, takeSampleButton.getWidth(), takeSampleButton.getHeight());
		takeSampleButton.setPosition(-(takeSampleButton.getWidth() / 2), -(Gdx.graphics.getHeight()/2) - 1 + (takeSampleButton.getHeight() / 2));
		takeSampleButtonBBox.setPosition(takeSampleButton.getX(), takeSampleButton.getY());

		// Set up the touch collision detection variables.
		win2world = new Vector3(0.0f, 0.0f, 0.0f);
		touchPointWorldCoords = new Vector2();
		takeSampleButtonTouched = false;
		takeSampleButtonPointer = -1;

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

		takeSample = false;
		lastSampleTaken = 0;

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

		// Fetch the current video frame.
		frame = frameMonitor.getCurrentFrame();

		// Apply the undistortion method if the camera has been calibrated already.
		if(core.cvProc.cameraIsCalibrated()){
			frame = core.cvProc.undistortFrame(frame);
		}

		// Find the calibration points in the video frame.
		CVCalibrationData data = core.cvProc.findCalibrationPattern(frame);

		// Disable the sampling button if the calibration pattern was not found.
		if(data.calibrationPoints != null && !core.cvProc.cameraIsCalibrated()){
			takeSampleButton.setDisabled(false);
		}else{
			takeSampleButton.setDisabled(true);
		}

		// If the user requested a sample be taken.
		if(takeSample && !core.cvProc.cameraIsCalibrated() && data.calibrationPoints != null){
			// Disable sample taking.
			takeSample = false;
			Gdx.app.log(TAG, CLASS_NAME + ".render(): Sample taken.");

			// Save the calibration points to the samples array.
			for(int i = 0; i < data.calibrationPoints.length; i += 2){
				Gdx.app.log(TAG, CLASS_NAME + ".render(): Value " + Integer.toString(i) + " = (" + Float.toString(data.calibrationPoints[i]) + ", " + Float.toString(data.calibrationPoints[i + 1]) + ")");
				calibrationSamples[lastSampleTaken][i] = data.calibrationPoints[i];
				calibrationSamples[lastSampleTaken][i + 1] = data.calibrationPoints[i + 1];
			}

			// Move to the next sample.
			lastSampleTaken++;

			// If enough samples has been taken then calibrate the camera.
			if(lastSampleTaken == ProjectConstants.CALIBRATION_SAMPLES){
				Gdx.app.log(TAG, CLASS_NAME + "render(): Last sample taken.");

				core.toast("Calibrating camera", false);
				core.cvProc.calibrateCamera(calibrationSamples, frame);
			}
		}

		if(frame != null && data != null && data.outFrame != null && !Arrays.equals(frame, prevFrame)){
			// If the received frame is valid and is different from the previous frame.
			// Make a texture from the frame.
			dimensions = frameMonitor.getFrameDimensions();
			videoFrame = new Pixmap(data.outFrame, 0, dimensions.getWidth() * dimensions.getHeight());
			videoFrameTexture = new Texture(videoFrame);
			videoFrameTexture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
			videoFrame.dispose();

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

			// Render the frame.
			if(!Ouya.runningOnOuya){
				core.batch.setProjectionMatrix(camera.combined);
			}else{
				core.batch.setProjectionMatrix(pixelPerfectCamera.combined);
			}
			core.batch.begin();{
				renderableVideoFrame.draw(core.batch);
			}core.batch.end();

			// Clear texture memory.
			videoFrameTexture.dispose();
		}

		// Render the user interface.
		if(!Ouya.runningOnOuya){
			core.batch.setProjectionMatrix(pixelPerfectCamera.combined);
			core.batch.begin();{
				takeSampleButton.draw(core.batch, 1.0f);
			}core.batch.end();
		}else{
			// TODO: Render OUYA gui.
		}

		// Save this frame as previous to avoid processing the same frame twice when network latency is high.
		prevFrame = frame;
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

	/*;;;;;;;;;;;;;;;;;;;;;;;;;;
	  ; INPUT LISTENER METHODS ;
	  ;;;;;;;;;;;;;;;;;;;;;;;;;;*/

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
		unprojectTouch(screenX, screenY);

		Gdx.app.log(TAG, CLASS_NAME + String.format(".touchDown(%d, %d, %d, %d)", screenX, screenY, pointer, button));
		Gdx.app.log(TAG, CLASS_NAME + String.format(".touchDown() :: Unprojected touch point: (%f, %f)", touchPointWorldCoords.x, touchPointWorldCoords.y));

		if(!takeSampleButton.isDisabled() && takeSampleButtonBBox.contains(touchPointWorldCoords) && !takeSampleButtonTouched){
			takeSampleButton.setChecked(true);
			takeSampleButtonTouched = true;
			takeSampleButtonPointer = pointer;
			Gdx.app.log(TAG, CLASS_NAME + ".touchDown() :: Sample button pressed.");
		}

		return true;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button){
		unprojectTouch(screenX, screenY);

		Gdx.app.log(TAG, CLASS_NAME + String.format(".touchUp(%d, %d, %d, %d)", screenX, screenY, pointer, button));
		Gdx.app.log(TAG, CLASS_NAME + String.format(".touchUp() :: Unprojected touch point: (%f, %f)", touchPointWorldCoords.x, touchPointWorldCoords.y));

		if(!takeSampleButton.isDisabled() && takeSampleButtonBBox.contains(touchPointWorldCoords) && takeSampleButtonTouched){
			takeSampleButton.setChecked(false);
			takeSampleButtonTouched = false;
			takeSampleButtonPointer = -1;
			takeSample = true;
			Gdx.app.log(TAG, CLASS_NAME + ".touchDown() :: Sample button released.");
		}

		return true;
	}

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer){
		unprojectTouch(screenX, screenY);

		if(!takeSampleButton.isDisabled() && takeSampleButtonTouched && pointer == takeSampleButtonPointer && !takeSampleButtonBBox.contains(touchPointWorldCoords)){
			takeSampleButtonPointer = -1;
			takeSampleButtonTouched = false;
			takeSampleButton.setChecked(false);
			Gdx.app.log(TAG, CLASS_NAME + ".touchDragged() :: Sample button released.");
		}

		return true;
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
