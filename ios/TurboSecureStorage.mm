#import "TurboSecureStorage.h"
#import <TurboSecureStorage/TurboSecureStorage.h>
#import <LocalAuthentication/LocalAuthentication.h>
#import <React/RCTLog.h>
#import <Security/Security.h>
#import <jsi/jsi.h>
#import <iostream>

@interface TurboSecureStorage() <NativeTurboSecureStorageSpec>
@end

@implementation TurboSecureStorage

- (std::shared_ptr<facebook::react::TurboModule>)getTurboModule:(const facebook::react::ObjCTurboModule::InitParams &)params
{
    return std::make_shared<facebook::react::NativeTurboSecureStorageSpecJSI>(params);
}

+ (NSString *)moduleName
{
    return @"TurboSecureStorage";
}

typedef enum {
    kBiometricsStateAvailable,
    kBiometricsStateNotAvailable,
    kBiometricsStateLocked
} BiometricsState;

BiometricsState getBiometricsState()
{
    LAContext *myContext = [[LAContext alloc] init];
    NSError *authError = nil;
    
    if ([myContext canEvaluatePolicy:LAPolicyDeviceOwnerAuthenticationWithBiometrics error:&authError]) {
        return kBiometricsStateAvailable;
    } else {
        if (authError.code == LAErrorBiometryLockout) {
            return kBiometricsStateLocked;
        } else {
            return kBiometricsStateNotAvailable;
        }
    }
}

-(SecAccessControlRef) getBioSecAccessControl
{
    return SecAccessControlCreateWithFlags(kCFAllocatorDefault, // default allocator
                                           kSecAttrAccessibleWhenPasscodeSetThisDeviceOnly, // Require passcode to be set on device
                                           kSecAccessControlUserPresence, // checks for user presence with touchID, faceID or passcode :)
                                           nil); // Error pointer
}

- (NSMutableDictionary *)newDefaultDictionary:(NSString *)key
{
    NSMutableDictionary* queryDictionary = [[NSMutableDictionary alloc] init];
    [queryDictionary setObject:(id)kSecClassGenericPassword forKey:(id)kSecClass];
    NSData *encodedIdentifier = [key dataUsingEncoding:NSUTF8StringEncoding];
    [queryDictionary setObject:encodedIdentifier forKey:(id)kSecAttrGeneric];
    [queryDictionary setObject:encodedIdentifier forKey:(id)kSecAttrAccount];
    [queryDictionary setObject:[[NSBundle mainBundle] bundleIdentifier] forKey:(id)kSecAttrService];
    
    return queryDictionary;
}

- (CFStringRef)getAccessibilityValue:(NSString *)accessibility
{
    NSDictionary *keyMap = @{
        @"AccessibleWhenUnlocked": (__bridge NSString *)kSecAttrAccessibleWhenUnlocked,
        @"AccessibleAfterFirstUnlock": (__bridge NSString *)kSecAttrAccessibleAfterFirstUnlock,
        @"AccessibleAlways": (__bridge NSString *)kSecAttrAccessibleAlways,
        @"AccessibleWhenPasscodeSetThisDeviceOnly": (__bridge NSString *)kSecAttrAccessibleWhenPasscodeSetThisDeviceOnly,
        @"AccessibleWhenUnlockedThisDeviceOnly": (__bridge NSString *)kSecAttrAccessibleWhenUnlockedThisDeviceOnly,
        @"AccessibleAfterFirstUnlockThisDeviceOnly": (__bridge NSString *)kSecAttrAccessibleAfterFirstUnlockThisDeviceOnly,
        @"AccessibleAlwaysThisDeviceOnly": (__bridge NSString *)kSecAttrAccessibleAlwaysThisDeviceOnly
    };
    
    NSString *result = keyMap[accessibility];
    
    if (result) {
        return (__bridge CFStringRef)result;
    }
    
    return kSecAttrAccessibleAfterFirstUnlock;
}

