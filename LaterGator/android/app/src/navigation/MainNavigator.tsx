// This is our Home Page


import React from 'react';
import { createBottomTabNavigator } from '@react-navigation/bottom-tabs';
import { View, Text, Button, ActivityIndicator, Alert } from 'react-native';
import { signOut } from '../features/auth/api/authService';

const Tab = createBottomTabNavigator();

// Placeholder screens
const HomeScreen = () => {
  <View style={{ flex: 1, justifyContent: 'center', alignItems: 'center' }}>
    <Text>Home</Text>
  </View>

  const [busy, setBusy] = React.useState(false);

  const handleSignOut = async () => {
    setBusy(true);
    try {
      await signOut();
    } catch (err) {
      console.error('Sign-out failed', err);
      Alert.alert('Sign out failed', String(err));
    } finally {
      setBusy(false);
    }
  };

  return (
    <View style={{ flex: 1, justifyContent: 'center', alignItems: 'center' }}>
      <Text>Home</Text>
      {busy ? (
        <ActivityIndicator style={{ marginTop: 12 }} />
      ) : (
        <Button title="Sign out" onPress={handleSignOut} />
      )}
    </View>
  );
};

const PomodoroScreen = () => (
  <View style={{ flex: 1, justifyContent: 'center', alignItems: 'center' }}>
    <Text>Pomodoro</Text>
  </View>
);

const SettingsScreen = () => (
  <View style={{ flex: 1, justifyContent: 'center', alignItems: 'center' }}>
    <Text>Settings</Text>
  </View>
);

const MainNavigator = () => (
  <Tab.Navigator screenOptions={{ headerShown: true, tabBarIcon: () => null, }}>
    <Tab.Screen name="Home" component={HomeScreen} />
    <Tab.Screen name="Pomodoro" component={PomodoroScreen} />
    <Tab.Screen name="Settings" component={SettingsScreen} />
  </Tab.Navigator>
);

export default MainNavigator;
