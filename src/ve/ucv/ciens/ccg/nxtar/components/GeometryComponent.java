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
import com.badlogic.gdx.math.Matrix3;
import com.badlogic.gdx.math.Vector3;

public class GeometryComponent extends Component {
	public Vector3 position;
	public Matrix3 rotation;
	public Vector3 scaling;

	public GeometryComponent(){
		this.position = new Vector3();
		this.rotation = new Matrix3();
		this.scaling = new Vector3(1.0f, 1.0f, 1.0f);
	}

	public GeometryComponent(Vector3 position, Matrix3 rotation, Vector3 scaling){
		this.position = new Vector3(position);
		this.rotation = new Matrix3(rotation);
		this.scaling = new Vector3(scaling);
	}
}
