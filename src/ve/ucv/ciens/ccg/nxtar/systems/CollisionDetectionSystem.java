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

import ve.ucv.ciens.ccg.nxtar.components.BombGameObjectTypeComponent;
import ve.ucv.ciens.ccg.nxtar.components.CollisionDetectionComponent;
import ve.ucv.ciens.ccg.nxtar.components.CollisionModelComponent;
import ve.ucv.ciens.ccg.nxtar.components.MarkerCodeComponent;
import ve.ucv.ciens.ccg.nxtar.components.VisibilityComponent;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.annotations.Mapper;
import com.artemis.managers.GroupManager;
import com.artemis.systems.EntityProcessingSystem;
import com.artemis.utils.ImmutableBag;
import com.badlogic.gdx.math.collision.BoundingBox;

public class CollisionDetectionSystem extends EntityProcessingSystem {
	public static final String COLLIDABLE_OBJECT = "COLLIDABLE";

	@Mapper ComponentMapper<CollisionModelComponent>     collisionModelMapper;
	@Mapper ComponentMapper<CollisionDetectionComponent> collisionDetectionMapper;
	@Mapper ComponentMapper<VisibilityComponent>         visibilityMapper;
	@Mapper ComponentMapper<BombGameObjectTypeComponent> typeMapper;

	private GroupManager groupManager;

	@SuppressWarnings("unchecked")
	public CollisionDetectionSystem(){
		super(Aspect.getAspectForAll(CollisionModelComponent.class, CollisionDetectionComponent.class).exclude(MarkerCodeComponent.class));
	}

	@Override
	protected void process(Entity e) {
		VisibilityComponent         visibility;
		CollisionModelComponent     collision;
		CollisionModelComponent     target;
		CollisionDetectionComponent onCollision;
		CollisionDetectionComponent onCollisionTarget;
		BoundingBox                 colBB = new BoundingBox();
		BoundingBox                 targetBB = new BoundingBox();
		ImmutableBag<Entity>        collidables;

		groupManager = this.world.getManager(GroupManager.class);
		collidables = groupManager.getEntities(COLLIDABLE_OBJECT);

		collision   = collisionModelMapper.get(e);
		onCollision = collisionDetectionMapper.get(e);

		for(int i = 0; i < collidables.size(); ++i){
			target            = collisionModelMapper.getSafe(collidables.get(i));
			visibility        = visibilityMapper.getSafe(collidables.get(i));
			onCollisionTarget = collisionDetectionMapper.getSafe(collidables.get(i));

			if(target == null || visibility == null || onCollisionTarget == null) continue;

			if(visibility.visible){
				collision.instance.calculateBoundingBox(colBB);
				target.instance.calculateBoundingBox(targetBB);

				if(colBB.contains(targetBB)){
					onCollision.colliding       = true;
					onCollisionTarget.colliding = true;
				}else{
					onCollision.colliding       = false;
					onCollisionTarget.colliding = false;
				}
			}
		}
	}
}
