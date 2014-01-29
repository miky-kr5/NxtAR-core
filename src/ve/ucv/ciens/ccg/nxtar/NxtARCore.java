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
import ve.ucv.ciens.ccg.nxtar.network.VideoFrameMonitor;
import ve.ucv.ciens.ccg.nxtar.network.VideoStreamingThread;
import ve.ucv.ciens.ccg.nxtar.utils.ProjectConstants;
import ve.ucv.ciens.ccg.nxtar.utils.Size;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.controllers.mappings.Ouya;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

public class NxtARCore implements ApplicationListener, NetworkConnectionListener, InputProcessor{
	private static final String TAG = "NXTAR_CORE_MAIN";
	private static final String CLASS_NAME = NxtARCore.class.getSimpleName();

	private float overscan;

	private OrthographicCamera camera;
	private OrthographicCamera pixelPerfectCamera;
	private SpriteBatch batch;
	private Texture texture;
	private Texture buttonTexture;
	private Sprite sprite;
	private Sprite motorA;
	private Sprite motorB;
	private Sprite motorC;
	private Sprite motorD;
	private Toaster toaster;
	private MulticastEnabler mcastEnabler;
	private BitmapFont font;
	private int connections;
	private float fontX;
	private float fontY;
	private Pixmap image;
	private Vector3 win2world;
	private Vector2 touchPointWorldCoords;
	private boolean[] motorButtonsTouched;
	private int[] motorButtonsPointers;

	private VideoFrameMonitor frameMonitor;
	private ServiceDiscoveryThread serviceDiscoveryThread;
	private VideoStreamingThread videoThread;
	private RobotControlThread robotThread;

	public NxtARCore(Application concreteApp){
		super();
		connections = 0;
		try{
			this.toaster = (Toaster)concreteApp;
			this.mcastEnabler = (MulticastEnabler)concreteApp;
		}catch(ClassCastException cc){
			Gdx.app.debug(TAG, CLASS_NAME + ".Main() :: concreteApp does not implement any of the required interfaces.");
			Gdx.app.exit();
		}
	}

	@Override
	public void create(){
		image = null;
		win2world = new Vector3(0.0f, 0.0f, 0.0f);
		touchPointWorldCoords = new Vector2();
		motorButtonsTouched = new boolean[4];
		motorButtonsTouched[0] = false;
		motorButtonsTouched[1] = false;
		motorButtonsTouched[2] = false;
		motorButtonsTouched[3] = false;

		motorButtonsPointers = new int[4];
		motorButtonsPointers[0] = -1;
		motorButtonsPointers[1] = -1;
		motorButtonsPointers[2] = -1;
		motorButtonsPointers[3] = -1;

		Gdx.input.setInputProcessor(this);

		font = new BitmapFont();

		font.setColor(1.0f, 1.0f, 0.0f, 1.0f);
		if(!Ouya.runningOnOuya){
			font.setScale(1.0f);
		}else{
			font.setScale(2.5f);
		}

		Gdx.app.setLogLevel(Application.LOG_INFO);
		//Gdx.app.setLogLevel(Application.LOG_NONE);

		pixelPerfectCamera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		camera = new OrthographicCamera(1.0f, Gdx.graphics.getHeight() / Gdx.graphics.getWidth());
		batch = new SpriteBatch();

		overscan = Ouya.runningOnOuya ? 0.9f : 1.0f;
		fontX = -((Gdx.graphics.getWidth() * overscan) / 2) + 10;
		fontY = ((Gdx.graphics.getHeight() * overscan) / 2) - 10;
		if(!Ouya.runningOnOuya) setUpButtons();

		Gdx.app.debug(TAG, CLASS_NAME + ".create() :: Creating network threads");
		frameMonitor = VideoFrameMonitor.getInstance();
		serviceDiscoveryThread = ServiceDiscoveryThread.getInstance();
		videoThread = VideoStreamingThread.getInstance()/*.setToaster(toaster)*/;
		//robotThread = RobotControlThread.getInstance().setToaster(toaster);

		mcastEnabler.enableMulticast();

		serviceDiscoveryThread.start();
		videoThread.start();
		videoThread.startStreaming();
		//robotThread.start();
	}

