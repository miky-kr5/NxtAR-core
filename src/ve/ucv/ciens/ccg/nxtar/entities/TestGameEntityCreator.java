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

import ve.ucv.ciens.ccg.nxtar.components.ModelComponent;
import ve.ucv.ciens.ccg.nxtar.components.GeometryComponent;
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
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;

public class TestGameEntityCreator extends EntityCreatorBase {
	private static final String TAG = "TEST_ENTITY_CREATOR";
	private static final String CLASS_NAME = TestGameEntityCreator.class.getSimpleName();

	private MeshBuilder builder;
	private Mesh sphereMesh;
	private Mesh cubeMesh;
	private Mesh capsuleMesh;
	private CustomShaderBase singleLightPhongShader;

	@Override
	public void createAllEntities() {
		Entity sphere;
		Entity cube;
		Entity capsule1;
		Entity capsule2;

		Gdx.app.log(TAG, CLASS_NAME + ".createAllEntities(): Started.");

		// Create the sphere.
		Gdx.app.log(TAG, CLASS_NAME + ".createAllEntities(): Creating the meshes.");
		builder = new MeshBuilder();
		builder.begin(new VertexAttributes(new VertexAttribute(Usage.Position, 3, "a_position"), new VertexAttribute(Usage.Normal, 3, "a_normal"), new VertexAttribute(Usage.Color, 4, "a_color")), GL20.GL_TRIANGLES);{
			builder.setColor(1.0f, 1.0f, 1.0f, 1.0f);
			builder.sphere(1.0f, 1.0f, 1.0f, 10, 10);
		}sphereMesh = builder.end();

		// Create the cube.
		builder.begin(new VertexAttributes(new VertexAttribute(Usage.Position, 3, "a_position"), new VertexAttribute(Usage.Normal, 3, "a_normal"), new VertexAttribute(Usage.Color, 4, "a_color")), GL20.GL_TRIANGLES);{
			builder.setColor(0.2f, 0.5f, 1.0f, 1.0f);
			builder.box(0.5f, 0.5f, 0.5f);
		}cubeMesh = builder.end();

		// Create the capsule.
		builder.begin(new VertexAttributes(new VertexAttribute(Usage.Position, 3, "a_position"), new VertexAttribute(Usage.Normal, 3, "a_normal"), new VertexAttribute(Usage.Color, 4, "a_color")), GL20.GL_TRIANGLES);{
			builder.setColor(1.0f, 1.0f, 1.0f, 1.0f);
			builder.capsule(0.25f, 0.5f, 10);
		}capsuleMesh = builder.end();

		// Load the phong shader.
		Gdx.app.log(TAG, CLASS_NAME + ".createAllEntities(): Loading the phong shader.");
		try{
			singleLightPhongShader = new SingleLightPhongShader().loadShader();
		}catch(ShaderFailedToLoadException se){
			Gdx.app.error(TAG, CLASS_NAME + ".InGameState(): " + se.getMessage());
			Gdx.app.exit();
		}

		// Create the entities.
		Gdx.app.log(TAG, CLASS_NAME + ".createAllEntities(): Creating the enitites.");
		sphere = world.createEntity();
		sphere.addComponent(new GeometryComponent(new Vector3(0.5f, 0.5f, 0.0f)));
		sphere.addComponent(new ModelComponent(sphereMesh));
		sphere.addComponent(new ShaderComponent(singleLightPhongShader));

		cube = world.createEntity();
		cube.addComponent(new GeometryComponent(new Vector3(-0.5f, -0.5f, 0.0f), new Quaternion(new Vector3(1.0f, 1.0f, 0.0f), 0.0f), new Vector3(1.0f, 1.0f, 1.0f)));
		cube.addComponent(new ModelComponent(cubeMesh));
		cube.addComponent(new ShaderComponent(singleLightPhongShader));

		capsule1 = world.createEntity();
		capsule1.addComponent(new GeometryComponent(new Vector3(-0.5f, 0.5f, 0.0f), new Quaternion(new Vector3(1.0f, 0.0f, 0.0f), 0.0f), new Vector3(1.5f, 1.0f, 1.0f)));
		capsule1.addComponent(new ModelComponent(capsuleMesh));
		capsule1.addComponent(new ShaderComponent(singleLightPhongShader));

		capsule2 = world.createEntity();
		capsule2.addComponent(new GeometryComponent(new Vector3(0.5f, -0.5f, 0.0f), new Quaternion(new Vector3(0.0f, 1.0f, 0.0f), 0.0f), new Vector3(1.0f, 1.5f, 1.0f)));
		capsule2.addComponent(new ModelComponent(capsuleMesh));
		capsule2.addComponent(new ShaderComponent(singleLightPhongShader));

		// Add the entities to the world.
		Gdx.app.log(TAG, CLASS_NAME + ".createAllEntities(): Adding entities to the world.");
		sphere.addToWorld();
		cube.addToWorld();
		capsule1.addToWorld();
		capsule2.addToWorld();

		Gdx.app.log(TAG, CLASS_NAME + ".createAllEntities(): Finished.");
	}

	@Override
	public void dispose() {
		if(singleLightPhongShader != null && singleLightPhongShader.getShaderProgram() != null)
			singleLightPhongShader.getShaderProgram().dispose();

		if(sphereMesh != null)
			sphereMesh.dispose();

		if(cubeMesh != null)
			cubeMesh.dispose();

		if(capsuleMesh != null)
			capsuleMesh.dispose();
	}

}
