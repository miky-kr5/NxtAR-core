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
import ve.ucv.ciens.ccg.nxtar.interfaces.NetworkConnectionListener;
import ve.ucv.ciens.ccg.nxtar.utils.ProjectConstants;

import com.badlogic.gdx.Gdx;

public class VideoStreamingThread extends Thread {
	public static final String THREAD_NAME = "VideoStreamingThread";
	private static final String TAG = "NXTAR_CORE_VIDEOTHREAD";
	private static final String CLASS_NAME = VideoStreamingThread.class.getSimpleName();

	//private enum ProtocolState_t {WAIT_FOR_START, SEND_CONTINUE, RECEIVE_DATA, SEND_ACK_NEXT, SEND_ACK_WAIT, PAUSED, END_STREAM};

	private NetworkConnectionListener netListener;
	//private ServerSocket server;
	private DatagramSocket socket;
	//private Toaster toaster;
	//private ProtocolState_t protocolState;
	private boolean protocolStarted;
	/*private boolean pauseProtocol;
	private boolean endProtocol;*/
	private boolean done;
	private boolean pause;
	private Object protocolPauseMonitor;
	private Socket client;
	//private ObjectInputStream reader;
	//private ObjectOutputStream writer;
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
		//toaster = null;
		protocolStarted = false;
		/*endProtocol = false;
		pauseProtocol = false;*/
		done = false;
		//protocolState = ProtocolState_t.WAIT_FOR_START;
		protocolPauseMonitor = new Object();
		frameMonitor = VideoFrameMonitor.getInstance();

