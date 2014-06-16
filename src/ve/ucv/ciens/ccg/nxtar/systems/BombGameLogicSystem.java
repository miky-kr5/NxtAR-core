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
	private GroupManager                manager;

	@SuppressWarnings("unchecked")
	public BombGameLogicSystem(){
		super(Aspect.getAspectForAll(BombGameObjectTypeComponent.class));
		manager =  world.getManager(GroupManager.class);
	}

	@Override
	protected void process(Entity e){
		BombGameObjectTypeComponent typeComponent;

		typeComponent = typeMapper.get(e);

		switch(typeComponent.type){
		case BombGameObjectTypeComponent.BOMB_WIRE_1:
		case BombGameObjectTypeComponent.BOMB_WIRE_2:
		case BombGameObjectTypeComponent.BOMB_WIRE_3:
			processWireBomb(e);
			break;

		case BombGameObjectTypeComponent.BIG_BUTTON:
			processInclinationBomb(e);
			break;

		case BombGameObjectTypeComponent.COM_BUTTON_1:
		case BombGameObjectTypeComponent.COM_BUTTON_2:
		case BombGameObjectTypeComponent.COM_BUTTON_3:
		case BombGameObjectTypeComponent.COM_BUTTON_4:
			processCombinationBomb(e);
			break;

		case BombGameObjectTypeComponent.DOOR:
			processDoor(e);
			break;

		default:
			break;
		}
	}

	/**
	 * <p>Checks if the current player interaction disables a wire based bomb.</p>
	 * 
	 * @param b An Artemis {@link Entity} that possibly represents any of a Wire Bomb's wires.
	 */
	private void processWireBomb(Entity b){
		int                         relatedWires = 0;
		CollisionDetectionComponent collision;
		MarkerCodeComponent         marker;
		BombGameObjectTypeComponent wireType;
		ImmutableBag<Entity>        related;

		// Get this wire's parameters.
		collision  = collisionMapper.getSafe(b);
		marker     = markerMapper.getSafe(b);
		wireType   = typeMapper.getSafe(b);

		// if any of the parameters is missing then skip.
		if(marker == null || collision == null || wireType == null){
			Gdx.app.log(TAG, CLASS_NAME + ".processInclinationBomb(): Wire bomb is missing some components.");
			return;
		}

		// If this bomb is still enabled and it's door is already open then process it.
		if(marker.enabled && isDoorOpen(marker.code, manager) && collision.colliding){
			manager.remove(b, CollisionDetectionSystem.COLLIDABLE_OBJECTS_GROUP);
			manager.remove(b, Integer.toString(marker.code));
			b.deleteFromWorld();
			related = manager.getEntities(Integer.toString(marker.code));

			// Check the state of the other wires associated with this bomb.
			for(int i = 0; i < related.size(); i++){
				tempType = typeMapper.getSafe(related.get(i));

				if(tempType == null) continue;

				if(tempType.type >= BombGameObjectTypeComponent.BOMB_WIRE_1 && tempType.type <= BombGameObjectTypeComponent.BOMB_WIRE_3){
					if(tempType.type != wireType.type){
						relatedWires++;
					}
				}
			}

			if(relatedWires == 0)
				disableBomb(marker.code);
		}
	}

	/**
	 * <p>Checks if the current player interaction disables a combination bomb.</p>
	 * 
	 * @param b An Artemis {@link Entity} that possibly represents any of a Combination Bomb's buttons.
	 */
	private void processCombinationBomb(Entity b){
		int                         relatedButtons = 0;
		CollisionDetectionComponent collision;
		MarkerCodeComponent         marker;
		BombGameObjectTypeComponent buttonType;
		ImmutableBag<Entity>        related;

		// Get this wire's parameters.
		collision  = collisionMapper.getSafe(b);
		marker     = markerMapper.getSafe(b);
		buttonType   = typeMapper.getSafe(b);

		// if any of the parameters is missing then skip.
		if(marker == null || collision == null || buttonType == null){
			Gdx.app.log(TAG, CLASS_NAME + ".processInclinationBomb(): Wire bomb is missing some components.");
			return;
		}

		// If this bomb is still enabled and it's door is already open then process it.
		if(marker.enabled && isDoorOpen(marker.code, manager) && collision.colliding){
			manager.remove(b, CollisionDetectionSystem.COLLIDABLE_OBJECTS_GROUP);
			manager.remove(b, Integer.toString(marker.code));
			b.deleteFromWorld();
			related = manager.getEntities(Integer.toString(marker.code));

			// Check the state of the other wires associated with this bomb.
			for(int i = 0; i < related.size(); i++){
				tempType = typeMapper.getSafe(related.get(i));

				if(tempType == null) continue;

				if(tempType.type >= BombGameObjectTypeComponent.COM_BUTTON_1 && tempType.type <= BombGameObjectTypeComponent.COM_BUTTON_4){
					if(tempType.type != buttonType.type){
						relatedButtons++;
					}
				}
			}

			if(relatedButtons == 0)
				disableBomb(marker.code);
		}
	}

	/**
	 * <p>Checks if the current player interaction disables an inclination bomb.</p>
	 * 
	 * @param b An Artemis {@link Entity} that possibly represents an Inclination Bomb's big button.
	 */
	private void processInclinationBomb(Entity b){
		// Get the components of the big button.
		CollisionDetectionComponent collision  = collisionMapper.getSafe(b);
		MarkerCodeComponent         marker     = markerMapper.getSafe(b);

		// If any of the components is missing, skip this entity.
		if(marker == null || collision == null ){
			Gdx.app.log(TAG, CLASS_NAME + ".processInclinationBomb(): Inclination bomb is missing some components.");
			return;
		}

		// If this bomb is still enabled and it's door is already open then process it.
		if(marker.enabled && isDoorOpen(marker.code, manager) && collision.colliding){
			// Disable the bomb and remove it from collision detection.
			marker.enabled = false;
			manager.remove(b, CollisionDetectionSystem.COLLIDABLE_OBJECTS_GROUP);
			manager.remove(b, Integer.toString(marker.code));
			b.deleteFromWorld();

			// Disable all related entities.
			disableBomb(marker.code);

			Gdx.app.log(TAG, CLASS_NAME + ".processInclinationBomb(): Inclination bomb disabled.");
		}
	}

	/**
	 * <p>Set's the animation for a door depending on it's collision and marker state.</p>
	 *
	 * @param d An Artemis {@link Entity} possibly representing a door.
	 */
	private void processDoor(Entity d){
		// Get the components of the door.
		CollisionDetectionComponent collision  = collisionMapper.getSafe(d);
		AnimationComponent          animation  = animationMapper.getSafe(d);
		VisibilityComponent         visibility = visibilityMapper.getSafe(d);
		MarkerCodeComponent         marker     = markerMapper.getSafe(d);

		// If any of the components is missing, skip this entity.
		if(marker == null || collision == null || animation == null || visibility == null){
			Gdx.app.log(TAG, CLASS_NAME + ".processDoor(): Door is missing some components.");
			return;
		}

		if(visibility.visible){
			if(marker.enabled){
				if(collision.colliding){
					// If the door is visible and enabled and the player is colliding with it then set
					// it's opening animation;
					animation.next = BombGameEntityCreator.DOOR_OPEN_ANIMATION;
					animation.loop = false;
					collision.colliding = false;
					world.getManager(GroupManager.class).remove(d, CollisionDetectionSystem.COLLIDABLE_OBJECTS_GROUP);
					Gdx.app.log(TAG, CLASS_NAME + ".processDoor(): Opening door.");
				}
			}else{
				// If the door is disabled and open, then set it's closing animation.
				if(animation.current != 0){
					animation.next = BombGameEntityCreator.DOOR_CLOSE_ANIMATION;
					animation.loop = false;
					Gdx.app.log(TAG, CLASS_NAME + ".processDoor(): Closing door.");
				}
			}
		}
	}

	/**
	 * <p>Checks if a door is either open or closed depending on the completeness of it's animation.</p>
	 * 
	 * @param markerCode The code of the door to check. Must be between 0 and 1023.
	 * @param manager An Artemis {@link GroupManager} to use to get all related entities.
	 * @return true if the opening animation of the door has finished playing.
	 */
	private boolean isDoorOpen(int markerCode, GroupManager manager){
		AnimationComponent   animation;
		boolean              doorOpen  = false;
		ImmutableBag<Entity> doors     = manager.getEntities(BombGameEntityCreator.DOORS_GROUP);

		// For every door.
		for(int i = 0; i < doors.size(); i++){
			tempMarker = markerMapper.getSafe(doors.get(i));
			animation  = animationMapper.getSafe(doors.get(i));

			if(animation == null || tempMarker == null) return false;

			// If this is the door we are looking for and it's opening animation is finished then this door is open.
			if(tempMarker.code == markerCode && animation.current == BombGameEntityCreator.DOOR_OPEN_ANIMATION && animation.controller.current.loopCount == 0){
				doorOpen = true;
				break;
			}
		}

		return doorOpen;
	}

	/**
	 * <p>Disables all entities associated with the corresponding marker code.</p>
	 * 
	 * @param markerCode
	 */
	private void disableBomb(int markerCode){
		ImmutableBag<Entity> related = manager.getEntities(Integer.toString(markerCode));

		// Disable every entity sharing this marker code except for the corresponding door frame.
		for(int i = 0; i < related.size(); i++){
			tempMarker = markerMapper.getSafe(related.get(i));
			tempType   = typeMapper.getSafe(related.get(i));

			// Enable collisions with the corresponding door frame entity. Disable collisions with other related entities.
			if(tempMarker != null) tempMarker.enabled = false;
			if(tempType != null){
				if(tempType.type != BombGameObjectTypeComponent.DOOR_FRAME){
					manager.remove(related.get(i), CollisionDetectionSystem.COLLIDABLE_OBJECTS_GROUP);
					manager.remove(related.get(i), Integer.toString(markerCode));
					related.get(i).deleteFromWorld();
				}else{
					manager.add(related.get(i), CollisionDetectionSystem.COLLIDABLE_OBJECTS_GROUP);
				}
			}
		}
	}
}
