package com.we.hirehub.dto.user;

import com.we.hirehub.entity.Role;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsersDto {

    private Long id;
    private String name;
    private String nickname;
    private String phone;
    private String dob;
    private String gender;
    private String email;
    private String education;
    private String careerLevel;

    private String position;

    private String address;

    private String location;

    private Role role;
}
