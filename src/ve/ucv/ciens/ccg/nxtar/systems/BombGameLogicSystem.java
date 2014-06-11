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

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.annotations.Mapper;
import com.badlogic.gdx.Gdx;

public class BombGameLogicSystem extends GameLogicSystemBase {
	private static final String TAG        = "BOMB_GAME_LOGIC";
	private static final String CLASS_NAME = BombGameLogicSystem.class.getSimpleName();

	@Mapper ComponentMapper<BombGameObjectTypeComponent> typeMapper;
	@Mapper ComponentMapper<AnimationComponent>          animationMapper;
	@Mapper ComponentMapper<VisibilityComponent>         visibilityMapper;
	@Mapper ComponentMapper<MarkerCodeComponent>         markerMapper;
	@Mapper ComponentMapper<CollisionDetectionComponent> collisionMapper;

	@SuppressWarnings("unchecked")
	public BombGameLogicSystem(){
		super(Aspect.getAspectForAll(BombGameObjectTypeComponent.class));
	}

	@Override
	protected void process(Entity e){
		BombGameObjectTypeComponent typeComponent;

		typeComponent = typeMapper.get(e);

		switch(typeComponent.type){
		case BombGameObjectTypeComponent.DOOR:
			processDoor(e);
			break;
		default: break;
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

		if(marker.enabled && visibility.visible && collision.colliding && animation.current != 1){
			animation.next = 1;
			animation.loop = false;
			Gdx.app.log(TAG, CLASS_NAME + ".processDoor(): Animating door.");
		}

		return;
	}
}