		try{
			//server = new ServerSocket(ProjectConstants.SERVER_TCP_PORT_1);
			socket = new DatagramSocket(ProjectConstants.SERVER_TCP_PORT_1);
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

	/*public VideoStreamingThread setToaster(Toaster toaster){
		this.toaster = toaster;
		return this;
	}*/

	public void addNetworkConnectionListener(NetworkConnectionListener listener){
		netListener = listener;
	}

	/*private void toast(String message){
		if(toaster != null)
			toaster.showShortToast(message);
	}*/

	public void startStreaming(){
		if(!protocolStarted){
			Gdx.app.debug(TAG, CLASS_NAME + ".startStreaming() :: Requesting protocol start.");
			synchronized(protocolPauseMonitor){
				protocolStarted = true;
				//protocolState = ProtocolState_t.SEND_CONTINUE;
				protocolPauseMonitor.notifyAll();
			}
		}
	}

	public void pauseStreaming(){
		if(protocolStarted){
			Gdx.app.debug(TAG, CLASS_NAME + ".pauseStreaming() :: Requesting protocol pause.");
			//pauseProtocol = true;
		}else
			return;
	}

	public void resumeStreaming(){
		if(protocolStarted){
			Gdx.app.debug(TAG, CLASS_NAME + ".resumeStreaming() :: Requesting protocol resume.");
			synchronized(protocolPauseMonitor){
				//pauseProtocol = false;
				protocolPauseMonitor.notifyAll();
			}
		}else
			return;
	}

	public void finishStreaming(){
		if(protocolStarted){
			Gdx.app.debug(TAG, CLASS_NAME + ".finishStreaming() :: Requesting protocol end.");
			//endProtocol = true;
		}else
			return;
	}

	public void finish(){
		done = true;
	}

	/*@Override
	public void run(){
		Object tmpMessage;
		VideoStreamingControlMessage controlMessage;
		VideoFrameDataMessage dataMessage;

		// Listen on the server socket until a client successfully connects.
		do{
			try{
				Gdx.app.debug(TAG, CLASS_NAME + ".run() :: Listening for client.");
				client = server.accept();
				if(netListener != null)
					netListener.networkStreamConnected(THREAD_NAME);
				writer = new ObjectOutputStream(client.getOutputStream());
				reader = new ObjectInputStream(client.getInputStream());
				toast("Client connected");
			}catch(IOException io){
				Gdx.app.error(TAG, CLASS_NAME + ".run() :: Error accepting client: " + io.getMessage(), io);
				client = null;
			}
		}while(client != null && !client.isConnected());

		while(!done){
			switch(protocolState){
			case WAIT_FOR_START:
				Gdx.app.debug(TAG, CLASS_NAME + ".run() :: State is WAIT_FOR_START.");
				// If the app has not started the protocol then wait.
				synchronized(protocolPauseMonitor){
					while(!protocolStarted){
						try{
							Gdx.app.debug(TAG, CLASS_NAME + ".run() :: Protocol has not started, waiting.");
							protocolPauseMonitor.wait();
						}catch(InterruptedException ie){ }
					}
				}
				break;

			case SEND_CONTINUE:
				Gdx.app.debug(TAG, CLASS_NAME + ".run() :: State is SEND_CONTINUE.");
				// Prepare the message.
				controlMessage = new VideoStreamingControlMessage();
				if(!endProtocol){
					Gdx.app.debug(TAG, CLASS_NAME + ".run() :: Preparing STREAM_CONTROL_END message.");
					controlMessage.message = VideoStreamingProtocol.STREAM_CONTROL_END;
				}else{
					Gdx.app.debug(TAG, CLASS_NAME + ".run() :: Preparing FLOW_CONTROL_CONTINUE message.");
					controlMessage.message = VideoStreamingProtocol.FLOW_CONTROL_CONTINUE;
				}

				// Send it!
				try{
					writer.writeObject(controlMessage);
				}catch(IOException io){
					Gdx.app.error(TAG, CLASS_NAME + ".run() :: Error sending message: " + io.getMessage(), io);
				}finally{
					protocolState = ProtocolState_t.RECEIVE_DATA;
				}
				break;

			case RECEIVE_DATA:
				Gdx.app.debug(TAG, CLASS_NAME + ".run() :: State is RECEIVE_DATA.");

				try{
					tmpMessage = reader.readObject();
				}catch(IOException io){
					Gdx.app.error(TAG, CLASS_NAME + ".run() :: IOException while receiving message: " + io.getMessage(), io);
					break;
				}catch(ClassNotFoundException cn){
					Gdx.app.error(TAG, CLASS_NAME + ".run() :: ClassNotFoundException while receiving message: " + cn.getMessage(), cn);
					break;
				}

				if(tmpMessage instanceof VideoStreamingControlMessage){
					Gdx.app.debug(TAG, CLASS_NAME + ".run() :: Received a control message.");
					controlMessage = (VideoStreamingControlMessage) tmpMessage;
					// TODO: handle this case correctly.

				}else if(tmpMessage instanceof VideoFrameDataMessage){
					Gdx.app.debug(TAG, CLASS_NAME + ".run() :: Received a data message.");
					dataMessage = (VideoFrameDataMessage) tmpMessage;

					Gdx.app.debug(TAG, CLASS_NAME + ".run() :: Received frame dimensions are: " +
							Integer.toString(dataMessage.imageWidth) + "x" + Integer.toString(dataMessage.imageHeight));
					frameMonitor.setFrameDimensions(dataMessage.imageWidth, dataMessage.imageHeight);
					frameMonitor.setNewFrame(dataMessage.data);

					if(pauseProtocol)
						protocolState = ProtocolState_t.SEND_ACK_WAIT;
					else
						protocolState = ProtocolState_t.SEND_ACK_NEXT;

				}else{
					Gdx.app.error(TAG, CLASS_NAME + ".run() :: Unrecognized message received!.");
					// TODO: handle this case correctly.
					System.exit(ProjectConstants.EXIT_FAILURE);
				}

				break;

			case SEND_ACK_NEXT:
				Gdx.app.debug(TAG, CLASS_NAME + ".run() :: State is SEND_ACK_NEXT.");
				// Prepare the message.
				controlMessage = new VideoStreamingControlMessage();
				if(!endProtocol){
					Gdx.app.debug(TAG, CLASS_NAME + ".run() :: Preparing STREAM_CONTROL_END message.");
					controlMessage.message = VideoStreamingProtocol.STREAM_CONTROL_END;
				}else{
					Gdx.app.debug(TAG, CLASS_NAME + ".run() :: Preparing ACK_SEND_NEXT message.");
					controlMessage.message = VideoStreamingProtocol.ACK_SEND_NEXT;
				}

				// Send it!
				try{
					writer.writeObject(controlMessage);
				}catch(IOException io){
					Gdx.app.error(TAG, CLASS_NAME + ".run() :: Error sending message: " + io.getMessage(), io);
				}finally{
					if(!endProtocol)
						protocolState = ProtocolState_t.RECEIVE_DATA;
					else
						protocolState = ProtocolState_t.END_STREAM;
				}
				break;

			case SEND_ACK_WAIT:
				Gdx.app.debug(TAG, CLASS_NAME + ".run() :: State is SEND_ACK_WAIT.");
				// Prepare the message.
				controlMessage = new VideoStreamingControlMessage();
				controlMessage.message = VideoStreamingProtocol.ACK_WAIT;

				// Send it!
				try{
					writer.writeObject(controlMessage);
				}catch(IOException io){
					Gdx.app.error(TAG, CLASS_NAME + ".run() :: Error sending message: " + io.getMessage(), io);
				}finally{
					protocolState = ProtocolState_t.PAUSED;
				}
				break;

			case PAUSED:
				Gdx.app.debug(TAG, CLASS_NAME + ".run() :: State is PAUSED.");
				// The app requested to stop the protocol temporarily.
				synchronized(protocolPauseMonitor){
					while(pauseProtocol){
						try{
							Gdx.app.debug(TAG, CLASS_NAME + ".run() :: Protocol pause requested, waiting.");
							protocolPauseMonitor.wait();
						}catch(InterruptedException ie){ }
					}
				}
				protocolState = ProtocolState_t.SEND_CONTINUE;
				break;

			case END_STREAM:
				Gdx.app.debug(TAG, CLASS_NAME + ".run() :: State is END_STREAM.");
				// Simply disconnect from the client and end the thread.
				try{
					client.close();
				}catch(IOException io){
					Gdx.app.error(TAG, CLASS_NAME + ".run() :: Error closing client: " + io.getMessage(), io);
				}
				done = true;
				break;
			}
		}
		Gdx.app.debug(TAG, CLASS_NAME + ".run() :: Thread finished.");
	}*/

	/*private void receiveImage(){
		Object tmpMessage;
		VideoFrameDataMessage dataMessage;

		Gdx.app.debug(TAG, CLASS_NAME + ".run() :: Receiving data.");

		try{
			tmpMessage = (VideoFrameDataMessage)reader.readObject();
		}catch(IOException io){
			Gdx.app.error(TAG, CLASS_NAME + ".run() :: IOException while receiving message: " + io.getMessage());
			return;
		}catch(ClassNotFoundException cn){
			Gdx.app.error(TAG, CLASS_NAME + ".run() :: ClassNotFoundException while receiving message: " + cn.getMessage());
			return;
		}

		if(tmpMessage instanceof VideoFrameDataMessage){
			Gdx.app.debug(TAG, CLASS_NAME + ".run() :: Received a data message.");
			dataMessage = (VideoFrameDataMessage) tmpMessage;
			frameMonitor.setFrameDimensions(dataMessage.imageWidth, dataMessage.imageHeight);
			frameMonitor.setNewFrame(dataMessage.data);

		}else{
			Gdx.app.debug(TAG, CLASS_NAME + ".run() :: Received something unknown.");
		}
	}*/

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

			Gdx.app.debug(TAG, CLASS_NAME + ".receiveUdp() :: Reading message size from socket.");
			try{
				packet = new DatagramPacket(size, size.length);
				socket.receive(packet);
			}catch(IOException io){
				Gdx.app.error(TAG, CLASS_NAME + ".receiveUdp() :: IOException receiving size " + io.getMessage());
				lostFramesPerSecond += 1;
				return;
			}

			Gdx.app.debug(TAG, CLASS_NAME + ".receiveUdp() :: Creating buffers.");
			intSize = byteArray2Int(size);
			data = new byte[intSize];

			Gdx.app.debug(TAG, CLASS_NAME + ".receiveUdp() :: Reading message from socket.");
			try{
				packet = new DatagramPacket(data, data.length);
				socket.receive(packet);
			}catch(IOException io){
				Gdx.app.error(TAG, CLASS_NAME + ".receiveUdp() :: IOException receiving data " + io.getMessage());
				lostFramesPerSecond += 1;
				return;
			}

			ByteArrayInputStream bais = new ByteArrayInputStream(data);

			Gdx.app.debug(TAG, CLASS_NAME + ".receiveUdp() :: Saving message in monitor.");
			try{
				ObjectInputStream ois = new ObjectInputStream(bais);
				tmpMessage = ois.readObject();

				if(tmpMessage instanceof VideoFrameDataMessage){
					Gdx.app.debug(TAG, CLASS_NAME + ".receiveUdp() :: Received a data message.");
					dataMessage = (VideoFrameDataMessage) tmpMessage;

					Gdx.app.debug(TAG, CLASS_NAME + ".receiveUdp() :: Received frame dimensions are: " +
							Integer.toString(dataMessage.imageWidth) + "x" + Integer.toString(dataMessage.imageHeight));
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
		// Listen on the server socket until a client successfully connects.
		/*do{
			try{
				Gdx.app.debug(TAG, CLASS_NAME + ".run() :: Listening for client.");
				client = server.accept();
				if(netListener != null)
					netListener.networkStreamConnected(THREAD_NAME);
				//writer = new ObjectOutputStream(client.getOutputStream());
				reader = new ObjectInputStream(client.getInputStream());
				toast("Client connected");
			}catch(IOException io){
				Gdx.app.error(TAG, CLASS_NAME + ".run() :: Error accepting client: " + io.getMessage(), io);
				client = null;
			}
		}while(client != null && !client.isConnected());*/

		then = System.currentTimeMillis();

		while(!done){
			synchronized (pauseMonitor) {
				while(pause){
					try{ pauseMonitor.wait(); }catch(InterruptedException ie){ }
				}
			}
			Gdx.app.debug(TAG, CLASS_NAME + ".run() :: Receiving.");
			if(netListener != null)
				netListener.networkStreamConnected(THREAD_NAME);
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
