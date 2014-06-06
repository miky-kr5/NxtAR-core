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

import ve.ucv.ciens.ccg.nxtar.NxtARCore.game_states_t;
import ve.ucv.ciens.ccg.nxtar.utils.ProjectConstants;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;

public abstract class MainMenuStateBase extends BaseState{
	protected static final String TAG = "MAIN_MENU";
	private static final String CLASS_NAME = MainMenuStateBase.class.getSimpleName();
	private static final String SHADER_PATH = "shaders/bckg/bckg";

	protected final int NUM_MENU_BUTTONS = 2;

	// Helper fields.
	protected boolean clientConnected;
	protected boolean cameraCalibrated;
	protected boolean assetsLoaded;
	private   float   u_scaling[];

	// Buttons and other gui components.
	protected TextButton startButton;
	protected Rectangle  startButtonBBox;
	protected TextButton calibrationButton;
	protected Rectangle  calibrationButtonBBox;
	protected Sprite     cameraCalibratedLedOn;
	protected Sprite     cameraCalibratedLedOff;
	protected Sprite     assetsLoadedLedOn;
	protected Sprite     assetsLoadedLedOff;

	protected Sprite background;

	// Graphic data for the start button.
	private Texture    menuButtonEnabledTexture;
	private Texture    menuButtonDisabledTexture;
	private Texture    menuButtonPressedTexture;
	private NinePatch  menuButtonEnabled9p;
	private NinePatch  menuButtonDisabled9p;
	private NinePatch  menuButtonPressed9p;
	private BitmapFont font;

	// Other graphics.
	private Texture       ledOffTexture;
	private Texture       ledOnTexture;
	private Texture       backgroundTexture;
	private ShaderProgram backgroundShader;

	// Button touch helper fields.
	protected boolean startButtonTouched;
	protected int     startButtonTouchPointer;
	protected boolean calibrationButtonTouched;
	protected int     calibrationButtonTouchPointer;

	public MainMenuStateBase(){
		TextureRegion         region;
		TextButtonStyle       textButtonStyle;
		FreeTypeFontGenerator fontGenerator;
		FreeTypeFontParameter fontParameters;

		this.pixelPerfectCamera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

		// Create the start button background.
		menuButtonEnabledTexture = new Texture(Gdx.files.internal("data/gfx/gui/Anonymous_Pill_Button_Yellow.png"));
		menuButtonEnabled9p = new NinePatch(new TextureRegion(menuButtonEnabledTexture, 0, 0, menuButtonEnabledTexture.getWidth(), menuButtonEnabledTexture.getHeight()), 49, 49, 45, 45);

		menuButtonDisabledTexture = new Texture(Gdx.files.internal("data/gfx/gui/Anonymous_Pill_Button_Cyan.png"));
		menuButtonDisabled9p = new NinePatch(new TextureRegion(menuButtonDisabledTexture, 0, 0, menuButtonDisabledTexture.getWidth(), menuButtonDisabledTexture.getHeight()), 49, 49, 45, 45);

		menuButtonPressedTexture = new Texture(Gdx.files.internal("data/gfx/gui/Anonymous_Pill_Button_Blue.png"));
		menuButtonPressed9p = new NinePatch(new TextureRegion(menuButtonPressedTexture, 0, 0, menuButtonPressedTexture.getWidth(), menuButtonPressedTexture.getHeight()), 49, 49, 45, 45);

		// Create the start button font.
		fontParameters = new FreeTypeFontParameter();
		fontParameters.characters = ProjectConstants.FONT_CHARS;
		fontParameters.size = ProjectConstants.MENU_BUTTON_FONT_SIZE;
		fontParameters.flip = false;
		fontGenerator = new FreeTypeFontGenerator(Gdx.files.internal("data/fonts/d-puntillas-B-to-tiptoe.ttf"));
		font = fontGenerator.generateFont(fontParameters);
		fontGenerator.dispose();

		// Create the start button.
		textButtonStyle = new TextButtonStyle();
		textButtonStyle.font = font;
		textButtonStyle.up = new NinePatchDrawable(menuButtonEnabled9p);
		textButtonStyle.checked = new NinePatchDrawable(menuButtonPressed9p);
		textButtonStyle.disabled = new NinePatchDrawable(menuButtonDisabled9p);
		textButtonStyle.disabledFontColor = new Color(0, 0, 0, 1);

		startButton = new TextButton("Start server", textButtonStyle);
		startButton.setText("Start game");
		startButton.setDisabled(true);
		startButtonBBox = new Rectangle(0, 0, startButton.getWidth(), startButton.getHeight());

		// Create the calibration button.
		calibrationButton = new TextButton("Calibrate camera", textButtonStyle);
		calibrationButton.setText("Calibrate camera");
		calibrationButton.setDisabled(true);
		calibrationButtonBBox = new Rectangle(0, 0, calibrationButton.getWidth(), calibrationButton.getHeight());

		// Create the connection leds.
		ledOnTexture = new Texture("data/gfx/gui/Anonymous_Button_Green.png");
		ledOffTexture = new Texture("data/gfx/gui/Anonymous_Button_Red.png");

		region = new TextureRegion(ledOnTexture);
		cameraCalibratedLedOn = new Sprite(region);

		region = new TextureRegion(ledOffTexture);
		cameraCalibratedLedOff = new Sprite(region);

		region = new TextureRegion(ledOnTexture);
		assetsLoadedLedOn = new Sprite(region);

		region = new TextureRegion(ledOffTexture);
		assetsLoadedLedOff = new Sprite(region);

		// Set up the background.
		backgroundTexture = new Texture(Gdx.files.internal("data/gfx/textures/tile_aqua.png"));
		backgroundTexture.setWrap(TextureWrap.Repeat, TextureWrap.Repeat);
		backgroundTexture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
		region = new TextureRegion(backgroundTexture);
		background = new Sprite(backgroundTexture);
		background.setSize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		background.setPosition(-(Gdx.graphics.getWidth() / 2), -(Gdx.graphics.getHeight() / 2));

		backgroundShader = new ShaderProgram(Gdx.files.internal(SHADER_PATH + "_vert.glsl"), Gdx.files.internal(SHADER_PATH + "_frag.glsl"));
		if(!backgroundShader.isCompiled()){
			Gdx.app.error(TAG, CLASS_NAME + ".MainMenuStateBase() :: Failed to compile the background shader.");
			Gdx.app.error(TAG, CLASS_NAME + backgroundShader.getLog());
			backgroundShader = null;
		}

		u_scaling = new float[2];
		u_scaling[0] = Gdx.graphics.getWidth() > Gdx.graphics.getHeight() ? 16.0f : 9.0f;
		u_scaling[1] = Gdx.graphics.getHeight() > Gdx.graphics.getWidth() ? 16.0f : 9.0f;

		win2world = new Vector3(0.0f, 0.0f, 0.0f);
		touchPointWorldCoords = new Vector2();
		startButtonTouched = false;
		startButtonTouchPointer = -1;
		calibrationButtonTouched = false;
		calibrationButtonTouchPointer = -1;

		clientConnected = false;
		cameraCalibrated = false;
		assetsLoaded = false;
		stateActive = false;
	}

