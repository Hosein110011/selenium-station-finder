package ir.niopdc.gslatlon.domain;

import lombok.Data;

import java.util.Map;

@Data
public class LocationDto {

    private String title;
    private String address;
    private String category;
    private String type;
    private String region;
    private Map<String, String> location;

}
