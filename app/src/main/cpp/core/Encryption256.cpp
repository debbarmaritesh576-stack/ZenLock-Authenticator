#include "Encryption256.h"
#include <cstring>
#include <openssl/aes.h>
#include <openssl/evp.h>
#include <openssl/rand.h>
#include <openssl/sha.h>

PDFEncryption256::PDFEncryption256() : currentAlgorithm(Algorithm::AES_256_GCM) {
    encryptionKey.resize(KEY_SIZE_256);
    salt.resize(SALT_SIZE);
    iv.resize(IV_SIZE);
}

PDFEncryption256::~PDFEncryption256() {
    memset(encryptionKey.data(), 0, encryptionKey.size());
    memset(salt.data(), 0, salt.size());
    memset(iv.data(), 0, iv.size());
}

void PDFEncryption256::setAlgorithm(Algorithm algo) {
    currentAlgorithm = algo;
    encryptionKey.resize(algo == Algorithm::AES_128 ? KEY_SIZE_128 : KEY_SIZE_256);
}

void PDFEncryption256::generateSalt() { RAND_bytes(salt.data(), SALT_SIZE); }
void PDFEncryption256::generateIV() { RAND_bytes(iv.data(), IV_SIZE); }

void PDFEncryption256::deriveKey(const char* password, size_t keySize) {
    generateSalt();
    PKCS5_PBKDF2_HMAC(password, strlen(password), salt.data(), SALT_SIZE,
                      PBKDF2_ITERATIONS, EVP_sha512(), keySize, encryptionKey.data());
}

bool PDFEncryption256::encrypt(const uint8_t* input, size_t len, const char* password,
                                std::vector<uint8_t>& output, Algorithm algo) {
    if (!input || !password || len == 0) return false;
    currentAlgorithm = algo;
    encryptionKey.resize(algo == Algorithm::AES_128 ? KEY_SIZE_128 : KEY_SIZE_256);
    deriveKey(password, encryptionKey.size());
    
    switch (algo) {
        case Algorithm::AES_256_GCM: return encryptAes256Gcm(input, len, output);
        case Algorithm::AES_256: return encryptAes256Cbc(input, len, output);
        case Algorithm::AES_128: encryptionKey.resize(KEY_SIZE_128); return encryptAes256Cbc(input, len, output);
    }
    return false;
}

bool PDFEncryption256::encryptAes256Gcm(const uint8_t* input, size_t len, std::vector<uint8_t>& output) {
    generateIV();
    EVP_CIPHER_CTX* ctx = EVP_CIPHER_CTX_new();
    if (!ctx) return false;
    
    int outLen, totalLen = 0;
    output.resize(SALT_SIZE + IV_SIZE + len + TAG_SIZE);
    memcpy(output.data(), salt.data(), SALT_SIZE);
    memcpy(output.data() + SALT_SIZE, iv.data(), IV_SIZE);
    
    uint8_t* ciphertext = output.data() + SALT_SIZE + IV_SIZE;
    EVP_EncryptInit_ex(ctx, EVP_aes_256_gcm(), nullptr, nullptr, nullptr);
    EVP_CIPHER_CTX_ctrl(ctx, EVP_CTRL_GCM_SET_IVLEN, IV_SIZE, nullptr);
    EVP_EncryptInit_ex(ctx, nullptr, nullptr, encryptionKey.data(), iv.data());
    
    if (EVP_EncryptUpdate(ctx, ciphertext, &outLen, input, len) != 1) { EVP_CIPHER_CTX_free(ctx); return false; }
    totalLen = outLen;
    if (EVP_EncryptFinal_ex(ctx, ciphertext + totalLen, &outLen) != 1) { EVP_CIPHER_CTX_free(ctx); return false; }
    totalLen += outLen;
    
    if (EVP_CIPHER_CTX_ctrl(ctx, EVP_CTRL_GCM_GET_TAG, TAG_SIZE, ciphertext + totalLen) != 1) { EVP_CIPHER_CTX_free(ctx); return false; }
    output.resize(SALT_SIZE + IV_SIZE + totalLen + TAG_SIZE);
    EVP_CIPHER_CTX_free(ctx);
    return true;
}

