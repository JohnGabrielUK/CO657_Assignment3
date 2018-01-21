package uk.co.johngabriel.co657a3.things;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import uk.co.johngabriel.co657a3.model.Ideal;
import uk.co.johngabriel.co657a3.model.json.DataType;
import uk.co.johngabriel.co657a3.model.json.Device;
import uk.co.johngabriel.co657a3.model.json.Site;
import uk.co.johngabriel.co657a3.model.json.Zone;

/**
 * Manages ideal values.
 * @author John Gabriel
 */
public class Idealator {
	private static final int HOUR_DAY_START = 7;
	private static final int HOUR_DAY_END = 19;
	private static final int MONTH_SUMMER_START = 5;
	private static final int MONTH_SUMMER_END = 10;
	
	private static List<Ideal> ideals = Arrays.asList(new Ideal[] {
		new Ideal(Ideal.TYPE_SITE, "gh1", DataType.GAS, 0, 1),
		new Ideal(Ideal.TYPE_SITE, "gh1", DataType.HUMIDITY, 30, 40),
		new Ideal(Ideal.TYPE_SITE, "gh1", DataType.TEMPERATURE, 280.15, 302.15, Ideal.DONT_CARE, Ideal.MUST_BE_SUMMER),
		new Ideal(Ideal.TYPE_SITE, "gh1", DataType.TEMPERATURE, 280.15, 302.15, Ideal.MUST_BE_DAY, Ideal.MUST_BE_WINTER),
		new Ideal(Ideal.TYPE_SITE, "gh1", DataType.TEMPERATURE, 281.15, 283.15, Ideal.MUST_BE_NIGHT, Ideal.MUST_BE_WINTER),
		new Ideal(Ideal.TYPE_SITE, "gh1", DataType.MOISTURE, 10, 60),
		new Ideal(Ideal.TYPE_SITE, "gh1", DataType.LUMOSITY, 0, 350),
		new Ideal(Ideal.TYPE_SITE, "gh2", DataType.GAS, 0, 1),
		new Ideal(Ideal.TYPE_SITE, "gh2", DataType.HUMIDITY, 30, 40),
		new Ideal(Ideal.TYPE_SITE, "gh2", DataType.TEMPERATURE, 280.15, 291.15),
		new Ideal(Ideal.TYPE_SITE, "gh2", DataType.MOISTURE, 8, 53),
		new Ideal(Ideal.TYPE_SITE, "gh2", DataType.LUMOSITY, 0, 350),
		new Ideal(Ideal.TYPE_SITE, "gh3", DataType.GAS, 0, 1),
		new Ideal(Ideal.TYPE_SITE, "gh3", DataType.HUMIDITY, 30, 40),
		new Ideal(Ideal.TYPE_SITE, "gh3", DataType.TEMPERATURE, 285.15, 300.15),
		new Ideal(Ideal.TYPE_SITE, "gh3", DataType.MOISTURE, 10, 60),
		new Ideal(Ideal.TYPE_SITE, "gh3", DataType.LUMOSITY, 0, 200),
		new Ideal("house", "store", DataType.TEMPERATURE, 283, 295),
		new Ideal("house", "store", DataType.LUMOSITY, 0, 195, Ideal.MUST_BE_DAY, Ideal.DONT_CARE),
		new Ideal("house", "store", DataType.LUMOSITY, 0, 5, Ideal.MUST_BE_NIGHT, Ideal.DONT_CARE),
		new Ideal("outside", "heap", DataType.TEMPERATURE, 290, 320),
		new Ideal("outside", "field", DataType.TEMPERATURE, 268, 305),
		new Ideal("outside", "field", DataType.MOISTURE, 65, 73)
	});
    
	/**
	 * @return Whether it's daytime right now.
	 */
    private static boolean isDaytime() {
    	int hour = LocalDateTime.now().getHour();
    	return (hour > HOUR_DAY_START && hour < HOUR_DAY_END);
    }
    
    /**
     * @return Whether it's summer right now.
     */
    private static boolean isSummer() {
    	int month = LocalDateTime.now().getMonthValue();
    	return (month > MONTH_SUMMER_START && month < MONTH_SUMMER_END);
    }

    /**
     * Determines the ideal range for the given site.
     */
    public static Ideal getSiteIdeal(Site site, DataType type) {
    	return ideals.stream()
    			.filter(i -> i.getType() == Ideal.TYPE_SITE)
    			.filter(i -> i.getId().equals(site.getId()))
    			.filter(i -> i.getDataType() == type)
    			.filter(i -> i.requirementsMatch(isDaytime(), isSummer()))
    			.findFirst().orElse(null);
    }

    /**
     * Determines the ideal range for the given zone.
     */
    public static Ideal getZoneIdeal(Zone zone, DataType type) {
    	// Is there a specific zone ideal that can serve our needs?
    	Ideal ideal = ideals.stream()
    			.filter(i -> i.getType() == Ideal.TYPE_ZONE)
    			.filter(i -> i.getSiteId().equals(zone.getSite().getId()))
    			.filter(i -> i.getZoneId().equals(zone.getId()))
    			.filter(i -> i.getDataType() == type)
    			.filter(i -> i.requirementsMatch(isDaytime(), isSummer()))
    			.findFirst().orElse(null);
    	if (ideal != null) return ideal;
    	// If not, is there a site ideal that could work?
    	return getSiteIdeal(zone.getSite(), type);
    }

    /**
     * Determines the ideal range for the given device.
     */
    public static Ideal getDeviceIdeal(Device device, DataType type) {
    	// Is there a specific device ideal that can serve our needs?
    	Ideal ideal = ideals.stream()
    			.filter(i -> i.getType() == Ideal.TYPE_DEVICE)
    			.filter(i -> i.getId().equals(device.getId()))
    			.filter(i -> i.getDataType() == type)
    			.filter(i -> i.requirementsMatch(isDaytime(), isSummer()))
    			.findFirst().orElse(null);
    	if (ideal != null) return ideal;
    	// If not, is there a zone/site ideal that could work?
    	return getZoneIdeal(device.getZone(), type);
    }
}
