package uk.co.johngabriel.co657a3.model.json;

/**
 * Enumeration of the type of data that is being read.
 * @author John Gabriel
 */
public enum DataType {
	GAS("gas", "gas_values", "gas_scale", 1),
	MOISTURE("moisture", "moisture_value", "moisture_scale", 2),
	LUMOSITY("light", "light_value", "light_scale", 10),
	TEMPERATURE("temperature", "temperature_value", "temp_scale", 2),
	HUMIDITY("humidity", "humidity_value", "humidity_scale", 2),
	SOLAR("solar", "solar_value", "solar_scale", 10);
	
	private String label;
	private String valueName;
	private String scaleName;
	private double sdMultiplier;
	
	DataType(String label, String valueName, String scaleName, double sdMultiplier) {
		this.label = label;
		this.valueName = valueName;
		this.scaleName = scaleName;
		this.sdMultiplier = sdMultiplier;
	}
	
	public String getLabel() { return label; }
	public String getValueName() { return valueName; }
	public String getScaleName() { return scaleName; }
	public double getSDMultiplier() { return sdMultiplier; } // This is how many times over the sd a reading can stray before it's considered 'extreme'.
}
