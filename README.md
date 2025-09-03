# Solana Pay with NFC

Android application that combines Solana Pay protocol with NFC technology. Tap two phones together to transfer SOL without QR codes or manual wallet addresses.

## Overview

This project was built for the Web3Auth Hackathon focusing on Solana Pay. It demonstrates how NFC can make cryptocurrency payments work like contactless card payments.

### Problem

Crypto payments require:
- Manual copying/pasting of wallet addresses  
- QR code scanning
- Risk of human error in address entry
- Multiple app switches

### Solution

Tap two phones together to initiate a Solana Pay transaction. NFC transmits the payment request between devices.

## Key Features

- Web3Auth Integration: Email-based authentication with wallet generation
- NFC Payments: Tap phones together to transfer SOL
- Biometric Security: Hardware-backed encryption with biometric authentication
- Solana Pay Protocol: Transactions on Solana blockchain  
- UI: Interface built with Jetpack Compose
- Multi-language Support: English and German

## Technical Architecture

The application follows clean architecture principles:

```
com.dag.mypayandroid/
├── base/                          # Core infrastructure & shared components
│   ├── helper/
│   │   ├── blockchain/           # Web3Auth & Solana integration
│   │   ├── security/            # NFC, Biometric & cryptographic operations
│   │   └── system/              # System-level utilities
│   └── solanapay/               # Solana Pay protocol implementation
├── feature/                      # Feature-specific modules
│   ├── home/                    # Main payment interface
│   ├── login/                   # Authentication flow
│   └── settings/                # App configuration
└── ui/                          # Design system & themes
```

### Core Components

#### Blockchain Integration (`base/helper/blockchain/`)

**Web3Auth Helper** (`Web3AuthHelper.kt`, `Web3AuthHelperImpl.kt`):
- Web3Auth SDK integration for email-based authentication
- Ed25519 private key generation for Solana
- Session management
- Social login providers

```kotlin
interface Web3AuthHelper {
    suspend fun login(loginParams: LoginParams): CompletableFuture<Web3AuthResponse>
    suspend fun logOut(): CompletableFuture<Void>
    fun getSolanaPrivateKey(): String
    fun getUserInfo(): UserInfo
    suspend fun isUserAuthenticated(): Boolean
}
```

**Solana Helper** (`SolanaHelper.kt`, `SolanaHelperImpl.kt`):
- Creates and manages Solana Pay URLs
- Handles SOL and SPL token transfers
- Integrates with Solana RPC for transaction processing
- Transaction signing and broadcasting

```kotlin
interface SolanaHelper {
    fun prepareSolanaPay(
        transferRequestField: TransferRequestURLFields,
        onUrlReady: (tx: URI)-> Unit
    )
    suspend fun receiveSolanaPayAndMakePayment(
        keypair: Keypair,
        paymentUrl: String,
        onSigned: (tx: Transaction)-> Unit
    )
}
```

**Wallet Manager** (`WalletManager.kt`):
- Storage of private keys using Android Keystore
- Biometric authentication for key access
- Hardware-backed encryption with AES-256
- Wallet lifecycle management (creation, locking, unlocking)

#### Security Layer (`base/helper/security/`)

**NFC Communication** (`NFCHelper.kt`, `MyHostApduService.kt`):
- Host Card Emulation (HCE) - converts phone to card terminal via HostApduService
- Reader and Tag modes for bidirectional communication
- APDU (Application Protocol Data Unit) command processing
- Custom AID (Application Identifier): `F0010203040506`

```kotlin
enum class NFCMode { TAG, READER }

interface NFCListener {
    fun onMessageReceived(message: String)
    fun onNFCError(error: String)
    fun onNFCStateChanged(mode: NFCMode)
}
```

**Biometric Security** (`BiometricHelper.kt`):
- Android Keystore integration for hardware-backed security
- Support for fingerprint, face unlock, and device PIN
- AES-256-CBC encryption with biometric-protected keys
- Key generation with `setUserAuthenticationRequired(true)`

#### Solana Pay Implementation (`base/solanapay/`)

**URL Encoding/Decoding** (`EncodeURL.kt`, `ParseURL.kt`):
- Solana Pay URL generation
- Transfer requests with metadata
- URL validation and parsing with error handling
- Standard Solana Pay format: `solana:<recipient>?amount=<amount>`

