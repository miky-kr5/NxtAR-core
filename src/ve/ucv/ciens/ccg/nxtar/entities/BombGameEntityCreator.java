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
import ve.ucv.ciens.ccg.nxtar.components.BombComponent;
import ve.ucv.ciens.ccg.nxtar.components.BombComponent.bomb_type_t;
import ve.ucv.ciens.ccg.nxtar.components.EnvironmentComponent;
import ve.ucv.ciens.ccg.nxtar.components.GeometryComponent;
import ve.ucv.ciens.ccg.nxtar.components.MarkerCodeComponent;
import ve.ucv.ciens.ccg.nxtar.components.RenderModelComponent;
import ve.ucv.ciens.ccg.nxtar.components.ShaderComponent;
import ve.ucv.ciens.ccg.nxtar.components.VisibilityComponent;
import ve.ucv.ciens.ccg.nxtar.graphics.shaders.DirectionalLightPerPixelShader;

import com.artemis.Entity;
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
import com.badlogic.gdx.utils.UBJsonReader;

public class BombGameEntityCreator extends EntityCreatorBase{
	private static final String TAG        = "BOMB_ENTITY_CREATOR";
	private static final String CLASS_NAME = BombGameEntityCreator.class.getSimpleName();

	private class EntityParameters{
		public Environment environment;
		public Shader      shader;
		public int         markerCode;
		public int         nextAnimation;
		public boolean     loopAnimation;

		public EntityParameters(){
			environment   = new Environment();
			shader        = null;
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
	private Model            bombModelWiresWire1;
	private Model            bombModelWiresWire2;
	private Model            bombModelWiresWire3;
	private Model            easterEggModel;
	private int              currentBombId;

	public BombGameEntityCreator(){
		G3dModelLoader loader = new G3dModelLoader(new UBJsonReader());
		currentBombId = 0;

		parameters = new EntityParameters();
		parameters.environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.3f, 0.3f, 0.3f, 1.0f));
		parameters.environment.add(new DirectionalLight().set(new Color(1, 1, 1, 1), new Vector3(0, 0, -1)));

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
		// TODO: Load collision models.
		doorModel            = loader.loadModel(Gdx.files.internal("models/render_models/bomb_game/door.g3db"));
		doorFrameModel       = loader.loadModel(Gdx.files.internal("models/render_models/bomb_game/door_frame1.g3db"));
//		bombModelCombination = loader.loadModel(Gdx.files.internal("models/render_models/bomb_game/"));
//		bombModelInclination = loader.loadModel(Gdx.files.internal("models/render_models/bomb_game/"));
		bombModelWires       = loader.loadModel(Gdx.files.internal("models/render_models/bomb_game/bomb_1_body.g3db"));
//		easterEggModel       = loader.loadModel(Gdx.files.internal("models/render_models/bomb_game/"));
		bombModelWiresWire1  = loader.loadModel(Gdx.files.internal("models/render_models/bomb_game/cable_1.g3db"));
		bombModelWiresWire2  = loader.loadModel(Gdx.files.internal("models/render_models/bomb_game/cable_2.g3db"));
		bombModelWiresWire3  = loader.loadModel(Gdx.files.internal("models/render_models/bomb_game/cable_3.g3db"));
	}

	@Override
	public void createAllEntities(){
		// TODO: Add the robot arms.

		// Add bombs.
//		parameters.markerCode = 89;
//		addBomb(parameters, bomb_type_t.COMBINATION);
//
//		parameters.markerCode = 90;
//		addBomb(parameters, bomb_type_t.INCLINATION);

		parameters.markerCode = 91;
		addBomb(parameters, bomb_type_t.WIRES);

		// Add doors.
		parameters.nextAnimation = 1;
		parameters.loopAnimation = false;

//		parameters.markerCode = 89;
//		addDoor(parameters);
//		parameters.markerCode = 90;
//		addDoor(parameters);
		parameters.markerCode = 91;
		addDoor(parameters);
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
		if(bombModelWiresWire1 != null)
			bombModelWiresWire1.dispose();
		if(bombModelWiresWire2 != null)
			bombModelWiresWire2.dispose();
		if(bombModelWiresWire3 != null)
			bombModelWiresWire3.dispose();
		if(easterEggModel != null)
			easterEggModel.dispose();
	}

