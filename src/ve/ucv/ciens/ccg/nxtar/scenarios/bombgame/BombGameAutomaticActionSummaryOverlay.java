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
package ve.ucv.ciens.ccg.nxtar.scenarios.bombgame;

import ve.ucv.ciens.ccg.nxtar.scenarios.SummaryBase;
import ve.ucv.ciens.ccg.nxtar.scenarios.SummaryOverlayBase;
import ve.ucv.ciens.ccg.nxtar.scenarios.bombgame.BombGameAutomaticActionPerformer.BombGameAutomaticActionSummary;
import ve.ucv.ciens.ccg.nxtar.utils.ProjectConstants;
import ve.ucv.ciens.ccg.nxtar.utils.Utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;

public class BombGameAutomaticActionSummaryOverlay extends SummaryOverlayBase{
	private static final float CANNONICAL_SCREEN_WIDTH = 800.0f;

	private Texture    inclinationBombTexture;
	private Texture    combinationBombTexture;
	private Texture    wireBombTexture;
	private BitmapFont font;
	private BitmapFont titleFont;
	private Sprite     inclinationBomb;
	private Sprite     combinationBomb;
	private Sprite     wireBomb;
	private float      inclinationX;
	private float      combinationX;
	private float      wireX;
	private float      inclinationY;
	private float      combinationY;
	private float      wireY;
	private float      titleWidth;
	private float      titleHeight;

	public BombGameAutomaticActionSummaryOverlay(){
		FreeTypeFontGenerator fontGenerator;
		FreeTypeFontParameter fontParameters;

		inclinationBombTexture = new Texture(Gdx.files.internal("data/gfx/bomb_game/incl_bomb.png"));
		combinationBombTexture = new Texture(Gdx.files.internal("data/gfx/bomb_game/comb_bomb.png"));
		wireBombTexture = new Texture(Gdx.files.internal("data/gfx/bomb_game/wire_bomb.png"));

		inclinationBomb = new Sprite(inclinationBombTexture);
		combinationBomb = new Sprite(combinationBombTexture);
		wireBomb = new Sprite(wireBombTexture);

		inclinationBomb.setSize(inclinationBomb.getWidth() * 0.5f, inclinationBomb.getHeight() * 0.5f);
		combinationBomb.setSize(combinationBomb.getWidth() * 0.5f, combinationBomb.getHeight() * 0.5f);
		wireBomb.setSize(wireBomb.getWidth() * 0.5f, wireBomb.getHeight() * 0.5f);

		combinationBomb.setPosition(-(Utils.getScreenWidthWithOverscan() / 4.0f) - combinationBomb.getWidth(), -(combinationBomb.getHeight() / 2.0f));
		inclinationBomb.setPosition(-(Utils.getScreenWidthWithOverscan() / 4.0f) - inclinationBomb.getWidth(), combinationBomb.getY() + combinationBomb.getHeight() + 10.0f);
		wireBomb.setPosition(-(Utils.getScreenWidthWithOverscan() / 4.0f) - wireBomb.getWidth(), combinationBomb.getY() - wireBomb.getHeight() - 10.0f);

		fontParameters = new FreeTypeFontParameter();
		fontParameters.characters = ProjectConstants.FONT_CHARS;
		fontParameters.size = (int)((float)ProjectConstants.MENU_BUTTON_FONT_SIZE * ((float)Gdx.graphics.getWidth() / CANNONICAL_SCREEN_WIDTH));
		fontParameters.flip = false;
		fontGenerator = new FreeTypeFontGenerator(Gdx.files.internal("data/fonts/d-puntillas-B-to-tiptoe.ttf"));

		font = fontGenerator.generateFont(fontParameters);
		font.setColor(Color.YELLOW);

		fontParameters.size = (int)(90.0f * ((float)Gdx.graphics.getWidth() / CANNONICAL_SCREEN_WIDTH));
		titleFont = fontGenerator.generateFont(fontParameters);

		fontGenerator.dispose();

		inclinationX = inclinationBomb.getX() + inclinationBomb.getWidth() + 15.0f;
		combinationX = combinationBomb.getX() + combinationBomb.getWidth() + 15.0f;
		wireX = wireBomb.getX() + wireBomb.getWidth() + 15.0f;

		inclinationY = inclinationBomb.getY() + (inclinationBomb.getWidth() / 2.0f) - (font.getCapHeight() / 2.0f);
		combinationY = combinationBomb.getY() + (combinationBomb.getWidth() / 2.0f) - (font.getCapHeight() / 2.0f);
		wireY = wireBomb.getY() + (wireBomb.getWidth() / 2.0f) - (font.getCapHeight() / 2.0f);

		titleWidth = titleFont.getBounds("Summary").width;
		titleHeight = titleFont.getBounds("Summary").height;
	}

	@Override
	public void dispose() {
		inclinationBombTexture.dispose();
		combinationBombTexture.dispose();
		wireBombTexture.dispose();
		font.dispose();
		titleFont.dispose();
	}

	@Override
	public void render(SpriteBatch batch, SummaryBase summary) throws ClassCastException{
		BombGameAutomaticActionSummary bombGameSummary;

		if(!(summary instanceof BombGameAutomaticActionSummary))
			throw new ClassCastException("Summary is not a bomb game summary.");

		bombGameSummary = (BombGameAutomaticActionSummary)summary;

		inclinationBomb.draw(batch);
		combinationBomb.draw(batch);
		wireBomb.draw(batch);

		font.draw(batch, String.format("Inclination bombs: %d", bombGameSummary.getNumInclinationBombs()), inclinationX, inclinationY);
		font.draw(batch, String.format("Combination bombs: %d", bombGameSummary.getNumCombinationBombs()), combinationX, combinationY);
		font.draw(batch, String.format("Wire bombs: %d", bombGameSummary.getNumWireBombs()), wireX, wireY);

		titleFont.draw(batch, "Summary", -(titleWidth / 2), (Utils.getScreenHeightWithOverscan() / 2) - titleHeight - 10);
	}
}
