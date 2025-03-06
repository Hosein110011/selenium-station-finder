package ir.niopdc.gslatlon.controller;

import com.fasterxml.jackson.databind.SequenceWriter;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import ir.niopdc.gslatlon.domain.FuelStation;
import ir.niopdc.gslatlon.domain.FuelStationRepository;
import ir.niopdc.gslatlon.domain.GeoDto;
import ir.niopdc.gslatlon.domain.LocationDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.*;

@RestController
@RequestMapping("/check")
public class CheckController {

    @Autowired
    FuelStationRepository fuelStationRepository;

    private Random random = new Random();

    @GetMapping("")
    public String check() throws InterruptedException {

        List<FuelStation> fuelStations = fuelStationRepository.findAll();

        int count = 0;

        for (FuelStation station : fuelStations) {
            if (station.getLatitude() == null || station.getLongitude() == null) {
                continue;
            }
            System.out.println(station);
            GeoDto response = callApi(Float.parseFloat(station.getLatitude()), Float.parseFloat(station.getLongitude()));
//            System.out.println(response);
            if (!response.getItems().isEmpty()) {
//                GeoDto geoDto = new GeoDto();
                LocationDto mainResponse = response.getItems().get(0);
//                ObjectMapper objectMapper = new ObjectMapper();
//                objectMapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
//                JsonNode geoDto = objectMapper.readTree(geo);
                if (!arabicToPersianConvertor(mainResponse.getRegion()).contains(arabicToPersianConvertor(station.getCity()))
                        && !arabicToPersianConvertor(mainResponse.getRegion()).contains(arabicToPersianConvertor(station.getProvince()))) {
                    station.setLatitude(null);
                    station.setLongitude(null);
                    fuelStationRepository.save(station);
//                    System.out.println(station.getName());
                    count++;
                }

            }

            Thread.sleep(random.nextInt(1000, 10000));
        }

        System.out.println("count: " + count);

        return "success";
    }


    private GeoDto callApi(float lat, float lon) {
        String url = "https://api.neshan.org/v1/search?term=جایگاه سوخت&lat=" + lat + "&lng=" + lon;
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Api-Key", "service.8321e56c9fc1452d8c56dab93bed28f3");

        HttpEntity entity = new HttpEntity<>(headers);

        GeoDto resp = restTemplate.exchange(url, HttpMethod.GET, entity, GeoDto.class).getBody();

        if (resp != null) {
            return resp;
        } else {
            url = "https://api.neshan.org/v1/search?term=پمپ بنزین&lat=" + lat + "&lng=" + lon;
            resp = restTemplate.exchange(url, HttpMethod.GET, entity, GeoDto.class).getBody();
            if (resp != null) {
                return resp;
            } else {
                url = "https://api.neshan.org/v1/search?term=پمپ گاز&lat=" + lat + "&lng=" + lon;
                resp = restTemplate.exchange(url, HttpMethod.GET, entity, GeoDto.class).getBody();
                if (resp != null) {
                    return resp;
                } else {
                    return null;
                }
            }
        }
    }

    private String arabicToPersianConvertor(String arabic) {
        return arabic.replace('ك', 'ک')
                .replace('ي', 'ی');
    }

}
