export const toast = {
    show: (title, message, type = 'info', action = null, duration = 5000) => {
        const id = Math.random().toString(36).substring(2, 9);
        const event = new CustomEvent('vortex-toast', {
            detail: { id, title, message, type, action, duration }
        });
        window.dispatchEvent(event);
        return id;
    },
    success: (title, message, action, duration) => toast.show(title, message, 'success', action, duration),
    error: (title, message, action, duration) => toast.show(title, message, 'error', action, duration),
    warning: (title, message, action, duration) => toast.show(title, message, 'warning', action, duration),
    info: (title, message, action, duration) => toast.show(title, message, 'info', action, duration)
};
