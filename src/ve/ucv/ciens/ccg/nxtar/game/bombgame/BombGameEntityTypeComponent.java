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
package ve.ucv.ciens.ccg.nxtar.game.bombgame;

import com.artemis.Component;

public class BombGameEntityTypeComponent extends Component {
	public static final int BOMB_WIRE_1  = 10;
	public static final int BOMB_WIRE_2  = 11;
	public static final int BOMB_WIRE_3  = 12;
	public static final int BIG_BUTTON   = 20;
	public static final int COM_BUTTON_1 = 30;
	public static final int COM_BUTTON_2 = 31;
	public static final int COM_BUTTON_3 = 32;
	public static final int COM_BUTTON_4 = 33;
	public static final int DOOR         = 40;
	public static final int DOOR_FRAME   = 41;
	public static final int FADE_EFFECT  = 90;

	public int type;

	public BombGameEntityTypeComponent(int type){
		this.type = type;
	}
}
