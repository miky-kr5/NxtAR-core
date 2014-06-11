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
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import ve.ucv.ciens.ccg.networkdata.MotorEvent;
import ve.ucv.ciens.ccg.networkdata.MotorEventACK;
import ve.ucv.ciens.ccg.nxtar.interfaces.ApplicationEventsListener;
import ve.ucv.ciens.ccg.nxtar.network.monitors.MotorEventQueue;
import ve.ucv.ciens.ccg.nxtar.utils.ProjectConstants;

import com.badlogic.gdx.Gdx;

public class RobotControlThread extends Thread {
	public static final String THREAD_NAME = "RobotControlThread";
	private static final String TAG = "NXTAR_CORE_ROBOTTHREAD";
	private static final String CLASS_NAME = RobotControlThread.class.getSimpleName();
	private static int refCount = 0;

	private ApplicationEventsListener netListener;
	private ServerSocket server;
	private Socket client;
	private MotorEventQueue queue;
	private Object pauseMonitor;
	private boolean paused;
	private boolean done;
	private ObjectOutputStream os;
	private ObjectInputStream is;

	private RobotControlThread(){
		super(THREAD_NAME);

		netListener = null;
		queue = MotorEventQueue.getInstance();
		pauseMonitor = new Object();
		paused = false;
		done = false;

		try{
			server = new ServerSocket(ProjectConstants.MOTOR_CONTROL_PORT);
		}catch(IOException io){
			Gdx.app.error(TAG, CLASS_NAME + ".RobotControlThread() :: Error creating server: " + io.getMessage(), io);
		}
	}

	private static class SingletonHolder{
		public static RobotControlThread INSTANCE;
	}

	public static RobotControlThread getInstance(){
		if(refCount == 0)
			SingletonHolder.INSTANCE = new RobotControlThread();
		refCount++;
		return SingletonHolder.INSTANCE;
	}

	public static void freeInstance(){
		refCount--;
		if(refCount == 0) SingletonHolder.INSTANCE = null;
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
		try{
			server.close();
		}catch(IOException io){
			Gdx.app.error(TAG, CLASS_NAME + ".run() :: Error closing client: " + io.getMessage(), io);
		}
	}

	@Override
	public void run(){
		MotorEvent message;
		MotorEventACK ack;

		try{
			client = server.accept();
			client.setTcpNoDelay(true);
			if(netListener != null) netListener.onNetworkStreamConnected(THREAD_NAME);
			os = new ObjectOutputStream(client.getOutputStream());
			is = new ObjectInputStream(client.getInputStream());

		}catch(IOException io){
			Gdx.app.error(TAG, CLASS_NAME + ".run() :: Error accepting client: " + io.getMessage(), io);
			return;
		}

		while(!paused){
			if(done){
				break;
			}

			// Send the motor event.
			try{
				message = queue.getNextEvent();
				os.writeObject(message);
				message = null;

			}catch(InvalidClassException ic){
				Gdx.app.error(TAG, CLASS_NAME + ".run() :: InvalidClassException during transmission: " + ic.getMessage(), ic);
				break;

			}catch(IOException io){
				Gdx.app.error(TAG, CLASS_NAME + ".run() :: IOException during transmission: " + io.getMessage(), io);
				break;
			}

			// Receive ack.
			try{
				ack = (MotorEventACK)is.readObject();
			}catch(ClassNotFoundException cn){
				Gdx.app.error(TAG, CLASS_NAME + ".run() :: InvalidClassException during reception: " + cn.getMessage(), cn);
				break;

			}catch(ClassCastException cc){
				Gdx.app.error(TAG, CLASS_NAME + ".run() :: InvalidClassException during reception: " + cc.getMessage(), cc);
				break;

			}catch(IOException io){
				Gdx.app.error(TAG, CLASS_NAME + ".run() :: InvalidClassException during reception: " + io.getMessage(), io);
				break;
			}

			if(ack.isClientQueueFull()){
				// Wait for client to notify.
				// A client will never send two queue full acks in a row.
				try{
					ack = (MotorEventACK)is.readObject();
				}catch(ClassNotFoundException cn){
					Gdx.app.error(TAG, CLASS_NAME + ".run() :: InvalidClassException during reception: " + cn.getMessage(), cn);
					break;

				}catch(ClassCastException cc){
					Gdx.app.error(TAG, CLASS_NAME + ".run() :: InvalidClassException during reception: " + cc.getMessage(), cc);
					break;

				}catch(IOException io){
					Gdx.app.error(TAG, CLASS_NAME + ".run() :: InvalidClassException during reception: " + io.getMessage(), io);
					break;
				}

			}else{
				// Clean and continue.
				ack = null;
				message = null;
				continue;
			}
		}

		try{
			client.close();
		}catch(IOException io){
			Gdx.app.error(TAG, CLASS_NAME + ".run() :: Error closing client: " + io.getMessage(), io);
		}
	}
}
