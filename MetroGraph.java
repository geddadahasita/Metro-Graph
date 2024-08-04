import java.net.*;
import java.util.*;
import com.google.gson.*;

/*
 * NEXT STEPS:
 * better names for objects and variables.
 * implement Djikstra's Algorithm to find shortest path between two stations BASED ON TIME - need to put time between stations as "weight" for edges.
 * can also add fares as another way to weight graphs
*/
/**
 * MetroGraph class.
 */
public class MetroGraph {
	/**
	 * API key.
	 */
	private static final String WMATA_API_KEY = "58dc2560ab8e4627854e8b8c93526816";

	/**
	 * List of all MetroNodes - contains duplicates to account for stations with
	 * multiple lines.
	 */
	private LinkedList<MetroNode> stationList;

	/**
	 * Hashmap that maps stations to indices of adjacency matrix.
	 */
	private Map<MetroNode, Integer> stationIndices;

	/**
	 * Global index variable for station indicies map.
	 */
	int index;

	/**
	 * Lists for each line.
	 */
	private LinkedList<String> red = new LinkedList<>();
	private LinkedList<String> blue = new LinkedList<>();
	private LinkedList<String> yellow = new LinkedList<>();
	private LinkedList<String> orange = new LinkedList<>();
	private LinkedList<String> green = new LinkedList<>();
	private LinkedList<String> silver = new LinkedList<>();

	/**
	 * MAIN MAP FOR GRAPH.
	 * Directed weighted graph of MetroNodes as an adjacency matrix.
	 */
	private int[][] adjacencyMatrix;
	//convert to MetroEdge 

	/**
	 * MetroGraph constructor
	 */
	public MetroGraph() {
		stationList = new LinkedList<>();
		stationIndices = new LinkedHashMap<>();
		index = 0;

		red = new LinkedList<>();
		blue = new LinkedList<>();
		yellow = new LinkedList<>();
		orange = new LinkedList<>();
		green = new LinkedList<>();
		silver = new LinkedList<>();
	}

	public void createMetroNode(String name, String color, String code) {
		//exclude incorect yellow stations
		if (!((name.equals("Shaw-Howard U")
				|| name.equals("U Street/African-Amer Civil War Memorial/Cardozo")
				|| name.equals("Columbia Heights") || name.equals("Georgia Ave-Petworth")
				|| name.equals("Fort Totten")) && color.equals("YL"))) {
			MetroNode node = new MetroNode(name, color, code, new LinkedList<>());
			stationList.add(node);
			stationIndices.put(node, index);
			index++;
		}
	}

