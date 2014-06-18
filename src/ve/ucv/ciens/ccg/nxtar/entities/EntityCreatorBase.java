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
package ve.ucv.ciens.ccg.nxtar.entities;

import ve.ucv.ciens.ccg.nxtar.NxtARCore;
import ve.ucv.ciens.ccg.nxtar.interfaces.ApplicationEventsListener;

import com.artemis.World;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.utils.Disposable;

/**
 * 
 */
public abstract class EntityCreatorBase implements Disposable{
	protected World                     world              = null;
	protected ApplicationEventsListener core               = null;
	protected boolean                   entitiesCreated    = false;
	protected AssetManager              manager            = null;

	/**
	 * <p>Sets the Artemis {@link World} to use to create entities.</p>
	 * 
	 * @param world The Artemis {@link World}.
	 * @throws IllegalArgumentException if world is null.
	 */
	public final void setWorld(World world) throws IllegalArgumentException{
		if(world == null)
			throw new IllegalArgumentException("World cannot be null.");

		this.world = world;
	}

	/**
	 * <p>Sets the application core to listen for asset loading events.</p>
	 * 
	 * @param core The application core to be used as listener.
	 * @throws IllegalArgumentException if core is null.
	 */
	public final void setCore(NxtARCore core) throws IllegalArgumentException{
		if(core == null) throw new IllegalArgumentException("Core is null.");
		this.core = core;
	}

	/**
	 * <p> Updates the state of the {@link AssetManager}.</p>
	 * 
	 * @return true if the {@link AssetManager} has finished loading.
	 */
	public abstract boolean updateAssetManager();

	/**
	 * <p>Unloads all assets loaded for the scenario.</p>
	 */
	public abstract void dispose();

	/**
	 * @return true if the createAllEntities method has been called.
	 */
	public boolean areEntitiesCreated(){
		return entitiesCreated;
	}

	/**
	 * <p>Creates all entities for a game scenario.</p>
	 */
	protected abstract void createAllEntities();
}
