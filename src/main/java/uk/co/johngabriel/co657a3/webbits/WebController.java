package uk.co.johngabriel.co657a3.webbits;

import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import uk.co.johngabriel.co657a3.model.json.DataResolution;
import uk.co.johngabriel.co657a3.model.json.Device;
import uk.co.johngabriel.co657a3.model.json.Site;
import uk.co.johngabriel.co657a3.model.json.Zone;
import uk.co.johngabriel.co657a3.things.DataManager;
import uk.co.johngabriel.co657a3.things.Prettyfier;

@Controller
public class WebController {
	private static final Logger LOG = LoggerFactory.getLogger(WebController.class);
	
    private DataManager dataManagerThing;
    
    public WebController() {
    	dataManagerThing = new DataManager();
    }

	@RequestMapping("/")
	public String index(Model model) {
		LOG.info("Responding to index request");
		dataManagerThing.update();
		model.addAttribute("sites", Prettyfier.getSiteTable(dataManagerThing.getSites()));
		model.addAttribute("problems", Prettyfier.getProblemTable(dataManagerThing.getProblemFinder().getProblems()));
		return "index";
	}

	@RequestMapping("/sites")
	public String sites(Model model) {
		LOG.info("Responding to /sites request");
		dataManagerThing.update();
		model.addAttribute("sites", Prettyfier.getSiteTable(dataManagerThing.getSites()));
		return "sites";
	}

	@RequestMapping("/zones")
	public String zones(Model model) {
		LOG.info("Responding to /zones request");
		dataManagerThing.update();
		model.addAttribute("zones", Prettyfier.getZoneTable(dataManagerThing.getZones()));
		return "zones";
	}

	@RequestMapping("/devices")
	public String devices(Model model) {
		LOG.info("Responding to /devices request");
		dataManagerThing.update();
		model.addAttribute("devices", Prettyfier.getDeviceTable(dataManagerThing.getDevices()));
		return "devices";
	}

	@RequestMapping("/problems")
	public String problems(Model model) {
		LOG.info("Responding to /problems request");
		dataManagerThing.update();
		model.addAttribute("problems", Prettyfier.getProblemTable(dataManagerThing.getProblemFinder().getProblems()));
		return "problems";
	}

	@RequestMapping("/site/{siteName}")
	public String sitePage(@PathVariable String siteName, Model model) {
		LOG.info("Responding to /site/{} request", siteName);
		dataManagerThing.update();
		Site siteObject = dataManagerThing.getSite(siteName);
		model.addAttribute("site", siteObject);
		model.addAttribute("info", Prettyfier.getSiteInfo(siteObject));
		model.addAttribute("averages", Prettyfier.getSiteAverages(siteObject));
		model.addAttribute("zones", Prettyfier.getSiteZones(siteObject));
		model.addAttribute("misc", Prettyfier.getSiteChartMisc(siteObject));
		return "site";
	}

	@RequestMapping("/zone")
	public String zonePage(@RequestParam(value="site", required=true) String siteName, @RequestParam(value="zone", required=true) String zoneName, Model model) {
		LOG.info("Responding to /zone?site={}&zone={} request", siteName, zoneName);
		dataManagerThing.update();
		Site siteObject = dataManagerThing.getSite(siteName);
		Zone zoneObject = siteObject.getZone(zoneName);
		model.addAttribute("zone", zoneObject);
		model.addAttribute("info", Prettyfier.getZoneInfo(zoneObject));
		model.addAttribute("devices", Prettyfier.getDeviceTable(zoneObject.getDevices()));
		model.addAttribute("averages", Prettyfier.getZoneAverages(zoneObject));
		model.addAttribute("misc", Prettyfier.getZoneChartMisc(zoneObject));
		return "zone";
	}

