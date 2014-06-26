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
package ve.ucv.ciens.ccg.nxtar.scenarios;

import ve.ucv.ciens.ccg.nxtar.NxtARCore;
import ve.ucv.ciens.ccg.nxtar.entities.EntityCreatorBase;
import ve.ucv.ciens.ccg.nxtar.systems.AnimationSystem;
import ve.ucv.ciens.ccg.nxtar.systems.CollisionDetectionSystem;
import ve.ucv.ciens.ccg.nxtar.systems.FadeEffectRenderingSystem;
import ve.ucv.ciens.ccg.nxtar.systems.GameLogicSystemBase;
import ve.ucv.ciens.ccg.nxtar.systems.GeometrySystem;
import ve.ucv.ciens.ccg.nxtar.systems.MarkerPositioningSystem;
import ve.ucv.ciens.ccg.nxtar.systems.MarkerRenderingSystem;
import ve.ucv.ciens.ccg.nxtar.systems.ObjectRenderingSystem;
import ve.ucv.ciens.ccg.nxtar.systems.PlayerSystemBase;
import ve.ucv.ciens.ccg.nxtar.systems.RobotArmPositioningSystem;

import com.artemis.EntitySystem;
import com.artemis.World;
import com.artemis.managers.GroupManager;
import com.artemis.utils.ImmutableBag;
import com.badlogic.gdx.controllers.mappings.Ouya;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.utils.Disposable;

public abstract class ScenarioGlobals{
	private static EntityCreatorBase                 entityCreator                    = null;
	private static GameLogicSystemBase               gameLogicSystem                  = null;
	private static World                             gameWorld                        = null;
	private static ModelBatch                        modelBatch                       = null;
	private static AutomaticActionPerformerBase      automaticActionPerformer         = null;
	private static SummaryOverlayBase                automaticActionSummaryOverlay    = null;
	private static PlayerSystemBase                  playerSystem                     = null;
	private static SummaryOverlayBase                scenarioSummaryOverlay           = null;
	private static HintsOverlayBase                  hintsOverlay                     = null;

	public static void init(NxtARCore core) throws IllegalArgumentException, InstantiationException, IllegalAccessException{
		if(core == null)
			throw new IllegalArgumentException("Core is null.");

		if(modelBatch == null)
			modelBatch = new ModelBatch();

		if(gameWorld == null){
			gameWorld = new World();
			gameWorld.setManager(new GroupManager());
		}

		if(entityCreator == null){
			try {
				entityCreator = (EntityCreatorBase) ScenarioImplementation.entityCreatorClass.newInstance();
			} catch (InstantiationException e) {
				System.out.println("Error instantiating entity creator.");
				throw e;
			} catch (IllegalAccessException e) {
				System.out.println("Error accessing entity creator.");
				throw e;
			}
			entityCreator.setWorld(gameWorld);
			entityCreator.setCore(core);
		}

		if(gameLogicSystem == null){
			try {
				gameLogicSystem = (GameLogicSystemBase) ScenarioImplementation.gameLogicSystemClass.newInstance();
			} catch (InstantiationException e) {
				System.out.println("Error instantiating game logic system.");
				throw e;
			} catch (IllegalAccessException e) {
				System.out.println("Error accessing game logic system.");
				throw e;
			}
		}

		if(automaticActionPerformer == null){
			try {
				automaticActionPerformer = (AutomaticActionPerformerBase) ScenarioImplementation.automaticActionPerformerClass.newInstance();
			} catch (InstantiationException e) {
				System.out.println("Error instantiating automatic action performer.");
				throw e;
			} catch (IllegalAccessException e) {
				System.out.println("Error accessing automatic action performer.");
				throw e;
			}
		}

		if(automaticActionSummaryOverlay == null){
			try {
				automaticActionSummaryOverlay = (SummaryOverlayBase) ScenarioImplementation.automaticActionSummaryOverlay.newInstance();
			} catch (InstantiationException e) {
				System.out.println("Error instantiating automatic action summary overlay");
				throw e;
			} catch (IllegalAccessException e) {
				System.out.println("Error accessing automatic action summary overlay.");
				throw e;
			}
		}

		if(playerSystem == null){
			try {
				playerSystem = (PlayerSystemBase) ScenarioImplementation.playerSystemClass.newInstance();
			} catch (InstantiationException e) {
				System.out.println("Error instantiating player system.");
				throw e;
			} catch (IllegalAccessException e) {
				System.out.println("Error accessing player system.");
				throw e;
			}
		}

		if(scenarioSummaryOverlay == null){
			try {
				scenarioSummaryOverlay = (SummaryOverlayBase) ScenarioImplementation.scenarioSummaryOverlayClass.newInstance();
			} catch (InstantiationException e) {
				System.out.println("Error instantiating scenario summary overlay.");
				throw e;
			} catch (IllegalAccessException e) {
				System.out.println("Error accessing scenario summary overlay.");
				throw e;
			}
		}

		if(hintsOverlay == null){
			try {
				hintsOverlay = (HintsOverlayBase) ScenarioImplementation.hintsOverlayClass.newInstance();
			} catch (InstantiationException e) {
				System.out.println("Error instantiating hints overlay.");
				throw e;
			} catch (IllegalAccessException e) {
				System.out.println("Error accessing hints overlay.");
				throw e;
			}
		}

		playerSystem.setCore(core);

		gameWorld.setSystem(new MarkerPositioningSystem());
		gameWorld.setSystem(new RobotArmPositioningSystem(), Ouya.runningOnOuya);
		gameWorld.setSystem(new GeometrySystem());
		gameWorld.setSystem(new AnimationSystem());
		gameWorld.setSystem(new CollisionDetectionSystem());
		gameWorld.setSystem(gameLogicSystem);
		gameWorld.setSystem(playerSystem, true);
		gameWorld.setSystem(new MarkerRenderingSystem(modelBatch), true);
		gameWorld.setSystem(new ObjectRenderingSystem(modelBatch), true);
		gameWorld.setSystem(new FadeEffectRenderingSystem(), true);

		gameWorld.initialize();
	}

