// top/yyf/psych_support/model/LoginResponse.java
package top.yyf.psych_support.model;

import lombok.Data;

@Data
public class LoginResponse {
    private String token;
    private UserResponse user;
}