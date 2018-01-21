package uk.co.johngabriel.co657a3.things;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.SocketException;
import java.net.URL;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.co.johngabriel.co657a3.model.json.DataResolution;


/**
 * Handles all the interactions with shed.kent.ac.uk.
 * 
 * References:
 * https://www.mkyong.com/java/how-to-send-http-request-getpost-in-java/
 * 
 * @author John Gabriel
 */
public class InternetGetterThing {
	private static final Logger LOG = LoggerFactory.getLogger(InternetGetterThing.class);
	
	private static final String USER_AGENT = "Mozilla/5.0";
	
	private static final String SITE_URL = "http://shed.kent.ac.uk/sites";
	private static final String DEVICES_URL = "http://shed.kent.ac.uk/devices";
	private static final String DEVICE_URL = "http://shed.kent.ac.uk/device/";
	
	public static String makeGetRequest(String url) throws SocketException, IOException {
		LOG.debug("Making GET request to {}", url);
		URL urlObject = new URL(url);
		// Construct the request
		HttpURLConnection connection = (HttpURLConnection) urlObject.openConnection();
		connection.setRequestMethod("GET");
		connection.setRequestProperty("User-Agent", USER_AGENT);
		// Get the response
		int responseCode = connection.getResponseCode();
		LOG.trace("Response from {} was {}", url, responseCode);
		// Read the response into a string
		BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
		StringBuffer response = new StringBuffer();
		String inputLine;
		while ((inputLine = br.readLine()) != null)
			response.append(inputLine);
		br.close();
		// Convert the response to a JSON object and return it
		String responseString = response.toString();
		return responseString;
	}
	
	public static JSONArray getJSONArray(String url) throws JSONException {
		String jsonString;
		try {
			jsonString = makeGetRequest(url);
		} catch (SocketException e) {
			LOG.error("Got SocketException for {}; probably the server crapping out again");
			return null;
		} catch (IOException e) {
			LOG.error("Got {} for {}", e.getMessage(), url);
			e.printStackTrace();
			return null;
		}
		return new JSONArray(jsonString);
	}
	
	public static JSONObject getJSONObject(String url) throws JSONException {
		String jsonString;
		try {
			jsonString = makeGetRequest(url);
		} catch (SocketException e) {
			LOG.error("Got SocketException for {}; probably the server crapping out again", url);
			return null;
		} catch (IOException e) {
			LOG.error("Got {} for {}", e.getMessage(), url);
			e.printStackTrace();
			return null;
		}
		return new JSONObject(jsonString);
	}
	
	public static JSONArray getSites() throws JSONException {
		return getJSONArray(SITE_URL);
	}
	
	public static JSONObject getDevices() throws JSONException {
		return getJSONObject(DEVICES_URL);
	}
	
	public static JSONObject getDevice(String deviceId) throws JSONException {
		return getJSONObject(DEVICE_URL + deviceId);
	}
	
	public static JSONObject getDeviceData(String deviceId, DataResolution resolution) throws JSONException {
		String resString;
		switch (resolution) {
		case EVERY_MINUTE: resString = "minute"; break;
		case EVERY_10MINUTES: resString = "10minute"; break;
		case EVERY_HOUR: resString = "hour"; break;
		default:
			LOG.error("getDeviceData for {} was called with invalid resolution option {}; defaulting to 0", deviceId, resolution);
			resString = "minute";
			break;
		}
		return getJSONObject(DEVICE_URL + deviceId + "/" + resString);
	}
}
