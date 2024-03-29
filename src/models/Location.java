package models;


import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

public class Location {
    protected static String apiKey = "AIzaSyBmJFq8fk7l0fA9cIUldb4Io7Prga1FmSc";
    protected static String apiKeyBackup = "AIzaSyALdczrg5jCqau0hhcNVPilRiwmdlQdUiY";
    protected static String apiKeySecondBackup = "AIzaSyD4BwCaD4ZIhBgD8bI-CAjS4W_dfzxAxCM";
    private int noPlace;
    private String name;
    private double longitude;
    private double latitude;

    public Location(int noPlace, String name, double latitude, double longitude) {
        this.noPlace = noPlace;
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public static void main(String[] args) throws Exception {
        Location l1 = new Location(1, "client1", 45.17823, 5.74396);
        Location l2 = new Location(2, "client2", 45.21854, 5.66133);

        System.out.println(l2.getDistanceTo(l1, true));
        System.out.println(l1.getDistanceFrom(l2, true));
        System.out.println(l2.getDistanceTo(l1, true));
        System.out.println(l1.getDistanceFrom(l2, true));

    }

    public int getNoPlace() {
        return noPlace;
    }

    public void setNoPlace(int noPlace) {
        assert noPlace > 0;
        this.noPlace = noPlace;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    protected JSONObject gMapsAPICall(String origin, String destination, String apiKey) throws Exception {

        // Shortest path by time
        // https://maps.googleapis.com/maps/api/distancematrix/json?origins=45.17823,5.74396&destinations=45.21854,5.66133&key=AIzaSyBmJFq8fk7l0fA9cIUldb4Io7Prga1FmSc
        // Shortest path with alternatives
        // https://maps.googleapis.com/maps/api/directions/json?origin=45.17823,5.74396&destination=45.21854,5.66133&alternatives=true&key=AIzaSyBmJFq8fk7l0fA9cIUldb4Io7Prga1FmSc
        String apiUrl = "https://maps.googleapis.com/maps/api/directions/json";
        String s = String.format("%s?origin=%s&destination=%s&alternatives=true&key=%s", apiUrl, origin, destination, apiKey);
        URL url = null;
        try {
            url = new URL(s);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        // Read from URL
        assert url != null;
        Scanner scan = null;
        try {
            scan = new Scanner(url.openStream());
        } catch (IOException e) {
            e.printStackTrace();
        }

        String read = "";
        assert scan != null;
        while (scan.hasNext()) {
            read += scan.nextLine();
        }
        scan.close();

        // Build JSON
        JSONObject jsonObj = new JSONObject(read);
        if (!jsonObj.get("status").equals("OK")) {
            if (jsonObj.get("status").equals("OVER_QUERY_LIMIT")) {
                if (apiKey.equals(apiKeySecondBackup)) {
                    throw new Exception("API Call limitation quota has been reached");
                } else if (apiKey.equals(apiKeyBackup)) {
                    jsonObj = gMapsAPICall(origin, destination, apiKeySecondBackup);
                } else {
                    jsonObj = gMapsAPICall(origin, destination, apiKeyBackup);
                }
            } else {
                throw new Exception("API call failed");
            }

        }

        return jsonObj;
    }

    public int getDistanceTo(Location loc, boolean API) {
        int distanceResult = 0;
        if (API) {
            assert loc != null;
            // Prep URL
            String origin = String.format("%s,%s", this.getLatitude(), this.getLongitude());
            String destination = String.format("%s,%s", loc.getLatitude(), loc.getLongitude());
            try {
                JSONObject jsonObj = gMapsAPICall(origin, destination, apiKey);
                // Get distance results
                distanceResult= Integer.MAX_VALUE;
                JSONArray routes = (JSONArray) jsonObj.get("routes");
                for (int i = 0; i < routes.length(); i++) {
                    JSONObject subElem = (JSONObject) routes.get(i);
                    JSONArray legs = (JSONArray) subElem.get("legs");
                    subElem = (JSONObject) legs.get(0);
                    JSONObject distance = (JSONObject) subElem.get("distance");
                    if (distance.getInt("value") < distanceResult) {
                        distanceResult = distance.getInt("value");
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else {
            // Orthodromic distance between two points
            // Set the coordinates in radians
            double lat1 = Math.toRadians(this.getLatitude());
            double lon1 = Math.toRadians(this.getLongitude());
            double lat2 = Math.toRadians(loc.getLatitude());
            double lon2 = Math.toRadians(loc.getLongitude());
            //Haverside formula
            double a = Math.pow(Math.sin((lat2 - lat1) / 2), 2)
                    + Math.cos(lat1) * Math.cos(lat2) * Math.pow(Math.sin((lon2 - lon1) / 2), 2);
            double angle = Math.toDegrees(2 * Math.asin(Math.min(1, Math.sqrt(a))));
            // Calculate the distance in meters (nautical mile*1852)
            double distance = 1852 * 60 * angle;
            distanceResult= (int) distance;
        }
        return distanceResult;
    }


    public long getDistanceFrom(Location loc, boolean useAPI) throws Exception {
        return loc.getDistanceTo(this, useAPI);
    }
}