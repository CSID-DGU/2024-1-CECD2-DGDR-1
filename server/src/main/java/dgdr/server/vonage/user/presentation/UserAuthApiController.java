package dgdr.server.vonage.user.presentation;

import dgdr.server.vonage.global.domain.TokenResponse;
import dgdr.server.vonage.user.application.UserAuthService;
import dgdr.server.vonage.user.domain.dto.LoginReq;
import dgdr.server.vonage.user.domain.dto.UserSignUpReq;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/user/auth")
public class UserAuthApiController {

    private final UserAuthService userAuthService;

    @PostMapping("/signup")
    public ResponseEntity<Void> signUp(@RequestBody UserSignUpReq userSignUpReq) {
        log.info("[UserAuthApiController] signUp userSignUpReq : {}", userSignUpReq);
        userAuthService.signUp(userSignUpReq);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/checkId")
    public ResponseEntity<Boolean> checkId(@RequestParam String id){
        log.info("[UserAuthApiController] checkId id : {}", id);
        return ResponseEntity.ok(userAuthService.checkId(id));
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@RequestBody LoginReq loginReq) {
        log.info("[UserAuthApiController] login loginReq : {}", loginReq);
        return ResponseEntity.ok(userAuthService.login(loginReq));
    }



}
