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
import com.artemis.World;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.loader.G3dModelLoader;
import com.badlogic.gdx.math.Matrix3;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.JsonReader;

public class BombGameEntityCreator extends EntityCreatorBase{
	private static final String TAG        = "BOMB_ENTITY_CREATOR";
	private static final String CLASS_NAME = BombGameEntityCreator.class.getSimpleName();

	/*private enum bomb_type_t{
		COMBINATION(0), INCLINATION(1), WIRES(2);

		private int value;

		private bomb_type_t(int value){
			this.value = value;
		}

		public int getValue(){
			return this.value;
		}
	};*/

	private class EntityParameters{
		public Environment environment;
		public Shader      shader;
		public Model       model1;
		public Model       model2;
		public int         markerCode;
		public int         nextAnimation;
		public boolean     loopAnimation;

		public EntityParameters(){
			environment   = new Environment();
			shader        = null;
			model1        = null;
			model2        = null;
			markerCode    = -1;
			nextAnimation = -1;
			loopAnimation = false;
		}
	}

	private EntityParameters parameters;
	private Shader           shader;
	private Model            doorModel;
	private Model            doorFrameModel;
	private Model            bombModelCombination;
	private Model            bombModelInclination;
	private Model            bombModelWires;
	private Model            easterEggModel;

	public BombGameEntityCreator(){
		G3dModelLoader loader = new G3dModelLoader(new JsonReader());

		parameters = new EntityParameters();
		parameters.environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.3f, 0.3f, 0.3f, 1.0f));
		parameters.environment.add(new DirectionalLight().set(new Color(1, 1, 1, 1), new Vector3(1, 0, -0.5f)));

		// Load the shader.
		shader = new DirectionalLightPerPixelShader();
		try{
			shader.init();
		}catch(GdxRuntimeException gdx){
			Gdx.app.error(TAG, CLASS_NAME + ".BombGameEntityCreator(): Shader failed to load: " + gdx.getMessage());
			shader = null;
		}
		parameters.shader = shader;

		// Create the models.
		// TODO: Set the correct model paths.
		doorModel            = loader.loadModel(Gdx.files.internal(""));
		doorFrameModel       = loader.loadModel(Gdx.files.internal(""));
		bombModelCombination = loader.loadModel(Gdx.files.internal(""));
		bombModelInclination = loader.loadModel(Gdx.files.internal(""));
		bombModelWires       = loader.loadModel(Gdx.files.internal(""));
		easterEggModel       = loader.loadModel(Gdx.files.internal(""));
	}

	@Override
	public void createAllEntities(){
		// TODO: Create the scene.

		// TODO: Add the robot arms.
		
		// Add bombs.
		parameters.markerCode = 89;
		parameters.model1 = bombModelCombination;
		addBomb(world, parameters);

		parameters.markerCode = 90;
		parameters.model1 = bombModelInclination;
		addBomb(world, parameters);

		parameters.markerCode = 91;
		parameters.model1 = bombModelWires;
		addBomb(world, parameters);

		// Add doors.
		parameters.model1 = doorFrameModel;
		parameters.model2 = doorModel;
		parameters.nextAnimation = 0;
		parameters.loopAnimation = false;

		parameters.markerCode = 89;
		addDoor(world, parameters);
		parameters.markerCode = 90;
		addDoor(world, parameters);
		parameters.markerCode = 91;
		addDoor(world, parameters);
	}

	@Override
	public void dispose() {
		if(shader != null)
			shader.dispose();

		// Dispose of the models.
		if(doorModel != null)
			doorModel.dispose();
		if(doorFrameModel != null)
			doorFrameModel.dispose();
		if(bombModelCombination != null)
			bombModelCombination.dispose();
		if(bombModelInclination != null)
			bombModelInclination.dispose();
		if(bombModelWires != null)
			bombModelWires.dispose();
		if(easterEggModel != null)
			easterEggModel.dispose();
	}

	private void addBomb(World world, EntityParameters parameters){
		Entity bomb;

		bomb = world.createEntity();
		bomb.addComponent(new GeometryComponent(new Vector3(), new Matrix3(), new Vector3(1, 1, 1)));
		bomb.addComponent(new ModelComponent(parameters.model1));
		bomb.addComponent(new EnvironmentComponent(parameters.environment));
		bomb.addComponent(new ShaderComponent(parameters.shader));
		bomb.addComponent(new MarkerCodeComponent(parameters.markerCode));
		bomb.addToWorld();
	}

	private void addDoor(World world, EntityParameters parameters){
		ModelInstance frameModel, doorModel;
		Entity frame, door;

		frameModel = new ModelInstance(parameters.model1);
		doorModel  = new ModelInstance(parameters.model2);

		frame = world.createEntity();
		frame.addComponent(new GeometryComponent(new Vector3(), new Matrix3(), new Vector3(1, 1, 1)));
		frame.addComponent(new ModelComponent(frameModel));
		frame.addComponent(new EnvironmentComponent(parameters.environment));
		frame.addComponent(new ShaderComponent(parameters.shader));
		frame.addComponent(new MarkerCodeComponent(parameters.markerCode));
		frame.addToWorld();

		door = world.createEntity();
		door.addComponent(new GeometryComponent(new Vector3(), new Matrix3(), new Vector3(1, 1, 1)));
		door.addComponent(new ModelComponent(doorModel));
		door.addComponent(new EnvironmentComponent(parameters.environment));
		door.addComponent(new ShaderComponent(parameters.shader));
		door.addComponent(new MarkerCodeComponent(parameters.markerCode));
		door.addComponent(new AnimationComponent(doorModel, parameters.nextAnimation, parameters.loopAnimation));
		door.addToWorld();
	}
}