	@Override
	public void dispose() {
		batch.dispose();
		if(texture != null)
			texture.dispose();
		if(buttonTexture != null)
			buttonTexture.dispose();
		font.dispose();
		image.dispose();
		videoThread.finish();
	}

	@Override
	public void render(){
		Pixmap temp;
		byte[] frame;
		Size dimensions = null;

		Gdx.gl.glClearColor(1, 1, 1, 1);
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);

		frame = frameMonitor.getCurrentFrame();
		if(frame != null){
			dimensions = frameMonitor.getFrameDimensions();
			temp = new Pixmap(frame, 0, dimensions.getWidth() * dimensions.getHeight());
			if(image == null){
				try{
					image = new Pixmap(getOptimalTextureSize(dimensions.getWidth()), getOptimalTextureSize(dimensions.getHeight()), temp.getFormat());
				}catch(ImageTooBigException e){
					toaster.showLongToast("Cannot display received frame.\n" + e.getMessage());
					Gdx.app.exit();
					return;
				}
			}
			image.drawPixmap(temp, 0, 0);
			temp.dispose();
			texture = new Texture(image);
			texture.setFilter(TextureFilter.Linear, TextureFilter.Linear);

			TextureRegion region = new TextureRegion(texture, 0, 0, dimensions.getWidth(), dimensions.getHeight());

			sprite = new Sprite(region);
			sprite.setOrigin(sprite.getWidth() / 2, sprite.getHeight() / 2);
			if(!Ouya.runningOnOuya){
				sprite.setSize(1.0f, sprite.getHeight() / sprite.getWidth() );
				sprite.rotate90(true);
				sprite.translate(-sprite.getWidth() / 2, 0.5f - sprite.getHeight());
			}else{
				float xSize = Gdx.graphics.getHeight() * (dimensions.getWidth() / dimensions.getHeight());
				sprite.setSize(xSize * overscan, Gdx.graphics.getHeight() * overscan);
				sprite.rotate90(true);
				sprite.translate(-sprite.getWidth() / 2, -sprite.getHeight() / 2);
			}

			if(!Ouya.runningOnOuya){
				batch.setProjectionMatrix(camera.combined);
			}else{
				batch.setProjectionMatrix(pixelPerfectCamera.combined);
			}
			batch.begin();{
				sprite.draw(batch);
			}batch.end();

			texture.dispose();
		}

