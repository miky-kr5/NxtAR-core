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
import ve.ucv.ciens.ccg.nxtar.components.VisibilityComponent;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.annotations.Mapper;
import com.artemis.systems.EntityProcessingSystem;
import com.badlogic.gdx.Gdx;

public class AnimationSystem extends EntityProcessingSystem {
	public static final int NO_ANIMATION = -1;

	@Mapper ComponentMapper<AnimationComponent> animationMapper;
	@Mapper ComponentMapper<VisibilityComponent> visibilityMapper;

	@SuppressWarnings("unchecked")
	public AnimationSystem(){
		super(Aspect.getAspectForAll(AnimationComponent.class, VisibilityComponent.class));
	}

	@Override
	protected void process(Entity e) {
		AnimationComponent  animation  = animationMapper.get(e);
		VisibilityComponent visibility = visibilityMapper.get(e);
		int                 loopCount  = animation.loop ? -1 : 1;

		if(animation.current != animation.next && animation.next >= 0 && animation.next < animation.animationsIds.size()){
			animation.current = animation.next;

			if(animation.controller.current == null){
				animation.controller.setAnimation(animation.animationsIds.get(animation.next), loopCount, 1, null);
			}else{
				animation.controller.animate(animation.animationsIds.get(animation.next), loopCount, 1, null, 0.1f);
			}

			if(animation.collisionController != null){
				if(animation.collisionController.current == null){
					animation.collisionController.setAnimation(animation.animationsIds.get(animation.next), loopCount, 1, null);
				}else{
					animation.collisionController.animate(animation.animationsIds.get(animation.next), loopCount, 1, null, 0.1f);
				}
			}
		}

		if(visibility.visible){
			animation.controller.update(Gdx.graphics.getDeltaTime());
			if(animation.collisionController != null)
				animation.collisionController.update(Gdx.graphics.getDeltaTime());
		}
	}
}
