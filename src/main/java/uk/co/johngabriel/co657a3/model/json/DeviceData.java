package uk.co.johngabriel.co657a3.model.json;

import java.util.ArrayList;

/**
 * A set of readings taken by a device of a specific type, and with specific regularity.
 * @author John Gabriel
 */
public class DeviceData {
	private String id;
	private String name;
	private Site site;
	private Zone zone;
	private String softwareVersion;
	private DataType type;
	private DataResolution resolution;
	private String scale;
	private ArrayList<Reading> readings;
	
	public DeviceData(String id, String name, Site site, Zone zone, String softwareVersion, DataType type,
			DataResolution resolution, String scale) {
		this.id = id;
		this.name = name;
		this.site = site;
		this.zone = zone;
		this.softwareVersion = softwareVersion;
		this.type = type;
		this.resolution = resolution;
		this.scale = scale;
		readings = new ArrayList<>();
	}

	public String getId() { return id; }
	public String getName() { return name; }
	public Site getSite() { return site; }
	public Zone getZone() { return zone; }
	public String getSoftwareVersion() { return softwareVersion; }
	public DataType getType() { return type; }
	public DataResolution getResolution() { return resolution; }
	public String getScale() { return scale; }
	public ArrayList<Reading> getReadings() { return readings; }
	
	public void addReading(Reading reading) { readings.add(reading); }
	
	/**
	 * @return The latest reading, if there are any; null otherwise.
	 */
	public Reading getLatestReading() {
		return readings.stream()
				.sorted((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()))
				.findFirst()
				.orElse(null);
	}
	
}