		batch.setProjectionMatrix(pixelPerfectCamera.combined);
		batch.begin();{
			if(!Ouya.runningOnOuya){
				motorA.draw(batch);
				motorB.draw(batch);
				motorC.draw(batch);
				motorD.draw(batch);
			}
			font.draw(batch, String.format("Render FPS: %d", Gdx.graphics.getFramesPerSecond()), fontX, fontY);
			font.draw(batch, String.format("Network FPS: %d", videoThread.getFps()), fontX, fontY - font.getCapHeight() - 5);
			font.draw(batch, String.format("Lost Network FPS: %d", videoThread.getLostFrames()), fontX, fontY - (2 * font.getCapHeight()) - 10);
			if(dimensions != null)
				font.draw(batch, String.format("Frame size: (%d, %d)", dimensions.getWidth(), dimensions.getHeight()), fontX, fontY - (3 * font.getCapHeight()) - 15);
		}batch.end();
	}

	@Override
	public void resize(int width, int height){
	}

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
	public synchronized void networkStreamConnected(String streamName){
		if(streamName.compareTo(VideoStreamingThread.THREAD_NAME) == 0 || streamName.compareTo(RobotControlThread.THREAD_NAME) == 0)
			connections += 1;
		if(connections >= 2){
			Gdx.app.debug(TAG, CLASS_NAME + ".networkStreamConnected() :: Stopping service broadcast.");
			serviceDiscoveryThread.finish();
			mcastEnabler.disableMulticast();
		}
	}

	private int getOptimalTextureSize(int imageSideLength) throws ImageTooBigException{
		for(int po2: ProjectConstants.POWERS_OF_2){
			if(imageSideLength < po2) return po2;
		}
		throw new ImageTooBigException("No valid texture size found. Image too large.");
	}

	private void setUpButtons(){
		buttonTexture = new Texture(Gdx.files.internal("data/gfx/gui/PBCrichton_Flat_Button.png"));
		buttonTexture.setFilter(TextureFilter.Linear, TextureFilter.Linear);

		TextureRegion region = new TextureRegion(buttonTexture, 0, 0, buttonTexture.getWidth(), buttonTexture.getHeight());

		motorA = new Sprite(region);
		motorA.setSize(motorA.getWidth() * 0.7f, motorA.getHeight() * 0.7f);

		motorB = new Sprite(region);
		motorB.setSize(motorB.getWidth() * 0.7f, motorB.getHeight() * 0.7f);

		motorC = new Sprite(region);
		motorC.setSize(motorC.getWidth() * 0.7f, motorC.getHeight() * 0.7f);

		motorD = new Sprite(region);
		motorD.setSize(motorD.getWidth() * 0.7f, motorD.getHeight() * 0.7f);

		motorA.setPosition(-(Gdx.graphics.getWidth() / 2) + 10, -(Gdx.graphics.getHeight() / 2) + motorB.getHeight() + 20);
		motorB.setPosition(-(Gdx.graphics.getWidth() / 2) + 20 + (motorA.getWidth() / 2), -(Gdx.graphics.getHeight() / 2) + 10);
		motorC.setPosition((Gdx.graphics.getWidth() / 2) - (1.5f * (motorD.getWidth())) - 20, -(Gdx.graphics.getHeight() / 2) + 10);
		motorD.setPosition((Gdx.graphics.getWidth() / 2) - motorD.getWidth() - 10, -(Gdx.graphics.getHeight() / 2) + 20 + motorC.getHeight());
	}

	private class ImageTooBigException extends Exception{
		private static final long serialVersionUID = 9989L;

		public ImageTooBigException(String msg){
			super(msg);
		}
	}

	@Override
	public boolean keyDown(int keycode) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean keyUp(int keycode) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean keyTyped(char character) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		if(!Ouya.runningOnOuya){
			win2world.set(screenX, screenY, 0.0f);
			camera.unproject(win2world);
			touchPointWorldCoords.set(win2world.x * Gdx.graphics.getWidth(), win2world.y * Gdx.graphics.getHeight());

			Gdx.app.log(TAG, CLASS_NAME + String.format(".touchDown(%d, %d, %d, %d)", screenX, screenY, pointer, button));
			Gdx.app.log(TAG, CLASS_NAME + String.format(".touchDown() :: Unprojected touch point: (%f, %f)", touchPointWorldCoords.x, touchPointWorldCoords.y));

			if(motorA.getBoundingRectangle().contains(touchPointWorldCoords)){
				Gdx.app.log(TAG, CLASS_NAME + ".touchDown() :: Motor A button pressed");
				motorButtonsTouched[0] = true;
				motorButtonsPointers[0] = pointer;
			}else if(motorB.getBoundingRectangle().contains(touchPointWorldCoords)){
				Gdx.app.log(TAG, CLASS_NAME + ".touchDown() :: Motor B button pressed");
				motorButtonsTouched[1] = true;
				motorButtonsPointers[1] = pointer;
			}else if(motorC.getBoundingRectangle().contains(touchPointWorldCoords)){
				Gdx.app.log(TAG, CLASS_NAME + ".touchDown() :: Motor C button pressed");
				motorButtonsTouched[2] = true;
				motorButtonsPointers[2] = pointer;
			}else if(motorD.getBoundingRectangle().contains(touchPointWorldCoords)){
				Gdx.app.log(TAG, CLASS_NAME + ".touchDown() :: Motor D button pressed");
				motorButtonsTouched[3] = true;
				motorButtonsPointers[3] = pointer;
			}
		}
		return true;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		if(!Ouya.runningOnOuya){
			win2world.set(screenX, screenY, 0.0f);
			camera.unproject(win2world);
			touchPointWorldCoords.set(win2world.x * Gdx.graphics.getWidth(), win2world.y * Gdx.graphics.getHeight());

			Gdx.app.log(TAG, CLASS_NAME + String.format(".touchUp(%d, %d, %d, %d)", screenX, screenY, pointer, button));
			Gdx.app.log(TAG, CLASS_NAME + String.format(".touchUp() :: Unprojected touch point: (%f, %f)", touchPointWorldCoords.x, touchPointWorldCoords.y));

			if(motorA.getBoundingRectangle().contains(touchPointWorldCoords)){
				Gdx.app.log(TAG, CLASS_NAME + ".touchUp() :: Motor A button released");
				motorButtonsPointers[0] = -1;
				motorButtonsTouched[0] = false;
			}else if(motorB.getBoundingRectangle().contains(touchPointWorldCoords)){
				Gdx.app.log(TAG, CLASS_NAME + ".touchUp() :: Motor B button released");
				motorButtonsPointers[1] = -1;
				motorButtonsTouched[1] = false;
			}else if(motorC.getBoundingRectangle().contains(touchPointWorldCoords)){
				Gdx.app.log(TAG, CLASS_NAME + ".touchUp() :: Motor C button released");
				motorButtonsPointers[2] = -1;
				motorButtonsTouched[2] = false;
			}else if(motorD.getBoundingRectangle().contains(touchPointWorldCoords)){
				Gdx.app.log(TAG, CLASS_NAME + ".touchUp() :: Motor D button released");
				motorButtonsPointers[3] = -1;
				motorButtonsTouched[3] = false;
			}
		}
		return true;
	}

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		if(!Ouya.runningOnOuya){
			win2world.set(screenX, screenY, 0.0f);
			camera.unproject(win2world);
			touchPointWorldCoords.set(win2world.x * Gdx.graphics.getWidth(), win2world.y * Gdx.graphics.getHeight());

			if(pointer == motorButtonsPointers[0] && !motorA.getBoundingRectangle().contains(touchPointWorldCoords)){
				Gdx.app.log(TAG, CLASS_NAME + ".touchDragged() :: Motor A button released");
				motorButtonsPointers[0] = -1;
				motorButtonsTouched[0] = false;
			}else if(pointer == motorButtonsPointers[1] && !motorB.getBoundingRectangle().contains(touchPointWorldCoords)){
				Gdx.app.log(TAG, CLASS_NAME + ".touchDragged() :: Motor B button released");
				motorButtonsPointers[1] = -1;
				motorButtonsTouched[1] = false;
			}else if(pointer == motorButtonsPointers[2] && !motorC.getBoundingRectangle().contains(touchPointWorldCoords)){
				Gdx.app.log(TAG, CLASS_NAME + ".touchDragged() :: Motor C button released");
				motorButtonsPointers[2] = -1;
				motorButtonsTouched[2] = false;
			}else if(pointer == motorButtonsPointers[3] && !motorD.getBoundingRectangle().contains(touchPointWorldCoords)){
				Gdx.app.log(TAG, CLASS_NAME + ".touchDragged() :: Motor D button released");
				motorButtonsPointers[3] = -1;
				motorButtonsTouched[3] = false;
			}
		}
		return true;
	}

	@Override
	public boolean mouseMoved(int screenX, int screenY) {
		Gdx.app.error(TAG, "MOUSE MOVED!");
		return false;
	}

	@Override
	public boolean scrolled(int amount) {
		// TODO Auto-generated method stub
		return false;
	};
}
