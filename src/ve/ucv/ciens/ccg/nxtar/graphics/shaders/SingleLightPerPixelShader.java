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
package ve.ucv.ciens.ccg.nxtar.graphics.shaders;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.FloatAttribute;
import com.badlogic.gdx.graphics.g3d.utils.RenderContext;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.GdxRuntimeException;

public class SingleLightPerPixelShader implements Shader{
	private static final String TAG                  = "SINGLE_LIGHT_PER_PIXEL_SHADER";
	private static final String CLASS_NAME           = SingleLightPerPixelShader.class.getSimpleName();
	private static final String VERTEX_SHADER_PATH   = "shaders/directionalPerPixelSingleLight/directionalPerPixel_vert.glsl";
	private static final String FRAGMENT_SHADER_PATH = "shaders/directionalPerPixelSingleLight/directionalPerPixel_frag.glsl";

	private ShaderProgram program;
	private Camera        camera;
	private RenderContext context;

	// Uniform locations.
	private int u_geomTrans;
	private int u_projTrans;
	private int u_lightPos;
	private int u_lightDiffuse;
	private int u_specular;
	private int u_ambient;
	private int u_shiny;
	private int u_cameraPos;
	private int u_materialDiffuse;

	public SingleLightPerPixelShader(){
		program = null;
		camera  = null;
		context = null;
	}

	@Override
	public void init() throws GdxRuntimeException{
		// Compile the shader.
		program = new ShaderProgram(Gdx.files.internal(VERTEX_SHADER_PATH), Gdx.files.internal(FRAGMENT_SHADER_PATH));
		if(!program.isCompiled()){
			Gdx.app.log(TAG, CLASS_NAME + ".init(): Shader failed to compile.");
			throw new GdxRuntimeException(program.getLog());
		}

		// Cache uniform locations.
		u_projTrans       = program.getUniformLocation("u_projTrans");
		u_geomTrans       = program.getUniformLocation("u_geomTrans");
		u_lightPos        = program.getUniformLocation("u_lightPos");
		u_lightDiffuse    = program.getUniformLocation("u_lightDiffuse");
		u_specular        = program.getUniformLocation("u_specular");
		u_ambient         = program.getUniformLocation("u_ambient");
		u_shiny           = program.getUniformLocation("u_shiny");
		u_cameraPos       = program.getUniformLocation("u_cameraPos");
		u_materialDiffuse = program.getUniformLocation("u_materialDiffuse");
	}

	@Override
	public void dispose(){
		if(program != null)
			program.dispose();
	}

	@Override
	public int compareTo(Shader other){
		return 0;
	}

	@Override
	public boolean canRender(Renderable renderable){
		// Check for all needed lighting and material attributes.
		if(renderable.environment.directionalLights.size < 1)
			return false;
		if(!renderable.environment.has(ColorAttribute.AmbientLight))
			return false;
		if(!renderable.material.has(ColorAttribute.Diffuse))
			return false;
		if(!renderable.material.has(ColorAttribute.Specular))
			return false;
		if(!renderable.material.has(FloatAttribute.Shininess))
			return false;

		return true;
	}

	@Override
	public void begin(Camera camera, RenderContext context) throws GdxRuntimeException{
		if(this.camera != null || this.context != null)
			throw new GdxRuntimeException("Called begin twice before calling end.");

		this.camera = camera;
		this.context = context;
		program.begin();

		// Set camera dependant uniforms.
		program.setUniformMatrix(u_projTrans, this.camera.combined);
		program.setUniformf(u_cameraPos, this.camera.position);

		// Set render context.
		this.context.setDepthTest(GL20.GL_LEQUAL);
		this.context.setDepthMask(true);
	}

	@Override
	public void render(Renderable renderable){
		// Get material colors.
		Vector3 lightPosition     = renderable.environment.directionalLights.get(0).direction;
		Color   diffuseLightColor = renderable.environment.directionalLights.get(0).color;
		Color   diffuseColor      = ((ColorAttribute)renderable.material.get(ColorAttribute.Diffuse)).color;
		Color   specularColor     = ((ColorAttribute)renderable.material.get(ColorAttribute.Specular)).color;
		Color   ambientColor      = ((ColorAttribute)renderable.environment.get(ColorAttribute.AmbientLight)).color;
		float   shininess         = ((FloatAttribute)renderable.material.get(FloatAttribute.Shininess)).value;

		// Set model dependant uniforms.
		program.setUniformMatrix(u_geomTrans, renderable.worldTransform);
		program.setUniformf(u_lightPos, lightPosition);
		program.setUniformf(u_lightDiffuse, diffuseLightColor);
		program.setUniformf(u_materialDiffuse, diffuseColor);
		program.setUniformf(u_specular, specularColor);
		program.setUniformf(u_ambient, ambientColor);
		program.setUniformf(u_shiny, shininess);

		// Render.
		renderable.mesh.render(program, renderable.primitiveType, renderable.meshPartOffset, renderable.meshPartSize);
	}

	@Override
	public void end(){
		program.end();

		this.camera = null;
		this.context = null;
	}

}
