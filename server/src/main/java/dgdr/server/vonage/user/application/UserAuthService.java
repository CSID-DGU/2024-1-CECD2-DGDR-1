package dgdr.server.vonage.user.application;

import dgdr.server.vonage.global.domain.TokenResponse;
import dgdr.server.vonage.global.utils.TokenProvider;
import dgdr.server.vonage.user.domain.User;
import dgdr.server.vonage.user.domain.dto.LoginReq;
import dgdr.server.vonage.user.domain.dto.UserSignUpReq;
import dgdr.server.vonage.user.infra.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserAuthService {
    private final UserRepository userRepository;
    private final TokenProvider tokenProvider;

    public void signUp(UserSignUpReq userSignUpReq) {
        log.info("[UserAuthService] userSignUpReq : {}", userSignUpReq);
        userRepository.save(userSignUpReq.toNonPermanenEntity());
    }

    public boolean checkId(String id) {
        return userRepository.existsById(id);
    }

    public TokenResponse login(LoginReq loginReq) {
        log.info("[UserAuthService] loginReq : {}", loginReq);

        User user = userRepository.findById(loginReq.id())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        return TokenResponse.builder()
                .accessToken(tokenProvider.createAccessToken(user.getUserId()))
                .refreshToken(tokenProvider.createRefreshToken(user.getUserId()))
                .build();
    }
}
