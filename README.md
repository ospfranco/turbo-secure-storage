![screenshot](https://github.com/ospfranco/turbo-secure-storage/blob/main/header.png?raw=true)

<pre align="center">yarn add turbo-secure-storage</pre>

<div align="center">
  <a align="center" href="https://github.com/ospfranco?tab=followers">
    <img src="https://img.shields.io/github/followers/ospfranco?label=Follow%20%40ospfranco&style=social" />
  </a>
  <br />
  <a align="center" href="https://twitter.com/ospfranco">
    <img src="https://img.shields.io/twitter/follow/ospfranco?label=Follow%20%40ospfranco&style=social" />
  </a>
</div>
<br />

---

A turbo-module to securely store data, uses Keychain on iOS and KeyStore/EncryptedSharedPreferences on Android. It also supports Biometric authentication

## Gotcha's

- This being a TurboModule is only compatible with RN 0.68+
- Android's min SDK API is 23, to take advantange of the official keystore implementation

## Examples

```ts
import TurboSecureStorage, { ACCESSIBILITY } from 'turbo-secure-storage';

const { error } = TurboSecureStorage.setItem('foo', 'bar', {
  accessibility: ACCESSIBILITY.WHEN_PASSCODE_SET_THIS_DEVICE_ONLY,
  // OR
  withBiometrics: true,
});

const { error, value } = TurboSecureStorage.getItem('foo', {
  withBiometrics: true,
});

const { error } = TurboSecureStorage.deleteItem('foo', {
  withBiometrics: true,
});
```

> Unfortunately due how codegen works, you **always** need to pass the options object, even if it is empty

### iOS Accessibility

On iOS you can specify an accesibility value which allows to customize when the data is readable. It is mutually exclusive biometrics. So pick one or the other.

## TODO

- [x] Implement [official Android keystore implementation](https://github.com/android/security-samples/blob/master/BiometricLoginKotlin/app/src/main/java/com/example/biometricloginsample/CryptographyManager.kt#L78)
- [x] Add passcode / password fallback for Android if possible
- [ ] Add `getAllKeys`, `getAllItems` and `deleteAllItems` methods (if possible)
- [ ] Support Secure Enclave on Apple devices
- [ ] Create testing device list
- [ ] Security audits

## About me

I'm available for React Native consulting and also create other products, [get in touch](https://ospfranco.com). You can also see how this library was built from scratch on my [YouTube channel](https://www.youtube.com/watch?v=U0shm20ClkU).

## License

MIT License
