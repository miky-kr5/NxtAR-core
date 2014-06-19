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
import ve.ucv.ciens.ccg.nxtar.components.VisibilityComponent;
import ve.ucv.ciens.ccg.nxtar.interfaces.ImageProcessor.MarkerData;
import ve.ucv.ciens.ccg.nxtar.utils.ProjectConstants;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.annotations.Mapper;
import com.artemis.systems.EntityProcessingSystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Matrix3;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;

public class MarkerPositioningSystem extends EntityProcessingSystem {
	@Mapper ComponentMapper<MarkerCodeComponent> markerMapper;
	@Mapper ComponentMapper<GeometryComponent>   geometryMapper;
	@Mapper ComponentMapper<VisibilityComponent> visibilityMapper;

	private MarkerData markers;
	private Quaternion qAux;
	private Matrix4    correctedRotation;
	private Matrix3    mAux;

	@SuppressWarnings("unchecked")
	public MarkerPositioningSystem(){
		super(Aspect.getAspectForAll(MarkerCodeComponent.class, GeometryComponent.class, VisibilityComponent.class));

		markers = null;
		qAux = new Quaternion();
		mAux = new Matrix3();
		correctedRotation = new Matrix4();
	}

	public void setMarkerData(MarkerData markers){
		this.markers = markers;
	}

	@Override
	protected void process(Entity e) {
		MarkerCodeComponent marker;
		GeometryComponent   geometry;
		VisibilityComponent visibility;

		if(markers == null)
			return;

		marker     = markerMapper.get(e);
		geometry   = geometryMapper.get(e);
		visibility = visibilityMapper.get(e);

		for(int i = 0; i < ProjectConstants.MAXIMUM_NUMBER_OF_MARKERS; i++){
			if(markers.markerCodes[i] != 1){
				if(markers.markerCodes[i] == marker.code){

					qAux.setFromMatrix(markers.rotationMatrices[i]).nor();

					if(Math.abs(qAux.getRoll()) > 10.0f){
//						qAux.setEulerAngles(qAux.getYaw(), qAux.getPitch(), 0.0f);
//						qAux.toMatrix(correctedRotation.val);
//						mAux.set(correctedRotation);
						mAux.set(markers.rotationMatrices[i]);

						Gdx.app.log("ROTATION", "YAW  : " + Float.toString(qAux.getYaw()));
						Gdx.app.log("ROTATION", "PITCH: " + Float.toString(qAux.getPitch()));
						Gdx.app.log("ROTATION", "ROLL : " + Float.toString(qAux.getRoll()));
						Gdx.app.log("ROTATION", "------------------------------------------");
					}else{
						mAux.set(markers.rotationMatrices[i]);
					}

					geometry.position.set(markers.translationVectors[i]);
					geometry.rotation.set(mAux);
					visibility.visible = true;
					break;
				}else{
					visibility.visible = false;
				}
			}
		}
	}
}
