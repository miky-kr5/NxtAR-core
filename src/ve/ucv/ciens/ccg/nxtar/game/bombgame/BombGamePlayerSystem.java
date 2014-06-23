/*
 * Copyright (C) 2013 Miguel Angel Astor Romero
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
package ve.ucv.ciens.ccg.nxtar.game.bombgame;

import ve.ucv.ciens.ccg.nxtar.NxtARCore;
import ve.ucv.ciens.ccg.nxtar.systems.PlayerSystemBase;
import ve.ucv.ciens.ccg.nxtar.utils.Utils;

import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.annotations.Mapper;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Disposable;

public class BombGamePlayerSystem extends PlayerSystemBase implements Disposable{
	private static final float HEART_Y_POS = (Utils.getScreenHeight() / 2) - 69;
	@Mapper ComponentMapper<BombGamePlayerComponent> playerMapper;

	private SpriteBatch batch;
	private Texture     heartTexture;
	private Sprite      heart;

	public BombGamePlayerSystem(NxtARCore core){
		super(BombGamePlayerComponent.class, core);
		batch        = new SpriteBatch();
		heartTexture = new Texture(Gdx.files.internal("data/gfx/gui/Anonymous_heart_1.png"));
		heart        = new Sprite(heartTexture);
		heart.setSize(heart.getWidth() * 0.5f, heart.getHeight() * 0.5f);
	}

	@Override
	protected void process(Entity e) {
		float heartXPos;
		BombGamePlayerComponent player = playerMapper.get(e);

		// Render remaining lives.
		heartXPos = -(Utils.getScreenWidth() / 2) + 5;
		for(int i = 0; i < player.lives; ++i){
			heart.setPosition(heartXPos, HEART_Y_POS);
			heart.draw(batch);
			heartXPos += heart.getWidth() + 5;
		}

		// Check ending conditions.
		if(player.lives == 0){
			player.gameFinished = true;
			player.victory = false;
		}else if(player.disabledBombs == BombGameEntityCreator.NUM_BOMBS){
			player.gameFinished = true;
			player.victory = true;
		}

		// If met ending conditions then end the game.
		if(player.gameFinished)
			finishGame(player.victory);
	}

	@Override
	public void dispose() {
		if(batch != null)
			batch.dispose();

		if(heartTexture != null)
			heartTexture.dispose();
	}
}
