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

import ve.ucv.ciens.ccg.nxtar.components.EnvironmentComponent;
import ve.ucv.ciens.ccg.nxtar.components.ModelComponent;
import ve.ucv.ciens.ccg.nxtar.components.ShaderComponent;
import ve.ucv.ciens.ccg.nxtar.components.GeometryComponent;
import ve.ucv.ciens.ccg.nxtar.components.MarkerCodeComponent;
import ve.ucv.ciens.ccg.nxtar.components.MeshComponent;
import ve.ucv.ciens.ccg.nxtar.exceptions.ShaderFailedToLoadException;
import ve.ucv.ciens.ccg.nxtar.graphics.shaders.CustomShaderBase;
import ve.ucv.ciens.ccg.nxtar.graphics.shaders.SingleLightPhongShader;

import com.artemis.Entity;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.DepthTestAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.loader.G3dModelLoader;
import com.badlogic.gdx.graphics.g3d.utils.MeshBuilder;
import com.badlogic.gdx.math.Matrix3;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.JsonReader;

public class MarkerTestEntityCreator extends EntityCreatorBase {
	private static final String TAG = "MARKER_TEST_ENTITY_CREATOR";
	private static final String CLASS_NAME = MarkerTestEntityCreator.class.getSimpleName();

	private Mesh sphereMesh;
	private Mesh boxMesh;
	private Model bombModel;
	private CustomShaderBase phongShader;
	private Mesh bombMesh;

	@Override
	public void createAllEntities() {
		MeshBuilder builder;
		Entity bomb, sphere, box, bombModelBatch;
		G3dModelLoader loader;
		Environment environment;

		// Create mesh.
		Gdx.app.log(TAG, CLASS_NAME + ".createAllEntities(): Creating the meshes.");
		builder = new MeshBuilder();
		/*builder.begin(new VertexAttributes(new VertexAttribute(Usage.Position, 3, "a_position"), new VertexAttribute(Usage.Normal, 3, "a_normal"), new VertexAttribute(Usage.Color, 4, "a_color")), GL20.GL_TRIANGLES);{
			builder.setColor(1.0f, 1.0f, 1.0f, 1.0f);
			Vector3 v00 = new Vector3(-0.5f, -0.5f, 0.0f);
			Vector3 v10 = new Vector3(-0.5f,  0.5f, 0.0f);
			Vector3 v11 = new Vector3( 0.5f,  0.5f, 0.0f);
			Vector3 v01 = new Vector3( 0.5f, -0.5f, 0.0f);
			Vector3 n = new Vector3(0.0f, 1.0f, 0.0f);
			builder.patch(v00, v10, v11, v01, n, 10, 10);
		}patchMesh = builder.end();*/

		builder.begin(new VertexAttributes(new VertexAttribute(Usage.Position, 3, "a_position"), new VertexAttribute(Usage.Normal, 3, "a_normal"), new VertexAttribute(Usage.Color, 4, "a_color")), GL20.GL_TRIANGLES);{
			builder.setColor(1.0f, 1.0f, 1.0f, 1.0f);
			builder.sphere(1.0f, 1.0f, 1.0f, 10, 10);
		}sphereMesh = builder.end();

		builder.begin(new VertexAttributes(new VertexAttribute(Usage.Position, 3, "a_position"), new VertexAttribute(Usage.Normal, 3, "a_normal"), new VertexAttribute(Usage.Color, 4, "a_color")), GL20.GL_TRIANGLES);{
			builder.setColor(1.0f, 1.0f, 1.0f, 1.0f);
			builder.box(0.5f, 0.5f, 6.0f);
		}boxMesh = builder.end();

		loader = new G3dModelLoader(new JsonReader());
		bombModel = loader.loadModel(Gdx.files.internal("models/Bomb_test_2.g3dj"));

		bombMesh = bombModel.meshes.get(0);
		Gdx.app.log(TAG, CLASS_NAME + ".createAllEntities(): " + bombMesh.getVertexAttributes().toString());

		// Load the phong shader.
		Gdx.app.log(TAG, CLASS_NAME + ".createAllEntities(): Loading the phong shader.");
		try{
			phongShader = new SingleLightPhongShader().loadShader();
		}catch(ShaderFailedToLoadException se){
			Gdx.app.error(TAG, CLASS_NAME + ".InGameState(): " + se.getMessage());
			Gdx.app.exit();
		}

		environment = new Environment();
		environment.set(new DepthTestAttribute(GL20.GL_LEQUAL, true));
		environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.3f, 0.3f, 0.3f, 1.0f));
		environment.add(new DirectionalLight().set(new Color(1, 1, 1, 1), new Vector3(-2, -2, -2)));

		// Create the entities.
		Gdx.app.log(TAG, CLASS_NAME + ".createAllEntities(): Creating the enitites.");
		bomb = world.createEntity();
		bomb.addComponent(new GeometryComponent(new Vector3(0.0f, 0.0f, 0.0f), new Matrix3().idt(), new Vector3(1.0f, 1.0f, 1.0f)));
		bomb.addComponent(new MeshComponent(bombMesh));
		bomb.addComponent(new ShaderComponent(phongShader));
		bomb.addComponent(new MarkerCodeComponent(1023));

		bombModelBatch = world.createEntity();
		bombModelBatch.addComponent(new GeometryComponent(new Vector3(0.0f, 0.0f, 0.0f), new Matrix3().idt(), new Vector3(1.0f, 1.0f, 1.0f)));
		bombModelBatch.addComponent(new ModelComponent(bombModel));
		bombModelBatch.addComponent(new EnvironmentComponent(environment));
		bombModelBatch.addComponent(new MarkerCodeComponent(89));

		sphere = world.createEntity();
		sphere.addComponent(new GeometryComponent(new Vector3(0.0f, 0.0f, 0.0f), new Matrix3().idt(), new Vector3(1.0f, 1.0f, 1.0f)));
		sphere.addComponent(new MeshComponent(sphereMesh));
		sphere.addComponent(new ShaderComponent(phongShader));
		sphere.addComponent(new MarkerCodeComponent(10));

		box = world.createEntity();
		box.addComponent(new GeometryComponent(new Vector3(-1.0f, 0.0f, 0.0f), new Matrix3().idt(), new Vector3(1.0f, 1.0f, 1.0f)));
		box.addComponent(new MeshComponent(boxMesh));
		box.addComponent(new ShaderComponent(phongShader));

		// Add the entities to the world.
		Gdx.app.log(TAG, CLASS_NAME + ".createAllEntities(): Adding entities to the world.");
		//sphere.addToWorld();
		bomb.addToWorld();
		bombModelBatch.addToWorld();
		sphere.addToWorld();
		box.addToWorld();
	}

	@Override
	public void dispose() {
		if(phongShader != null && phongShader.getShaderProgram() != null)
			phongShader.getShaderProgram().dispose();

		if(sphereMesh != null)
			sphereMesh.dispose();

		if(boxMesh != null)
			boxMesh.dispose();

		if(bombModel != null)
			bombModel.dispose();
	}
}
