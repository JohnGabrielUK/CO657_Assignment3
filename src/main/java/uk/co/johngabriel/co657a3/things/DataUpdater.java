package uk.co.johngabriel.co657a3.things;

import java.awt.List;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

import org.apache.tomcat.util.http.fileupload.util.Streams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.co.johngabriel.co657a3.model.Ideal;
import uk.co.johngabriel.co657a3.model.json.DataResolution;
import uk.co.johngabriel.co657a3.model.json.DataType;
import uk.co.johngabriel.co657a3.model.json.Device;
import uk.co.johngabriel.co657a3.model.json.DeviceData;
import uk.co.johngabriel.co657a3.model.json.DeviceType;
import uk.co.johngabriel.co657a3.model.json.Reading;
import uk.co.johngabriel.co657a3.model.json.Site;
import uk.co.johngabriel.co657a3.model.json.Zone;

/**
 * @author John Gabriel
 */
public class DataUpdater implements Runnable {
	private static final Logger LOG = LoggerFactory.getLogger(DataUpdater.class);
	private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss[.][n][n][n][n][n][n][n][n][n]'Z'");
    
    private HashMap<String, Device> devices;
    private HashMap<String, Zone> zones;
    private HashMap<String, Site> sites;
    
    private HashSet<String> expectedDevices;
    
    private HashMap<String, Device> updating_devices;
    private HashMap<String, Zone> updating_zones;
    private HashMap<String, Site> updating_sites;
    
    private HashSet<String> updating_expectedDevices;
    
    private boolean updating;
    private LocalDateTime lastUpdateTime; // When the last update finished
    
    private DataManager manager;
    private ProblemFinder finder;
    
    public DataUpdater(DataManager manager, ProblemFinder finder, Idealator idealator) {
    	this.manager = manager;
    	this.finder = finder;
		devices = new HashMap<>();
		zones = new HashMap<>();
		sites = new HashMap<>();
		expectedDevices = new HashSet<>();
    	updating = false;
		updating_devices = new HashMap<>();
		updating_zones = new HashMap<>();
		updating_sites = new HashMap<>();
		updating_expectedDevices = new HashSet<>();
    }
    
    private String extractString(JSONObject json, String key) throws JSONException {
    	if (json.has(key))
    		return json.getString(key);
    	LOG.error("JSONObject has no {} value", key);
    	return null;
    }
    
    private double extractDouble(JSONObject json, String key) throws JSONException {
    	if (json.has(key))
    		return json.getDouble(key);
    	LOG.error("JSONObject has no {} value", key);
    	return -1;
    }
    
    private LocalDateTime parseDateTime(String str) {
		LocalDateTime dateTime = null;
    	try {
    		dateTime = LocalDateTime.parse(str, DTF);
		}
    	catch (DateTimeParseException e3) {
			LOG.error("Could not parse timestamp {}", str);
		}
    	return dateTime;
    }
    
    private Zone makeZone(JSONObject json) throws JSONException {
    	String id = extractString(json, "id");
    	LOG.info("Making Zone {}", id);
		String name = extractString(json, "name");
		Zone zone = new Zone(id, name);
		return zone;
    }
	
	private Site makeSite(JSONObject json) throws JSONException {
		String id = extractString(json, "id");
    	LOG.info("Making Site {}", id);
		String name = extractString(json, "name");
		String countryCode = extractString(json, "country_code");
		String timezone = extractString(json, "time_zone");
		double latitude = extractDouble(json, "lat");
		double longitude = extractDouble(json, "lon");
		double altitude = extractDouble(json, "al");
		// Construct the Site object
		Site newSite = new Site(id, name, longitude, latitude, altitude, countryCode, timezone);
		// Set ideals
		for (DataType type: DataType.values()) {
			Ideal ideal = Idealator.getSiteIdeal(newSite, type);
			if (ideal != null)
				newSite.setIdeal(type, ideal);
		}
		// Add zones
		JSONArray zonesJSON = json.getJSONArray("zones");
		for (int j = 0; j < zonesJSON.length(); j++) {
			JSONObject zone = zonesJSON.getJSONObject(j);
			Zone newZone = makeZone(zone);
			newZone.setSite(newSite);
			// Set ideals
			for (DataType type: DataType.values()) {
				Ideal ideal = Idealator.getZoneIdeal(newZone, type);
				if (ideal != null) newZone.setIdeal(type, ideal);
			}
			// Add to many things
			newSite.addZone(newZone.getId(), newZone);
			updating_zones.put(id + "/" + newZone.getId(), newZone);
		}
		return newSite;
	}
	
