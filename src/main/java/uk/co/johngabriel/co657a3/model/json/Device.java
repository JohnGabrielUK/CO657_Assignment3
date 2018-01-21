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
import uk.co.johngabriel.co657a3.things.Prettyfier;

/**
 * Representation of a device on the farm.
 * @author John Gabriel
 */
public class Device {
	private static final Logger LOG = LoggerFactory.getLogger(Device.class);

	private String id;
	private String name;
	private DeviceType type;
	private Site site;
	private Zone zone;
	private LocalDateTime lastConnection;
	private String softwareVersion;
	private ArrayList<DeviceData> data;
	private HashMap<DataType, Ideal> ideals;

	public Device(String id, String name, DeviceType type, Site site, Zone zone, LocalDateTime lastConnection, String softwareVersion) {
		this.id = id;
		this.name = name;
		this.type = type;
		this.site = site;
		this.zone = zone;
		this.lastConnection = lastConnection;
		this.softwareVersion = softwareVersion;
		data = new ArrayList<>();
		ideals = new HashMap<>();
	}

	public String getId() { return id; }
	public String getName() { return name; }
	public DeviceType getType() { return type; }
	public Site getSite() { return site; }
	public Zone getZone() { return zone; }
	public LocalDateTime getLastConnection() { return lastConnection; }
	public String getSoftwareVersion() { return softwareVersion; }
	public ArrayList<DeviceData> getData() { return data; }
	public Ideal getIdeal(DataType type) { return ideals.get(type); }
	
	public void addData(DeviceData dataToAdd) { data.add(dataToAdd); }
	public void setIdeal(DataType type, Ideal ideal) { ideals.put(type, ideal); }
	
	/**
	 * @return Whether this device has any data of the given type.
	 */
	public boolean hasData(DataType type) {
		for (DeviceData next: data)
			if (next.getType() == type) return true;
		return false;
		//return data.stream().anyMatch(i -> i.getType() == type);
	}
	
	/**
	 * @return What data types this device gathers.
	 */
	public List<DataType> getTypes() {
		return Arrays.asList(DataType.values()).stream()
				.filter(i -> hasData(i))
				.collect(Collectors.toList());
	}
	
	/**
	 * @return The DeviceData of the given type and resolution, if it can; null otherwise.
	 */
	public DeviceData getDeviceData(DataType type, DataResolution resolution) {
		return data.stream()
				.filter(i ->  i.getType() == type)
				.filter(i -> i.getResolution() == resolution)
				.findFirst()
				.orElse(null);
	}
	
	/**
	 * @return A list of all the timestamps for which we have data, rounded to the nearest minute.
	 */
	public List<LocalDateTime> getTimestamps() {
		HashSet<LocalDateTime> timestamps = new HashSet<>();
		for (DataType next: getTypes()) {
			ArrayList<Reading> readings = getAllReadings(next);
			readings.parallelStream()
			.map(i -> Prettyfier.toNearestMinute(i.getTimestamp()))
			.forEach(i -> {
				timestamps.add(i);
			});
		}
		List<LocalDateTime> results = timestamps.stream()
				.collect(Collectors.toList());
		results.sort((a, b) -> b.compareTo(a));
		return results;
	}
	
	/**
	 * @return A list of all the timestamps for which we have data at the given resolution, rounded to the nearest minute.
	 */
	public List<LocalDateTime> getTimestamps(DataResolution resolution) {
		HashSet<LocalDateTime> timestamps = new HashSet<>();
		for (DataType next: getTypes()) {
			DeviceData data = getDeviceData(next, resolution);
			data.getReadings().parallelStream()
			.map(i -> Prettyfier.toNearestMinute(i.getTimestamp()))
			.forEach(i -> {
				timestamps.add(i);
			});
		}
		List<LocalDateTime> results = timestamps.stream()
				.collect(Collectors.toList());
		results.sort((a, b) -> b.compareTo(a));
		return results;
	}
	
