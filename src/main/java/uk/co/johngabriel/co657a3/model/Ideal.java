package uk.co.johngabriel.co657a3.model;

import uk.co.johngabriel.co657a3.model.json.DataType;

/**
 * States what the minimum and maximum boundaries are
 * for a given site.
 * @author John Gabriel
 */
public class Ideal {
	public static final byte TYPE_SITE = 0;
	public static final byte TYPE_ZONE = 1;
	public static final byte TYPE_DEVICE = 2;
	public static final byte DONT_CARE = 0;
	public static final byte MUST_BE_DAY = 1;
	public static final byte MUST_BE_NIGHT = 2;
	public static final byte MUST_BE_SUMMER = 1;
	public static final byte MUST_BE_WINTER = 2;
	
	private String id; // Not linking directly because the devices get updated so often
	private String siteId;
	private String zoneId;
	private byte type;
	private DataType dataType;
	private double min;
	private double max;
	private byte requiredTime;
	private byte requiredSeason;
	
	public Ideal(byte type, String id, DataType dataType, double min, double max) {
		this.type = type;
		this.id = id;
		this.dataType = dataType;
		this.min = min;
		this.max = max;
		requiredTime = DONT_CARE;
		requiredSeason = DONT_CARE;
	}
	
	public Ideal(String siteId, String zoneId, DataType dataType, double min, double max) {
		this.type = TYPE_ZONE;
		this.siteId = siteId;
		this.zoneId = zoneId;
		this.dataType = dataType;
		this.min = min;
		this.max = max;
		requiredTime = DONT_CARE;
		requiredSeason = DONT_CARE;
	}
	
	public Ideal(byte type, String id, DataType dataType, double min, double max, byte requiredTime, byte requiredSeason) {
		this.type = type;
		this.id = id;
		this.dataType = dataType;
		this.min = min;
		this.max = max;
		this.requiredTime = requiredTime;
		this.requiredSeason = requiredSeason;
	}
	
	public Ideal(String siteId, String zoneId, DataType dataType, double min, double max, byte requiredTime, byte requiredSeason) {
		this.type = TYPE_ZONE;
		this.siteId = siteId;
		this.zoneId = zoneId;
		this.dataType = dataType;
		this.min = min;
		this.max = max;
		this.requiredTime = requiredTime;
		this.requiredSeason = requiredSeason;
	}

	public byte getType() { return type; }
	public String getSiteId() { return siteId; }
	public String getZoneId() { return zoneId; }
	public String getId() { return id; }
	public DataType getDataType() { return dataType; }
	public double getMin() { return min; }
	public double getMax() { return max; }
	public byte getRequiredTime() { return requiredTime; }
	public byte getRequiredSeason() { return requiredSeason; }
	
	public boolean requirementsMatch(boolean isDay, boolean isSummer) {
		if (requiredTime == DONT_CARE && requiredSeason == DONT_CARE) return true;
		if (requiredTime == MUST_BE_DAY && !isDay) return false;
		if (requiredTime == MUST_BE_NIGHT && isDay) return false;
		if (requiredSeason == MUST_BE_SUMMER && !isSummer) return false;
		if (requiredSeason == MUST_BE_WINTER && isSummer) return false;
		return true;
	}
}
