package uk.co.johngabriel.co657a3.things;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.co.johngabriel.co657a3.model.Cell;
import uk.co.johngabriel.co657a3.model.Ideal;
import uk.co.johngabriel.co657a3.model.Problem;
import uk.co.johngabriel.co657a3.model.Table;
import uk.co.johngabriel.co657a3.model.json.DataResolution;
import uk.co.johngabriel.co657a3.model.json.DataType;
import uk.co.johngabriel.co657a3.model.json.Device;
import uk.co.johngabriel.co657a3.model.json.DeviceType;
import uk.co.johngabriel.co657a3.model.json.IdealStatus;
import uk.co.johngabriel.co657a3.model.json.Reading;
import uk.co.johngabriel.co657a3.model.json.Site;
import uk.co.johngabriel.co657a3.model.json.Zone;
import uk.co.johngabriel.co657a3.webbits.WebController;

/**
 * For shared functionality that improves readability.
 * @author John Gabriel
 */
public class Prettyfier {
	private static final Logger LOG = LoggerFactory.getLogger(Prettyfier.class);
	
	private static DateTimeFormatter formatter1 = DateTimeFormatter.ofPattern("yyyy/MM/dd");
	private static DateTimeFormatter formatter2 = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:SS");

	public static String timeSince(LocalDateTime ldt) {
		long seconds = ldt.until(LocalDateTime.now(), ChronoUnit.SECONDS);
		long minutes = ldt.until(LocalDateTime.now(), ChronoUnit.MINUTES);
		long hours = ldt.until(LocalDateTime.now(), ChronoUnit.HOURS);
		if (seconds < 60) return seconds + "s ago";
		else if (minutes < 60) return minutes + "m ago";
		else if (hours < 24) return hours + "h ago";
		else return "on " + ldt.format(formatter1);
	}

	public static String ldtToString(LocalDateTime ldt) {
		return ldt.format(formatter2);
	}

	public static String getIdealReadout(Ideal ideal, DataType type) {
		if (ideal == null) return "N/A";
		return String.format("%.2f to %.2f%s",
				getReadingValue(ideal.getMin(), type),
				getReadingValue(ideal.getMax(), type),
				getReadingScale(type)
				);
	}
	
	public static LocalDateTime toNearestMinute(LocalDateTime timestamp) {
		if (timestamp.getSecond() > 30)
			return timestamp.truncatedTo(ChronoUnit.MINUTES).plusMinutes(1);
		else
			return timestamp.truncatedTo(ChronoUnit.MINUTES);
	}
	
	private static final byte TEMP_KELVIN = 0;
	private static final byte TEMP_CELCIUS = 1;
	private static final byte TEMP_FAHRENHEIT = 2;
	private static final byte TEMP_FELCIUS = 3;
	
	private static byte tempType = TEMP_CELCIUS;
	private static String[] tempChar = new String[] {"°K", "°C", "°F", "°⋲"};
	
	public static double convertTemperature(double reading) {
		switch (tempType) {
		case TEMP_KELVIN: return reading;
		case TEMP_CELCIUS: return reading - 273.15;
		case TEMP_FAHRENHEIT: return ((reading + 459.67) * (5d/9));
		case TEMP_FELCIUS: return ((reading - 273.15) * 5) - 16;
		default: return reading;
		}
	}
	
	/**
	 * @return A raw reading, adjusted for the currently selected scale for that data type.
	 */
	public static double getReadingValue(Reading reading) {
		DataType type = reading.getType();
		switch (type) {
		case TEMPERATURE: return convertTemperature(reading.getRawReading());
		default: return reading.getRawReading();
		}
	}
	
	/**
	 * @return A raw reading, adjusted for the currently selected scale for that data type.
	 */
	public static double getReadingValue(double reading, DataType type) {
		switch (type) {
		case TEMPERATURE: return convertTemperature(reading);
		default: return reading;
		}
	}
	
	public static String getReadingScale(DataType type) {
		switch (type) {
		case TEMPERATURE: return tempChar[tempType];
		case GAS: return "ppm";
		case HUMIDITY: return "%";
		case LUMOSITY: return "lux";
		case MOISTURE: return "% vwc";
		case SOLAR: return "W";
		default: return "";
		}
	}
	
	public static String getReadingString(Reading reading) {
		if (reading.getRawReading() == -1) return "error";
		return String.format("%.2f%s", reading.getReading(), getReadingScale(reading.getType()));
	}
	
