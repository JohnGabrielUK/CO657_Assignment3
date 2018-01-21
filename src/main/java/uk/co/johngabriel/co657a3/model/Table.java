package uk.co.johngabriel.co657a3.model;

/**
 * Neat little way of representing things that you might print in an HTML table.
 * @author John Gabriel
 */
public class Table {
	private String[] header;
	private Cell[][] rows;

	public Table(String[] header, Cell[][] rows) {
		this.header = header;
		this.rows = rows;
	}
	
	public String[] getHeader() { return header; }
	public Cell[][] getRows() { return rows; }
}
