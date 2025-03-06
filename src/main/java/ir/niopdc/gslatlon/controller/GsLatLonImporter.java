package ir.niopdc.gslatlon.controller;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SequenceWriter;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import ir.niopdc.gslatlon.domain.FuelStation;
import ir.niopdc.gslatlon.domain.FuelStationRepository;
import ir.niopdc.gslatlon.domain.GeoDto;
import lombok.SneakyThrows;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@RestController
@RequestMapping("/")
public class GsLatLonImporter {

    @Autowired
    FuelStationRepository fuelStationRepository;

    private CsvMapper csvMapper = new CsvMapper();
    private JsonMapper jsonMapper = new JsonMapper();

    @GetMapping("/")
    public String getGsLatLon() throws IOException {

        File file = new File("/home/hosein/Downloads/data.csv");
        File newCsvFile = new File("/home/hosein/Downloads/data-lat-lon.csv");

        CsvSchema schema = CsvSchema.emptySchema().withHeader().withColumnSeparator(',');

        MappingIterator<Map<String, String>> it = csvMapper.readerForMapOf(String.class)
                .with(schema)
                .readValues(file);

        for (Map station : it.readAll()) {
            System.out.println(station);
            String name = station.get("name").toString();
            List<Map<String, String>> response = callApi(name);
            System.out.println(response);
            FuelStation fuelStation = new FuelStation();
            fuelStation.setGsId(Integer.valueOf(station.get("gsId").toString()));
            fuelStation.setName(name);
            if (!response.isEmpty()) {
//                GeoDto geoDto = new GeoDto();
                 Map<String, String> geo = response.getFirst();
//                ObjectMapper objectMapper = new ObjectMapper();
//                objectMapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
//                JsonNode geoDto = objectMapper.readTree(geo);

                System.out.println(geo.get("lat"));
                System.out.println(geo.get("lon"));
                fuelStation.setLatitude(geo.get("lat"));
                fuelStation.setLongitude(geo.get("lon"));

            }
            CsvSchema fuelSchema = csvMapper.schemaFor(FuelStation.class).withColumnSeparator(',');

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(newCsvFile, true))) {
                SequenceWriter sequenceWriter = csvMapper.writer(fuelSchema).writeValues(writer);
                sneakyWrite(sequenceWriter, fuelStation);
            }
//            System.out.println(fuelStation);
        }

        return "success";

    }

    private List<Map<String, String>> callApi(String name) {
        String searchParam =  "جایگاه سوخت " + name;
        System.out.println(searchParam);
        String url = "https://nominatim.openstreetmap.org/search?q=" + searchParam + "&format=json";
        RestTemplate restTemplate = new RestTemplate();
        List<Map<String, String>> resp = restTemplate.getForObject(url, List.class);
        if (!resp.isEmpty()) {
            return resp;
        } else {
            searchParam = "جایگاه " + name;
            System.out.println(searchParam);
            url = "https://nominatim.openstreetmap.org/search?q=" + searchParam + "&format=json";
            resp = restTemplate.getForObject(url, List.class);
            if (!resp.isEmpty()) {
                return resp;
            } else {
                searchParam = "پمپ بنزین " + name;
                System.out.println(searchParam);
                url = "https://nominatim.openstreetmap.org/search?q=" + searchParam + "&format=json";
                resp = restTemplate.getForObject(url, List.class);
                if (!resp.isEmpty()) {
                    return resp;
                } else {
                    searchParam = "پمپ گاز " + name;
                    System.out.println(searchParam);
                    url = "https://nominatim.openstreetmap.org/search?q=" + searchParam + "&format=json";
                    return restTemplate.getForObject(url, List.class);
                }
            }
        }
    }

    @SneakyThrows
    private void sneakyWrite(SequenceWriter sequenceWriter, Object o) {
        sequenceWriter.write(o);
    }

    @GetMapping("/count")
    public int countRows() throws IOException {
        int count = 0;
//        File newCsvFile = new File("/home/hosein/Downloads/data-lat-lon2.csv");
//
//        CsvSchema schema = CsvSchema.emptySchema().withHeader().withColumnSeparator(',');
//
//        MappingIterator<Map<String, String>> it = csvMapper.readerForMapOf(String.class)
//                .with(schema)
//                .readValues(newCsvFile);
//
//        for (Map station : it.readAll()) {
//
//            if (station.get("latitude") != "" && station.get("longitude") != "") {
//                count++;
//            }
//        }
        List<FuelStation> fuelStations = fuelStationRepository.findAll();

        for (FuelStation fuelStation : fuelStations) {
            if (fuelStation.getLatitude() != null && fuelStation.getLongitude() != null) {
                count++;
            }
        }

        return count;
    }

    @GetMapping("/remain")
    public String getRemainGsLatLon() throws IOException {

        File file = new File("/home/hosein/Downloads/data-lat-lon2.csv");

        CsvSchema schema = CsvSchema.emptySchema().withHeader().withColumnSeparator(',');

        MappingIterator<Map<String, String>> it = csvMapper.readerForMapOf(String.class)
                .with(schema)
                .readValues(file);

        for (Map station : it.readAll()) {

            if (station.get("latitude") != "" && station.get("longitude") != "") {
                continue;
            }

            String name = station.get("name").toString();
            List<Map<String, String>> response = callApiAgain(name);
            System.out.println(response);
            FuelStation fuelStation = new FuelStation();
            fuelStation.setGsId(Integer.valueOf(station.get("gsId").toString()));
            fuelStation.setName(name);
            if (!response.isEmpty()) {
//                GeoDto geoDto = new GeoDto();
                Map<String, String> geo = response.getFirst();
//                ObjectMapper objectMapper = new ObjectMapper();
//                objectMapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
//                JsonNode geoDto = objectMapper.readTree(geo);

                System.out.println(geo.get("lat"));
                System.out.println(geo.get("lon"));
                fuelStation.setLatitude(geo.get("lat"));
                fuelStation.setLongitude(geo.get("lon"));

                CsvSchema fuelSchema = csvMapper.schemaFor(FuelStation.class).withColumnSeparator(',');

                try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, true))) {
                    SequenceWriter sequenceWriter = csvMapper.writer(fuelSchema).writeValues(writer);
                    sneakyWrite(sequenceWriter, fuelStation);
                }
            }

