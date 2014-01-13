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
import com.badlogic.gdx.graphics.FPSLogger;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class NxtARCore implements ApplicationListener, NetworkConnectionListener {
	private static final String TAG = "NXTAR_CORE_MAIN";
	private static final String CLASS_NAME = NxtARCore.class.getSimpleName();

	private OrthographicCamera camera;
	private SpriteBatch batch;
	private Texture texture;
	private Sprite sprite;
	private Toaster toaster;
	private MulticastEnabler mcastEnabler;
	private FPSLogger fps;
	private BitmapFont font;
	private int connections;

	private VideoFrameMonitor frameMonitor;
	private ServiceDiscoveryThread udpThread;
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
			System.exit(ProjectConstants.EXIT_FAILURE);
		}
	}

	@Override
	public void create(){
		float w = Gdx.graphics.getWidth();
		float h = Gdx.graphics.getHeight();

		fps = new FPSLogger();
		font = new BitmapFont();

		//Gdx.app.setLogLevel(Application.LOG_NONE);

		camera = new OrthographicCamera(1, h/w);
		batch = new SpriteBatch();

		texture = new Texture(Gdx.files.internal("data/libgdx.png"));
		texture.setFilter(TextureFilter.Linear, TextureFilter.Linear);

		TextureRegion region = new TextureRegion(texture, 0, 0, 512, 275);

		sprite = new Sprite(region);
		sprite.setSize(0.9f, 0.9f * sprite.getHeight() / sprite.getWidth());
		sprite.setOrigin(sprite.getWidth()/2, sprite.getHeight()/2);
		sprite.setPosition(-sprite.getWidth()/2, -sprite.getHeight()/2);

		Gdx.app.debug(TAG, CLASS_NAME + ".create() :: Creating network threads");
		frameMonitor = VideoFrameMonitor.getInstance();
		mcastEnabler.enableMulticast();
		udpThread = ServiceDiscoveryThread.getInstance();
		videoThread = VideoStreamingThread.getInstance().setToaster(toaster);
		//robotThread = RobotControlThread.getInstance().setToaster(toaster);

		udpThread.start();
		videoThread.start();
		videoThread.startStreaming();
		//robotThread.start();
	}

	@Override
	public void dispose() {
		batch.dispose();
		texture.dispose();
		font.dispose();
	}

	@Override
	public void render(){
		Pixmap image;
		Pixmap temp;
		byte[] frame;
		Size dimensions;

		Gdx.gl.glClearColor(1, 1, 1, 1);
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);

		frame = frameMonitor.getCurrentFrame();
		if(frame != null){
			texture.dispose();

			dimensions = frameMonitor.getFrameDimensions();
			temp = new Pixmap(frame, 0, dimensions.getWidth() * dimensions.getHeight());
			image = new Pixmap(1024, 512, temp.getFormat());
			image.drawPixmap(temp, 0, 0);
			texture = new Texture(image);
			texture.setFilter(TextureFilter.Linear, TextureFilter.Linear);

			TextureRegion region = new TextureRegion(texture, 0, 0, dimensions.getWidth(), dimensions.getHeight());

			sprite = new Sprite(region);
			sprite.setSize(0.9f, 0.9f * sprite.getWidth() / sprite.getHeight());
			sprite.setOrigin(sprite.getWidth()/2, sprite.getHeight()/2);
			sprite.setPosition(-sprite.getWidth()/2, -sprite.getHeight()/2);
			sprite.rotate90(true);

			batch.setProjectionMatrix(camera.combined);
			batch.begin();{
				sprite.draw(batch);
				font.setColor(0.0f, 0.0f, 0.0f, 1.0f);
				font.setScale(100.0f);
				font.draw(batch, String.format("Render FPS: %d", Gdx.graphics.getFramesPerSecond()), 10, 300);
			}batch.end();

			Gdx.app.log("Main", String.format("Network FPS: %d", videoThread.getFps()));
			Gdx.app.log("Main", String.format("Render  FPS: %d", Gdx.graphics.getFramesPerSecond()));

			texture.dispose();
			temp.dispose();
			image.dispose();

			//fps.log();
		}
	}

	@Override
	public void resize(int width, int height){
	}

	@Override
	public void pause(){
	}

	@Override
	public void resume(){
	}

	@Override
	public synchronized void networkStreamConnected(String streamName){
		if(streamName.compareTo(VideoStreamingThread.THREAD_NAME) == 0 || streamName.compareTo(RobotControlThread.THREAD_NAME) == 0)
			connections += 1;
		if(connections >= 2){
			Gdx.app.debug(TAG, CLASS_NAME + ".networkStreamConnected() :: Stopping service broadcast.");
			udpThread.finish();
			mcastEnabler.disableMulticast();
		}
	}
}
