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
package ve.ucv.ciens.ccg.nxtar.game.bombgame;

import java.util.LinkedList;
import java.util.List;

import ve.ucv.ciens.ccg.nxtar.game.AutomaticActionPerformerBase;
import ve.ucv.ciens.ccg.nxtar.game.GameGlobals;
import ve.ucv.ciens.ccg.nxtar.interfaces.ImageProcessor.MarkerData;
import ve.ucv.ciens.ccg.nxtar.utils.ProjectConstants;

import com.artemis.Entity;
import com.artemis.World;
import com.artemis.managers.GroupManager;
import com.artemis.utils.ImmutableBag;
import com.badlogic.gdx.Gdx;

public class BombGameAutomaticActionPerformer extends AutomaticActionPerformerBase {
	private static final String TAG                               = "BOMB_GAME_AUTO_PERFORMER";
	private static final String CLASS_NAME                        = BombGameAutomaticActionPerformer.class.getSimpleName();
	private static final int    GOAL_FLOOR_MIN_LUMINANCE          = 75;
	private static final int    MARKER_NEARBY_FLOOR_MIN_LUMINANCE = 45;

	private enum action_state_t{
		START, WALK_FORWARD, DETECT_MARKER, FINISHING, END;
	}

	public class BombGameAutomaticActionSummary extends AutomaticActionSummary{
		private int numCombinationBombs;
		private int numInclinationBombs;
		private int numWireBombs;

		public BombGameAutomaticActionSummary(){
			reset();
		}

		public int getNumCombinationBombs() {
			return numCombinationBombs;
		}

		public int getNumInclinationBombs() {
			return numInclinationBombs;
		}

		public int getNumWireBombs() {
			return numWireBombs;
		}

		public void addCombinationBomb(){
			numCombinationBombs++;
		}

		public void addInclinationBomb(){
			numInclinationBombs++;
		}

		public void addWireBomb(){
			numWireBombs++;
		}

		@Override
		public void reset() {
			this.numCombinationBombs = 0;
			this.numInclinationBombs = 0;
			this.numWireBombs        = 0;
		}
	}

	private automatic_action_t             nextAction;
	private action_state_t                 state;
	private List<Integer>                  detectedMarkers;
	private float                          then;
	private float                          now;
	private GroupManager                   manager;
	private BombGameAutomaticActionSummary summary;

	public BombGameAutomaticActionPerformer(){
		nextAction      = automatic_action_t.NO_ACTION;
		state           = action_state_t.START;
		detectedMarkers = new LinkedList<Integer>();
		then            = 0.0f;
		now             = 0.0f;
		manager         = null;
		summary         = new BombGameAutomaticActionSummary();
	}

