package de.wise2324.puf.grpl.shrimps.shrimpsserver.websocket;

import de.wise2324.puf.grpl.shrimps.shrimpsserver.entity.OnlineStatus;
import de.wise2324.puf.grpl.shrimps.shrimpsserver.model.Message;
import de.wise2324.puf.grpl.shrimps.shrimpsserver.model.Player;
import de.wise2324.puf.grpl.shrimps.shrimpsserver.repository.PlayerRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.sql.Timestamp;
import java.util.Objects;

@Component
public class ChatWebSocketEventListener {

    @Autowired
    private SimpMessageSendingOperations messagingTemplate;

    @Autowired
    private PlayerRepository playerRepository;

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
    /*
        String email = Objects.requireNonNull(event.getUser()).getName();
        Player player = playerRepository.findByEmail(email);

        if (player != null) {

            Message message = new Message();
            message.setTimestamp(new Timestamp(System.currentTimeMillis()));
            message.setSender(player);
            message.setText(player.getUsername() + " hat den Chat betreten");

            // Just send no need to save
            messagingTemplate.convertAndSend("/chat/messages", message);

            player.setStatus(OnlineStatus.Online);
            playerRepository.save(player);
        }
        */
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        String email = Objects.requireNonNull(event.getUser()).getName();
        Player player = playerRepository.findByEmail(email);

        if (player != null) {
            if (player.getStatus() == OnlineStatus.Online)
            {
                // Wenn Spieler in der Lobby war (OnlineStatus.Online)
                // dann im Chat eine Nachricht hinterlassen
                Message message = new Message();
                message.setTimestamp(new Timestamp(System.currentTimeMillis()));
                message.setSender(player);
                message.setText(player.getUsername() + " hat den Chat verlassen");

                // Just send no need to save
                messagingTemplate.convertAndSend("/chat/messages", message);
            }

            // Update Online-Status
            player.setStatus(OnlineStatus.Offline);
            playerRepository.save(player);
        }
    }
}
