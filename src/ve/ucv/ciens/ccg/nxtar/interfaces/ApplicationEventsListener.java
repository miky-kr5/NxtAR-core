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
package ve.ucv.ciens.ccg.nxtar.interfaces;

public interface ApplicationEventsListener {
	/**
	 * <p>Callback used by the networking threads to notify sucessfull connections
	 * to the application</p>
	 * 
	 * @param streamName The name of the thread notifying a connection.
	 */
	public void onNetworkStreamConnected(String streamName);

	/**
	 * <p>Callback used by the assets loader to notify that all 
	 * required game assets are ready to be used.</p>
	 */
	public void onAssetsLoaded();

	/**
	 * <p>Callback used by the camera calibration state to notify that the
	 * camera has been succesfully calibrated.</p>
	 */
	public void onCameraCalibrated();
}
