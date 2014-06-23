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

import com.badlogic.gdx.Gdx;

import ve.ucv.ciens.ccg.nxtar.game.AutomaticActionPerformerBase;
import ve.ucv.ciens.ccg.nxtar.interfaces.ImageProcessor.MarkerData;
import ve.ucv.ciens.ccg.nxtar.utils.ProjectConstants;

public class BombGameAutomaticActionPerformer extends AutomaticActionPerformerBase {
	private static final int GOAL_FLOOR_MIN_LUMINANCE = 85;
	private static final int MARKER_NEARBY_FLOOR_MIN_LUMINANCE = 45;

	private enum action_state_t{
		START, WALK_FORWARD, DETECT_MARKER, FINISHING, END;
	}

	private automatic_action_t nextAction;
	private action_state_t     state;
	private List<Integer>      detectedMarkers;
	private float              then;

	public BombGameAutomaticActionPerformer(){
		nextAction      = automatic_action_t.NO_ACTION;
		state           = action_state_t.START;
		detectedMarkers = new LinkedList<Integer>();
		then            = 0.0f;
	}

	@Override
	public boolean performAutomaticAction(int lightSensorReading, MarkerData markers) throws IllegalStateException, IllegalArgumentException{
		boolean finish       = false;
		int     detectedCode = -1;
		float   now, deltaT;

		if(markers == null)
			throw new IllegalArgumentException("Markers is null");

		switch(state){
		case START:
			nextAction = automatic_action_t.ROTATE_90;
			state = action_state_t.WALK_FORWARD;
			finish = false;
			break;

		case WALK_FORWARD:
			if(lightSensorReading >= GOAL_FLOOR_MIN_LUMINANCE){
				nextAction = automatic_action_t.STOP;
				state = action_state_t.END;
			}else{
				if(lightSensorReading >= MARKER_NEARBY_FLOOR_MIN_LUMINANCE && lightSensorReading < GOAL_FLOOR_MIN_LUMINANCE){
					nextAction = automatic_action_t.STOP;
					state = action_state_t.DETECT_MARKER;
					then = Gdx.graphics.getDeltaTime();
				}else{
					nextAction = automatic_action_t.GO_FORWARD;
				}
			}
			finish = false;
			break;

		case DETECT_MARKER:
			for(int i = 0; i < ProjectConstants.MAXIMUM_NUMBER_OF_MARKERS; i++){
				// Check if this marker has not been detected already.
				for(Integer code : detectedMarkers){
					if(markers.markerCodes[i] == code){
						i = ProjectConstants.MAXIMUM_NUMBER_OF_MARKERS;
						break;
					}
				}

				// If the marker has not been detected before then examine it.
				if(i < ProjectConstants.MAXIMUM_NUMBER_OF_MARKERS){
					detectedCode = markers.markerCodes[i];
					// TODO: If marker is a bomb then add it to the summary.
				}
			}

			if(detectedCode == -1)
				detectedMarkers.add(detectedCode);

			if(lightSensorReading < MARKER_NEARBY_FLOOR_MIN_LUMINANCE){
				state      = action_state_t.WALK_FORWARD;
				nextAction = automatic_action_t.STOP;
				then       = 0.0f;
			}else{
				now    = Gdx.graphics.getDeltaTime();
				deltaT = now - then;
				if(deltaT >= 2.0f){
					nextAction = automatic_action_t.GO_FORWARD;
					then = Gdx.graphics.getDeltaTime();
				}
			}

			finish = false;

			break;

		case FINISHING:
			detectedMarkers.clear();
			state = action_state_t.END;
			nextAction = automatic_action_t.RECENTER;
			finish = false;
			break;

		case END:
			state = action_state_t.START;
			nextAction = automatic_action_t.NO_ACTION;
			finish = true;
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
}
