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

import com.badlogic.gdx.math.Vector3;

public class TouchUserInput extends UserInput {
	public Vector3 userTouchEndPoint;

	public TouchUserInput(){
		this.userTouchEndPoint = new Vector3();
	}

	public TouchUserInput(Vector3 userTouchEndPoint){
		this.userTouchEndPoint = new Vector3(userTouchEndPoint);
	}

	public TouchUserInput(float x, float y, float z){
		this.userTouchEndPoint = new Vector3(x, y, z);
	}
}
