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

import ve.ucv.ciens.ccg.nxtar.components.AnimationComponent;
import ve.ucv.ciens.ccg.nxtar.components.BombGameObjectTypeComponent;
import ve.ucv.ciens.ccg.nxtar.components.CollisionDetectionComponent;
import ve.ucv.ciens.ccg.nxtar.components.MarkerCodeComponent;
import ve.ucv.ciens.ccg.nxtar.components.VisibilityComponent;
import ve.ucv.ciens.ccg.nxtar.entities.BombGameEntityCreator;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.annotations.Mapper;
import com.artemis.managers.GroupManager;
import com.artemis.utils.ImmutableBag;
import com.badlogic.gdx.Gdx;

public class BombGameLogicSystem extends GameLogicSystemBase {
	private static final String TAG        = "BOMB_GAME_LOGIC";
	private static final String CLASS_NAME = BombGameLogicSystem.class.getSimpleName();

	@Mapper ComponentMapper<BombGameObjectTypeComponent> typeMapper;
	@Mapper ComponentMapper<AnimationComponent>          animationMapper;
	@Mapper ComponentMapper<VisibilityComponent>         visibilityMapper;
	@Mapper ComponentMapper<MarkerCodeComponent>         markerMapper;
	@Mapper ComponentMapper<CollisionDetectionComponent> collisionMapper;

	private MarkerCodeComponent         tempMarker;
	private BombGameObjectTypeComponent tempType;

	@SuppressWarnings("unchecked")
	public BombGameLogicSystem(){
		super(Aspect.getAspectForAll(BombGameObjectTypeComponent.class));
	}

	@Override
	protected void process(Entity e){
		BombGameObjectTypeComponent typeComponent;

		typeComponent = typeMapper.get(e);

		switch(typeComponent.type){
		case BombGameObjectTypeComponent.BOMB_WIRE_1:
			break;
		case BombGameObjectTypeComponent.BOMB_WIRE_2:
			break;
		case BombGameObjectTypeComponent.BOMB_WIRE_3:
			break;
		case BombGameObjectTypeComponent.BIG_BUTTON:
			processInclinationBomb(e);
			break;
		case BombGameObjectTypeComponent.COM_BUTTON_1:
			break;
		case BombGameObjectTypeComponent.COM_BUTTON_2:
			break;
		case BombGameObjectTypeComponent.COM_BUTTON_3:
			break;
		case BombGameObjectTypeComponent.COM_BUTTON_4:
			break;
		case BombGameObjectTypeComponent.DOOR:
			processDoor(e);
			break;
		case BombGameObjectTypeComponent.DOOR_FRAME:
			break;
		default:
			Gdx.app.debug(TAG, CLASS_NAME + ".process(): Unrecognized object type.");
			break;
		}
	}

	private void processInclinationBomb(Entity b){
		CollisionDetectionComponent collision  = collisionMapper.getSafe(b);
		MarkerCodeComponent         marker     = markerMapper.getSafe(b);
		GroupManager                manager    =  world.getManager(GroupManager.class);
		ImmutableBag<Entity>        related;

		if(marker == null || collision == null ){
			Gdx.app.log(TAG, CLASS_NAME + ".processInclinationBomb(): Inclination bomb is missing some components.");
			return;
		}

		if(isDoorOpen(marker.code, manager) && marker.enabled && collision.colliding){
			marker.enabled = false;
			manager.remove(b, CollisionDetectionSystem.COLLIDABLE_OBJECTS_GROUP);

			// Disable all related entities.
			related = manager.getEntities(Integer.toString(marker.code));
			for(int i = 0; i < related.size(); i++){
				tempMarker = markerMapper.getSafe(related.get(i));
				tempType   = typeMapper.getSafe(related.get(i));

				// Enable collisions with the door frame. Disable collisions with other related objects.
				if(tempMarker != null) tempMarker.enabled = false;
				if(tempType != null){
					if(tempType.type != BombGameObjectTypeComponent.DOOR_FRAME){
						manager.remove(related.get(i), CollisionDetectionSystem.COLLIDABLE_OBJECTS_GROUP);
					}else{
						manager.add(related.get(i), CollisionDetectionSystem.COLLIDABLE_OBJECTS_GROUP);
					}
				}
			}

			Gdx.app.log(TAG, CLASS_NAME + ".processInclinationBomb(): Disabling inclination bomb.");
		}
	}

	private void processDoor(Entity d){
		CollisionDetectionComponent collision  = collisionMapper.getSafe(d);
		AnimationComponent          animation  = animationMapper.getSafe(d);
		VisibilityComponent         visibility = visibilityMapper.getSafe(d);
		MarkerCodeComponent         marker     = markerMapper.getSafe(d);

		if(marker == null || collision == null || animation == null || visibility == null){
			Gdx.app.log(TAG, CLASS_NAME + ".processDoor(): Door is missing some components.");
			return;
		}

		if(visibility.visible){
			if(marker.enabled){
				if(collision.colliding){
					animation.next = 1;
					animation.loop = false;
					collision.colliding = false;
					world.getManager(GroupManager.class).remove(d, CollisionDetectionSystem.COLLIDABLE_OBJECTS_GROUP);
					Gdx.app.log(TAG, CLASS_NAME + ".processDoor(): Opening door.");
				}
			}else{
				if(animation.current != 0){
					animation.next = 0;
					animation.loop = false;
					Gdx.app.log(TAG, CLASS_NAME + ".processDoor(): Closing door.");
				}
			}
		}
	}

	private boolean isDoorOpen(int markerCode, GroupManager manager){
		AnimationComponent   animation;
		boolean              doorOpen  = false;
		ImmutableBag<Entity> doors     = manager.getEntities(BombGameEntityCreator.DOORS_GROUP);

		for(int i = 0; i < doors.size(); i++){
			tempMarker = markerMapper.getSafe(doors.get(i));
			animation  = animationMapper.getSafe(doors.get(i));

			if(animation == null || tempMarker == null) return false;

			if(tempMarker.code == markerCode && animation.current == 1 && animation.controller.current.loopCount == 0){
				doorOpen = true;
				break;
			}
		}

		return doorOpen;
	}
}
