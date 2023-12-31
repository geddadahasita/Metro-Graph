import java.util.*;

/**
 * Class that stores Station Information for each WMATA (Metro) Station.
 * Includes name, color line, and code.
 */
public class StationInfo {

	private String name;
	private ArrayList<String> lineColors;

	public StationInfo(String name, ArrayList<String> lineColors) {
		this.name = name;
		this.lineColors = lineColors;
	}

	public String getName() {
		return name;
	}

	public ArrayList<String> getLineColors() {
		return lineColors;
	}

	public void addLineColors(ArrayList<String> existing, ArrayList<String> lineCols) {
		String color;
		for (int i = 0; i < lineCols.size(); i++) {
			color = lineCols.get(i);
			if (!existing.contains(color)) {
				existing.add(color);

			}
		}
	}

	public String toString() {
		StringBuilder stationInfo = new StringBuilder();

		stationInfo.append(name + " " + lineColors);

		return stationInfo.toString();
	}
	
}