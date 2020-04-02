package edu.jhuapl.sbmt.lidar.gui.color;

/**
 * Interface that defines the methods to allow configuration of ColorProviders
 * (used to render lidar data).
 *
 * @author lopeznr1
 */
public interface LidarColorConfigPanel
{
	/**
	 * Notifies the panel of its active state.
	 */
	public abstract void activate(boolean aIsActive);

	/**
	 * Returns the GroupColorProvider that should be used to color lidar data
	 * points associated with the source (spacecraft).
	 */
	public abstract GroupColorProvider getSourceGroupColorProvider();

	/**
	 * Returns the GroupColorProvider that should be used to color lidar data
	 * points associated with the target.
	 */
	public abstract GroupColorProvider getTargetGroupColorProvider();

}