	private void addBomb(EntityParameters parameters, bomb_type_t type) throws IllegalArgumentException{
		Entity bomb;
		BombComponent bombComponent = new BombComponent(currentBombId, type);

		bomb = world.createEntity();
		bomb.addComponent(new GeometryComponent(new Vector3(), new Matrix3(), new Vector3(1, 1, 1)));
		bomb.addComponent(new EnvironmentComponent(parameters.environment));
		bomb.addComponent(new ShaderComponent(parameters.shader));
		bomb.addComponent(new MarkerCodeComponent(parameters.markerCode));
		bomb.addComponent(bombComponent);
		bomb.addComponent(new VisibilityComponent());

		// Add the collision and render models depending on the bomb type.
		if(type == bomb_type_t.COMBINATION){
			bomb.addComponent(new RenderModelComponent(bombModelCombination));
		}else if(type == bomb_type_t.INCLINATION){
			bomb.addComponent(new RenderModelComponent(bombModelInclination));
		}else if(type == bomb_type_t.WIRES){
			bomb.addComponent(new RenderModelComponent(bombModelWires));
			addBombWires(parameters, bombComponent);
		}else
			throw new IllegalArgumentException("Unrecognized bomb type: " + Integer.toString(type.getValue()));

		bomb.addToWorld();
		currentBombId++;
	}

	private void addBombWires(EntityParameters parameters, BombComponent bomb){
		// TODO: Add collision models.
		Entity wire1, wire2, wire3;

		wire1 = world.createEntity();
		wire1.addComponent(new GeometryComponent(new Vector3(), new Matrix3(), new Vector3(1, 1, 1)));
		wire1.addComponent(new EnvironmentComponent(parameters.environment));
		wire1.addComponent(new ShaderComponent(parameters.shader));
		wire1.addComponent(new RenderModelComponent(bombModelWiresWire1));
		wire1.addComponent(new BombComponent(bomb));
		wire1.addComponent(new VisibilityComponent());
		wire1.addComponent(new MarkerCodeComponent(parameters.markerCode));
		wire1.addToWorld();

		wire2 = world.createEntity();
		wire2.addComponent(new GeometryComponent(new Vector3(), new Matrix3(), new Vector3(1, 1, 1)));
		wire2.addComponent(new EnvironmentComponent(parameters.environment));
		wire2.addComponent(new ShaderComponent(parameters.shader));
		wire2.addComponent(new RenderModelComponent(bombModelWiresWire2));
		wire2.addComponent(new BombComponent(bomb));
		wire2.addComponent(new VisibilityComponent());
		wire2.addComponent(new MarkerCodeComponent(parameters.markerCode));
		wire2.addToWorld();

		wire3 = world.createEntity();
		wire3.addComponent(new GeometryComponent(new Vector3(), new Matrix3(), new Vector3(1, 1, 1)));
		wire3.addComponent(new EnvironmentComponent(parameters.environment));
		wire3.addComponent(new ShaderComponent(parameters.shader));
		wire3.addComponent(new RenderModelComponent(bombModelWiresWire3));
		wire3.addComponent(new BombComponent(bomb));
		wire3.addComponent(new VisibilityComponent());
		wire3.addComponent(new MarkerCodeComponent(parameters.markerCode));
		wire3.addToWorld();
	}

	private void addDoor(EntityParameters parameters){
		// TODO: Add collision models.
		ModelInstance doorInstance;
		Entity frame, door;

		frame = world.createEntity();
		frame.addComponent(new GeometryComponent(new Vector3(), new Matrix3(), new Vector3(1, 1, 1)));
		frame.addComponent(new RenderModelComponent(doorFrameModel));
		frame.addComponent(new EnvironmentComponent(parameters.environment));
		frame.addComponent(new ShaderComponent(parameters.shader));
		frame.addComponent(new VisibilityComponent());
		frame.addComponent(new MarkerCodeComponent(parameters.markerCode));

		frame.addToWorld();

		door = world.createEntity();
		door.addComponent(new GeometryComponent(new Vector3(), new Matrix3(), new Vector3(1, 1, 1)));
		door.addComponent(new RenderModelComponent(doorModel));
		door.addComponent(new EnvironmentComponent(parameters.environment));
		door.addComponent(new ShaderComponent(parameters.shader));
		door.addComponent(new MarkerCodeComponent(parameters.markerCode));
		door.addComponent(new VisibilityComponent());
		doorInstance = door.getComponent(RenderModelComponent.class).instance;
		door.addComponent(new AnimationComponent(doorInstance, parameters.nextAnimation, parameters.loopAnimation));

		door.addToWorld();
	}
}
