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
package ve.ucv.ciens.ccg.nxtar.utils;

import ve.ucv.ciens.ccg.nxtar.NxtARCore;
import ve.ucv.ciens.ccg.nxtar.entities.BombGameEntityCreator;
import ve.ucv.ciens.ccg.nxtar.entities.EntityCreatorBase;
import ve.ucv.ciens.ccg.nxtar.systems.BombGameLogicSystem;
import ve.ucv.ciens.ccg.nxtar.systems.GameLogicSystemBase;

import com.artemis.World;
import com.artemis.managers.GroupManager;

public abstract class GameSettings{
	private static EntityCreatorBase   entityCreator   = null;
	private static GameLogicSystemBase gameLogicSystem = null;
	private static World               gameWorld       = null;

	public static void initGameSettings(NxtARCore core) throws IllegalArgumentException{
		if(core == null)
			throw new IllegalArgumentException("Core is null.");

		if(getGameWorld() == null){
			gameWorld = new World();
			gameWorld.setManager(new GroupManager());
		}

		if(getEntityCreator() == null){
			entityCreator = new BombGameEntityCreator();
			entityCreator.setWorld(GameSettings.getGameWorld());
			entityCreator.setCore(core);
		}

		if(getGameLogicSystem() == null)
			gameLogicSystem = new BombGameLogicSystem();
	}

	public static void clearGameSettings(){
		entityCreator.dispose();
		entityCreator = null;
		gameLogicSystem = null;
		gameWorld = null;
		System.gc();
	}

	/**
	 * @return the entityCreator
	 */
	public static EntityCreatorBase getEntityCreator() {
		return entityCreator;
	}

	/**
	 * @return the gameLogicSystem
	 */
	public static GameLogicSystemBase getGameLogicSystem() {
		return gameLogicSystem;
	}

	/**
	 * @return the gameWorld
	 */
	public static World getGameWorld() {
		return gameWorld;
	}
}