	public static void setTempType(byte type) {
		tempType = type;
	}

	/*
	 * PROBLEM STUFF
	 */

	private static final String[] PROBLEM_HEADER = new String[] {"Type", "Target", "Description"};

	public static Cell[] getProblemRow(Problem problem) {
		Object target;
		String type;
		String name;
		String link;
		if (problem.getDevice() != null) {
			target = problem.getDevice();
			type = "Device";
			name = problem.getDevice().getName();
			link = getDeviceLink(problem.getDevice());
		}
		else if (problem.getZone() != null) {
			target = problem.getZone();
			type = "Zone";
			name = problem.getZone().getName();
			link = getZoneLink(problem.getZone());
		}
		else {
			target = problem.getSite();
			type = "Site";
			name = problem.getSite().getName();
			link = getSiteLink(problem.getSite());
		}
		return new Cell[] {
				new Cell(type),
				new Cell(name, link),
				new Cell(problem.getMessage())
		};
	}

	public static Table getProblemTable(ArrayList<Problem> problems) {
		Cell[][] contents = problems.stream()
				.map(i -> getProblemRow(i))
				.toArray(Cell[][]::new);
		return new Table(PROBLEM_HEADER, contents);
	}

	/*
	 * SITE STUFF
	 */

	private static final String[] SITE_HEADER = new String[] {"ID", "Name", "Latitude", "Longitude", "Altitude",
	"Status"};

	public static Cell[] getSiteRow(Site site) {
		return new Cell[] {
				new Cell(site.getId(), "/site/" + site.getId()),
				new Cell(site.getName()),
				new Cell(site.getLatitude()),
				new Cell(site.getLongitude()),
				new Cell(site.getAltitude()),
				new Cell(site.isOkay() ? "O.K." : "Has problems!")
		};
	}

	public static Table getSiteTable(ArrayList<Site> sites) {
		Cell[][] contents = sites.stream()
				.map(i -> getSiteRow(i))
				.toArray(Cell[][]::new);
		return new Table(SITE_HEADER, contents);
	}

	public static Table getSiteInfo(Site sites) {
		return new Table(SITE_HEADER, new Cell[][] {getSiteRow(sites)});
	}

	public static Table getSiteAverages(Site site) {
		String[] header = new String[] {"Type", "Current", "Mean", "S.D.", "Ideal", "Status"};
		Cell[][] contents = Arrays.asList(DataType.values()).stream()
				.filter(i -> site.hasData(i))
				.map(i -> new Cell[] {
						new Cell(i.getLabel()),
						new Cell(String.format("%.2f%s", site.getCurrentReading(i), getReadingScale(i))),
						new Cell(String.format("%.2f%s", site.getAverageReading(i), getReadingScale(i))),
						new Cell(String.format("%.2f", site.getStandardDeviation(i))),
						new Cell(getIdealReadout(site.getIdeal(i), i)),
						new Cell(site.getStatus(i).getLabel())
				})
				.toArray(Cell[][]::new);
		return new Table(header, contents);
	}

	public static Table getSiteZones(Site site) {
		return getZoneTable(site.getZones());
	}
	
	/**
	 * Used for the function below.
	 */
	public static Object[] getSiteChartRow(Site site, DataResolution resolution, List<DataType> types, LocalDateTime timestamp) {
		Object[] result = new Object[types.size()+1];
		result[0] = dateToSillyDate(timestamp);
		boolean anyNotNull = false;
		for (int i = 0; i < types.size(); i++) {
			double reading = site.getReadingClosestToDateTime(types.get(i), timestamp, resolution);
			if (reading != -1) {
				result[i+1] = reading;
				anyNotNull = true;
			}
			else
				result[i+1] = null;
		}
		if (anyNotNull)	return result;
		else return null;
	}

	/**
	 * This one is for filling the chart.
	 */
	public static HashMap<String, Object> getSiteChartData(Site site, DataResolution resolution) {
		HashMap<String, Object> result = new HashMap<>();
		List<DataType> types = site.getTypes();
		String[] typeNames = types.stream().map(i -> i.getLabel()).toArray(String[]::new);
		result.put("name", site.getName());
		result.put("types", typeNames);
		List<LocalDateTime> timestamps = site.getTimestamps();
		Object[][] table = timestamps.parallelStream()
				.map(i -> getSiteChartRow(site, resolution, types, i))
				.filter(i -> i != null)
				.toArray(Object[][]::new);
		result.put("values", table);
		return result;
	}
	
