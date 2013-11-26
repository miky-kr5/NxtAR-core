package ve.ucv.ciens.ccg.nxtar.network;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;

import ve.ucv.ciens.ccg.nxtar.utils.ProjectConstants;

import com.badlogic.gdx.Gdx;

public class ServiceDiscoveryThread extends Thread {
	public static final String THREAD_NAME = "ServiceDiscoveryThread";
	private static final String TAG = "NXTAR_CORE_UDPTHREAD";
	private static final String CLASS_NAME = ServiceDiscoveryThread.class.getSimpleName();

	private Object semaphore;
	private boolean done;
	private DatagramSocket udpServer;
	private InetAddress group;

	public ServiceDiscoveryThread(){
		super(THREAD_NAME);

		done = false;
		semaphore = new Object();

		try{
			group = InetAddress.getByName(ProjectConstants.MULTICAST_ADDRESS);
		}catch(UnknownHostException uh){ }

		Gdx.app.debug(TAG, CLASS_NAME + ".ServiceDiscoveryThread() :: Creating multicast server.");
		try{
			udpServer = new DatagramSocket(ProjectConstants.SERVER_UDP_PORT);
		}catch(IOException io){
			Gdx.app.error(TAG, CLASS_NAME + ".ServiceDiscoveryThread() :: Error creating UDP socket: " + io.getMessage(), io);
		}
		Gdx.app.debug(TAG, CLASS_NAME + ".ServiceDiscoveryThread() :: Multicast server created.");
	}

	@Override
	public void run(){
		byte[] buffer = (new String("NxtAR server here!")).getBytes();
		try{
			while(true){
				synchronized(semaphore){
					if(done){
						udpServer.close();
						break;
					}
				}
				//Gdx.app.debug(TAG, CLASS_NAME + ".ServiceDiscoveryThread() :: Resending packet");
				DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group, ProjectConstants.SERVER_UDP_PORT);
				udpServer.send(packet);
				//Gdx.app.debug(TAG, CLASS_NAME + ".ServiceDiscoveryThread() :: Packet sent");
				try{ sleep(250L); }catch(InterruptedException ie){ }
			}
		}catch(IOException io){
			Gdx.app.debug(TAG, CLASS_NAME + ".ServiceDiscoveryThread() :: Error sending packet: " + io.getMessage(), io);
		}
	}

	public void finish(){
		synchronized(semaphore){
			done = true;
		}
	}
}
