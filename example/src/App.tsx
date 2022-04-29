import React, { useState } from 'react';
import { Alert, Button, SafeAreaView, StyleSheet, Text } from 'react-native';
import TurboSecureStorage, { ACCESSIBILITY } from 'turbo-secure-storage';

const App = () => {
  const [value, setValue] = useState<string | null>(null);

  return (
    <SafeAreaView style={STYLES.container}>
      <Text>Value is:</Text>
      <Text style={STYLES.value}>{value ?? '?'}</Text>
      <Button
        title="Set"
        onPress={() => {
          const { error } = TurboSecureStorage.setItem('foo', 'bar', {
            accessibility: ACCESSIBILITY.WHEN_PASSCODE_SET_THIS_DEVICE_ONLY,
            biometricAuthentication: true,
          });
          if (error) {
            Alert.alert('Could not save string');
          }
        }}
      />
      <Button
        title="Get"
        onPress={() => {
          const { error, value } = TurboSecureStorage.getItem('foo', {
            biometricAuthentication: true,
          });

          if (error) {
            Alert.alert('Could not get Item');
          }

          setValue(value);
        }}
      />
      <Button
        title="Delete"
        onPress={() => {
          const { error } = TurboSecureStorage.deleteItem('foo', {
            biometricAuthentication: true,
          });

          if (error) {
            Alert.alert('Could not delete Item');
          }

          setValue(null);
        }}
      />
    </SafeAreaView>
  );
};

export default App;

const STYLES = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: 'white',
    justifyContent: 'center',
    alignItems: 'center',
  },
  value: {
    paddingVertical: 20,
    fontSize: 32,
  },
});
