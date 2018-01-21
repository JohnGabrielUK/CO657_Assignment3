package uk.co.johngabriel.co657a3.model.json;

/**
 * Enumeration of the type of device.
 * @author John Gabriel
 */
public enum DeviceType {
	GAS("gas"),
	SOLAR("solar"),
	HYDROMETER("hydrometer"),
	TEMPHUMID("tempHumid"),
	LUMOSITY("lumosity");
	
	private String label;
	
	DeviceType(String label) {
		this.label = label;
	}
	
	public String getLabel() { return label; }
}
