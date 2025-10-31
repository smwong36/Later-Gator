//This gives your components easy access to the current Firebase user.

import { useEffect, useState } from 'react';
import auth, { FirebaseAuthTypes } from '@react-native-firebase/auth';

export const useAuth = () => {
  const [user, setUser] = useState<FirebaseAuthTypes.User | null>(auth().currentUser);

  useEffect(() => {
    const unsubscribe = auth().onAuthStateChanged(setUser);
    return unsubscribe;
  }, []);

  return user;
};


// Now, in any component (like your “Profile” screen or “Settings” page), you can do:

// import { useAuth } from '@/features/auth/hooks/useAuth';

// const ProfileScreen = () => {
//   const user = useAuth();

//   return <Text>{user?.displayName}</Text>;
// };