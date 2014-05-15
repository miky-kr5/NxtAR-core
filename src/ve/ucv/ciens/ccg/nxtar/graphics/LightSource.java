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
 * <p>A 3D light source.</p>
 */
public class LightSource{
	private Vector3 position;
	private Color ambientColor;
	private Color diffuseColor;
	private Color specularColor;
	private float shinyness;

	/**
	 * <p>Creates a default white light source positioned at (0,0,0).</p>
	 */
	public LightSource(){
		position = new Vector3(0.0f, 0.0f, 0.0f);
		ambientColor = new Color(0.15f, 0.15f, 0.15f, 1.0f);
		diffuseColor = new Color(1.0f, 1.0f, 1.0f, 1.0f);
		specularColor = new Color(1.0f, 1.0f, 1.0f, 1.0f);
		ambientColor = new Color(1.0f, 1.0f, 1.0f, 1.0f);
		shinyness = 10.0f;
	}

	/**
	 * <p>Creates a white light source at the specified position.</p>
	 * 
	 * @param position The location of the light source.
	 */
	public LightSource(Vector3 position){
		this.position = new Vector3();

		this.position.set(position);
		ambientColor = new Color(0.15f, 0.15f, 0.15f, 1.0f);
		diffuseColor = new Color(1.0f, 1.0f, 1.0f, 1.0f);
		specularColor = new Color(1.0f, 1.0f, 1.0f, 1.0f);
		ambientColor = new Color(1.0f, 1.0f, 1.0f, 1.0f);
		shinyness = 10.0f;
	}

	/**
	 * <p>Creates a custom light source.</p>
	 *  
	 * @param position The location of the light source.
	 * @param ambientColor
	 * @param diffuseColor
	 * @param specularColor 
	 * @param shinyness The shinyness component. Must be between (0.0, 128.0].
	 * @throws IllegalArgumentException When shinyness is outside the valid range.
	 */
	public LightSource(Vector3 position, Color ambientColor, Color diffuseColor, Color specularColor, float shinyness) throws IllegalArgumentException {
		if(shinyness <= 0.0 || shinyness > 128.0)
			throw new IllegalArgumentException("Shinyness must be between (0.0, 128.0].");

		this.position = new Vector3();
		this.ambientColor = new Color();
		this.diffuseColor = new Color();
		this.ambientColor = new Color();
		this.specularColor = new Color();

		this.position.set(position);
		this.ambientColor.set(ambientColor);
		this.diffuseColor.set(diffuseColor);
		this.specularColor.set(specularColor);
		this.shinyness = shinyness;
	}

	public LightSource(LightSource light){
		this.position = new Vector3();
		this.ambientColor = new Color();
		this.diffuseColor = new Color();
		this.ambientColor = new Color();
		this.specularColor = new Color();

		set(light);
	}

	public void set(LightSource light){
		this.position.set(light.getPosition());
		this.ambientColor.set(light.getAmbientColor());
		this.diffuseColor.set(light.getDiffuseColor());
		this.specularColor.set(light.getSpecularColor());
		this.shinyness = light.shinyness;
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

	public void setShinyness(float shinyness) throws IllegalArgumentException {
		if(shinyness <= 0.0 || shinyness > 128.0)
			throw new IllegalArgumentException("Shinyness must be between (0.0, 128.0].");

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
