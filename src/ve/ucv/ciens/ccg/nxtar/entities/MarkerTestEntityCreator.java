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
package ve.ucv.ciens.ccg.nxtar.entities;

import ve.ucv.ciens.ccg.nxtar.components.GeometryComponent;
import ve.ucv.ciens.ccg.nxtar.components.MarkerCodeComponent;
import ve.ucv.ciens.ccg.nxtar.components.MeshComponent;
import ve.ucv.ciens.ccg.nxtar.components.ShaderComponent;
import ve.ucv.ciens.ccg.nxtar.exceptions.ShaderFailedToLoadException;
import ve.ucv.ciens.ccg.nxtar.graphics.shaders.CustomShaderBase;
import ve.ucv.ciens.ccg.nxtar.graphics.shaders.SingleLightPhongShader;

import com.artemis.Entity;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.utils.MeshBuilder;
import com.badlogic.gdx.math.Matrix3;
import com.badlogic.gdx.math.Vector3;

public class MarkerTestEntityCreator extends EntityCreatorBase {
	private static final String TAG = "MARKER_TEST_ENTITY_CREATOR";
	private static final String CLASS_NAME = MarkerTestEntityCreator.class.getSimpleName();

	private Mesh patchMesh, sphereMesh, boxMesh;
	private CustomShaderBase phongShader;

	@Override
	public void createAllEntities() {
		MeshBuilder builder;
		Matrix3 identity = new Matrix3().idt();
		Entity patch, sphere, box;

		// Create mesh.
		Gdx.app.log(TAG, CLASS_NAME + ".createAllEntities(): Creating the meshes.");
		builder = new MeshBuilder();
		builder.begin(new VertexAttributes(new VertexAttribute(Usage.Position, 3, "a_position"), new VertexAttribute(Usage.Normal, 3, "a_normal"), new VertexAttribute(Usage.Color, 4, "a_color")), GL20.GL_TRIANGLES);{
			builder.setColor(1.0f, 1.0f, 1.0f, 1.0f);
			Vector3 v00 = new Vector3(-0.5f, -0.5f, 0.0f);
			Vector3 v10 = new Vector3(-0.5f,  0.5f, 0.0f);
			Vector3 v11 = new Vector3( 0.5f,  0.5f, 0.0f);
			Vector3 v01 = new Vector3( 0.5f, -0.5f, 0.0f);
			Vector3 n = new Vector3(0.0f, 1.0f, 0.0f);
			builder.patch(v00, v10, v11, v01, n, 10, 10);
		}patchMesh = builder.end();

		builder.begin(new VertexAttributes(new VertexAttribute(Usage.Position, 3, "a_position"), new VertexAttribute(Usage.Normal, 3, "a_normal"), new VertexAttribute(Usage.Color, 4, "a_color")), GL20.GL_TRIANGLES);{
			builder.setColor(1.0f, 1.0f, 1.0f, 1.0f);
			builder.sphere(1.0f, 1.0f, 1.0f, 10, 10);
		}sphereMesh = builder.end();

		builder.begin(new VertexAttributes(new VertexAttribute(Usage.Position, 3, "a_position"), new VertexAttribute(Usage.Normal, 3, "a_normal"), new VertexAttribute(Usage.Color, 4, "a_color")), GL20.GL_TRIANGLES);{
			builder.setColor(1.0f, 1.0f, 1.0f, 1.0f);
			builder.box(0.5f, 0.5f, 6.0f);
		}boxMesh = builder.end();

		// Load the phong shader.
		Gdx.app.log(TAG, CLASS_NAME + ".createAllEntities(): Loading the phong shader.");
		try{
			phongShader = new SingleLightPhongShader().loadShader();
		}catch(ShaderFailedToLoadException se){
			Gdx.app.error(TAG, CLASS_NAME + ".InGameState(): " + se.getMessage());
			Gdx.app.exit();
		}

		// Create the entities.
		Gdx.app.log(TAG, CLASS_NAME + ".createAllEntities(): Creating the enitites.");
		patch = world.createEntity();
		patch.addComponent(new GeometryComponent(new Vector3(0.0f, 0.0f, 0.0f), identity, new Vector3(1.0f, 1.0f, 1.0f)));
		patch.addComponent(new MeshComponent(patchMesh));
		patch.addComponent(new ShaderComponent(phongShader));
		patch.addComponent(new MarkerCodeComponent(213));

		sphere = world.createEntity();
		sphere.addComponent(new GeometryComponent(new Vector3(0.0f, 0.0f, 0.0f), identity, new Vector3(1.0f, 1.0f, 1.0f)));
		sphere.addComponent(new MeshComponent(sphereMesh));
		sphere.addComponent(new ShaderComponent(phongShader));
		sphere.addComponent(new MarkerCodeComponent(10));

		box = world.createEntity();
		box.addComponent(new GeometryComponent(new Vector3(-1.0f, 0.0f, 0.0f), identity, new Vector3(1.0f, 1.0f, 1.0f)));
		box.addComponent(new MeshComponent(boxMesh));
		box.addComponent(new ShaderComponent(phongShader));

		// Add the entities to the world.
		Gdx.app.log(TAG, CLASS_NAME + ".createAllEntities(): Adding entities to the world.");
		sphere.addToWorld();
		patch.addToWorld();
		box.addToWorld();
	}

	@Override
	public void dispose() {
		if(phongShader != null && phongShader.getShaderProgram() != null)
			phongShader.getShaderProgram().dispose();

		if(patchMesh != null)
			patchMesh.dispose();

		if(sphereMesh != null)
			sphereMesh.dispose();

		if(boxMesh != null)
			boxMesh.dispose();
	}
}
