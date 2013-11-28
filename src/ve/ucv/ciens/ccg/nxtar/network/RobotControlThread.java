package ve.ucv.ciens.ccg.nxtar.network;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import ve.ucv.ciens.ccg.nxtar.interfaces.NetworkConnectionListener;
import ve.ucv.ciens.ccg.nxtar.interfaces.Toaster;
import ve.ucv.ciens.ccg.nxtar.utils.ProjectConstants;

import com.badlogic.gdx.Gdx;

public class RobotControlThread extends Thread {
	public static final String THREAD_NAME = "RobotControlThread";
	private static final String TAG = "NXTAR_CORE_ROBOTTHREAD";
	private static final String CLASS_NAME = RobotControlThread.class.getSimpleName();

	private NetworkConnectionListener netListener;
	private ServerSocket server;
	private Socket client;
	private Toaster toaster;

	private RobotControlThread(){
		super(THREAD_NAME);

		netListener = null;

		try{
			server = new ServerSocket(ProjectConstants.SERVER_TCP_PORT_2);
		}catch(IOException io){
			Gdx.app.error(TAG, CLASS_NAME + ".RobotControlThread() :: Error creating server: " + io.getMessage(), io);
		}
	}
	
	private static class SingletonHolder{
		public static final RobotControlThread INSTANCE = new RobotControlThread();
	}

	public static RobotControlThread getInstance(){
		return SingletonHolder.INSTANCE;
	}

	public RobotControlThread setToaster(Toaster toaster){
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
			toaster.showShortToast("Client connected to RobotControlThread");
			client.close();
		}catch(IOException io){
			Gdx.app.error(TAG, CLASS_NAME + ".run() :: Error accepting client: " + io.getMessage(), io);
		}
	}
}
