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

public class BombComponent extends Component {
	public enum bomb_type_t{
		COMBINATION(0), INCLINATION(1), WIRES(2);

		private int value;

		private bomb_type_t(int value){
			this.value = value;
		}

		public int getValue(){
			return this.value;
		}
	};

	public int id;
	public bomb_type_t bombType;
	public boolean enabled;

	public BombComponent(int id, bomb_type_t bomb_type){
		this.id = id;
		this.bombType = bomb_type;
		this.enabled = true;
	}

	public BombComponent(BombComponent bomb){
		this.id = bomb.id;
		this.bombType = bomb.bombType;
		this.enabled = bomb.enabled;
	}
}
