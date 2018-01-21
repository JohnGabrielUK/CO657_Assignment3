package uk.co.johngabriel.co657a3.model;

import uk.co.johngabriel.co657a3.model.json.Device;
import uk.co.johngabriel.co657a3.model.json.Site;
import uk.co.johngabriel.co657a3.model.json.Zone;

/**
 * Representation of a problem somewhere on the site.
 * @author John Gabriel
 */
public class Problem {
	private Site site;
	private Zone zone;
	private Device device;
	private String message;
	private byte type;
	
	private static final byte TYPE_SITE = 1;
	private static final byte TYPE_ZONE = 2;
	private static final byte TYPE_DEVICE = 3;
	
	public Problem(Device device, String message) {
		this.device = device;
		this.message = message;
		type = TYPE_DEVICE;
	}
	
	public Problem(Zone zone, String message) {
		this.zone = zone;
		this.message = message;
		type = TYPE_ZONE;
	}
	
	public Problem(Site site, String message) {
		this.site = site;
		this.message = message;
		type = TYPE_SITE;
	}
	
	public Site getSite() { return site; }
	public Zone getZone() { return zone; }
	public Device getDevice() { return device; }
	public String getMessage() { return message; }
	public byte getType() { return type; }
}
