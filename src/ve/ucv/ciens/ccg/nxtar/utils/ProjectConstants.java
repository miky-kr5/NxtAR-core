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

public abstract class ProjectConstants {
	public static final int SERVER_UDP_PORT = 8889;
	public static final int SERVER_TCP_PORT_1 = 9989;
	public static final int SERVER_TCP_PORT_2 = 9990;
	public static final String MULTICAST_ADDRESS = "230.0.0.1";
	public static final int EXIT_SUCCESS = 0;
	public static final int EXIT_FAILURE = 1;

	public static final boolean DEBUG = true;
	
	public static final int[] POWERS_OF_2 = {64, 128, 256, 512, 1024, 2048};
}