	@Override
	public boolean performAutomaticAction(int lightSensorReading, MarkerData markers) throws IllegalStateException, IllegalArgumentException{
		BombComponent               bomb;
		boolean                     finish         = false;
		boolean                     markerDetected = false;
		int                         detectedCode   = -1;
		ImmutableBag<Entity>        entities       = null;
		float                       deltaT;
		World                       world;

		if(manager == null){
			world = GameGlobals.getGameWorld();
			if(world == null)
				throw new IllegalStateException("World is null after getGameWorld().");

			manager = world.getManager(GroupManager.class);
			if(manager == null)
				throw new IllegalStateException("World has no group managers.");
		}

		if(markers == null)
			throw new IllegalArgumentException("Markers is null");

		switch(state){
		case START:
			Gdx.app.log(TAG, CLASS_NAME + ".performAutomaticAction(): State is START.");
			summary.reset();
			nextAction = automatic_action_t.ROTATE_90;
			state = action_state_t.WALK_FORWARD;
			finish = false;
			break;

		case WALK_FORWARD:
			Gdx.app.log(TAG, CLASS_NAME + ".performAutomaticAction(): State is WALK_FORWARD.");
			if(lightSensorReading >= GOAL_FLOOR_MIN_LUMINANCE){
				Gdx.app.log(TAG, CLASS_NAME + ".performAutomaticAction(): Found goal.");
				nextAction = automatic_action_t.STOP;
				state = action_state_t.FINISHING;
			}else{
				if(lightSensorReading >= MARKER_NEARBY_FLOOR_MIN_LUMINANCE && lightSensorReading < GOAL_FLOOR_MIN_LUMINANCE){
					Gdx.app.log(TAG, CLASS_NAME + ".performAutomaticAction(): There is a marker nearby.");
					nextAction = automatic_action_t.STOP;
					state = action_state_t.DETECT_MARKER;
					then = Gdx.graphics.getDeltaTime();
				}else{
					Gdx.app.log(TAG, CLASS_NAME + ".performAutomaticAction(): Walking.");
					nextAction = automatic_action_t.GO_FORWARD;
				}
			}
			finish = false;
			break;

		case DETECT_MARKER:
			Gdx.app.log(TAG, CLASS_NAME + ".performAutomaticAction(): State is DETECT_MARKER.");
			for(int i = 0; !markerDetected && i < ProjectConstants.MAXIMUM_NUMBER_OF_MARKERS; i++){
				// Check if this marker has not been detected already.
				for(Integer code : detectedMarkers){
					if(markers.markerCodes[i] == code){
						Gdx.app.log(TAG, CLASS_NAME + ".performAutomaticAction(): Marker already detected.");
						markerDetected = true;
						break;
					}
				}

				// If the marker has not been detected before then examine it.
				if(!markerDetected){
					Gdx.app.log(TAG, CLASS_NAME + ".performAutomaticAction(): New marker detected.");
					detectedCode = markers.markerCodes[i];
					entities = manager.getEntities(Integer.toString(detectedCode));

					for(int e = 0; entities != null && e < entities.size() && entities.get(e) != null; e++){
						bomb = entities.get(e).getComponent(BombComponent.class);

						if(bomb == null){
							Gdx.app.log(TAG, CLASS_NAME + ".performAutomaticAction(): Entity has no bomb component. Skipping.");
						}else{
							Gdx.app.log(TAG, CLASS_NAME + ".performAutomaticAction(): Adding bomb.");
							switch(bomb.bombType){
							case COMBINATION:
								summary.addCombinationBomb();
								break;
							case INCLINATION:
								summary.addInclinationBomb();
								break;
							case WIRES:
								summary.addWireBomb();
								break;
							default:
								throw new IllegalStateException("Unrecognized bomb type.");
							}
							break;
						}
					}

					break;
				}
			}

			if(!markerDetected && detectedCode != -1)
				detectedMarkers.add(detectedCode);

			if(lightSensorReading < MARKER_NEARBY_FLOOR_MIN_LUMINANCE){
				Gdx.app.log(TAG, CLASS_NAME + ".performAutomaticAction(): Switching to WALK_FORWARD.");
				state      = action_state_t.WALK_FORWARD;
				nextAction = automatic_action_t.STOP;
				then       = 0.0f;
				now        = 0.0f;
			}else{
				Gdx.app.log(TAG, CLASS_NAME + ".performAutomaticAction(): Clearing MARKER_NEARBY_FLOOR.");
				now    += Gdx.graphics.getDeltaTime();
				deltaT  = now - then;
				if(deltaT >= 2.0f){
					nextAction = automatic_action_t.GO_FORWARD;
					then = Gdx.graphics.getDeltaTime();
					now  = 0.0f;
				}
			}

			finish = false;

			break;

		case FINISHING:
			Gdx.app.log(TAG, CLASS_NAME + ".performAutomaticAction(): State is FINISHING.");
			state      = action_state_t.END;
			nextAction = automatic_action_t.RECENTER;
			finish     = false;
			break;

		case END:
			Gdx.app.log(TAG, CLASS_NAME + ".performAutomaticAction(): State is END.");
			nextAction = automatic_action_t.NO_ACTION;
			state      = action_state_t.START;
			finish     = true;
			now        = 0.0f;
			then       = 0.0f;
			detectedMarkers.clear();
			break;

		default:
			throw new IllegalStateException("Unknown automatic action state.");
		}

		return finish;
	}

	@Override
	public automatic_action_t getNextAction() {
		switch(nextAction){
		default:
		case NO_ACTION:
			return automatic_action_t.NO_ACTION;
		case GO_BACKWARDS:
			return automatic_action_t.GO_BACKWARDS;
		case GO_FORWARD:
			return automatic_action_t.GO_FORWARD;
		case STOP:
			return automatic_action_t.STOP;
		case ROTATE_90:
			return automatic_action_t.ROTATE_90;
		case RECENTER:
			return automatic_action_t.RECENTER;
		}
	}

	@Override
	public AutomaticActionSummary getSummary() {
		return (AutomaticActionSummary)summary;
	}

	@Override
	public void reset() {
		Gdx.app.log(TAG, CLASS_NAME + ".reset(): Reset requested.");
		detectedMarkers.clear();
		summary.reset();
		state      = action_state_t.START;
		nextAction = automatic_action_t.NO_ACTION;
		then       = 0.0f;
	}
}
