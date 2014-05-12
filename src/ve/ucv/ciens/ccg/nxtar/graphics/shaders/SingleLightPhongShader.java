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

import ve.ucv.ciens.ccg.nxtar.exceptions.ShaderFailedToLoadException;
import ve.ucv.ciens.ccg.nxtar.graphics.LightSource;
import ve.ucv.ciens.ccg.nxtar.graphics.RenderParameters;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;

public class SingleLightPhongShader extends CustomShaderBase{
	private static String VERTEX_SHADER_PATH = "shaders/singleDiffuseLight/singleDiffuseLight_vert.glsl";
	private static String FRAGMENT_SHADER_PATH = "shaders/singleDiffuseLight/singleDiffuseLight_frag.glsl";

	@Override
	public SingleLightPhongShader loadShader() throws ShaderFailedToLoadException{
		shaderProgram = new ShaderProgram(Gdx.files.internal(VERTEX_SHADER_PATH), Gdx.files.internal(FRAGMENT_SHADER_PATH));

		if(!shaderProgram.isCompiled()){
			throw new ShaderFailedToLoadException("SingleLightPerPixelPhongShader failed to load.\n" + shaderProgram.getLog());
		}

		return this;
	}

	@Override
	public void setUniforms(){
		LightSource light = RenderParameters.getLightSource1();
		float[] diffuseColor = {light.getDiffuseColor().r, light.getDiffuseColor().g, light.getDiffuseColor().b, light.getDiffuseColor().a};
		float[] ambientColor = {light.getAmbientColor().r, light.getAmbientColor().g, light.getAmbientColor().b, light.getAmbientColor().a};
		float[] specularColor = {light.getSpecularColor().r, light.getSpecularColor().g, light.getSpecularColor().b, light.getSpecularColor().a};
		float[] position = {light.getPosition().x, light.getPosition().y, light.getPosition().z, 0.0f};
		float[] shinyness = {light.getShinyness()};

		shaderProgram.setUniformMatrix("u_projTrans", RenderParameters.getModelViewProjectionMatrix());
		shaderProgram.setUniformMatrix("u_geomTrans", RenderParameters.getTransformationMatrix());
		shaderProgram.setUniform4fv("u_lightPos", position, 0, 4);
		shaderProgram.setUniform4fv("u_lightDiffuse", diffuseColor, 0, 4);
		shaderProgram.setUniform4fv("u_specular", specularColor, 0, 4);
		shaderProgram.setUniform4fv("u_ambient", ambientColor, 0, 4);
		shaderProgram.setUniform1fv("u_shiny", shinyness, 0, 1);
		shaderProgram.setUniformf("u_cameraPos", RenderParameters.getEyePosition());
	}
}
