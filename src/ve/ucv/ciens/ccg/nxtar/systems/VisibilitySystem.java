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

import ve.ucv.ciens.ccg.nxtar.components.CollisionModelComponent;
import ve.ucv.ciens.ccg.nxtar.components.GeometryComponent;
import ve.ucv.ciens.ccg.nxtar.components.VisibilityComponent;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.annotations.Mapper;
import com.artemis.systems.EntityProcessingSystem;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.math.collision.BoundingBox;

public class VisibilitySystem extends EntityProcessingSystem {
	@Mapper ComponentMapper<VisibilityComponent>       visibilityMapper;
	@Mapper ComponentMapper<GeometryComponent>         geometryMapper;
	@Mapper ComponentMapper<CollisionModelComponent>   collisionMapper;

	private PerspectiveCamera camera;

	@SuppressWarnings("unchecked")
	public VisibilitySystem(){
		super(Aspect.getAspectForAll(VisibilityComponent.class, CollisionModelComponent.class));
		this.camera = null;
	}

	public void setCamera(PerspectiveCamera camera){
		this.camera = camera;
	}

	@Override
	protected void process(Entity e){
		VisibilityComponent     visibility = visibilityMapper.get(e);
		CollisionModelComponent colModel   = collisionMapper.get(e);
		BoundingBox             bBox       = new BoundingBox();

		if(camera != null){
			colModel.instance.calculateBoundingBox(bBox);
			bBox.mul(colModel.instance.transform);
			visibility.visible = camera.frustum.boundsInFrustum(bBox);
		}
	}
}
