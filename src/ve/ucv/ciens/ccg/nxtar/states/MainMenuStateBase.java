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

import ve.ucv.ciens.ccg.nxtar.utils.ProjectConstants;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.controllers.Controllers;
import com.badlogic.gdx.controllers.mappings.Ouya;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;


public abstract class MainMenuStateBase extends BaseState{
	protected static final String TAG = "MAIN_MENU";

	// Client connection helper fields.
	protected boolean clientConnected;

	// Buttons and other gui components.
	protected TextButton startButton;
	protected Rectangle startButtonBBox;
	protected Sprite clientConnectedLedOn;
	protected Sprite clientConnectedLedOff;

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

	public MainMenuStateBase(){
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
		TextureRegion region;
		clientConnectedLedOnTexture = new Texture("data/gfx/gui/Anonymous_Button_Green.png");
		region = new TextureRegion(clientConnectedLedOnTexture, clientConnectedLedOnTexture.getWidth(), clientConnectedLedOnTexture.getHeight());
		clientConnectedLedOn = new Sprite(region);

		clientConnectedLedOffTexture = new Texture("data/gfx/gui/Anonymous_Button_Red.png");
		region = new TextureRegion(clientConnectedLedOffTexture, clientConnectedLedOffTexture.getWidth(), clientConnectedLedOffTexture.getHeight());
		clientConnectedLedOff = new Sprite(region);

		clientConnected = false;
	}

	@Override
	public abstract void render(float delta);

	@Override
	public abstract void resize(int width, int height);

	@Override
	public abstract void show();
	@Override
	public abstract void hide();

	@Override
	public abstract void pause();

	@Override
	public abstract void resume();

	@Override
	public void dispose(){
		startButtonEnabledTexture.dispose();
		startButtonDisabledTexture.dispose();
		startButtonPressedTexture.dispose();
		clientConnectedLedOnTexture.dispose();
		clientConnectedLedOffTexture.dispose();
		font.dispose();
	}

	@Override
	public void onStateSet(){
		Controllers.addListener(this);
		Gdx.input.setInputProcessor(this);
	}
	
	public void onClientConnected(){
		clientConnected = true;
		startButton.setDisabled(false);
	}
}