	/**
	 * Makes a Reading for a temperature reading
	 */
	private Reading makeTempReading(LocalDateTime timestamp, double value, String scale) {
		// Convert the reading to Kelvin
		double kelvin;
		if (scale.equals("C"))
			kelvin = value + 273.15;
		else if (scale.equals("F"))
			kelvin = (value + 459.67) * (5/9);
		else if (scale.equals("K"))
			kelvin = value;
		else {
			LOG.warn("Unknown temperature scale '{}'. We'll assume this is Felsius.", scale);
			kelvin = ((((value + 16) * 5) / 7) + 273.15); 
		}
		// Now make sure the reading isn't beyond the realm of the possible
		if (kelvin < 0) return null;
		return new Reading(timestamp, kelvin, DataType.TEMPERATURE);
	}
	
	/**
	 * Makes a Reading for a luminosity reading
	 */
	private Reading makeLuxReading(LocalDateTime timestamp, double value, String scale) {
		// If the reading is reporting negative lux, something's off; discard this reading
		if (value < 0) return null;
		// If all is well, return the reading without transformation
		return new Reading(timestamp, value, DataType.LUMOSITY);
	}
	
	/**
	 * Smartly makes a reading based on the raw data from the server.
	 */
	private Reading makeReading(LocalDateTime timestamp, double value, DataType type, String scale) {
		if (value == -1) return null;
		switch (type) {
		case TEMPERATURE: return makeTempReading(timestamp, value, scale);
		case LUMOSITY: return makeLuxReading(timestamp, value, scale);
		default: return new Reading(timestamp, value, type);
		}
	}
	
	private void makeReadings(DeviceData deviceData, DataType type, String scale, JSONArray json) throws JSONException {
		// Process each reading
		for (int i = 0; i < json.length(); i++) {
			JSONArray next = json.getJSONArray(i);
			// Get the fields from the reading
			String timeStr = next.getString(0);
			LocalDateTime timestamp = parseDateTime(timeStr);
			double value = -1;
			if (!next.isNull(1))
				value = next.getDouble(1);
			Reading reading = makeReading(timestamp, value, type, scale);
			if (reading != null)
				deviceData.addReading(reading);
		}
	}
	
	/**
	 * Makes a DeviceData from the given JSON object at the given res and type - if the JSON contains that type. Returns null otherwise.
	 */
	private DeviceData makeDeviceData(JSONObject json, DataType type, DataResolution resolution) throws JSONException {
		// First, check that the JSON has the type of data we're looking for
		String valueName = type.getValueName();
		String scaleName = type.getScaleName();
		if (!json.has(valueName) || !json.has(scaleName))
			return null;
		// Get all the parameters we need
		String id = extractString(json, "id");
		String name = extractString(json, "name");
		String siteId = extractString(json, "site_id");
		String zoneId = extractString(json, "zone_id");
		String softwareVersion = extractString(json, "software_version");
		String scale = extractString(json, scaleName);
		// Check that the siteId and zoneId referenced actually exist
		Site site = updating_sites.get(siteId);
		Zone zone = updating_zones.get(siteId + "/" + zoneId);
		if (site == null) {
			LOG.error("DeviceData for {} references non-existant site {}", id, siteId);
			return null;
		}
		if (zone == null) {
			LOG.error("DeviceData for {} references non-existant zone {}", id, zoneId);
			return null;
		}
		// Construct the DeviceData instance
		DeviceData data = new DeviceData(id, name, site, zone, softwareVersion, type, resolution, scale);
		// Now fill it with the readings
		JSONArray valuesJson = json.getJSONArray(type.getValueName());
		makeReadings(data, type, scale, valuesJson);
		return data;
	}
	
	private Device makeDeviceObject(String id, DeviceType type, JSONObject json) throws JSONException {
		String name = extractString(json, "name");
		String siteId = extractString(json, "site_id");
		String zoneId = extractString(json, "zone_id");
		String lastConStr = extractString(json, "last_connection");
		String softwareVersion = extractString(json, "software_version");
		LocalDateTime lastConnection = parseDateTime(lastConStr);
		Site site = updating_sites.get(siteId);
		Zone zone = site.getZone(zoneId);
		// Construct the Device object
		return new Device(id, name, type, site, zone, lastConnection, softwareVersion);
	}
	
