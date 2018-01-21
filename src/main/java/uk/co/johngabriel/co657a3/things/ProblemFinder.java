package uk.co.johngabriel.co657a3.things;

import java.time.LocalDateTime;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.co.johngabriel.co657a3.model.Problem;
import uk.co.johngabriel.co657a3.model.json.DataType;
import uk.co.johngabriel.co657a3.model.json.Device;
import uk.co.johngabriel.co657a3.model.json.IdealStatus;
import uk.co.johngabriel.co657a3.model.json.Site;
import uk.co.johngabriel.co657a3.model.json.Zone;

/**
 * Class responsible for registering problems across sites, zones and devices.
 * @author John Gabriel
 */
public class ProblemFinder {
	private static final Logger LOG = LoggerFactory.getLogger(ProblemFinder.class);
	private ArrayList<Problem> problems;
	
	public ProblemFinder() {
		problems = new ArrayList<>();
	}
	
	public ArrayList<Problem> getProblems() { return problems; }
	
	private void checkDevice(Device device, Site site) {
		if (device.getLastConnection().isBefore(LocalDateTime.now().minusMinutes(5)))
			problems.add(new Problem(device, 
							"The device hasn't connected for at least five minutes."
							));
	}
	
	/**
	 * @return Whether the device, as a whole, is okay.
	 */
	private boolean checkDeviceAverage(Device device, DataType type) {
		Site site = device.getSite();
		IdealStatus status = device.getStatus(type);
		if (status == IdealStatus.OVER) {
			String message;
			switch (type) {
			case GAS: message = "The %s is too gassy."; break;
			case MOISTURE: message = "The %s are too wet."; break;
			case LUMOSITY: message = "It's too bright in the %s."; break;
			case TEMPERATURE: message = "The %s is too hot."; break;
			case HUMIDITY: message = "The %s is too humid."; break;
			default: message = "The readings are above the specified limits."; break;
			}
			problems.add(new Problem(device, String.format(message, device.getZone().getName())));
			return false;
		}
		else if (status == IdealStatus.UNDER) {
			String message;
			switch (type) {
			case GAS: message = "The %s isn't gassy enough."; break;
			case MOISTURE: message = "The %s need watering."; break;
			case LUMOSITY: message = "The %s needs more light."; break;
			case TEMPERATURE: message = "The %s is too cold."; break;
			case HUMIDITY: message = "The %s needs to be more humid."; break;
			default: message = "The readings are below the specified limits."; break;
			}
			problems.add(new Problem(device, String.format(message, device.getName())));
			return false;
		}
		else if (status == IdealStatus.EXTREME) {
			problems.add(new Problem(site,
					String.format("%s is reporting extreme %s data.", device.getName(), type.getLabel()))
					);
			return false;
		}
		else if (status == IdealStatus.LATE) {
			problems.add(new Problem(site,
					String.format("%s is not reporting valid %s data.", device.getName(), type.getLabel()))
					);
			return false;
		}
		else return true;
	}
	
