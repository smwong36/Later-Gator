import React from 'react';
import { createNativeStackNavigator } from '@react-navigation/native-stack';
import LoginScreen from '../features/auth/screens/LoginScreen';
import MainNavigator from './MainNavigator';

const Stack = createNativeStackNavigator();

const AppNavigator = ({ user }: { user: any }) => (
  <Stack.Navigator screenOptions={{ headerShown: false }}>
    {user ? (
      <Stack.Screen name="Main" component={MainNavigator} />
    ) : (
      <Stack.Screen name="Login" component={LoginScreen} />
    )}
  </Stack.Navigator>
);

export default AppNavigator;
