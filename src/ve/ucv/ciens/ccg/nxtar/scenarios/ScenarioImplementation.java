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

import ve.ucv.ciens.ccg.nxtar.scenarios.bombgame.BombGameAutomaticActionPerformer;
import ve.ucv.ciens.ccg.nxtar.scenarios.bombgame.BombGameAutomaticActionSummaryOverlay;
import ve.ucv.ciens.ccg.nxtar.scenarios.bombgame.BombGameEntityCreator;
import ve.ucv.ciens.ccg.nxtar.scenarios.bombgame.BombGameInstructionsOverlay;
import ve.ucv.ciens.ccg.nxtar.scenarios.bombgame.BombGameLogicSystem;
import ve.ucv.ciens.ccg.nxtar.scenarios.bombgame.BombGamePlayerSystem;
import ve.ucv.ciens.ccg.nxtar.scenarios.bombgame.BombGameScenarioEndingOverlay;

@SuppressWarnings("rawtypes")
public final class ScenarioImplementation{
	public static final Class gameLogicSystemClass           = BombGameLogicSystem.class;
	public static final Class entityCreatorClass             = BombGameEntityCreator.class;
	public static final Class automaticActionPerformerClass  = BombGameAutomaticActionPerformer.class;
	public static final Class automaticActionSummaryOverlay  = BombGameAutomaticActionSummaryOverlay.class;
	public static final Class playerSystemClass              = BombGamePlayerSystem.class;
	public static final Class scenarioSummaryOverlayClass    = BombGameScenarioEndingOverlay.class;
	public static final Class hintsOverlayClass              = BombGameInstructionsOverlay.class;

	private ScenarioImplementation(){}
}
