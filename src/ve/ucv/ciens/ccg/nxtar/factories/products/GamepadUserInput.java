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
package ve.ucv.ciens.ccg.nxtar.factories.products;

public class GamepadUserInput extends UserInput {
	public float        axisLeftX;
	public float        axisLeftY;
	public float        axisRightX;
	public float        axisRightY;

	public GamepadUserInput(){
		this.axisLeftX   = 0.0f;
		this.axisLeftY   = 0.0f;
		this.axisRightX  = 0.0f;
		this.axisRightY  = 0.0f;
	}
}
