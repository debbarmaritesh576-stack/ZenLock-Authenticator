#ifndef AEGIS_VULKAN_RENDERER_H
#define AEGIS_VULKAN_RENDERER_H

#ifdef __ANDROID__
#include <vulkan/vulkan.h>
#include <android/native_window.h>

class VulkanRenderer {
public:
    VulkanRenderer();
    ~VulkanRenderer();

    bool initialize(ANativeWindow* window);
    void render();
    void cleanup();
    
    bool isSupported() const;

private:
    VkInstance instance;
    VkPhysicalDevice physicalDevice;
    VkDevice device;
    VkQueue graphicsQueue;
    VkSurfaceKHR surface;
    VkSwapchainKHR swapchain;
    VkCommandPool commandPool;
    VkRenderPass renderPass;
    
    bool initialized;
    
    bool createInstance();
    bool pickPhysicalDevice();
    bool createLogicalDevice();
    bool createSwapchain();
    bool createRenderPass();
};

#endif // __ANDROID__
#endif // AEGIS_VULKAN_RENDERER_H