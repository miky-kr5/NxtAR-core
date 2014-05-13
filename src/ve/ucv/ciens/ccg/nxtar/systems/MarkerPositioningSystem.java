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
import ve.ucv.ciens.ccg.nxtar.interfaces.ImageProcessor.MarkerData;
import ve.ucv.ciens.ccg.nxtar.utils.ProjectConstants;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.annotations.Mapper;
import com.artemis.systems.EntityProcessingSystem;
import com.badlogic.gdx.Gdx;

public class MarkerPositioningSystem extends EntityProcessingSystem {
	@Mapper ComponentMapper<MarkerCodeComponent> markerMapper;
	@Mapper ComponentMapper<GeometryComponent> geometryMapper;

	private static final String TAG = "MARKER_POSITIONING_SYSTEM";
	private static final String CLASS_NAME = MarkerPositioningSystem.class.getSimpleName();

	private MarkerData markers;

	@SuppressWarnings("unchecked")
	public MarkerPositioningSystem(){
		super(Aspect.getAspectForAll(MarkerCodeComponent.class, GeometryComponent.class));

		markers = null;
	}

	public void setMarkerData(MarkerData markers){
		this.markers = markers;
	}

	@Override
	protected void process(Entity e) {
		MarkerCodeComponent marker;
		GeometryComponent   geometry;

		if(markers == null)
			return;

		Gdx.app.log(TAG, CLASS_NAME + ".process(): Getting components.");
		marker   = markerMapper.get(e);
		geometry = geometryMapper.get(e);

		Gdx.app.log(TAG, CLASS_NAME + ".process(): Processing markers.");
		for(int i = 0; i < ProjectConstants.MAXIMUM_NUMBER_OF_MARKERS; i++){
			if(markers.markerCodes[i] != 1){
				if(markers.markerCodes[i] == marker.code){
					Gdx.app.log(TAG, CLASS_NAME + ".process(): Processing marker code " + Integer.toString(markers.markerCodes[i]) + ".");
					geometry.position.set(markers.translationVectors[i]);
					geometry.rotation.set(markers.rotationMatrices[i]);
				}
			}else{
				Gdx.app.log(TAG, CLASS_NAME + ".process(): Skipping marker number " + Integer.toString(i) + ".");
			}
		}

		markers = null;
	}
}