**Transaction Creation** (`CreateTransfer.kt`):
- Builds Solana transactions for SOL and SPL tokens
- Associated Token Account (ATA) creation
- Transaction fees and recent blockhash
- Signs transactions with user's private key

```kotlin
data class TransferRequestURLFields(
    val recipient: Recipient,
    val amount: Amount,
    val tokenDecimal: Int,
    val splToken: SPLToken? = null,
    val reference: References? = null,
    val label: Label? = null,
    val message: Message? = null,
    val memo: Memo? = null
)
```

#### System Integration (`base/helper/system/`)

**Activity Management** (`ActivityHolder.kt`):
- Activity lifecycle management
- Current activity context for biometric operations
- Activity transitions and state management

**Alert Dialog System** (`AlertDialogManager.kt`):
- Dialog management
- Error handling and user feedback
- Various dialog types and actions

## Payment Flow

### Sending Money (Initiating Payment)

1. **User Authentication**: User logs in via Web3Auth with email
2. **Amount Entry**: User specifies amount to send in the ReceiveView
3. **NFC Tag Mode**: App switches to NFC Tag mode (HCE)
4. **Solana Pay URL Generation**: Creates standardized payment URL
5. **NFC Transmission**: URL stored in SharedPreferences for HCE service
6. **Tap Detection**: When another phone taps, HCE service responds with payment data

### Receiving Money (Processing Payment)

1. **NFC Reader Mode**: Receiving phone operates as card reader
2. **Tag Discovery**: Detects sender phone acting as card terminal via HostApduService
3. **APDU Communication**: Sends SELECT command to HCE service
4. **Data Reception**: Receives Solana Pay URL from sender
5. **URL Parsing**: Validates and extracts payment details
6. **Transaction Creation**: Builds Solana transaction
7. **Biometric Confirmation**: User authenticates to access private key
8. **Transaction Signing**: Signs transaction with recovered private key
9. **Blockchain Submission**: Broadcasts transaction to Solana network

## Security Features

### Multi-Layer Security

1. **Hardware Security Module**: Android Keystore for key protection
2. **Biometric Authentication**: Required for sensitive operations
3. **Encrypted Storage**: Private keys encrypted with hardware-backed keys
4. **Session Management**: Web3Auth session handling
5. **Input Validation**: Validation of user inputs and URLs
6. **Error Handling**: Error handling without exposing data

### NFC Security

- **Custom AID**: Uses Application Identifier to avoid conflicts
- **Data Validation**: All received NFC data undergoes validation
- **Timeout Mechanisms**: NFC operations have timeouts
- **Error Recovery**: Error handling for NFC communication failures

## Getting Started

### Prerequisites

- Android 6.0+ (API level 23+)
- NFC-enabled Android device
- Active internet connection
- Biometric authentication or device PIN set up

### Installation

1. Clone the repository
2. Open in Android Studio
3. Configure Web3Auth credentials in your build configuration
4. Set up Solana RPC endpoint (Mainnet/Devnet/Testnet)
5. Build and install on NFC-enabled Android devices

### Configuration

Update your `local.properties` or build configuration with:
```properties
WEB3AUTH_CLIENT_ID=your_web3auth_client_id
SOLANA_RPC_URL=https://api.mainnet-beta.solana.com
```

## Testing

The application supports testing on Mainnet and Devnet:

- **Devnet**: For development and testing
- **Testnet**: For pre-production testing
- **Mainnet**: For production use with real SOL

## Supported Features

- SOL transfers via NFC tap
- Email-based authentication (Web3Auth)
- Biometric wallet security
- Multi-language support (EN/DE)
- Transaction history
- Settings management
- SPL token support (implemented, testing in progress)
- Transaction metadata and memos
- Reference tracking for payments

## License

This project is built for hackathon purposes and demonstrates the integration of Web3Auth, Solana Pay, and NFC technologies.

### Why This Matters

This project bridges the gap between contactless payments and decentralized finance. By making crypto payments work through phone tapping, it removes barriers to cryptocurrency adoption and creates an intuitive payment experience.

The combination of Web3Auth authentication, Solana transactions, and NFC availability creates a foundation for peer-to-peer payments.

---

Built for the Web3Auth Hackathon - Making crypto payments work through phone tapping
