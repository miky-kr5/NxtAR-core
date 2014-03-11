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

import ve.ucv.ciens.ccg.nxtar.interfaces.CVProcessor;
import ve.ucv.ciens.ccg.nxtar.interfaces.MulticastEnabler;
import ve.ucv.ciens.ccg.nxtar.interfaces.NetworkConnectionListener;
import ve.ucv.ciens.ccg.nxtar.interfaces.Toaster;
import ve.ucv.ciens.ccg.nxtar.network.RobotControlThread;
import ve.ucv.ciens.ccg.nxtar.network.ServiceDiscoveryThread;
import ve.ucv.ciens.ccg.nxtar.network.VideoStreamingThread;
import ve.ucv.ciens.ccg.nxtar.states.BaseState;
import ve.ucv.ciens.ccg.nxtar.states.InGameState;
import ve.ucv.ciens.ccg.nxtar.states.MainMenuStateBase;
import ve.ucv.ciens.ccg.nxtar.states.OuyaMainMenuState;
import ve.ucv.ciens.ccg.nxtar.states.PauseState;
import ve.ucv.ciens.ccg.nxtar.states.TabletMainMenuState;
import ve.ucv.ciens.ccg.nxtar.utils.ProjectConstants;
import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenEquations;
import aurelienribon.tweenengine.primitives.MutableFloat;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.controllers.Controllers;
import com.badlogic.gdx.controllers.mappings.Ouya;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

/**
 * <p>Core of the application.</p>
 * 
 * <p>This class has three basic resposibilities:</p>
 * <ul>
 *     <li> Handling the main game loop.</li>
 *     <li> Starting and destroying the networking threads.</li>
 *     <li> Rendering debug information.</li>
 * </ul>
 * @author Miguel Angel Astor Romero
 */
public class NxtARCore extends Game implements NetworkConnectionListener{
	private static final String TAG = "NXTAR_CORE_MAIN";
	private static final String CLASS_NAME = NxtARCore.class.getSimpleName();

	/**
	 * Valid game states.
	 */
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
	/**
	 * The current application state.
	 */
	private game_states_t currState;
	/**
	 * <p>The state to change to.</p>
	 * <p> Usually null. A state change is scheduled by setting this field to a {@link game_states_t} value.</p>
	 */
	public game_states_t nextState;

	// Screens.
	private BaseState[] states;

	// Assorted fields.
	public SpriteBatch batch;
	public CVProcessor cvProc;
	private Toaster toaster;

	// Networking related fields.
	private int connections;
	private MulticastEnabler mcastEnabler;
	private ServiceDiscoveryThread serviceDiscoveryThread;
	private VideoStreamingThread videoThread;
	private RobotControlThread robotThread;

	// Overlays.
	private OrthographicCamera pixelPerfectCamera;
	private float fontX;
	private float fontY;
	private BitmapFont font;

	private Texture fadeTexture;
	private MutableFloat alpha;
	private Tween fadeOut;
	private Tween fadeIn;
	private boolean fading;

	/**
	 * <p>Set up the basic application fields.</p>
	 */
	public NxtARCore(Application concreteApp){
		super();
		connections = 0;
		try{
			this.toaster = (Toaster)concreteApp;
		}catch(ClassCastException cc){
			Gdx.app.debug(TAG, CLASS_NAME + ".Main() :: concreteApp does not implement the Toaster interface. Toasting disabled.");
			this.toaster = null;
		}

		try{
			this.mcastEnabler = (MulticastEnabler)concreteApp;
		}catch(ClassCastException cc){
			Gdx.app.error(TAG, CLASS_NAME + ".Main() :: concreteApp does not implement MulticastEnabler. Quitting.");
			Gdx.app.exit();
		}

		try{
			this.cvProc = (CVProcessor)concreteApp;
		}catch(ClassCastException cc){
			Gdx.app.error(TAG, CLASS_NAME + ".Main() :: concreteApp does not implement the CVProcessor interface. Quitting.");
			Gdx.app.exit();
		}
	}

	public void create(){
		// Create the state objects.
		states = new BaseState[3];
		if(Ouya.runningOnOuya)states[game_states_t.MAIN_MENU.getValue()] = new OuyaMainMenuState(this);
		else states[game_states_t.MAIN_MENU.getValue()] = new TabletMainMenuState(this);
		states[game_states_t.IN_GAME.getValue()] = new InGameState(this);
		states[game_states_t.PAUSED.getValue()] = new PauseState(this);

		for(BaseState state : states){
			Controllers.addListener(state);
		}

		// Set up fields.
		batch = new SpriteBatch();

		pixelPerfectCamera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

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
		videoThread = VideoStreamingThread.getInstance();
		robotThread = RobotControlThread.getInstance();

		serviceDiscoveryThread.start();

		videoThread.start();
		videoThread.startStreaming();
		videoThread.addNetworkConnectionListener(this);

		robotThread.addNetworkConnectionListener(this);
		robotThread.start();

		// Set the current and next states.
		currState = game_states_t.MAIN_MENU;
		nextState = null;
		this.setScreen(states[currState.getValue()]);
		states[currState.getValue()].onStateSet();

		// Prepare the fadeToBlack sprite;
		Pixmap pixmap = new Pixmap(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), Format.RGBA4444);
		pixmap.setColor(0, 0, 0, 1);
		pixmap.fill();
		fadeTexture = new Texture(pixmap);
		pixmap.dispose();

