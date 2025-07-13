# Circle Client-Side Wallet SDK Integration

This module provides integration with Circle's Web3 Services client-side wallet SDK for Android applications.

## Prerequisites

1. **Circle Developer Account**: Sign up at [Circle Developer Console](https://console.circle.com/signup)
2. **GitHub Personal Access Token**: Create a token with `read:packages` permission
3. **Android Studio**: Version 2021.3.1 or later
4. **Minimum Android API Level**: 21 (Android 5.0)

## Installation Steps

### 1. Configure GitHub Credentials

Create or update your `local.properties` file in the root of your project:

```properties
# Circle W3S Android SDK Configuration
circle.maven.username=YOUR_GITHUB_USERNAME
circle.maven.password=YOUR_GITHUB_PERSONAL_ACCESS_TOKEN

# Existing configuration
pwsdk.maven.url=https://maven.pkg.github.com/circlefin/w3s-android-sdk
pwsdk.maven.username=YOUR_GITHUB_USERNAME
pwsdk.maven.password=YOUR_GITHUB_PERSONAL_ACCESS_TOKEN
```

### 2. Get Your App ID

1. Go to [Circle Developer Console](https://console.circle.com/)
2. Create a new app or select an existing one
3. Copy your App ID from the dashboard

### 3. Dependencies

The dependencies are already configured in this module:

```kotlin
// Circle Programmable Wallet SDK
implementation(libs.release.x024.x1.x4t171807)
implementation(libs.sdk)
```

### 4. Permissions

Add these permissions to your `AndroidManifest.xml`:

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
```

## Usage

### Basic Implementation

```kotlin
import com.dag.evm.CircleWallet
import android.content.Context

class YourActivity : AppCompatActivity() {
    
    private lateinit var circleWallet: CircleWallet
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize Circle Wallet
        val appId = "YOUR_CIRCLE_APP_ID" // Get this from Circle Developer Console
        circleWallet = CircleWallet(this, appId)
        
        // Initialize the SDK
        circleWallet.initialize(
            onSuccess = {
                // SDK initialized successfully
                Log.d("CircleWallet", "SDK initialized")
                setupAuthentication()
            },
            onError = { error ->
                Log.e("CircleWallet", "Failed to initialize: ${error.message}")
            }
        )
    }
    
    private fun setupAuthentication() {
        // These should come from your backend authentication system
        val userToken = getUserToken() // Implement this method
        val encryptionKey = getEncryptionKey() // Implement this method
        
        circleWallet.setAuthentication(
            userToken = userToken,
            encryptionKey = encryptionKey,
            onSuccess = {
                Log.d("CircleWallet", "Authentication set successfully")
                // Now you can execute wallet operations
            },
            onError = { error ->
                Log.e("CircleWallet", "Authentication failed: ${error.message}")
            }
        )
    }
    
    private fun executeWalletOperation(challengeId: String) {
        circleWallet.executeChallenge(
            challengeId = challengeId,
            callback = object : Callback<ExecuteResult> {
                override fun onResult(result: Result<ExecuteResult>) {
                    when (result) {
                        is Result.Success -> {
                            val executeResult = result.data
                            when (executeResult.resultType) {
                                ExecuteResult.ResultType.COMPLETE -> {
                                    Log.d("CircleWallet", "Operation completed: ${executeResult.data}")
                                }
                                ExecuteResult.ResultType.PENDING -> {
                                    Log.d("CircleWallet", "Operation pending")
                                }
                                ExecuteResult.ResultType.FAILED -> {
                                    Log.e("CircleWallet", "Operation failed")
                                }
                            }
                        }
                        is Result.Error -> {
                            Log.e("CircleWallet", "Error: ${result.error.message}")
                        }
                    }
                }
                
                override fun onError(error: ApiError) {
                    Log.e("CircleWallet", "API Error: ${error.message}")
                }
            }
        )
    }
    
    override fun onDestroy() {
        super.onDestroy()
        circleWallet.release()
    }
}
```

### Integration with Existing Wallet System

If you want to integrate with your existing wallet system, you can use the `CircleWallet` class as a component:

```kotlin
// In your wallet manager class
class WalletManager(private val context: Context) {
    
    private var circleWallet: CircleWallet? = null
    
    fun initializeCircleWallet(appId: String) {
        circleWallet = CircleWallet(context, appId)
        circleWallet?.initialize(
            onSuccess = { /* Handle success */ },
            onError = { /* Handle error */ }
        )
    }
    
    fun getCircleWallet(): CircleWallet? = circleWallet
}
```

## Common Operations

### Creating a Wallet

```kotlin
// This would typically be done through your backend
// The challenge ID comes from Circle's API
val createWalletChallengeId = "challenge_id_from_backend"
circleWallet.executeChallenge(createWalletChallengeId, callback)
```

### Sending Transactions

```kotlin
// Transaction challenge ID from your backend
val transactionChallengeId = "transaction_challenge_id"
circleWallet.executeChallenge(transactionChallengeId, callback)
```

## Error Handling

The SDK provides comprehensive error handling:

```kotlin
override fun onError(error: ApiError) {
    when (error.code) {
        "UNAUTHORIZED" -> {
            // Handle authentication errors
        }
        "NETWORK_ERROR" -> {
            // Handle network issues
        }
        else -> {
            // Handle other errors
            Log.e("CircleWallet", "Error: ${error.message}")
        }
    }
}
```

## Security Best Practices

1. **Never hardcode credentials**: Always use environment variables or secure storage
2. **Validate user tokens**: Ensure user tokens are valid and not expired
3. **Use HTTPS**: Always use secure connections
4. **Implement proper error handling**: Don't expose sensitive information in error messages

## Troubleshooting

### Build Issues

1. **Authentication Error**: Ensure your GitHub token has `read:packages` permission
2. **Dependency Resolution**: Check that your `local.properties` file is correctly configured
3. **Network Issues**: Verify your internet connection and proxy settings

### Runtime Issues

1. **SDK Not Initialized**: Always check `isWalletInitialized()` before operations
2. **Authentication Errors**: Verify your user token and encryption key are correct
3. **Challenge Execution**: Ensure challenge IDs are valid and not expired

## Support

- [Circle Developer Documentation](https://developers.circle.com/w3s/docs)
- [Circle Discord](https://discord.gg/circle)
- [Circle GitHub](https://github.com/circlefin)

## License

This integration follows the Apache-2.0 License as per Circle's SDK licensing. 