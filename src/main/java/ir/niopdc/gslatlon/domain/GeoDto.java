package ir.niopdc.gslatlon.domain;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
public class GeoDto {

        private String count;
        private List<LocationDto> items;

}
