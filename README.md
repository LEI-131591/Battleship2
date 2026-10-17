# ⚓ Battleship 2.0

![Build Status](https://img.shields.io/badge/build-passing-brightgreen)
![Java Version](https://img.shields.io/badge/Java-17%2B-blue)
![License](https://img.shields.io/badge/license-MIT-green)

> A modern take on the classic naval warfare game, designed for the XVII century setting with updated software engineering patterns.

---

## 📖 Table of Contents
- [Project Overview](#-project-overview)
- [Key Features](#-key-features)
- [Technical Stack](#-technical-stack)
- [Installation & Setup](#-installation--setup)
- [Code Architecture](#-code-architecture)
- [Roadmap](#-roadmap)
- [Contributing](#-contributing)

---

## 🎯 Project Overview
This project serves as a template and reference for students learning **Object-Oriented Programming (OOP)** and **Software Quality**. It simulates a battleship environment where players must strategically place ships and sink the enemy fleet.

### 🎮 The Rules
The game is played on a grid (typically 10x10). The coordinate system is defined as:

$$(x, y) \in \{0, \dots, 9\} \times \{0, \dots, 9\}$$

Hits are calculated based on the intersection of the shot vector and the ship's bounding box.

---

## ✨ Key Features
| Feature | Description | Status |
| :--- | :--- | :---: |
| **Grid System** | Flexible $N \times N$ board generation. | ✅ |
| **Ship Varieties** | Galleons, Frigates, and Brigantines (XVII Century theme). | ✅ |
| **AI Opponent** | Heuristic-based targeting system. | 🚧 |
| **Network Play** | Socket-based multiplayer. | ❌ |

---

## 🛠 Technical Stack
* **Language:** Java 17
* **Build Tool:** Maven / Gradle
* **Testing:** JUnit 5
* **Logging:** Log4j2

---

## 🚀 Installation & Setup

### Prerequisites
* JDK 17 or higher
* Git

### Step-by-Step
1. **Clone the repository:**
   ```bash
   git clone [https://github.com/britoeabreu/Battleship2.git](https://github.com/britoeabreu/Battleship2.git)
   ```
2. **Navigate to directory:**
   ```bash
   cd Battleship2
   ```
3. **Compile and Run:**
   ```bash
   javac Main.java && java Main
   ```

---

## 📚 Documentation

You can access the generated Javadoc here:

👉 [Battleship2 API Documentation](https://britoeabreu.github.io/Battleship2/)


### Core Logic
```java
public class Ship {
    private String name;
    private int size;
    private boolean isSunk;

    // TODO: Implement damage logic
    public void hit() {
        // Implementation here
    }
}
```

### Design Patterns Used:
- **Strategy Pattern:** For different AI difficulty levels.
- **Observer Pattern:** To update the UI when a ship is hit.
</details>

### Logic Flow
```mermaid
graph TD
    A[Start Game] --> B{Place Ships}
    B --> C[Player Turn]
    C --> D[Target Coordinate]
    D --> E{Hit or Miss?}
    E -- Hit --> F[Check if Sunk]
    E -- Miss --> G[AI Turn]
    F --> G
    G --> C
```

---

## 🗺 Roadmap
- [x] Basic grid implementation
- [x] Ship placement validation
- [ ] Add sound effects (SFX)
- [ ] Implement "Fog of War" mechanic
- [ ] **Multiplayer Integration** (High Priority)

---

## Move History with SQLite

The console simulator automatically stores games and moves in a local SQLite database. The history remains available after closing and restarting the application.

### Exporting the Current Game to CSV

After starting a game with `gerafrota` or `lefrota` and firing at least one
`rajada`, enter `exportar` to save the current game's shots to a CSV file. The
command remains available after the final ship is sunk. Enter `desisto` to leave
the console or start another game.

Each row contains the move number, row, column, and outcome (`MISS`, `HIT`,
`SUNK`, `REPEATED`, or `INVALID`). Files are created under `data/exports/` with
unique names, so exporting again does not overwrite an earlier file. This
feature uses Apache Commons CSV and does not export past games from SQLite.

Each new game created with `gerafrota` or `lefrota` receives a unique identifier. Both manual moves (`rajada`) and automatically generated moves (`simula`) are saved.

### Stored Information

The database contains three related tables:

- **`games`** — game identifier and start time.
- **`moves`** — associated game, move number, and recording time.
- **`shots`** — shot order, coordinates, outcome, and ship type when applicable.

Each move contains three shots. Possible outcomes are:

| Outcome | Meaning |
|---------|---------|
| `MISS` | The shot hit water. |
| `HIT` | The shot hit a ship. |
| `SUNK` | The shot sank a ship. |
| `REPEATED` | The position had already been targeted. |
| `INVALID` | The position is outside the board. |

Each move and its three shots are saved in a single transaction. If an insertion fails, the transaction is rolled back to prevent incomplete records.

### Viewing the History

Run the console application through `battleship.Main`.

List saved games:

```text
jogos
```

Display the history of a specific game:

```text
historico GAME_ID
```

Replace `GAME_ID` with the full identifier shown by the `jogos` command. History can be viewed immediately after restarting the application, without starting a new game.

### Database Setup

Persistence uses the `org.xerial:sqlite-jdbc` dependency, managed by Maven. No separate database server is required.

The database and its tables are created automatically at:

```text
data/battleship.db
```

This path is relative to the application's working directory. The database file is excluded from version control.

If a persistence error occurs, the application reports the error and stops the console session. Previously committed records remain stored.

### Tests

Run the persistence tests with:

```bash
mvn -Dtest=SqliteMoveRepositoryTest test
```

The tests use temporary databases and cover:

- Reading saved results through a new repository instance.
- Preserving shot order and invalid coordinates.
- Keeping games separate and rejecting duplicate moves.
- Rolling back a complete move when the third shot cannot be saved.


---

## AI Communication Protocol — Part C

The protocol exercises were performed by the AI assistant using the
project's Java simulator. The records include four validated example
boards and eleven exchanges with actual JSON responses.

[View the protocol exercises](docs/ia/C-protocolo-evidencias.md)

---

### Scope

Persistence currently supports the console simulator. It stores historical moves; it does not restore a saved game for continued play or persist moves from the REST server.

---

## 🧪 Testing
We use high-coverage unit testing to ensure game stability. Run tests using:
```bash
mvn test
```

> [!TIP]
> Use the `-Dtest=ClassName` flag to run specific test suites during development.

---

## 🤝 Contributing
Contributions are what make the open-source community such an amazing place to learn, inspire, and create.

1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the Branch (`git push origin feature/AmazingFeature`)
5. Open a **Pull Request**

---

## 📄 License
Distributed under the MIT License. See `LICENSE` for more information.

---
**Maintained by:** [@britoeabreu](https://github.com/britoeabreu)  
*Created for the Software Engineering students at ISCTE-IUL.*
