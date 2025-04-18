package showcase.streaming.event.account.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Location {
    private String id;
    private String address;
    private String cityTown;
    private String stateProvince;
    private String zipPostalCode;
    private String countryCode;
}
