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
import ve.ucv.ciens.ccg.nxtar.scenarios.bombgame.BombGamePlayerSystem.BombGamePlayerSummary;
import ve.ucv.ciens.ccg.nxtar.utils.ProjectConstants;
import ve.ucv.ciens.ccg.nxtar.utils.Utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.controllers.mappings.Ouya;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.BitmapFont.TextBounds;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;

public class BombGameScenarioEndingOverlay extends SummaryOverlayBase {
	private static final float CANNONICAL_SCREEN_WIDTH = 800.0f;

	private BitmapFont font;
	private BitmapFont titleFont;
	private float      textX;
	private float      baseTextY;
	private TextBounds titleBounds;

	public BombGameScenarioEndingOverlay(){
		FreeTypeFontGenerator fontGenerator;
		FreeTypeFontParameter fontParameters;

		fontParameters = new FreeTypeFontParameter();
		fontParameters.characters = ProjectConstants.FONT_CHARS;
		if(!Ouya.runningOnOuya)
			fontParameters.size = (int)(65.0f * ((float)Gdx.graphics.getWidth() / CANNONICAL_SCREEN_WIDTH));
		else
			fontParameters.size = ProjectConstants.MENU_BUTTON_FONT_SIZE;
		fontParameters.flip = false;
		fontGenerator = new FreeTypeFontGenerator(Gdx.files.internal("data/fonts/d-puntillas-B-to-tiptoe.ttf"));

		font = fontGenerator.generateFont(fontParameters);
		font.setColor(Color.YELLOW);

		fontParameters.size = (int)(90.0f * ((float)Gdx.graphics.getWidth() / CANNONICAL_SCREEN_WIDTH));
		titleFont = fontGenerator.generateFont(fontParameters);

		fontGenerator.dispose();

		textX       = -(Utils.getScreenWidthWithOverscan() / 7.0f);
		baseTextY   = -(font.getCapHeight() / 2.0f);
	}

	@Override
	public void dispose(){
		font.dispose();
		titleFont.dispose();
	}

	@Override
	public void render(SpriteBatch batch, SummaryBase summary) throws ClassCastException{
		BombGamePlayerSummary bombGamePlayerSummary;
		String                title;
		String                text;

		// Get the player's summary.
		if(!(summary instanceof BombGamePlayerSummary))
			throw new ClassCastException("Summary is not a bomb game summary.");
		bombGamePlayerSummary = (BombGamePlayerSummary)summary;

		// Render the summary.
		text = String.format("Lives left: %d", bombGamePlayerSummary.livesLeft);
		textX = -(font.getBounds(text).width / 2);
		font.draw(batch, text, textX, baseTextY + font.getCapHeight() + 15);

		text = String.format("Bombs defused: %d", bombGamePlayerSummary.disabledBombs);
		textX = -(font.getBounds(text).width / 2);
		font.draw(batch, text, textX, baseTextY);

		text = String.format("Bombs detonated: %d", bombGamePlayerSummary.detonatedBombs);
		textX = -(font.getBounds(text).width / 2);
		font.draw(batch, text, textX, baseTextY - font.getCapHeight() - 15);

		// Render the title.
		if(bombGamePlayerSummary.victory)
			title = "Victory!";
		else
			title = "Game Over";
		titleBounds = titleFont.getBounds(title);

		if(!Ouya.runningOnOuya)
			titleFont.draw(batch, title, -(titleBounds.width / 2), (Utils.getScreenHeightWithOverscan() / 2) - titleBounds.height - 10);
		else
			titleFont.draw(batch, title, -(titleBounds.width / 2), (Utils.getScreenHeightWithOverscan() / 2) - 10);
	}
}
