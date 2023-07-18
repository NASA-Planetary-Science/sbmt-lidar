package edu.jhuapl.sbmt.lidar.config;

import java.util.Date;
import java.util.Map;

import edu.jhuapl.saavtk.config.ViewConfig;
import edu.jhuapl.sbmt.core.body.BodyViewConfig;
import edu.jhuapl.sbmt.core.config.BaseFeatureConfigIO;
import edu.jhuapl.sbmt.core.config.Instrument;

import crucible.crust.metadata.api.Key;
import crucible.crust.metadata.api.Metadata;
import crucible.crust.metadata.api.Version;
import crucible.crust.metadata.impl.SettableMetadata;

public class LidarInstrumentConfigIO extends BaseFeatureConfigIO //extends BaseInstrumentConfigIO implements MetadataManager
{

	final Key<Boolean> hasLidarData = Key.of("hasLidarData");
    final Key<Boolean> hasHypertreeBasedLidarSearch = Key.of("hasHypertreeBasedLidarSearch");

    final Key<Boolean> lidarBrowseIntensityEnabled = Key.of("lidarBrowseIntensityEnabled");
    final Key<Long> lidarSearchDefaultStartDate = Key.of("lidarSearchDefaultStartDate");
    final Key<Long> lidarSearchDefaultEndDate = Key.of("lidarSearchDefaultEndDate");

    final Key<Map> lidarSearchDataSourceMap = Key.of("lidarSearchDataSourceMap");
    final Key<Map> lidarBrowseDataSourceMap = Key.of("lidarBrowseDataSourceMap");
    final Key<Map> lidarBrowseWithPointsDataSourceMap = Key.of("lidarBrowseWithPointsDataSourceMap");

    final Key<Map> lidarSearchDataSourceTimeMap = Key.of("lidarSearchDataSourceTimeMap");
    final Key<int[]> lidarBrowseXYZIndices = Key.of("lidarBrowseXYZIndices");
    final Key<int[]> lidarBrowseSpacecraftIndices = Key.of("lidarBrowseSpacecraftIndices");

    final Key<Boolean> lidarBrowseIsSpacecraftInSphericalCoordinates = Key.of("lidarBrowseIsSpacecraftInSphericalCoordinates");
    final Key<Boolean> lidarBrowseIsLidarInSphericalCoordinates = Key.of("lidarBrowseIsLidarInSphericalCoordinates");
    final Key<Boolean> lidarBrowseIsRangeExplicitInData = Key.of("lidarBrowseIsRangeExplicitInData");
    final Key<Boolean> lidarBrowseIsTimeInET = Key.of("lidarBrowseIsTimeInET");
    final Key<Integer> lidarBrowseRangeIndex = Key.of("lidarBrowseRangeIndex");

    final Key<Integer> lidarBrowseTimeIndex = Key.of("lidarBrowseTimeIndex");
    final Key<Integer> lidarBrowseNoiseIndex = Key.of("lidarBrowseNoiseIndex");
    final Key<Integer> lidarBrowseOutgoingIntensityIndex = Key.of("lidarBrowseOutgoingIntensityIndex");
    final Key<Integer> lidarBrowseReceivedIntensityIndex = Key.of("lidarBrowseReceivedIntensityIndex");
    final Key<String> lidarBrowseFileListResourcePath = Key.of("lidarBrowseFileListResourcePath");
    final Key<Integer> lidarBrowseNumberHeaderLines = Key.of("lidarBrowseNumberHeaderLines");
    final Key<Boolean> lidarBrowseIsInMeters = Key.of("lidarBrowseIsInMeters");
    final Key<Double> lidarOffsetScale = Key.of("lidarOffsetScale");
    final Key<Boolean> lidarBrowseIsBinary = Key.of("lidarBrowseIsBinary");
    final Key<Integer> lidarBrowseBinaryRecordSize = Key.of("lidarBrowseBinaryRecordSize");
    final Key<String> lidarInstrumentName = Key.of("lidarInstrumentName");