	/**
	 * @return The latest reading of the given type, if it exists; null otherwise.
	 */
	public Reading getLatestReading(DataType type) {
		return data.stream()
				.filter(i -> i.getType() == type)
				.map(i -> i.getLatestReading())
				.sorted((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()))
				.findFirst()
				.orElse(null);
	}
	
	/**
	 * @return The reading at the given time, if it exists; null otherwise.
	 */
	public Reading getReadingAtDateTime(DataType type, DataResolution resolution, LocalDateTime time) {
		return getDeviceData(type, resolution).getReadings().stream()
				.filter(i -> i.getTimestamp().equals(time))
				.findFirst().orElse(null);
	}
	
	/**
	 * @return The reading at the given time, if it exists; null otherwise.
	 */
	public Reading getReadingAtDateTime(DataType type, LocalDateTime time) {
		return getAllReadings(type).stream()
				.filter(i -> i.getTimestamp().equals(time))
				.findFirst().orElse(null);
	}
	
	/**
	 * @return The reading within a minute of the given time, if it exists; null otherwise.
	 */
	public boolean hasReadingCloseToDateTime(DataType type, LocalDateTime time, DataResolution resolution) {
		if (!hasData(type)) return false;
		return getDeviceData(type, resolution).getReadings().stream()
				.anyMatch(i -> i.getTimestamp().isAfter(time.minusSeconds(30))
						&& i.getTimestamp().isBefore(time.plusSeconds(30)));
	}
	
	/**
	 * @return The reading within a minute of the given time, if it exists; null otherwise.
	 */
	public Reading getReadingClosestToDateTime(DataType type, LocalDateTime time, DataResolution resolution) {
		return getDeviceData(type, resolution).getReadings().stream()
				.filter(i -> i.getTimestamp().isAfter(time.minusSeconds(resolution.getMargin())))
				.filter(i -> i.getTimestamp().isBefore(time.plusSeconds(resolution.getMargin())))
				.findFirst().orElse(null);
	}
	
	/**
	 * @return The reading within a minute of the given time, if it exists; null otherwise.
	 */
	public Reading getReadingClosestToDateTime(DataType type, LocalDateTime time) {
		return getAllReadings(type).stream()
				.filter(i -> i.getTimestamp().isAfter(time.minusSeconds(30)))
				.filter(i -> i.getTimestamp().isBefore(time.plusSeconds(30)))
				.findFirst().orElse(null);
	}
	
	/**
	 * @return A list of all readings, across all resolutions, with the given type.
	 */
	public ArrayList<Reading> getAllReadings(DataType type) {
		ArrayList<Reading> results = new ArrayList<>();
		HashSet<LocalDateTime> seen = new HashSet<>();
		// First, get all the DeviceData that will contribute to this
		DeviceData[] contributors = data.stream()
				.filter(i -> i.getType() == type)
				.toArray(DeviceData[]::new);
		for (DeviceData next: contributors) {
			for (Reading nextReading: next.getReadings()) {
				LocalDateTime timestamp = nextReading.getTimestamp();
				// To avoid duplicates, make sure we haven't seen this timestamp before
				if (!seen.contains(timestamp)) {
					results.add(nextReading);
					seen.add(timestamp);
				}
			}
		}
		return results;
	}
	
	/**
	 * @return The average of all readings of the given type, if it can; -1 otherwise.
	 */
	public double getAverageReading(DataType type) {
		return getAllReadings(type).stream()
				.mapToDouble(i -> i.getReading())
				.average()
				.orElse(-1);
	}
	
	/**
	 * @return The average of all readings of the given type, if it can; -1 otherwise.
	 */
	public double getRawAverageReading(DataType type) {
		return getAllReadings(type).stream()
				.mapToDouble(i -> i.getRawReading())
				.average()
				.orElse(-1);
	}
	