- (NSDictionary *)setItem:(NSString *)key value:(NSString *)value options:(JS::NativeTurboSecureStorage::SpecSetItemOptions &)options
{
    CFStringRef accessibility = kSecAttrAccessibleAfterFirstUnlock;
    
     if(options.accessibility()) {
         accessibility = [self getAccessibilityValue:options.accessibility()];
     }
    
    bool withBiometrics = options.biometricAuthentication().value();
    
    [self innerDelete:key withBiometrics:withBiometrics];
    
    NSMutableDictionary *dict = [self newDefaultDictionary:key];
    
    // kSecAttrAccessControl is mutually excluse with kSecAttrAccessible
    // https://mobile-security.gitbook.io/mobile-security-testing-guide/ios-testing-guide/0x06f-testing-local-authentication
    if(withBiometrics) {
        [dict setObject:(__bridge_transfer id)[self getBioSecAccessControl] forKey:(id)kSecAttrAccessControl];
    } else {
        [dict setObject:(__bridge id)accessibility forKey:(id)kSecAttrAccessible];
    }
    
    NSData* valueData = [value dataUsingEncoding:NSUTF8StringEncoding];
    [dict setObject:valueData forKey:(id)kSecValueData];

    OSStatus status = SecItemAdd((CFDictionaryRef)dict, NULL);
    
    if (status == noErr) {
        return @{};
    }
    
    return @{
        @"error": @"Could not save value",
    };
}

- (NSDictionary *)getItem:(NSString *)key options:(JS::NativeTurboSecureStorage::SpecGetItemOptions &)options {
    NSMutableDictionary *dict = [self newDefaultDictionary:key];
    
    [dict setObject:(id)kSecMatchLimitOne forKey:(id)kSecMatchLimit];
    [dict setObject:(id)kCFBooleanTrue forKey:(id)kSecReturnData];
    
    if(options.biometricAuthentication()) {
        BiometricsState biometricsState = getBiometricsState();
        LAContext *authContext = [[LAContext alloc] init];
        
        // If device has no passcode/faceID/touchID then wallet-core cannot read the value from memory
        if(biometricsState == kBiometricsStateNotAvailable) {
            return @{
                @"error": @"Biometrics not available"
            };
        }
        
        if(biometricsState == kBiometricsStateLocked) {
            
            // TODO receiving a localized string might be necessary if this is happening on production
            [authContext evaluatePolicy:LAPolicyDeviceOwnerAuthenticationWithBiometrics
                        localizedReason:@"You need to unlock your device"
                                  reply:^(BOOL success, NSError *error) {
                if (!success) {
                    // Edge case when device might locked out after too many password attempts
                    // User has failed to authenticate, but due to this being a callback cannot interrupt upper lexical scope
                    // We should somehow prompt/tell the user that it has failed to authenticate
                    // and wallet could not be loaded
                }
            }];
        }

        [dict setObject:(__bridge id)[self getBioSecAccessControl] forKey:(id)kSecAttrAccessControl];
    }
    
    CFDataRef dataResult = nil;
    OSStatus status = SecItemCopyMatching((CFDictionaryRef)dict, (CFTypeRef*) &dataResult);
    
    if (status == noErr) {
        NSData* result = (__bridge NSData*) dataResult;
        NSString* returnString = [[NSString alloc] initWithData:result encoding:NSUTF8StringEncoding];
        
        return @{
            @"value": returnString
        };
    }
    
    return @{
        @"error": @"Could not get value"
    };
}

- (void)innerDelete:(NSString *)key withBiometrics:(bool)withBiometrics {
    NSMutableDictionary *dict = [self newDefaultDictionary:key];
    if(withBiometrics) {
        [dict setObject:(__bridge id)[self getBioSecAccessControl] forKey:(id)kSecAttrAccessControl];
    }
    SecItemDelete((CFDictionaryRef)dict);
}

- (NSDictionary *)deleteItem:(NSString *)key options:(JS::NativeTurboSecureStorage::SpecDeleteItemOptions &)options {

    [self innerDelete:key withBiometrics:options.biometricAuthentication().value()];

    return @{};
}

@end
