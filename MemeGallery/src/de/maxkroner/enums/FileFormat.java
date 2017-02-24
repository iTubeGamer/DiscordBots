package de.maxkroner.enums;

public enum FileFormat {
	ONE_PER_LINE, SEPERATED_BY_FREE_LINE;

	@Override
	public String toString() {
		switch (this) {
		case ONE_PER_LINE:
			return "one joke per line";
		case SEPERATED_BY_FREE_LINE:
			return "jokes are sperated by a free line";
		default:
			return "";
		}
	}

}
