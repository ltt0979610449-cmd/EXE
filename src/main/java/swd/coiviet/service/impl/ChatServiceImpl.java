package swd.coiviet.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import swd.coiviet.dto.response.ChatConversationResponse;
import swd.coiviet.dto.response.ChatMessageResponse;
import swd.coiviet.enums.Role;
import swd.coiviet.exception.AppException;
import swd.coiviet.exception.ErrorCode;
import swd.coiviet.model.ChatConversation;
import swd.coiviet.model.ChatMessage;
import swd.coiviet.model.User;
import swd.coiviet.repository.ChatConversationRepository;
import swd.coiviet.repository.ChatMessageRepository;
import swd.coiviet.repository.UserRepository;
import swd.coiviet.service.ChatService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ChatServiceImpl implements ChatService {
    private final ChatConversationRepository conversationRepository;
    private final ChatMessageRepository messageRepository;
    private final UserRepository userRepository;

    public ChatServiceImpl(ChatConversationRepository conversationRepository,
                           ChatMessageRepository messageRepository,
                           UserRepository userRepository) {
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public ChatMessageResponse sendMessage(Long senderId, Long recipientId, String content) {
        if (senderId == null || recipientId == null) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Sender/Recipient không hợp lệ");
        }
        if (content == null || content.isBlank()) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Nội dung tin nhắn không được để trống");
        }

        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND, "Người gửi không tồn tại"));
        User recipient = userRepository.findById(recipientId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND, "Người nhận không tồn tại"));

        ChatConversation conversation = resolveConversation(sender, recipient);

        ChatMessage message = ChatMessage.builder()
                .conversation(conversation)
                .sender(sender)
                .recipient(recipient)
                .content(content.trim())
                .build();
        ChatMessage saved = messageRepository.save(message);

        conversation.setLastMessage(saved.getContent());
        conversation.setLastMessageAt(saved.getCreatedAt() != null ? saved.getCreatedAt() : LocalDateTime.now());
        conversationRepository.save(conversation);

        return toMessageResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChatConversationResponse> getConversations(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND, "Người dùng không tồn tại"));

        return conversationRepository.findByCustomerIdOrArtisanIdOrderByUpdatedAtDesc(user.getId(), user.getId())
                .stream()
                .map(conversation -> toConversationResponse(conversation, user))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChatMessageResponse> getMessages(String username, Long conversationId) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND, "Người dùng không tồn tại"));
        ChatConversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Conversation không tồn tại"));

        if (!isParticipant(conversation, user)) {
            throw new AppException(ErrorCode.FORBIDDEN, "Bạn không có quyền truy cập hội thoại này");
        }

        return messageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId)
                .stream()
                .map(this::toMessageResponse)
                .collect(Collectors.toList());
    }

    private ChatConversation resolveConversation(User sender, User recipient) {
        User customer;
        User artisan;

        if (sender.getRole() == Role.CUSTOMER && isStaffRole(recipient.getRole())) {
            customer = sender;
            artisan = recipient;
        } else if (recipient.getRole() == Role.CUSTOMER && isStaffRole(sender.getRole())) {
            customer = recipient;
            artisan = sender;
        } else {
            throw new AppException(ErrorCode.FORBIDDEN, "Chỉ khách hàng và nghệ nhân/staff mới được chat");
        }

        Optional<ChatConversation> existing = conversationRepository.findByCustomerIdAndArtisanId(
                customer.getId(), artisan.getId());
        return existing.orElseGet(() -> conversationRepository.save(ChatConversation.builder()
                .customer(customer)
                .artisan(artisan)
                .build()));
    }

    private boolean isStaffRole(Role role) {
        return role == Role.ARTISAN || role == Role.ADMIN;
    }

    private boolean isParticipant(ChatConversation conversation, User user) {
        return conversation.getCustomer().getId().equals(user.getId())
                || conversation.getArtisan().getId().equals(user.getId());
    }

    private ChatMessageResponse toMessageResponse(ChatMessage message) {
        return ChatMessageResponse.builder()
                .id(message.getId())
                .conversationId(message.getConversation().getId())
                .senderId(message.getSender().getId())
                .recipientId(message.getRecipient().getId())
                .content(message.getContent())
                .createdAt(message.getCreatedAt())
                .build();
    }

    private ChatConversationResponse toConversationResponse(ChatConversation conversation, User currentUser) {
        User partner = conversation.getCustomer().getId().equals(currentUser.getId())
                ? conversation.getArtisan()
                : conversation.getCustomer();

        return ChatConversationResponse.builder()
                .id(conversation.getId())
                .partnerId(partner.getId())
                .partnerName(partner.getFullName() != null ? partner.getFullName() : partner.getUsername())
                .partnerAvatarUrl(partner.getAvatarUrl())
                .lastMessage(conversation.getLastMessage())
                .lastMessageAt(conversation.getLastMessageAt())
                .updatedAt(conversation.getUpdatedAt())
                .build();
    }
}
