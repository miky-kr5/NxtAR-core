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

public class FadeEffectComponent extends Component{
	private MutableFloat alpha;
	private Tween fadeIn;
	private Tween fadeOut;

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
	}

	public float getFloatValue(){
		return alpha.floatValue();
	}

	public void update(float delta){
		if(fadeIn != null)
			fadeIn.update(delta);

		if(fadeOut != null)
			fadeOut.update(delta);
	}

	public void startEffect(){
		if(fadeIn != null)
			fadeIn.start();

		if(fadeOut != null)
			fadeOut.start();
	}

	public boolean isEffectStarted(){
		return fadeIn != null ? fadeIn.isStarted() : fadeOut.isStarted();
	}

	public boolean isEffectFadeIn(){
		return fadeIn != null;
	}

	public boolean isEffectFinished(){
		return fadeIn != null ? fadeIn.isFinished() : fadeOut.isFinished();
	}
}
