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

import ve.ucv.ciens.ccg.nxtar.components.EnvironmentComponent;
import ve.ucv.ciens.ccg.nxtar.components.GeometryComponent;
import ve.ucv.ciens.ccg.nxtar.components.MarkerCodeComponent;
import ve.ucv.ciens.ccg.nxtar.components.RenderModelComponent;
import ve.ucv.ciens.ccg.nxtar.components.ShaderComponent;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.annotations.Mapper;
import com.artemis.systems.EntityProcessingSystem;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Disposable;

/**
 * <p>Entity processing system in charge of rendering 3D objects using OpenGL. The
 * entities to be rendered must have a geometry, shader and mesh component associated.</p>
 */
public class RobotArmRenderingSystem extends EntityProcessingSystem implements Disposable{
	@Mapper ComponentMapper<ShaderComponent>       shaderMapper;
	@Mapper ComponentMapper<RenderModelComponent>  modelMapper;
	@Mapper ComponentMapper<EnvironmentComponent>  environmentMapper;
	@Mapper ComponentMapper<GeometryComponent>     geometryMapper;

	private PerspectiveCamera camera;
	private ModelBatch        batch;
	private Model             lineModel;
	private ModelInstance     lineInstance;
	private Vector3           temp;

	@SuppressWarnings("unchecked")
	public RobotArmRenderingSystem(ModelBatch batch) {
		super(Aspect.getAspectForAll(ShaderComponent.class, RenderModelComponent.class, EnvironmentComponent.class).exclude(MarkerCodeComponent.class));

		camera     = null;
		this.batch = batch;
//		MeshBuilder builder = new MeshBuilder();
//		builder.begin(new VertexAttributes(new VertexAttribute(Usage.Position, 4, "a_position"), new VertexAttribute(Usage.Color, 4, "a_color")), GL20.GL_LINES);{
//			builder.line(new Vector3(0.0f, 0.0f, RobotArmPositioningSystem.MIN_Z), Color.YELLOW, new Vector3(0.0f, 0.0f, RobotArmPositioningSystem.MAX_Z), Color.YELLOW);
//		}lineMesh = builder.end();
//		lineModel = ModelBuilder.createFromMesh(lineMesh, GL20.GL_LINES, new Material(new ColorAttribute(ColorAttribute.Diffuse, Color.YELLOW)));
//		lineModel = new ModelBuilder().createArrow(new Vector3(0.0f, 0.0f, RobotArmPositioningSystem.MIN_Z), new Vector3(0.0f, 0.0f, RobotArmPositioningSystem.MAX_Z), new Material(new ColorAttribute(ColorAttribute.Diffuse, Color.YELLOW)), Usage.Position | Usage.Color | Usage.Normal);
		lineModel = new ModelBuilder().createBox(0.01f, 0.01f, 3.5f, new Material(new ColorAttribute(ColorAttribute.Diffuse, Color.YELLOW)), Usage.Position | Usage.Color | Usage.Normal);
		lineInstance = new ModelInstance(lineModel);
		temp = new Vector3();
	}

	public void begin(PerspectiveCamera camera) throws RuntimeException{
		if(this.camera != null)
			throw new RuntimeException("Begin called twice without calling end.");

		this.camera = camera;
		batch.begin(camera);
	}

	public void end(){
		batch.end();
		camera = null;
	}

	@Override
	protected void process(Entity e) {
		EnvironmentComponent  environment;
		ShaderComponent       shaderComponent;
		RenderModelComponent  renderModelComponent;
		GeometryComponent     geometry;

		// Get the necessary components.
		renderModelComponent = modelMapper.get(e);
		shaderComponent      = shaderMapper.get(e);
		environment          = environmentMapper.get(e);
		geometry = geometryMapper.getSafe(e);

		if(geometry != null){
			temp.set(geometry.position.x, geometry.position.y, -2.5f);
			lineInstance.transform.idt().setToTranslation(temp);
		}

		// Render this entity.
		batch.render(renderModelComponent.instance, environment.environment, shaderComponent.shader);
		batch.render(lineInstance, environment.environment, shaderComponent.shader);
	}

	@Override
	public void dispose() {
		if(lineModel != null)
			lineModel.dispose();
	}
}
