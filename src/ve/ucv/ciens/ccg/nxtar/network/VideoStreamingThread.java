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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Socket;

import ve.ucv.ciens.ccg.networkdata.VideoFrameDataMessage;
import ve.ucv.ciens.ccg.nxtar.interfaces.ApplicationEventsListener;
import ve.ucv.ciens.ccg.nxtar.network.monitors.VideoFrameMonitor;
import ve.ucv.ciens.ccg.nxtar.utils.ProjectConstants;

import com.badlogic.gdx.Gdx;

public class VideoStreamingThread extends Thread{
	public static final String THREAD_NAME = "VideoStreamingThread";
	private static final String TAG = "NXTAR_CORE_VIDEOTHREAD";
	private static final String CLASS_NAME = VideoStreamingThread.class.getSimpleName();

	private ApplicationEventsListener netListener;
	private DatagramSocket socket;
	private boolean protocolStarted;
	private boolean done;
	private boolean pause;
	private boolean coreNotified;
	private Object protocolPauseMonitor;
	private Socket client;
	private VideoFrameMonitor frameMonitor;
	private long then;
	private long now;
	private long delta;
	private int fps;
	private int lostFramesPerSecond;
	private int lostFrames;
	private Object pauseMonitor;

	private VideoStreamingThread(){
		super(THREAD_NAME);

		pauseMonitor = new Object();
		fps = 0;
		lostFramesPerSecond = 0;
		netListener = null;
		protocolStarted = false;
		done = false;
		coreNotified = false;
		protocolPauseMonitor = new Object();
		frameMonitor = VideoFrameMonitor.getInstance();

		try{
			socket = new DatagramSocket(ProjectConstants.VIDEO_STREAMING_PORT);
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

	public void addNetworkConnectionListener(ApplicationEventsListener listener){
		netListener = listener;
	}

	public void startStreaming(){
		if(!protocolStarted){
			Gdx.app.debug(TAG, CLASS_NAME + ".startStreaming() :: Requesting protocol start.");
			synchronized(protocolPauseMonitor){
				protocolStarted = true;
				protocolPauseMonitor.notifyAll();
			}
		}
	}

	public void pauseStreaming(){
		if(protocolStarted){
			Gdx.app.debug(TAG, CLASS_NAME + ".pauseStreaming() :: Requesting protocol pause.");
		}else
			return;
	}

	public void resumeStreaming(){
		if(protocolStarted){
			Gdx.app.debug(TAG, CLASS_NAME + ".resumeStreaming() :: Requesting protocol resume.");
			synchronized(protocolPauseMonitor){
				protocolPauseMonitor.notifyAll();
			}
		}else
			return;
	}

	public void finishStreaming(){
		if(protocolStarted){
			Gdx.app.debug(TAG, CLASS_NAME + ".finishStreaming() :: Requesting protocol end.");
		}else
			return;
	}

	public void finish(){
		done = true;
	}

	private int byteArray2Int(byte[] array){
		int number = 0;
		for(int i = 0; i < 4; i++){
			number |= (array[3-i] & 0xff) << (i << 3);
		}
		return number;
	}

	private void receiveUdp(){
		try{
			int intSize;
			byte[] size = new byte[4];
			byte[] data;
			DatagramPacket packet;
			VideoFrameDataMessage dataMessage;
			Object tmpMessage;

			//Gdx.app.debug(TAG, CLASS_NAME + ".receiveUdp() :: Reading message size from socket.");
			try{
				packet = new DatagramPacket(size, size.length);
				socket.receive(packet);
			}catch(IOException io){
				Gdx.app.error(TAG, CLASS_NAME + ".receiveUdp() :: IOException receiving size " + io.getMessage());
				lostFramesPerSecond += 1;
				return;
			}

			//Gdx.app.debug(TAG, CLASS_NAME + ".receiveUdp() :: Creating buffers.");
			intSize = byteArray2Int(size);
			data = new byte[intSize];

			//Gdx.app.debug(TAG, CLASS_NAME + ".receiveUdp() :: Reading message from socket.");
			try{
				packet = new DatagramPacket(data, data.length);
				socket.receive(packet);
			}catch(IOException io){
				Gdx.app.error(TAG, CLASS_NAME + ".receiveUdp() :: IOException receiving data " + io.getMessage());
				lostFramesPerSecond += 1;
				return;
			}

			ByteArrayInputStream bais = new ByteArrayInputStream(data);

			//Gdx.app.debug(TAG, CLASS_NAME + ".receiveUdp() :: Saving message in monitor.");
			try{
				ObjectInputStream ois = new ObjectInputStream(bais);
				tmpMessage = ois.readObject();

				if(tmpMessage instanceof VideoFrameDataMessage){
					//Gdx.app.debug(TAG, CLASS_NAME + ".receiveUdp() :: Received a data message.");
					dataMessage = (VideoFrameDataMessage) tmpMessage;

					//Gdx.app.debug(TAG, CLASS_NAME + ".receiveUdp() :: Received frame dimensions are: " + Integer.toString(dataMessage.imageWidth) + "x" + Integer.toString(dataMessage.imageHeight));
					frameMonitor.setFrameDimensions(dataMessage.imageWidth, dataMessage.imageHeight);
					frameMonitor.setNewFrame(dataMessage.data);

				}else{
					Gdx.app.debug(TAG, CLASS_NAME + ".receiveUdp() :: Received something unknown.");
					lostFramesPerSecond += 1;
				}
			}catch(IOException io){
				Gdx.app.error(TAG, CLASS_NAME + ".receiveUdp() :: IOException received deserializing message " + io.getMessage());
				lostFramesPerSecond += 1;
				return;
			}catch(ClassNotFoundException cn){
				Gdx.app.error(TAG, CLASS_NAME + ".receiveUdp() :: ClassNotFoundException received " + cn.getMessage());
				lostFramesPerSecond += 1;
				return;
			}
		}catch(Exception e){
			Gdx.app.error(TAG, CLASS_NAME + ".receiveUdp() :: Exception received " + e.getMessage());
			lostFramesPerSecond += 1;
			return;
		}
	}

	public int getFps(){
		return fps;
	}
	
	public int getLostFrames(){
		return lostFrames;
	}

	@Override
	public void run(){
		int frames = 0;
		lostFrames = 0;
		then = System.currentTimeMillis();

		while(!done){
			synchronized (pauseMonitor) {
				while(pause){
					try{ pauseMonitor.wait(); }catch(InterruptedException ie){ }
				}
			}
			//Gdx.app.debug(TAG, CLASS_NAME + ".run() :: Receiving.");
			if(netListener != null && !coreNotified && frameMonitor.getCurrentFrame() != null){
				coreNotified = true;
				netListener.networkStreamConnected(THREAD_NAME);
			}
			receiveUdp();
			frames++;
			now = System.currentTimeMillis();
			delta = now - then;
			if(delta >= 1000){
				fps = frames;
				frames = 0;
				lostFrames = lostFramesPerSecond;
				lostFramesPerSecond = 0;
				then = now;
				delta = 0;
			}
		}

		try{
			client.close();
		}catch(IOException io){
			Gdx.app.error(TAG, CLASS_NAME + ".run() :: Error closing client socket.", io);
		}

		Gdx.app.debug(TAG, CLASS_NAME + ".run() :: Thread finished.");
	}

	public void pause(){
		synchronized (pauseMonitor){
			pause = true;
		}
	}
	
	public void play(){
		synchronized (pauseMonitor){
			pause = false;
			pauseMonitor.notifyAll();
		}
	}
}
