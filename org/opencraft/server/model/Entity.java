/*
 * Jacob_'s Capture the Flag for Minecraft Classic and ClassiCube
 * Copyright (c) 2010-2014 Jacob Morgan
 * Based on OpenCraft v0.2
 *
 * OpenCraft License
 * 
 * Copyright (c) 2009 Graham Edgecombe, S�ren Enevoldsen and Brett Russell.
 * All rights reserved.
 *
 * Distribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Distributions of source code must retain the above copyright notice,
 *       this list of conditions and the following disclaimer.
 *       
 *     * Distributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *       
 *     * Neither the name of the OpenCraft nor the names of its
 *       contributors may be used to endorse or promote products derived from
 *       this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package org.opencraft.server.model;


import java.util.HashSet;
import java.util.Set;

/**
 * The superclass for players and mobs.
 * @author Graham Edgecombe
 */
public abstract class Entity {
	
	/**
	 * A collection of local entities.
	 */
	private final Set<Entity> localEntities = new HashSet<Entity>();
	
	/**
	 * The old position.
	 */
	private Position oldPosition;
	
	/**
	 * The current position.
	 */
	private Position position;
	
	/**
	 * The old rotation.
	 */
	private Rotation oldRotation;
	
	/**
	 * The current rotation.
	 */
	private Rotation rotation;
	
	/**
	 * The current id.
	 */
	private int id = -1;
	
	/**
	 * The old id.
	 */
	private int oldId = -1;
        
        public Position goodPosition;
	
	/**
	 * Default public constructor.
	 */
	public Entity() {
		position = new Position(0, 0, 0);
		rotation = new Rotation(0, 0);
		resetOldPositionAndRotation();
	}
	
	/**
	 * Gets the local entity set.
	 * @return The local entity set.
	 */
	public Set<Entity> getLocalEntities() {
		return localEntities;
	}
	
	/**
	 * Gets the id.
	 * @return The id.
	 */
	public int getId() {
		return id;
	}
	
	/**
	 * Sets the id.
	 * @param id The id.
	 */
	public void setId(int id) {
                if(id != -1)
                    this.oldId = id;
		this.id = id;
	}
	
	/**
	 * Gets the old id.
	 * @return The old id.
	 */
	public int getOldId() {
		return oldId;
	}
	
	/**
	 * Sets the rotation.
	 * @param rotation The rotation.
	 */
	public void setRotation(Rotation rotation) {
		this.rotation = rotation;
	}
	
	/**
	 * Gets the rotation.
	 * @return The rotation.
	 */
	public Rotation getRotation() {
		return rotation;
	}
	
	/**
	 * Sets the position.
	 * @param position The position.
	 */
	public void setPosition(Position position) {
		this.position = position;
	}
	
	/**
	 * Gets the position.
	 * @return The position.
	 */
	public Position getPosition() {
            return position;
	}
	
	/**
	 * Gets the old position.
	 * @return The old position.
	 */
	public Position getOldPosition() {
		return oldPosition;
	}
	
	/**
	 * Gets the old rotation.
	 * @return The old rotation.
	 */
	public Rotation getOldRotation() {
		return oldRotation;
	}
	
	/**
	 * Resets the old position and rotation data.
	 */
	public void resetOldPositionAndRotation() {
		oldPosition = position;
		oldRotation = rotation;
	}
	
	/**
	 * Gets the name of this entity.
	 * @return The name of this entity.
	 */
	public abstract String getName();

        public abstract String getColoredName();
	
}
