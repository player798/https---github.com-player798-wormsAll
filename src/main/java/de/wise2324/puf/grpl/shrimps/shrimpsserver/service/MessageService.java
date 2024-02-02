package de.wise2324.puf.grpl.shrimps.shrimpsserver.service;

import de.wise2324.puf.grpl.shrimps.shrimpsserver.dto.MessageDTO;
import de.wise2324.puf.grpl.shrimps.shrimpsserver.model.Game;
import de.wise2324.puf.grpl.shrimps.shrimpsserver.model.Message;
import de.wise2324.puf.grpl.shrimps.shrimpsserver.model.Player;
import de.wise2324.puf.grpl.shrimps.shrimpsserver.repository.MessageRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.ApplicationScope;

import java.util.Optional;

@Service
@Transactional
@ApplicationScope
public class MessageService {

    @Autowired
    MessageRepository messageRepository;

    @Autowired
    PlayerService playerService;

    @Autowired
    GameService gameService;

    public MessageDTO getDTOFromObject(Message message) {
        MessageDTO messageDTO = new MessageDTO();

        messageDTO.setReceiver(
                this.playerService.getDTOFromObject(
                        message.getReceiver()
                )
        );
        messageDTO.setSender(
                this.playerService.getDTOFromObject(
                        message.getSender()
                )
        );
        messageDTO.setGameInvite(
                this.gameService.getDTOFromObject(
                        message.getGameInvite()
                )
        );

        messageDTO.setId(message.getId());
        messageDTO.setTimestamp(message.getTimestamp());
        messageDTO.setText(message.getText());

        return messageDTO;
    }

    public MessageDTO sendInviteToPlayer(Long gameId, String senderMail, String receiverMail) {
        MessageDTO result;
        Message message = new Message();

        Player sender   = this.playerService.getPlayerByEmail(senderMail);
        Player receiver = this.playerService.getPlayerByEmail(receiverMail);

        Game game       = this.gameService.getGameFromId(gameId);

        message.setSender(sender);
        message.setReceiver(receiver);
        message.setGameInvite(game);

        result          = this.getDTOFromObject(message);

        return result;
    }

    public void deleteInvite(MessageDTO messageDTO) {
        Optional<Message> message = this.messageRepository.findById((messageDTO.getId()));
        message.ifPresent(value -> this.messageRepository.delete(value));
    }
}