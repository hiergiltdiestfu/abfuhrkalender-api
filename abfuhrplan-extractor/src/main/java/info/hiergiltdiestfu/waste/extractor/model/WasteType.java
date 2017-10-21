package info.hiergiltdiestfu.waste.extractor.model;

public enum WasteType {

	RESTABFALL("Restabfall", "Restmüll"), 
	BIOABFALL("Bio-Tonne", "Bioabfall", "Biotonne", "Biomüll"), 
	DUALES_SYSTEM("Gelbe Tonne", "Duales System", "Gelber Sack", "Verpackungen"), 
	PAPIER("Blaue Tonne", "Pappe und Papier"), 
	SONSTIGE("Sonstige"),
	TEST("Einhornpüreeverpackungen");
	
	private String[] aliases;
	
	private WasteType(String... aliases) {
		this.aliases = aliases;
	}
	
	public static WasteType fromValue(String s) {
		for (WasteType t : WasteType.values()) {
			for (String v : t.aliases) {
				if (v.equalsIgnoreCase(s)) {
					return t;
				}
			}
		}
		return SONSTIGE;
	}
}
