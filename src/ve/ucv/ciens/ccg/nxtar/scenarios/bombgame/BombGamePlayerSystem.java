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
package ve.ucv.ciens.ccg.nxtar.scenarios.bombgame;

import ve.ucv.ciens.ccg.nxtar.scenarios.SummaryBase;
import ve.ucv.ciens.ccg.nxtar.systems.PlayerSystemBase;
import ve.ucv.ciens.ccg.nxtar.utils.Utils;

import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.annotations.Mapper;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class BombGamePlayerSystem extends PlayerSystemBase{
	public final class BombGamePlayerSummary extends SummaryBase{
		public int     livesLeft;
		public int     disabledBombs;
		public int     detonatedBombs;
		public boolean victory;

		public BombGamePlayerSummary(){
			reset();
		}

		@Override
		public void reset() {
			this.livesLeft = 0;
			this.disabledBombs = 0;
			this.detonatedBombs = 0;
			this.victory = false;
		}
	}

	@Mapper ComponentMapper<BombGamePlayerComponent> playerMapper;

	private SpriteBatch           batch;
	private Texture               heartTexture;
	private Sprite                heart;
	private OrthographicCamera    camera;
	private BombGamePlayerSummary summary;
	private float                 heartYPos;

	public BombGamePlayerSystem(){
		super(BombGamePlayerComponent.class);
		camera       = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		batch        = new SpriteBatch();
		heartTexture = new Texture(Gdx.files.internal("data/gfx/bomb_game/Anonymous_heart_1.png"));
		summary      = new BombGamePlayerSummary();
		heart        = new Sprite(heartTexture);
		heart.setSize(heart.getWidth() * 0.25f, heart.getHeight() * 0.25f);
		heartYPos = (Utils.getScreenHeightWithOverscan() / 2) - heart.getHeight() - 64 - 5;
	}

	@Override
	protected void process(Entity e){
		float heartXPos;
		BombGamePlayerComponent player = playerMapper.get(e);

		// Render remaining lives.
		batch.setProjectionMatrix(camera.combined);
		batch.begin();{
			heartXPos = -(Utils.getScreenWidthWithOverscan() / 2) + 5;
			for(int i = 0; i < player.lives; ++i){
				heart.setPosition(heartXPos, heartYPos);
				heart.draw(batch);
				heartXPos += heart.getWidth() + 5;
			}
		}batch.end();

		// Check ending conditions.
		if(player.lives <= 0){
			player.gameFinished = true;
			player.victory = false;
		}else if(player.disabledBombs >= BombGameEntityCreator.NUM_BOMBS){
			player.gameFinished = true;
			player.victory = true;
		}

		if(player.gameFinished){
			summary.victory        = player.victory;
			summary.livesLeft      = player.lives;
			summary.disabledBombs  = BombGameEntityCreator.NUM_BOMBS - (player.startingLives - player.lives);
			summary.detonatedBombs = player.startingLives - player.lives;
			finishGame(player.victory);
		}
	}

	@Override
	public void dispose() {
		if(batch != null)
			batch.dispose();

		if(heartTexture != null)
			heartTexture.dispose();
	}

	@Override
	public SummaryBase getPlayerSummary() {
		return summary;
	}
}
