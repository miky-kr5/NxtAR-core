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

import ve.ucv.ciens.ccg.nxtar.components.EnvironmentComponent;
import ve.ucv.ciens.ccg.nxtar.components.RenderModelComponent;
import ve.ucv.ciens.ccg.nxtar.components.ShaderComponent;
import ve.ucv.ciens.ccg.nxtar.components.VisibilityComponent;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.annotations.Mapper;
import com.artemis.systems.EntityProcessingSystem;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g3d.ModelBatch;

public class MarkerRenderingSystem extends EntityProcessingSystem {
	//	@Mapper ComponentMapper<MarkerCodeComponent>   markerMapper;
	@Mapper ComponentMapper<RenderModelComponent>  modelMapper;
	@Mapper ComponentMapper<EnvironmentComponent>  environmentMapper;
	@Mapper ComponentMapper<ShaderComponent>       shaderMapper;
	@Mapper ComponentMapper<VisibilityComponent>   visibiltyMapper;

	private PerspectiveCamera camera;
	private ModelBatch batch;

	@SuppressWarnings("unchecked")
	public MarkerRenderingSystem(ModelBatch batch){
		super(Aspect.getAspectForAll(ShaderComponent.class, EnvironmentComponent.class, RenderModelComponent.class, VisibilityComponent.class));

		camera     = null;
		this.batch = batch;
	}

	public void begin(PerspectiveCamera camera) throws RuntimeException{
		if(this.camera != null)
			throw new RuntimeException("Begin called twice without calling end.");

		this.camera = camera;
		batch.begin(camera);
	}

	public void end(){
		batch.end();
		camera = null;
	}

	@Override
	protected void process(Entity e) {
		EnvironmentComponent  environment;
		RenderModelComponent  model;
		ShaderComponent       shader;
		VisibilityComponent   visibility;

		if(camera == null)
			return;

		model       = modelMapper.get(e);
		environment = environmentMapper.get(e);
		shader      = shaderMapper.get(e);
		visibility  = visibiltyMapper.get(e);

		if(visibility.visible){
			// Render the marker;
			batch.render(model.instance, environment.environment, shader.shader);
		}
	}
}
