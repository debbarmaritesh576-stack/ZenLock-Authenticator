#ifndef AEGIS_ENCRYPTION_256_H
#define AEGIS_ENCRYPTION_256_H

#include <cstdint>
#include <string>
#include <vector>

class PDFEncryption256 {
public:
    PDFEncryption256();
    ~PDFEncryption256();

    enum class Algorithm { AES_128, AES_256, AES_256_GCM };

    bool encrypt(const uint8_t* input, size_t len, const char* password, 
                 std::vector<uint8_t>& output, Algorithm algo = Algorithm::AES_256_GCM);
    bool decrypt(const uint8_t* input, size_t len, const char* password, 
                 std::vector<uint8_t>& output);
    void setAlgorithm(Algorithm algo);
    bool needsPassword() const;
    bool validatePassword(const char* password, const uint8_t* encryptedData, size_t len);

private:
    Algorithm currentAlgorithm;
    std::vector<uint8_t> encryptionKey;
    std::vector<uint8_t> salt;
    std::vector<uint8_t> iv;
    
    static constexpr size_t SALT_SIZE = 32;
    static constexpr size_t IV_SIZE = 12;
    static constexpr size_t TAG_SIZE = 16;
    static constexpr size_t KEY_SIZE_128 = 16;
    static constexpr size_t KEY_SIZE_256 = 32;
    static constexpr int PBKDF2_ITERATIONS = 100000;
    
    void deriveKey(const char* password, size_t keySize);
    void generateSalt();
    void generateIV();
    bool encryptAes256Gcm(const uint8_t* input, size_t len, std::vector<uint8_t>& output);
    bool decryptAes256Gcm(const uint8_t* input, size_t len, std::vector<uint8_t>& output);
    bool encryptAes256Cbc(const uint8_t* input, size_t len, std::vector<uint8_t>& output);
    bool decryptAes256Cbc(const uint8_t* input, size_t len, std::vector<uint8_t>& output);
};

#endif