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
package ve.ucv.ciens.ccg.nxtar.graphics;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector3;

/**
 * <p>A 3D point or directional light source.</p>
 */
public class LightSource{
	private Vector3 position;
	private Color ambientColor;
	private Color diffuseColor;
	private Color specularColor;
	private float shinyness;

	public LightSource(){
		position = new Vector3(0.0f, 0.0f, 0.0f);
		ambientColor = new Color(0.15f, 0.15f, 0.15f, 1.0f);
		diffuseColor = new Color(1.0f, 1.0f, 1.0f, 1.0f);
		ambientColor = new Color(1.0f, 1.0f, 1.0f, 1.0f);
		shinyness = 10.0f;
	}

	public LightSource(Vector3 position){
		this.position.set(position);
		ambientColor = new Color(0.15f, 0.15f, 0.15f, 1.0f);
		diffuseColor = new Color(1.0f, 1.0f, 1.0f, 1.0f);
		ambientColor = new Color(1.0f, 1.0f, 1.0f, 1.0f);
		shinyness = 10.0f;
	}

	public LightSource(Vector3 position, Color ambientColor, Color diffuseColor, Color specularColor, float shinyness){
		this.position.set(position);
		this.ambientColor.set(ambientColor);
		this.diffuseColor.set(diffuseColor);
		this.specularColor.set(specularColor);
		this.shinyness = shinyness;
	}

	public void setPosition(float x, float y, float z){
		position.set(x, y, z);
	}

	public void setPosition(Vector3 position){
		this.position.set(position);
	}

	public void setAmbientColor(float r, float g, float b, float a){
		ambientColor.set(r, g, b, a);
	}

	public void setAmbientColor(Color ambientColor){
		this.ambientColor.set(ambientColor);
	}

	public void setDiffuseColor(float r, float g, float b, float a){
		diffuseColor.set(r, g, b, a);
	}

	public void setdiffuseColor(Color diffuseColor){
		this.diffuseColor.set(diffuseColor);
	}

	public void setSpecularColor(float r, float g, float b, float a){
		specularColor.set(r, g, b, a);
	}

	public void setSpecularColor(Color specularColor){
		this.specularColor.set(specularColor);
	}

	public void setShinyness(float shinyness){
		this.shinyness = shinyness;
	}

	public Vector3 getPosition(){
		return position;
	}

	public Color getAmbientColor(){
		return ambientColor;
	}

	public Color getDiffuseColor(){
		return diffuseColor;
	}

	public Color getSpecularColor(){
		return specularColor;
	}

	public float getShinyness(){
		return shinyness;
	}
}
