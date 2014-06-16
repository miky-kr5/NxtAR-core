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
package ve.ucv.ciens.ccg.nxtar.components;

import com.artemis.Component;
import com.badlogic.gdx.math.Vector3;

public class AutomaticMovementComponent extends Component{
	public boolean moving;
	public boolean forward;
	public float   distance;
	public Vector3 startPoint;
	public Vector3 endPoint;

	public AutomaticMovementComponent(Vector3 startPoint, Vector3 endPoint, boolean moving){
		this.moving     = moving;
		this.forward    = true;
		this.distance   = 0.0f;
		this.startPoint = startPoint;
		this.endPoint   = endPoint;
	}

	public AutomaticMovementComponent(Vector3 startPoint, Vector3 endPoint){
		this(startPoint, endPoint, false);
	}

	public AutomaticMovementComponent(boolean moving){
		this(new Vector3(0.0f, 0.0f, 0.0f), new Vector3(), moving);
	}

	public AutomaticMovementComponent(Vector3 endPoint){
		this(new Vector3(0.0f, 0.0f, 0.0f), endPoint, false);
	}

	public AutomaticMovementComponent(){
		this(false);
	}
}
