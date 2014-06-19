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
package ve.ucv.ciens.ccg.nxtar.systems;

import ve.ucv.ciens.ccg.nxtar.NxtARCore;
import ve.ucv.ciens.ccg.nxtar.components.BombGamePlayerComponent;
import ve.ucv.ciens.ccg.nxtar.entities.BombGameEntityCreator;

import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.annotations.Mapper;

public class BombGamePlayerSystem extends PlayerSystemBase{
	@Mapper ComponentMapper<BombGamePlayerComponent> playerMapper;

	public BombGamePlayerSystem(NxtARCore core){
		super(BombGamePlayerComponent.class, core);
	}

	@Override
	protected void process(Entity e) {
		BombGamePlayerComponent player = playerMapper.get(e);

		if(player.lives == 0){
			player.gameFinished = true;
			player.victory = false;
		}else if(player.disabledBombs == BombGameEntityCreator.NUM_BOMBS){
			player.gameFinished = true;
			player.victory = true;
		}

		if(player.gameFinished)
			finishGame(player.victory);
	}
}
