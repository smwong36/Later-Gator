import React from 'react';
import { View, Button, Text, StyleSheet, Image } from 'react-native';
import { signInWithGoogle } from '../api/authService';

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
      <Image
        source={require('../../../assets/logo.png')}
        style={{ width: 120, height: 120, marginBottom: 32 }}
      />
      <Text style={styles.title}>Welcome to LaterGator</Text>
      <Button title="Continue with Google" onPress={handleGoogleLogin} />
    </View>
  );
};

const styles = StyleSheet.create({
  container: { flex: 1, justifyContent: 'center', alignItems: 'center', padding: 24 },
  title: { fontSize: 22, fontWeight: '600', marginBottom: 24 },
});

export default LoginScreen;
