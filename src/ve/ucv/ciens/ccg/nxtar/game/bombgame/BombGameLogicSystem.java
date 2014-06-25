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
package ve.ucv.ciens.ccg.nxtar.game.bombgame;

import ve.ucv.ciens.ccg.nxtar.components.AnimationComponent;
import ve.ucv.ciens.ccg.nxtar.components.CollisionDetectionComponent;
import ve.ucv.ciens.ccg.nxtar.components.FadeEffectComponent;
import ve.ucv.ciens.ccg.nxtar.components.MarkerCodeComponent;
import ve.ucv.ciens.ccg.nxtar.components.PlayerComponentBase;
import ve.ucv.ciens.ccg.nxtar.components.VisibilityComponent;
import ve.ucv.ciens.ccg.nxtar.systems.CollisionDetectionSystem;
import ve.ucv.ciens.ccg.nxtar.systems.GameLogicSystemBase;
import ve.ucv.ciens.ccg.nxtar.utils.ProjectConstants;
import ve.ucv.ciens.ccg.nxtar.utils.Utils;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.World;
import com.artemis.annotations.Mapper;
import com.artemis.managers.GroupManager;
import com.artemis.utils.ImmutableBag;
import com.badlogic.gdx.Gdx;

public class BombGameLogicSystem extends GameLogicSystemBase {
	private static final String TAG        = "BOMB_GAME_LOGIC";
	private static final String CLASS_NAME = BombGameLogicSystem.class.getSimpleName();

	private enum combination_button_state_t{
		CORRECT(0), INCORRECT(1), DISABLED(2);

		private int value;

		private combination_button_state_t(int value){
			this.value = value;
		}

		public int getValue(){
			return this.value;
		}
	}

	@Mapper ComponentMapper<BombGameEntityTypeComponent> typeMapper;
	@Mapper ComponentMapper<AnimationComponent>          animationMapper;
	@Mapper ComponentMapper<VisibilityComponent>         visibilityMapper;
	@Mapper ComponentMapper<MarkerCodeComponent>         markerMapper;
	@Mapper ComponentMapper<CollisionDetectionComponent> collisionMapper;
	@Mapper ComponentMapper<FadeEffectComponent>         fadeMapper;

	private MarkerCodeComponent         tempMarker;
	private BombGameEntityTypeComponent tempType;
	private GroupManager                manager;
	private int                         then;

	@SuppressWarnings("unchecked")
	public BombGameLogicSystem(){
		super(Aspect.getAspectForAll(BombGameEntityTypeComponent.class));
		manager = null;
		then = 0;
	}

