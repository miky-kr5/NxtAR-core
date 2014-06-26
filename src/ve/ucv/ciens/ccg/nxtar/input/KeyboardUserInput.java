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
package ve.ucv.ciens.ccg.nxtar.input;

public class KeyboardUserInput extends UserInput {
	public boolean      keyLeft;
	public boolean      keyRight;
	public boolean      keyUp;
	public boolean      keyDown;
	public boolean      keySpace;

	public KeyboardUserInput(){
		this.keyLeft     = false;
		this.keyRight    = false;
		this.keyUp       = false;
		this.keyDown     = false;
		this.keySpace    = false;
	}
}
