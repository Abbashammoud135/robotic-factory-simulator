package fr.tp.inf112.projects.robotsim.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import fr.tp.inf112.projects.canvas.model.Style;
import fr.tp.inf112.projects.canvas.model.impl.RGBColor;
import fr.tp.inf112.projects.robotsim.model.motion.Motion;
import fr.tp.inf112.projects.robotsim.model.path.FactoryPathFinder;
import fr.tp.inf112.projects.robotsim.model.shapes.CircularShape;
import fr.tp.inf112.projects.robotsim.model.shapes.PositionedShape;
import fr.tp.inf112.projects.robotsim.model.shapes.RectangularShape;

public class Robot extends Component {
	
	private static final long serialVersionUID = -1218857231970296747L;

	private static final Style STYLE = new ComponentStyle(RGBColor.GREEN, RGBColor.BLACK, 3.0f, null);

	private static final Style BLOCKED_STYLE = new ComponentStyle(RGBColor.RED, RGBColor.BLACK, 3.0f, new float[]{4.0f});

	private final Battery battery;
	
	private int speed;
	
	private List<Component> targetComponents;
	
	private transient Iterator<Component> targetComponentsIterator;
	
	private Component currTargetComponent;
	
	private transient Iterator<Position> currentPathPositionsIter;
	
	private transient boolean blocked;

	private Position blockedTargetPosition;
	
	private FactoryPathFinder pathFinder;

	public Robot(final Factory factory,
				 final FactoryPathFinder pathFinder,
				 final CircularShape shape,
				 final Battery battery,
				 final String name ) {
		super(factory, shape, name);
		
		this.pathFinder = pathFinder;
		
		this.battery = battery;
		
		targetComponents = new ArrayList<>();
		currTargetComponent = null;
		currentPathPositionsIter = null;
		speed = 5;
		setBlocked(false);
		blockedTargetPosition = null;
	}

	@Override
	public String toString() {
		return super.toString() + " battery=" + battery + "]";
	}

	protected int getSpeed() {
		return speed;
	}

	protected void setSpeed(final int speed) {
		this.speed = speed;
	}
	
	public Position getBlockedTargetPosition() {
		return blockedTargetPosition;
	}
	
	private List<Component> getTargetComponents() {
		if (targetComponents == null) {
			targetComponents = new ArrayList<>();
		}
		
		return targetComponents;
	}
	
	public boolean addTargetComponent(final Component targetComponent) {
		return getTargetComponents().add(targetComponent);
	}
	
	public boolean removeTargetComponent(final Component targetComponent) {
		return getTargetComponents().remove(targetComponent);
	}
	
	@Override
	public boolean isMobile() {
		return true;
	}
	
	public boolean isBlocked() {
		return blocked;
	}

	public void setBlocked(boolean blocked) {
		if ( this.blocked != blocked) {
			this.blocked = blocked;
			notifyObservers();
		}
	}

	@Override
	public boolean behave() {
		if (getTargetComponents().isEmpty()) {
			return false;
		}
		
		if (currTargetComponent == null || hasReachedCurrentTarget()) {
			currTargetComponent = nextTargetComponentToVisit();
			
			computePathToCurrentTargetComponent();
		}
		//computePathToCurrentTargetComponent();

		return moveToNextPathPosition() != 0;
	}
		
	private Component nextTargetComponentToVisit() {
		if (targetComponentsIterator == null || !targetComponentsIterator.hasNext()) {
			targetComponentsIterator = getTargetComponents().iterator();
		}
		
		return targetComponentsIterator.hasNext() ? targetComponentsIterator.next() : null;
	}
	
	private int moveToNextPathPosition() { 
	    final Motion motion = computeMotion(); 
	   
	    int displacement = motion == null ? 0 : motion.moveToTarget(); 
	     
	    if (displacement != 0) { 
	        notifyObservers(); 
	    } 
	    else if (isLivelyLocked()) { 
	        final Position freeNeighbouringPosition = findFreeNeighbouringPosition(); 
	     
	        if (freeNeighbouringPosition != null) { 
	            blockedTargetPosition = freeNeighbouringPosition; 
	            displacement = moveToNextPathPosition(); 
	            computePathToCurrentTargetComponent(); 
	        } 
	    } 
	 
	    return displacement; 
	}
	
