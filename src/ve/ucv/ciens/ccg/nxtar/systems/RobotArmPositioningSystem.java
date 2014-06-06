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
package ve.ucv.ciens.ccg.nxtar.systems;

import ve.ucv.ciens.ccg.nxtar.components.GeometryComponent;
import ve.ucv.ciens.ccg.nxtar.factories.products.GamepadUserInput;
import ve.ucv.ciens.ccg.nxtar.factories.products.KeyboardUserInput;
import ve.ucv.ciens.ccg.nxtar.factories.products.TouchUserInput;
import ve.ucv.ciens.ccg.nxtar.factories.products.UserInput;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.annotations.Mapper;
import com.artemis.systems.EntityProcessingSystem;

public class RobotArmPositioningSystem extends EntityProcessingSystem {
	@Mapper ComponentMapper<GeometryComponent> geometryMapper;

	private UserInput input;

	@SuppressWarnings("unchecked")
	public RobotArmPositioningSystem(){
		super(Aspect.getAspectForAll(GeometryComponent.class));
	}

	public void setUserInput(UserInput input){
		this.input = input;
	}

	@Override
	protected void process(Entity e) {
		GeometryComponent geometry = geometryMapper.get(e);

		if(input == null) return;

		if(input instanceof TouchUserInput){

		}else if(input instanceof GamepadUserInput){

		}else if(input instanceof KeyboardUserInput){

		}else
			throw new ClassCastException("Input is not a valid UserInput instance.");

		input = null;
	}
}
