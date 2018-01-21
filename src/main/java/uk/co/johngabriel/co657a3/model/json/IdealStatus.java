package uk.co.johngabriel.co657a3.model.json;

/**
 * The state of a Device, Zone or Site, with regard to a single DataType.
 * @author John Gabriel
 */
public enum IdealStatus {
	OKAY("O.K."),
	EXTREME("Giving extreme readings"),
	OVER("Over limit"),
	UNDER("Under limit"),
	DOES_NOT_SERVE("Not serving that type of data"),
	LATE("Late to give valid responses"),
	UNKNOWN("Unknown");
	
	private String label;
	
	IdealStatus(String label) {
		this.label = label;
	}
	
	public String getLabel() { return label; }
}
