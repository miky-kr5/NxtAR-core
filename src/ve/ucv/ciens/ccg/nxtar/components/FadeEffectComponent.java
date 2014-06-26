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
package ve.ucv.ciens.ccg.nxtar.components;

import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenEquations;
import aurelienribon.tweenengine.primitives.MutableFloat;

import com.artemis.Component;
import com.badlogic.gdx.graphics.Color;

public class FadeEffectComponent extends Component{
	private MutableFloat alpha;
	private Tween fadeIn;
	private Tween fadeOut;
	public  Color color;

	/**
	 * <p>Creates a fade to/from white depending on the parameter.</p>
	 * 
	 * @param fadeIn True to create a fade FROM white, false for a fade TO white.
	 */
	public FadeEffectComponent(boolean fadeIn){
		if(fadeIn){
			this.alpha = new MutableFloat(1.0f);
			this.fadeIn = Tween.to(alpha, 0, 2.0f).target(0.0f).ease(TweenEquations.easeInQuint);
			this.fadeOut = null;
		}else{
			this.alpha = new MutableFloat(0.0f);
			this.fadeOut = Tween.to(alpha, 0, 2.5f).target(1.0f).ease(TweenEquations.easeInQuint);
			this.fadeIn = null;
		}
		color = new Color(Color.WHITE);
	}

	/**
	 * <p>Creates a fade effect with the desired parameters.</p>
	 * 
	 * @param fadeIn True to create a fade FROM color, false for a fade TO color.
	 * @param color The color of the effect.
	 */
	public FadeEffectComponent(boolean fadeIn, Color color){
		this(fadeIn);
		this.color.set(color);
	}

	/**
	 * <p>Creates a fade out effect of the desired color.</p>
	 * 
	 * @param color The color of the effect.
	 */
	public FadeEffectComponent(Color color){
		this(false, color);
	}

	/**
	 * <p>Creates a white fade out effect.</p>
	 */
	public FadeEffectComponent(){
		this(false);
	}

	/**
	 * <p>The current transparency of the effect.</p>
	 * 
	 * @return The transparency.
	 */
	public float getFloatValue(){
		return alpha.floatValue();
	}

	/**
	 * <p>Interpolates the transparency of the effect by the given delta time in seconds.</p>
	 * 
	 * @param delta
	 */
	public void update(float delta){
		if(fadeIn != null)
			fadeIn.update(delta);

		if(fadeOut != null)
			fadeOut.update(delta);
	}

	/**
	 * <p>Initializes the effect.</p>
	 */
	public void startEffect(){
		if(fadeIn != null)
			fadeIn.start();

		if(fadeOut != null)
			fadeOut.start();
	}

	/**
	 * @return True if the effect has been initialized. False otherwise.
	 */
	public boolean isEffectStarted(){
		return fadeIn != null ? fadeIn.isStarted() : fadeOut.isStarted();
	}

	/**
	 * @return True if this effect is a fade in. False if it is a fade out.
	 */
	public boolean isEffectFadeIn(){
		return fadeIn != null;
	}

	/**
	 * @return True if the effect's interpolation is over. False otherwise.
	 */
	public boolean isEffectFinished(){
		return fadeIn != null ? fadeIn.isFinished() : fadeOut.isFinished();
	}
}
