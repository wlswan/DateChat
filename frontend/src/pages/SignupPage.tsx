import { useState, type FormEvent } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';
import { authApi } from '../api/auth.api';
import type { AppLang, Gender } from '../types/auth.types';
import './AuthPages.css';

type Step = 1 | 2 | 3 | 4;

export function SignupPage() {
  const [step, setStep] = useState<Step>(1);

  // Step 1: Account
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');

  // Step 2: Profile
  const [nickname, setNickname] = useState('');
  const [lang, setLang] = useState<AppLang>('KR');
  const [gender, setGender] = useState<Gender>('MALE');
  const [birthDate, setBirthDate] = useState('');

  // Step 3: Bio
  const [bio, setBio] = useState('');
  const [profileImageUrl, setProfileImageUrl] = useState('');

  // Step 4: Preference
  const [minAge, setMinAge] = useState(18);
  const [maxAge, setMaxAge] = useState(40);
  const [minHeight, setMinHeight] = useState(140);
  const [maxHeight, setMaxHeight] = useState(200);

  const [error, setError] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);

  const { signUp, login } = useAuth();
  const navigate = useNavigate();

  const handleNext = (e: FormEvent) => {
    e.preventDefault();
    setError('');

    if (step === 1) {
      if (password !== confirmPassword) {
        setError('비밀번호가 일치하지 않습니다.');
        return;
      }
      if (password.length < 8) {
        setError('비밀번호는 8자 이상이어야 합니다.');
        return;
      }
      setStep(2);
    } else if (step === 2) {
      if (!birthDate) {
        setError('생년월일을 입력해주세요.');
        return;
      }
      setStep(3);
    } else if (step === 3) {
      setStep(4);
    }
  };

  const handleBack = () => {
    if (step > 1) {
      setStep((prev) => (prev - 1) as Step);
    }
  };

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setError('');
    setIsSubmitting(true);

    try {
      // 1. Sign up
      await signUp({ email, password, nickname, lang });

      // 2. Login
      await login({ email, password });

      // 3. Update profile
      await authApi.updateProfile({
        gender,
        birthDate,
        bio: bio || undefined,
        profileImageUrl: profileImageUrl || undefined,
      });

      // 4. Update preference
      await authApi.updatePreference({
        minAge,
        maxAge,
        minHeight,
        maxHeight,
      });

      navigate('/discover');
    } catch (err) {
      setError('회원가입에 실패했습니다. 다시 시도해주세요.');
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="auth-container">
      <div className="auth-card">
        <div className="step-indicator">
          <span className={step >= 1 ? 'active' : ''}>1</span>
          <span className={step >= 2 ? 'active' : ''}>2</span>
          <span className={step >= 3 ? 'active' : ''}>3</span>
          <span className={step >= 4 ? 'active' : ''}>4</span>
        </div>

        <h1 className="auth-title">
          {step === 1 && '계정 만들기'}
          {step === 2 && '프로필 설정'}
          {step === 3 && '자기소개'}
          {step === 4 && '선호도 설정'}
        </h1>

        {error && <div className="auth-error">{error}</div>}

        {/* Step 1: Account */}
        {step === 1 && (
          <form onSubmit={handleNext} className="auth-form">
            <div className="form-group">
              <label htmlFor="email">이메일</label>
              <input
                type="email"
                id="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                placeholder="example@email.com"
                required
              />
            </div>

            <div className="form-group">
              <label htmlFor="password">비밀번호</label>
              <input
                type="password"
                id="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                placeholder="8자 이상 입력하세요"
                required
              />
            </div>

            <div className="form-group">
              <label htmlFor="confirmPassword">비밀번호 확인</label>
              <input
                type="password"
                id="confirmPassword"
                value={confirmPassword}
                onChange={(e) => setConfirmPassword(e.target.value)}
                placeholder="비밀번호를 다시 입력하세요"
                required
              />
            </div>

            <button type="submit" className="auth-button">
              다음
            </button>
          </form>
        )}

        {/* Step 2: Profile */}
        {step === 2 && (
          <form onSubmit={handleNext} className="auth-form">
            <div className="form-group">
              <label htmlFor="nickname">닉네임</label>
              <input
                type="text"
                id="nickname"
                value={nickname}
                onChange={(e) => setNickname(e.target.value)}
                placeholder="닉네임을 입력하세요"
                required
              />
            </div>

            <div className="form-group">
              <label>성별</label>
              <div className="lang-selector">
                <button
                  type="button"
                  className={`lang-button ${gender === 'MALE' ? 'active' : ''}`}
                  onClick={() => setGender('MALE')}
                >
                  남성
                </button>
                <button
                  type="button"
                  className={`lang-button ${gender === 'FEMALE' ? 'active' : ''}`}
                  onClick={() => setGender('FEMALE')}
                >
                  여성
                </button>
              </div>
            </div>

            <div className="form-group">
              <label htmlFor="birthDate">생년월일</label>
              <input
                type="date"
                id="birthDate"
                value={birthDate}
                onChange={(e) => setBirthDate(e.target.value)}
                required
              />
            </div>

            <div className="form-group">
              <label>언어 선택</label>
              <div className="lang-selector">
                <button
                  type="button"
                  className={`lang-button ${lang === 'KR' ? 'active' : ''}`}
                  onClick={() => setLang('KR')}
                >
                  한국어
                </button>
                <button
                  type="button"
                  className={`lang-button ${lang === 'JP' ? 'active' : ''}`}
                  onClick={() => setLang('JP')}
                >
                  日本語
                </button>
              </div>
            </div>

            <div className="button-group">
              <button type="button" className="auth-button secondary" onClick={handleBack}>
                이전
              </button>
              <button type="submit" className="auth-button">
                다음
              </button>
            </div>
          </form>
        )}

        {/* Step 3: Bio */}
        {step === 3 && (
          <form onSubmit={handleNext} className="auth-form">
            <div className="form-group">
              <label htmlFor="bio">자기소개 (선택)</label>
              <textarea
                id="bio"
                value={bio}
                onChange={(e) => setBio(e.target.value)}
                placeholder="자신을 소개해주세요"
                rows={4}
              />
            </div>

            <div className="form-group">
              <label htmlFor="profileImageUrl">프로필 이미지 URL (선택)</label>
              <input
                type="url"
                id="profileImageUrl"
                value={profileImageUrl}
                onChange={(e) => setProfileImageUrl(e.target.value)}
                placeholder="https://example.com/image.jpg"
              />
            </div>

            <div className="button-group">
              <button type="button" className="auth-button secondary" onClick={handleBack}>
                이전
              </button>
              <button type="submit" className="auth-button">
                다음
              </button>
            </div>
          </form>
        )}

        {/* Step 4: Preference */}
        {step === 4 && (
          <form onSubmit={handleSubmit} className="auth-form">
            <div className="form-group">
              <label>선호 나이</label>
              <div className="range-inputs">
                <input
                  type="number"
                  value={minAge}
                  onChange={(e) => setMinAge(Number(e.target.value))}
                  min={18}
                  max={100}
                  placeholder="최소"
                />
                <span>~</span>
                <input
                  type="number"
                  value={maxAge}
                  onChange={(e) => setMaxAge(Number(e.target.value))}
                  min={18}
                  max={100}
                  placeholder="최대"
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
                  min={100}
                  max={250}
                  placeholder="최소"
                />
                <span>~</span>
                <input
                  type="number"
                  value={maxHeight}
                  onChange={(e) => setMaxHeight(Number(e.target.value))}
                  min={100}
                  max={250}
                  placeholder="최대"
                />
                <span>cm</span>
              </div>
            </div>

            <div className="button-group">
              <button type="button" className="auth-button secondary" onClick={handleBack}>
                이전
              </button>
              <button type="submit" className="auth-button" disabled={isSubmitting}>
                {isSubmitting ? '가입 중...' : '완료'}
              </button>
            </div>
          </form>
        )}

        <p className="auth-link">
          이미 계정이 있으신가요? <Link to="/login">로그인</Link>
        </p>
      </div>
    </div>
  );
}