	/**
	 * @return The average of all readings of the given type and resolution, if it can; -1 otherwise.
	 */
	public double getAverageReading(DataType type, DataResolution resolution) {
		return getDeviceData(type, resolution).getReadings().stream()
				.mapToDouble(i -> i.getReading())
				.average()
				.orElse(-1);
	}
	
	/**
	 * @return The average of all readings of the given type and resolution, if it can; -1 otherwise.
	 */
	public double getAverageRawReading(DataType type, DataResolution resolution) {
		return getDeviceData(type, resolution).getReadings().stream()
				.mapToDouble(i -> i.getRawReading())
				.average()
				.orElse(-1);
	}
	
	/**
	 * @return The standard deviation of all readings of the given type, if it can; -1 otherwise.
	 */
	public double getStandardDeviation(DataType type) {
		double average = getAverageReading(type);
		if (average == -1) return -1;
		double variance = getAllReadings(type).stream()
				.mapToDouble(i -> i.getReading())
				.map(i -> i - average)
				.map(i -> i*i)
				.average()
				.orElse(-1);
		if (variance == -1) return -1;
		else return Math.sqrt(variance);
	}
	
	/**
	 * @return The standard deviation of all readings of the given type, if it can; -1 otherwise.
	 */
	public double getStandardDeviationRaw(DataType type) {
		double average = getAverageReading(type);
		if (average == -1) return -1;
		double variance = getAllReadings(type).stream()
				.mapToDouble(i -> i.getRawReading())
				.map(i -> i - average)
				.map(i -> i*i)
				.average()
				.orElse(-1);
		if (variance == -1) return -1;
		else return Math.sqrt(variance);
	}
	
	/**
	 * @return The standard deviation of all readings of the given type and resolution, if it can; -1 otherwise.
	 */
	/*
	public double getStandardDeviation(DataType type, DataResolution resolution) {
		double average = getAverageReading(type, resolution);
		if (average == -1) return -1;
		double variance = getDeviceData(type, resolution).getReadings().stream()
				.mapToDouble(i -> i.getReading())
				.map(i -> i - average)
				.map(i -> i*i)
				.average()
				.orElse(-1);
		if (variance == -1) return -1;
		else return Math.sqrt(variance);
	}
	*/
	
	/**
	 * @return The LocalDateTime of the latest reading of the given type, if possible; null otherwise.
	 */
	public LocalDateTime getTimeOfLastReading(DataType type) {
		return data.stream()
				.filter(i -> i.getType() == type)
				.map(i -> i.getLatestReading().getTimestamp())
				.sorted((a, b) -> b.compareTo(a))
				.findFirst()
				.orElse(null);
	}
	
	/**
	 * @return The device's status with regards to the given DataType, if it can; null otherwise.
	 */
	public IdealStatus getStatus(DataType type) {
		// First, do we even do data for this DataType?
		if (!hasData(type)) return IdealStatus.DOES_NOT_SERVE;
		// Do we have a time of last reading?
		LocalDateTime last = getTimeOfLastReading(type);
		// Is that recent enough?
		if (last.isBefore(LocalDateTime.now().minusMinutes(5)))
			return IdealStatus.LATE;
		//if (last == null) return IdealStatus.UNKNOWN;
		// Okay, so do we know what sort of boundaries we should be in?
		Ideal ideal = ideals.get(type);
		double average = getRawAverageReading(type);
		double sd = getStandardDeviationRaw(type);
		double current = getLatestReading(type).getRawReading();
		double sdMulti = type.getSDMultiplier();
		if ((current > average + (sdMulti*sd)) || (current < average - (sdMulti*sd))) return IdealStatus.EXTREME;
		else if (ideal == null) return IdealStatus.UNKNOWN;
		else if (current > ideal.getMax()) return IdealStatus.OVER;
		else if (current < ideal.getMin()) return IdealStatus.UNDER;
		else return IdealStatus.OKAY;
	}
	
	/**
	 * @return Whether the Device as a whole is okay.
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
