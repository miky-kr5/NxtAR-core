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
package ve.ucv.ciens.ccg.nxtar.states;

import ve.ucv.ciens.ccg.nxtar.NxtARCore;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;

public class TabletMainMenuState extends MainMenuStateBase{

	public TabletMainMenuState(final NxtARCore core){
		super();

		this.core = core;

		startButton.setPosition(-(startButton.getWidth() / 2), -(startButton.getHeight() / 2));
		startButtonBBox.setPosition(startButton.getX(), startButton.getY());

		calibrationButton.setPosition(-(calibrationButton.getWidth() / 2), (startButton.getY() + startButton.getHeight()) + 10);
		calibrationButtonBBox.setPosition(calibrationButton.getX(), calibrationButton.getY());

		float ledYPos = (-(Gdx.graphics.getHeight() / 2) * 0.5f) + (calibrationButton.getY() * 0.5f);
		clientConnectedLedOn.setSize(clientConnectedLedOn.getWidth() * 0.5f, clientConnectedLedOn.getHeight() * 0.5f);
		clientConnectedLedOn.setPosition(-(clientConnectedLedOn.getWidth() / 2), ledYPos);

		clientConnectedLedOff.setSize(clientConnectedLedOff.getWidth() * 0.5f, clientConnectedLedOff.getHeight() * 0.5f);
		clientConnectedLedOff.setPosition(-(clientConnectedLedOff.getWidth() / 2), ledYPos);

		// TODO: Set calibration led attributes.
	}

	@Override
	public void render(float delta){
		Gdx.gl.glClearColor(1, 1, 1, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		core.batch.setProjectionMatrix(pixelPerfectCamera.combined);
		core.batch.begin();{

			core.batch.disableBlending();
			drawBackground(core.batch);
			core.batch.enableBlending();

			if(clientConnected){
				clientConnectedLedOn.draw(core.batch);
			}else{
				clientConnectedLedOff.draw(core.batch);
			}

			// TODO: Render calibration led.

			startButton.draw(core.batch, 1.0f);
			calibrationButton.draw(core.batch, 1.0f);

		}core.batch.end();
	}
}
