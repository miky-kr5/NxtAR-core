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
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.FloatAttribute;
import com.badlogic.gdx.graphics.g3d.utils.RenderContext;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.GdxRuntimeException;

public class DirectionalLightPerPixelShader implements Shader{
	private static final int     MAX_NUM_BONES        = 4;
	private static final Matrix4 IDENTITY             = new Matrix4();
	private static final String  VERTEX_SHADER_PATH   = "shaders/directionalPerPixelSingleLight/directionalPerPixel_vert.glsl";
	private static final String  FRAGMENT_SHADER_PATH = "shaders/directionalPerPixelSingleLight/directionalPerPixel_frag.glsl";
	private static final String  INCLUDE_SKINNING     = "#define SKINNING\n";

	private ShaderProgram skinningProgram;
	private ShaderProgram baseProgram;
	private Camera        camera;
	private RenderContext context;
	private Matrix4       normalMatrix;

	// Uniform locations.
	private int[]   u_geomTrans;
	private int[]   u_projTrans;
	private int[]   u_lightPos;
	private int[]   u_lightDiffuse;
	private int[]   u_specular;
	private int[]   u_ambient;
	private int[]   u_shiny;
	private int[]   u_cameraPos;
	private int[]   u_materialDiffuse;
	private int[]   u_normalMatrix;
	private int[]   u_bones;

	public DirectionalLightPerPixelShader(){
		skinningProgram = null;
		baseProgram     = null;
		camera          = null;
		context         = null;
	}

	@Override
	public void init() throws GdxRuntimeException{
		normalMatrix = new Matrix4().idt();
		u_bones      = new int[MAX_NUM_BONES];

		// Compile the shader.
		String vertexCode   = Gdx.files.internal(VERTEX_SHADER_PATH).readString();
		String fragmentCode = Gdx.files.internal(FRAGMENT_SHADER_PATH).readString();

		skinningProgram = new ShaderProgram(INCLUDE_SKINNING + vertexCode, fragmentCode);
		baseProgram     = new ShaderProgram(vertexCode, fragmentCode); 

		if(!skinningProgram.isCompiled())
			throw new GdxRuntimeException(skinningProgram.getLog());

		if(!baseProgram.isCompiled())
			throw new GdxRuntimeException(baseProgram.getLog());

		// Create uniform locations.
		u_projTrans       = new int[2];
		u_geomTrans       = new int[2];
		u_lightPos        = new int[2];
		u_lightDiffuse    = new int[2];
		u_specular        = new int[2];
		u_ambient         = new int[2];
		u_shiny           = new int[2];
		u_cameraPos       = new int[2];
		u_materialDiffuse = new int[2];
		u_normalMatrix    = new int[2];

		// Cache uniform locations.
		u_projTrans       [0]        = skinningProgram.getUniformLocation("u_projTrans");
		u_geomTrans       [0]        = skinningProgram.getUniformLocation("u_geomTrans");
		u_lightPos        [0]        = skinningProgram.getUniformLocation("u_lightPos");
		u_lightDiffuse    [0]        = skinningProgram.getUniformLocation("u_lightDiffuse");
		u_specular        [0]        = skinningProgram.getUniformLocation("u_specular");
		u_ambient         [0]        = skinningProgram.getUniformLocation("u_ambient");
		u_shiny           [0]        = skinningProgram.getUniformLocation("u_shiny");
		u_cameraPos       [0]        = skinningProgram.getUniformLocation("u_cameraPos");
		u_materialDiffuse [0]        = skinningProgram.getUniformLocation("u_materialDiffuse");
		u_normalMatrix    [0]        = skinningProgram.getUniformLocation("u_normalMatrix");

		u_projTrans       [1]        = baseProgram.getUniformLocation("u_projTrans");
		u_geomTrans       [1]        = baseProgram.getUniformLocation("u_geomTrans");
		u_lightPos        [1]        = baseProgram.getUniformLocation("u_lightPos");
		u_lightDiffuse    [1]        = baseProgram.getUniformLocation("u_lightDiffuse");
		u_specular        [1]        = baseProgram.getUniformLocation("u_specular");
		u_ambient         [1]        = baseProgram.getUniformLocation("u_ambient");
		u_shiny           [1]        = baseProgram.getUniformLocation("u_shiny");
		u_cameraPos       [1]        = baseProgram.getUniformLocation("u_cameraPos");
		u_materialDiffuse [1]        = baseProgram.getUniformLocation("u_materialDiffuse");
		u_normalMatrix    [1]        = baseProgram.getUniformLocation("u_normalMatrix");

		for(int i = 0; i < MAX_NUM_BONES; i++){
			u_bones[i] = skinningProgram.getUniformLocation("u_bone" + Integer.toString(i));
		}
	}

	@Override
	public void dispose(){
		if(skinningProgram != null) skinningProgram.dispose();
		if(baseProgram != null)     baseProgram.dispose();
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

		this.camera  = camera;
		this.context = context;

		// Set render context.
		this.context.setDepthTest(GL20.GL_LEQUAL);
		this.context.setDepthMask(true);
	}

	@Override
	public void render(Renderable renderable){
		ShaderProgram program;
		int index;
		boolean bonesEnabled;

		// Get material colors.
		Vector3 lightPosition   = renderable.environment.directionalLights.get(0).direction;
		Color diffuseLightColor = renderable.environment.directionalLights.get(0).color;
		Color diffuseColor      = ((ColorAttribute)renderable.material.get(ColorAttribute.Diffuse)).color;
		Color specularColor     = ((ColorAttribute)renderable.material.get(ColorAttribute.Specular)).color;
		Color ambientColor      = ((ColorAttribute)renderable.environment.get(ColorAttribute.AmbientLight)).color;
		float shininess         = ((FloatAttribute)renderable.material.get(FloatAttribute.Shininess)).value;

		if(renderable.mesh.getVertexAttribute(VertexAttributes.Usage.BoneWeight) != null){
			program      = skinningProgram;
			index        = 0;
			bonesEnabled = true;
		}else{
			program      = baseProgram;
			index        = 1;
			bonesEnabled = false;
		}

		program.begin();

		// Set camera dependant uniforms.
		program.setUniformMatrix(u_projTrans[index], this.camera.combined);
		program.setUniformf(u_cameraPos[index], this.camera.position);

		// Set model dependant uniforms.
		program.setUniformMatrix(u_geomTrans[index], renderable.worldTransform);
		program.setUniformMatrix(u_normalMatrix[index], normalMatrix.set(renderable.worldTransform).toNormalMatrix());
		program.setUniformf(u_lightPos[index], lightPosition);
		program.setUniformf(u_lightDiffuse[index], diffuseLightColor);
		program.setUniformf(u_materialDiffuse[index], diffuseColor);
		program.setUniformf(u_specular[index], specularColor);
		program.setUniformf(u_ambient[index], ambientColor);
		program.setUniformf(u_shiny[index], shininess);

		// Set the bones uniforms.
		if(bonesEnabled){
			for(int i = 0; i < MAX_NUM_BONES; i++){
				if(renderable.bones != null && i < renderable.bones.length && renderable.bones[i] != null)
					skinningProgram.setUniformMatrix(u_bones[i], renderable.bones[i]);
				else
					skinningProgram.setUniformMatrix(u_bones[i], IDENTITY);
			}
		}

		renderable.mesh.render(program, renderable.primitiveType, renderable.meshPartOffset, renderable.meshPartSize);

		program.end();
	}

	@Override
	public void end(){
		this.camera  = null;
		this.context = null;
	}

}
