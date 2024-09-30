package dgdr.server.vonage;

import com.vonage.client.VonageClient;
import com.vonage.client.voice.ncco.ConnectAction;
import com.vonage.client.voice.ncco.EventType;
import com.vonage.client.voice.ncco.Ncco;
import com.vonage.client.voice.ncco.WebSocketEndpoint;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class VonageService {
    private final VonageClient vonageClient;

    public VonageService(@Value("${vonage.apiKey}") String apiKey,
                         @Value("${vonage.applicationId}") String applicationId,
                         @Value("${vonage.privateKeyPath}") String privateKeyPath,
                         @Value("${vonage.apiSecret}") String apiSecret) {
        this.vonageClient = VonageClient.builder()
                .applicationId(applicationId)
                .apiKey(apiKey)
                .apiSecret(apiSecret)
                .privateKeyPath(privateKeyPath)
                .build();
    }

    public Ncco createConversationNcco(String callerId) {
        String websocketUri = String.format("wss://" + Constants.URL + "/ws/audio?caller-id=%s", callerId);

        WebSocketEndpoint websocketEndpoint = WebSocketEndpoint
                .builder(websocketUri, "audio/l16;rate=16000")
                .build();

        ConnectAction connectAction = ConnectAction.builder()
                .endpoint(websocketEndpoint)
                .build();

        return new Ncco(connectAction);
    }
}
