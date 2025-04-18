package showcase.streaming.event.account.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Account {
    private String id;
    private String name;
    private String accountType;
    private String status;
    private String notes;
    private Location location;
}
