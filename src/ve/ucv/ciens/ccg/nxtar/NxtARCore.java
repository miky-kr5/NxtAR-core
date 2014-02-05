/*
 * Copyright (C) 2013 Miguel Angel Astor Romero
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
package ve.ucv.ciens.ccg.nxtar;

import ve.ucv.ciens.ccg.nxtar.interfaces.MulticastEnabler;
import ve.ucv.ciens.ccg.nxtar.interfaces.NetworkConnectionListener;
import ve.ucv.ciens.ccg.nxtar.interfaces.Toaster;
import ve.ucv.ciens.ccg.nxtar.network.RobotControlThread;
import ve.ucv.ciens.ccg.nxtar.network.ServiceDiscoveryThread;
import ve.ucv.ciens.ccg.nxtar.network.VideoStreamingThread;
import ve.ucv.ciens.ccg.nxtar.states.BaseState;
import ve.ucv.ciens.ccg.nxtar.states.InGameState;
import ve.ucv.ciens.ccg.nxtar.states.OuyaMainMenuState;
import ve.ucv.ciens.ccg.nxtar.states.PauseState;
import ve.ucv.ciens.ccg.nxtar.states.TabletMainMenuState;
import ve.ucv.ciens.ccg.nxtar.utils.ProjectConstants;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.controllers.Controllers;
import com.badlogic.gdx.controllers.mappings.Ouya;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class NxtARCore extends Game implements NetworkConnectionListener{
	private static final String TAG = "NXTAR_CORE_MAIN";
	private static final String CLASS_NAME = NxtARCore.class.getSimpleName();

	// Game state management fields.
	public enum game_states_t {
		MAIN_MENU(0), IN_GAME(1), PAUSED(2);

		private int value;

		private game_states_t(int value){
			this.value = value;
		}

		public int getValue(){
			return this.value;
		}
	};
	public game_states_t nextState;
	private game_states_t currState;

	// Screens.
	private BaseState[] states;

	// Assorted fields.
	public SpriteBatch batch;
	public Toaster toaster;

	// Networking related fields.
	private int connections;
	private MulticastEnabler mcastEnabler;
	private ServiceDiscoveryThread serviceDiscoveryThread;
	private VideoStreamingThread videoThread;
	private RobotControlThread robotThread;

	// Overlay font.
	private float fontX;
	private float fontY;
	private BitmapFont font;

	public NxtARCore(Application concreteApp){
		super();
		connections = 0;
		try{
			this.toaster = (Toaster)concreteApp;
			this.mcastEnabler = (MulticastEnabler)concreteApp;
		}catch(ClassCastException cc){
			Gdx.app.debug(TAG, CLASS_NAME + ".Main() :: concreteApp does not implement some of the required interfaces.");
			Gdx.app.exit();
		}
	}

	@Override
	public void create(){
		// Create the state objects.
		states = new BaseState[3];
		if(Ouya.runningOnOuya)states[0] = new OuyaMainMenuState(this);
		else states[1] = new TabletMainMenuState(this);
		states[1] = new InGameState(this);
		states[2] = new PauseState(this);

		// Set up fields.
		batch = new SpriteBatch();

		if(ProjectConstants.DEBUG){
			// Set up the overlay font.
			fontX = -((Gdx.graphics.getWidth() * ProjectConstants.OVERSCAN) / 2) + 10;
			fontY = ((Gdx.graphics.getHeight() * ProjectConstants.OVERSCAN) / 2) - 10;

			font = new BitmapFont();
			font.setColor(1.0f, 1.0f, 0.0f, 1.0f);
			if(!Ouya.runningOnOuya){
				font.setScale(1.0f);
			}else{
				font.setScale(2.5f);
			}
		}

		// Start networking.
		mcastEnabler.enableMulticast();

		Gdx.app.debug(TAG, CLASS_NAME + ".create() :: Creating network threads");
		serviceDiscoveryThread = ServiceDiscoveryThread.getInstance();
		videoThread = VideoStreamingThread.getInstance()/*.setToaster(toaster)*/;
		//robotThread = RobotControlThread.getInstance().setToaster(toaster);

		serviceDiscoveryThread.start();
		videoThread.start();
		videoThread.startStreaming();
		//robotThread.start();

		// Set the current and next states.
		currState = game_states_t.MAIN_MENU;
		nextState = null;
		this.setScreen(states[currState.getValue()]);

		// Set initial input handlers.
		Gdx.input.setInputProcessor(states[currState.getValue()]);
		Controllers.addListener(states[currState.getValue()]);

		// Anything else.
		Gdx.app.setLogLevel(Application.LOG_INFO);
		// Gdx.app.setLogLevel(Application.LOG_NONE);
	}

	@Override
	public void render(){
		super.render();

		// If the current state set a value for nextState then switch to that state.
		if(nextState != null){
			// Change the input processors.
			Gdx.input.setInputProcessor(states[nextState.getValue()]);
			Controllers.removeListener(states[currState.getValue()]);
			Controllers.addListener(states[nextState.getValue()]);

			// Swap the pointers and set the new screen.
			currState = nextState;
			nextState = null;
			setScreen(states[currState.getValue()]);
		}

		if(ProjectConstants.DEBUG){
			// Draw the FPS overlay.
			batch.begin();{
				font.draw(batch, String.format("Render FPS:        %d", Gdx.graphics.getFramesPerSecond()), fontX, fontY);
				font.draw(batch, String.format("Total  stream FPS: %d", videoThread.getFps()), fontX, fontY - font.getCapHeight() - 5);
				font.draw(batch, String.format("Lost   stream FPS: %d", videoThread.getLostFrames()), fontX, fontY - (2 * font.getCapHeight()) - 10);
			}batch.end();
		}
	}

	@Override
	public void resize(int width, int height){ }

	@Override
	public void pause(){
		if(videoThread != null)
			videoThread.pause();
	}

	@Override
	public void resume(){
		if(videoThread != null)
			videoThread.play();
	}

	@Override
	public void dispose(){
		// Finish network threads.
		videoThread.finish();

		// Dispose graphic objects.
		batch.dispose();
		if(ProjectConstants.DEBUG){
			font.dispose();
		}

		// Dispose screens.
		for(int i = 0; i < states.length; i++){
			states[i].dispose();
		}
	}

	@Override
	public synchronized void networkStreamConnected(String streamName){
		if(streamName.compareTo(VideoStreamingThread.THREAD_NAME) == 0 || streamName.compareTo(RobotControlThread.THREAD_NAME) == 0)
			connections += 1;
		if(connections >= 2){
			Gdx.app.debug(TAG, CLASS_NAME + ".networkStreamConnected() :: Stopping service broadcast.");
			serviceDiscoveryThread.finish();
			mcastEnabler.disableMulticast();
		}
	}
}
