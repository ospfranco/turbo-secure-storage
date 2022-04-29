import type { TurboModule } from 'react-native';
import { TurboModuleRegistry } from 'react-native';

export enum ACCESSIBILITY {
  /**
   * The data in the keychain item cannot be accessed after a restart until the device
   * has been unlocked once by the user.
   */
  AFTER_FIRST_UNLOCK = 'AccessibleAfterFirstUnlock',
  /**
   * The data in the keychain item cannot be accessed after a restart until the device
   * has been unlocked once by the user.
   * Items with this attribute never migrate to a new device.
   */
  AFTER_FIRST_UNLOCK_THIS_DEVICE_ONLY = 'AccessibleAfterFirstUnlockThisDeviceOnly',
  /**
   * The data in the keychain item can always be accessed regardless of whether
   * the device is locked.
   */
  ALWAYS = 'AccessibleAlways',
  /**
   * The data in the keychain item can always be accessed regardless of whether the
   * device is locked.
   * Items with this attribute never migrate to a new device.
   */
  ALWAYS_THIS_DEVICE_ONLY = 'AccessibleAlwaysThisDeviceOnly',
  /**
   * The data in the keychain can only be accessed when the device is unlocked.
   * Only available if a passcode is set on the device.
   * Items with this attribute never migrate to a new device.
   */
  WHEN_PASSCODE_SET_THIS_DEVICE_ONLY = 'AccessibleWhenPasscodeSetThisDeviceOnly',
  /**
   * The data in the keychain item can be accessed only while the device is
   * unlocked by the user.
   * This is the default value.
   */
  WHEN_UNLOCKED = 'AccessibleWhenUnlocked',
  /**
   * The data in the keychain item can be accessed only while the device is
   * unlocked by the user.
   * Items with this attribute do not migrate to a new device.
   */
  WHEN_UNLOCKED_THIS_DEVICE_ONLY = 'AccessibleWhenUnlockedThisDeviceOnly',
}

export interface Spec extends TurboModule {
  getConstants: () => {};

  setItem(
    key: string,
    value: string,
    options: {
      accessibility?: string;
      biometricAuthentication?: boolean;
    }
  ): { error?: Object };

  getItem(
    key: string,
    options: {
      biometricAuthentication?: boolean;
    }
  ): { error?: Object; value: string };

  deleteItem(
    key: string,
    options: {
      biometricAuthentication?: boolean;
    }
  ): { error?: Object };
}

export default TurboModuleRegistry.getEnforcing<Spec>('TurboSecureStorage');