	@Override
	protected void process(Entity e){
		BombGameEntityTypeComponent typeComponent;

		if(manager == null)
			manager = world.getManager(GroupManager.class);

		typeComponent = typeMapper.get(e);

		switch(typeComponent.type){
		case BombGameEntityTypeComponent.BOMB_WIRE_1:
		case BombGameEntityTypeComponent.BOMB_WIRE_2:
		case BombGameEntityTypeComponent.BOMB_WIRE_3:
			processWireBomb(e);
			break;

		case BombGameEntityTypeComponent.BIG_BUTTON:
			processInclinationBomb(e);
			break;

		case BombGameEntityTypeComponent.COM_BUTTON_1:
		case BombGameEntityTypeComponent.COM_BUTTON_2:
		case BombGameEntityTypeComponent.COM_BUTTON_3:
		case BombGameEntityTypeComponent.COM_BUTTON_4:
			processCombinationBomb(e);
			break;

		case BombGameEntityTypeComponent.DOOR:
			processDoor(e);
			break;

		case BombGameEntityTypeComponent.FADE_EFFECT:
			processFade(e);
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
		CollisionDetectionComponent collision;
		MarkerCodeComponent         marker;
		BombGameEntityTypeComponent wireType;

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
		try{
			if(marker.enabled && isDoorOpen(marker.code, manager) && collision.colliding){
				manager.remove(b, CollisionDetectionSystem.COLLIDABLE_OBJECTS_GROUP);
				manager.remove(b, Integer.toString(marker.code));
				b.deleteFromWorld();

				if(wireType.type != BombGameEntityTypeComponent.BOMB_WIRE_1){
					Gdx.app.log(TAG, CLASS_NAME + ".processWireBomb(): Wire bomb exploded.");
					createFadeOutEffect();
					reducePlayerLivesByOne();
				}

				disableBomb(marker.code);
				Gdx.app.log(TAG, CLASS_NAME + ".processWireBomb(): Wire bomb disabled.");
			}
		}catch(IllegalArgumentException e){
			Gdx.app.error(TAG, CLASS_NAME + ".processWireBomb(): IllegalArgumentException caught: " + e.getMessage());
		}
	}

	/**
	 * <p>Checks if the current player interaction disables a combination bomb.</p>
	 * 
	 * @param b An Artemis {@link Entity} that possibly represents any of a Combination Bomb's buttons.
	 */
	private void processCombinationBomb(Entity b){
		combination_button_state_t  state;
		CollisionDetectionComponent collision;
		MarkerCodeComponent         marker;
		BombGameEntityTypeComponent buttonType;

		// Get this wire's parameters.
		collision  = collisionMapper.getSafe(b);
		marker     = markerMapper.getSafe(b);
		buttonType = typeMapper.getSafe(b);

		// if any of the parameters is missing then skip.
		if(marker == null || collision == null || buttonType == null){
			Gdx.app.log(TAG, CLASS_NAME + ".processInclinationBomb(): Wire bomb is missing some components.");
			return;
		}

		// If this bomb is still enabled and it's door is already open then process it.
		try{
			if(marker.enabled && isDoorOpen(marker.code, manager) && collision.colliding){
				manager.remove(b, CollisionDetectionSystem.COLLIDABLE_OBJECTS_GROUP);
				manager.remove(b, Integer.toString(marker.code));
				b.deleteFromWorld();

				// Check the state of the other buttons associated with this bomb.

				state = checkCombinationBombButtons(buttonType.type, marker.code);

				if(state.getValue() == combination_button_state_t.INCORRECT.getValue()){
					Gdx.app.log(TAG, CLASS_NAME + ".processCombinationBomb(): Combination bomb exploded.");
					createFadeOutEffect();
					disableBomb(marker.code);
					reducePlayerLivesByOne();
				}else if(state.getValue() == combination_button_state_t.DISABLED.getValue()){
					Gdx.app.log(TAG, CLASS_NAME + ".processCombinationBomb(): Combination bomb disabled.");
					disableBomb(marker.code);
				}
			}
		}catch(IllegalArgumentException e){
			Gdx.app.error(TAG, CLASS_NAME + ".processCombinationBomb(): IllegalArgumentException caught: " + e.getMessage());
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
		try{
			if(marker.enabled && isDoorOpen(marker.code, manager) && collision.colliding){
				// Disable the bomb and remove it from collision detection.
				marker.enabled = false;
				manager.remove(b, CollisionDetectionSystem.COLLIDABLE_OBJECTS_GROUP);
				manager.remove(b, Integer.toString(marker.code));
				b.deleteFromWorld();

				if(Utils.isDeviceRollValid() && Math.abs(Gdx.input.getRoll()) > ProjectConstants.MAX_ABS_ROLL){
					Gdx.app.log(TAG, CLASS_NAME + ".processInclinationBomb(): Inclination bomb exploded.");
					createFadeOutEffect();
					reducePlayerLivesByOne();
				}

				// Disable all related entities.
				disableBomb(marker.code);

				Gdx.app.log(TAG, CLASS_NAME + ".processInclinationBomb(): Inclination bomb disabled.");
			}
		}catch(IllegalArgumentException e){
			Gdx.app.error(TAG, CLASS_NAME + ".processInclinationBomb(): IllegalArgumentException caught: " + e.getMessage());
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
				if(animation.current != BombGameEntityCreator.DOOR_CLOSE_ANIMATION){
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
	 * @throws IllegalArgumentException If marker code is not in the range [0, 1023], inclusive.
	 */
	private boolean isDoorOpen(int markerCode, GroupManager manager) throws IllegalArgumentException{
		AnimationComponent   animation;
		boolean              doorOpen  = false;
		ImmutableBag<Entity> doors     = manager.getEntities(BombGameEntityCreator.DOORS_GROUP);

		if(markerCode < 0 || markerCode > 1023)
			throw new IllegalArgumentException("Marker code is not within range [0, 1023]: " + Integer.toString(markerCode));

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
	 * <p>Updates the player's lives count.</p>
	 */
	private void reducePlayerLivesByOne(){
		Entity                  player;
		BombGamePlayerComponent playerComponent;
		ImmutableBag<Entity>    players;

		players = manager.getEntities(PlayerComponentBase.PLAYER_GROUP);
		if(players !=  null && players.size() > 0 && players.get(0) != null){
			player = players.get(0);
			playerComponent = player.getComponent(BombGamePlayerComponent.class);

			if(playerComponent != null){
				playerComponent.lives -= 1;
			}else{
				Gdx.app.log(TAG, CLASS_NAME + ".reducePlayerLivesByOne(): Players is missing required components.");
			}

		}else{
			Gdx.app.log(TAG, CLASS_NAME + ".reducePlayerLivesByOne(): No players found.");
		}
	}

	/**
	 * <p>Disables all entities associated with the corresponding marker code.</p>
	 * 
	 * @param markerCode
	 * @throws IllegalArgumentException If marker code is not in the range [0, 1023], inclusive.
	 */
	private void disableBomb(int markerCode) throws IllegalArgumentException{
		ImmutableBag<Entity> related = manager.getEntities(Integer.toString(markerCode));

		if(markerCode < 0 || markerCode > 1023)
			throw new IllegalArgumentException("Marker code is not within range [0, 1023]: " + Integer.toString(markerCode));

		// Disable every entity sharing this marker code except for the corresponding door frame.
		for(int i = 0; i < related.size(); i++){
			tempMarker = markerMapper.getSafe(related.get(i));
			tempType   = typeMapper.getSafe(related.get(i));

			// Enable collisions with the corresponding door frame entity. Disable collisions with other related entities.
			if(tempMarker != null) tempMarker.enabled = false;
			if(tempType != null){
				if(tempType.type != BombGameEntityTypeComponent.DOOR_FRAME && tempType.type != BombGameEntityTypeComponent.DOOR){
					manager.remove(related.get(i), CollisionDetectionSystem.COLLIDABLE_OBJECTS_GROUP);
					manager.remove(related.get(i), Integer.toString(markerCode));
				}else if(tempType.type != BombGameEntityTypeComponent.DOOR_FRAME){
					manager.add(related.get(i), CollisionDetectionSystem.COLLIDABLE_OBJECTS_GROUP);
				}
			}
		}
	}

	/**
	 * <p>Checks if a combination bomb is being disabled in the correct sequence.</p>
	 * 
	 * @param buttonType A number between {@link BombGameEntityTypeComponent.COM_BUTTON_1} and {@link BombGameEntityTypeComponent.COM_BUTTON_4}.
	 * @param markerCode A marker code between [0, 1023], inclusive.
	 * @return The current state of the bomb.
	 * @throws IllegalArgumentException If marker code is not in range or if buttonType is not valid.
	 */
	private combination_button_state_t checkCombinationBombButtons(int buttonType, int markerCode) throws IllegalArgumentException{
		combination_button_state_t state;
		boolean                    correctSequence  = true;
		int                        remainingButtons = 0;
		ImmutableBag<Entity>       related;

		if(buttonType < BombGameEntityTypeComponent.COM_BUTTON_1 || buttonType > BombGameEntityTypeComponent.COM_BUTTON_4)
			throw new IllegalArgumentException("Button is not a valid combination bomb button: " + Integer.toString(buttonType));

		if(markerCode < 0 || markerCode > 1023)
			throw new IllegalArgumentException("Marker code is not within range [0, 1023]: " + Integer.toString(markerCode));

		related = manager.getEntities(Integer.toString(markerCode));

		// Check the state of the other buttons associated with this bomb.
		for(int i = 0; i < related.size(); i++){
			tempType = typeMapper.getSafe(related.get(i));

			if(tempType == null) continue;

			if(tempType.type >= BombGameEntityTypeComponent.COM_BUTTON_1 && tempType.type <= BombGameEntityTypeComponent.COM_BUTTON_4){
				if(tempType.type >= buttonType){
					// If this remaining button is a correct one then skip it.
					remainingButtons++;
					continue;
				}else{
					// If this remaining button is an incorrect one then the sequence is wrong.
					correctSequence = false;
					break;
				}
			}else continue;
		}

		if(!correctSequence)
			state = combination_button_state_t.INCORRECT;
		else
			if(remainingButtons == 0)
				state = combination_button_state_t.DISABLED;
			else
				state = combination_button_state_t.CORRECT;

		return state;
	}

	/**
	 * <p>Adds a new fade out entity to the {@link World}.</p>
	 */
	private void createFadeOutEffect(){
		Entity effect = world.createEntity();
		effect.addComponent(new BombGameEntityTypeComponent(BombGameEntityTypeComponent.FADE_EFFECT));
		effect.addComponent(new FadeEffectComponent());
		effect.addToWorld();
	}

	/**
	 * <p>Adds a new fade in entity to the {@link World}.</p>
	 */
	private void createFadeInEffect(){
		Entity effect = world.createEntity();
		effect.addComponent(new BombGameEntityTypeComponent(BombGameEntityTypeComponent.FADE_EFFECT));
		effect.addComponent(new FadeEffectComponent(true));
		effect.addToWorld();
	}

	/**
	 * <p>Updates a fade effect entity.</p>
	 * 
	 * @param f An Artemis {@link Entity} possibly referencing a fade effect.
	 */
	private void processFade(Entity f){
		FadeEffectComponent fade = fadeMapper.getSafe(f);

		if(fade != null){
			if(!fade.isEffectStarted())
				fade.startEffect();

			if(!fade.isEffectFinished()){
				// If the fade has not finished then just update it.
				Gdx.app.log(TAG, CLASS_NAME + ".processFade(): Updating fade.");
				fade.update(Gdx.graphics.getDeltaTime());
			}else{
				// If the fade finished.
				if(fade.isEffectFadeIn()){
					// If the effect was a fade in then just remove it.
					Gdx.app.log(TAG, CLASS_NAME + ".processFade(): deleting fade in.");
					f.deleteFromWorld();
				}else{
					// If the effect was a fade out then wait for one second and then remove it and start a fade in.
					then += (int)(Gdx.graphics.getDeltaTime() * 1000.0f);
					if(then >= 1500){
						Gdx.app.log(TAG, CLASS_NAME + ".processFade(): Deleting fade out.");
						f.deleteFromWorld();
						Gdx.app.log(TAG, CLASS_NAME + ".processFade(): Creating fade in.");
						createFadeInEffect();
						then = 0;
					}else{
						Gdx.app.log(TAG, CLASS_NAME + ".processFade(): Waiting after fade out: " + Integer.toString(then));
					}
				}
			}
		}
	}
}
