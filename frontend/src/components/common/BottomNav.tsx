import { NavLink } from 'react-router-dom';
import './BottomNav.css';

export function BottomNav() {
  return (
    <nav className="bottom-nav">
      <NavLink
        to="/discover"
        className={({ isActive }) =>
          `nav-item ${isActive ? 'active' : ''}`
        }
      >
        <span className="nav-icon">🔍</span>
        <span className="nav-label">발견</span>
      </NavLink>
      <NavLink
        to="/matches"
        className={({ isActive }) =>
          `nav-item ${isActive ? 'active' : ''}`
        }
      >
        <span className="nav-icon">💝</span>
        <span className="nav-label">매칭</span>
      </NavLink>
      <NavLink
        to="/rooms"
        className={({ isActive }) =>
          `nav-item ${isActive ? 'active' : ''}`
        }
      >
        <span className="nav-icon">💬</span>
        <span className="nav-label">채팅</span>
      </NavLink>
      <NavLink
        to="/profile"
        className={({ isActive }) =>
          `nav-item ${isActive ? 'active' : ''}`
        }
      >
        <span className="nav-icon">👤</span>
        <span className="nav-label">프로필</span>
      </NavLink>
    </nav>
  );
}
