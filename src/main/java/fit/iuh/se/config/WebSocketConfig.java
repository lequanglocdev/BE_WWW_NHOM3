package fit.iuh.se.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    // Dùng @Autowired field thay vì @RequiredArgsConstructor
    // vì CGLIB proxy yêu cầu @Configuration class có no-arg constructor
    @Autowired
    private WebSocketAuthChannelInterceptor authChannelInterceptor;

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                // Cho phép kết nối từ mọi nguồn (file HTML cục bộ, frontend khác)
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Prefix cho các @MessageMapping trong Controller
        registry.setApplicationDestinationPrefixes("/app");

        // /topic  → broadcast (1-nhiều)
        // /queue  → tin nhắn cá nhân (1-1)
        registry.enableSimpleBroker("/topic", "/queue");

        // Prefix cho tin nhắn cá nhân (convertAndSendToUser)
        registry.setUserDestinationPrefix("/user");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        // Gắn interceptor xác thực JWT vào inbound channel
        registration.interceptors(authChannelInterceptor);
    }
}
