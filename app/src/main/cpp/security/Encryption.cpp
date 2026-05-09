#include "Encryption.h"
#include <cstring>
#include <cstdlib>
#include <ctime>
#include <openssl/aes.h>
#include <openssl/sha.h>
#include <openssl/rand.h>

PDFEncryption::PDFEncryption() : algorithm("AES-128") {}
PDFEncryption::~PDFEncryption() {}

void PDFEncryption::setAlgorithm(const char* algo) {
    algorithm = algo;
}

bool PDFEncryption::encrypt(const uint8_t* input, size_t len, 
                             const char* password, std::vector<uint8_t>& output) {
    if (!input || !password) return false;
    
    deriveKey(password);
    
    // Generate IV
    uint8_t iv[16];
    generateIV(iv, 16);
    
    // Setup AES
    AES_KEY aesKey;
    AES_set_encrypt_key(encryptionKey.data(), 128, &aesKey);
    
    // Pad input to 16-byte boundary
    size_t paddedLen = ((len + 15) / 16) * 16;
    std::vector<uint8_t> padded(paddedLen);
    memcpy(padded.data(), input, len);
    size_t padVal = paddedLen - len;
    for (size_t i = len; i < paddedLen; i++) padded[i] = padVal;
    
    output.resize(16 + paddedLen);
    memcpy(output.data(), iv, 16);
    
    // CBC encrypt
    uint8_t prevBlock[16];
    memcpy(prevBlock, iv, 16);
    
    for (size_t i = 0; i < paddedLen; i += 16) {
        for (int j = 0; j < 16; j++) padded[i + j] ^= prevBlock[j];
        AES_encrypt(padded.data() + i, output.data() + 16 + i, &aesKey);
        memcpy(prevBlock, output.data() + 16 + i, 16);
    }
    
    return true;
}

bool PDFEncryption::decrypt(const uint8_t* input, size_t len, 
                             const char* password, std::vector<uint8_t>& output) {
    if (len < 32 || !password) return false;
    
    deriveKey(password);
    
    // Extract IV
    uint8_t iv[16];
    memcpy(iv, input, 16);
    
    AES_KEY aesKey;
    AES_set_decrypt_key(encryptionKey.data(), 128, &aesKey);
    
    size_t dataLen = len - 16;
    output.resize(dataLen);
    
    uint8_t prevBlock[16];
    memcpy(prevBlock, iv, 16);
    
    for (size_t i = 0; i < dataLen; i += 16) {
        AES_decrypt(input + 16 + i, output.data() + i, &aesKey);
        for (int j = 0; j < 16; j++) output[i + j] ^= prevBlock[j];
        memcpy(prevBlock, input + 16 + i, 16);
    }
    
    // Remove PKCS7 padding
    uint8_t pad = output.back();
    if (pad <= 16) output.resize(output.size() - pad);
    
    return true;
}

void PDFEncryption::deriveKey(const char* password) {
    salt.resize(32);
    RAND_bytes(salt.data(), 32);
    
    encryptionKey.resize(32);
    PKCS5_PBKDF2_HMAC(password, strlen(password), 
                       salt.data(), 32, 10000, 
                       EVP_sha256(), 32, encryptionKey.data());
}

void PDFEncryption::generateIV(uint8_t* iv, size_t len) {
    RAND_bytes(iv, len);
}

bool PDFEncryption::needsPassword() const { return true; }
bool PDFEncryption::validatePassword(const char* password) { return true; }