	/**
	 * API call to obtain stations and build metro node list and station indicies
	 * map.
	 */
	public void retrieveStations() {
		try {
			String wmataURL = "https://api.wmata.com/Rail.svc/json/jStations";
			URL url = new URL(wmataURL + "?api_key=" + WMATA_API_KEY);

			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("GET");

			int statusCode = connection.getResponseCode();

			if (statusCode == 200) {
				StringBuilder sb = new StringBuilder();
				Scanner scan = new Scanner(url.openStream());

				while (scan.hasNext()) {
					sb.append(scan.nextLine() + " ");
				}

				sb.replace(sb.length() - 1, sb.length(), "");
				scan.close();

				JsonParser parser = new JsonParser();
				JsonObject object = (JsonObject) parser.parse(sb.toString());
				JsonArray stationsArray = object.getAsJsonArray("Stations");

				JsonObject obj;
				String tempName, tempColor, tempCode;

				for (int i = 0; i < stationsArray.size(); i++) {
					obj = stationsArray.get(i).getAsJsonObject();
					tempName = obj.get("Name").getAsString();
					tempColor = obj.get("LineCode1").getAsString();
					tempCode = obj.get("Code").getAsString();

					createMetroNode(tempName, tempColor, tempCode);

					if (!obj.get("LineCode2").isJsonNull()) {
						tempColor = obj.get("LineCode2").getAsString();
						createMetroNode(tempName, tempColor, tempCode);
					}

					if (!obj.get("LineCode3").isJsonNull()) {
						tempColor = obj.get("LineCode3").getAsString();
						createMetroNode(tempName, tempColor, tempCode);
					}
				}
			} else {
				System.out.println("Error: " + statusCode);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void createStationLists() {
		MetroNode tempNode;
		String tempName;
		String tempLine;

		for (int i = 0; i < stationList.size(); i++) {
			tempNode = stationList.get(i);
			tempName = tempNode.getName();
			tempLine = tempNode.getLine();

			if (tempLine.equals("RD")) {
				red.add(tempName);
			}
			if (tempLine.equals("BL")) {
				blue.add(tempName);
			}
			if (tempLine.equals("YL")) {
				yellow.add(tempName);
			}
			if (tempLine.equals("OR")) {
				orange.add(tempName);
			}
			if (tempLine.equals("GR")) {
				green.add(tempName);
			}
			if (tempLine.equals("SV")) {
				silver.add(tempName);
			}
		}
	}

	private void sortRed() {
		LinkedList<String> tempRed = new LinkedList<>();
		int x = red.indexOf("Shady Grove");

		for (int i = x; i >= 0; i--) {
			tempRed.add(red.get(i));
		}

		x = red.indexOf("Gallery Pl-Chinatown");

		for (int i = x; i < x + 3; i++) {
			tempRed.add(red.get(i));
		}

		tempRed.add("NoMa-Gallaudet U");

		for (int i = x + 3; i < red.size() - 1; i++) {
			tempRed.add(red.get(i));
		}

		red = tempRed;
	}

	private void sortBlue() {
		LinkedList<String> tempBlue = new LinkedList<>();
		tempBlue.add(blue.get(blue.size() - 1));
		tempBlue.add(blue.get(blue.size() - 2));

		int x = blue.indexOf("King St-Old Town");

		for (int i = x; i >= 0; i--) {
			tempBlue.add(blue.get(i));
		}

		x++;

		for (int i = x; i < blue.size() - 2; i++) {
			tempBlue.add(blue.get(i));
		}

		blue = tempBlue;
	}

	private void sortYellow() {
		LinkedList<String> tempYellow = new LinkedList<>();
		int x = yellow.indexOf("Pentagon");
		int y = yellow.indexOf("Huntington");

		for (int i = y; i >= x; i--) {
			tempYellow.add(yellow.get(i));
		}

		tempYellow.add("L'Enfant Plaza");
		tempYellow.add("Archives-Navy Memorial-Penn Quarter");
		tempYellow.add("Gallery Pl-Chinatown");
		tempYellow.add("Mt Vernon Sq 7th St-Convention Center");

		yellow = tempYellow;
	}

	private void sortOrange() {
		LinkedList<String> tempOrange = new LinkedList<>();
		int x = orange.indexOf("Court House");

		for (int i = orange.size() - 1; i >= x; i--) {
			tempOrange.add(orange.get(i));
		}

		x = orange.indexOf("Rosslyn");

		for (int i = x; i >= 0; i--) {
			tempOrange.add(orange.get(i));
		}

		x = orange.indexOf("Federal Triangle");

		for (int i = x; i < x + 13; i++) {
			tempOrange.add(orange.get(i));
		}

		orange = tempOrange;
	}

	private void sortGreen() {
		LinkedList<String> tempGreen = new LinkedList<>();
		int x = green.indexOf("Waterfront");

		for (int i = green.size() - 1; i >= x; i--) {
			tempGreen.add(green.get(i));
		}

		tempGreen.add("L'Enfant Plaza");
		tempGreen.add("Archives-Navy Memorial-Penn Quarter");
		tempGreen.add("Gallery Pl-Chinatown");

		x = green.indexOf("Mt Vernon Sq 7th St-Convention Center");

		for (int i = x; i < x + 5; i++) {
			tempGreen.add(green.get(i));

		}

		tempGreen.add("Fort Totten");

		x = green.indexOf("West Hyattsville");

		for (int i = x; i < 12; i++) {
			tempGreen.add(green.get(i));
		}

		green = tempGreen;
	}

	private void sortSilver() {
		LinkedList<String> tempSilver = new LinkedList<>();
		int x = silver.indexOf("Court House");

		for (int i = silver.size() - 1; i >= x; i--) {
			tempSilver.add(silver.get(i));
		}

		x = silver.indexOf("Rosslyn");

		for (int i = x; i >= 0; i--) {
			tempSilver.add(silver.get(i));
		}

		for (int i = silver.indexOf("Federal Triangle"); i < silver.indexOf("Court House"); i++) {
			tempSilver.add(silver.get(i));
		}

		silver = tempSilver;
	}

	public void sortStationLists() {
		sortRed();
		sortBlue();
		sortYellow();
		sortOrange();
		sortGreen();
		sortSilver();
	}

	public void assignValues(int i1, int i2) {
		adjacencyMatrix[i1][i2] = 1;
		adjacencyMatrix[i2][i1] = 1;
	}

	// need to consider line color!!!
	public MetroNode findNode(String name, String line) {
		MetroNode tempNode;

		for (int i = 0; i < stationList.size(); i++) {
			tempNode = stationList.get(i);

			if (tempNode.getName().equals(name) && tempNode.getLine().equals(line)) {
				return stationList.get(i);
			}
		}
		// will never reach this point
		return null;
	}

	public void createGraphHelper(LinkedList<String> lineList, String lineCode) {
		MetroNode prevNode, curNode, nextNode;
		int prevIndex, curIndex, nextIndex;

		for (int i = 0; i < lineList.size(); i++) {
			curNode = findNode(lineList.get(i), lineCode);
			curIndex = stationIndices.get(curNode);

			if (i != lineList.size() - 1) {
				nextNode = findNode(lineList.get(i + 1), lineCode);
				nextIndex = stationIndices.get(nextNode);

				// if (!curNode.getConnectingStations().contains(nextNode)) {
				curNode.connectingStations.add(nextNode);
				// }
				// determine if this is needed
				// curNode.redNext = nextNode;

				assignValues(curIndex, nextIndex);
			}
			if (i != 0) {
				prevNode = findNode(lineList.get(i - 1), lineCode);
				prevIndex = stationIndices.get(prevNode);

				// if (!curNode.getConnectingStations().contains(prevNode)) {
				curNode.connectingStations.add(prevNode);
				// }
				// determine if this is needed
				// curNode.redPrev = prevNode;

				assignValues(curIndex, prevIndex);
			}
		}
	}

	/**
	 * Assigns the previous and next fields for each line color for the MetroNodes
	 */
	public void createGraph() {
		adjacencyMatrix = new int[stationList.size()][stationList.size()];
		createGraphHelper(red, "RD");
		createGraphHelper(blue, "BL");
		createGraphHelper(yellow, "YL");
		createGraphHelper(orange, "OR");
		createGraphHelper(green, "GR");
		createGraphHelper(silver, "SV");
	}

	/**
	 * Assigns times between stations as weights to MetroEdges
	 */
	public void timeBetweenStations() {
		try {
			String wmataURL = "https://api.wmata.com/Rail.svc/json/jPath?FromStationCode=N06&ToStationCode=G05";
			URL url = new URL(wmataURL);

			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("GET");
			connection.setRequestProperty("api_key", WMATA_API_KEY);

			int statusCode = connection.getResponseCode();

			if (statusCode == 200) {
				System.out.println("yay");

				StringBuilder sb = new StringBuilder();
				Scanner scan = new Scanner(url.openStream());

				while (scan.hasNext()) {
					sb.append(scan.nextLine() + " ");
				}

				sb.replace(sb.length() - 1, sb.length(), "");
				scan.close();

				JsonParser parser = new JsonParser();
				JsonObject object = (JsonObject) parser.parse(sb.toString());
				JsonArray stationsArray = object.getAsJsonArray("Stations");
				// JsonObject obj;
				// String tempName, tempColor, tempCode;
				// LinkedList<String> tempColors;
				// MetroNode tempNode;
				// int index = 0;

				for (int i = 0; i < stationsArray.size(); i++) {
					// obj = stationsArray.get(i).getAsJsonObject();
					// tempName = obj.get("Name").getAsString();
					// tempColors = new LinkedList<>();
					// tempColors.add(obj.get("LineCode1").getAsString());
					// tempCode = obj.get("Code").getAsString();

					// if (!obj.get("LineCode2").isJsonNull()) {
					// tempColors.add(obj.get("LineCode2").getAsString());
					// }
					// if (!obj.get("LineCode3").isJsonNull()) {
					// tempColors.add(obj.get("LineCode3").getAsString());
					// }

					// if (!stationIndices.containsKey(tempName)) {
					// tempNode = new MetroNode(tempName, tempColors, tempCode, new LinkedList<>());
					// stationList.add(tempNode);
					// stationIndices.put(tempName, index);
					// index++;
					// }

				}

			} else {
				System.out.println("Error: " + statusCode);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void printMatrix() {
		for (int[] a : adjacencyMatrix) {
			for (int b : a) {
				System.out.print(b + " ");
			}
			System.out.println();
		}
	}

	public String toString() {
		StringBuilder graph = new StringBuilder();

		for (int i = 0; i < stationList.size(); i++) {
			graph.append(stationList.get(i) + ": " + stationList.get(i).getConnectingStations() + "\n");
		}

		graph.replace(graph.length() - 1, graph.length(), "");

		return graph.toString();
	}

	/**
	 * Main method for testing.
	 * 
	 * @param args cla - not needed
	 */
	public static void main(String[] args) {
		/**
		 * Initialize a MetroGraph object and set the graph up
		 */
		MetroGraph metroGraph = new MetroGraph();
		metroGraph.retrieveStations();
		metroGraph.createStationLists();
		metroGraph.sortStationLists();
		metroGraph.createGraph();

		// System.out.println(metroGraph.stationIndices);
		System.out.println(metroGraph); // essentially an adjacency list but with better readability
		/**
		 * Testing methods
		 */
		// metroGraph.printMatrix();

		// metroGraph.timeBetweenStations();
		// System.out.println(metroGraph.stationList); //list of all stations as
		// MetroNode objects
		// System.out.println("____________________________");
		// System.out.println(metroGraph.stationIndices); //HashMap of indices and their
		// respective stations for adjacency matrix
		// metroGraph.printMatrix(); // correct

	}
}
