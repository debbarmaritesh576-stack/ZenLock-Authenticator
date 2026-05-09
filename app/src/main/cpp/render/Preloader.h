#ifndef AEGIS_PRELOADER_H
#define AEGIS_PRELOADER_H

#include <cstdint>
#include <atomic>
#include <thread>
#include <queue>
#include <mutex>
#include <functional>

struct PreloadTask {
    int pageNumber;
    int priority; // Higher = more urgent
    std::function<void(int)> callback;
    
    bool operator<(const PreloadTask& other) const {
        return priority < other.priority;
    }
};

class Preloader {
public:
    Preloader(int cacheSlots = 3);
    ~Preloader();

    void queuePage(int pageNumber, int priority = 0);
    void setRenderCallback(std::function<void(int)> callback);
    void cancelAll();
    
    bool isPageReady(int pageNumber) const;
    int getCachedPage() const { return cachedPage; }

private:
    std::priority_queue<PreloadTask> taskQueue;
    std::mutex queueMutex;
    std::thread workerThread;
    std::atomic<bool> running;
    std::function<void(int)> renderCallback;
    
    std::vector<int> readyPages;
    int cachedPage;
    int maxSlots;
    
    void worker();
};

#endif