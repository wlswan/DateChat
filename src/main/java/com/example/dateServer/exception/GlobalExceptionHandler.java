package com.example.dateServer.exception;

import com.example.dateServer.auth.exception.DuplicateEmailException;
import com.example.dateServer.auth.exception.InvalidPasswordException;
import com.example.dateServer.auth.exception.UserNotFoundException;
import com.example.dateServer.chat.exception.ChatRoomAccessDeniedException;
import com.example.dateServer.chat.exception.ChatRoomClosedException;
import com.example.dateServer.chat.exception.ChatRoomNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateEmail(DuplicateEmailException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse("DUPLICATE_EMAIL", e.getMessage()));
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFound(UserNotFoundException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponse("USER_NOT_FOUND", e.getMessage()));
    }

    @ExceptionHandler(InvalidPasswordException.class)
    public ResponseEntity<ErrorResponse> handleInvalidPassword(InvalidPasswordException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponse("INVALID_PASSWORD", e.getMessage()));
    }

    @ExceptionHandler(ChatRoomNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleChatRoomNotFound(ChatRoomNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse("CHAT_ROOM_NOT_FOUND", e.getMessage()));
    }

    @ExceptionHandler(ChatRoomAccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleChatRoomAccessDenied(ChatRoomAccessDeniedException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ErrorResponse("CHAT_ROOM_ACCESS_DENIED", e.getMessage()));
    }

    @ExceptionHandler(ChatRoomClosedException.class)
    public ResponseEntity<ErrorResponse> handleChatRoomClosed(ChatRoomClosedException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse("CHAT_ROOM_CLOSED", e.getMessage()));
    }
}
