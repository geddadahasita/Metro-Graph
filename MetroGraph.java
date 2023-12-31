import com.google.gson.*;
import java.net.*;
import java.util.*;

//update GitHub
/*
 * NEXT STEPS:
 *  -implement MetroEdge
 *  -implement pathBetweenStations functions for same line (straightforward) and different line (fastest with Djikstra's Algorithm)
 */
/**
 * Class that retrieves and stores all Metro Stations and categorizes them by
 * line color.
 */
public class MetroGraph {
	private class MetroNode {
		private String station;
		private MetroNode redPrev;
		private MetroNode redNext;
		private MetroNode bluePrev;
		private MetroNode blueNext;
		private MetroNode yellowPrev;
		private MetroNode yellowNext;
		private MetroNode orangePrev;
		private MetroNode orangeNext;
		private MetroNode greenPrev;
		private MetroNode greenNext;
		private MetroNode silverPrev;
		private MetroNode silverNext;

        public MetroNode(String station) {
            this.station = station;
			
        }

		public String toString() {
			return station;
		}
    }

	private static final String WMATA_API_KEY = "58dc2560ab8e4627854e8b8c93526816";

	
	// unsorted list of all stations 
	private ArrayList<String> stations = new ArrayList<>();
	// unsorted map of station names and their respective line color
	private Map<String, ArrayList<String>> lineMap = new LinkedHashMap<>();

	// sorted lists for each line
	private ArrayList<String> red = new ArrayList<>();
	private ArrayList<String> blue = new ArrayList<>();
	private ArrayList<String> yellow = new ArrayList<>();
	private ArrayList<String> orange = new ArrayList<>();
	private ArrayList<String> green = new ArrayList<>();
	private ArrayList<String> silver = new ArrayList<>();

	/**
	 * ACTUAL MAP FOR GRAPH
	 */
	//list of all MetroNodes - INCLUDES DUPLICATES 
	private ArrayList<MetroNode> metroNodeList = new ArrayList<>();
	//undirected graph of MetroNodes
	private Map<MetroNode, LinkedList<MetroNode>> metroMap = new LinkedHashMap<>();