    private String metadataVersion = "1.0";



	public LidarInstrumentConfigIO()
	{

	}

	public LidarInstrumentConfigIO(String metadataVersion, ViewConfig config)
	{
		this.metadataVersion = metadataVersion;
		this.viewConfig = config;
	}



	@Override
	public void retrieve(Metadata configMetadata)
	{
		featureConfig = new LidarInstrumentConfig((BodyViewConfig)viewConfig);
		LidarInstrumentConfig c = (LidarInstrumentConfig)featureConfig;

        c.hasLidarData = read(hasLidarData, configMetadata);
        c.hasHypertreeBasedLidarSearch = read(hasHypertreeBasedLidarSearch, configMetadata);
        if (c.hasLidarData)
        {
        	c.lidarBrowseIntensityEnabled = read(lidarBrowseIntensityEnabled, configMetadata);
	        Long lidarSearchDefaultStart = read(lidarSearchDefaultStartDate, configMetadata);
	        if (lidarSearchDefaultStart == null) lidarSearchDefaultStart = 0L;
	        c.lidarSearchDefaultStartDate = new Date(lidarSearchDefaultStart);
	        Long lidarSearchDefaultEnd = read(lidarSearchDefaultEndDate, configMetadata);
	        if (lidarSearchDefaultEnd == null) lidarSearchDefaultEnd = 0L;
	        c.lidarSearchDefaultEndDate = new Date(lidarSearchDefaultEnd);
	        c.lidarSearchDataSourceMap = read(lidarSearchDataSourceMap, configMetadata);
	        c.lidarBrowseDataSourceMap = read(lidarBrowseDataSourceMap, configMetadata);
	        if (configMetadata.hasKey(lidarBrowseWithPointsDataSourceMap))
	        	c.lidarBrowseWithPointsDataSourceMap = read(lidarBrowseWithPointsDataSourceMap, configMetadata);

	        c.lidarSearchDataSourceTimeMap = read(lidarSearchDataSourceTimeMap, configMetadata);
	        //TODO PUT IN OREX SPECIFIC CONFIG
//	        c.orexSearchTimeMap = read(orexSearchTimeMap, configMetadata);

	        c.lidarBrowseXYZIndices = read(lidarBrowseXYZIndices, configMetadata);
	        c.lidarBrowseSpacecraftIndices = read(lidarBrowseSpacecraftIndices, configMetadata);
	        c.lidarBrowseIsLidarInSphericalCoordinates = read(lidarBrowseIsLidarInSphericalCoordinates, configMetadata);
	        c.lidarBrowseIsSpacecraftInSphericalCoordinates = read(lidarBrowseIsSpacecraftInSphericalCoordinates, configMetadata);
	        c.lidarBrowseIsRangeExplicitInData = read(lidarBrowseIsRangeExplicitInData, configMetadata);
	        c.lidarBrowseRangeIndex = read(lidarBrowseRangeIndex, configMetadata);

	        c.lidarBrowseIsTimeInET = read(lidarBrowseIsTimeInET, configMetadata);
	        c.lidarBrowseTimeIndex = read(lidarBrowseTimeIndex, configMetadata);
	        c.lidarBrowseNoiseIndex = read(lidarBrowseNoiseIndex, configMetadata);
	        c.lidarBrowseOutgoingIntensityIndex = read(lidarBrowseOutgoingIntensityIndex, configMetadata);
	        c.lidarBrowseReceivedIntensityIndex = read(lidarBrowseReceivedIntensityIndex, configMetadata);
	        c.lidarBrowseFileListResourcePath = read(lidarBrowseFileListResourcePath, configMetadata);
	        c.lidarBrowseNumberHeaderLines = read(lidarBrowseNumberHeaderLines, configMetadata);
	        c.lidarBrowseIsInMeters = read(lidarBrowseIsInMeters, configMetadata);
	        c.lidarBrowseIsBinary = read(lidarBrowseIsBinary, configMetadata);
	        c.lidarBrowseBinaryRecordSize = read(lidarBrowseBinaryRecordSize, configMetadata);
	        c.lidarOffsetScale = read(lidarOffsetScale, configMetadata);
	        c.lidarInstrumentName = Instrument.valueOf(""+read(lidarInstrumentName, configMetadata));

        }

	}

