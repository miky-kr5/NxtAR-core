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
import ve.ucv.ciens.ccg.nxtar.utils.Utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;

public class TabletMainMenuState extends MainMenuStateBase{
	private float ledYPos;

	public TabletMainMenuState(final NxtARCore core){
		super();

		this.core = core;

		// Set buttons.
		startButton.setPosition(-(startButton.getWidth() / 2), -(startButton.getHeight() / 2));
		startButtonBBox.setPosition(startButton.getX(), startButton.getY());
		calibrationButton.setPosition(-(calibrationButton.getWidth() / 2), (startButton.getY() + startButton.getHeight()) + 10);
		calibrationButtonBBox.setPosition(calibrationButton.getX(), calibrationButton.getY());

		// Set leds.
		ledYPos = (-(Utils.getScreenHeight() / 2) * 0.5f) + (calibrationButton.getY() * 0.5f);
		cameraCalibratedLedOn.setSize(cameraCalibratedLedOn.getWidth() * 0.5f, cameraCalibratedLedOn.getHeight() * 0.5f);
		cameraCalibratedLedOn.setPosition(-cameraCalibratedLedOn.getWidth() - 5, ledYPos);
		cameraCalibratedLedOff.setSize(cameraCalibratedLedOff.getWidth() * 0.5f, cameraCalibratedLedOff.getHeight() * 0.5f);
		cameraCalibratedLedOff.setPosition(-cameraCalibratedLedOff.getWidth() - 5, ledYPos);
		assetsLoadedLedOn.setSize(assetsLoadedLedOn.getWidth() * 0.5f, assetsLoadedLedOn.getHeight() * 0.5f);
		assetsLoadedLedOn.setPosition(5, ledYPos);
		assetsLoadedLedOff.setSize(assetsLoadedLedOff.getWidth() * 0.5f, assetsLoadedLedOff.getHeight() * 0.5f);
		assetsLoadedLedOff.setPosition(5, ledYPos);
	}

	@Override
	public void render(float delta){
		Gdx.gl.glClearColor(1, 1, 1, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		core.batch.setProjectionMatrix(pixelPerfectCamera.combined);
		core.batch.begin();{

			// Render background.
			core.batch.disableBlending();
			drawBackground(core.batch);
			core.batch.enableBlending();

			// Render leds.
			if(cameraCalibrated) cameraCalibratedLedOn.draw(core.batch);
			else cameraCalibratedLedOff.draw(core.batch);
			if(assetsLoaded) assetsLoadedLedOn.draw(core.batch);
			else assetsLoadedLedOff.draw(core.batch);

			// Render buttons.
			startButton.draw(core.batch, 1.0f);
			calibrationButton.draw(core.batch, 1.0f);

		}core.batch.end();
	}
}
