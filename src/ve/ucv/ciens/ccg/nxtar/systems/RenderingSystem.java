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
import ve.ucv.ciens.ccg.nxtar.components.ModelComponent;
import ve.ucv.ciens.ccg.nxtar.components.ShaderComponent;
import ve.ucv.ciens.ccg.nxtar.graphics.LightSource;
import ve.ucv.ciens.ccg.nxtar.graphics.RenderParameters;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.annotations.Mapper;
import com.artemis.systems.EntityProcessingSystem;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;

public class RenderingSystem extends EntityProcessingSystem {
	@Mapper ComponentMapper<GeometryComponent> geometryMapper;
	@Mapper ComponentMapper<ShaderComponent> shaderMapper;
	@Mapper ComponentMapper<ModelComponent> modelMapper;

	private Matrix4 translationMatrix;
	private Matrix4 rotationMatrix;
	private Matrix4 scalingMatrix;
	private Matrix4 combinedTransformationMatrix;

	@SuppressWarnings("unchecked")
	public RenderingSystem() {
		super(Aspect.getAspectForAll(GeometryComponent.class, ShaderComponent.class, ModelComponent.class));

		RenderParameters.setLightSource1(new LightSource(new Vector3(2.0f, 2.0f, 4.0f), new Color(0.0f, 0.1f, 0.2f, 1.0f), new Color(1.0f, 1.0f, 1.0f, 1.0f), new Color(1.0f, 0.8f, 0.0f, 1.0f), 50.0f));

		translationMatrix = new Matrix4().setToTranslation(0.0f, 0.0f, 0.0f);
		rotationMatrix = new Matrix4().setToRotation(1.0f, 0.0f, 0.0f, 0.0f);
		scalingMatrix = new Matrix4().setToScaling(0.0f, 0.0f, 0.0f);
		combinedTransformationMatrix = new Matrix4();
	}

	@Override
	protected void process(Entity e) {
		GeometryComponent geometryComponent;
		ShaderComponent shaderComponent;
		ModelComponent modelComponent;

		// Get the necessary components.
		geometryComponent = geometryMapper.get(e);
		modelComponent = modelMapper.get(e);
		shaderComponent = shaderMapper.get(e);

		// Calculate the geometric transformation for this entity.
		translationMatrix.setToTranslation(geometryComponent.position);
		rotationMatrix.rotate(geometryComponent.rotation);
		scalingMatrix.setToScaling(geometryComponent.scaling);
		combinedTransformationMatrix.idt().mul(scalingMatrix).mul(rotationMatrix).mul(translationMatrix);

		// Set up the global rendering parameters for this frame.
		RenderParameters.setTransformationMatrix(combinedTransformationMatrix);

		// Render this entity.
		shaderComponent.shader.getShaderProgram().begin();{
			shaderComponent.shader.setUniforms();
			modelComponent.model.render(shaderComponent.shader.getShaderProgram(), GL20.GL_TRIANGLES);
		}shaderComponent.shader.getShaderProgram().end();
	}
}
