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

import ve.ucv.ciens.ccg.nxtar.components.GeometryComponent;
import ve.ucv.ciens.ccg.nxtar.components.MarkerCodeComponent;
import ve.ucv.ciens.ccg.nxtar.components.MeshComponent;
import ve.ucv.ciens.ccg.nxtar.components.CustomShaderComponent;
import ve.ucv.ciens.ccg.nxtar.graphics.RenderParameters;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.annotations.Mapper;
import com.artemis.systems.EntityProcessingSystem;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.math.Matrix4;

/**
 * <p>Entity processing system in charge of rendering 3D objects using OpenGL. The
 * entities to be rendered must have a geometry, shader and mesh component associated.</p>
 */
public class ObjectRenderingSystem extends EntityProcessingSystem {
	@Mapper ComponentMapper<GeometryComponent> geometryMapper;
	@Mapper ComponentMapper<CustomShaderComponent> shaderMapper;
	@Mapper ComponentMapper<MeshComponent> modelMapper;

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

	/**
	 * <p>The total transformation to be applied to an entity.</p>
	 */
	private Matrix4 combinedTransformationMatrix;

	@SuppressWarnings("unchecked")
	public ObjectRenderingSystem() {
		super(Aspect.getAspectForAll(GeometryComponent.class, CustomShaderComponent.class, MeshComponent.class).exclude(MarkerCodeComponent.class));

		translationMatrix = new Matrix4().setToTranslation(0.0f, 0.0f, 0.0f);
		rotationMatrix = new Matrix4().idt();
		scalingMatrix = new Matrix4().setToScaling(0.0f, 0.0f, 0.0f);
		combinedTransformationMatrix = new Matrix4();
	}

	/**
	 * <p>Renders the entity passed by parameter, calculating it's corresponding geometric
	 * transformation and setting and calling it's associated shader program.</p>
	 * 
	 * @param e The entity to be processed.
	 */
	@Override
	protected void process(Entity e) {
		GeometryComponent geometryComponent;
		CustomShaderComponent customShaderComponent;
		MeshComponent meshComponent;

		// Get the necessary components.
		geometryComponent = geometryMapper.get(e);
		meshComponent = modelMapper.get(e);
		customShaderComponent = shaderMapper.get(e);

		// Calculate the geometric transformation for this entity.
		translationMatrix.setToTranslation(geometryComponent.position);
		rotationMatrix.set(geometryComponent.rotation);
		scalingMatrix.setToScaling(geometryComponent.scaling);
		combinedTransformationMatrix.idt().mul(translationMatrix).mul(rotationMatrix).mul(scalingMatrix);

		// Set up the global rendering parameters for this frame.
		RenderParameters.setTransformationMatrix(combinedTransformationMatrix);

		// Render this entity.
		customShaderComponent.shader.getShaderProgram().begin();{
			customShaderComponent.shader.setUniforms();
			meshComponent.model.render(customShaderComponent.shader.getShaderProgram(), GL20.GL_TRIANGLES);
		}customShaderComponent.shader.getShaderProgram().end();
	}
}
