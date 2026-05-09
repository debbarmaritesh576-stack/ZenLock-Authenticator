#include "Preloader.h"
#include <chrono>

Preloader::Preloader(int cacheSlots) 
    : running(true), cachedPage(-1), maxSlots(cacheSlots) {
    workerThread = std::thread(&Preloader::worker, this);
}

Preloader::~Preloader() {
    running = false;
    if (workerThread.joinable()) workerThread.join();
}

void Preloader::queuePage(int pageNumber, int priority) {
    std::lock_guard<std::mutex> lock(queueMutex);
    PreloadTask task;
    task.pageNumber = pageNumber;
    task.priority = priority;
    task.callback = renderCallback;
    taskQueue.push(task);
}

void Preloader::setRenderCallback(std::function<void(int)> callback) {
    renderCallback = callback;
}

void Preloader::cancelAll() {
    std::lock_guard<std::mutex> lock(queueMutex);
    while (!taskQueue.empty()) taskQueue.pop();
}

bool Preloader::isPageReady(int pageNumber) const {
    for (int p : readyPages) {
        if (p == pageNumber) return true;
    }
    return false;
}

void Preloader::worker() {
    while (running) {
        std::unique_lock<std::mutex> lock(queueMutex);
        
        if (taskQueue.empty()) {
            lock.unlock();
            std::this_thread::sleep_for(std::chrono::milliseconds(10));
            continue;
        }
        
        PreloadTask task = taskQueue.top();
        taskQueue.pop();
        lock.unlock();
        
        // Render page in background
        if (task.callback) {
            task.callback(task.pageNumber);
        }
        
        readyPages.push_back(task.pageNumber);
        if (readyPages.size() > maxSlots) {
            readyPages.erase(readyPages.begin());
        }
    }
}