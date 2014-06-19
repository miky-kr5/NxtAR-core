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
package ve.ucv.ciens.ccg.nxtar.components;

public class BombGamePlayerComponent extends PlayerComponentBase {
	public static final int MIN_LIVES = 1;
	public static final int MAX_LIVES = 5;

	private int startingLives;
	public  int     lives;
	public  int     disabledBombs;

	public BombGamePlayerComponent(int lives) throws IllegalArgumentException{
		super();

		if(lives < MIN_LIVES || lives > MAX_LIVES)
			throw new IllegalArgumentException("Lives number out of range: " + Integer.toString(lives));

		startingLives = lives;
		reset();
	}

	public BombGamePlayerComponent(){
		this(3);
	}
	
	@Override
	public void reset(){
		super.reset();

		this.lives = startingLives;
		this.disabledBombs = 0;
	}
}