	public MetroGraph() {
		retrieveStations();
		createStationLists();
		sortStationLists();
		createMetroGraph();//combine these two?
	} 
	public void retrieveStations() {
		try {
			String wmataURL = "https://api.wmata.com/Rail.svc/json/jStations";
			URL url = new URL(wmataURL + "?api_key=" + WMATA_API_KEY);

			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("GET");
			connection.connect();

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
				String name, color;
				ArrayList<String> colors;

				for (int i = 0; i < stationsArray.size(); i++) {
					obj = stationsArray.get(i).getAsJsonObject();
					name = obj.get("Name").getAsString();
					colors = new ArrayList<>();
					colors.add(obj.get("LineCode1").getAsString());

					if (!obj.get("LineCode2").isJsonNull()) {
						colors.add(obj.get("LineCode2").getAsString());
					}
					if (!obj.get("LineCode3").isJsonNull()) {
						colors.add(obj.get("LineCode3").getAsString());
					}

					if (!lineMap.containsKey(name)) {
						stations.add(name);
						lineMap.put(name, colors);
					}
					else {
						for (int j = 0; j < colors.size(); j++) {
							color = colors.get(j);
							if (!lineMap.get(name).contains(color)) {
								lineMap.get(name).add(color);
							}
						}
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
		ArrayList<String> tempColors; 
		String tempName;

		for (int i = 0; i < stations.size(); i++) {
			tempName = stations.get(i);
			tempColors = lineMap.get(tempName);

			if (tempColors.contains("RD")) {
				red.add(tempName);
			}
			if (tempColors.contains("BL")) {
				blue.add(tempName);
			}
			if (tempColors.contains("YL")) {
				yellow.add(tempName);
			}
			if (tempColors.contains("OR")) {
				orange.add(tempName);
			}
			if (tempColors.contains("GR")) {
				green.add(tempName);
			}
			if (tempColors.contains("SV")) {
				silver.add(tempName);
			}
		}
	}

	private void sortRed() {
		ArrayList<String> tempRed = new ArrayList<>();
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
		ArrayList<String> tempBlue = new ArrayList<>();

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
		ArrayList<String> tempYellow = new ArrayList<>();
		int x = yellow.indexOf("Pentagon");
		int y = yellow.indexOf("Huntington");

		for (int i = y; i >= x; i--) {
			tempYellow.add(yellow.get(i));
		}

		tempYellow.add("L'Enfant Plaza");
		tempYellow.add("Archives-Navy Memorial-Penn Quarter");
		tempYellow.add("Gallery Pl-Chinatown");

		x = yellow.indexOf("Mt Vernon Sq 7th St-Convention Center");

		for (int i = x; i < x + 5; i++) {
			tempYellow.add(yellow.get(i));
		}

		tempYellow.add("Fort Totten");

		yellow = tempYellow;
	}

	private void sortOrange() {
		ArrayList<String> tempOrange = new ArrayList<>();
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
		ArrayList<String> tempGreen = new ArrayList<>();
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
		ArrayList<String> tempSilver = new ArrayList<>();
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

	public int findStationIndex(String station) {
		int index = 0;

		while (index < stations.size() && !stations.get(index).equals(station)) {
			index++;
		}

		return index;
	}

	public void sortStationLists() {
		sortRed();
		sortBlue();
		sortYellow();
		sortOrange();
		sortGreen();
		sortSilver();
	}
	
	public void createNodeHelper(ArrayList<String> line) {
		MetroNode temp;

		for (int i = 0; i < line.size(); i++) {
			if (metroNodeList.size() == 0 || findMetroNode(line.get(i)) == -1) {
				temp = new MetroNode(line.get(i));
				metroNodeList.add(temp);
				if (!metroMap.containsKey(temp)) {
					metroMap.put(temp, new LinkedList<>());
				}
			}
		}
	}
	public void createMetroNodes() {
		createNodeHelper(red);
		createNodeHelper(blue);
		createNodeHelper(yellow);
		createNodeHelper(orange);
		createNodeHelper(green);
		createNodeHelper(silver);
    }
	
	public int findMetroNode(String station) {
		int index = 0;

		while (index < metroNodeList.size() && !metroNodeList.get(index).station.equals(station)) {
			index++;
		}

		if (index == metroNodeList.size()) {
			return -1;
		}

		return index;
	}

	public void createMetroGraph() {
		createMetroNodes();
		MetroNode prevNode, curNode, nextNode;
		int prevIndex, curIndex, nextIndex;
		LinkedList<MetroNode> temp;
		
		for (int i = 0; i < red.size(); i++) {
			curIndex = findMetroNode(red.get(i));
			curNode = metroNodeList.get(curIndex);
			temp = metroMap.get(curNode);

			if (i != red.size() - 1) {
				nextIndex = findMetroNode(red.get(i + 1));
				nextNode = metroNodeList.get(nextIndex);
				curNode.redNext = nextNode;
				
				if (!temp.contains(nextNode)) {
					temp.add(nextNode);
				}
			}
			if (i != 0) {
				prevIndex = findMetroNode(red.get(i - 1));
				prevNode = metroNodeList.get(prevIndex);
				curNode.redPrev = prevNode;
				if (!temp.contains(prevNode)) {
					temp.add(prevNode);
				}
			}
		}

		for (int i = 0; i < blue.size(); i++) {
			curIndex = findMetroNode(blue.get(i));
			curNode = metroNodeList.get(curIndex);
			temp = metroMap.get(curNode);

			if (i != blue.size() - 1) {
				nextIndex = findMetroNode(blue.get(i + 1));
				nextNode = metroNodeList.get(nextIndex);
				curNode.blueNext = nextNode;
				
				if (!temp.contains(nextNode)) {
					temp.add(nextNode);
				}
			}
			if (i != 0) {
				prevIndex = findMetroNode(blue.get(i - 1));
				prevNode = metroNodeList.get(prevIndex);
				curNode.bluePrev = prevNode;
				if (!temp.contains(prevNode)) {
					temp.add(prevNode);
				}
			}
		}

		for (int i = 0; i < yellow.size(); i++) {
			curIndex = findMetroNode(yellow.get(i));
			curNode = metroNodeList.get(curIndex);
			temp = metroMap.get(curNode);

			if (i != yellow.size() - 1) {
				nextIndex = findMetroNode(yellow.get(i + 1));
				nextNode = metroNodeList.get(nextIndex);
				curNode.yellowNext = nextNode;
				
				if (!temp.contains(nextNode)) {
					temp.add(nextNode);
				}
			}
			if (i != 0) {
				prevIndex = findMetroNode(yellow.get(i - 1));
				prevNode = metroNodeList.get(prevIndex);
				curNode.yellowPrev = prevNode;
				if (!temp.contains(prevNode)) {
					temp.add(prevNode);
				}
			}
		}

		for (int i = 0; i < orange.size(); i++) {
			curIndex = findMetroNode(orange.get(i));
			curNode = metroNodeList.get(curIndex);
			temp = metroMap.get(curNode);

			if (i != orange.size() - 1) {
				nextIndex = findMetroNode(orange.get(i + 1));
				nextNode = metroNodeList.get(nextIndex);
				curNode.orangeNext = nextNode;
				
				if (!temp.contains(nextNode)) {
					temp.add(nextNode);
				}
			}
			if (i != 0) {
				prevIndex = findMetroNode(orange.get(i - 1));
				prevNode = metroNodeList.get(prevIndex);
				curNode.orangePrev = prevNode;
				if (!temp.contains(prevNode)) {
					temp.add(prevNode);
				}
			}
		}

		for (int i = 0; i < green.size(); i++) {
			curIndex = findMetroNode(green.get(i));
			curNode = metroNodeList.get(curIndex);
			temp = metroMap.get(curNode);

			if (i != green.size() - 1) {
				nextIndex = findMetroNode(green.get(i + 1));
				nextNode = metroNodeList.get(nextIndex);
				curNode.greenNext = nextNode;
				
				if (!temp.contains(nextNode)) {
					temp.add(nextNode);
				}
			}
			if (i != 0) {
				prevIndex = findMetroNode(green.get(i - 1));
				prevNode = metroNodeList.get(prevIndex);
				curNode.greenPrev = prevNode;
				if (!temp.contains(prevNode)) {
					temp.add(prevNode);
				}
			}
		}

		for (int i = 0; i < silver.size(); i++) {
			curIndex = findMetroNode(silver.get(i));
			curNode = metroNodeList.get(curIndex);
			temp = metroMap.get(curNode);

			if (i != silver.size() - 1) {
				nextIndex = findMetroNode(silver.get(i + 1));
				nextNode = metroNodeList.get(nextIndex);
				curNode.silverNext = nextNode;
				
				if (!temp.contains(nextNode)) {
					temp.add(nextNode);
				}
			}
			if (i != 0) {
				prevIndex = findMetroNode(silver.get(i - 1));
				prevNode = metroNodeList.get(prevIndex);
				curNode.silverPrev = prevNode;
				if (!temp.contains(prevNode)) {
					temp.add(prevNode);
				}
			}
		}
	}

	public LinkedList<MetroNode> getConnectedStations(String station) {
        int index = findStationIndex(station);

		return metroMap.get(metroNodeList.get(index));
    }

	/**
	 * Main method for testing.
	 * @param args cla - not needed
	 */
	public static void main(String[] args) {
		MetroGraph metroStations = new MetroGraph();
		ArrayList<String> stations = metroStations.stations;
		// System.out.println(metroStations.stations);		
		// System.out.println(metroStations.lineMap);
		List<MetroNode> nodeList = metroStations.metroNodeList;
		Map<MetroNode, LinkedList<MetroNode>> map = metroStations.metroMap;
		// System.out.println(map);
		// System.out.println(map.size());
		int index = metroStations.findMetroNode("Metro Center");
		MetroNode node = nodeList.get(index);
		System.out.println(node.redPrev);
		// System.out.println(node.redNext);
		System.out.println(node.yellowPrev);
		
		
		// System.out.println(stations.get(97));
		// System.out.println(metroStations.getConnectedStations(stations.get(97)));
	}
}

