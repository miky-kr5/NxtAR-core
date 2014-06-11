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
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.collision.BoundingBox;

public class CollisionDetectionSystem extends EntityProcessingSystem {
	public static final String COLLIDABLE_OBJECTS_GROUP = "COLLIDABLE";

	@Mapper ComponentMapper<CollisionModelComponent>     collisionModelMapper;
	@Mapper ComponentMapper<CollisionDetectionComponent> collisionDetectionMapper;
	@Mapper ComponentMapper<VisibilityComponent>         visibilityMapper;

	private GroupManager groupManager;
	private BoundingBox colBB;
	private BoundingBox targetBB;

	@SuppressWarnings("unchecked")
	public CollisionDetectionSystem(){
		super(Aspect.getAspectForAll(CollisionModelComponent.class, CollisionDetectionComponent.class).exclude(MarkerCodeComponent.class));
		colBB    = new BoundingBox();
		targetBB = new BoundingBox();
	}

	@Override
	protected void process(Entity e) {
		VisibilityComponent         visibility;
		CollisionModelComponent     collision;
		CollisionModelComponent     target;
		CollisionDetectionComponent onCollision;
		CollisionDetectionComponent onCollisionTarget;
		ImmutableBag<Entity>        collidables;

		// Get this entity's known necessary components.
		collision   = collisionModelMapper.get(e);
		onCollision = collisionDetectionMapper.get(e);

		// Get all other entities this entity can collide with. 
		groupManager = this.world.getManager(GroupManager.class);
		collidables = groupManager.getEntities(COLLIDABLE_OBJECTS_GROUP);

		for(int i = 0; i < collidables.size(); ++i){
			// Try to get the necessary components for the collidable entity.
			target            = collisionModelMapper.getSafe(collidables.get(i));
			visibility        = visibilityMapper.getSafe(collidables.get(i));
			onCollisionTarget = collisionDetectionMapper.getSafe(collidables.get(i));

			// If any of the needed components does not exist then proceed to the next entity.
			if(target == null || visibility == null || onCollisionTarget == null) continue;

			// Id the target is visible then examine the collision. Else there is no collision possible.
			if(visibility.visible){
				// Get the bounding box for both entities.
				collision.instance.calculateBoundingBox(colBB);
				target.instance.calculateBoundingBox(targetBB);

				// Apply the model matrix to the bounding boxes.
				colBB.mul(collision.instance.transform);
				targetBB.mul(target.instance.transform);

				// If the bounding boxes intersect then there is a collision.
				if(colBB.intersects(targetBB) || targetBB.intersects(colBB)){
					Gdx.app.log("TAG", "Collision hit.");
					onCollision.colliding       = true;
					onCollisionTarget.colliding = true;
					break;
				}else{
					Gdx.app.log("TAG", "Collision miss.");
					onCollision.colliding       = false;
					onCollisionTarget.colliding = false;
				}
			}else{
				onCollision.colliding       = false;
				onCollisionTarget.colliding = false;
			}
		}
	}
}
