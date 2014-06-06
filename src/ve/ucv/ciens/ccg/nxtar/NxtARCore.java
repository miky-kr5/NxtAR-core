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

import ve.ucv.ciens.ccg.nxtar.interfaces.ImageProcessor;
import ve.ucv.ciens.ccg.nxtar.interfaces.ApplicationEventsListener;
import ve.ucv.ciens.ccg.nxtar.interfaces.ActionResolver;
import ve.ucv.ciens.ccg.nxtar.network.RobotControlThread;
import ve.ucv.ciens.ccg.nxtar.network.SensorReportThread;
import ve.ucv.ciens.ccg.nxtar.network.ServiceDiscoveryThread;
import ve.ucv.ciens.ccg.nxtar.network.VideoStreamingThread;
import ve.ucv.ciens.ccg.nxtar.states.BaseState;
import ve.ucv.ciens.ccg.nxtar.states.CameraCalibrationState;
import ve.ucv.ciens.ccg.nxtar.states.InGameState;
import ve.ucv.ciens.ccg.nxtar.states.MainMenuStateBase;
import ve.ucv.ciens.ccg.nxtar.states.OuyaMainMenuState;
import ve.ucv.ciens.ccg.nxtar.states.PauseState;
import ve.ucv.ciens.ccg.nxtar.states.TabletMainMenuState;
import ve.ucv.ciens.ccg.nxtar.utils.GameSettings;
import ve.ucv.ciens.ccg.nxtar.utils.ProjectConstants;
import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenEquations;
import aurelienribon.tweenengine.primitives.MutableFloat;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.controllers.Controllers;
import com.badlogic.gdx.controllers.mappings.Ouya;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;

/**
 * <p>Core of the application.</p>
 * 
 * <p>This class has three basic resposibilities:</p>
 * <ul>
 *     <li> Handling the main game loop.</li>
 *     <li> Starting and destroying the networking threads.</li>
 *     <li> Rendering debug information.</li>
 * </ul>
 */
public class NxtARCore extends Game implements ApplicationEventsListener{
	/**
	 * Tag used for logging.
	 */
	private static final String TAG = "NXTAR_CORE_MAIN";

	/**
	 * Class name used for logging.
	 */
	private static final String CLASS_NAME = NxtARCore.class.getSimpleName();

	/**
	 * Valid game states.
	 */
	public enum game_states_t {
		MAIN_MENU(0), IN_GAME(1), PAUSED(2), CALIBRATION(3);

		private int value;

		private game_states_t(int value){
			this.value = value;
		}

		public int getValue(){
			return this.value;
		}

