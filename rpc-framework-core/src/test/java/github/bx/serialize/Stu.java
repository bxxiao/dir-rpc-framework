package github.bx.serialize;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Stu {
    private String name;
    private int age;
    private String cardId;
}