	private void fillDeviceWithData(Device device) throws JSONException {
		String id = device.getId();
		Arrays.asList(DataResolution.values()).parallelStream()
		.forEach(nextResolution -> {
			JSONObject jsonData;
			try {
				jsonData = InternetGetterThing.getDeviceData(id, nextResolution);
				for (DataType type: DataType.values()) {
					DeviceData datum = makeDeviceData(jsonData, type, nextResolution);
					if (datum != null)
						device.addData(datum);
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});
	}
	
	private Device makeDevice(String id, DeviceType type, JSONObject json) throws JSONException  {
		LOG.info("Making Device {}", id);
		Device device = makeDeviceObject(id, type, json);
		fillDeviceWithData(device);
		// Add the device to its parent site and zone
		device.getSite().addDevice(device);
		device.getZone().addDevice(device);
		for (DataType dataType: DataType.values()) {
			Ideal ideal = Idealator.getDeviceIdeal(device, dataType);
			if (ideal != null) device.setIdeal(dataType, ideal);
		}
		return device;
	}
	
	private void makeDevices(JSONArray json, DeviceType type) throws JSONException {
		// First, turn it into a regular ArrayList, so we can process it
		ArrayList<String> ids = new ArrayList<>();
		for (int i = 0; i < json.length(); i++) {
			String id = json.getString(i);
			ids.add(id);
		}
		ids.parallelStream()
		.forEach(i -> {
			updating_expectedDevices.add(i);
			JSONObject jsonObj;
			try {
				jsonObj = InternetGetterThing.getDevice(i);
				// Make sure we actually got it
				if (jsonObj == null)
					LOG.error("Got null from InternetGetterThing; cannot create Device instance for {}", i);
				else {
					Device newDevice = makeDevice(i, type, jsonObj);
					updating_devices.put(i, newDevice);
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});
	}
	
	/**
	 * Fills the sites map.
	 * @throws JSONException 
	 */
	private void makeSites() throws JSONException {
		LOG.info("Getting sites/zones");
		JSONArray siteArray = InternetGetterThing.getSites();
		// Process each site
		for (int i = 0; i < siteArray.length(); i++) {
			JSONObject next = (JSONObject) siteArray.get(i);
			Site newSite = makeSite(next);
			if (newSite != null)
				updating_sites.put(newSite.getId(), newSite);
		}
		LOG.info("Finished getting sites, {} in total", updating_sites.size());
	}
	
	/**
	 * Fills the devices map.
	 * @throws JSONException 
	 */
	private void makeDevices() throws JSONException {
		LOG.info("Getting devices");
		// First, turn it into a regular array so we can process it in parallel
		JSONObject deviceObject = InternetGetterThing.getDevices();
		ArrayList<Object[]> jsonArray = new ArrayList<>();
		for (DeviceType type: DeviceType.values()) {
			JSONArray devices = deviceObject.getJSONArray(type.getLabel());
			jsonArray.add(new Object[] {devices, type});
		}
		// Now process the array
		jsonArray.parallelStream()
		.forEach(i -> {
			JSONArray devices = (JSONArray) i[0];
			DeviceType type = (DeviceType) i[1];
			try {
				makeDevices(devices, type);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});
		LOG.info("Finished getting devices, expected {}, {} in total", updating_expectedDevices.size(), updating_devices.size());
	}
	
	@Override
	public void run() {
    	updating = true;
    	updating_devices = new HashMap<>();
    	updating_zones= new HashMap<>();
    	updating_sites= new HashMap<>();
    	updating_expectedDevices= new HashSet<>();
		try {
			makeSites();
			makeDevices();
			manager.update();
			finder.update(new ArrayList<Site>(updating_sites.values()), new ArrayList<Device>(updating_devices.values()));
			lastUpdateTime = LocalDateTime.now();
			devices = updating_devices;
			zones = updating_zones;
			sites = updating_sites;
			expectedDevices = updating_expectedDevices;
			updating = false;
		} catch (JSONException e) {
			LOG.error("Caught JSONException when parsing data from server", e);
		}
	}

	public HashMap<String, Device> getDevices() { return devices; }
	public HashMap<String, Zone> getZones() { return zones; }
	public HashMap<String, Site> getSites() { return sites; }
	public HashSet<String> getExpectedDevices() { return expectedDevices; }
	
	public boolean isUpdating() { return updating; }
	public LocalDateTime getLastUpdateTime() { return lastUpdateTime; }
}