		public static int getNumStates(){
			return 4;
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
	/**
	 * <p>The application states.</p>
	 */
	private BaseState[] states;

	// Assorted fields.
	/**
	 * <p>Global sprite batch used for rendering trough the application.</p>
	 */
	public SpriteBatch batch;

	/**
	 * <p>The OpenCV wrapper.</p>
	 */
	public ImageProcessor cvProc;

	/**
	 * <p>Wrapper around the Operating System methods.</p>
	 */
	private ActionResolver actionResolver;

	// Networking related fields.
	/**
	 * <p>The number of connections successfully established with the NxtAR-cam application.</p>
	 */
	private int connections;

	/**
	 * <p>Worker thread used to broadcast this server over the network.</p>
	 */
	private ServiceDiscoveryThread serviceDiscoveryThread;

	/**
	 * <p>Worker thread used to receive video frames over UDP.<p>
	 */
	private VideoStreamingThread videoThread;

	/**
	 * <p>Worker thread used to send control commands to the NxtAR-cam application.
	 */
	private RobotControlThread robotThread;

	/**
	 * <p>Worker thread used to receive sensor data from the NxtAR-cam application.</p>
	 */
	private SensorReportThread sensorThread;

	// Overlays.
	/**
	 * <p>Camera used to render the debugging overlay.</p>
	 */
	private OrthographicCamera pixelPerfectCamera;

	/**
	 * <p>The base x coordinate for rendering the debugging overlay.</p>
	 */
	private float overlayX;

	/**
	 * <p>The base y coordinate for rendering the debugging overlay.</p>
	 */
	private float overlayY;

	/**
	 * <p>The font used to render the debugging overlay.</p>
	 */
	private BitmapFont font;

	// Fade in/out effect fields.
	/**
	 * <p>The graphic used to render the fading effect.</p>
	 */
	private Texture fadeTexture;

	/**
	 * <p>The interpolation value for the fading effect.</p>
	 */
	private MutableFloat alpha;

	/**
	 * <p>The fade out interpolator.</p>
	 */
	private Tween fadeOut;

	/**
	 * <p>The fade in interpolator.</p>
	 */
	private Tween fadeIn;

	/**
	 * <p>Flag used to indicate if a fading effect is active.</p>
	 */
	private boolean fading;

	/**
	 * <p>Set up the basic application fields.</p>
	 */
	public NxtARCore(Application concreteApp){
		super();

		connections = 0;

		// Check if the concrete application implements all required interfaces.
		try{
			this.actionResolver = (ActionResolver)concreteApp;
		}catch(ClassCastException cc){
			Gdx.app.debug(TAG, CLASS_NAME + ".Main() :: concreteApp does not implement the Toaster interface. Toasting disabled.");
			this.actionResolver = null;
		}

		try{
			this.cvProc = (ImageProcessor)concreteApp;
		}catch(ClassCastException cc){
			Gdx.app.error(TAG, CLASS_NAME + ".Main() :: concreteApp does not implement the CVProcessor interface. Quitting.");
			Gdx.app.exit();
		}
	}

	/*;;;;;;;;;;;;;;;;;;;;;;;;;;;
	  ; GAME SUPERCLASS METHODS ;
	  ;;;;;;;;;;;;;;;;;;;;;;;;;;;*/

	/**
	 * <p>Initialize the member fields and launch the networking threads. Also creates and
	 * sets the application states.</p>
	 */
	public void create(){
		GameSettings.initGameSettings(this);

		// Create the state objects.
		states = new BaseState[game_states_t.getNumStates()];
		if(Ouya.runningOnOuya)
			states[game_states_t.MAIN_MENU.getValue()] = new OuyaMainMenuState(this);
		else
			states[game_states_t.MAIN_MENU.getValue()] = new TabletMainMenuState(this);
		states[game_states_t.IN_GAME.getValue()] = new InGameState(this);
		states[game_states_t.PAUSED.getValue()] = new PauseState(this);
		states[game_states_t.CALIBRATION.getValue()] = new CameraCalibrationState(this);

		// Register controller listeners.
		for(BaseState state : states){
			Controllers.addListener(state);
		}

		// Set up rendering fields and settings.
		batch = new SpriteBatch();
		batch.enableBlending();
		batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

		pixelPerfectCamera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

		ShaderProgram.pedantic = false;

		// Set up the overlay font.
		if(ProjectConstants.DEBUG){
			overlayX = -((Gdx.graphics.getWidth() * ProjectConstants.OVERSCAN) / 2) + 10;
			overlayY = ((Gdx.graphics.getHeight() * ProjectConstants.OVERSCAN) / 2) - 10;

			font = new BitmapFont();
			font.setColor(1.0f, 1.0f, 0.0f, 1.0f);
			if(!Ouya.runningOnOuya){
				font.setScale(1.0f);
			}else{
				font.setScale(2.5f);
			}
		}

		// Start networking.
		actionResolver.enableMulticast();

		Gdx.app.debug(TAG, CLASS_NAME + ".create() :: Creating network threads");
		serviceDiscoveryThread = ServiceDiscoveryThread.getInstance();
		videoThread = VideoStreamingThread.getInstance();
		robotThread = RobotControlThread.getInstance();
		sensorThread = SensorReportThread.getInstance();

		// Launch networking threads.
		serviceDiscoveryThread.start();

		videoThread.start();
		videoThread.startStreaming();
		videoThread.addNetworkConnectionListener(this);

		robotThread.addNetworkConnectionListener(this);
		robotThread.start();

		sensorThread.addNetworkConnectionListener(this);
		sensorThread.start();

		// Set the current and next states.
		currState = game_states_t.MAIN_MENU;
		nextState = null;
		this.setScreen(states[currState.getValue()]);
		states[currState.getValue()].onStateSet();

		// Prepare the fading effect.
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

		// Set log level
		if(ProjectConstants.DEBUG){
			Gdx.app.setLogLevel(Application.LOG_DEBUG);
		}else{
			Gdx.app.setLogLevel(Application.LOG_NONE);
		}
	}

	/**
	 * <p>Update and render the currently enabled application state. This method
	 * also handles state switching, rendering state transitions and global overlays.</p>
	 */
	public void render(){
		super.render();

		// Load the assets.
		if(!GameSettings.getEntityCreator().areEntitiesCreated())
			GameSettings.getEntityCreator().updateAssetManager();

		// If the current state set a value for nextState then switch to that state.
		if(nextState != null){
			states[currState.getValue()].onStateUnset();

			if(!fadeOut.isStarted()){
				// Start the fade out effect.
				fadeOut.start();
				fading = true;
			}else{
				// Update the fade out effect.
				fadeOut.update(Gdx.graphics.getDeltaTime());

				// When the fade out effect finishes, change to the requested state
				// and launh the fade in effect.
				if(fadeOut.isFinished()){
					// Change to the requested state.
					currState = nextState;
					nextState = null;
					states[currState.getValue()].onStateSet();
					setScreen(states[currState.getValue()]);

					// Reset the fade out effect and launch the fade in.
					Gdx.app.log(TAG, CLASS_NAME + ".onRender() :: Freeing fade out.");
					fadeOut.free();
					fadeOut = Tween.to(alpha, 0, 0.5f).target(1.0f).ease(TweenEquations.easeInQuint);
					fadeIn.start();
				}
			}
		}

		// If there is a fade in effect in progress.
		if(fadeIn.isStarted()){
			if(!fadeIn.isFinished()){
				// Update it until finished.
				fadeIn.update(Gdx.graphics.getDeltaTime());
			}else{
				// Stop and reset it when done.
				fading = false;
				fadeIn.free();
				fadeIn = Tween.to(alpha, 0, 0.5f).target(0.0f).ease(TweenEquations.easeInQuint);
			}
		}

		// Render the fading sprite with alpha blending.
		if(fading){
			batch.setProjectionMatrix(pixelPerfectCamera.combined);
			batch.begin();{
				batch.setColor(1, 1, 1, alpha.floatValue());
				batch.draw(fadeTexture, -(Gdx.graphics.getWidth() / 2), -(Gdx.graphics.getHeight() / 2));
				batch.setColor(1, 1, 1, 1);
			}batch.end();
		}

		// Render the debug overlay.
		if(ProjectConstants.DEBUG){
			batch.setProjectionMatrix(pixelPerfectCamera.combined);
			batch.begin();{
				// Draw the FPS overlay.
				font.draw(batch, String.format("Render FPS:        %d", Gdx.graphics.getFramesPerSecond()), overlayX, overlayY);
				font.draw(batch, String.format("Total  stream FPS: %d", videoThread.getFps()), overlayX, overlayY - font.getCapHeight() - 5);
				font.draw(batch, String.format("Lost   stream FPS: %d", videoThread.getLostFrames()), overlayX, overlayY - (2 * font.getCapHeight()) - 10);
				font.draw(batch, String.format("Light sensor data: %d", sensorThread.getLightSensorReading()), overlayX, overlayY - (3 * font.getCapHeight()) - 15);
			}batch.end();
		}
	}

	/**
	 * <p>Pause a currently running thread. Pausing an already paused thread is a
	 * no op.</p>
	 */
	public void pause(){
		if(videoThread != null)
			videoThread.pause();
		// TODO: Ignore pausing paused threads.
		// TODO: Pause the other threads.
	}

	/**
	 * <p>Resume a currently paused thread. Resuming an already resumed thread is a
	 * no op.</p>
	 */
	public void resume(){
		if(videoThread != null)
			videoThread.play();
		// TODO: Ignore resuming resumed threads.
		// TODO: Resume the other threads.
	}

	/**
	 * <p>Clear graphic resources</p> 
	 */
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

		if(GameSettings.getEntityCreator() != null)
			GameSettings.getEntityCreator().dispose();

		// Dispose screens.
		for(int i = 0; i < states.length; i++){
			states[i].dispose();
		}
	}

