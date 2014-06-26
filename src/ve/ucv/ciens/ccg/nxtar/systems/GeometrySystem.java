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

import ve.ucv.ciens.ccg.nxtar.components.CollisionModelComponent;
import ve.ucv.ciens.ccg.nxtar.components.GeometryComponent;
import ve.ucv.ciens.ccg.nxtar.components.RenderModelComponent;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.annotations.Mapper;
import com.artemis.systems.EntityProcessingSystem;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Matrix4;

public class GeometrySystem extends EntityProcessingSystem {
	@Mapper ComponentMapper<GeometryComponent>       geometryMapper;
	@Mapper ComponentMapper<RenderModelComponent>    renderModelMapper;
	@Mapper ComponentMapper<CollisionModelComponent> colModelMapper;

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

	@SuppressWarnings("unchecked")
	public GeometrySystem(){
		super(Aspect.getAspectForAll(GeometryComponent.class).one(RenderModelComponent.class, CollisionModelComponent.class));

		translationMatrix = new Matrix4().setToTranslation(0.0f, 0.0f, 0.0f);
		rotationMatrix    = new Matrix4().idt();
		scalingMatrix     = new Matrix4().setToScaling(0.0f, 0.0f, 0.0f);
	}

	@Override
	protected void process(Entity e) {
		GeometryComponent       geometry;
		RenderModelComponent    renderModel;
		CollisionModelComponent colModel;

		geometry    = geometryMapper.get(e);
		renderModel = renderModelMapper.getSafe(e);
		colModel    = colModelMapper.getSafe(e);

		if(renderModel != null)
			applyWorldTransform(renderModel.instance, geometry);
		if(colModel != null)
			applyWorldTransform(colModel.instance, geometry);
	}

	private void applyWorldTransform(ModelInstance model, GeometryComponent geometry){
		translationMatrix.setToTranslation(geometry.position);

		rotationMatrix.val[Matrix4.M00] = geometry.rotation.val[0];
		rotationMatrix.val[Matrix4.M10] = geometry.rotation.val[1];
		rotationMatrix.val[Matrix4.M20] = geometry.rotation.val[2];
		rotationMatrix.val[Matrix4.M30] = 0;

		rotationMatrix.val[Matrix4.M01] = geometry.rotation.val[3];
		rotationMatrix.val[Matrix4.M11] = geometry.rotation.val[4];
		rotationMatrix.val[Matrix4.M21] = geometry.rotation.val[5];
		rotationMatrix.val[Matrix4.M31] = 0;

		rotationMatrix.val[Matrix4.M02] = geometry.rotation.val[6];
		rotationMatrix.val[Matrix4.M12] = geometry.rotation.val[7];
		rotationMatrix.val[Matrix4.M22] = geometry.rotation.val[8];
		rotationMatrix.val[Matrix4.M32] = 0;

		rotationMatrix.val[Matrix4.M03] = 0;
		rotationMatrix.val[Matrix4.M13] = 0;
		rotationMatrix.val[Matrix4.M23] = 0;
		rotationMatrix.val[Matrix4.M33] = 1;

		scalingMatrix.setToScaling(geometry.scaling);

		model.transform.idt().mul(translationMatrix).mul(rotationMatrix).mul(scalingMatrix);
		model.calculateTransforms();
	}
}
