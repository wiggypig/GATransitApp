package com.tts.transitapp.model.coordinates;

import lombok.Data;

import java.util.List;

@Data
public class GeocodingResponse {

    public List<Geocoding> results;

}//end GeocodingResponse class
