package ir.niopdc.gslatlon.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

import java.math.BigDecimal;

@Entity
@Table(name = "fuel_station")
@Data
public class FuelStation {

    private String name;
    @Id
    private Integer gsId;
    private String latitude;
    private String longitude;
    private String province;
    private String city;
    private String zone;
    private String area;

}
