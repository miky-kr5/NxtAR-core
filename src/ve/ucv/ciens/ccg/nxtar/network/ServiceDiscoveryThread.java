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
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;

import ve.ucv.ciens.ccg.nxtar.utils.ProjectConstants;

import com.badlogic.gdx.Gdx;

/**
 * Ad hoc service discovery server thread.
 * 
 * <p> This thread performs an ad hoc service discovery protocol. A multicast datagram packet is sent every
 * 250 miliseconds carrying the string "NxtAR server is here!" on the multicast address defined 
 * in {@link ve.ucv.ciens.ccg.nxtar.utils.ProjectConstants#MULTICAST_ADDRESS}. The port defined in
 * {@link ve.ucv.ciens.ccg.nxtar.utils.ProjectConstants#SERVICE_DISCOVERY_PORT} is used for the transmissions. The server stops
 * when another thread calls the {@link #finish()} method or the server fails to transmit {@link #MAX_RETRIES} packets in
 * a row, whichever happens first.</p>
 * 
 * @author miky
 */
public class ServiceDiscoveryThread extends Thread {
	/**
	 * The name used to identify this thread.
	 */
	public static final String THREAD_NAME = "ServiceDiscoveryThread";
	/**
	 * Tag used for logging.
	 */
	private static final String TAG = "NXTAR_CORE_UDPTHREAD";
	/**
	 * Class name used for logging.
	 */
	private static final String CLASS_NAME = ServiceDiscoveryThread.class.getSimpleName();
	/**
	 * Maximum number of transmission attempts before ending the thread abruptly.
	 */
	private static final int MAX_RETRIES = 5;

	/**
	 * A semaphore object used to synchronize acces to this thread finish flag.
	 */
	private Object semaphore;
	/**
	 * The finish flag.
	 */
	private boolean done;
	/**
	 * The UDP server socket used for the ad hoc service discovery protocol.
	 */
	private DatagramSocket udpServer;
	/**
	 * Holder for the multicast address used in the protocol.
	 */
	private InetAddress group;

	private ServiceDiscoveryThread(){
		// Setup this thread name.
		super(THREAD_NAME);

		done = false;
		semaphore = new Object();

		// Try to get the InetAddress defined by the IP address defined in ProjectConstants.MULTICAST_ADDRESS.
		try{
			group = InetAddress.getByName(ProjectConstants.MULTICAST_ADDRESS);
		}catch(UnknownHostException uh){
			group = null;
		}

		// Create a UDP socket at the port defined in ProjectConstants.SERVER_UDP_PORT.
		Gdx.app.debug(TAG, CLASS_NAME + ".ServiceDiscoveryThread() :: Creating multicast server.");
		try{
			udpServer = new DatagramSocket(ProjectConstants.SERVICE_DISCOVERY_PORT);
		}catch(IOException io){
			Gdx.app.error(TAG, CLASS_NAME + ".ServiceDiscoveryThread() :: Error creating UDP socket: " + io.getMessage());
			udpServer = null;
		}
		Gdx.app.debug(TAG, CLASS_NAME + ".ServiceDiscoveryThread() :: Multicast server created.");
	}

	/**
	 * Singleton holder for this class.
	 */
	private static class SingletonHolder{
		public static final ServiceDiscoveryThread INSTANCE = new ServiceDiscoveryThread();
	}

	/**
	 * Get the singleton instance of this class.
	 * 
	 * @return The singleton instance.
	 */
	public static ServiceDiscoveryThread getInstance(){
		return SingletonHolder.INSTANCE;
	}

	/**
	 * This thread's run method.
	 * 
	 * <p>This method executes the ad hoc service discovery protocol implemented by this class, as
	 * described in the class introduction.</p>
	 */
	@Override
	public void run(){
		int retries = 0;
		byte[] buffer = (new String("NxtAR server here!")).getBytes();

		// If failed to get any of the required network elements then end the thread right away.
		if(group == null || udpServer == null){
			Gdx.app.error(TAG, CLASS_NAME + ".run() :: No multicast address defined, ending thread.");
			return;
		}
		if(group == null){
			Gdx.app.error(TAG, CLASS_NAME + ".run() :: No server available, ending thread.");
			return;
		}

		while(true){
			// Verify if the thread should end. If that is the case, close the server.
			synchronized(semaphore){
				if(done){
					udpServer.close();
					break;
				}
			}
			try{
				// End the thread if already at the last retry attempt after too many failed transmissions.
				if(retries >= MAX_RETRIES){
					Gdx.app.error(TAG, CLASS_NAME + ".run() :: Too many failed transmissions, ending thread.");
					udpServer.close();
					break;
				}
				// Send the packet and reset the retry counter.
				DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group, ProjectConstants.SERVICE_DISCOVERY_PORT);
				udpServer.send(packet);
				retries = 0;
				try{ sleep(250L); }catch(InterruptedException ie){ }

			}catch(IOException io){
				Gdx.app.error(TAG, CLASS_NAME + ".run() :: Error sending packet: " + io.getMessage());
				retries += 1;
			}
		}
		if(retries < MAX_RETRIES)
			Gdx.app.debug(TAG, CLASS_NAME + ".run() :: Service discovery successfully terminated.");
		else
			Gdx.app.debug(TAG, CLASS_NAME + ".run() :: Service discovery terminated after too many failed transmissions.");
	}

	/**
	 * Marks this thread as ready to end.
	 */
	public void finish(){
		synchronized(semaphore){
			Gdx.app.debug(TAG, CLASS_NAME + ".finish() :: Finishing service discovery thread.");
			done = true;
		}
	}
}
