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

import ve.ucv.ciens.ccg.nxtar.components.AutomaticMovementComponent;
import ve.ucv.ciens.ccg.nxtar.components.CollisionDetectionComponent;
import ve.ucv.ciens.ccg.nxtar.components.GeometryComponent;
import ve.ucv.ciens.ccg.nxtar.components.MarkerCodeComponent;
import ve.ucv.ciens.ccg.nxtar.input.GamepadUserInput;
import ve.ucv.ciens.ccg.nxtar.input.KeyboardUserInput;
import ve.ucv.ciens.ccg.nxtar.input.TouchUserInput;
import ve.ucv.ciens.ccg.nxtar.input.UserInput;
import ve.ucv.ciens.ccg.nxtar.utils.Utils;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.annotations.Mapper;
import com.artemis.systems.EntityProcessingSystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector3;

public class RobotArmPositioningSystem extends EntityProcessingSystem {
	private static final String TAG        = "ROBOT_ARM_POSITIONING_SYSTEM";
	private static final String CLASS_NAME = RobotArmPositioningSystem.class.getSimpleName();
	private static final float  STEP_SIZE  = 0.05f;

	@Mapper ComponentMapper<GeometryComponent>           geometryMapper;
	@Mapper ComponentMapper<AutomaticMovementComponent>  autoMapper;
	@Mapper ComponentMapper<CollisionDetectionComponent> collisionMapper;

	private UserInput input;

	@SuppressWarnings("unchecked")
	public RobotArmPositioningSystem(){
		super(Aspect.getAspectForAll(GeometryComponent.class, AutomaticMovementComponent.class, CollisionDetectionComponent.class).exclude(MarkerCodeComponent.class));
	}

	public void setUserInput(UserInput input){
		this.input = input;
	}

	@Override
	protected void process(Entity e) throws ClassCastException{
		Vector3                     endPoint;
		GamepadUserInput            tempGP;
		KeyboardUserInput           tempKey;
		GeometryComponent           geometry  = geometryMapper.get(e);
		AutomaticMovementComponent  auto      = autoMapper.get(e);
		CollisionDetectionComponent collision = collisionMapper.get(e);

		if(input == null){
			if(auto.moving) autoMove(geometry, auto, collision);
			else return;

		}else{
			if(input instanceof TouchUserInput){
				if(!auto.moving){
					endPoint = ((TouchUserInput) input).userTouchEndPoint;
					endPoint.set(endPoint.x, endPoint.y, -4.5f);
					auto.startPoint.set(geometry.position);
					auto.endPoint.set(endPoint);
					auto.moving = true;
					auto.forward = true;

					Gdx.app.log(TAG, CLASS_NAME + ".process(): Started moving from " + Utils.vector2String(auto.startPoint) + " to " + Utils.vector2String(auto.endPoint));
				}else autoMove(geometry, auto, collision);

			}else if(input instanceof GamepadUserInput){
				tempGP = (GamepadUserInput) input;
				geometry.position.x += !collision.colliding ? tempGP.axisLeftY * STEP_SIZE : 0.0f;
				geometry.position.y += !collision.colliding ? tempGP.axisLeftX * STEP_SIZE : 0.0f;
				geometry.position.z += !collision.colliding ? tempGP.axisRightY * STEP_SIZE : 0.0f;
				clampPosition(geometry);

			}else if(input instanceof KeyboardUserInput){
				tempKey = (KeyboardUserInput) input;
				geometry.position.x -= tempKey.keyUp && !collision.colliding ? STEP_SIZE : 0.0f;
				geometry.position.x += tempKey.keyDown && !collision.colliding ? STEP_SIZE : 0.0f;
				geometry.position.y -= tempKey.keyLeft && !collision.colliding ? STEP_SIZE : 0.0f;
				geometry.position.y += tempKey.keyRight && !collision.colliding ? STEP_SIZE : 0.0f;
				geometry.position.z -= tempKey.keyZ && !collision.colliding ? STEP_SIZE : 0.0f;
				geometry.position.z += tempKey.keyA && !collision.colliding ? STEP_SIZE : 0.0f;
				clampPosition(geometry);

			}else
				throw new ClassCastException("Input is not a valid UserInput instance.");
		}

		input = null;
	}

	private void autoMove(GeometryComponent geometry, AutomaticMovementComponent auto, CollisionDetectionComponent collision){
		float step;

		if(auto.moving){
			if(auto.forward)
				step = STEP_SIZE;
			else
				step = -STEP_SIZE;

			Gdx.app.log(TAG, CLASS_NAME + ".autoMove(): Step = " + Float.toString(step));

			auto.distance += step;

			Gdx.app.log(TAG, CLASS_NAME + ".autoMove(): Step = " + Float.toString(auto.distance));

			geometry.position.x = (auto.startPoint.x * (1.0f - auto.distance)) + (auto.endPoint.x * auto.distance);
			geometry.position.y = (auto.startPoint.y * (1.0f - auto.distance)) + (auto.endPoint.y * auto.distance);
			geometry.position.z = (auto.startPoint.z * (1.0f - auto.distance)) + (auto.endPoint.z * auto.distance);

			Gdx.app.log(TAG, CLASS_NAME + ".autoMove(): Current position: " + Utils.vector2String(geometry.position));

			if(auto.distance >= 1.0f || collision.colliding){
				auto.forward = false;
				Gdx.app.log(TAG, CLASS_NAME + ".autoMove(): Going backwards now.");
			}else if(auto.distance <= 0.0f){
				auto.forward = true;
				auto.moving = false;
				Gdx.app.log(TAG, CLASS_NAME + ".autoMove(): Going forward now.");
			}

		}else return;
	}

	private void clampPosition(GeometryComponent geometry){
		geometry.position.x = geometry.position.x >= -1.0f ? geometry.position.x : -1.0f;
		geometry.position.x = geometry.position.x <= 1.0f ? geometry.position.x : 1.0f;
		geometry.position.y = geometry.position.y >= -1.0f ? geometry.position.y : -1.0f;
		geometry.position.y = geometry.position.y <= 1.0f ? geometry.position.y : 1.0f;
		geometry.position.z = geometry.position.z >= 0.0f ? geometry.position.z : 0.0f;
		geometry.position.z = geometry.position.z <= 6.0f ? geometry.position.z : 6.0f;
	}
}
