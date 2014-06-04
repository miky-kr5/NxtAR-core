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

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.annotations.Mapper;
import com.artemis.systems.EntityProcessingSystem;

public class ObjectPositioningSystem extends EntityProcessingSystem {
	@Mapper ComponentMapper<GeometryComponent> geometryMapper;

	@SuppressWarnings("unchecked")
	public ObjectPositioningSystem(){
		super(Aspect.getAspectForAll(GeometryComponent.class));
	}

	public void setUserInput(){
		// TODO: Desing a representation for user input.
		// TODO: Store user input for processing.
	}

	@Override
	protected void process(Entity e) {
		GeometryComponent geometry = geometryMapper.get(e);
		// TODO: Set the geometry fields based on user input.
	}
}