	public static HashMap<String, Object> getSiteChartMisc(Site zone) {
		String[] labels = zone.getTypes().stream()
				.map(i -> String.format("%s (%s)", i.getLabel(), getReadingScale(i)))
				.toArray(String[]::new);
		String title = String.format("Readings from {}", zone.getName());
		HashMap<String, Object> result = new HashMap<>();
		result.put("labels", labels);
		return result;
	}

	public static String getSiteLink(Site site) {
		return String.format("/site/%s", site.getId());
	}

	/*
	 * ZONE STUFF
	 */

	private static final String[] ZONE_HEADER = new String[] {"ID", "Name", "Site", "Data Types", "Status"};

	public static Cell[] getZoneRow(Zone zone) {
		ArrayList<DataType> types = new ArrayList<>();
		for (Device device: zone.getDevices())
			types.addAll(device.getTypes());
		return new Cell[] {
				new Cell(zone.getId(), getZoneLink(zone)),
				new Cell(zone.getName()),
				new Cell(zone.getSite().getName(), "/site/" + zone.getSite().getId()),
				new Cell(
						types.stream()
						.map(i -> i.getLabel())
						.collect(Collectors.joining(", "))
								),
				new Cell(zone.isOkay() ? "O.K." : "Has problems!")
		};
	}

	public static Table getZoneTable(ArrayList<Zone> zones) {
		Cell[][] contents = zones.stream()
				.map(i -> getZoneRow(i))
				.toArray(Cell[][]::new);
		return new Table(ZONE_HEADER, contents);
	}

	public static Table getZoneInfo(Zone zone) {
		return new Table(ZONE_HEADER, new Cell[][] {getZoneRow(zone)});
	}

	public static Table getZoneAverages(Zone zone) {
		String[] header = new String[] {"Type", "Current", "Mean", "S.D.", "Ideal", "Status"};
		Cell[][] contents = Arrays.asList(DataType.values()).stream()
				.filter(i -> zone.hasData(i))
				.map(i -> new Cell[] {
						new Cell(i.getLabel()),
						new Cell(String.format("%.2f%s", zone.getCurrentReading(i), getReadingScale(i))),
						new Cell(String.format("%.2f%s", zone.getAverageReading(i), getReadingScale(i))),
						new Cell(String.format("%.2f", zone.getStandardDeviation(i))),
						new Cell(getIdealReadout(zone.getIdeal(i), i)),
						new Cell(zone.getStatus(i).getLabel())
				})
				.toArray(Cell[][]::new);
		return new Table(header, contents);
	}

	public static Object[][] getAverageReadings(Zone zone) {
		return Arrays.asList(DataType.values()).stream()
				.filter(i -> zone.hasData(i))
				.map(i -> new Object[] {i.getLabel(), zone.getAverageReading(i)} )
				.toArray(Object[][]::new);
	}
	
	/**
	 * Used for the function below.
	 */
	public static Object[] getZoneChartRow(Zone zone, DataResolution resolution, List<DataType> types, LocalDateTime timestamp) {
		Object[] result = new Object[types.size()+1];
		result[0] = dateToSillyDate(timestamp);
		boolean anyNotNull = false;
		for (int i = 0; i < types.size(); i++) {
			double reading = zone.getReadingClosestToDateTime(types.get(i), timestamp, resolution);
			if (reading != -1) {
				result[i+1] = reading;
				anyNotNull = true;
			}
			else
				result[i+1] = null;
		}
		if (anyNotNull)	return result;
		else return null;
	}

	/**
	 * This one is for filling the chart.
	 */
	public static HashMap<String, Object> getZoneChartData(Zone zone, DataResolution resolution) {
		HashMap<String, Object> result = new HashMap<>();
		List<DataType> types = zone.getTypes();
		String[] typeNames = types.stream().map(i -> i.getLabel()).toArray(String[]::new);
		result.put("name", zone.getName());
		result.put("types", typeNames);
		List<LocalDateTime> timestamps = zone.getTimestamps();
		Object[][] table = timestamps.parallelStream()
				.map(i -> getZoneChartRow(zone, resolution, types, i))
				.filter(i -> i != null)
				.toArray(Object[][]::new);
		result.put("values", table);
		return result;
	}
	
