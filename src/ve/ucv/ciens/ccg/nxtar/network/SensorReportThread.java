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
package ve.ucv.ciens.ccg.nxtar.network;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

import ve.ucv.ciens.ccg.nxtar.interfaces.ApplicationEventsListener;
import ve.ucv.ciens.ccg.nxtar.utils.ProjectConstants;

import com.badlogic.gdx.Gdx;

public class SensorReportThread extends Thread {
	public static final String THREAD_NAME = "SensorReportThread";
	private static final String TAG = "NXTAR_CORE_ROBOTTHREAD";
	private static final String CLASS_NAME = SensorReportThread.class.getSimpleName();

	private ApplicationEventsListener netListener;
	private ServerSocket server;
	private Socket client;
	private Object pauseMonitor;
	private boolean paused;
	private boolean done;
	private InputStream reader;
	private Byte lightReading;

	private SensorReportThread(){
		paused = false;
		done = false;
		netListener = null;
		pauseMonitor = null;
		client = null;
		lightReading = -1;

		try{
			server = new ServerSocket(ProjectConstants.SENSOR_REPORT_PORT);
		}catch(IOException io){
			Gdx.app.error(TAG, CLASS_NAME + ".RobotControlThread() :: Error creating server: " + io.getMessage(), io);
			server = null;
		}
	}

	private static class SingletonHolder{
		public final static SensorReportThread INSTANCE = new SensorReportThread();
	}

	public static SensorReportThread getInstance(){
		return SingletonHolder.INSTANCE;
	}

	public void addNetworkConnectionListener(ApplicationEventsListener listener){
		netListener = listener;
	}

	public void pauseThread(){
		synchronized(pauseMonitor){
			paused = true;
		}
	}

	public void resumeThread(){
		synchronized(pauseMonitor){
			paused = false;
		}
	}

	public void finish(){
		done = true;
	}

	public byte getLightSensorReading(){
		byte data;

		synchronized(lightReading){
			data = lightReading.byteValue();
		}

		return data;
	}

	@Override
	public void run(){
		byte[] reading = new byte[1];

		try{
			client = server.accept();
			client.setTcpNoDelay(true);
			if(netListener != null) netListener.networkStreamConnected(THREAD_NAME);
			reader = client.getInputStream();

		}catch(IOException io){
			Gdx.app.error(TAG, CLASS_NAME + ".run() :: Error accepting client: " + io.getMessage(), io);
			return;
		}

		while(!paused){
			if(done) break;

			try{
				reader.read(reading);
			}catch(IOException io){
				Gdx.app.error(TAG, CLASS_NAME + ".run() :: IOException during sensor read: " + io.getMessage(), io);
				break;
			}

			synchronized (lightReading) {
				lightReading = reading[0];
			}
		}
	}
}
