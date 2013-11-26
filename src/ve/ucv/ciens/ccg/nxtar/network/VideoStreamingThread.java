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

	public VideoStreamingThread(Toaster toaster){
		super(THREAD_NAME);

		this.toaster = toaster;
		netListener = null;

		try{
			server = new ServerSocket(ProjectConstants.SERVER_TCP_PORT_1);
		}catch(IOException io){
			Gdx.app.error(TAG, CLASS_NAME + ".VideoStreamingThread() :: Error creating server: " + io.getMessage(), io);
		}
	}

	public void addNetworkConnectionListener(NetworkConnectionListener listener){
		netListener = listener;
	}

	@Override
	public void run(){
		try{
			client = server.accept();
			if(netListener != null)
				netListener.interfaceConnected(THREAD_NAME);
			toaster.showShortToast("Client connected to VideoStreamingThread");
			client.close();
		}catch(IOException io){
			Gdx.app.error(TAG, CLASS_NAME + ".run() :: Error accepting client: " + io.getMessage(), io);
		}
	}
}
