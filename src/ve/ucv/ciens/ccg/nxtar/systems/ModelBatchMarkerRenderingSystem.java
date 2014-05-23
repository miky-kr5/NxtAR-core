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
import ve.ucv.ciens.ccg.nxtar.interfaces.ImageProcessor.MarkerData;
import ve.ucv.ciens.ccg.nxtar.utils.ProjectConstants;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.annotations.Mapper;
import com.artemis.systems.EntityProcessingSystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.math.Matrix4;

public class ModelBatchMarkerRenderingSystem extends EntityProcessingSystem {
	@Mapper ComponentMapper<MarkerCodeComponent>   markerMapper;
	@Mapper ComponentMapper<GeometryComponent>     geometryMapper;
	@Mapper ComponentMapper<ModelComponent>        modelMapper;
	@Mapper ComponentMapper<EnvironmentComponent>  environmentMapper;
	@Mapper ComponentMapper<ShaderComponent>       shaderMapper;

	private static final String TAG = "MODEL_BATCH_MARKER_RENDERING_SYSTEM";
	private static final String CLASS_NAME = ModelBatchMarkerRenderingSystem.class.getSimpleName();

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

	private MarkerData markers;

	private PerspectiveCamera camera;

	private ModelBatch batch;

	@SuppressWarnings("unchecked")
	public ModelBatchMarkerRenderingSystem(){
		super(Aspect.getAspectForAll(MarkerCodeComponent.class, GeometryComponent.class, ShaderComponent.class, EnvironmentComponent.class, ModelComponent.class));

		markers           = null;
		camera            = null;
		batch             = new ModelBatch();
		translationMatrix = new Matrix4().setToTranslation(0.0f, 0.0f, 0.0f);
		rotationMatrix    = new Matrix4().idt();
		scalingMatrix     = new Matrix4().setToScaling(0.0f, 0.0f, 0.0f);
	}

	public void dispose(){
		batch.dispose();
	}

	public void begin(PerspectiveCamera camera, MarkerData markers) throws RuntimeException{
		if(this.camera != null)
			throw new RuntimeException("Begin called twice without calling end.");

		if(this.markers != null)
			throw new RuntimeException("Begin called twice without calling end.");

		this.markers = markers;
		this.camera = camera;
		batch.begin(camera);
	}

	public void end(){
		batch.end();
		camera = null;
		markers = null;
	}

	@Override
	protected void process(Entity e) {
		MarkerCodeComponent   marker;
		GeometryComponent     geometry;
		EnvironmentComponent  environment;
		ModelComponent        model;
		ShaderComponent       shader;

		if(markers == null || camera == null)
			return;

		Gdx.app.log(TAG, CLASS_NAME + ".process(): Getting components.");
		marker      = markerMapper.get(e);
		geometry    = geometryMapper.get(e);
		model       = modelMapper.get(e);
		environment = environmentMapper.get(e);
		shader      = shaderMapper.get(e);

		Gdx.app.log(TAG, CLASS_NAME + ".process(): Processing markers.");
		for(int i = 0; i < ProjectConstants.MAXIMUM_NUMBER_OF_MARKERS; i++){
			if(markers.markerCodes[i] != 1){
				if(markers.markerCodes[i] == marker.code){
					Gdx.app.log(TAG, CLASS_NAME + ".process(): Rendering marker code " + Integer.toString(markers.markerCodes[i]) + ".");
					// Set the geometric transformations.
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

					// Apply the geometric transformations to the model.
					model.instance.transform.idt().mul(translationMatrix).mul(rotationMatrix).mul(scalingMatrix);
					model.instance.calculateTransforms();

					// Render the marker;
					batch.render(model.instance, environment.environment, shader.shader);
				}
			}else{
				Gdx.app.log(TAG, CLASS_NAME + ".process(): Skipping marker number " + Integer.toString(i) + ".");
			}
		}
	}

}
