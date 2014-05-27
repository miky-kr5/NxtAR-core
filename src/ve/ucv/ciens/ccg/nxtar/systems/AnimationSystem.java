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

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.annotations.Mapper;
import com.artemis.systems.EntityProcessingSystem;
import com.badlogic.gdx.Gdx;

public class AnimationSystem extends EntityProcessingSystem {
	@Mapper ComponentMapper<AnimationComponent> animationMapper;

	@SuppressWarnings("unchecked")
	public AnimationSystem(){
		super(Aspect.getAspectForAll(AnimationComponent.class));
	}

	@Override
	protected void process(Entity e) {
		AnimationComponent animation = animationMapper.get(e);

		if(animation.current != animation.next){
			animation.controller.setAnimation(animation.animationsIds.get(animation.next));
		}

		animation.controller.update(Gdx.graphics.getDeltaTime());
	}

}