	/**
	 * @return Whether the zone, as a whole, is okay.
	 */
	private boolean checkZoneAverage(Zone zone, DataType type) {
		Site site = zone.getSite();
		IdealStatus status = zone.getStatus(type);
		if (status == IdealStatus.OVER) {
			String message;
			switch (type) {
			case GAS: message = "The %s in %s is too gassy."; break;
			case MOISTURE: message = "The %s in %s is too moist."; break;
			case LUMOSITY: message = "It's too bright in the %s in %s."; break;
			case TEMPERATURE: message = "The %s in %s is too hot."; break;
			case HUMIDITY: message = "The %s in %s is too humid."; break;
			default: message = "The readings are above the specified limits."; break;
			}
			problems.add(new Problem(zone, String.format(message, zone.getName(), zone.getSite().getName())));
			return false;
		}
		else if (status == IdealStatus.UNDER) {
			String message;
			switch (type) {
			case GAS: message = "The %s in %s isn't gassy enough."; break;
			case MOISTURE: message = "The %s in %s is too dry."; break;
			case LUMOSITY: message = "The %s in %s needs more light."; break;
			case TEMPERATURE: message = "The %s in %s is too cold."; break;
			case HUMIDITY: message = "The %s in %s needs to be more humid."; break;
			default: message = "The readings are below the specified limits."; break;
			}
			problems.add(new Problem(zone, String.format(message, zone.getName(), zone.getSite().getName())));
			return false;
		}
		else if (status == IdealStatus.EXTREME) {
			problems.add(new Problem(site,
					String.format("%s is reading extreme %s data.", site.getName(), type.getLabel()))
					);
			return false;
		}
		else if (status == IdealStatus.LATE) {
			problems.add(new Problem(site,
					String.format("%s is not receiving valid %s data.", site.getName(), type.getLabel()))
					);
			return false;
		}
		else return true;
	}
	
	/**
	 * @return Whether the given site is okay
	 */
	private boolean checkSiteAverage(Site site, DataType type) {
		LOG.debug("Checking site {} average for {}", type.name(), site.getId());
		// If this site doesn't have any sensors for the given DataType, move on
		if (!site.hasData(type)) return true;
		IdealStatus status = site.getStatus(type);
		if (status == IdealStatus.OVER) {
			String message;
			switch (type) {
			case GAS: message = "%s is too gassy."; break;
			case MOISTURE: message = "%s is too moist."; break;
			case LUMOSITY: message = "It's too bright in %s."; break;
			case TEMPERATURE: message = "%s is too hot."; break;
			case HUMIDITY: message = "%s is too humid."; break;
			default: message = "The readings in %s above the specified limits."; break;
			}
			problems.add(new Problem(site, String.format(message, site.getName())));
			return false;
		}
		else if (status == IdealStatus.UNDER) {
			String message;
			switch (type) {
			case GAS: message = "%s isn't gassy enough."; break;
			case MOISTURE: message = "%s is not moist enough."; break;
			case LUMOSITY: message = "%s needs more light."; break;
			case TEMPERATURE: message = "%s is too cold."; break;
			case HUMIDITY: message = "%s is not humid enough."; break;
			default: message = "The readings in %s are below the specified limits."; break;
			}
			problems.add(new Problem(site, String.format(message, site.getName())));
			return false;
		}
		else if (status == IdealStatus.EXTREME) {
			problems.add(new Problem(site,
					String.format("%s is reading extreme %s data.", site.getName(), type.getLabel()))
					);
			return false;
		}
		else if (status == IdealStatus.LATE) {
			problems.add(new Problem(site,
					String.format("%s is not receiving valid %s data.", site.getName(), type.getLabel()))
					);
			return false;
		}
		else return true;
	}
	
	private void checkZone(Zone zone, DataType type) {
		boolean zoneIsOkay = checkZoneAverage(zone, type);
			// If the zone itself is okay, we can look for individual problems
			if (zoneIsOkay) {
				zone.getDevices().stream()
				.filter(i -> i.hasData(type))
				.forEach(i -> checkDeviceAverage(i, type));
			}
	}
	
	private void checkSite(Site site) {
		for (DataType type: DataType.values()) {
			boolean siteIsOkay = checkSiteAverage(site, type);
			// If the site itself is okay, we can look for individual problems
			if (siteIsOkay) {
				site.getZones().stream()
				.filter(i -> i.hasData(type))
				.forEach(i -> checkZone(i, type));
			}
		}
		// We also need to know if any devices within the site are malfunctioning
		for (Device device: site.getDevices())
			checkDevice(device, site);
	}
	
	public void update(ArrayList<Site> sites, ArrayList<Device> devices) {
		LOG.info("Finding problems");
		problems.clear();
		for (Site next: sites)
			checkSite(next);
		LOG.info("Finished finding problems");
	}
}
