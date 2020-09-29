package edu.jhuapl.sbmt.lidar;

import com.google.common.collect.ImmutableSet;

import edu.jhuapl.saavtk.feature.FeatureType;

/**
 * Utility class that defines the available lidar based {@link FeatureType}s.
 *
 * @author lopeznr1
 */
public class LidarFeatureType
{
	// Constants
	public static final FeatureType Time = new FeatureType("Time", null, 1.0);

	public static final FeatureType Radius = new FeatureType("Radius", null, 1.0);

	public static final FeatureType Range = new FeatureType("Spacecraft Range", null, 1.0);

	public static final FeatureType Intensity = new FeatureType("Intensity", null, 1.0);

	/** Provides access to all of the available lidar {@link FeatureType}s. */
	public static final ImmutableSet<FeatureType> FullSet = ImmutableSet.of(Time, Radius, Range, Intensity);
}