//            System.out.println(fuelStation);
        }

        return "success";

    }

    private List<Map<String, String>> callApiAgain(String name) {
        String searchParam;
        if (name.startsWith("0") || name.startsWith("1") || name.startsWith("2") || name.startsWith("3") || name.startsWith("4") || name.startsWith("5")
        || name.startsWith("6") || name.startsWith("7") || name.startsWith("8") || name.startsWith("9")) {
            StringBuilder stringBuilder = new StringBuilder();
            for (char ch : name.toCharArray()) {
                if (ch == '0' || ch == '1' || ch == '2' || ch == '3' || ch == '4' ||
                 ch == '5' || ch == '6' || ch == '7' || ch == '8' || ch == '9') {
                    stringBuilder.append(ch);
                    name = name.replaceAll(String.valueOf(ch), "");
                }
            }
            stringBuilder.reverse();
            searchParam = name + " " + stringBuilder.toString();
            System.out.println(searchParam);

//            searchParam = name;
            String url = "https://nominatim.openstreetmap.org/search?q=" + searchParam + "&format=json";
            RestTemplate restTemplate = new RestTemplate();
            return restTemplate.getForObject(url, List.class);

//            if (!resp.isEmpty()) {
//                return resp;
//            } else if (name.contains("اختصاصی")) {
//                System.out.println("name " + name);
//                name = name.replaceAll("اختصاصی", "");
//                searchParam = name;
//                System.out.println("search param " + searchParam);
//                url = "https://nominatim.openstreetmap.org/search?q=" + searchParam + "&format=json";
//                return restTemplate.getForObject(url, List.class);
//            }
        } else {
//            System.out.println("empty  " + name);
            return Collections.emptyList();
        }
    }

    @GetMapping("/region")
    public String addRegion() throws IOException {

        File file = new File("/home/hosein/Downloads/zone-fuel.csv");

        CsvSchema schema = CsvSchema.emptySchema().withHeader().withColumnSeparator(',');

        MappingIterator<Map<String, String>> it = csvMapper.readerForMapOf(String.class)
                .with(schema)
                .readValues(file);

        for (Map station : it.readAll()) {
            String gsId = station.get("gsId").toString();
            FuelStation fuelStation = fuelStationRepository.findById(gsId).orElse(null);
            fuelStation.setZone(station.get("zone").toString());
            fuelStation.setArea(station.get("area").toString());
            fuelStationRepository.save(fuelStation);


        }
        return "success";
    }

    @GetMapping("/lat-lon-region")
    public String addLanLonPerRegion() {

        List<FuelStation> fuelStations = fuelStationRepository.findAll();

        for (FuelStation station : fuelStations) {

            if (station.getLatitude() != null && station.getLongitude() != null) {
                continue;
            }

            List<Map<String, String>> responses = callApiForSequencedNames(station.getName());

//            if (!responses.isEmpty()) {
//                for (Map<String, String> response  : responses) {
//                    if (response.get("display_name").contains(station.getCity()) || response.get("display_name").contains(station.getProvince())) {
//                        station.setLatitude(response.get("lat"));
//                        station.setLongitude(response.get("lon"));
//                        fuelStationRepository.save(station);
//                    }
//                }
//            }



        }

        return "success";
    }

    private List<Map<String, String>> callApiForSequencedNames(String name) {
        String searchParam;
        if (name.endsWith("0") || name.endsWith("1") || name.endsWith("2") || name.endsWith("3") || name.endsWith("4") || name.endsWith("5")
                || name.endsWith("6") || name.endsWith("7") || name.endsWith("8") || name.endsWith("9")) {
            for (char ch : name.toCharArray()) {
                if (ch == '0' || ch == '1' || ch == '2' || ch == '3' || ch == '4' ||
                        ch == '5' || ch == '6' || ch == '7' || ch == '8' || ch == '9') {
                    name = name.replaceAll(String.valueOf(ch), "");
                }
            }
//            return callApi(name);
            System.out.println(name);
        }

        return Collections.emptyList();
    }

}