	public static void dispose() throws IllegalStateException{
		ImmutableBag<EntitySystem> systems;

		if(entityCreator == null || gameWorld == null || gameLogicSystem == null || automaticActionPerformer == null || automaticActionSummaryOverlay == null)
			throw new IllegalStateException("Calling dispose before init or after previous dispose.");

		systems = gameWorld.getSystems();

		for(int i = 0; i < systems.size(); i++){
			if(systems.get(i) instanceof Disposable){
				((Disposable)systems.get(i)).dispose();
			}
		}

		scenarioSummaryOverlay.dispose();
		automaticActionSummaryOverlay.dispose();
		entityCreator.dispose();
		hintsOverlay.dispose();

		entityCreator                 = null;
		gameLogicSystem               = null;
		gameWorld                     = null;
		automaticActionPerformer      = null;
		automaticActionSummaryOverlay = null;
		playerSystem                  = null;
		scenarioSummaryOverlay        = null;
		hintsOverlay                  = null;

		System.gc();
	}

	public static EntityCreatorBase getEntityCreator() throws IllegalStateException{
		if(entityCreator == null)
			throw new IllegalStateException("Calling getEntityCreator() before init.");

		return entityCreator;
	}

	public static GameLogicSystemBase getGameLogicSystem() throws IllegalStateException{
		if(gameLogicSystem == null)
			throw new IllegalStateException("Calling getGameLogicSystem() before init.");

		return gameLogicSystem;
	}

	public static World getGameWorld() throws IllegalStateException{
		if(gameWorld == null)
			throw new IllegalStateException("Calling getGameWorld() before init.");

		return gameWorld;
	}

	public static AutomaticActionPerformerBase getAutomaticActionPerformer() throws IllegalStateException{
		if(automaticActionPerformer == null)
			throw new IllegalStateException("Calling getAutomaticActionPerformer() before init.");

		return automaticActionPerformer;
	}

	public static SummaryOverlayBase getAutomaticActionSummaryOverlay() throws IllegalStateException{
		if(automaticActionSummaryOverlay == null)
			throw new IllegalStateException("Calling getAutomaticActionSummaryOverlay() before init.");

		return automaticActionSummaryOverlay;
	}

	public static PlayerSystemBase getPlayerSystem() throws IllegalStateException{
		if(playerSystem == null)
			throw new IllegalStateException("Calling getPlayerSystem() before init.");

		return playerSystem;
	}

	public static SummaryOverlayBase getScenarioSummaryOverlay() throws IllegalStateException{
		if(scenarioSummaryOverlay == null)
			throw new IllegalStateException("Calling getScenarioSummaryOverlay() before init.");

		return scenarioSummaryOverlay;
	}

	public static HintsOverlayBase getHintsOverlay() throws IllegalStateException{
		if(hintsOverlay == null)
			throw new IllegalStateException("Calling getHintsOverlay() before init.");

		return hintsOverlay;
	}
}
