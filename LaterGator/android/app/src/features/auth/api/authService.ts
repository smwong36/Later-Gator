import auth from '@react-native-firebase/auth';
import { GoogleSignin } from '@react-native-google-signin/google-signin';

// Initialize once at app startup
export const configureGoogleSignIn = () => {
  GoogleSignin.configure({
    webClientId: '958331286313-nvkgljsm8nuf6vulq2mtlrk1jjeoou8n.apps.googleusercontent.com', // from google-services-json
  });
};

export const signInWithGoogle = async () => {
  try {
    
    await GoogleSignin.signIn();
    //const { idToken } = await GoogleSignin.signIn();
    const { idToken } = await GoogleSignin.getTokens();
    const googleCredential = auth.GoogleAuthProvider.credential(idToken);
    return auth().signInWithCredential(googleCredential);
  } catch (error) {
    console.error('Google Sign-In Error:', error);
    throw error;
  }
};

export const signOut = async () => {
  try {
    await GoogleSignin.signOut();
    await auth().signOut();
  } catch (error) {
    console.error('Sign-Out Error:', error);
  }
};
