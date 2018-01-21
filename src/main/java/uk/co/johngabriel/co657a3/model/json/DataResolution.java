package uk.co.johngabriel.co657a3.model.json;

/**
 * Enumeration of the resolution of a device's readings.
 * @author John Gabriel
 */
public enum DataResolution {
	EVERY_MINUTE("every minute", 30),
	EVERY_10MINUTES("every 10 minutes", 300),
	EVERY_HOUR("every hour", 30*60);
	
	private String label;
	private long margin;
	
	DataResolution(String label, long margin) {
		this.label = label;
		this.margin = margin;
	}
	
	public String getLabel() { return label; }
	public long getMargin() { return margin; }
}
