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
import ve.ucv.ciens.ccg.nxtar.components.PlayerComponentBase;
import ve.ucv.ciens.ccg.nxtar.utils.GameSettings;

import com.artemis.Aspect;
import com.artemis.Entity;
import com.artemis.systems.EntityProcessingSystem;

public abstract class PlayerSystemBase extends EntityProcessingSystem {
	protected NxtARCore core;

	@SuppressWarnings("unchecked")
	public PlayerSystemBase(Class<? extends PlayerComponentBase> component, NxtARCore core){
		super(Aspect.getAspectForAll(component));

		if(component == null)
			throw new IllegalArgumentException("Component is null.");

		if(core == null)
			throw new IllegalArgumentException("Core is null.");

		this.core = core;
	}

	protected final void finishGame(boolean victory){
		// TODO: Switch to game over state.
		// TODO: Set game over state parameters.
		GameSettings.getEntityCreator().resetAllEntities();
		core.nextState = NxtARCore.game_states_t.MAIN_MENU;
	}

	@Override
	protected abstract void process(Entity e);

}
