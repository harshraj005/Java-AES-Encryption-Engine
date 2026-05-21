# 🔐 Java AES-GCM Secure File Vault

A robust Java implementation of **AES-256-GCM** (Galois/Counter Mode) for encrypting and decrypting files of any type and size. This project demonstrates authenticated symmetric-key cryptography using Java's built-in `javax.crypto` library — now with both a **CLI** and a **Swing GUI** interface.

---

## 📌 Table of Contents

- [About the Project](#about-the-project)
- [Features](#features)
- [Project Structure](#project-structure)
- [Prerequisites](#prerequisites)
- [Getting Started](#getting-started)
  - [Clone the Repository](#clone-the-repository)
  - [Running the GUI](#running-the-gui)
  - [Running the CLI](#running-the-cli)
  - [Running from Command Line (Manual Compile)](#running-from-command-line-manual-compile)
- [How It Works](#how-it-works)
- [AES-GCM Overview](#aes-gcm-overview)
- [Security Notes](#security-notes)
- [Technologies Used](#technologies-used)
- [Contributing](#contributing)
- [License](#license)

---

## About the Project

The **Java AES-GCM Secure File Vault** provides a clean, production-grade implementation of AES-256-GCM authenticated encryption for files. It is suitable for learning cryptographic concepts, prototyping secure data handling, or integrating file encryption into Java-based applications.

The project ships with two interfaces:
- **GUI** — a dark-themed Swing application with file browser dialogs and real-time status feedback
- **CLI** — the original terminal-based interface for scripting or headless environments

---

## ✨ Features

- **AES-256-GCM** authenticated encryption — confidentiality *and* integrity in one pass
- **128-bit authentication tag** — tampered files are detected and rejected on decryption
- **96-bit random IV** (NIST SP 800-38D recommended) prepended automatically to each encrypted file
- **4 KB streaming** — encrypts and decrypts files of any size without loading them into memory
- **Swing GUI** with file browser, color-coded status bar, and non-blocking background operations
- **CLI** interface for terminal and scripted workflows
- No external dependencies — only `javax.crypto` and `java.security`
- Eclipse-compatible project setup (`.classpath` and `.project` included)

---

## 📁 Project Structure

```
Java-AES-Encryption-Engine/
├── src/com/aes/
│   ├── main/
│   │   └── Main.java                   # CLI entry point — terminal-based interface
│   │
│   ├── gui/
│   │   └── AESGCMGui.java              # GUI entry point — Swing desktop interface
│   │
│   ├── key/
│   │   └── KeyGeneratorUtil.java       # Generates AES-256 secret keys and 96-bit random IVs
│   │
│   ├── cryptographyEngine/
│   │   └── AESGCMCipher.java           # AES-GCM cipher engine (init, streaming update, finalize)
│   │
│   └── file_io/
│       └── SecureFileStream.java       # Reads/writes files in 4 KB chunks; prepends/reads IV
│
├── .classpath
├── .project
└── README.md
```

---

## ✅ Prerequisites

- **Java JDK 8 or higher** — [Download here](https://www.oracle.com/java/technologies/downloads/)
- **Eclipse IDE** *(optional)* — [Download here](https://www.eclipse.org/downloads/)

Verify your Java installation:

```bash
java -version
javac -version
```

---

## 🚀 Getting Started

### Clone the Repository

```bash
git clone https://github.com/harshraj005/Java-AES-Encryption-Engine.git
cd Java-AES-Encryption-Engine
```

---

### Running the GUI

The GUI provides a dark-themed desktop interface with file browser dialogs and live status feedback.

**In Eclipse:**
1. Go to **File → Import → Existing Projects into Workspace** and select the cloned folder.
2. Right-click `AESGCMGui.java` inside `src/com/aes/gui/` and select **Run As → Java Application**.

**Screenshot overview:**
- **Encrypt tab** — browse for any source file, choose an output path, click **Encrypt**
- **Decrypt tab** — browse for an `.encrypted` file, choose an output path, click **Decrypt**
- **New Session Key** button (top-right) — generates a fresh 256-bit key + IV for a new session
- **Status bar** — color-coded feedback (green = success, red = error, yellow = warning)

---

### Running the CLI

The original terminal interface is still fully available.

**In Eclipse:**
1. Right-click `Main.java` inside `src/com/aes/main/` and select **Run As → Java Application**.
2. Follow the on-screen prompts — press `1` to encrypt, `2` to decrypt, `3` to exit.

---

### Running from Command Line (Manual Compile)

```bash
# Navigate to the src directory
cd src

# Compile all source files
javac com/aes/key/KeyGeneratorUtil.java \
      com/aes/cryptographyEngine/AESGCMCipher.java \
      com/aes/file_io/SecureFileStream.java \
      com/aes/gui/AESGCMGui.java \
      com/aes/main/Main.java

# Launch the GUI
java com.aes.gui.AESGCMGui

# OR launch the CLI
java com.aes.main.Main
```

---

## ⚙️ How It Works

The project is split into focused modules, each with a single responsibility:

| Module | Class | Role |
|---|---|---|
| `main` | `Main.java` | CLI entry point — drives the encrypt/decrypt loop |
| `gui` | `AESGCMGui.java` | Swing GUI — file browsers, background workers, status bar |
| `key` | `KeyGeneratorUtil.java` | Generates AES-256 secret keys and 96-bit random IVs |
| `cryptographyEngine` | `AESGCMCipher.java` | Initializes the cipher, processes chunks, finalizes with GCM tag |
| `file_io` | `SecureFileStream.java` | Streams file bytes in 4 KB chunks; handles IV prepend/read |

**Encryption flow:**

```
Input File
    ↓
[SecureFileStream] — reads in 4 KB chunks
    ↓
[KeyGeneratorUtil] — provides AES-256 key + random IV
    ↓
[AESGCMCipher] — encrypts chunks, appends 128-bit auth tag
    ↓
Encrypted Output File (IV prepended as first 12 bytes)
```

**Decryption flow:**

```
Encrypted File
    ↓
[SecureFileStream] — reads first 12 bytes as IV, then ciphertext in 4 KB chunks
    ↓
[AESGCMCipher] — decrypts chunks, verifies 128-bit auth tag
    ↓
Decrypted Output File (or rejection if file is tampered)
```

> If the authentication tag does not match — meaning the file has been modified, corrupted, or decrypted with the wrong key — an `AEADBadTagException` is thrown and no output is written.

---

## 🔑 AES-GCM Overview

**AES (Advanced Encryption Standard)** is a symmetric block cipher standardized by NIST (FIPS PUB 197). **GCM (Galois/Counter Mode)** adds authenticated encryption, providing both privacy and integrity without a separate HMAC step.

| Property | Details |
|---|---|
| Type | Symmetric AEAD (Authenticated Encryption with Associated Data) |
| Key size | 256 bits |
| IV (Nonce) size | 96 bits (12 bytes) — NIST recommended |
| Block size | 128 bits |
| Auth tag size | 128 bits |
| Standard | NIST SP 800-38D |

---

## 🔒 Security Notes

- **Session-scoped keys** — the secret key and IV are generated fresh at application startup and live in memory only. To encrypt and then decrypt the same file, both operations must be performed within the same session.
- **Never reuse an IV** with the same key in GCM mode — this application generates a new random IV on every encryption, which satisfies this requirement.
- **Key persistence** — for production use, integrate a key store (e.g., Java `KeyStore`, HSM, or a KMS) so keys can be saved and reloaded across sessions.
- **IV storage** — the IV is safely stored as the first 12 bytes of every encrypted file; no separate IV file is needed.

---

## 🛠️ Technologies Used

- **Java** — Core language (JDK 8+)
- **javax.crypto** — `Cipher`, `KeyGenerator`, `SecretKey`, `GCMParameterSpec`
- **java.security** — `SecureRandom`
- **javax.swing** — Desktop GUI (tabs, file chooser, custom-painted components)
- **Eclipse IDE** — Development environment

---

## 🤝 Contributing

Contributions are welcome! To contribute:

1. Fork the repository
2. Create a new branch: `git checkout -b feature/your-feature-name`
3. Commit your changes: `git commit -m "Add your message"`
4. Push to the branch: `git push origin feature/your-feature-name`
5. Open a Pull Request

---

## 📄 License

This project is open source. Feel free to use, modify, and distribute it for educational or personal use.

---

> **Author:** [harshraj005](https://github.com/harshraj005)
> **Repository:** [Java-AES-Encryption-Engine](https://github.com/harshraj005/Java-AES-Encryption-Engine)