	public static HashMap<String, Object> getZoneChartMisc(Zone zone) {
		String[] labels = zone.getTypes().stream()
				.map(i -> String.format("%s (%s)", i.getLabel(), getReadingScale(i)))
				.toArray(String[]::new);
		String title = String.format("Readings from {}", zone.getName());
		HashMap<String, Object> result = new HashMap<>();
		result.put("labels", labels);
		return result;
	}

	public static String getZoneLink(Zone zone) {
		return String.format("/zone?site=%s&zone=%s", zone.getSite().getId(), zone.getId());
	}

	/*
	 * DEVICE STUFF
	 */

	private static final String[] DEVICE_HEADER = new String[] {"Name", "Type", "Zone", "Site",
			"Latest reading(s)", "Last connected", "Software Version", "Status"};


	public static Cell[] getDeviceRow(Device device) {
		String zoneId = device.getZone().getId();
		String siteId = device.getSite().getId();
		return new Cell[] {
				new Cell(device.getName(), "/device/" + device.getId()),
				new Cell(device.getType().getLabel()),
				new Cell(device.getZone().getName(), getZoneLink(device.getZone())),
				new Cell(device.getSite().getName(), "/site/" + siteId),
				new Cell(getDeviceLatestReadouts(device)),
				new Cell(getConnectionReadout(device)),
				new Cell(device.getSoftwareVersion()),
				new Cell((device.isOkay()) ? "O.K." : "Attention needed")
		};
	}
	
	public static String getDeviceLatestReadouts(Device device) {
		return device.getTypes().stream()
				.map(i -> getReadingString(device.getLatestReading(i)))
				.collect(Collectors.joining(", "));
	}

	public static Table getDeviceTable(ArrayList<Device> devices) {
		Cell[][] contents = devices.stream()
				.map(i -> getDeviceRow(i))
				.toArray(Cell[][]::new);
		return new Table(DEVICE_HEADER, contents);
	}

	public static Table getDeviceInfo(Device device) {
		return new Table(DEVICE_HEADER, new Cell[][] {getDeviceRow(device)});
	}
	
	public static String[] getDeviceReadingHeader(Device device, List<DataType> types) {
		String[] header = new String[types.size()+1];
		header[0] = "Timestamp";
		for (int i = 0; i < types.size(); i++)
			header[i+1] = types.get(i).getLabel();
		return header;
	}
	
	public static Cell[] getDeviceReadingRow(Device device, List<DataType> types, LocalDateTime timestamp) {
		Cell[] result = new Cell[types.size()+1];
		result[0] = new Cell(timestamp.format(formatter2));
		for (int i = 0; i < types.size(); i++) {
			Reading reading = device.getReadingClosestToDateTime(types.get(i), timestamp);
			if (reading != null)
				result[i+1] = new Cell(getReadingString(reading));
			else
				result[i+1] = new Cell("");
		}
		return result;
	}

	public static Table getDeviceReadingTable(Device device) {
		List<DataType> types = device.getTypes();
		String[] header = getDeviceReadingHeader(device, types);
		List<LocalDateTime> timestamps = device.getTimestamps();
		Cell[][] contents = timestamps.parallelStream()
				.map(i -> getDeviceReadingRow(device, types, i))
				.toArray(Cell[][]::new);
		return new Table(header, contents);
	}
	
	/**
	 * Used for the function below.
	 */
	public static Object[] getChartRow(Device device, DataResolution resolution, List<DataType> types, LocalDateTime timestamp) {
		Object[] result = new Object[types.size()+1];
		result[0] = dateToSillyDate(timestamp);
		for (int i = 0; i < types.size(); i++) {
			Reading reading = device.getReadingClosestToDateTime(types.get(i), timestamp, resolution);
			if (reading != null)
				result[i+1] = reading.getReading();
			else
				result[i+1] = null;
		}
		return result;
	}

	/**
	 * This one is for filling the chart.
	 */
	public static HashMap<String, Object> getChartData(Device device, DataResolution resolution) {
		HashMap<String, Object> result = new HashMap<>();
		List<DataType> types = device.getTypes();
		String[] typeNames = types.stream().map(i -> i.getLabel()).toArray(String[]::new);
		result.put("name", device.getName());
		result.put("types", typeNames);
		List<LocalDateTime> timestamps = device.getTimestamps(resolution);
		Object[][] table = timestamps.parallelStream()
				.map(i -> getChartRow(device, resolution, types, i))
				.toArray(Object[][]::new);
		result.put("values", table);
		return result;
	}

