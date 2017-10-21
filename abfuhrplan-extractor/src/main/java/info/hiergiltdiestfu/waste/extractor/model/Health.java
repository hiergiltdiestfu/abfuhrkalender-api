package info.hiergiltdiestfu.waste.extractor.model;

public class Health {

	private int percent = 100;
	private String description = "Everything is fine.";
	
	public Health(int percent, String description) {
		super();
		this.percent = percent;
		this.description = description;
	}
	public int getPercent() {
		return percent;
	}
	public void setPercent(int percent) {
		this.percent = percent;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	
	
}