	@Override
    public Metadata store()
    {
        SettableMetadata result = SettableMetadata.of(Version.of(metadataVersion));
        storeConfig(result);
        return result;
    }

	private SettableMetadata storeConfig(SettableMetadata configMetadata)
    {
		LidarInstrumentConfig c = (LidarInstrumentConfig)featureConfig;
        write(hasLidarData, c.hasLidarData, configMetadata);
        write(hasHypertreeBasedLidarSearch, c.hasHypertreeBasedLidarSearch, configMetadata);
//        write(hasMapmaker, c.hasMapmaker, configMetadata);
		write(lidarBrowseIntensityEnabled, c.lidarBrowseIntensityEnabled, configMetadata);
        writeDate(lidarSearchDefaultStartDate, c.lidarSearchDefaultStartDate, configMetadata);
        writeDate(lidarSearchDefaultEndDate, c.lidarSearchDefaultEndDate, configMetadata);
        write(lidarSearchDataSourceMap, c.lidarSearchDataSourceMap, configMetadata);
        write(lidarBrowseDataSourceMap, c.lidarBrowseDataSourceMap, configMetadata);
        if (lidarBrowseWithPointsDataSourceMap != null)
        	write(lidarBrowseWithPointsDataSourceMap, c.lidarBrowseWithPointsDataSourceMap, configMetadata);

        write(lidarSearchDataSourceTimeMap, c.lidarSearchDataSourceTimeMap, configMetadata);

        write(lidarBrowseXYZIndices, c.lidarBrowseXYZIndices, configMetadata);
        write(lidarBrowseSpacecraftIndices, c.lidarBrowseSpacecraftIndices, configMetadata);
        write(lidarBrowseIsLidarInSphericalCoordinates, c.lidarBrowseIsLidarInSphericalCoordinates, configMetadata);
        write(lidarBrowseIsSpacecraftInSphericalCoordinates, c.lidarBrowseIsSpacecraftInSphericalCoordinates, configMetadata);
        write(lidarBrowseIsRangeExplicitInData, c.lidarBrowseIsRangeExplicitInData, configMetadata);
        write(lidarBrowseRangeIndex, c.lidarBrowseRangeIndex, configMetadata);

        write(lidarBrowseIsTimeInET, c.lidarBrowseIsTimeInET, configMetadata);
        write(lidarBrowseTimeIndex, c.lidarBrowseTimeIndex, configMetadata);
        write(lidarBrowseNoiseIndex, c.lidarBrowseNoiseIndex, configMetadata);
        write(lidarBrowseOutgoingIntensityIndex, c.lidarBrowseOutgoingIntensityIndex, configMetadata);
        write(lidarBrowseReceivedIntensityIndex, c.lidarBrowseReceivedIntensityIndex, configMetadata);
        write(lidarBrowseFileListResourcePath, c.lidarBrowseFileListResourcePath, configMetadata);
        write(lidarBrowseNumberHeaderLines, c.lidarBrowseNumberHeaderLines, configMetadata);
        write(lidarBrowseIsInMeters, c.lidarBrowseIsInMeters, configMetadata);
        write(lidarBrowseIsBinary, c.lidarBrowseIsBinary, configMetadata);
        write(lidarBrowseBinaryRecordSize, c.lidarBrowseBinaryRecordSize, configMetadata);
        write(lidarOffsetScale, c.lidarOffsetScale, configMetadata);
        writeEnum(lidarInstrumentName, c.lidarInstrumentName, configMetadata);

        return configMetadata;
	}
}