bool PDFEncryption256::decryptAes256Gcm(const uint8_t* input, size_t len, std::vector<uint8_t>& output) {
    if (len < SALT_SIZE + IV_SIZE + TAG_SIZE + 16) return false;
    memcpy(salt.data(), input, SALT_SIZE);
    memcpy(iv.data(), input + SALT_SIZE, IV_SIZE);
    
    const uint8_t* ciphertext = input + SALT_SIZE + IV_SIZE;
    size_t cipherLen = len - SALT_SIZE - IV_SIZE - TAG_SIZE;
    const uint8_t* tag = input + len - TAG_SIZE;
    
    EVP_CIPHER_CTX* ctx = EVP_CIPHER_CTX_new();
    if (!ctx) return false;
    
    int outLen, totalLen = 0;
    output.resize(cipherLen + 16);
    
    EVP_DecryptInit_ex(ctx, EVP_aes_256_gcm(), nullptr, nullptr, nullptr);
    EVP_CIPHER_CTX_ctrl(ctx, EVP_CTRL_GCM_SET_IVLEN, IV_SIZE, nullptr);
    EVP_DecryptInit_ex(ctx, nullptr, nullptr, encryptionKey.data(), iv.data());
    
    if (EVP_DecryptUpdate(ctx, output.data(), &outLen, ciphertext, cipherLen) != 1) { EVP_CIPHER_CTX_free(ctx); return false; }
    totalLen = outLen;
    if (EVP_CIPHER_CTX_ctrl(ctx, EVP_CTRL_GCM_SET_TAG, TAG_SIZE, (void*)tag) != 1) { EVP_CIPHER_CTX_free(ctx); return false; }
    
    int ret = EVP_DecryptFinal_ex(ctx, output.data() + totalLen, &outLen);
    totalLen += outLen;
    EVP_CIPHER_CTX_free(ctx);
    output.resize(totalLen);
    return ret > 0;
}

bool PDFEncryption256::encryptAes256Cbc(const uint8_t* input, size_t len, std::vector<uint8_t>& output) {
    generateIV();
    size_t paddedLen = ((len + 16) / 16) * 16;
    std::vector<uint8_t> padded(paddedLen);
    memcpy(padded.data(), input, len);
    uint8_t padVal = paddedLen - len;
    for (size_t i = len; i < paddedLen; i++) padded[i] = padVal;
    
    output.resize(SALT_SIZE + IV_SIZE + paddedLen);
    memcpy(output.data(), salt.data(), SALT_SIZE);
    memcpy(output.data() + SALT_SIZE, iv.data(), IV_SIZE);
    
    AES_KEY aesKey;
    AES_set_encrypt_key(encryptionKey.data(), 256, &aesKey);
    uint8_t* out = output.data() + SALT_SIZE + IV_SIZE;
    uint8_t prevBlock[16];
    memcpy(prevBlock, iv.data(), 16);
    
    for (size_t i = 0; i < paddedLen; i += 16) {
        for (int j = 0; j < 16; j++) padded[i + j] ^= prevBlock[j];
        AES_encrypt(padded.data() + i, out + i, &aesKey);
        memcpy(prevBlock, out + i, 16);
    }
    return true;
}

bool PDFEncryption256::decryptAes256Cbc(const uint8_t* input, size_t len, std::vector<uint8_t>& output) {
    if (len < SALT_SIZE + IV_SIZE + 16) return false;
    memcpy(salt.data(), input, SALT_SIZE);
    memcpy(iv.data(), input + SALT_SIZE, IV_SIZE);
    
    size_t dataLen = len - SALT_SIZE - IV_SIZE;
    output.resize(dataLen);
    
    AES_KEY aesKey;
    AES_set_decrypt_key(encryptionKey.data(), 256, &aesKey);
    uint8_t prevBlock[16];
    memcpy(prevBlock, iv.data(), 16);
    
    for (size_t i = 0; i < dataLen; i += 16) {
        AES_decrypt(input + SALT_SIZE + IV_SIZE + i, output.data() + i, &aesKey);
        for (int j = 0; j < 16; j++) output[i + j] ^= prevBlock[j];
        memcpy(prevBlock, input + SALT_SIZE + IV_SIZE + i, 16);
    }
    
    uint8_t pad = output.back();
    if (pad <= 16) output.resize(output.size() - pad);
    return true;
}

bool PDFEncryption256::decrypt(const uint8_t* input, size_t len, const char* password, std::vector<uint8_t>& output) {
    if (!input || !password || len < SALT_SIZE + IV_SIZE + TAG_SIZE) return false;
    memcpy(salt.data(), input, SALT_SIZE);
    PKCS5_PBKDF2_HMAC(password, strlen(password), salt.data(), SALT_SIZE,
                      PBKDF2_ITERATIONS, EVP_sha512(), encryptionKey.size(), encryptionKey.data());
    return (len >= SALT_SIZE + IV_SIZE + TAG_SIZE) ? decryptAes256Gcm(input, len, output) 
                                                    : decryptAes256Cbc(input, len, output);
}

bool PDFEncryption256::validatePassword(const char* password, const uint8_t* data, size_t len) {
    if (!password || !data || len == 0) return false;
    std::vector<uint8_t> dummy;
    return decrypt(data, len, password, dummy);
}