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
package ve.ucv.ciens.ccg.nxtar.game;

import ve.ucv.ciens.ccg.nxtar.interfaces.ImageProcessor.MarkerData;

public abstract class AutomaticActionPerformerBase {
	public abstract class AutomaticActionSummary{
		public abstract void reset();
	}

	public enum automatic_action_t{
		NO_ACTION,
		GO_FORWARD,
		GO_BACKWARDS,
		STOP,
		TURN_LEFT,
		TURN_RIGHT,
		BACKWARDS_LEFT,
		BACKWARDS_RIGHT,
		ROTATE_90,
		RECENTER,
		LOOK_RIGHT,
		LOOK_LEFT,
		STOP_LOOKING;
	}

	public abstract boolean                performAutomaticAction(int lightSensorReading, MarkerData markers);
	public abstract automatic_action_t     getNextAction();
	public abstract AutomaticActionSummary getSummary();
	public abstract void                   reset();
}
