import React from 'react';
import { View, Button, Text, Image, Pressable } from 'react-native';
import { signInWithGoogle } from '../api/authService';
import { styles } from '../../../styles';

const LoginScreen = () => {
  const handleGoogleLogin = async () => {
    try {
      await signInWithGoogle();
      console.log('Logged in with Google!');
    } catch (error) {
      console.error(error);
    }
  };

  return (
    <View style={styles.container}>
      <View style ={styles.loginContainer}>
        <Text style={{...styles.heading, fontSize:30}}>Welcome to LaterGator</Text>
        <Pressable style={styles.buttonAccent} onPress={handleGoogleLogin}>
          <Text style={{...styles.buttonText}}>Continue with Google</Text>
        </Pressable>
      </View>
    </View>
  );
};

export default LoginScreen;