	@RequestMapping("/device/{deviceName}")
	public String devicePage(@PathVariable String deviceName, Model model) {
		LOG.info("Responding to /device/{} request", deviceName);
		dataManagerThing.update();
		Device deviceObject = dataManagerThing.getDevice(deviceName);
		model.addAttribute("device", deviceObject);
		model.addAttribute("info", Prettyfier.getDeviceInfo(deviceObject));
		model.addAttribute("current", Prettyfier.getCurrentData(deviceObject));
		model.addAttribute("readings", Prettyfier.getDeviceReadingTable(deviceObject));
		model.addAttribute("misc", Prettyfier.getDeviceChartMisc(deviceObject));
		return "device";
	}

	@RequestMapping("/api/sites")
	public String sitesTable(Model model) {
		LOG.info("Responding to /api/sites request");
		dataManagerThing.update();
		model.addAttribute("sites", Prettyfier.getSiteTable(dataManagerThing.getSites()));
		return "api :: sites";
	}

	@RequestMapping("/api/zones")
	public String zonesTable(Model model) {
		LOG.info("Responding to /api/zones request");
		dataManagerThing.update();
		model.addAttribute("zones", Prettyfier.getZoneTable(dataManagerThing.getZones()));
		return "api :: zones";
	}

	@RequestMapping("/api/devices")
	public String deviceTable(Model model) {
		LOG.info("Responding to /api/devices request");
		dataManagerThing.update();
		model.addAttribute("devices", Prettyfier.getDeviceTable(dataManagerThing.getDevices()));
		return "api :: devices";
	}

	@RequestMapping("/api/problems")
	public String problemTable(Model model) {
		LOG.info("Responding to /api/problems request");
		dataManagerThing.update();
		model.addAttribute("problems", Prettyfier.getProblemTable(dataManagerThing.getProblemFinder().getProblems()));
		return "api :: problems";
	}

	@RequestMapping("/api/site/{siteName}")
	public String siteAPI(@PathVariable String siteName, Model model) {
		LOG.info("Responding to /api/site/{} request", siteName);
		dataManagerThing.update();
		Site siteObject = dataManagerThing.getSite(siteName);
		model.addAttribute("site", siteObject);
		model.addAttribute("info", Prettyfier.getSiteInfo(siteObject));
		model.addAttribute("averages", Prettyfier.getSiteAverages(siteObject));
		model.addAttribute("zones", Prettyfier.getSiteZones(siteObject));
		model.addAttribute("misc", Prettyfier.getSiteChartMisc(siteObject));
		return "api :: site";
	}

	@RequestMapping("/api/zone")
	public String zoneAPI(@RequestParam(value="site", required=true) String siteName, @RequestParam(value="zone", required=true) String zoneName, Model model) {
		LOG.info("Responding to /api/zone?site={}&zone={} request", siteName, zoneName);
		dataManagerThing.update();
		Site siteObject = dataManagerThing.getSite(siteName);
		Zone zoneObject = siteObject.getZone(zoneName);
		model.addAttribute("zone", zoneObject);
		model.addAttribute("info", Prettyfier.getZoneInfo(zoneObject));
		model.addAttribute("devices", Prettyfier.getDeviceTable(zoneObject.getDevices()));
		model.addAttribute("averages", Prettyfier.getZoneAverages(zoneObject));
		model.addAttribute("misc", Prettyfier.getZoneChartMisc(zoneObject));
		return "api :: zone";
	}

	@RequestMapping("/api/device/{deviceName}")
	public String deviceAPI(@PathVariable String deviceName, Model model) {
		LOG.info("Responding to /api/device/{} request", deviceName);
		dataManagerThing.update();
		Device deviceObject = dataManagerThing.getDevice(deviceName);
		model.addAttribute("device", deviceObject);
		model.addAttribute("info", Prettyfier.getDeviceInfo(deviceObject));
		model.addAttribute("current", Prettyfier.getCurrentData(deviceObject));
		model.addAttribute("readings", Prettyfier.getDeviceReadingTable(deviceObject));
		model.addAttribute("misc", Prettyfier.getDeviceChartMisc(deviceObject));
		return "api :: device";
	}

