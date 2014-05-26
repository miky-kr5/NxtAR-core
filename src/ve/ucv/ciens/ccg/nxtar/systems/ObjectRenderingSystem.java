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
import ve.ucv.ciens.ccg.nxtar.components.GeometryComponent;
import ve.ucv.ciens.ccg.nxtar.components.MarkerCodeComponent;
import ve.ucv.ciens.ccg.nxtar.components.ModelComponent;
import ve.ucv.ciens.ccg.nxtar.components.ShaderComponent;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.annotations.Mapper;
import com.artemis.systems.EntityProcessingSystem;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.math.Matrix4;

/**
 * <p>Entity processing system in charge of rendering 3D objects using OpenGL. The
 * entities to be rendered must have a geometry, shader and mesh component associated.</p>
 */
public class ObjectRenderingSystem extends EntityProcessingSystem {
	@Mapper ComponentMapper<GeometryComponent>     geometryMapper;
	@Mapper ComponentMapper<ShaderComponent>       shaderMapper;
	@Mapper ComponentMapper<ModelComponent>        modelMapper;
	@Mapper ComponentMapper<EnvironmentComponent>  environmentMapper;

	/**
	 * <p>A matrix representing 3D translations.</p>
	 */
	private Matrix4 translationMatrix;

	/**
	 * <p>A matrix representing 3D rotations.</p>
	 */
	private Matrix4 rotationMatrix;

	/**
	 * <p>A matrix representing 3D scalings.</p>
	 */
	private Matrix4 scalingMatrix;

	private PerspectiveCamera camera;

	private ModelBatch batch;

	@SuppressWarnings("unchecked")
	public ObjectRenderingSystem(ModelBatch batch) {
		super(Aspect.getAspectForAll(GeometryComponent.class, ShaderComponent.class, ModelComponent.class, EnvironmentComponent.class).exclude(MarkerCodeComponent.class));

		camera            = null;
		this.batch        = batch;
		translationMatrix = new Matrix4().setToTranslation(0.0f, 0.0f, 0.0f);
		rotationMatrix    = new Matrix4().idt();
		scalingMatrix     = new Matrix4().setToScaling(0.0f, 0.0f, 0.0f);
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

	/**
	 * <p>Renders the entity passed by parameter, calculating it's corresponding geometric
	 * transformation and setting and calling it's associated shader program.</p>
	 * 
	 * @param e The entity to be processed.
	 */
	@Override
	protected void process(Entity e) {
		EnvironmentComponent  environment;
		GeometryComponent     geometryComponent;
		ShaderComponent       shaderComponent;
		ModelComponent        modelComponent;

		// Get the necessary components.
		geometryComponent = geometryMapper.get(e);
		modelComponent    = modelMapper.get(e);
		shaderComponent   = shaderMapper.get(e);
		environment       = environmentMapper.get(e);

		// Calculate the geometric transformation for this entity.
		translationMatrix.setToTranslation(geometryComponent.position);
		rotationMatrix.set(geometryComponent.rotation);
		scalingMatrix.setToScaling(geometryComponent.scaling);
		modelComponent.instance.transform.idt().mul(translationMatrix).mul(rotationMatrix).mul(scalingMatrix);
		modelComponent.instance.calculateTransforms();

		// Render this entity.
		batch.render(modelComponent.instance, environment.environment, shaderComponent.shader);
	}
}
