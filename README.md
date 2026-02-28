# Scores Keeper

Android app for tracking board game scores. Create custom games, manage players, and track scores round by round with automatic Google Drive synchronization.

## Features

- **Configurable games** — Create games with min/max player count and win condition (highest or lowest score wins)
- **Player management** — Add players with custom colored avatars
- **Game sessions** — Start sessions and enter scores round by round
- **Edit and delete rounds** — Modify or delete a round during a game (tap to edit, long press to delete)
- **Live ranking** — Dynamic leaderboard with cumulative totals
- **Score chart** — Per-player colored line chart showing score progression
- **Player statistics** — Games played, wins, total points
- **Google Drive backup** — Automatic sync after each change with manual backup/restore
- **French UI**

## Tech Stack

| Component | Technology |
|---|---|
| Language | Kotlin 2.0.20 |
| UI | Jetpack Compose (Material 3) |
| Architecture | Clean Architecture (domain / data / presentation) |
| Dependency Injection | Hilt |
| Database | Room |
| Navigation | Navigation Compose |
| Charts | Vico |
| Preferences | DataStore |
| Backup | Google Drive REST API v3 |
| SDK | minSdk 26, targetSdk 35 |

## Build

```bash
# Debug build
./gradlew assembleDebug

# Build + install + launch on connected device
bash build-and-run.sh

# Clean build
bash build-and-run.sh --clean
```

Debug APK output: `app/build/outputs/apk/debug/app-debug.apk`

## Project Structure

```
com.scoreskeeper/
├── domain/
│   ├── model/          # Data models (Game, Player, Session, RoundScore...)
│   ├── repository/     # Repository interfaces
│   └── usecase/        # Use cases (one per business operation)
├── data/
│   ├── local/          # Room: entities, DAOs, mappers
│   ├── repository/     # Repository implementations
│   ├── di/             # Hilt modules
│   └── backup/         # Google Drive sync (DriveBackupService, SyncManager)
└── presentation/
    ├── navigation/     # Navigation graph
    ├── theme/          # Material 3 theme
    ├── components/     # Reusable composables
    └── screens/        # Screens: home, game, session, settings
```
