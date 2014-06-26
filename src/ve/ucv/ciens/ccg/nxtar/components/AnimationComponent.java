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
package ve.ucv.ciens.ccg.nxtar.components;

import java.util.LinkedList;
import java.util.List;

import com.artemis.Component;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.utils.AnimationController;

public class AnimationComponent extends Component {
	public AnimationController controller;
	public AnimationController collisionController;
	public List<String> animationsIds;
	public int current;
	public int next;
	public boolean loop;

	public AnimationComponent(ModelInstance instance, ModelInstance collisionInstance){
		this(instance, -1, false, collisionInstance);
	}

	public AnimationComponent(ModelInstance instance) throws IllegalArgumentException{
		this(instance, -1, false);
	}

	public AnimationComponent(ModelInstance instance, int next) throws IllegalArgumentException{
		this(instance, next, false);
	}

	public AnimationComponent(ModelInstance instance, int next, boolean loop) throws IllegalArgumentException{
		if(instance == null)
			throw new IllegalArgumentException("Instance is null.");
		else if(next > instance.animations.size)
			throw new IllegalArgumentException("Next is greater than the number of animations for this model.");

		controller          = new AnimationController(instance);
		collisionController = null;
		animationsIds       = new LinkedList<String>();
		current             = -1;
		this.next           = next;
		this.loop           = loop;

		for(int i = 0; i < instance.animations.size; i++){
			animationsIds.add(instance.animations.get(i).id);
		}
	}

	public AnimationComponent(ModelInstance instance, int next, boolean loop, ModelInstance collisionInstance) throws IllegalArgumentException{
		this(instance, next, loop);

		if(instance.animations.size != collisionInstance.animations.size)
			throw new IllegalArgumentException("Animation number doesn't match between render model and collision model.");

		for(int i = 0; i < instance.animations.size; i++){
			if(!instance.animations.get(i).id.contentEquals(collisionInstance.animations.get(i).id))
				throw new IllegalArgumentException("Animations don't match between render model and collision model.");
		}

		collisionController = new AnimationController(collisionInstance);
	}
}
