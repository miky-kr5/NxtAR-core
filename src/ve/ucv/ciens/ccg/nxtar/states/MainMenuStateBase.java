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
import com.badlogic.gdx.controllers.mappings.Ouya;
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

	// Helper fields.
	protected boolean clientConnected;
	private float u_scaling[];
	protected OrthographicCamera pixelPerfectCamera;

	// Buttons and other gui components.
	protected TextButton startButton;
	protected Rectangle startButtonBBox;
	protected Sprite clientConnectedLedOn;
	protected Sprite clientConnectedLedOff;
	protected Sprite background;

	// Graphic data for the start button.
	private Texture startButtonEnabledTexture;
	private Texture startButtonDisabledTexture;
	private Texture startButtonPressedTexture;
	private NinePatch startButtonEnabled9p;
	private NinePatch startButtonDisabled9p;
	private NinePatch startButtonPressed9p;
	private BitmapFont font;

	// Other graphics.
	private Texture clientConnectedLedOffTexture;
	private Texture clientConnectedLedOnTexture;
	private Texture backgroundTexture;
	private ShaderProgram backgroundShader;

	// Button touch helper fields.
	private Vector3 win2world;
	protected Vector2 touchPointWorldCoords;
	protected boolean startButtonTouched;
	protected int startButtonTouchPointer;

	public MainMenuStateBase(){
		TextureRegion region;

		this.pixelPerfectCamera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

		// Create the start button background.
		startButtonEnabledTexture = new Texture(Gdx.files.internal("data/gfx/gui/Anonymous_Pill_Button_Yellow.png"));
		startButtonEnabled9p = new NinePatch(new TextureRegion(startButtonEnabledTexture, 0, 0, startButtonEnabledTexture.getWidth(), startButtonEnabledTexture.getHeight()), 49, 49, 45, 45);

		startButtonDisabledTexture = new Texture(Gdx.files.internal("data/gfx/gui/Anonymous_Pill_Button_Cyan.png"));
		startButtonDisabled9p = new NinePatch(new TextureRegion(startButtonDisabledTexture, 0, 0, startButtonDisabledTexture.getWidth(), startButtonDisabledTexture.getHeight()), 49, 49, 45, 45);

		startButtonPressedTexture = new Texture(Gdx.files.internal("data/gfx/gui/Anonymous_Pill_Button_Blue.png"));
		startButtonPressed9p = new NinePatch(new TextureRegion(startButtonPressedTexture, 0, 0, startButtonPressedTexture.getWidth(), startButtonPressedTexture.getHeight()), 49, 49, 45, 45);

		// Create the start button font.
		FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("data/fonts/d-puntillas-B-to-tiptoe.ttf"));
		font = generator.generateFont(Ouya.runningOnOuya ? 60 : 40, ProjectConstants.FONT_CHARS, false);
		generator.dispose();

		// Create the start button itself.
		TextButtonStyle tbs = new TextButtonStyle();
		tbs.font = font;
		tbs.up = new NinePatchDrawable(startButtonEnabled9p);
		tbs.checked = new NinePatchDrawable(startButtonPressed9p);
		tbs.disabled = new NinePatchDrawable(startButtonDisabled9p);
		tbs.disabledFontColor = new Color(0, 0, 0, 1);
		startButton = new TextButton("Start server", tbs);
		startButton.setText("Start game");
		startButton.setDisabled(true);
		startButtonBBox = new Rectangle(0, 0, startButton.getWidth(), startButton.getHeight());

		// Create the connection leds.
		clientConnectedLedOnTexture = new Texture("data/gfx/gui/Anonymous_Button_Green.png");
		region = new TextureRegion(clientConnectedLedOnTexture);
		clientConnectedLedOn = new Sprite(region);

		clientConnectedLedOffTexture = new Texture("data/gfx/gui/Anonymous_Button_Red.png");
		region = new TextureRegion(clientConnectedLedOffTexture);
		clientConnectedLedOff = new Sprite(region);

		// Set up the background.
		backgroundTexture = new Texture(Gdx.files.internal("data/gfx/textures/tile_aqua.png"));
		backgroundTexture.setWrap(TextureWrap.Repeat, TextureWrap.Repeat);
		backgroundTexture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
		region = new TextureRegion(backgroundTexture);
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


		win2world = new Vector3(0.0f, 0.0f, 0.0f);
		touchPointWorldCoords = new Vector2();
		startButtonTouched = false;
		startButtonTouchPointer = -1;

		clientConnected = false;
		stateActive = false;
	}

	@Override
	public abstract void render(float delta);

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
		startButtonEnabledTexture.dispose();
		startButtonDisabledTexture.dispose();
		startButtonPressedTexture.dispose();
		clientConnectedLedOnTexture.dispose();
		clientConnectedLedOffTexture.dispose();
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
		startButton.setDisabled(false);
	}

	/*;;;;;;;;;;;;;;;;;;
	  ; HELPER METHODS ;
	  ;;;;;;;;;;;;;;;;;;*/

	protected void unprojectTouch(int screenX, int screenY){
		win2world.set(screenX, screenY, 0.0f);
		pixelPerfectCamera.unproject(win2world);
		touchPointWorldCoords.set(win2world.x, win2world.y);
	}

	/*;;;;;;;;;;;;;;;;;;;;;;;;;;
	  ; INPUT LISTENER METHODS ;
	  ;;;;;;;;;;;;;;;;;;;;;;;;;;*/

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button){
		unprojectTouch(screenX, screenY);

		Gdx.app.log(TAG, CLASS_NAME + String.format(".touchDown(%d, %d, %d, %d)", screenX, screenY, pointer, button));
		Gdx.app.log(TAG, CLASS_NAME + String.format(".touchDown() :: Unprojected touch point: (%f, %f)", touchPointWorldCoords.x, touchPointWorldCoords.y));

		if(!startButton.isDisabled() && startButtonBBox.contains(touchPointWorldCoords)){
			startButton.setChecked(true);
			startButtonTouched = true;
			startButtonTouchPointer = pointer;
			Gdx.app.log(TAG, CLASS_NAME + ".touchDown() :: Start button pressed.");
		}

		return true;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button){
		unprojectTouch(screenX, screenY);

		Gdx.app.log(TAG, CLASS_NAME + String.format(".touchUp(%d, %d, %d, %d)", screenX, screenY, pointer, button));
		Gdx.app.log(TAG, CLASS_NAME + String.format(".touchUp() :: Unprojected touch point: (%f, %f)", touchPointWorldCoords.x, touchPointWorldCoords.y));

		if(!startButton.isDisabled() && startButtonBBox.contains(touchPointWorldCoords)){
			startButton.setChecked(false);
			startButtonTouched = false;
			startButtonTouchPointer = -1;
			core.nextState = game_states_t.IN_GAME;
			Gdx.app.log(TAG, CLASS_NAME + ".touchDown() :: Start button released.");
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
