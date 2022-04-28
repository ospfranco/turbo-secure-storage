import React from 'react';
import { Alert, Button, SafeAreaView } from 'react-native';
import TurboSecureStorage from 'turbo-secure-storage';

const App = () => {
  return (
    <SafeAreaView>
      <Button
        title="Set Item"
        onPress={() => {
          const { error } = TurboSecureStorage.setItem('foo', 'bar', {
            // accessibility: ACCESSIBILITY.WHEN_PASSCODE_SET_THIS_DEVICE_ONLY,
            biometricAuthentication: true,
          });
          // const { error } = TurboSecureStorage.setItem('foo', 'bar');
          if (error) {
            Alert.alert('Could not save string');
          }
        }}
      />
      <Button
        title="Get Item"
        onPress={() => {
          const { error, value } = TurboSecureStorage.getItem('foo', {
            biometricAuthentication: true,
          });

          if (error) {
            Alert.alert('Could not get Item');
          }

          console.warn(`value is ${value}`);
        }}
      />
      <Button
        title="Delete item"
        onPress={() => {
          const { error } = TurboSecureStorage.deleteItem('foo', {
            biometricAuthentication: true,
          });

          if (error) {
            Alert.alert('Could not delete Item');
          }

          console.warn('item deleted');
        }}
      />
    </SafeAreaView>
  );
};

export default App;
