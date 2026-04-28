import { BrowserRouter, Routes, Route, Navigate, useLocation } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import { WebSocketProvider } from './context/WebSocketContext';
import { ProtectedRoute } from './components/common/ProtectedRoute';
import { BottomNav } from './components/common/BottomNav';
import { LoginPage } from './pages/LoginPage';
import { SignupPage } from './pages/SignupPage';
import { ChatRoomListPage } from './pages/ChatRoomListPage';
import { ChatRoomPage } from './pages/ChatRoomPage';
import { DiscoverPage } from './pages/DiscoverPage';
import { MatchListPage } from './pages/MatchListPage';
import { MatchDetailPage } from './pages/MatchDetailPage';
import { ProfilePage } from './pages/ProfilePage';
import { useAuth } from './hooks/useAuth';

function AppContent() {
  const location = useLocation();
  const { isAuthenticated } = useAuth();

  const showBottomNav = isAuthenticated &&
    !location.pathname.startsWith('/rooms/') &&
    !location.pathname.match(/^\/matches\/\d+$/) &&
    location.pathname !== '/login' &&
    location.pathname !== '/signup';

  return (
    <>
      <Routes>
        <Route path="/login" element={<LoginPage />} />
        <Route path="/signup" element={<SignupPage />} />
        <Route element={<ProtectedRoute />}>
          <Route path="/discover" element={<DiscoverPage />} />
          <Route path="/matches" element={<MatchListPage />} />
          <Route path="/matches/:matchId" element={<MatchDetailPage />} />
          <Route path="/rooms" element={<ChatRoomListPage />} />
          <Route path="/rooms/:roomId" element={<ChatRoomPage />} />
          <Route path="/profile" element={<ProfilePage />} />
        </Route>
        <Route path="*" element={<Navigate to="/discover" replace />} />
      </Routes>
      {showBottomNav && <BottomNav />}
    </>
  );
}

function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <WebSocketProvider>
          <AppContent />
        </WebSocketProvider>
      </AuthProvider>
    </BrowserRouter>
  );
}

export default App;
