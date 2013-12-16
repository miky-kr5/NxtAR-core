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
package ve.ucv.ciens.ccg.nxtar.network;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import ve.ucv.ciens.ccg.nxtar.interfaces.NetworkConnectionListener;
import ve.ucv.ciens.ccg.nxtar.interfaces.Toaster;
import ve.ucv.ciens.ccg.nxtar.utils.ProjectConstants;

import com.badlogic.gdx.Gdx;

public class VideoStreamingThread extends Thread {
	public static final String THREAD_NAME = "VideoStreamingThread";
	private static final String TAG = "NXTAR_CORE_VIDEOTHREAD";
	private static final String CLASS_NAME = VideoStreamingThread.class.getSimpleName();

	private NetworkConnectionListener netListener;
	private ServerSocket server;
	private Socket client;
	private Toaster toaster;

	private VideoStreamingThread(){
		super(THREAD_NAME);

		netListener = null;
		try{
			server = new ServerSocket(ProjectConstants.SERVER_TCP_PORT_1);
		}catch(IOException io){
			Gdx.app.error(TAG, CLASS_NAME + ".VideoStreamingThread() :: Error creating server: " + io.getMessage(), io);
		}
	}

	private static class SingletonHolder{
		public static final VideoStreamingThread INSTANCE = new VideoStreamingThread();
	}

	public static VideoStreamingThread getInstance(){
		return SingletonHolder.INSTANCE;
	}

	public VideoStreamingThread setToaster(Toaster toaster){
		this.toaster = toaster;
		return this;
	}

	public void addNetworkConnectionListener(NetworkConnectionListener listener){
		netListener = listener;
	}

	@Override
	public void run(){
		try{
			client = server.accept();
			if(netListener != null)
				netListener.networkStreamConnected(THREAD_NAME);
			toaster.showShortToast("Client connected to VideoStreamingThread");
			client.close();
		}catch(IOException io){
			Gdx.app.error(TAG, CLASS_NAME + ".run() :: Error accepting client: " + io.getMessage(), io);
		}
	}
}
