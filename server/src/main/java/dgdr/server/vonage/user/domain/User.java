package dgdr.server.vonage.user.domain;


import dgdr.server.vonage.record.domain.Record;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name="users")
public class User {
    @Id
    private String userId;

    private String name;

    private String password;

    private String phone;
    @OneToMany(mappedBy = "user")
    private List<Record> records;

}
