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
import ve.ucv.ciens.ccg.nxtar.game.AutomaticActionPerformerBase;
import ve.ucv.ciens.ccg.nxtar.game.AutomaticActionSummaryOverlayBase;
import ve.ucv.ciens.ccg.nxtar.game.GameGlobals;
import ve.ucv.ciens.ccg.nxtar.utils.ProjectConstants;
import ve.ucv.ciens.ccg.nxtar.utils.Utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.mappings.Ouya;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
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

public class AutomaticActionSummaryState extends BaseState{
	private static final String TAG = "AUTO_SUMMARY";
	private static final String CLASS_NAME = AutomaticActionSummaryState.class.getSimpleName();
	private static final String SHADER_PATH = "shaders/movingBckg/movingBckg";

	// Helper fields.
	private float u_scaling[];
	private float u_displacement;

	// Buttons and other gui components.
	private TextButton    continueButton;
	private Rectangle     continueButtonBBox;
	private Sprite        background;
	private Texture       backgroundTexture;
	private ShaderProgram backgroundShader;
	private Texture       ouyaOButtonTexture;
	private Sprite        ouyaOButton;
	private boolean       oButtonPressed;

	// Graphic data for the start button.
	private Texture    buttonEnabledTexture;
	private Texture    buttonDisabledTexture;
	private Texture    buttonPressedTexture;
	private NinePatch  buttonEnabled9p;
	private NinePatch  buttonDisabled9p;
	private NinePatch  buttonPressed9p;
	private BitmapFont font;

	// Summary overlay related fields.
	AutomaticActionPerformerBase     automaticActionPerformer;
	AutomaticActionSummaryOverlayBase summaryOverlay;

	// Button touch helper fields.
	private boolean continueButtonTouched;
	private int     continueButtonTouchPointer;

	public  AutomaticActionSummaryState(NxtARCore core) throws IllegalArgumentException{
		TextButtonStyle       textButtonStyle;
		FreeTypeFontGenerator fontGenerator;
		FreeTypeFontParameter fontParameters;

		if(core == null)
			throw new IllegalArgumentException(CLASS_NAME + ": Core is null.");

		this.core                = core;
		this.pixelPerfectCamera  = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		oButtonPressed           = false;
		automaticActionPerformer = GameGlobals.getAutomaticActionPerformer();
		summaryOverlay           = GameGlobals.getAutomaticActionSummaryOverlay();

		// Create the start button background.
		buttonEnabledTexture = new Texture(Gdx.files.internal("data/gfx/gui/Anonymous_Pill_Button_Yellow.png"));
		buttonEnabled9p = new NinePatch(new TextureRegion(buttonEnabledTexture, 0, 0, buttonEnabledTexture.getWidth(), buttonEnabledTexture.getHeight()), 49, 49, 45, 45);
		buttonDisabledTexture = new Texture(Gdx.files.internal("data/gfx/gui/Anonymous_Pill_Button_Cyan.png"));
		buttonDisabled9p = new NinePatch(new TextureRegion(buttonDisabledTexture, 0, 0, buttonDisabledTexture.getWidth(), buttonDisabledTexture.getHeight()), 49, 49, 45, 45);
		buttonPressedTexture = new Texture(Gdx.files.internal("data/gfx/gui/Anonymous_Pill_Button_Blue.png"));
		buttonPressed9p = new NinePatch(new TextureRegion(buttonPressedTexture, 0, 0, buttonPressedTexture.getWidth(), buttonPressedTexture.getHeight()), 49, 49, 45, 45);

		// Create the start button font.
		fontParameters = new FreeTypeFontParameter();
		fontParameters.characters = ProjectConstants.FONT_CHARS;
		fontParameters.size = ProjectConstants.MENU_BUTTON_FONT_SIZE;
		fontParameters.flip = false;
		fontGenerator = new FreeTypeFontGenerator(Gdx.files.internal("data/fonts/d-puntillas-B-to-tiptoe.ttf"));
		font = fontGenerator.generateFont(fontParameters);
		fontGenerator.dispose();

		// Create the contine button.
		textButtonStyle = new TextButtonStyle();
		textButtonStyle.font = font;
		textButtonStyle.up = new NinePatchDrawable(buttonEnabled9p);
		textButtonStyle.checked = new NinePatchDrawable(buttonPressed9p);
		textButtonStyle.disabled = new NinePatchDrawable(buttonDisabled9p);
		textButtonStyle.fontColor = new Color(Color.BLACK);
		textButtonStyle.downFontColor = new Color(Color.WHITE);
		textButtonStyle.disabledFontColor = new Color(Color.BLACK);

		continueButton = new TextButton("Continue", textButtonStyle);
		continueButton.setText("Continue");
		continueButton.setPosition(-(continueButton.getWidth() / 2), -(Utils.getScreenHeightWithOverscan() / 2) + 10);
		continueButtonBBox = new Rectangle(0, 0, continueButton.getWidth(), continueButton.getHeight());
		continueButtonBBox.setPosition(continueButton.getX(), continueButton.getY());

		// Set OUYA's O button.
		if(Ouya.runningOnOuya){
			ouyaOButtonTexture = new Texture("data/gfx/gui/OUYA_O.png");
			ouyaOButton = new Sprite(ouyaOButtonTexture);
			ouyaOButton.setSize(ouyaOButton.getWidth() * 0.6f, ouyaOButton.getHeight() * 0.6f);
			oButtonPressed = false;
		}else{
			ouyaOButtonTexture = null;
		}

		// Set up the background.
		backgroundTexture = new Texture(Gdx.files.internal("data/gfx/textures/tile_aqua.png"));
		backgroundTexture.setWrap(TextureWrap.Repeat, TextureWrap.Repeat);
		backgroundTexture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
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

		u_displacement = 1.0f;

		win2world = new Vector3(0.0f, 0.0f, 0.0f);
		touchPointWorldCoords = new Vector2();
		continueButtonTouched = false;
		continueButtonTouchPointer = -1;
		stateActive = false;
	}

