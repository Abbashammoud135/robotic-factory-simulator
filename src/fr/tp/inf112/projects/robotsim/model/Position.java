package fr.tp.inf112.projects.robotsim.model;

import java.io.Serializable;

//added by me
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Position implements Serializable {

	private static final long serialVersionUID = 7274819087013715987L;

	private int xCoordinate;

	private int yCoordinate;

	
	// added by me
	// Read Lock
	private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
	Lock readLock = lock.readLock();
	
	// Write Lock
	Lock writeLock = lock.writeLock();
	
	
	public Position(final int xCoordinate, 
					final int yCoordinate) {
		super();

		this.xCoordinate = xCoordinate;
		this.yCoordinate = yCoordinate;
	}

	public int getxCoordinate() {
		try {
			readLock.lock();

			return xCoordinate;
		}
		finally {
			readLock.unlock();
		}
	}

	public int getyCoordinate() {
		try {
			readLock.lock();

			return yCoordinate;
		}
		finally {
			readLock.unlock();
		}
	}

	public boolean setxCoordinate(final int xCoordinate) {
		 try {
		        writeLock.lock();
		        
				if (this.xCoordinate == xCoordinate) {
					return false;
				}
				
				this.xCoordinate = xCoordinate;
				
				return true;
			}
		 finally {
		        writeLock.unlock();
		 }
	}

	public boolean setyCoordinate(final int yCoordinate) {
		try {
				writeLock.lock();
				if (this.yCoordinate == yCoordinate) {
				return false;
				}
				
				this.yCoordinate = yCoordinate;
				
				return true;
		}
		
		finally {
	        writeLock.unlock();
		}
	}
	
	@Override
	public boolean equals(final Object objectToCompare) {
		if (objectToCompare == null) {
			return false;
		}
		
		final Position position = (Position) objectToCompare;
		
		return getxCoordinate() == position.getxCoordinate() && getyCoordinate() == position.getyCoordinate();
	}
	
	@Override
	public String toString() {
		final StringBuilder strBuild = new StringBuilder("Position = (");
		strBuild.append(getxCoordinate());
		strBuild.append(", ");
		strBuild.append(getyCoordinate());
		strBuild.append(")");
		
		return strBuild.toString();
	}
}
