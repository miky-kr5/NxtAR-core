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
package ve.ucv.ciens.ccg.nxtar.utils;

import com.badlogic.gdx.controllers.mappings.Ouya;

public abstract class ProjectConstants{
	public static final int SERVICE_DISCOVERY_PORT = 9988;
	public static final int VIDEO_STREAMING_PORT = 9989;
	public static final int MOTOR_CONTROL_PORT = 9990;
	public static final int SENSOR_REPORT_PORT = 9991;
	public static final int APP_CONTROL_PORT = 9992;

	public static final String MULTICAST_ADDRESS = "230.0.0.1";

	public static final int EXIT_SUCCESS = 0;
	public static final int EXIT_FAILURE = 1;

	public static final boolean DEBUG = true;

	public static final int[] POWERS_OF_2 = {64, 128, 256, 512, 1024, 2048};

	public static final float OVERSCAN;

	public static final String FONT_CHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";

	public static final int CALIBRATION_PATTERN_POINTS = 54;

	static{
		OVERSCAN = Ouya.runningOnOuya ? 0.9f : 1.0f;
	}
}
