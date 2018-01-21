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
 * Representation of a zone on the farm.
 * @author John Gabriel
 */
public class Zone {
	private static final Logger LOG = LoggerFactory.getLogger(Zone.class);

	private String id;
	private String name;
	private Site site;
	private ArrayList<Device> devices;
	private HashMap<DataType, Ideal> ideals;

	public Zone(String id, String name) {
		this.id = id;
		this.name = name;
		devices = new ArrayList<>();
		ideals = new HashMap<>();
	}

	public String getId() { return id; }
	public String getName() { return name; }
	public Site getSite() { return site; }
	public ArrayList<Device> getDevices() { return devices; }
	public Ideal getIdeal(DataType type) { return ideals.get(type); }
	
	public void setSite(Site site) { this.site = site; }
	public void addDevice(Device device) { devices.add(device); }
	public void setIdeal(DataType type, Ideal ideal) { ideals.put(type, ideal); }
	
	/**
	 * @return Whether the Zone has any devices recording data of the given type.
	 */
	public boolean hasData(DataType type) {
		return devices.stream()
				.anyMatch(i -> i.hasData(type));
	}
	
	/**
	 * @return What data types this zone gathers.
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
		for (Device next: devices)
			timestamps.addAll(next.getTimestamps());
		return timestamps.stream()
				.sorted((a, b) -> b.compareTo(a))
				.collect(Collectors.toList());
	}
	
	/**
	 * @return A list of all the timestamps for which we have data at the given resolution, rounded to the nearest minute.
	 */
	public List<LocalDateTime> getTimestamps(DataResolution resolution) {
		HashSet<LocalDateTime> timestamps = new HashSet<>();
		for (Device next: devices)
			timestamps.addAll(next.getTimestamps(resolution));
		List<LocalDateTime> results = timestamps.stream()
				.collect(Collectors.toList());
		results.sort((a, b) -> b.compareTo(a));
		return results;
	}
	
	/**
	 * @return The current reading for the current data type, averaged from all devices.
	 * For devices to be included, their latest reading must be less than five minutes old.
	 */
	public double getCurrentReading(DataType type) {
		return devices.stream()
				.filter(i -> i.hasData(type))
				.map(i -> i.getLatestReading(type))
				.filter(i -> i.getTimestamp().isAfter(LocalDateTime.now().minusMinutes(5)))
				.mapToDouble(i -> i.getReading())
				.average()
				.orElse(-1);
	}
	
	/**
	 * @return The current reading for the current data type, averaged from all devices.
	 * For devices to be included, their latest reading must be less than five minutes old.
	 */
	public double getCurrentReadingRaw(DataType type) {
		return devices.stream()
				.filter(i -> i.hasData(type))
				.map(i -> i.getLatestReading(type))
				.filter(i -> i.getTimestamp().isAfter(LocalDateTime.now().minusMinutes(5)))
				.mapToDouble(i -> i.getRawReading())
				.average()
				.orElse(-1);
	}

	/**
	 * @return The reading at the given time, if it exists; null otherwise.
	 */
	/*
	public double getReadingAtDateTime(DataType type, DataResolution resolution, LocalDateTime time) {
		return devices.stream()
				.filter(i -> i.hasData(type))
				.mapToDouble(i -> i.getReadingAtDateTime(type, resolution, time).getReading())
				.filter(i -> i != -1)
				.average()
				.orElse(-1);
	}
	*/
	
	/**
	 * @return The reading at the given time, if it exists; null otherwise.
	 */
	/*
	public double getReadingAtDateTime(DataType type, LocalDateTime time) {
		return devices.stream()
				.filter(i -> i.hasData(type))
				.mapToDouble(i -> i.getReadingAtDateTime(type, time).getReading())
				.filter(i -> i != -1)
				.average()
				.orElse(-1);
	}
	*/
	
	/**
	 * @return The reading at the given time, if it exists; null otherwise.
	 */
	/*
	public double getRawReadingAtDateTime(DataType type, DataResolution resolution, LocalDateTime time) {
		return devices.stream()
				.filter(i -> i.hasData(type))
				.mapToDouble(i -> i.getReadingAtDateTime(type, resolution, time).getRawReading())
				.filter(i -> i != -1)
				.average()
				.orElse(-1);
	}
	*/
	
	/**
	 * @return The reading at the given time, if it exists; null otherwise.
	 */
	/*
	public double getRawReadingAtDateTime(DataType type, LocalDateTime time) {
		return devices.stream()
				.filter(i -> i.hasData(type))
				.mapToDouble(i -> i.getReadingAtDateTime(type, time).getRawReading())
				.filter(i -> i != -1)
				.average()
				.orElse(-1);
	}
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
	 * @return The current average reading of the given type, if possible; -1 otherwise.
	 */
	public double getAverageReading(DataType type) {
		return devices.stream()
				.filter(i -> i.hasData(type))
				.mapToDouble(i -> i.getAverageReading(type))
				.average()
				.orElse(-1);
	}
	
	/**
	 * @return The current average reading of the given type, if possible; -1 otherwise.
	 */
	public double getAverageReadingRaw(DataType type) {
		return devices.stream()
				.filter(i -> i.hasData(type))
				.mapToDouble(i -> i.getRawAverageReading(type))
				.average()
				.orElse(-1);
	}
	
	/**
	 * @return The standard deviation of all readings of the given type, if it can; -1 otherwise.
	 */
	public double getStandardDeviation(DataType type) {
		return devices.stream()
				.filter(i -> i.hasData(type))
				.mapToDouble(i -> i.getStandardDeviation(type))
				.average()
				.orElse(-1);
	}
	
	/**
	 * @return The standard deviation of all readings of the given type, if it can; -1 otherwise.
	 */
	public double getRawStandardDeviation(DataType type) {
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
	 * @return The Zone's status with regards to the given DataType, if it can; null otherwise.
	 */
	public IdealStatus getStatus(DataType type) {
		// First, do we even do data for this DataType?
		if (!hasData(type)) return IdealStatus.DOES_NOT_SERVE;
		// Do we have a time of last reading?
		LocalDateTime last = getTimeOfLastReading(type);
		// Is that recent enough?
		if (last.isBefore(LocalDateTime.now().minusMinutes(5)))
			return IdealStatus.LATE;
		//else if (last == null) return IdealStatus.UNKNOWN;
		// Okay, so do we know what sort of boundaries we should be in?
		double average = getAverageReadingRaw(type);
		double sd = getRawStandardDeviation(type);
		double current = getCurrentReadingRaw(type);
		Ideal ideal = ideals.get(type);
		double sdMulti = type.getSDMultiplier();
		if ((current > average + (sdMulti*sd)) || (current < average - (sdMulti*sd))) return IdealStatus.EXTREME;
		else if (ideal == null) return IdealStatus.UNKNOWN;
		else if (current > ideal.getMax()) return IdealStatus.OVER;
		else if (current < ideal.getMin()) return IdealStatus.UNDER;
		else return IdealStatus.OKAY;
	}
	
	/**
	 * @return Whether the Zone as a whole is okay.
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
