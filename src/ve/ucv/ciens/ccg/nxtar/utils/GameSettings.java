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
import ve.ucv.ciens.ccg.nxtar.systems.AnimationSystem;
import ve.ucv.ciens.ccg.nxtar.systems.BombGameLogicSystem;
import ve.ucv.ciens.ccg.nxtar.systems.CollisionDetectionSystem;
import ve.ucv.ciens.ccg.nxtar.systems.FadeEffectRenderingSystem;
import ve.ucv.ciens.ccg.nxtar.systems.GameLogicSystemBase;
import ve.ucv.ciens.ccg.nxtar.systems.GeometrySystem;
import ve.ucv.ciens.ccg.nxtar.systems.MarkerPositioningSystem;
import ve.ucv.ciens.ccg.nxtar.systems.MarkerRenderingSystem;
import ve.ucv.ciens.ccg.nxtar.systems.ObjectRenderingSystem;
import ve.ucv.ciens.ccg.nxtar.systems.RobotArmPositioningSystem;

import com.artemis.EntitySystem;
import com.artemis.World;
import com.artemis.managers.GroupManager;
import com.artemis.utils.ImmutableBag;
import com.badlogic.gdx.controllers.mappings.Ouya;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.utils.Disposable;

public abstract class GameSettings{
	private static EntityCreatorBase   entityCreator   = null;
	private static GameLogicSystemBase gameLogicSystem = null;
	private static World               gameWorld       = null;
	private static ModelBatch          modelBatch      = null;

	public static void initGameSettings(NxtARCore core) throws IllegalArgumentException{
		if(core == null)
			throw new IllegalArgumentException("Core is null.");

		if(modelBatch == null)
			modelBatch = new ModelBatch();

		if(getGameWorld() == null){
			gameWorld = new World();
			gameWorld.setManager(new GroupManager());
		}

		if(getEntityCreator() == null){
			entityCreator = new BombGameEntityCreator();
			entityCreator.setWorld(gameWorld);
			entityCreator.setCore(core);
		}

		if(getGameLogicSystem() == null)
			gameLogicSystem = new BombGameLogicSystem();

		gameWorld.setSystem(new MarkerPositioningSystem());
		gameWorld.setSystem(new RobotArmPositioningSystem(), Ouya.runningOnOuya);
		gameWorld.setSystem(new GeometrySystem());
		gameWorld.setSystem(new AnimationSystem());
		gameWorld.setSystem(new CollisionDetectionSystem());
		gameWorld.setSystem(gameLogicSystem);
		gameWorld.setSystem(new MarkerRenderingSystem(modelBatch), true);
		gameWorld.setSystem(new ObjectRenderingSystem(modelBatch), true);
		gameWorld.setSystem(new FadeEffectRenderingSystem(), true);

		gameWorld.initialize();
	}

	public static void clearGameSettings(){
		ImmutableBag<EntitySystem> systems = gameWorld.getSystems();

		for(int i = 0; i < systems.size(); i++){
			if(systems.get(i) instanceof Disposable){
				((Disposable)systems.get(i)).dispose();
			}
		}

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
