package dgdr.server.vonage.user.domain.dto;

import dgdr.server.vonage.user.domain.User;

public record UserSignUpReq (
    String id,
    String name,
    String password,
    String phone
){
    public User toNonPermanenEntity() {
        return User.builder()
                .userId(id)
                .name(name)
                .password(password)
                .phone(phone)
                .build();
    }
}
