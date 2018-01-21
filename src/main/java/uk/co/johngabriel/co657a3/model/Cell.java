package uk.co.johngabriel.co657a3.model;

/**
 * @author John Gabriel
 */
public class Cell {
	private Object contents;
	private String link;
	private boolean isLink;
	
	public Cell(Object contents) {
		this.contents = contents;
		isLink = false;
	}
	
	public Cell(Object contents, String link) {
		this.contents = contents;
		this.link = link;
		isLink = true;
	}
	
	public Object getContents() { return contents; }
	public String getLink() { return link; }
	public boolean isLink() { return isLink; }
}
