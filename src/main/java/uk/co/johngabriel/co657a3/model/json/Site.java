package uk.co.johngabriel.co657a3.model.json;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.co.johngabriel.co657a3.model.Ideal;

/**
 * Representation of a site on the farm.
 * @author John Gabriel
 */
public class Site {
	private static final Logger LOG = LoggerFactory.getLogger(Site.class);
	
	private String id;
	private String name;
	private double latitude;
	private double longitude;
	private double altitude;
	private String countryCode;
	private String timezone;
	private ArrayList<Device> devices;
	private HashMap<String, Zone> zones;
	private HashMap<DataType, Ideal> ideals;
	
	public Site(String id, String name, double longitude, double latitude, double altitude, String countryCode,
			String timezone) {
		this.id = id;
		this.name = name;
		this.longitude = longitude;
		this.latitude = latitude;
		this.altitude = altitude;
		this.countryCode = countryCode;
		this.timezone = timezone;
		devices = new ArrayList<>();
		zones = new HashMap<>();
		ideals = new HashMap<>();
	}

	public String getId() { return id; }
	public String getName() { return name; }
	public double getLongitude() { return longitude; }
	public double getLatitude() { return latitude; }
	public double getAltitude() { return altitude; }
	public String getCountryCode() { return countryCode; }
	public String getTimezone() { return timezone; }
	public ArrayList<Device> getDevices() { return devices; }
	public ArrayList<Zone> getZones() { return new ArrayList<Zone>(zones.values()); }
	public Zone getZone(String id) { return zones.get(id); }
	public Ideal getIdeal(DataType type) { return ideals.get(type); }
	
	public void addDevice(Device device) { devices.add(device); }
	public void addZone(String id, Zone zone) { zones.put(id, zone); }
	public void setIdeal(DataType type, Ideal ideal) { ideals.put(type, ideal); }
	
	/**
	 * @return Whether the site has any data of the given type.
	 */
	public boolean hasData(DataType type) {
		return zones.values().stream()
				.anyMatch(i -> i.hasData(type));
	}
	
	/**
	 * @return What data types this site gathers.
	 */
	public List<DataType> getTypes() {
		return Arrays.asList(DataType.values()).stream()
				.filter(i -> hasData(i))
				.collect(Collectors.toList());
	}
	
	/**
	 * @return A list of all the timestamps for which we have data, rounded to the nearest minute.
	 */
	public List<LocalDateTime> getTimestamps() {
		HashSet<LocalDateTime> timestamps = new HashSet<>();
		for (Zone next: zones.values())
			timestamps.addAll(next.getTimestamps());
		return timestamps.stream()
				.sorted((a, b) -> b.compareTo(a))
				.collect(Collectors.toList());
	}
	
	/**
	 * @return The historical average reading of the given type across this site, if it can; -1 otherwise.
	 */
	public double getAverageReading(DataType type) {
		return zones.values().stream()
				.filter(i -> i.hasData(type))
				.mapToDouble(i -> i.getAverageReading(type))
				.filter(i -> i != -1)
				.average()
				.orElse(-1);
	}
	
	/**
	 * @return The historical average reading of the given type across this site, if it can; -1 otherwise.
	 */
	public double getAverageReadingRaw(DataType type) {
		return zones.values().stream()
				.filter(i -> i.hasData(type))
				.mapToDouble(i -> i.getAverageReadingRaw(type))
				.filter(i -> i != -1)
				.average()
				.orElse(-1);
	}
	
	/**
	 * @return The current average reading of the given type across this site, if it can; -1 otherwise.
	 */
	public double getCurrentReading(DataType type) {
		return zones.values().stream()
				.filter(i -> i.hasData(type))
				.mapToDouble(i -> i.getCurrentReading(type))
				.filter(i -> i != -1)
				.average()
				.orElse(-1);
	}
	
	/**
	 * @return The current average reading of the given type across this site, if it can; -1 otherwise.
	 */
	public double getCurrentReadingRaw(DataType type) {
		return zones.values().stream()
				.filter(i -> i.hasData(type))
				.mapToDouble(i -> i.getCurrentReadingRaw(type))
				.filter(i -> i != -1)
				.average()
				.orElse(-1);
	}
	
	/**
	 * @return The reading (adjusted to scale) that is closest to the given timestamp, of the given type, if it exists; -1 otherwise.
	 */
	public double getReadingClosestToDateTime(DataType type, LocalDateTime time, DataResolution resolution) {
		return devices.stream()
				.filter(i -> hasData(type))
				.filter(i -> i.hasReadingCloseToDateTime(type, time, resolution))
				.map(i -> i.getReadingClosestToDateTime(type, time, resolution))
				.filter(i -> i != null)
				.mapToDouble(i -> i.getReading())
				.filter(i -> i != -1)
				.average()
				.orElse(-1);
	}
	
	/**
	 * @return The standard deviation of all readings of the given type, if it can; -1 otherwise.
	 */
	public double getStandardDeviation(DataType type) {
		double average = getAverageReading(type);
		if (average == -1) return -1;
		return devices.stream()
				.filter(i -> i.hasData(type))
				.mapToDouble(i -> i.getStandardDeviation(type))
				.average()
				.orElse(-1);
	}
	
	/**
	 * @return The standard deviation of all readings of the given type, if it can; -1 otherwise.
	 */
	public double getStandardDeviationRaw(DataType type) {
		double average = getAverageReading(type);
		if (average == -1) return -1;
		return devices.stream()
				.filter(i -> i.hasData(type))
				.mapToDouble(i -> i.getStandardDeviationRaw(type))
				.average()
				.orElse(-1);
	}
	
	/**
	 * @return The LocalDateTime of the latest reading of the given type, if possible; null otherwise.
	 */
	public LocalDateTime getTimeOfLastReading(DataType type) {
		return devices.stream()
				.filter(i -> i.hasData(type))
				.map(i -> i.getTimeOfLastReading(type))
				.sorted((a, b) -> b.compareTo(a))
				.findFirst()
				.orElse(null);
	}
	
	/**
	 * @return The Site's status with regards to the given DataType, if it can; null otherwise.
	 */
	public IdealStatus getStatus(DataType type) {
		// First, do we even do data for this DataType?
		if (!hasData(type)) return IdealStatus.DOES_NOT_SERVE;
		// Do we have a time of last reading?
		LocalDateTime last = getTimeOfLastReading(type);
		//if (last == null) return IdealStatus.UNKNOWN;
		// Is that recent enough?
		if (last.isBefore(LocalDateTime.now().minusMinutes(5)))
			return IdealStatus.LATE;
		// Okay, so do we know what sort of boundaries we should be in?
		Ideal ideal = ideals.get(type);
		double average = getAverageReadingRaw(type);
		double sd = getStandardDeviationRaw(type);
		double current = getCurrentReadingRaw(type);
		double sdMulti = type.getSDMultiplier();
		if ((current > average + (sdMulti*sd)) || (current < average - (sdMulti*sd))) return IdealStatus.EXTREME;
		else if (ideal == null) return IdealStatus.UNKNOWN;
		else if (current > ideal.getMax()) return IdealStatus.OVER;
		else if (current < ideal.getMin()) return IdealStatus.UNDER;
		else return IdealStatus.OKAY;
	}
	
	/**
	 * @return Whether the Site, as a whole, is okay.
	 */
	public boolean isOkay() {
		for (DataType type: DataType.values()) {
			IdealStatus status = getStatus(type);
			if (status == IdealStatus.OVER || status == IdealStatus.UNDER || status == IdealStatus.LATE)
				return false;
		}
		return true;
	}
}
