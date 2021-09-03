package github.bx.enums;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public enum RpcResponseCodeEnum {
    SUCCESS(200, "远程调用成功"),
    FAIL(500, "远程调用出现异常");


    private Integer code;
    private String message;
}
