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
import ve.ucv.ciens.ccg.nxtar.components.CollisionModelComponent;
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
	private static final String  TAG                                         = "BOMB_ENTITY_CREATOR";
	private static final String  CLASS_NAME                                  = BombGameEntityCreator.class.getSimpleName();
	private static final boolean DEBUG_RENDER_BOMB_COLLISION_MODELS          = false;
	private static final boolean DEBUG_RENDER_DOOR_COLLISION_MODELS          = false;
	private static final boolean DEBUG_RENDER_PARAPHERNALIA_COLLISION_MODELS = false;

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
	private int              currentBombId;

	// Render models.
	private Model            doorModel                     = null;
	private Model            doorFrameModel                = null;
	private Model            combinationBombModel          = null;
	private Model            combinationButton1Model       = null;
	private Model            combinationButton2Model       = null;
	private Model            combinationButton3Model       = null;
	private Model            combinationButton4Model       = null;
	private Model            inclinationBombModel          = null;
	private Model            inclinationBombButtonModel    = null;
	private Model            wiresBombModel                = null;
	private Model            wiresBombModelWire1           = null;
	private Model            wiresBombModelWire2           = null;
	private Model            wiresBombModelWire3           = null;
	private Model            easterEggModel                = null;

	// Collision models.
	private Model            doorCollisionModel                  = null;
	private Model            doorFrameCollisionModel             = null;
	private Model            combinationBombCollisionModel       = null;
	private Model            combinationButton1CollisionModel    = null;
	private Model            combinationButton2CollisionModel    = null;
	private Model            combinationButton3CollisionModel    = null;
	private Model            combinationButton4CollisionModel    = null;
	private Model            inclinationBombCollisionModel       = null;
	private Model            inclinationBombButtonCollisionModel = null;
	private Model            wiresBombCollisionModel             = null;
	private Model            wiresBombCollisionModelWire1        = null;
	private Model            wiresBombCollisionModelWire2        = null;
	private Model            wiresBombCollisionModelWire3        = null;
	private Model            easterEggCollisionModel             = null;

	public BombGameEntityCreator(){
		G3dModelLoader loader = new G3dModelLoader(new UBJsonReader());
		currentBombId = 0;

		// Create and set the lighting.
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

		// Load the render models.
		doorModel                  = loader.loadModel(Gdx.files.internal("models/render_models/bomb_game/door.g3db"));
		doorFrameModel             = loader.loadModel(Gdx.files.internal("models/render_models/bomb_game/door_frame1.g3db"));

		combinationBombModel       = loader.loadModel(Gdx.files.internal("models/render_models/bomb_game/bomb_3_body.g3db"));
		combinationButton1Model    = loader.loadModel(Gdx.files.internal("models/render_models/bomb_game/bomb_3_btn_1.g3db"));
		combinationButton2Model    = loader.loadModel(Gdx.files.internal("models/render_models/bomb_game/bomb_3_btn_2.g3db"));
		combinationButton3Model    = loader.loadModel(Gdx.files.internal("models/render_models/bomb_game/bomb_3_btn_3.g3db"));
		combinationButton4Model    = loader.loadModel(Gdx.files.internal("models/render_models/bomb_game/bomb_3_btn_4.g3db"));

		inclinationBombModel       = loader.loadModel(Gdx.files.internal("models/render_models/bomb_game/bomb_2_body.g3db"));
		inclinationBombButtonModel = loader.loadModel(Gdx.files.internal("models/render_models/bomb_game/big_btn.g3db"));

		wiresBombModel             = loader.loadModel(Gdx.files.internal("models/render_models/bomb_game/bomb_1_body.g3db"));
		wiresBombModelWire1        = loader.loadModel(Gdx.files.internal("models/render_models/bomb_game/cable_1.g3db"));
		wiresBombModelWire2        = loader.loadModel(Gdx.files.internal("models/render_models/bomb_game/cable_2.g3db"));
		wiresBombModelWire3        = loader.loadModel(Gdx.files.internal("models/render_models/bomb_game/cable_3.g3db"));
		// easterEggModel       = loader.loadModel(Gdx.files.internal("models/render_models/bomb_game/"));

		// Load the collision models.
		doorCollisionModel                  = loader.loadModel(Gdx.files.internal("models/collision_models/bomb_game/door_col.g3db"));
		doorFrameCollisionModel             = loader.loadModel(Gdx.files.internal("models/collision_models/bomb_game/door_frame1_col.g3db"));

		combinationBombCollisionModel       = loader.loadModel(Gdx.files.internal("models/collision_models/bomb_game/bomb_3_body_col.g3db"));
		combinationButton1CollisionModel    = loader.loadModel(Gdx.files.internal("models/collision_models/bomb_game/bomb_3_btn_1_col.g3db"));
		combinationButton2CollisionModel    = loader.loadModel(Gdx.files.internal("models/collision_models/bomb_game/bomb_3_btn_2_col.g3db"));
		combinationButton3CollisionModel    = loader.loadModel(Gdx.files.internal("models/collision_models/bomb_game/bomb_3_btn_3_col.g3db"));
		combinationButton4CollisionModel    = loader.loadModel(Gdx.files.internal("models/collision_models/bomb_game/bomb_3_btn_4_col.g3db"));

		inclinationBombCollisionModel       = loader.loadModel(Gdx.files.internal("models/collision_models/bomb_game/bomb_2_body_col.g3db"));
		inclinationBombButtonCollisionModel = loader.loadModel(Gdx.files.internal("models/collision_models/bomb_game/big_btn_col.g3db"));

		wiresBombCollisionModel             = loader.loadModel(Gdx.files.internal("models/collision_models/bomb_game/bomb_1_body_col.g3db"));
		wiresBombCollisionModelWire1        = loader.loadModel(Gdx.files.internal("models/collision_models/bomb_game/cable_1_col.g3db"));
		wiresBombCollisionModelWire2        = loader.loadModel(Gdx.files.internal("models/collision_models/bomb_game/cable_2_col.g3db"));
		wiresBombCollisionModelWire3        = loader.loadModel(Gdx.files.internal("models/collision_models/bomb_game/cable_3_col.g3db"));
		// easterEggCollisionModel       = loader.loadModel(Gdx.files.internal("models/collision_models/bomb_game/door.g3db"));
	}

	@Override
	public void createAllEntities(){
		// TODO: Add the robot arms.

		// Add bombs.
		parameters.markerCode = 89;
		addBomb(parameters, bomb_type_t.COMBINATION);
		parameters.markerCode = 90;
		addBomb(parameters, bomb_type_t.INCLINATION);
		parameters.markerCode = 91;
		addBomb(parameters, bomb_type_t.WIRES);

		// Add doors.
		parameters.nextAnimation = 1;
		parameters.loopAnimation = false;

		parameters.markerCode = 89;
		addDoor(parameters);
		parameters.markerCode = 90;
		addDoor(parameters);
		parameters.markerCode = 91;
		addDoor(parameters);

		// TODO: Add easter egg.
	}

	@Override
	public void dispose() {
		if(shader != null) shader.dispose();

		// Dispose of the render models.
		if(doorModel               != null) doorModel.dispose();
		if(doorFrameModel          != null) doorFrameModel.dispose();
		if(combinationBombModel    != null) combinationBombModel.dispose();
		if(combinationButton1Model != null) combinationButton1Model.dispose();
		if(combinationButton2Model != null) combinationButton2Model.dispose();
		if(combinationButton3Model != null) combinationButton3Model.dispose();
		if(combinationButton4Model != null) combinationButton4Model.dispose();
		if(inclinationBombModel    != null) inclinationBombModel.dispose();
		if(wiresBombModel          != null) wiresBombModel.dispose();
		if(wiresBombModelWire1     != null) wiresBombModelWire1.dispose();
		if(wiresBombModelWire2     != null) wiresBombModelWire2.dispose();
		if(wiresBombModelWire3     != null) wiresBombModelWire3.dispose();
		if(easterEggModel          != null) easterEggModel.dispose();

		// Dispose of the collision models.
		if(doorCollisionModel               != null) doorCollisionModel.dispose();
		if(doorFrameCollisionModel          != null) doorFrameCollisionModel.dispose();
		if(combinationBombCollisionModel    != null) combinationBombCollisionModel.dispose();
		if(combinationButton1CollisionModel != null) combinationButton1CollisionModel.dispose();
		if(combinationButton2CollisionModel != null) combinationButton2CollisionModel.dispose();
		if(combinationButton3CollisionModel != null) combinationButton3CollisionModel.dispose();
		if(combinationButton4CollisionModel != null) combinationButton4CollisionModel.dispose();
		if(inclinationBombCollisionModel    != null) inclinationBombCollisionModel.dispose();
		if(wiresBombCollisionModel          != null) wiresBombCollisionModel.dispose();
		if(wiresBombCollisionModelWire1     != null) wiresBombCollisionModelWire1.dispose();
		if(wiresBombCollisionModelWire2     != null) wiresBombCollisionModelWire2.dispose();
		if(wiresBombCollisionModelWire3     != null) wiresBombCollisionModelWire3.dispose();
		if(easterEggCollisionModel          != null) easterEggCollisionModel.dispose();
	}

	private void addBomb(EntityParameters parameters, bomb_type_t type) throws IllegalArgumentException{
		Entity bomb;
		BombComponent bombComponent = new BombComponent(currentBombId, type);

		// Create a bomb entity and add it's generic components.
		bomb = world.createEntity();
		bomb.addComponent(new GeometryComponent(new Vector3(), new Matrix3(), new Vector3(1, 1, 1)));
		bomb.addComponent(new EnvironmentComponent(parameters.environment));
		bomb.addComponent(new ShaderComponent(parameters.shader));
		bomb.addComponent(new MarkerCodeComponent(parameters.markerCode));
		bomb.addComponent(bombComponent);
		bomb.addComponent(new VisibilityComponent());

		// Add the collision and render models depending on the bomb type.
		if(type == bomb_type_t.COMBINATION){
			bomb.addComponent(new RenderModelComponent(combinationBombModel));
			bomb.addComponent(new CollisionModelComponent(combinationBombCollisionModel));
			addBombCombinationButtons(parameters, bombComponent);
			if(DEBUG_RENDER_BOMB_COLLISION_MODELS)
				addDebugCollisionModelRenderingEntity(combinationBombCollisionModel, parameters, false);

		}else if(type == bomb_type_t.INCLINATION){
			bomb.addComponent(new RenderModelComponent(inclinationBombModel));
			bomb.addComponent(new CollisionModelComponent(inclinationBombCollisionModel));
			addBombInclinationButton(parameters, bombComponent);
			if(DEBUG_RENDER_BOMB_COLLISION_MODELS)
				addDebugCollisionModelRenderingEntity(inclinationBombCollisionModel, parameters, false);

		}else if(type == bomb_type_t.WIRES){
			bomb.addComponent(new RenderModelComponent(wiresBombModel));
			bomb.addComponent(new CollisionModelComponent(wiresBombCollisionModel));
			addBombWires(parameters, bombComponent);
			if(DEBUG_RENDER_BOMB_COLLISION_MODELS)
				addDebugCollisionModelRenderingEntity(wiresBombCollisionModel, parameters, false);

		}else
			throw new IllegalArgumentException("Unrecognized bomb type: " + Integer.toString(type.getValue()));

		// Add the bomb and increase the id for the next one.
		bomb.addToWorld();
		currentBombId++;
	}

	private void addBombCombinationButtons(EntityParameters parameters, BombComponent bomb){
		Entity button1, button2, button3, button4;

		button1 = addBombParaphernalia(combinationButton1Model, combinationButton1CollisionModel, bomb, parameters);
		button2 = addBombParaphernalia(combinationButton2Model, combinationButton2CollisionModel, bomb, parameters);
		button3 = addBombParaphernalia(combinationButton3Model, combinationButton3CollisionModel, bomb, parameters);
		button4 = addBombParaphernalia(combinationButton4Model, combinationButton4CollisionModel, bomb, parameters);

		// TODO: Add button parameters.

		button1.addToWorld();
		button2.addToWorld();
		button3.addToWorld();
		button4.addToWorld();
	}

	private void addBombInclinationButton(EntityParameters parameters, BombComponent bomb){
		Entity button;

		button = addBombParaphernalia(inclinationBombButtonModel, inclinationBombButtonCollisionModel, bomb, parameters);

		// TODO: Add button parameters.

		button.addToWorld();
	}

	private void addBombWires(EntityParameters parameters, BombComponent bomb){
		Entity wire1, wire2, wire3;

		wire1 = addBombParaphernalia(wiresBombModelWire1, wiresBombCollisionModelWire1, bomb, parameters);
		wire2 = addBombParaphernalia(wiresBombModelWire2, wiresBombCollisionModelWire2, bomb, parameters);
		wire3 = addBombParaphernalia(wiresBombModelWire3, wiresBombCollisionModelWire3, bomb, parameters);

		// TODO: Add Wire parameters.

		wire1.addToWorld();
		wire2.addToWorld();
		wire3.addToWorld();
	}

	private Entity addBombParaphernalia(Model renderModel, Model collisionModel, BombComponent bomb, EntityParameters parameters){
		Entity thing;

		thing = world.createEntity();
		thing.addComponent(new GeometryComponent(new Vector3(), new Matrix3(), new Vector3(1, 1, 1)));
		thing.addComponent(new EnvironmentComponent(parameters.environment));
		thing.addComponent(new ShaderComponent(parameters.shader));
		thing.addComponent(new RenderModelComponent(renderModel));
		thing.addComponent(new CollisionModelComponent(collisionModel));
		thing.addComponent(new BombComponent(bomb));
		thing.addComponent(new VisibilityComponent());
		thing.addComponent(new MarkerCodeComponent(parameters.markerCode));

		if(DEBUG_RENDER_PARAPHERNALIA_COLLISION_MODELS)
			addDebugCollisionModelRenderingEntity(collisionModel, parameters, false);

		return thing;
	}

	private void addDoor(EntityParameters parameters){
		ModelInstance doorInstance;
		Entity frame, door;

		frame = world.createEntity();
		frame.addComponent(new GeometryComponent(new Vector3(), new Matrix3(), new Vector3(1, 1, 1)));
		frame.addComponent(new RenderModelComponent(doorFrameModel));
		frame.addComponent(new CollisionModelComponent(doorFrameCollisionModel));
		frame.addComponent(new EnvironmentComponent(parameters.environment));
		frame.addComponent(new ShaderComponent(parameters.shader));
		frame.addComponent(new VisibilityComponent());
		frame.addComponent(new MarkerCodeComponent(parameters.markerCode));
		frame.addToWorld();

		door = world.createEntity();
		door.addComponent(new GeometryComponent(new Vector3(), new Matrix3(), new Vector3(1, 1, 1)));
		door.addComponent(new RenderModelComponent(doorModel));
		door.addComponent(new CollisionModelComponent(doorCollisionModel));
		door.addComponent(new EnvironmentComponent(parameters.environment));
		door.addComponent(new ShaderComponent(parameters.shader));
		door.addComponent(new MarkerCodeComponent(parameters.markerCode));
		door.addComponent(new VisibilityComponent());
		doorInstance = door.getComponent(RenderModelComponent.class).instance;
		door.addComponent(new AnimationComponent(doorInstance, parameters.nextAnimation, parameters.loopAnimation));
		door.addToWorld();

		if(DEBUG_RENDER_DOOR_COLLISION_MODELS){
			addDebugCollisionModelRenderingEntity(doorFrameCollisionModel, parameters, false);
			addDebugCollisionModelRenderingEntity(doorCollisionModel, parameters, true);
		}
	}

	private void addDebugCollisionModelRenderingEntity(Model collisionModel, EntityParameters parameters, boolean animation){
		ModelInstance instance;
		Entity thing;

		thing = world.createEntity();
		thing.addComponent(new GeometryComponent(new Vector3(), new Matrix3(), new Vector3(1, 1, 1)));
		thing.addComponent(new EnvironmentComponent(parameters.environment));
		thing.addComponent(new ShaderComponent(parameters.shader));
		thing.addComponent(new RenderModelComponent(collisionModel));
		thing.addComponent(new VisibilityComponent());
		thing.addComponent(new MarkerCodeComponent(parameters.markerCode));
		if(animation){
			instance = thing.getComponent(RenderModelComponent.class).instance;
			thing.addComponent(new AnimationComponent(instance, parameters.nextAnimation, parameters.loopAnimation));
		}
		thing.addToWorld();
	}
}