	@Override
	public void render(float delta){
		Gdx.gl.glClearColor(1, 1, 1, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		core.batch.setProjectionMatrix(pixelPerfectCamera.combined);
		core.batch.begin();{

			// Render background.
			core.batch.disableBlending();
			drawBackground(core.batch);
			core.batch.enableBlending();

			summaryOverlay.render(core.batch, automaticActionPerformer.getSummary());

			// Render buttons.
			continueButton.draw(core.batch, 1.0f);
			if(Ouya.runningOnOuya)
				ouyaOButton.draw(core.batch);

		}core.batch.end();
	}

	@Override
	public void dispose(){
		buttonEnabledTexture.dispose();
		buttonDisabledTexture.dispose();
		buttonPressedTexture.dispose();
		if(ouyaOButtonTexture != null)
			ouyaOButtonTexture.dispose();
		backgroundTexture.dispose();
		if(backgroundShader != null) backgroundShader.dispose();
		font.dispose();
	}

	private void drawBackground(SpriteBatch batch){
		if(backgroundShader != null){
			batch.setShader(backgroundShader);
			backgroundShader.setUniform2fv("u_scaling", u_scaling, 0, 2);
			backgroundShader.setUniformf("u_displacement", u_displacement);
		}
		background.draw(batch);
		if(backgroundShader != null) batch.setShader(null);
		u_displacement = u_displacement < 0.0f ? 1.0f : u_displacement - 0.0005f;
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

	/*;;;;;;;;;;;;;;;;;;;;;;;;;;
	  ; INPUT LISTENER METHODS ;
	  ;;;;;;;;;;;;;;;;;;;;;;;;;;*/

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button){
		unprojectTouch(screenX, screenY);

		Gdx.app.log(TAG, CLASS_NAME + String.format(".touchDown(%d, %d, %d, %d)", screenX, screenY, pointer, button));
		Gdx.app.log(TAG, CLASS_NAME + String.format(".touchDown() :: Unprojected touch point: (%f, %f)", touchPointWorldCoords.x, touchPointWorldCoords.y));

		if(!continueButton.isDisabled() && continueButtonBBox.contains(touchPointWorldCoords)){
			continueButton.setChecked(true);
			continueButtonTouched = true;
			continueButtonTouchPointer = pointer;
			Gdx.app.log(TAG, CLASS_NAME + ".touchDown() :: Start button pressed.");
		}

		return true;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button){
		unprojectTouch(screenX, screenY);

		Gdx.app.log(TAG, CLASS_NAME + String.format(".touchUp(%d, %d, %d, %d)", screenX, screenY, pointer, button));
		Gdx.app.log(TAG, CLASS_NAME + String.format(".touchUp() :: Unprojected touch point: (%f, %f)", touchPointWorldCoords.x, touchPointWorldCoords.y));

		if(!continueButton.isDisabled() && continueButtonBBox.contains(touchPointWorldCoords) && continueButtonTouched){
			continueButton.setChecked(false);
			continueButtonTouched = false;
			continueButtonTouchPointer = -1;
			core.nextState = game_states_t.MAIN_MENU;
			Gdx.app.log(TAG, CLASS_NAME + ".touchDown() :: Start button released.");
		}

		return true;
	}

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer){
		unprojectTouch(screenX, screenY);

		if(!continueButton.isDisabled() && continueButtonTouched && pointer == continueButtonTouchPointer && !continueButtonBBox.contains(touchPointWorldCoords)){
			continueButtonTouchPointer = -1;
			continueButtonTouched = false;
			continueButton.setChecked(false);
			Gdx.app.log(TAG, CLASS_NAME + ".touchDragged() :: Start button released.");
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

	/*;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
	  ; CONTROLLER LISTENER METHODS ;
	  ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;*/

	@Override
	public boolean buttonDown(Controller controller, int buttonCode){
		if(stateActive){
			if(buttonCode == Ouya.BUTTON_O && !continueButton.isDisabled()){
				Gdx.app.log(TAG, CLASS_NAME + ".buttonDown(): O button pressed.");
				oButtonPressed = true;
				continueButton.setChecked(true);
			}
			return true;
		}else{
			return false;
		}
	}

	@Override
	public boolean buttonUp(Controller controller, int buttonCode){
		if(stateActive){
			if(buttonCode == Ouya.BUTTON_O){
				Gdx.app.log(TAG, CLASS_NAME + ".buttonDown(): O button released.");
				if(oButtonPressed){
					oButtonPressed = false;
					continueButton.setChecked(false);
					core.nextState = game_states_t.MAIN_MENU;
				}
			}
			return true;
		}else{
			return false;
		}
	}
}
