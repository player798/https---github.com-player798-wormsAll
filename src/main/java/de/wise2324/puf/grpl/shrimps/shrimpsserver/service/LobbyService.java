package de.wise2324.puf.grpl.shrimps.shrimpsserver.service;

import de.wise2324.puf.grpl.shrimps.shrimpsserver.model.Message;
import de.wise2324.puf.grpl.shrimps.shrimpsserver.repository.MessageRepository;
import de.wise2324.puf.grpl.shrimps.shrimpsserver.repository.PlayerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LobbyService {

    @Autowired
    PlayerRepository playerRepository;

    @Autowired
    MessageRepository messageRepository;

    public List<Message> getMessagesFor(String email) {
        List<Message> result = null;
        result = messageRepository.findByReceiverPlayerId(
                            playerRepository.findByEmail(email).getId());

        return result;
    }

    public List<Message> getLobbyMessages() {
        return messageRepository.findAllForLobby();
    }

    public List<Message> getLobbyMessagesLast20() {
        return messageRepository.findLobbyChat20();
    }

    public String sendMessage(Message message) {
        String result = "";

        if (message != null && message.getSender() != null) {

            // Fehler in der Id-Erzeugung erfordert Umschichtung
            Message newMessage = new Message();
            message.setId(newMessage.getId());

            messageRepository.save(message);

            if (message.getReceiver() == null) {
                if (message.getGameInvite() == null) {
                    result = "lobby";
                } else {
                    result = "game";
                }
            } else {
                if (message.getGameInvite() == null) {
                    result = "message";
                } else {
                    result = "invite";
                }
            }
        } else {
            result = "error";
        }

        return result;
    }
}
