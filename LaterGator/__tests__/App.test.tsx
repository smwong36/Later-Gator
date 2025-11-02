/**
 * @format
 */

import React from 'react';
import ReactTestRenderer from 'react-test-renderer';
import { render } from '@testing-library/react-native';
import PomodoroScreen from '../android/app/src/features/pomodoro/PomodoroScreen';
import App from '../App';

test('renders correctly', async () => {
  await ReactTestRenderer.act(() => {
    ReactTestRenderer.create(<App />);
  });
});


test('Pomodoro screen shows heading and timer', () => {
  const { getByText } = render(<PomodoroScreen />);
  expect(getByText('Pomodoro')).toBeTruthy();
  expect(getByText(/25:00/)).toBeTruthy();
});

jest.mock('@react-native-google-signin/google-signin', () => {
  const GoogleSignin = {
    configure: jest.fn(),
    signIn: jest.fn(),
    getTokens: jest.fn(),
    signOut: jest.fn(),
  };
  return { __esModule: true, GoogleSignin };
});

jest.mock('@react-native-firebase/auth', () => {
  // create mocks for instance methods
  const signInWithCredential = jest.fn();
  const signOut = jest.fn();

  // function that returns the auth instance
  const authFn: any = jest.fn(() => ({
    signInWithCredential,
    signOut,
  }));

  // GoogleAuthProvider helper (static on the auth module)
  authFn.GoogleAuthProvider = {
    credential: jest.fn((idToken: string) => ({ token: idToken })),
  };

  return { __esModule: true, default: authFn };
});

import { GoogleSignin } from '@react-native-google-signin/google-signin';
import auth from '@react-native-firebase/auth'; // this is the mocked factory
import {
  configureGoogleSignIn,
  signInWithGoogle,
  signOut as signOutFromService,
} from '../android/app/src/features/auth/api/authService';

describe('authService', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('configureGoogleSignIn calls GoogleSignin.configure with client id', () => {
    configureGoogleSignIn();

    expect(GoogleSignin.configure).toHaveBeenCalledTimes(1);
    const cfgArg = (GoogleSignin.configure as jest.Mock).mock.calls[0][0];
    expect(cfgArg).toMatchObject({
      webClientId: expect.any(String),
    });
   
  });

  it('signInWithGoogle calls GoogleSignin.getTokens and signs in with credential', async () => {
    // Arrange: make getTokens return a specific idToken and make signInWithCredential resolve
    (GoogleSignin.getTokens as jest.Mock).mockResolvedValue({ idToken: 'test-id-token' });
    const fakeResult = { user: { uid: 'user-1' } };
    const mockedAuthInstance = (auth as unknown as jest.Mock).mock.results[0]?.value;
    // Because auth is a mock factory function, calls to auth() within the implementation will produce an object.
    // Instead of relying on previous mock result, we can set the mock implementation for auth function:
    const signInWithCredentialMock = jest.fn().mockResolvedValue(fakeResult);
    const signOutMock = jest.fn().mockResolvedValue(undefined);
    (auth as unknown as jest.Mock).mockImplementation(() => ({
      signInWithCredential: signInWithCredentialMock,
      signOut: signOutMock,
    }));

    // Act
    const result = await signInWithGoogle();

    // Assert: google tokens requested, credential built, and auth called
    expect(GoogleSignin.getTokens).toHaveBeenCalledTimes(1);
    expect((auth as any).GoogleAuthProvider.credential).toHaveBeenCalledWith('test-id-token');
    expect(signInWithCredentialMock).toHaveBeenCalledWith({ token: 'test-id-token' });
    expect(result).toBe(fakeResult);
  });

  it('signOut calls GoogleSignin.signOut and auth().signOut', async () => {
    // Arrange
    const signOutMock = jest.fn().mockResolvedValue(undefined);
    (auth as unknown as jest.Mock).mockImplementation(() => ({
      signOut: signOutMock,
      signInWithCredential: jest.fn(),
    }));

    (GoogleSignin.signOut as jest.Mock).mockResolvedValue(undefined);

    // Act
    await signOutFromService();

    // Assert
    expect(GoogleSignin.signOut).toHaveBeenCalledTimes(1);
    expect(signOutMock).toHaveBeenCalledTimes(1);
  });
});
