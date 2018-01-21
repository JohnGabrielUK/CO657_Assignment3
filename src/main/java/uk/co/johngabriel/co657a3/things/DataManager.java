package uk.co.johngabriel.co657a3.things;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Repository;

import uk.co.johngabriel.co657a3.model.json.Device;
import uk.co.johngabriel.co657a3.model.json.Site;
import uk.co.johngabriel.co657a3.model.json.Zone;

/**
 * @author John Gabriel
 */
@Repository
public class DataManager {
	DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
	
	private DataUpdater updater;
	private ProblemFinder problemFinder;
	private Idealator idealator;
	
	private HashMap<String, Device> devices;
	private HashMap<String, Zone> zones;
	private HashMap<String, Site> sites;
	private HashSet<String> expectedDevices;
    
    public DataManager() {
		problemFinder = new ProblemFinder();
		idealator = new Idealator();
		updater = new DataUpdater(this, problemFinder, idealator);
    	ScheduledExecutorService ex = Executors.newSingleThreadScheduledExecutor();
    	ex.scheduleAtFixedRate(updater, 0, 1, TimeUnit.MINUTES);
    }
    
    public void update() {
		devices = updater.getDevices();
		zones = updater.getZones();
		sites = updater.getSites();
		expectedDevices = updater.getExpectedDevices();
    }
	
	public boolean hasDevice(String id) { return devices.containsKey(id); }
	public Device getDevice(String id) { return devices.get(id); }

	public boolean hasZone(String id) { return zones.containsKey(id); }
	public Zone getZone(String id) { return zones.get(id); }
	
	public boolean hasSite(String id) { return sites.containsKey(id); }
	public Site getSite(String id) { return sites.get(id); }

	public ArrayList<Device> getDevices() { return new ArrayList<Device>(devices.values()); }
	public ArrayList<Zone> getZones() { return new ArrayList<Zone>(zones.values()); }
	public ArrayList<Site> getSites() { return new ArrayList<Site>(sites.values()); }
	public HashSet<String> getExpectedDevices() { return expectedDevices; }

	public DataUpdater getUpdater() { return updater; }
	public ProblemFinder getProblemFinder() { return problemFinder; }
	public Idealator getIdealator() { return idealator; }
	
	public static void main(String[] args) {
		DataManager dm = new DataManager();
		dm.update();
	}
	
}