	private void computePathToCurrentTargetComponent() {
		final List<Position> currentPathPositions = pathFinder.findPath(this, currTargetComponent);
		currentPathPositionsIter = currentPathPositions.iterator();
	}
	
	
	
	
	
	private Motion computeMotion() {
		if (!currentPathPositionsIter.hasNext()) {
	        computePathToCurrentTargetComponent();  // try once more — obstruction may have cleared

	        if (!currentPathPositionsIter.hasNext()) {
	            setBlocked(true);
	            return null;
	        }
	    }
		
		setBlocked(false);  // a valid path exists, so we're not actually blocked , (i had to add this because at some point the robot turned red but was still moving, and tuned out we didnt have anything that sets the color back to green if found a path

		final Position targetPosition = getTargetPosition();
		final PositionedShape shape = new RectangularShape(targetPosition.getxCoordinate(),
														   targetPosition.getyCoordinate(),
				   										   2,
				   										   2);
		
		// If there is another robot, memorize the blocked target position for the next run
		if (getFactory().hasMobileComponentAt(shape, this)) {
			this.blockedTargetPosition = targetPosition;
			
			return null;
		}

		// Reset the memorized position
		this.blockedTargetPosition = null;
			
		return new Motion(getPosition(), targetPosition);
	}
	
	private Position getTargetPosition() {
		// If a target position was memorized, it means that the robot was blocked during the last iteration 
		// so it waited for another robot to pass. So try to move to this memorized position otherwise move to  
		// the next position from the path
		return this.blockedTargetPosition == null ? currentPathPositionsIter.next() : this.blockedTargetPosition;
	}
	
	private boolean hasReachedCurrentTarget() {
		return getPositionedShape().overlays(currTargetComponent.getPositionedShape());
	}
	
	@Override
	public boolean canBeOverlayed(final PositionedShape shape) {
		return true;
	}
	
	@Override
	public Style getStyle() {
		return isBlocked() ? BLOCKED_STYLE : STYLE;
	}
	
	
	// added by me
	public boolean isLivelyLocked() {
		 final Position blockedTargetPosition = getBlockedTargetPosition();
		 if (blockedTargetPosition == null) {
		 return false;
		 }
		 final Component otherRobot =
		 getFactory().getMobileComponentAt(blockedTargetPosition, this);
		 return otherRobot != null &&
		 getPosition().equals(((Robot) otherRobot).getBlockedTargetPosition());
		}
	
	
	public Position findFreeNeighbouringPosition() {
	
		final int resolution = getFactory().getPathResolution();
		final Position current = getPosition();
		Position right = new Position(current.getxCoordinate() + resolution, current.getyCoordinate());
		Position left = new Position(current.getxCoordinate() - resolution, current.getyCoordinate());
		Position up = new Position(current.getxCoordinate(), current.getyCoordinate() + resolution);
		Position down = new Position(current.getxCoordinate(), current.getyCoordinate() - resolution);
		
		// check for each position if it has obstacle or if there is someone in it 1st
		// if it's good (nothing of these then add the element to the list and then shuffle the positions
		// so that we decrease the possibility of both of robots to go the same free position
		
		ArrayList<Position> positions =  new ArrayList<>();
		
		 if (getFactory().getMobileComponentAt(right, this) == null && !(getFactory().hasObstacleAt(new RectangularShape(right.getxCoordinate(), right.getyCoordinate(), resolution, resolution)))) {
		        positions.add(right);
		    }
		    if (getFactory().getMobileComponentAt(left, this) == null && !(getFactory().hasObstacleAt(new RectangularShape(left.getxCoordinate(), left.getyCoordinate(), resolution, resolution)))) {
		        positions.add(left);
		    }
		    if (getFactory().getMobileComponentAt(down, this) == null && !(getFactory().hasObstacleAt(new RectangularShape(down.getxCoordinate(), down.getyCoordinate(), resolution, resolution)))) {
		        positions.add(down);
		    }
		    if (getFactory().getMobileComponentAt(up, this) == null && !(getFactory().hasObstacleAt(new RectangularShape(up.getxCoordinate(), up.getyCoordinate(), resolution, resolution)))) {
		        positions.add(up);
		    }
		
		if (positions.isEmpty()) { return null; }
		Collections.shuffle(positions);
		return positions.get(0);
		
		
		
	}
	

}