	@RequestMapping("/api/updateIndicator")
	public String updateIndicator(Model model) {
		LOG.info("Responding to /api/updateIndicator request");
		dataManagerThing.update();
		model.addAttribute("isUpdating", dataManagerThing.getUpdater().isUpdating());
		model.addAttribute("lastUpdate", dataManagerThing.getUpdater().getLastUpdateTime());
		return "fragments :: updateIndicator";
	}

	@CrossOrigin(origins = "*")
	@RequestMapping("/json/markers")
	@ResponseBody
	public List<HashMap<String, Object>> mapMarkers() {
		LOG.info("Responding to /json/markers request");
		dataManagerThing.update();
		return Prettyfier.getDeviceMapMarkers(dataManagerThing.getDevices());
	}
	
	@CrossOrigin(origins = "*")
	@RequestMapping("/json/device")
	@ResponseBody
	public HashMap<String, Object> deviceTypes(@RequestParam(value="id", required=true) String deviceName) {
		LOG.info("Responding to /json/device/{} request", deviceName);
		dataManagerThing.update();
		Device deviceObject = dataManagerThing.getDevice(deviceName);
		return Prettyfier.getDeviceData(deviceObject);
	}
	
	@CrossOrigin(origins = "*")
	@RequestMapping("/json/deviceData")
	@ResponseBody
	public HashMap<String, Object> deviceReadings(@RequestParam(value="id", required=true) String deviceName, @RequestParam(value="res", required=true) int res) {
		LOG.info("Responding to /json/deviceData/{}/{} request", deviceName, res);
		dataManagerThing.update();
		DataResolution resolution = DataResolution.EVERY_MINUTE;
		switch (res) {
		case 0: resolution = DataResolution.EVERY_MINUTE; break;
		case 1: resolution = DataResolution.EVERY_10MINUTES; break;
		case 2: resolution = DataResolution.EVERY_HOUR; break;
		}
		Device deviceObject = dataManagerThing.getDevice(deviceName);
		return Prettyfier.getChartData(deviceObject, resolution);
	}
	
	@CrossOrigin(origins = "*")
	@RequestMapping("/json/zoneData")
	@ResponseBody
	public HashMap<String, Object> zoneReadings(@RequestParam(value="site", required=true) String siteName, @RequestParam(value="zone", required=true) String zoneName, @RequestParam(value="res", required=true) int res) {
		LOG.info("Responding to /json/zoneData/{}/{} request", zoneName, res);
		dataManagerThing.update();
		DataResolution resolution = DataResolution.EVERY_MINUTE;
		switch (res) {
		case 0: resolution = DataResolution.EVERY_MINUTE; break;
		case 1: resolution = DataResolution.EVERY_10MINUTES; break;
		case 2: resolution = DataResolution.EVERY_HOUR; break;
		}
		Zone zoneObject = dataManagerThing.getZone(siteName + "/" + zoneName);
		return Prettyfier.getZoneChartData(zoneObject, resolution);
	}
	
	@CrossOrigin(origins = "*")
	@RequestMapping("/json/siteData")
	@ResponseBody
	public HashMap<String, Object> siteReadings(@RequestParam(value="site", required=true) String siteName, @RequestParam(value="res", required=true) int res) {
		LOG.info("Responding to /json/siteReadings/{} request", siteName, res);
		dataManagerThing.update();
		DataResolution resolution = DataResolution.EVERY_MINUTE;
		switch (res) {
		case 0: resolution = DataResolution.EVERY_MINUTE; break;
		case 1: resolution = DataResolution.EVERY_10MINUTES; break;
		case 2: resolution = DataResolution.EVERY_HOUR; break;
		}
		Site siteObject = dataManagerThing.getSite(siteName);
		return Prettyfier.getSiteChartData(siteObject, resolution);
	}
	
	@CrossOrigin(origins = "*")
	@RequestMapping("/setTempType")
	@ResponseBody
	public String deviceTypes(@RequestParam(value="type", required=true) byte type) {
		LOG.info("Responding to /setTempType/{} request", type);
		Prettyfier.setTempType(type);
		return "yea boi";
	}

	@RequestMapping("/header")
	public String header(Model model) {
		return "fragments :: header";
	}

}