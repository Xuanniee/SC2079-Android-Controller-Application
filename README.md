# SC2079 Multi-Disciplinary Project Android Controller

## 1 Android Architecture
- Jetpack Compose UI with Material3
- MVVM with StateFlow and Hilt for DI
- Navigation Compose
- Kotlin coroutines for async work
- Lottie for animations (see ui/components/LottieAnimation.kt)

### 1.1 Modules
#### 1.1.1 BluetoothService
- Scan and list connected devices
- Connect/Disconnect with Android Tablet
- Read Loop (For incoming strings)
- Write Queue (For outgoing strings)
- Auto-reconnect (C.8)

#### 1.1.2 UI
- Home Screen:
    - Bluetooth Connection (C.2)
    - Initiate scanning, selection & connection with Android
- Controller buttons (C.3)
    - For controlling the robot car manually
- Robot Status Screen (C.4)
    - Provides updates to what the robot is currently doing e.g. moving
    - Updates on TARGET / ROBOT strings (C.9–C.10)
- Maze Arena Screen
    - Allow users to view the obstacles and robot (C.5)
    - Placement & Removal of obstacles (C.6)
    - Annotation of Obstacle Face (C.7)

#### 1.1.3 Robot Communication Protocol
- Parse incoming messages into typed events like STATUS, ROBOT,...
- Build outgoing commands to be sent to the robot

#### 1.1.4 Map Model
- To model the state of the robot on the map
- To model the state of the obstacle on the map

## Project Structure (Jetpack Compose, MVVM, Hilt)
```
app/src/main/java/com/sc2079/androidcontroller/
├── ControllerApplication.kt
├── di/
│   └── AppModule.kt
├── navigation/
│   ├── AppNavHost.kt
│   └── Screen.kt
├── ui/
│   ├── components/
│   │   └── LottieAnimation.kt
│   └── theme/
│       ├── Color.kt
│       ├── Theme.kt
│       └── Type.kt
├── util/
│   └── permissions/
│       └── BluetoothPermissions.kt
└── features/
    ├── main/
    │   ├── data/
    │   ├── domain/
    │   ├── network/
    │   ├── presentation/
    │   └── ui/
    │       └── MainActivity.kt
    │
    ├── bluetooth/
    │   ├── data/
    │   │   ├── client/
    │   │   │   └── BluetoothClient.kt
    │   │   └── repository/
    │   │       └── BluetoothRepositoryImpl.kt
    │   ├── domain/
    │   │   ├── model/
    │   │   │   ├── BluetoothConnectionState.kt
    │   │   │   └── BluetoothDeviceInfo.kt
    │   │   └── repository/
    │   │       └── BluetoothRepository.kt
    │   ├── network/
    │   ├── presentation/
    │   │   └── BluetoothViewModel.kt
    │   └── ui/
    │       └── BluetoothScreen.kt
    │
    ├── map/
    │   ├── data/
    │   ├── domain/
    │   ├── network/
    │   ├── presentation/
    │   └── ui/
    │
    └── controller/
        ├── data/
        ├── domain/
        ├── network/
        ├── presentation/
        └── ui/
```