	/**
	 * @return A date that can be used by JavaScript
	 */
	public static int[] dateToSillyDate(LocalDateTime date) {
		return new int[] {
				date.getYear(),
				date.getMonthValue()-1,
				date.getDayOfMonth(),
				date.getHour(),
				date.getMinute(),
				date.getSecond()
		};
	}

	/**
	 * Used in a JSON request so we know how many charts/tables we need on the Device page.
	 */
	public static HashMap<String, Object> getDeviceData(Device device) {
		HashMap<String, Object> result = new HashMap<>();
		result.put("name", device.getName());
		result.put("types", Arrays.asList(DataType.values()).stream()
				.filter(i -> device.hasData(i))
				.map(i -> i.getLabel())
				.toArray(String[]::new));
		return result;
	}

	public static Table getCurrentData(Device device) {
		String[] header = new String[] {"Type", "Latest reading", "Mean", "S.D.", "Last reading", "Ideal", "Status"};
		Cell[][] contents = Arrays.asList(DataType.values()).stream()
				.filter(i -> device.hasData(i))
				.map(i -> new Cell[] {
						new Cell(i.getLabel()),
						new Cell(getReadingString(device.getLatestReading(i))),
						new Cell(String.format("%.2f%s", device.getAverageReading(i), getReadingScale(i))),
						new Cell(String.format("%.2f", device.getStandardDeviation(i))),
						new Cell(device.getTimeOfLastReading(i).format(formatter2)),
						new Cell(getIdealReadout(device.getIdeal(i), i)),
						new Cell(device.getStatus(i).getLabel())
				})
				.toArray(Cell[][]::new);
		return new Table(header, contents);
	}
	
	public static HashMap<String, Object> getDeviceChartMisc(Device device) {
		String[] labels = device.getTypes().stream()
				.map(i -> String.format("%s (%s)", i.getLabel(), getReadingScale(i)))
				.toArray(String[]::new);
		String title = String.format("Readings from {}", device.getName());
		HashMap<String, Object> result = new HashMap<>();
		result.put("labels", labels);
		return result;
	}

	/*
	public static Object[][] getChartTable(Device device, DataType type) {
		Object[][] table = device.getDeviceData(type, DataResolution.EVERY_10MINUTES).getReadings().stream()
				.filter(i -> i.getTimestamp().isAfter(LocalDateTime.now().minusWeeks(1)))
				.filter(i -> i.getReading() != -1)
				.sorted((a, b) -> a.getTimestamp().compareTo(b.getTimestamp()))
				.map(i -> new Object[] {
						i.getTimestamp().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss[.][n]'Z'")),
						i.getReading()
				})
				.toArray(Object[][]::new);
		return table;
	}

	public static Object[][] getDeviceChart(Device device, DataType type) {
		Object[][] table = device.getAllReadings(type)
				.stream()
				.filter(i -> i.getReading() != -1)
				.map(i -> new Object[] {
						i.getTimestamp(),
						i.getReading()
				})
				.toArray(Object[][]::new);
		return table;
	}

	public static String getDeviceLatestReadout(Device device, DataType type) {
		Reading mostRecent = device.getLatestReading(type);
		String readout;
		double reading = mostRecent.getReading();
		if (reading != -1)
			readout = String.format("%.2f", reading);
		else readout = "Error";
		return readout;
	}
	 */

	public static final String MARKER_URL_GREEN = "https://maps.google.com/mapfiles/ms/micons/green-dot.png";
	public static final String MARKER_URL_YELLOW = "https://maps.google.com/mapfiles/ms/micons/yellow-dot.png";
	public static final String MARKER_URL_RED = "https://maps.google.com/mapfiles/ms/micons/red-dot.png";

