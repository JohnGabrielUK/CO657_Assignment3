package uk.co.johngabriel.co657a3.model.json;

import java.time.LocalDateTime;

import uk.co.johngabriel.co657a3.things.Prettyfier;

/**
 * A single reading from a device.
 * @author John Gabriel
 */
public class Reading {
	private LocalDateTime timestamp;
	private double reading;
	private DataType type;
	
	public Reading(LocalDateTime timestamp, double reading, DataType type) {
		this.timestamp = timestamp;
		this.reading = reading;
		this.type = type;
	}

	public LocalDateTime getTimestamp() { return timestamp; }
	public double getRawReading() { return reading; }
	public double getReading() { return Prettyfier.getReadingValue(this); }
	public DataType getType() { return type; }
}
