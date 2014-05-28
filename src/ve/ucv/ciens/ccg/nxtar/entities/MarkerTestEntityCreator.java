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

import ve.ucv.ciens.ccg.nxtar.components.AnimationComponent;
import ve.ucv.ciens.ccg.nxtar.components.EnvironmentComponent;
import ve.ucv.ciens.ccg.nxtar.components.GeometryComponent;
import ve.ucv.ciens.ccg.nxtar.components.MarkerCodeComponent;
import ve.ucv.ciens.ccg.nxtar.components.ModelComponent;
import ve.ucv.ciens.ccg.nxtar.components.ShaderComponent;
import ve.ucv.ciens.ccg.nxtar.graphics.shaders.DirectionalLightPerPixelShader;

import com.artemis.Entity;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.FloatAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.loader.G3dModelLoader;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Matrix3;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.JsonReader;

public class MarkerTestEntityCreator extends EntityCreatorBase {
	private static final String TAG = "MARKER_TEST_ENTITY_CREATOR";
	private static final String CLASS_NAME = MarkerTestEntityCreator.class.getSimpleName();

	private Model bombModel;
	private Model animatedModel;
	private Model boxModel;
	private DirectionalLightPerPixelShader ppShader;

	@Override
	public void createAllEntities() {
		ModelBuilder builder;
		Entity bomb, box, anim;
		G3dModelLoader loader;
		Environment environment;
		Material material;

		// Create mesh.
		Gdx.app.log(TAG, CLASS_NAME + ".createAllEntities(): Creating the meshes.");

		loader = new G3dModelLoader(new JsonReader());

		bombModel = loader.loadModel(Gdx.files.internal("models/Bomb_test_2.g3dj"));
		animatedModel = loader.loadModel(Gdx.files.internal("models/cube.g3dj"));

		material = new Material(new FloatAttribute(FloatAttribute.Shininess, 50.0f), new ColorAttribute(ColorAttribute.Diffuse, 1.0f, 1.0f, 1.0f, 1.0f), new ColorAttribute(ColorAttribute.Specular, 1.0f, 1.0f, 1.0f, 1.0f));

		builder = new ModelBuilder();
		boxModel = builder.createBox(0.5f, 0.5f, 6.0f, material, new VertexAttributes(new VertexAttribute(Usage.Position, 3, "a_position"), new VertexAttribute(Usage.Normal, 3, "a_normal"), new VertexAttribute(Usage.Color, 4, "a_color")).getMask());

		// Load the shader.
		ppShader = new DirectionalLightPerPixelShader();
		ppShader.init();

		environment = new Environment();
		environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.3f, 0.3f, 0.3f, 1.0f));
		environment.add(new DirectionalLight().set(new Color(1, 1, 1, 1), new Vector3(1, 0, -0.5f)));

		// Create the entities.
		Gdx.app.log(TAG, CLASS_NAME + ".createAllEntities(): Creating the enitites.");
		bomb = world.createEntity();
		bomb.addComponent(new GeometryComponent(new Vector3(0.0f, 0.0f, 0.0f), new Matrix3().idt(), new Vector3(1.0f, 1.0f, 1.0f)));
		bomb.addComponent(new ModelComponent(bombModel));
		bomb.addComponent(new EnvironmentComponent(environment));
		bomb.addComponent(new ShaderComponent(ppShader));
		bomb.addComponent(new MarkerCodeComponent(1023));

		anim = world.createEntity();
		anim.addComponent(new GeometryComponent(new Vector3(0.0f, 0.0f, 0.0f), new Matrix3().idt(), new Vector3(0.25f, 0.25f, -0.25f)));
		anim.addComponent(new ModelComponent(animatedModel));
		anim.addComponent(new AnimationComponent(anim.getComponent(ModelComponent.class).instance, 0, true));
		anim.addComponent(new EnvironmentComponent(environment));
		anim.addComponent(new MarkerCodeComponent(89));
		anim.addComponent(new ShaderComponent(ppShader));

		box = world.createEntity();
		box.addComponent(new GeometryComponent(new Vector3(-1.0f, 0.0f, 0.0f), new Matrix3().idt(), new Vector3(1.0f, 1.0f, 1.0f)));
		box.addComponent(new ModelComponent(boxModel));
		box.addComponent(new ShaderComponent(ppShader));
		box.addComponent(new EnvironmentComponent(environment));

		// Add the entities to the world.
		Gdx.app.log(TAG, CLASS_NAME + ".createAllEntities(): Adding entities to the world.");
		//sphere.addToWorld();
		bomb.addToWorld();
		anim.addToWorld();
		box.addToWorld();
	}

	@Override
	public void dispose() {
		if(boxModel != null)
			boxModel.dispose();

		if(animatedModel != null)
			animatedModel.dispose();

		if(bombModel != null)
			bombModel.dispose();

		if(ppShader != null)
			ppShader.dispose();
	}
}