		alpha = new MutableFloat(0.0f);
		fadeOut = Tween.to(alpha, 0, 0.5f).target(1.0f).ease(TweenEquations.easeInQuint);
		fadeIn = Tween.to(alpha, 0, 0.5f).target(0.0f).ease(TweenEquations.easeInQuint);

		fading = false;

		// Set initial input handlers.
		Gdx.input.setInputProcessor(states[currState.getValue()]);
		Controllers.addListener(states[currState.getValue()]);

		// Anything else.
		Gdx.app.setLogLevel(Application.LOG_INFO);
		//Gdx.app.setLogLevel(Application.LOG_DEBUG);
		//Gdx.app.setLogLevel(Application.LOG_NONE);
	}

	public void render(){
		super.render();

		// If the current state set a value for nextState then switch to that state.
		if(nextState != null){
			states[currState.getValue()].onStateUnset();

			if(!fadeOut.isStarted()){
				Gdx.app.log(TAG, CLASS_NAME + ".onRender() :: Starting fade out.");
				fadeOut.start();
				fading = true;
			}else{
				Gdx.app.log(TAG, CLASS_NAME + ".onRender() :: Updating fade out.");
				fadeOut.update(Gdx.graphics.getDeltaTime());

				if(fadeOut.isFinished()){
					currState = nextState;
					nextState = null;

					states[currState.getValue()].onStateSet();

					setScreen(states[currState.getValue()]);

					Gdx.app.log(TAG, CLASS_NAME + ".onRender() :: Freeing fade out.");
					fadeOut.free();
					fadeOut = Tween.to(alpha, 0, 0.5f).target(1.0f).ease(TweenEquations.easeInQuint);
					fadeIn.start();
				}
			}
		}

		if(fadeIn.isStarted()){
			if(!fadeIn.isFinished()){
				fadeIn.update(Gdx.graphics.getDeltaTime());
			}else{
				fading = false;
				fadeIn.free();
				fadeIn = Tween.to(alpha, 0, 0.5f).target(0.0f).ease(TweenEquations.easeInQuint);
			}
		}

		if(fading){
			batch.setProjectionMatrix(pixelPerfectCamera.combined);
			batch.begin();{
				batch.setColor(1, 1, 1, alpha.floatValue());
				batch.draw(fadeTexture, -(Gdx.graphics.getWidth() / 2), -(Gdx.graphics.getHeight() / 2));
				batch.setColor(1, 1, 1, 1);
			}batch.end();
		}

		if(ProjectConstants.DEBUG){
			batch.setProjectionMatrix(pixelPerfectCamera.combined);
			batch.begin();{
				// Draw the FPS overlay.
				font.draw(batch, String.format("Render FPS:        %d", Gdx.graphics.getFramesPerSecond()), fontX, fontY);
				font.draw(batch, String.format("Total  stream FPS: %d", videoThread.getFps()), fontX, fontY - font.getCapHeight() - 5);
				font.draw(batch, String.format("Lost   stream FPS: %d", videoThread.getLostFrames()), fontX, fontY - (2 * font.getCapHeight()) - 10);
			}batch.end();
		}
	}

	public void pause(){
		if(videoThread != null)
			videoThread.pause();
	}

	public void resume(){
		if(videoThread != null)
			videoThread.play();
	}

	public void dispose(){
		// Finish network threads.
		videoThread.finish();
		robotThread.finish();

		// Dispose graphic objects.
		fadeTexture.dispose();
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
		//if(streamName.equals(VideoStreamingThread.THREAD_NAME) || streamName.equals(RobotControlThread.THREAD_NAME))
		Gdx.app.log(TAG, CLASS_NAME + ".networkStreamConnected() :: Stream " + streamName + " connected.");
		connections += 1;
		if(connections >= 2){
			Gdx.app.debug(TAG, CLASS_NAME + ".networkStreamConnected() :: Stopping service broadcast.");
			serviceDiscoveryThread.finish();
			mcastEnabler.disableMulticast();
			toaster.showShortToast("Client connected");
			((MainMenuStateBase)states[game_states_t.MAIN_MENU.getValue()]).onClientConnected();
		}
	}

	public void toast(String msg, boolean longToast){
		if(toaster != null){
			if(longToast) toaster.showLongToast(msg);
			else toaster.showShortToast(msg);
		}
	}
}
