import { useState, useEffect, type FormEvent } from 'react';
import { useAuth } from '../hooks/useAuth';
import { authApi } from '../api/auth.api';
import './ProfilePage.css';

export function ProfilePage() {
  const { user, logout } = useAuth();

  const [bio, setBio] = useState('');
  const [profileImageUrl, setProfileImageUrl] = useState('');
  const [minAge, setMinAge] = useState(18);
  const [maxAge, setMaxAge] = useState(40);
  const [minHeight, setMinHeight] = useState(140);
  const [maxHeight, setMaxHeight] = useState(200);

  const [isSaving, setIsSaving] = useState(false);
  const [success, setSuccess] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    if (user) {
      setBio(user.bio ?? '');
      setProfileImageUrl(user.profileImageUrl ?? '');
      setMinAge(user.minAge ?? 18);
      setMaxAge(user.maxAge ?? 40);
      setMinHeight(user.minHeight ?? 140);
      setMaxHeight(user.maxHeight ?? 200);
    }
  }, [user]);

  const handleSave = async (e: FormEvent) => {
    e.preventDefault();
    setError('');
    setSuccess(false);
    setIsSaving(true);
    try {
      await authApi.updateMe({
        bio: bio || undefined,
        profileImageUrl: profileImageUrl || undefined,
        minAge,
        maxAge,
        minHeight,
        maxHeight,
      });
      setSuccess(true);
      setTimeout(() => setSuccess(false), 3000);
    } catch {
      setError('저장에 실패했습니다. 다시 시도해주세요.');
    } finally {
      setIsSaving(false);
    }
  };

  if (!user) return null;

  const genderLabel = user.gender === 'MALE' ? '남성' : '여성';
  const langLabel = user.appLang === 'KR' ? '한국어' : '日本語';

  return (
    <div className="profile-page">
      <div className="profile-header">
        {user.profileImageUrl ? (
          <img src={user.profileImageUrl} alt="프로필" className="profile-avatar" />
        ) : (
          <div className="profile-avatar-placeholder">👤</div>
        )}
        <div className="profile-header-name">{user.nickname}</div>
        <div className="profile-header-email">{user.email}</div>
      </div>

      <div className="profile-content">
        {/* 변경 불가 정보 */}
        <div className="profile-section">
          <div className="profile-section-title">기본 정보</div>
          <div className="profile-info-row">
            <span className="profile-info-label">닉네임</span>
            <span className="profile-info-value">{user.nickname}</span>
          </div>
          <div className="profile-info-row">
            <span className="profile-info-label">성별</span>
            <span className="profile-info-value">{genderLabel}</span>
          </div>
          <div className="profile-info-row">
            <span className="profile-info-label">생년월일</span>
            <span className="profile-info-value">{user.birthDate}</span>
          </div>
          <div className="profile-info-row">
            <span className="profile-info-label">언어</span>
            <span className="profile-info-value">{langLabel}</span>
          </div>
        </div>

        {/* 수정 가능 정보 */}
        <div className="profile-section">
          <div className="profile-section-title">프로필 수정</div>
          <form className="profile-form" onSubmit={handleSave}>
            <div className="form-group">
              <label>자기소개</label>
              <textarea
                value={bio}
                onChange={(e) => setBio(e.target.value)}
                placeholder="자신을 소개해주세요"
              />
            </div>

            <div className="form-group">
              <label>프로필 이미지 URL</label>
              <input
                type="url"
                value={profileImageUrl}
                onChange={(e) => setProfileImageUrl(e.target.value)}
                placeholder="https://example.com/image.jpg"
              />
            </div>

            <div className="form-group">
              <label>선호 나이</label>
              <div className="range-inputs">
                <input
                  type="number"
                  value={minAge}
                  onChange={(e) => setMinAge(Number(e.target.value))}
                  min={18} max={100}
                />
                <span>~</span>
                <input
                  type="number"
                  value={maxAge}
                  onChange={(e) => setMaxAge(Number(e.target.value))}
                  min={18} max={100}
                />
                <span>세</span>
              </div>
            </div>

            <div className="form-group">
              <label>선호 키</label>
              <div className="range-inputs">
                <input
                  type="number"
                  value={minHeight}
                  onChange={(e) => setMinHeight(Number(e.target.value))}
                  min={100} max={250}
                />
                <span>~</span>
                <input
                  type="number"
                  value={maxHeight}
                  onChange={(e) => setMaxHeight(Number(e.target.value))}
                  min={100} max={250}
                />
                <span>cm</span>
              </div>
            </div>

            {success && <div className="profile-success">저장되었습니다.</div>}
            {error && <div className="profile-error">{error}</div>}

            <button type="submit" className="profile-save-btn" disabled={isSaving}>
              {isSaving ? '저장 중...' : '저장'}
            </button>
          </form>
        </div>

        {/* 로그아웃 */}
        <div className="profile-section">
          <button className="profile-logout-btn" onClick={logout}>
            로그아웃
          </button>
        </div>
      </div>
    </div>
  );
}
