package dgdr.server.vonage.user.application;

import dgdr.server.vonage.user.domain.PrincipalDetails;
import dgdr.server.vonage.user.infra.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PrincipalDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    @Override
    public PrincipalDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.info("[PrincipalDetailsService] username : {}", username);
        return userRepository.findByUsername(username)
                .map(PrincipalDetails::new)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }
}
