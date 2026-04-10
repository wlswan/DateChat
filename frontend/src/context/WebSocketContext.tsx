import {
  createContext,
  useContext,
  useEffect,
  useRef,
  useState,
  useCallback,
  type ReactNode,
} from 'react';
import { Client, type IMessage } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { storage } from '../utils/storage';
import { useAuth } from '../hooks/useAuth';
import type { ChatMessage, ChatEvent, SendMessageRequest, ChatReadRequest } from '../types/chat.types';

interface WebSocketContextType {
  isConnected: boolean;
  subscribe: (roomId: number, callback: (message: ChatMessage) => void) => void;
  unsubscribe: (roomId: number) => void;
  subscribeToEvents: (roomId: number, callback: (event: ChatEvent) => void) => void;
  unsubscribeFromEvents: (roomId: number) => void;
  sendMessage: (request: SendMessageRequest) => void;
  markAsRead: (request: ChatReadRequest) => void;
  subscribeToErrors: (callback: (message: string) => void) => void;
}

const WebSocketContext = createContext<WebSocketContextType | null>(null);

const SOCKET_URL = 'http://localhost:8080/ws/chat';

interface WebSocketProviderProps {
  children: ReactNode;
}

export function WebSocketProvider({ children }: WebSocketProviderProps) {
  const [isConnected, setIsConnected] = useState(false);
  const clientRef = useRef<Client | null>(null);
  const subscriptionsRef = useRef<Map<number, { unsubscribe: () => void }>>(new Map());
  const eventSubscriptionsRef = useRef<Map<number, { unsubscribe: () => void }>>(new Map());
  const errorCallbackRef = useRef<((message: string) => void) | null>(null);
  const errorSubscriptionRef = useRef<{ unsubscribe: () => void } | null>(null);
  const { isAuthenticated } = useAuth();

  useEffect(() => {
    const token = storage.getAccessToken();
    if (!token || !isAuthenticated) return;

    const client = new Client({
      webSocketFactory: () => new SockJS(SOCKET_URL),
      connectHeaders: {
        Authorization: `Bearer ${token}`,
      },
      debug: (str) => {
        console.log('[STOMP]', str);
      },
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
    });

    client.onConnect = () => {
      console.log('WebSocket connected');
      setIsConnected(true);

      const errorSub = client.subscribe('/user/queue/errors', (message: IMessage) => {
        try {
          const body = JSON.parse(message.body);
          errorCallbackRef.current?.(body.message);
        } catch {
          errorCallbackRef.current?.(message.body);
        }
      });
      errorSubscriptionRef.current = errorSub;
    };

    client.onDisconnect = () => {
      console.log('WebSocket disconnected');
      setIsConnected(false);
    };

    client.onStompError = (frame) => {
      const message = frame.headers['message'];
      console.error('STOMP error:', message);
      if (message?.includes('종료된 채팅방')) {
        errorCallbackRef.current?.('종료된 채팅방입니다.');
      }
    };

    client.activate();
    clientRef.current = client;

    return () => {
      subscriptionsRef.current.forEach((sub) => sub.unsubscribe());
      subscriptionsRef.current.clear();
      eventSubscriptionsRef.current.forEach((sub) => sub.unsubscribe());
      eventSubscriptionsRef.current.clear();
      errorSubscriptionRef.current?.unsubscribe();
      errorSubscriptionRef.current = null;
      client.deactivate();
    };
  }, [isAuthenticated]);

  const subscribe = useCallback(
    (roomId: number, callback: (message: ChatMessage) => void) => {
      const client = clientRef.current;
      if (!client || !client.connected) {
        console.warn('WebSocket not connected, cannot subscribe');
        return;
      }

      // Unsubscribe from existing subscription if any
      const existingSub = subscriptionsRef.current.get(roomId);
      if (existingSub) {
        existingSub.unsubscribe();
      }

      const subscription = client.subscribe(
        `/topic/chat/${roomId}`,
        (message: IMessage) => {
          try {
            const chatMessage: ChatMessage = JSON.parse(message.body);
            callback(chatMessage);
          } catch (error) {
            console.error('Failed to parse message:', error);
          }
        }
      );

      subscriptionsRef.current.set(roomId, subscription);
    },
    []
  );

  const subscribeToEvents = useCallback(
    (roomId: number, callback: (event: ChatEvent) => void) => {
      const client = clientRef.current;
      if (!client || !client.connected) return;

      const existingSub = eventSubscriptionsRef.current.get(roomId);
      if (existingSub) existingSub.unsubscribe();

      const subscription = client.subscribe(
        `/topic/chat/${roomId}/events`,
        (message: IMessage) => {
          try {
            const event: ChatEvent = JSON.parse(message.body);
            callback(event);
          } catch (error) {
            console.error('Failed to parse event:', error);
          }
        }
      );
      eventSubscriptionsRef.current.set(roomId, subscription);
    },
    []
  );

  const unsubscribeFromEvents = useCallback((roomId: number) => {
    const subscription = eventSubscriptionsRef.current.get(roomId);
    if (subscription) {
      subscription.unsubscribe();
      eventSubscriptionsRef.current.delete(roomId);
    }
  }, []);

  const unsubscribe = useCallback((roomId: number) => {
    const subscription = subscriptionsRef.current.get(roomId);
    if (subscription) {
      subscription.unsubscribe();
      subscriptionsRef.current.delete(roomId);
    }
  }, []);

  const sendMessage = useCallback((request: SendMessageRequest) => {
    const client = clientRef.current;
    if (!client || !client.connected) {
      console.warn('WebSocket not connected, cannot send message');
      return;
    }

    client.publish({
      destination: '/app/chat.send',
      body: JSON.stringify(request),
    });
  }, []);

  const subscribeToErrors = useCallback((callback: (message: string) => void) => {
    errorCallbackRef.current = callback;
  }, []);

  const markAsRead = useCallback((request: ChatReadRequest) => {
    const client = clientRef.current;
    if (!client || !client.connected) {
      console.warn('WebSocket not connected, cannot mark as read');
      return;
    }

    client.publish({
      destination: '/app/chat.read',
      body: JSON.stringify(request),
    });
  }, []);

  const value: WebSocketContextType = {
    isConnected,
    subscribe,
    unsubscribe,
    subscribeToEvents,
    unsubscribeFromEvents,
    sendMessage,
    markAsRead,
    subscribeToErrors,
  };

  return (
    <WebSocketContext.Provider value={value}>
      {children}
    </WebSocketContext.Provider>
  );
}

export function useWebSocket() {
  const context = useContext(WebSocketContext);
  if (!context) {
    throw new Error('useWebSocket must be used within a WebSocketProvider');
  }
  return context;
}
