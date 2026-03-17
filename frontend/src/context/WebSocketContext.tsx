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
import type { ChatMessage, SendMessageRequest, ChatReadRequest } from '../types/chat.types';

interface WebSocketContextType {
  isConnected: boolean;
  subscribe: (roomId: number, callback: (message: ChatMessage) => void) => void;
  unsubscribe: (roomId: number) => void;
  sendMessage: (request: SendMessageRequest) => void;
  markAsRead: (request: ChatReadRequest) => void;
}

const WebSocketContext = createContext<WebSocketContextType | null>(null);

const SOCKET_URL = 'http://localhost:8080/ws/chat';

interface WebSocketProviderProps {
  children: ReactNode;
}

export function WebSocketProvider({ children }: WebSocketProviderProps) {
  const [isConnected, setIsConnected] = useState(false);
  const clientRef = useRef<Client | null>(null);
  const subscriptionsRef = useRef<Map<number, { unsubscribe: () => void }>>(
    new Map()
  );
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
    };

    client.onDisconnect = () => {
      console.log('WebSocket disconnected');
      setIsConnected(false);
    };

    client.onStompError = (frame) => {
      console.error('STOMP error:', frame.headers['message']);
    };

    client.activate();
    clientRef.current = client;

    return () => {
      subscriptionsRef.current.forEach((sub) => sub.unsubscribe());
      subscriptionsRef.current.clear();
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
    sendMessage,
    markAsRead,
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