	@Override
	public abstract void render(float delta);

	@Override
	public void dispose(){
		menuButtonEnabledTexture.dispose();
		menuButtonDisabledTexture.dispose();
		menuButtonPressedTexture.dispose();
		ledOnTexture.dispose();
		ledOffTexture.dispose();
		backgroundTexture.dispose();
		if(backgroundShader != null) backgroundShader.dispose();
		font.dispose();
	}

	protected void drawBackground(SpriteBatch batch){
		if(backgroundShader != null){
			batch.setShader(backgroundShader);
			backgroundShader.setUniform2fv("u_scaling", u_scaling, 0, 2);
		}
		background.draw(batch);
		if(backgroundShader != null) batch.setShader(null);
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

	public void onClientConnected(){
		clientConnected = true;
		calibrationButton.setDisabled(false);
	}

	public void onCameraCalibrated(){
		cameraCalibrated = true;
		startGame();
	}

	public void onAssetsLoaded(){
		assetsLoaded = true;
		startGame();
	}

	private void startGame(){
		startButton.setDisabled(!(cameraCalibrated && assetsLoaded));
	}

	/*;;;;;;;;;;;;;;;;;;;;;;;;;;
	  ; INPUT LISTENER METHODS ;
	  ;;;;;;;;;;;;;;;;;;;;;;;;;;*/

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button){
		unprojectTouch(screenX, screenY);

		Gdx.app.log(TAG, CLASS_NAME + String.format(".touchDown(%d, %d, %d, %d)", screenX, screenY, pointer, button));
		Gdx.app.log(TAG, CLASS_NAME + String.format(".touchDown() :: Unprojected touch point: (%f, %f)", touchPointWorldCoords.x, touchPointWorldCoords.y));

		if(!startButton.isDisabled() && startButtonBBox.contains(touchPointWorldCoords) && !calibrationButtonTouched){
			startButton.setChecked(true);
			startButtonTouched = true;
			startButtonTouchPointer = pointer;
			Gdx.app.log(TAG, CLASS_NAME + ".touchDown() :: Start button pressed.");
		}else if(!calibrationButton.isDisabled() && calibrationButtonBBox.contains(touchPointWorldCoords) && !startButtonTouched){
			calibrationButton.setChecked(true);
			calibrationButtonTouched = true;
			calibrationButtonTouchPointer = pointer;
			Gdx.app.log(TAG, CLASS_NAME + ".touchDown() :: Calibration button pressed.");
		}

		return true;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button){
		unprojectTouch(screenX, screenY);

		Gdx.app.log(TAG, CLASS_NAME + String.format(".touchUp(%d, %d, %d, %d)", screenX, screenY, pointer, button));
		Gdx.app.log(TAG, CLASS_NAME + String.format(".touchUp() :: Unprojected touch point: (%f, %f)", touchPointWorldCoords.x, touchPointWorldCoords.y));

		if(!startButton.isDisabled() && startButtonBBox.contains(touchPointWorldCoords) && startButtonTouched){
			startButton.setChecked(false);
			startButtonTouched = false;
			startButtonTouchPointer = -1;
			core.nextState = game_states_t.IN_GAME;
			Gdx.app.log(TAG, CLASS_NAME + ".touchDown() :: Start button released.");
		}else if(!calibrationButton.isDisabled() && calibrationButtonBBox.contains(touchPointWorldCoords) && calibrationButtonTouched){
			calibrationButton.setChecked(false);
			calibrationButtonTouched = false;
			calibrationButtonTouchPointer = -1;
			core.nextState = game_states_t.CALIBRATION;
			Gdx.app.log(TAG, CLASS_NAME + ".touchDown() :: Calibration button released.");
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
		}else if(!calibrationButton.isDisabled() && calibrationButtonTouched && pointer == calibrationButtonTouchPointer && !calibrationButtonBBox.contains(touchPointWorldCoords)){
			calibrationButtonTouchPointer = -1;
			calibrationButtonTouched = false;
			calibrationButton.setChecked(false);
			Gdx.app.log(TAG, CLASS_NAME + ".touchDragged() :: Start button released.");
		}

		return true;
	}

	@Override
	public boolean keyDown(int keycode){
		if(keycode == Input.Keys.BACK){
			// Ignore.
			return true;
		}
		return false;
	}
}
