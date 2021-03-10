package com.tts.transitapp.service;

import com.tts.transitapp.model.*;
import com.tts.transitapp.model.coordinates.GeocodingResponse;
import com.tts.transitapp.model.coordinates.Location;
import com.tts.transitapp.model.distance.DistanceResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/*
Serialization is the process of turning some object into a data format that
can be restored later. Deserialization is the reverse of that process,
taking data structured from some format (JSON in this case), and rebuilding
it into an object.

JSON Page
http://developer.itsmarta.com/BRDRestService/RestBusRealTimeService/GetAllBus
API Documentation - tells whether api is working
https://developers.google.com/maps/documentation/geocoding/get-api-key
 */

@Service
public class TransitService {

    @Value("${transit_url}")
    public String transitUrl;

    @Value("${geocoding_url}")
    public String geocodingUrl;

    @Value("${distance_url}")
    public String distanceUrl;

    @Value("${google_api_key}")
    public String googleApiKey;

    /*
    getBuses will hit the MARTA API and return everything.
     */
    private List<Bus> getBuses(){
        RestTemplate restTemplate = new RestTemplate();
        Bus[] buses = restTemplate.getForObject(transitUrl, Bus[].class);
        return Arrays.asList(buses);
    }

    /*
    getCoordinates will use the Geocoding API
     */
    private Location getCoordinates(String description) {
        description = description.replace(" ", "+");
        String url = geocodingUrl + description + "+GA&key=" + googleApiKey;
        RestTemplate restTemplate = new RestTemplate();
        GeocodingResponse response = restTemplate.getForObject(url, GeocodingResponse.class);
        return response.results.get(0).geometry.location;
    }

    /*
    GetDistance will use the Distance Matrix API to find the distance
    between two coordinates
     */
    private double getDistance(Location origin, Location destination) {
        String url = distanceUrl + "origins=" + origin.lat + "," + origin.lng
                + "&destinations=" + destination.lat + "," + destination.lng
                + "&key=" + googleApiKey;
        RestTemplate restTemplate = new RestTemplate();
        DistanceResponse response = restTemplate.getForObject(url, DistanceResponse.class);
        return response.rows.get(0).elements.get(0).distance.value * 0.000621371;
    }

    /*
     GetNearbyBuses that ties all of our API calls together to do something useful
     */
    public List<Bus> getNearbyBuses(BusRequest request) {
        /*
        The method first calls the getBuses method to get back a list of all
        active buses.
         */
        List<Bus> allBuses = this.getBuses();
        /*
        Then, it gets the location of the person based on what they pass in
        through the request object.
         */
        Location personLocation = this.getCoordinates(request.address + " " + request.city);
        List<Bus> nearbyBuses = new ArrayList<>();
        /*
        Now is where we get into the real logic. For each bus in the system,
        we need to determine if it is close to user or not. So, we start by
        building a Location object from fields in the Bus object.
         */
        for (Bus bus : allBuses) {
            Location busLocation = new Location();
            busLocation.lat = bus.LATITUDE;
            busLocation.lng = bus.LONGITUDE;
            /*
            We then perform a fuzzy distance comparison between each bus and the user.
             */
            double latDistance = Double.parseDouble(busLocation.lat) - Double.parseDouble(personLocation.lat);
            double lngDistance = Double.parseDouble(busLocation.lng) - Double.parseDouble(personLocation.lng);
            if (Math.abs(latDistance) <= 0.02 && Math.abs(lngDistance) <= 0.02) {
                /*
                From there, we check the actual distance to see if it is less than a
                mile. If it is, we consider it a nearby bus and add it to the list.
                 */
                double distance = getDistance(busLocation, personLocation);
                if (distance <= 1) {
                    bus.distance = (double) Math.round(distance * 100) / 100;
                    nearbyBuses.add(bus);
                }

            }
        }
        /*
        Finally, we sort the buses by distance so that we can easily display
        them on the front end in that order.
         */
        Collections.sort(nearbyBuses, new BusComparator());
        return nearbyBuses;
    }


}//end TransitService class