	/*;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
	  ; APPLICATION EVENTS LISTENER INTERFACE METHODS ;
	  ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;*/

	@Override
	public synchronized void onNetworkStreamConnected(String streamName){
		Gdx.app.log(TAG, CLASS_NAME + ".networkStreamConnected() :: Stream " + streamName + " connected.");
		connections += 1;

		if(connections >= 3){
			Gdx.app.debug(TAG, CLASS_NAME + ".networkStreamConnected() :: Stopping service broadcast.");
			serviceDiscoveryThread.finish();
			if(actionResolver != null) actionResolver.disableMulticast();
			if(actionResolver != null) actionResolver.showShortToast("Client connected");
			((MainMenuStateBase)states[game_states_t.MAIN_MENU.getValue()]).onClientConnected();
		}
	}

	@Override
	public void onAssetsLoaded(){
		if(actionResolver != null) actionResolver.showShortToast("All assets sucessfully loaded.");
		((MainMenuStateBase)states[game_states_t.MAIN_MENU.getValue()]).onAssetsLoaded();
	}

	@Override
	public void onCameraCalibrated(){
		if(actionResolver != null) actionResolver.showShortToast("Camera successfully calibrated.");
		((MainMenuStateBase)states[game_states_t.MAIN_MENU.getValue()]).onCameraCalibrated();
	}

	/*;;;;;;;;;;;;;;;;;;
	  ; HELPER METHODS ;
	  ;;;;;;;;;;;;;;;;;;*/

	/**
	 * <p>Show a toast message on screen using the O.S. functionality
	 * provider.</p>
	 * @param msg The message to show.
	 * @param longToast True for a lasting toast. False for a short toast.
	 */
	public void toast(String msg, boolean longToast){
		if(actionResolver != null){
			if(longToast) actionResolver.showLongToast(msg);
			else actionResolver.showShortToast(msg);
		}
	}
}
