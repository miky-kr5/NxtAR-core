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
package ve.ucv.ciens.ccg.nxtar.network.monitors;

import ve.ucv.ciens.ccg.nxtar.utils.Size;

import com.badlogic.gdx.Gdx;

public class VideoFrameMonitor{
	private final String TAG = "VIDEO_FRAME_MONITOR";
	private final String CLASS_NAME = VideoFrameMonitor.class.getSimpleName();

	private byte[] frameA;
	private byte[] frameB;
	private Object frameMonitor;
	private Size frameDimensions;

	private VideoFrameMonitor(){
		frameA = null;
		frameB = null;
		frameMonitor = new Object();
		frameDimensions = new Size();
	}

	private static class SingletonHolder{
		public static final VideoFrameMonitor INSTANCE = new VideoFrameMonitor();
	}

	public static VideoFrameMonitor getInstance(){
		return SingletonHolder.INSTANCE;
	}

	public void setFrameDimensions(int width, int height){
		try{
			frameDimensions.setWidth(width);
			frameDimensions.setHeight(height);
		}catch(IllegalArgumentException ia){
			Gdx.app.debug(TAG, CLASS_NAME + ".setFrameDimensions() :: Bad argument to Size: " + ia.getMessage());
			frameDimensions.setWidth(0);
			frameDimensions.setHeight(0);
		}
	}

	public Size getFrameDimensions(){
		return frameDimensions;
	}

	public void setNewFrame(byte[] frame){
		byte[] temp;

		Gdx.app.debug(TAG, CLASS_NAME + ".setNewFrame() :: Loading new frame in frameA.");
		frameA = frame;
		temp = frameA;
		synchronized(frameMonitor){
			Gdx.app.debug(TAG, CLASS_NAME + ".setNewFrame() :: Swapping frameA and frameB.");
			frameA = frameB;
			frameB = temp;
			Gdx.app.debug(TAG, CLASS_NAME + ".setNewFrame() :: Swapping done.");
		}
	}

	public byte[] getCurrentFrame(){
		byte[] frame;

		synchronized(frameMonitor){
			//Gdx.app.debug(TAG, CLASS_NAME + ".getCurrentFrame() :: Fetching frameB.");
			frame = frameB;
		}
		return frame;
	}
}