	public static List<Object[]> markerPositions = Arrays.asList(new Object[][] {
		{"gh3_seed_temp", 1.1037076d, 51.3092622d},
		{"gh3_east_door_temp", 1.1049897d, 51.3095137d},
		{"gh3_west_door_temp", 1.1029941d, 51.3088598d},
		{"gh1_north_door_temp", 1.102699d, 51.3079142d},
		{"gh1_south_door_temp", 1.1034608d, 51.3072972d},
		{"gh1_co2Production_gas", 1.1027312d, 51.3072032d},
		{"gh3_seed_lux", 1.1035842d, 51.3093695d},
		{"gh3_west_door_lux", 1.1028868d, 51.3089571d},
		{"gh3_east_door_lux", 1.1048717d, 51.3096143d},
		{"gh1_north_door_lux", 1.1028814d, 51.3079745d},
		{"gh1_south_door_lux", 1.1032033d, 51.3072569d},
		{"gh1_plantzone_1_temp", 1.1032059d, 51.3076375d},
		{"gh1_plantzone_1_lux", 1.1029619d, 51.3076023d},
		{"gh3_co2Production_gas", 1.1035788d, 51.309896d},
		{"outside_field_moisture", 1.1061537d, 51.3074715d},
		{"outside_heap_temp", 1.1058989d, 51.3078924d},
		{"outside_field_lux", 1.1058426d, 51.3074044d},
		{"outside_field_temp", 1.1064756d, 51.3075386d},
		{"gh2_co2Production_gas", 1.0991156d, 51.3081959d},
		{"gh2_south_door_temp", 1.100277d, 51.3079259d},
		{"gh2_south_door_lux", 1.100505d, 51.3080282d},
		{"gh2_north_door_temp", 1.0997272d, 51.308444d},
		{"gh2_north_door_lux", 1.0999632d, 51.3085513d},
		{"gh2_plantzone_1_temp", 1.0996333d, 51.3080215d},
		{"gh2_plantzone_1_moisture", 1.0998398d, 51.3081036d},
		{"gh2_plantzone_1_lux", 1.1000517d, 51.3081959d},
		{"gh2_mains_temp", 1.1006767d, 51.3086117d},
		{"gh2_mains_moisture", 1.100843d, 51.3084775d},
		{"gh2_mains_lux", 1.1009985d, 51.3083635d},
		{"gh3_seed_moisture", 1.1038417d, 51.3091482d},
		{"house_store_temp", 1.1066566d, 51.3084985d},
		{"house_store_lux", 1.1068001d, 51.3083669d},
		{"gh1_plantzone_1_moisture", 1.1034286d, 51.3076727d},
		{"gh1_co2Production_temp", 1.102581d, 51.3072502d}
	});

	public static double[] getMarkerPos(String deviceId) {
		return markerPositions.stream()
				.filter(i -> ((String) i[0]).equals(deviceId))
				.map(i -> new double[] {(double) i[2], (double) i[1]})
				.findFirst()
				.orElse(new double[] {-1, -1});
	}
	
	public static String getIconUrl(Device device) {
		switch (device.getType()) {
		case SOLAR:
			if (device.isOkay()) return "/img/elec_okay.png";
			else return "/img/elec_attention.png";
		case GAS:
			if (device.isOkay()) return "/img/gas_okay.png";
			else return "/img/gas_attention.png";
		case HYDROMETER:
			if (device.isOkay()) return "/img/tap_okay.png";
			else return "/img/tap_attention.png";
		case LUMOSITY:
			if (device.isOkay()) return "/img/sun_okay.png";
			else return "/img/sun_attention.png";
		case TEMPHUMID:
			if (device.isOkay()) return "/img/thermo_okay.png";
			else return "/img/thermo_attention.png";
		default:
			if (device.isOkay()) return "/img/default_okay.png";
			else return "/img/default_attention.png";
		}
	}

	public static List<HashMap<String, Object>> getDeviceMapMarkers(ArrayList<Device> devices) {
		List<HashMap<String, Object>> array = new ArrayList<>();
		devices.stream()
		.map(i -> {
			String iconUrl = getIconUrl(i);
			double[] pos = getMarkerPos(i.getId());
			HashMap<String, Object> newObject = new HashMap<>();
			newObject.put("title", i.getName());
			newObject.put("url", getDeviceLink(i));
			newObject.put("icon", iconUrl);
			newObject.put("latitude", pos[0]);
			newObject.put("longitude", pos[1]);
			return newObject;
		})
		.forEach(i -> array.add(i));
		return array;
	}

	public static String getDeviceLink(Device device) {
		return String.format("/device/%s", device.getId());
	}

	/**
	 * @return How many seconds have passed since the device last connected
	 */
	public static String getConnectionReadout(Device device) {
		return Prettyfier.timeSince(device.getLastConnection());
	}

}
