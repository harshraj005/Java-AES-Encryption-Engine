# 🔐 Java AES Encryption Engine

A lightweight Java implementation of the **Advanced Encryption Standard (AES)** algorithm for encrypting and decrypting data. This project demonstrates symmetric-key cryptography using Java's built-in `javax.crypto` library.

---

## 📌 Table of Contents

- [About the Project](#about-the-project)
- [Features](#features)
- [Project Structure](#project-structure)
- [Prerequisites](#prerequisites)
- [Getting Started](#getting-started)
  - [Clone the Repository](#clone-the-repository)
  - [Running with Eclipse](#running-with-eclipse)
  - [Running from Command Line](#running-from-command-line)
- [How It Works](#how-it-works)
- [AES Overview](#aes-overview)
- [Technologies Used](#technologies-used)
- [Contributing](#contributing)
- [License](#license)

---

## About the Project

The **Java AES Encryption Engine** provides a clean and straightforward implementation of AES encryption and decryption in Java. It is ideal for learning cryptographic concepts, prototyping secure data handling, or integrating encryption into Java-based applications.

---

## ✨ Features

- AES-based symmetric encryption and decryption
- Encodes encrypted output in **Base64** for safe text representation
- Uses Java's standard `javax.crypto` and `java.security` libraries (no external dependencies)
- Modular package structure with clear separation of concerns (`main`, `key_management`, `cryptography_engine`, `file_io`)
- Eclipse-compatible project setup (`.classpath` and `.project` included)

---

## 📁 Project Structure

```
Java-AES-Encryption-Engine/
├── src/com/aes
│   ├── main/
│   │   └── ApplicationMain.java        # The Orchestrator — entry point, ties all modules together
│   │
│   ├── key_management/
│   │   └── KeyGeneratorUtil.java       # The Vault — handles AES key generation and management
│   │
│   ├── cryptography_engine/
│   │   └── AESGCMCipher.java           # The Grinder — performs AES-GCM encryption & decryption
│   │
│   └── file_io/
│       └── SecureFileStreamer.java     # The Pipeline — manages secure file reading and writing
│
├── .classpath                          # Eclipse classpath configuration
├── .project                            # Eclipse project configuration
└── README.md
```

---

## ✅ Prerequisites

- **Java JDK 8 or higher** — [Download here](https://www.oracle.com/java/technologies/downloads/)
- **Eclipse IDE** *(optional, for IDE-based setup)* — [Download here](https://www.eclipse.org/downloads/)

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

### Running with Eclipse

1. Open **Eclipse IDE**.
2. Go to **File → Import → Existing Projects into Workspace**.
3. Browse to the cloned project folder and click **Finish**.
4. Right-click `ApplicationMain.java` inside `src/main/` and select **Run As → Java Application**.

### Running from Command Line

```bash
# Navigate to the src directory
cd src

# Compile all Java files across all packages
javac main/ApplicationMain.java key_management/KeyGeneratorUtil.java cryptography_engine/AESGCMCipher.java file_io/SecureFileStreamer.java

# Run the application
java main.ApplicationMain
```

---

## ⚙️ How It Works

The engine is split into four focused modules, each with a single responsibility:

| Module | Class | Role |
|--------|-------|------|
| `main` | `ApplicationMain.java` | **The Orchestrator** — entry point that wires all modules together and drives the flow |
| `key_management` | `KeyGeneratorUtil.java` | **The Vault** — generates and manages the AES secret key securely |
| `cryptography_engine` | `AESGCMCipher.java` | **The Grinder** — performs AES-GCM authenticated encryption and decryption |
| `file_io` | `SecureFileStreamer.java` | **The Pipeline** — handles reading plaintext input and writing encrypted output to files |

**Encryption flow:**

```
Input File  →  [SecureFileStreamer]  →  [KeyGeneratorUtil]  →  [AESGCMCipher]  →  Encrypted Output File
```

**Decryption flow:**

```
Encrypted File  →  [SecureFileStreamer]  →  [AESGCMCipher]  →  [KeyGeneratorUtil]  →  Decrypted Output File
```

> AES-GCM (Galois/Counter Mode) provides both **confidentiality** (encryption) and **integrity** (authentication tag), making it more secure than plain AES-ECB or AES-CBC modes.

---

## 🔑 AES Overview

**AES (Advanced Encryption Standard)** is a symmetric block cipher adopted by the U.S. National Institute of Standards and Technology (NIST). Key characteristics:

| Property        | Details                          |
|-----------------|----------------------------------|
| Type            | Symmetric block cipher           |
| Key sizes       | 128, 192, or 256 bits            |
| Block size      | 128 bits                         |
| Standard        | FIPS PUB 197                     |
| Common modes    | ECB, CBC, **GCM** ✅ (used here) |

AES operates through multiple rounds of substitution, permutation, and mixing steps to produce ciphertext that is computationally infeasible to reverse without the correct key.

---

## 🛠️ Technologies Used

- **Java** — Core language
- **javax.crypto** — Cipher, SecretKeySpec
- **java.security** — Key management
- **java.util.Base64** — Encoding/decoding encrypted output
- **Eclipse IDE** — Development environment

---

## 🤝 Contributing

Contributions are welcome! To contribute:

1. Fork the repository
2. Create a new branch: `git checkout -b feature/your-feature-name`
3. Make your changes and commit: `git commit -m "Add your message"`
4. Push to the branch: `git push origin feature/your-feature-name`
5. Open a Pull Request

---

## 📄 License

This project is open source. Feel free to use, modify, and distribute it for educational or personal use.

---

> **Author:** [harshraj005](https://github.com/harshraj005)  
> **Repository:** [Java-AES-Encryption-Engine](https://github.com/harshraj005/Java-AES-Encryption-Engine)
